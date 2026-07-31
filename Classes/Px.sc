/*
TODO: Midinote notation in uppercase return chords
TODO: When used in a group, Number solo method mutes new patterns already played.
Example on Mastegots.scd
*/
Px {
  classvar <>chorusPatterns;
  classvar <>colors;
  classvar <drumMachinesPath;
  classvar <>last;
  classvar <>lastFormatted;
  classvar <meterFunc;
  classvar <meterIdMap;
  classvar <meterLevels;
  classvar <meterNextId;
  classvar <meterRoutine;
  classvar <meterViews;
  classvar <midiClient;
  classvar <>midiHoldedNotes;
  classvar <midiOut;
  classvar <>mutedPatterns;
  classvar <>ndefList;
  classvar <>patternState;
  classvar <patternViews;
  classvar <>pausedPatterns;
  classvar <>quant;
  classvar <samplesDict;
  classvar <samplesPath;
  classvar <seeds;
  classvar <shuffleHistory;
  classvar <>window;
  classvar <windowWidth;
  classvar <windowHeight;

  *initClass {
    chorusPatterns = Dictionary.new;
    colors = Dictionary.new;
    last = Dictionary.new;
    lastFormatted = Dictionary.new;
    meterIdMap = Dictionary.new;
    meterLevels = Dictionary.new;
    meterNextId = 0;
    midiHoldedNotes = Dictionary.new;
    patternViews = Dictionary.new;

    meterFunc = OSCFunc({ |msg|
      var meterId = msg[2].asInteger;
      var peakL = msg[3];
      var peakR = msg[5];
      var patternId = meterIdMap[meterId];

      if (patternId.notNil)
      { meterLevels[patternId] = max(peakL, peakR) };
    }, '/pxMeter');

    mutedPatterns = Dictionary.new;
    ndefList = Dictionary.new;
    pausedPatterns = IdentitySet.new;
    quant = 4;
    seeds = Dictionary.new;
    shuffleHistory = Dictionary.new;
    windowWidth = 68;
    windowHeight = 350.min(Window.screenBounds.height / 4);

    CmdPeriod.add { this.clear };

    ServerBoot.add {
      this.listen;
      this.loadSynthDefs;
      thisProcess.interpreter.t = TempoClock.default.tempo;
    };
  }

  *new { |newPattern|
    var pattern, pdef, playList, isNewNdef;

    this.prInitializeDictionaries(newPattern);
    this.prHandleSoloPattern(newPattern);

    pattern = this.prCreateBufInstruments(newPattern);
    pattern = this.prCreateInstrument(pattern);
    pattern = this.prCreateLoops(pattern);
    pattern = this.prCreateAmp(pattern);
    pattern = this.prCreateDur(pattern);
    pattern = this.prCreateBeatRest(pattern);
    pattern = this.prCreatePan(pattern);
    pattern = this.prCreateDegrees(pattern);
    pattern = this.prCreateOctaves(pattern);
    pattern = this.prCreateMidi(pattern);

    isNewNdef = ndefList[pattern[\id]].isNil;
    pdef = this.prCreatePdef(pattern);
    playList = this.prCreatePlayList(pattern[\id], pdef);

    if (Ndef(\px).isPlaying.not)
    { Ndef(\px).quant_(quant).play };

    if (isNewNdef)
    { Ndef(\px)[0] = { Mix.new(playList.values) } };

    if (isNewNdef and: { pattern[\chan].isNil }) {
      var meterId = meterNextId;
      meterNextId = meterNextId + 1;
      meterIdMap[meterId] = pattern[\id];

      fork {
        Server.default.sync;

        Ndef(pattern[\id]).filter(100, { |in|
          SendPeakRMS.kr(in, 20, 0.3, "/pxMeter", meterId);
          in;
        });
      };
    };

    lastFormatted[newPattern[\id]] = pattern;

    this.prRemoveFinitePatternFromLast(pattern);
    this.prAutoRefreshGui;
  }

  *prCreateAmp { |pattern|
    var amp = pattern[\amp] ?? 0.3;

    if (amp.isKindOf(Pattern)
      and: { pattern[\beat].notNil or: { pattern[\fill].notNil } })
    {
      amp = this.prExtractAmpMax(amp);
      pattern[\amp] = amp;
    };

    if (pattern[\beat].notNil)
    { amp = this.prCreateRhythmBeat(amp, pattern) };

    if (pattern[\fill].notNil)
    { amp = this.prCreateFillFromBeat(amp, pattern) };

    pattern[\amp] = amp;

    if (pattern[\amp].isArray) {
      var repeats = pattern[\repeat] ?? inf;
      pattern[\amp] = Pseq(pattern[\amp], repeats);
    };

    ^pattern;
  }

  *prExtractAmpMax { |amp|
    if (amp.isKindOf(Pwhite)) { ^amp.hi };

    if (amp.isKindOf(Pattern) and: { amp.respondsTo(\list) }) {
      ^amp.list.reject { |x| x.isKindOf(Rest) }.collect { |x|
        this.prExtractAmpMax(x)
      }.maxItem;
    };

    ^amp;
  }

  *prCreateChop { |pattern, pbindef|
    if (pattern[\chop].isArray) {
      var dur = pattern[\chop][0];
      var drop = pattern[\chop][1];

      if (dur != 0 and: (dur != Nil)) {
        if (pattern[\instrument] == \loop or: { pattern[\instrument] == \grainLoop }) {
          pbindef = Pbindf(pbindef,
            \beats, pattern[\beats] ?? pattern[\dur],
            \dur, dur
          );
        };

        pbindef = Pseq([
          Pfindur(dur.max(0.25), Pdrop(drop, pbindef))
        ], inf);
      };
    };

    ^pbindef;
  }

  *prCreateDur { |pattern|
    var dur = pattern[\dur];

    if (dur.isNil or: (dur == 0))
    { dur = Pseq([8], pattern[\repeat] ?? 1) };

    if (dur.isArray) {
      var containsString = dur any: { |item| item.isString };
      dur = containsString.if { 1 } { Pseq(dur, inf) };
    };

    if (dur.isString)
    { dur = 1 };

    if (pattern[\euclid].notNil)
    { dur = Pbjorklund2(pattern[\euclid][0], pattern[\euclid][1]) * dur };

    pattern[\dur] = dur;

    ^this.prHumanize(pattern);
  }

  *prCreateInstrument { |pattern|
    if (pattern[\instrument].isArray) {
      pattern[\instrument] = Pseq(pattern[\instrument], pattern[\repeat] ?? inf);
    };

    ^pattern;
  }

  *prCreateFade { |pbindef, fade|
    var defaultFadeTime = 16;
    var direction, fadeTime;

    if (fade.isNil)
    { ^pbindef };

    if (fade.isArray) {
      direction = fade[0];
      fadeTime = fade[1];
    } {
      direction = fade;
      fadeTime = defaultFadeTime;
    };

    if (direction == \in)
    { ^PfadeIn(pbindef, fadeTime) }
    { ^PfadeOut(pbindef, fadeTime) };
  }

  *prCreatePan { |pattern|
    pattern[\pan] = switch (pattern[\pan].asSymbol)

    { \rand }
    { Pwhite(-0.6, 0.6, inf) }

    { \rotate }
    { Pwalk((0..10).normalize(-1, 1), 1, Pseq([1, -1], inf), startPos: 5) }

    { pattern[\pan] };

    ^pattern;
  }

  *prCreatePdef { |pattern|
    var pbindef;
    var stopBeats = pattern[\stop];
    var bindPattern = pattern.copy;

    bindPattern.removeAt(\repeat);
    bindPattern.removeAt(\stop);

    pbindef = Pbind(*bindPattern.asPairs);

    if (pattern[\midiControl] != 1)
    { pbindef = this.prCreateFade(pbindef, pattern[\fade]) };

    pbindef = this.prCreateChop(pattern, pbindef);

    if (stopBeats.notNil)
    { pbindef = Pfindur(stopBeats, pbindef) };

    ^pbindef = Pdef(pattern[\id], pbindef).quant_(quant);
  }

  *prHandleSoloPattern { |pattern|
    if (pattern[\solo] == true) {
      pattern.removeAt(\solo);
      last[pattern[\id]].removeAt(\solo);

      if (pattern[\dx] == true)
      { ^Dx.solo(pattern[\id]) }
      { ^Px.solo(pattern[\id]) };
    };
  }

  *prHumanize { |pattern|
    if (pattern[\human].notNil) {
      var delay = pattern[\human] * 0.04;
      pattern[\lag] = Pwhite(delay.neg, delay);
    };

    ^pattern;
  }

  *prInitializeDictionaries { |pattern|
    if (Ndef(\px).isPlaying.not) {
      chorusPatterns.clear;
      colors.clear;
      last.clear;
      meterIdMap.clear;
      meterLevels.clear;
      meterNextId = 0;
      ndefList.clear;
    };

    last[pattern[\id]] = pattern;
  }

  *prCreatePlayList { |id, pdef|
    if (ndefList[id].isNil)
    { ndefList.put(id, Ndef(id, pdef).quant_(quant)) };

    ^ndefList.copy;
  }

  *prPrint { |value|
    value.postln;
  }

  *prReevaluate { |patterns|
    patterns = patterns ?? last;

    ^patterns do: { |value, key|
      this.new(value);
    }
  }

  *prSortedPatternIds { |patterns|
    var drumMachineIds, otherIds;

    patterns = patterns ?? last;
    drumMachineIds = patterns.select { |pattern|
      pattern[\drumMachine].notNil
    }.keys.asSortedList;
    otherIds = patterns.select { |pattern|
      pattern[\drumMachine].isNil
    }.keys.asArray.sort({ |a, b| a.asInteger < b.asInteger });

    ^drumMachineIds ++ otherIds;
  }

  *prRemoveFinitePatternFromLast { |pattern|
    var hasFadeIn = pattern[\fade].isArray
    and: { pattern[\fade][0] == \in };
    var hasFadeOut = pattern[\fade].isArray
    and: { pattern[\fade][0] == \out };
    var hasEmptyDur = pattern[\dur] == 0
    or: { pattern[\dur].isNil };
    var hasRepeat = pattern[\repeat].notNil;
    var hasStop = pattern[\stop].notNil;

    if (hasFadeIn)
    { last[pattern[\id]].removeAt(\fade) };

    if (hasFadeOut)
    { last.removeAt(pattern[\id]) };

    if (hasRepeat or: hasEmptyDur or: hasStop) {
      last.removeAt(pattern[\id]);
      ndefList.removeAt(pattern[\id]);

      meterIdMap = meterIdMap.select { |v| v != pattern[\id] };
      meterLevels.removeAt(pattern[\id]);
    };
  }
}
