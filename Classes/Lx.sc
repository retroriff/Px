Lx : Px {
  classvar <bufs;
  classvar <channelCount;
  classvar <channelNames;
  classvar <folderPath;
  classvar <monoBufs;
  classvar <tracks;
  classvar <mutedChannels;
  classvar <shuffleAmounts;
  classvar <soloedChannels;
  classvar <window;
  classvar <isPlaying;
  classvar pausedByState;
  classvar <meterLevels;
  classvar meterFunc;
  classvar <meterViews;
  classvar <meterRoutine;

  *initClass {
    bufs = Dictionary.new;
    channelCount = 0;
    channelNames = Array.new;
    monoBufs = Dictionary.new;
    mutedChannels = IdentitySet.new;
    shuffleAmounts = Array.new;
    soloedChannels = IdentitySet.new;
    tracks = Array.new;
    isPlaying = false;
    pausedByState = IdentitySet.new;
    meterLevels = Array.new;
    meterViews = Array.new;

    meterFunc = OSCFunc({ |msg|
      var channel = msg[2].asInteger;
      var peakL = msg[3];
      var peakR = msg[5];

      if (channel >= 0 and: { channel < meterLevels.size })
      { meterLevels[channel] = max(peakL, peakR) };
    }, '/lxMeter');

    CmdPeriod.add {
      this.prStopAll;
    };

    ^super.initClass;
  }

  *loadSamples { |path, verbose = true|
    var root = PathName(path.standardizePath);
    var folders;

    if (File.exists(root.fullPath).not)
    { ^("🔴 Path does not exist:" + root.fullPath) };

    folders = root.entries.select { |entry| entry.isFolder };

    if (folders.isEmpty)
    { ^("🔴 No subfolders found in:" + root.fullPath) };

    bufs = Dictionary.new;
    monoBufs = Dictionary.new;
    channelNames = Array.new;
    tracks = Array.new;
    folderPath = path;

    folders.do { |folder|
      var audioFiles = folder.files.select { |file|
        file.extension.toLower == "wav" or: { file.extension.toLower == "aiff" }
      };

      if (audioFiles.notEmpty) {
        var index = bufs.size;
        bufs[index] = audioFiles.collect { |file|
          Buffer.read(Server.default, file.fullPath)
        };
        monoBufs[index] = audioFiles.collect { |file|
          Buffer.readChannel(Server.default, file.fullPath, channels: [0])
        };
        channelNames = channelNames.add(folder.folderName);
        tracks = tracks.add(0);
      };
    };

    channelCount = bufs.size;
    shuffleAmounts = Array.fill(channelCount, { 1 });
    meterLevels = Array.fill(channelCount, { 0 });

    if (verbose)
    { ^("🔄 Lx with" + channelCount + "channels") };
  }

  *amp { |channel, value = 0.3|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\amp] = value;
    this.prCreatePattern(channel);
  }

  *buf { |channel, index|
    if (this.prValidateChannel(channel).notNil) { ^nil };

    index = index.clip(0, bufs[channel].size - 1);
    tracks[channel] = index;
    this.prCreatePattern(channel);
    this.prRefreshGui;
  }

  *dur { |channel, value = 4|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\dur] = value;
    last[this.prCreateId(channel)].removeAt(\beats);
    this.prCreatePattern(channel);
  }

  *next { |channel|
    if (this.prValidateChannel(channel).notNil) { ^nil };

    tracks[channel] = (tracks[channel] + 1) % bufs[channel].size;
    this.prCreatePattern(channel);
    this.prRefreshGui;
  }

  *play { |channel, fadeTime|
    if (bufs.isEmpty)
    { ^this.prPrint("🔴 No samples loaded. Call Lx.loadSamples first") };

    isPlaying = true;

    if (channel.notNil) {
      if (this.prValidateChannel(channel).notNil) { ^nil };

      this.prCreatePattern(channel, fadeTime);
    } {
      channelCount.do { |i|
        this.prCreatePattern(i, fadeTime);
      };
    };
  }

  *prev { |channel|
    if (this.prValidateChannel(channel).notNil) { ^nil };

    tracks[channel] = (tracks[channel] - 1) % bufs[channel].size;
    this.prCreatePattern(channel);
    this.prRefreshGui;
  }

  *trim { |channel, value|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\trim] = value;
    this.prCreatePattern(channel);
  }

  *start { |channel, value = 0|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\start] = value;
    this.prCreatePattern(channel);
  }

  *shuffle { |channel, amount|
    if (isPlaying.not)
    { isPlaying = true };

    if (channel.notNil) {
      this.prShuffleChannel(channel, amount ?? shuffleAmounts[channel]);
    } {
      channelCount.do { |i|
        this.prShuffleChannel(i, amount ?? shuffleAmounts[i]);
      };
    };

    this.prRefreshGui;
  }

  *prShuffleChannel { |i, amount|
    var currentDur, currentHz, densitySteps, durSteps, grainDurSteps, id, lengthSteps, spreadSteps;

    durSteps = [0.125, 0.25, 0.5, 1, 2, 4, 8, 16];
    densitySteps = [0.5, 1, 2, 3, 4, 6, 8, 12, 16];
    grainDurSteps = [0.01, 0.03, 0.05, 0.1, 0.15, 0.2, 0.3];
    lengthSteps = [0.125, 0.25, 0.5, 1, 2, 4, 8];
    spreadSteps = [0, 0, 0.02, 0.05, 0.1, 0.3];

    if (amount <= 0)
    { ^nil };

    if (this.prValidateChannel(i).notNil) { ^nil };

    id = this.prCreateId(i);

    if (amount > 0.7) {
      if (amount > 0.85)
      { tracks[i] = bufs[i].size.rand }
      { tracks[i] = (tracks[i] + [-1, 1].choose) % bufs[i].size };
    };

    if (last[id].isNil)
    { last[id] = () };

    if (last[id][\start].notNil)
    { last[id][\start] = (last[id][\start] + (rrand(-1.0, 1.0) * amount)).clip(0, 1) }
    { last[id][\start] = 1.0.rand * amount };

    currentDur = last[id][\dur] ?? 4;
    last[id][\dur] = this.prPickNearby(durSteps, currentDur.abs, amount);

    if (amount >= 0.5)
    { last[id][\dur] = last[id][\dur] * [-1, 1].choose }
    { last[id][\dur] = last[id][\dur] * currentDur.sign };

    currentHz = last[id][\density] ?? 10;
    last[id][\density] = this.prPickNearby(densitySteps, currentHz / TempoClock.default.tempo, amount) * TempoClock.default.tempo;
    last[id][\grainDur] = this.prPickNearby(grainDurSteps, last[id][\grainDur] ?? 0.1, amount);

    if (last[id][\scatter].notNil)
    { last[id][\scatter] = (last[id][\scatter] + (rrand(-1.0, 1.0) * amount)).clip(0, 1) }
    { last[id][\scatter] = 1.0.rand * amount };

    last[id][\spread] = this.prPickNearby(spreadSteps, last[id][\spread] ?? 0, amount);

    last[id][\length] = this.prPickNearby(lengthSteps, last[id][\length] ?? 4, amount).max(last[id][\dur].abs);

    this.prCreatePattern(i);
  }

  *prPickNearby { |steps, currentVal, amount|
    var currentIndex, maxIdx, minIdx, windowSize;

    currentIndex = steps.collect { |s| (s - currentVal).abs }.minIndex;
    windowSize = (steps.size * amount).ceil.asInteger.max(1);
    minIdx = (currentIndex - windowSize).max(0);
    maxIdx = (currentIndex + windowSize).min(steps.size - 1);

    ^steps[rrand(minIdx, maxIdx)];
  }

  *stop { |channel|
    if (channel.notNil) {
      var id = this.prCreateId(channel);

      if (channel < meterLevels.size)
      { meterLevels[channel] = 0 };

      Fx.remove(id);
      Px.stop(id);
    } {
      this.prStopAll;
    };
  }

  *vol { |value|
    if (value.isNil)
    { ^this.prPrint("🟡 Provide a volume value") };

    channelCount.do { |i|
      var id = this.prCreateId(i);

      if (last[id].notNil) {
        last[id][\amp] = value;
        this.prCreatePattern(i);
      };
    };
  }

  *density { |channel, value = 10|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\density] = value;
    this.prCreatePattern(channel);
  }

  *grainDur { |channel, value = 0.1|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\grainDur] = value;
    this.prCreatePattern(channel);
  }

  *scatter { |channel, value = 0|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\scatter] = value;
    this.prCreatePattern(channel);
  }

  *spread { |channel, value = 0|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\spread] = value;
    this.prCreatePattern(channel);
  }

  *length { |channel, value|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\length] = value;
    this.prCreatePattern(channel);
  }

  *freeze { |channel, value = 0|
    if (this.prValidatePlayingChannel(channel).notNil) { ^nil };

    last[this.prCreateId(channel)][\freeze] = value;
    this.prCreatePattern(channel);
  }

  *prValidateChannel { |channel|
    if (channel.isNil)
    { ^this.prPrint("🟡 Provide a channel number") };

    if (bufs[channel].isNil)
    { ^this.prPrint("🔴 Channel" + channel + "not found") };

    ^nil;
  }

  *prValidatePlayingChannel { |channel|
    var error = this.prValidateChannel(channel);

    if (error.notNil)
    { ^error };

    if (last[this.prCreateId(channel)].isNil)
    { ^this.prPrint("🟡 Channel" + channel + "is not playing") };

    ^nil;
  }

  *prRefreshGui {
    if (window.notNil and: { window.isClosed.not })
    { { this.gui }.defer };
  }

  *prCreateId { |channel|
    ^("lx" ++ channel).asSymbol;
  }

  *prCreatePattern { |channel, fadeTime|
    var id = this.prCreateId(channel);
    var existing = last[id];
    var dur = existing !? { existing[\beats] ?? existing[\dur] } ?? 4;
    var pattern = (
      amp: existing !? { existing[\amp] } ?? 0.3,
      dur: dur,
      id: id,
      lx: true,
    );

    pattern[\grain] = monoBufs[channel][tracks[channel]];
    pattern[\density] = existing !? { existing[\density] } ?? 10;
    pattern[\grainDur] = existing !? { existing[\grainDur] } ?? 0.1;
    pattern[\scatter] = existing !? { existing[\scatter] } ?? 0;
    pattern[\spread] = existing !? { existing[\spread] } ?? 0;
    pattern[\freeze] = existing !? { existing[\freeze] } ?? 0;

    if (existing.notNil and: { existing[\start].notNil })
    { pattern[\start] = existing[\start] };

    if (existing.notNil and: { existing[\length].notNil }) {
      if (dur < 0) { pattern[\rate] = -1 };
      pattern[\beats] = dur.abs;
      pattern[\dur] = existing[\length];
      pattern[\length] = existing[\length];
    };

    if (fadeTime.notNil)
    { pattern[\fade] = [\in, fadeTime] };

    super.new(pattern);
    this.prApplyMuteState(channel);

    fork {
      Server.default.sync;

      Ndef(id).filter(100, { |in|
        SendPeakRMS.kr(in, 20, 0.3, "/lxMeter", channel);
        in;
      });
    };
  }

  *prApplyMuteState { |channel|
    var id = this.prCreateId(channel);
    var shouldBeSilent = mutedChannels.includes(channel) or: {
      soloedChannels.notEmpty and: { soloedChannels.includes(channel).not }
    };

    if (shouldBeSilent) {
      pausedByState.add(channel);
      Ndef(id).pause;
    } {
      if (pausedByState.includes(channel)) {
        pausedByState.remove(channel);
        Ndef(id).resume;
      };
    };
  }

  *prStopAll {
    isPlaying = false;
    pausedByState = IdentitySet.new;
    meterLevels = Array.fill(channelCount.max(0), { 0 });

    last.copy do: { |pattern|
      if (pattern[\lx] == true) {
        Fx.remove(pattern[\id]);
        Px.stop(pattern[\id]);
      };
    };
  }
}
