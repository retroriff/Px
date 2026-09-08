/*
TODO: Rand degree from old examples files doesn't work anymore, should we deprecate it? 909 i: \oh dur: 0.25 beat: 0.7 amp: 0.4 degree: \rand length: 3;
*/
Px {
  classvar <>chorusPatterns;
  classvar <>colors;
  classvar <>cycleOrigins;
  classvar <defaultAmp;
  classvar <drumMachinesPath;
  classvar <>last;
  classvar <>lastFormatted;
  classvar <>maxQuant;
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
  classvar <>skipFillCascade;
  classvar <>window;
  classvar <windowWidth;
  classvar <windowHeight;

  *initClass {
    chorusPatterns = Dictionary.new;
    colors = Dictionary.new;
    cycleOrigins = Dictionary.new;
    defaultAmp = 0.3;
    last = Dictionary.new;
    lastFormatted = Dictionary.new;
    maxQuant = 16;
    meterIdMap = Dictionary.new;
    meterLevels = Dictionary.new;
    meterNextId = 0;
    midiHoldedNotes = Dictionary.new;
    patternViews = Dictionary.new;
    mutedPatterns = Dictionary.new;
    ndefList = Dictionary.new;
    pausedPatterns = IdentitySet.new;
    quant = 4;
    seeds = Dictionary.new;
    shuffleHistory = Dictionary.new;
    skipFillCascade = false;
    windowWidth = 68;
    windowHeight = 350.min(Window.screenBounds.height / 4);

    this.prStartMeterListener;

    CmdPeriod.add { this.clear };

    ServerBoot.add {
      this.prStartMeterListener;
      this.listen;
      this.loadSynthDefs;
      thisProcess.interpreter.t = TempoClock.default.tempo;
    };
  }

  *new { |newPattern|
    var pattern, pdef, playList, isNewNdef;
    var previousPattern = last[newPattern[\id]];
    var previousRhythm = previousPattern !? { previousPattern[\rhythmBeats] ?? previousPattern[\totalBeats] };

    this.prInitializeDictionaries(newPattern);
    this.prHandleSoloPattern(newPattern);

    pattern = this.prCreateBufInstruments(newPattern);

    if (pattern[\bufMissing] == true) {
      if (previousPattern.notNil)
      { last[pattern[\id]] = previousPattern }
      { last.removeAt(pattern[\id]) };

      ^this;
    };

    pattern = this.prCreateInstrument(pattern);
    pattern = this.prCreateLoops(pattern);
    pattern = this.prCreateAmp(pattern);
    pattern = this.prCreateDur(pattern);
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

        this.prAddMeter(pattern[\id], meterId, "/pxMeter");
      };
    };

    lastFormatted[newPattern[\id]] = pattern;

    this.prRemoveFinitePatternFromLast(pattern);
    this.prAutoRefreshGui;

    if (skipFillCascade.not and: {
      var currentRhythm = last[pattern[\id]] !? { |p| p[\rhythmBeats] ?? p[\totalBeats] };
      this.prRhythmChanged(previousRhythm, currentRhythm);
    })
    { this.prReevaluateFillDependents(pattern) };
  }

  *prAddMeter { |id, meterId, cmdName|
    var ndef = Ndef(id);

    if (ndef.isNeutral)
    { ndef.initBus(\audio, 2) };

    ndef.filter(100, { |in|
      SendPeakRMS.kr(in, 20, 0.3, cmdName, meterId);
      in;
    });
  }

  *prStartMeterListener {
    meterFunc !? { meterFunc.free };

    meterFunc = OSCFunc({ |msg|
      var meterId = msg[2].asInteger;
      var peakL = msg[3] ?? 0;
      var peakR = msg[5] ?? peakL;
      var patternId = meterIdMap[meterId];

      if (patternId.notNil)
      { meterLevels[patternId] = max(peakL, peakR) };
    }, '/pxMeter');

    meterFunc.permanent_(true);
  }

  *prCreateAmp { |pattern|
    var amp = pattern[\amp] ?? defaultAmp;
    var repeats = pattern[\repeat] ?? inf;
    var ampPattern, beats;

    if (amp.isKindOf(Pattern)
      and: { pattern[\beat].notNil or: { pattern[\fill].notNil } })
    {
      ampPattern = amp;
      amp = this.prExtractAmpMax(amp);
    };

    if (pattern[\beat].notNil)
    { beats = this.prCreateRhythmBeat(amp, pattern) };

    if (pattern[\fill].notNil)
    { beats = this.prCreateFillFromBeat(amp, pattern) };

    pattern[\amp] = ampPattern ?? amp;
    pattern.removeAt(\ampBeat);

    if (beats.isNil) {
      if (pattern[\amp].isArray)
      { pattern[\amp] = Pseq(pattern[\amp], repeats) };

      ^pattern;
    };

    if (beats.isArray)
    { beats = Pseq(beats, repeats) };

    if (ampPattern.notNil and: { beats.isKindOf(Pattern) })
    { beats = this.prApplyAmpPattern(beats, ampPattern) };

    pattern[\ampBeat] = beats;

    ^pattern;
  }

  *prExtractAmpMax { |amp|
    if (amp.isNumber) { ^amp };

    if (amp.isKindOf(Pwhite)) { ^this.prExtractAmpMax(amp.hi) };

    if (amp.isKindOf(Pattern) and: { amp.respondsTo(\list) }) {
      var values = amp.list.reject { |x| x.isKindOf(Rest) }.collect { |x|
        this.prExtractAmpMax(x)
      }.select(_.isNumber);

      ^if (values.notEmpty) { values.maxItem } { defaultAmp };
    };

    this.prPrint("🔴 amp" + amp.class + "could not be resolved, using" + defaultAmp);
    ^defaultAmp;
  }

  *prScaleAmp { |amp, ratio|
    if (amp.isKindOf(Rest)) { ^amp };

    if (amp.isNumber) { ^amp * ratio };

    if (amp.isKindOf(Pwhite)) {
      ^Pwhite(
        this.prScaleAmp(amp.lo, ratio),
        this.prScaleAmp(amp.hi, ratio)
      )
    };

    if (amp.isKindOf(Pattern) and: { amp.respondsTo(\list) }) {
      amp.list = amp.list.collect { |x| this.prScaleAmp(x, ratio) };
      ^amp
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
    var hasDur = dur.notNil and: { dur != 0 };

    if (hasDur.not)
    { dur = Pseq([8], pattern[\repeat] ?? 1) };

    if (dur.isArray) {
      var containsString = dur any: { |item| item.isString };
      dur = containsString.if { 1 } { Pseq(dur, inf) };
    };

    if (dur.isString)
    { dur = 1 };

    if (hasDur)
    { pattern[\durStep] = dur }
    { pattern.removeAt(\durStep) };

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

  *prPatternQuant { |pattern|
    var id = pattern[\id];
    var previous = lastFormatted[id];
    var cycle;

    if (Pdef(id).source.isNil or: { previous.isNil })
    { ^quant };

    if (previous[\repeat].notNil or: { previous[\stop].notNil })
    { ^quant };

    cycle = this.prCycleBeats(previous);

    if (cycle.isNil or: { cycle <= quant } or: { cycle > maxQuant })
    { ^quant };

    ^[cycle, (cycleOrigins[id] ?? 0) % cycle];
  }

  *prCreatePdef { |pattern|
    var pbindef;
    var id = pattern[\id];
    var pdef = Pdef(id);
    var patternQuant = this.prPatternQuant(pattern);
    var stopBeats = pattern[\stop];
    var bindPattern = pattern.copy;

    bindPattern[\amp] = bindPattern[\ampBeat] ?? bindPattern[\amp];

    bindPattern.removeAt(\ampBeat);
    bindPattern.removeAt(\durStep);
    bindPattern.removeAt(\repeat);
    bindPattern.removeAt(\rest);
    bindPattern.removeAt(\rhythmBeats);
    bindPattern.removeAt(\stop);
    bindPattern.removeAt(\totalBeats);

    pbindef = Pbind(*bindPattern.asPairs);
    pbindef = this.prCreateRest(pattern, pbindef);

    if (pattern[\midiControl] != 1)
    { pbindef = this.prCreateFade(pbindef, pattern[\fade]) };

    pbindef = this.prCreateChop(pattern, pbindef);

    if (stopBeats.notNil)
    { pbindef = Pfindur(stopBeats, pbindef) };

    pdef.quant = patternQuant;

    if (pausedPatterns.includes(id))
    { ^pdef };

    pdef.source = pbindef;
    cycleOrigins[id] = patternQuant.asQuant.nextTimeOnGrid(TempoClock.default);

    ^pdef;
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
      cycleOrigins.clear;
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
    var isFullReevaluation = patterns.isNil;

    patterns = (patterns ?? last).reject { |v| pausedPatterns.includes(v[\id]) };

    if (isFullReevaluation) {
      skipFillCascade = true;
      this.prSortedForEvaluation(patterns) do: { |value| this.new(value) };
      skipFillCascade = false;

      ^patterns;
    };

    ^patterns do: { |value, key|
      this.new(value);
    }
  }

  *prSortedForEvaluation { |patterns|
    var drumValues, otherValues;
    var values = patterns.isKindOf(Dictionary).if({ patterns.values }, { patterns.asArray });

    drumValues = values.select { |v| v[\drumMachineIntegerId].notNil };
    otherValues = values.reject { |v| v[\drumMachineIntegerId].notNil };

    drumValues = drumValues.sort({ |a, b| a[\drumMachineIntegerId] < b[\drumMachineIntegerId] });
    otherValues = otherValues.sort({ |a, b| a[\id].asInteger < b[\id].asInteger });

    ^drumValues ++ otherValues;
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
      cycleOrigins.removeAt(pattern[\id]);
      last.removeAt(pattern[\id]);
      ndefList.removeAt(pattern[\id]);

      meterIdMap = meterIdMap.select { |v| v != pattern[\id] };
      meterLevels.removeAt(pattern[\id]);
    };
  }
}
