Lx : Px {
  classvar <bufs;
  classvar <channelCount;
  classvar <channelNames;
  classvar <folderPath;
  classvar <tracks;
  classvar <mutedChannels;
  classvar <soloedChannels;
  classvar <window;

  *initClass {
    bufs = Dictionary.new;
    channelCount = 0;
    channelNames = Array.new;
    mutedChannels = IdentitySet.new;
    soloedChannels = IdentitySet.new;
    tracks = Array.new;

    CmdPeriod.add {
      this.prStopAll;
    };

    ^super.initClass;
  }

  *loadSamples { |path|
    var root = PathName(path.standardizePath);
    var folders;

    if (File.exists(root.fullPath).not)
    { ^this.prPrint("🔴 Path does not exist:" + root.fullPath) };

    folders = root.entries.select { |entry| entry.isFolder };

    if (folders.isEmpty)
    { ^this.prPrint("🔴 No subfolders found in:" + root.fullPath) };

    bufs = Dictionary.new;
    channelNames = Array.new;
    tracks = Array.new;
    folderPath = path;

    folders.do { |folder|
      var audioFiles = folder.files.select { |file|
        file.extension.toLower == "wav" or: { file.extension.toLower == "aiff" }
      };

      if (audioFiles.notEmpty) {
        bufs[bufs.size] = audioFiles.collect { |file|
          Buffer.read(Server.default, file.fullPath)
        };
        channelNames = channelNames.add(folder.folderName);
        tracks = tracks.add(0);
      };
    };

    channelCount = bufs.size;
    this.prPrint("🔄 Lx with" + channelCount + "channels");
  }

  *amp { |channel, value = 0.3|
    if (channel.isNil)
    { ^this.prPrint("🟡 Provide a channel number") };

    if (last[this.prCreateId(channel)].isNil)
    { ^this.prPrint("🟡 Channel" + channel + "is not playing") };

    last[this.prCreateId(channel)][\amp] = value;
    this.prCreatePattern(channel);
  }

  *buf { |channel, index|
    if (channel.isNil)
    { ^this.prPrint("🟡 Provide a channel number") };

    if (bufs[channel].isNil)
    { ^this.prPrint("🔴 Channel" + channel + "not found") };

    index = index.clip(0, bufs[channel].size - 1);
    tracks[channel] = index;
    this.prCreatePattern(channel);
    this.prRefreshGui;
  }

  *dur { |channel, value = 4|
    if (channel.isNil)
    { ^this.prPrint("🟡 Provide a channel number") };

    if (last[this.prCreateId(channel)].isNil)
    { ^this.prPrint("🟡 Channel" + channel + "is not playing") };

    last[this.prCreateId(channel)][\dur] = value;
    this.prCreatePattern(channel);
  }

  *next { |channel|
    if (channel.isNil)
    { ^this.prPrint("🟡 Provide a channel number") };

    if (bufs[channel].isNil)
    { ^this.prPrint("🔴 Channel" + channel + "not found") };

    tracks[channel] = (tracks[channel] + 1) % bufs[channel].size;
    this.prCreatePattern(channel);
    this.prRefreshGui;
  }

  *play { |channel, fadeTime|
    if (bufs.isEmpty)
    { ^this.prPrint("🔴 No samples loaded. Call Lx.loadSamples first") };

    if (channel.notNil) {

      if (bufs[channel].isNil)
      { ^this.prPrint("🔴 Channel" + channel + "not found") };

      this.prCreatePattern(channel, fadeTime);
    } {
      channelCount.do { |i|
        this.prCreatePattern(i, fadeTime);
      };
    };
  }

  *prev { |channel|
    if (channel.isNil)
    { ^this.prPrint("🟡 Provide a channel number") };

    if (bufs[channel].isNil)
    { ^this.prPrint("🔴 Channel" + channel + "not found") };

    tracks[channel] = (tracks[channel] - 1) % bufs[channel].size;
    this.prCreatePattern(channel);
    this.prRefreshGui;
  }

  *rate { |channel, value = 1|
    if (channel.isNil)
    { ^this.prPrint("🟡 Provide a channel number") };

    if (last[this.prCreateId(channel)].isNil)
    { ^this.prPrint("🟡 Channel" + channel + "is not playing") };

    last[this.prCreateId(channel)][\rate] = value;
    this.prCreatePattern(channel);
  }

  *start { |channel, value = 0|
    if (channel.isNil)
    { ^this.prPrint("🟡 Provide a channel number") };

    if (last[this.prCreateId(channel)].isNil)
    { ^this.prPrint("🟡 Channel" + channel + "is not playing") };

    last[this.prCreateId(channel)][\start] = value;
    this.prCreatePattern(channel);
  }

  *shuffle {
    channelCount.do { |i|
      var id = this.prCreateId(i);

      tracks[i] = bufs[i].size.rand;

      if (last[id].notNil) {
        last[id][\rate] = rrand(-1.0, 2.0);
        last[id][\start] = 1.0.rand;
        last[id][\dur] = rrand(0.25, 16.0);
      };

      this.prCreatePattern(i);
    };

    this.prRefreshGui;
  }

  *stop { |channel|
    if (channel.notNil) {
      var id = this.prCreateId(channel);
      Fx.prClearProxy(id);
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
    var pattern = (
      amp: existing !? { existing[\amp] } ?? 0.3,
      dur: existing !? { existing[\dur] } ?? 4,
      id: id,
      loop: bufs[channel][tracks[channel]],
      lx: true,
    );

    if (existing.notNil and: { existing[\rate].notNil })
    { pattern[\rate] = existing[\rate] };

    if (existing.notNil and: { existing[\start].notNil })
    { pattern[\start] = existing[\start] };

    if (fadeTime.notNil)
    { pattern[\fade] = [\in, fadeTime] };

    super.new(pattern);
  }

  *prStopAll {
    last.copy do: { |pattern|

      if (pattern[\lx] == true) {
        Fx.prClearProxy(pattern[\id]);
        Px.stop(pattern[\id]);
      };
    };
  }
}
