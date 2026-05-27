/*
TODO: Midinote notation in uppercase return chords
TODO: When used in a group, Number solo method mutes new patterns already played.
Example on Mastegots.scd
*/
Px {
  classvar <>chorusPatterns;
  classvar <>colors;
  classvar <drumMachinesPath;
  classvar <>fxState;
  classvar <>last;
  classvar <>lastFormatted;
  classvar <meterFunc;
  classvar <meterIdMap;
  classvar <mixBus;
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
    fxState = Dictionary.new;
    last = Dictionary.new;
    lastFormatted = Dictionary.new;
    meterIdMap = Dictionary.new;
    meterLevels = Dictionary.new;
    meterNextId = 0;
    midiHoldedNotes = Dictionary.new;

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

      fork {
        Server.default.sync;
        this.prInitMasterNdef;
      };
    };
  }

  *new { |newPattern|
    var pattern, pdef, isNewNdef;
    var prevSize = last.size;

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
    pdef = this.prCreatePbind(pattern);
    this.prCreatePlayList(pattern[\id], pdef);

    if (isNewNdef)
    { Ndef(pattern[\id]).play(out: mixBus.index, fadeTime: 0) };

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

    if (last.size != prevSize) {
      this.prAutoRefreshGui;
    };
  }

  *prCreateAmp { |pattern|
    var amp = pattern[\amp] ?? 0.3;

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

  *prCreatePbind { |pattern|
    var pbind;
    var stopBeats = pattern[\stop];

    pattern.removeAt(\repeat);
    pattern.removeAt(\stop);

    pbind = Pbind(*pattern.asPairs);

    if (pattern[\midiControl] != 1)
    { pbind = this.prCreateFade(pbind, pattern[\fade]) };

    pbind = this.prCreateChop(pattern, pbind);

    if (stopBeats.notNil)
    { pbind = Pfindur(stopBeats, pbind) };

    ^pbind;
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
    if (ndefList.isEmpty) {
      chorusPatterns.clear;
      colors.clear;
      last.clear;
      meterIdMap.clear;
      meterLevels.clear;
      meterNextId = 0;
    };

    last[pattern[\id]] = pattern;
  }

  *prCreatePlayList { |id, pbind|
    if (ndefList[id].isNil)
    { ndefList.put(id, Ndef(id, pbind).quant_(quant)) }
    { Ndef(id, pbind) };
  }

  *prInitMasterNdef {
    if (mixBus.isNil)
    { mixBus = Bus.audio(Server.default, 2) };

    Ndef(\px, { InFeedback.ar(mixBus.index, 2) }).play(fadeTime: 0);
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

  *prRemoveFinitePatternFromLast { |pattern|
    var hasFadeIn = pattern[\fade].isArray
    and: { pattern[\fade][0] == \in };
    var hasFadeOut = pattern[\fade].isArray
    and: { pattern[\fade][0] == \out };
    var hasEmptyDur = pattern[\dur] == 0
    or: { pattern[\dur].isNil };
    var hasRepeat = pattern[\repeat].notNil;
    var hasStop = pattern[\stop].notNil;

    case
    { hasFadeIn }
    { last[pattern[\id]].removeAt(\fade) }

    { hasFadeOut }
    { last.removeAt(pattern[\id]) }

    { hasRepeat or: hasEmptyDur or: hasStop } {
      last.removeAt(pattern[\id]);
      ndefList.removeAt(pattern[\id]);

      meterIdMap = meterIdMap.select { |v| v != pattern[\id] };
      meterLevels.removeAt(pattern[\id]);
    };
  }
}
