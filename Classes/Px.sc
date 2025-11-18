/*
TODO: Any param should have a beat function.
      Maybe \beat or [0.3, 0.5].beat(16)
      Already created Number.prCreateBeat
TODO: Fix 707 set: \cp amp: 0.5;
TODO: Rename name references to id?
*/

Px {
  classvar <>chorusPatterns;
  classvar <>colors;
  classvar <drumMachinesPath;
  classvar <>last;
  classvar <>lastFormatted;
  classvar <lastName;
  classvar <midiClient;
  classvar <>midiHoldedNotes;
  classvar <midiOut;
  classvar <>ndefList;
  classvar <>patternState;
  classvar <samplesDict;
  classvar <samplesPath;
  classvar <seeds;
  classvar <>window;
  classvar <windowWidth;
  classvar <windowHeight;
  classvar count;

  *initClass {
    // CmdPeriod.add { Px.clear };
    chorusPatterns = Dictionary.new;
    colors = Dictionary.new;
    last = Dictionary.new;
    lastFormatted = Dictionary.new;
    midiHoldedNotes = Dictionary.new;
    ndefList = Dictionary.new;
    seeds = Dictionary.new;
    windowWidth = 68;
    windowHeight = 350.min(Window.screenBounds.height / 4);
    count = 0;
  }

  *new { |newPattern|
    var pattern, pdef, playList;

    count = count + 1;
    count.postln;

    this.prInitializeDictionaries(newPattern);
    this.prHandleSoloPattern(newPattern);

    pattern = this.prCreateBufInstruments(newPattern);
    pattern = this.prCreateLoops(pattern);
    pattern = this.prCreateAmp(pattern);
    pattern = this.prCreateDur(pattern);
    pattern = this.prCreatePan(pattern);
    pattern = this.prCreateDegrees(pattern);
    pattern = this.prCreateOctaves(pattern);
    pattern = this.prCreateMidi(pattern);
    pattern = this.prCreateFx(pattern);

    pdef = this.prCreatePdef(pattern);
    playList = this.prCreatePlayList(pattern[\id], pdef);

    if (Ndef(\px).isPlaying.not)
    { Ndef(\px).quant_(4).play };

    Ndef(\px)[0] = { Mix.new(playList.values) };

    lastFormatted[newPattern[\id]] = pattern;
    this.prRemoveFinitePatternFromLast(newPattern);
  }

  *prCreateAmp { |pattern|
    var amp = pattern[\amp] ?? 1;

    if (pattern[\beat].notNil)
    { amp = this.prCreateRhythmBeat(amp, pattern) };

    if (pattern[\fill].notNil)
    { amp = this.prCreateFillFromBeat(amp, pattern) };

    pattern[\dur] = this.prCreateBeatRest(pattern);
    pattern[\amp] = amp;

    if (pattern[\amp].isArray)
    { pattern[\amp] = Pseq(pattern[\amp], inf) };

    ^pattern;
  }

  *prCreateChop { |pattern, pbindef|
    if (pattern[\chop].isArray) {
      var dur = pattern[\chop][0];
      var drop = pattern[\chop][1];

      if (dur != 0 and: (dur != Nil)) {
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
    { dur = Pseq([8], 1) };

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

    if (this.prHasFX(pattern) == true)
    { pbindef = this.prCreatePbindFx(pattern) }
    { pbindef = Pbind(*pattern.asPairs) };

    pbindef = this.prCreateFade(pbindef, pattern[\fade]);
    pbindef = this.prCreateChop(pattern, pbindef);

    ^pbindef = Pdef(pattern[\id], pbindef).quant_(4);
  }

  *prHandleSoloPattern { |pattern|
    var hasSolo = pattern[\solo] == true;

    if (hasSolo) {
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
      ndefList.clear;
    };

    last[pattern[\id]] = pattern;
  }

  *prCreatePlayList { |id, pdef|
    if (ndefList[id].isNil)
    { ndefList.put(id, Ndef(id, pdef).quant_(4)) };

    ^ndefList.copy;
  }

  *prPrint { |value|
    if (~isUnitTestRunning != true)
    { value.postln };
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
    var hasRepeats = pattern[\repeats].notNil;
    var hasEmptyDur = pattern[\dur] == 0;

    case
    { hasFadeIn }
    { last[pattern[\id]].removeAt(\fade) }

    { hasFadeOut }
    { last.removeAt(pattern[\id]) }

    { hasRepeats or: hasEmptyDur } {
      last.removeAt(pattern[\id]);
      ndefList.removeAt(pattern[\id]);
    };
  }
}
