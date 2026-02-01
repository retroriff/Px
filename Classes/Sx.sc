Sx {
  classvar <defaultEvent;
  classvar <defaultScale;
  classvar padSynths;
  classvar <mode;
  classvar <>last;
  classvar synth;
  classvar <waveList;

  *initClass {
    // It fixes this error when we play a pad after stopping it with "Command .".
    // Not sure if generates other issues while playing other stuff:
    // FAILURE IN SERVER /n_set Node 1017 not found
    // CmdPeriod.add { Sx.clear };
    defaultScale = \scriabin;
    defaultEvent = (
      amp: 1,
      atk: 0,
      chord: [0],
      degree: [0],
      dur: [1],
      env: 0,
      octave: [0],
      rel: 3,
      root: 0,
      scale: defaultScale,
      vcf: 1,
      wave: \saw;
    );
    last = Event.new;
    mode = \seq;
    padSynths = [];
    waveList = [\pulse, \saw, \sine, \triangle];
  }

  *new { |event, fadeTime|
    var isPadMode;

    event = this.prCreateDefaultArgs(event ?? Event.new);
    isPadMode = this.prIsPadMode(event);
    last = event.copy;

    if (isPadMode) {
      this.prPlayPad(event, fadeTime ?? event[\atk]);
    } {
      if (mode == \pad) {
        this.prFreePadSynths;
        mode = \seq;
      };

      this.play(fadeTime);

      event.keysValuesDo { |key, value|
        this.qset(key, value);
      };
    };
  }

  *clear {
    this.initClass;
  }

  *loadSynth {
    var path = "../SynthDefs/Sx.scd";
    var file = PathName((path).resolveRelative);
    File.readAllString(file.fullPath);
    file.fullPath.load;
  }

  *play { |fadeTime|
    if (mode == \pad) {
      ^Ndef(\sx).play(fadeTime: fadeTime ?? 5);
    };

    mode = \seq;

    if (Ndef(\sx).isPlaying.not) {
      synth = Synth(\sx);
    };

    Ndef(\sx, { In.ar(~sxBus, 2) }).play(fadeTime: fadeTime ?? 0);
  }

  *qset { |key, value, lag|
    this.set(key: key, value: value, lag: lag, quant: true);
  }

  *release { |fadeTime = 10|
    Ndef(\sx).free(fadeTime);

    if (mode == \pad) {
      ^this.prFreePadSynths(fadeTime);
    }

    ^fork {
      (fadeTime * 2).wait;
      synth.free;
    }
  }

  *set { |key, value, lag, quant|
    var arraySizePair = Array.new;
    var pairs;

    last.putAll([key, value]);
    value = this.prConvertToArray(key, value);
    pairs = [key, value];

    case
    { key == \degree } {
      var octave = last[\octave] ?? defaultEvent[\octave];
      octave = this.prConvertToArray(\octave, octave);
      pairs = this.prGenerateDegree(value, octave);
    }

    { key == \euclid }
    { pairs = this.prGenerateEuclid(value) }

    { key == \octave } {
      var degree = last[\degree] ?? defaultEvent[\degree];
      degree = this.prConvertToArray(\degree, degree);
      pairs = this.prGenerateDegree(degree, value);
    }

    { key == \root } {
      var degree = this.prConvertToArray(\degree, last[\degree]);
      var octave = this.prConvertToArray(\octave, last[\octave]);
      pairs = this.prGenerateDegree(degree, octave, value);
    }

    { key == \scale }
    { pairs = this.prGenerateScale(value) }

    { key == \wave }
    { pairs = this.prGenerateWave(value) }

    { key == \pad and: (mode == \pad)} {
        var event = last.copy;
        event[\pad] = value;

        if (quant.isNil)
        { ^this.prUpdateOrPlayPad(event, last[\atk], lag) }
        { ^this.prScheduleQuantized({ this.prUpdateOrPlayPad(event, last[\atk], lag) }) };
    };

    pairs = pairs ++ this.prGenerateArraySize(pairs[0], pairs[1]);
    pairs = pairs ++ [\lag, lag ?? 0];

    if (quant.isNil)
    { ^this.prSet(pairs) }
    { ^this.prCreateQuantizedSet(pairs) };
  }

  *stop { |fadeTime|
    Ndef(\sx).stop(fadeTime);
  }

  *tempo { |tempo|
    if (synth.notNil)
    { synth.set(\tempo, tempo) };
  }

  *vol { |value|
    if (value.isNil) {
      var vol = Ndef(\sx).vol;
      this.prPrint("Sx vol is" + vol);
    } {
      ^Ndef(\sx).vol_(value);
    }
  }

  *prCreateQuantizedSet { |pairs|
    this.prScheduleQuantized({ this.prSet(pairs) });
  }

  *prScheduleQuantized { |func|
    var clock = TempoClock.default;
    var nextBeat = clock.nextTimeOnGrid(4);
    clock.schedAbs(nextBeat, func);
  }

  *prGenerateDegree { |degree, octave, root|
    var maxLen = max(degree.size, octave.size);
    var result = Array.new;
    var degIndex = 0;
    var octIndex = 0;
    var dur = last[\dur] ?? defaultEvent[\dur];

    while ({ result.size < maxLen }) {
      var deg = degree[degIndex];
      var oct = octave[octIndex].clip(-2, 2);

      result = result.add(deg + (oct * 12));
      if (oct == -0)
      { oct = 0 };

      degIndex = (degIndex + 1) % degree.size;
      octIndex = (octIndex + 1) % octave.size;
    };

    root = root ?? last[\root] ?? defaultEvent[\root];
    ^[\degree, result + root.clip(-12, 12), \dur, dur];
  }

  *prCreateDefaultArgs { |event|
    defaultEvent.keys do: { |key|
      event[key] = event[key] ?? defaultEvent[key];
    };

    ^event;
  }

  *prGenerateEuclid { |value|
    var dur = this.prConvertToArray(\dur, last[\dur]);
    var euclid = Bjorklund2(*value) * dur[0];

    ^[\dur, euclid];
  }

  *prConvertToArray { |key, value|
    if (this.prShouldBeArray(key) and: value.isNumber)
    { value = [value] };

    ^value;
  }

  *prGenerateArrayName { |key|
    ^(key ++ "Size").asSymbol;
  }

  *prGenerateArraySize { |key, value|
    var pairs = Array.new;

    if (this.prShouldBeArray(key))
    { pairs = [this.prGenerateArrayName(key), value.size] };

    ^pairs;
  }

  *prGenerateScale { |value|
    var buffer;

    if (value == \default)
    { value = defaultScale };

    buffer = Buffer.loadCollection(Server.default, Scale.at(value));

    ^[\scale, buffer];
  }

  *prGenerateWave { |value|
    var pairs = Array.new;

    if (waveList.includes(value).not) {
      this.prPrint("🔴 Wave not valid. Use:" + waveList);
      ^pairs;
    };

    waveList do: { |wave|
      var waveValue = 0;

      if (value == wave)
      { waveValue = 1 };

      pairs = pairs ++ [wave, waveValue];
    };

    ^pairs;
  }

  *prPrint { |value|
    if (~isUnitTestRunning != true)
    { value.postln };
  }

  *prSet { |pairs|
    if (mode == \seq) {
      synth.set(*pairs);
    } {
      padSynths.do { |sxPad|
        sxPad.set(*pairs);
      };
    };
  }

  *prShouldBeArray { |key|
    var arrayKeys = [\chord, \degree, \dur, \octave, \pad];

    ^arrayKeys.includes(key);
  }

  *prUpdateLast { |key, value|
    last.putAll([key, value]);
  }

  *prIsPadMode { |event|
    var pad = event[\pad];
    var degree = event[\degree];
    var padIsDefault = (pad == [0]) or: (pad == defaultEvent[\pad]);
    var degreeIsDefault = (degree == [0]) or: (degree == defaultEvent[\degree]);
    var padIsSymbol = pad.isKindOf(Symbol);
    ^(padIsSymbol or: padIsDefault.not) and: degreeIsDefault;
  }

  *prPlayPad { |event, fadeTime|
    var pad = event[\pad];
    var midinotes;
    var amp = event[\amp] ?? 1;
    var octave = event[\octave] ?? [0];
    var wavePairs = this.prGenerateWave(event[\wave] ?? \saw);
    var crossfadeTime = event[\rel] ?? 3;

    if (pad.isKindOf(Symbol)) {
      Nx.set(pad);
      midinotes = Nx.midinotes;
      // this.prPrint("Pad:" + pad + "midinotes:" + midinotes);
    } {
      var key = event[\key] ?? 60;
      midinotes = pad.collect { |interval| key + interval };
    };

    this.prFreePadSynths(crossfadeTime);

    if (synth.notNil) {
      synth.free;
      synth = nil;
    };

    mode = \pad;
    last = event.copy;

    Ndef(\sx, { In.ar(~sxBus, 2) }).play(fadeTime: fadeTime ?? 5);

    midinotes.do { |note, i|
      var oct = octave.wrapAt(i).clip(-2, 2);
      var midinote = note + (oct * 12);
      var synthAmp = amp / (i + 1).sqrt;
      var newSynth = Synth(\sx, [
        \amp, synthAmp,
        \atk, event[\atk] ?? fadeTime ?? 5,
        \midinote, midinote,
        \rel, event[\rel] ?? 3,
        \seq, 0,
        \vcf, event[\vcf] ?? 1,
      ] ++ wavePairs);

      padSynths = padSynths.add(newSynth);
    };
  }

  *prUpdateOrPlayPad { |event, fadeTime, lag|
    var shouldUpdate = (lag.notNil) and: { lag > 0 } and: { padSynths.notEmpty };

    if (shouldUpdate) {
      ^this.prUpdatePad(event, lag);
    } {
      ^this.prPlayPad(event, fadeTime);
    };
  }

  *prUpdatePad { |event, lag|
    var pad = event[\pad];
    var newMidinotes;
    var octave = event[\octave] ?? [0];
    var oldSize = padSynths.size;
    var newSize, minSize;
    var lastPad = last[\pad];
    var shouldScramble = false;

    if (pad.isKindOf(Symbol)) {
      Nx.set(pad);
      newMidinotes = Nx.midinotes;
    } {
      var key = event[\key] ?? 60;
      newMidinotes = pad.collect { |interval| key + interval };
    };

    newMidinotes = newMidinotes.collect { |note, i|
      var oct = octave.wrapAt(i).clip(-2, 2);
      note + (oct * 12);
    };

    if (pad.isKindOf(Symbol) and: lastPad.isKindOf(Symbol)) {
      if (pad == lastPad) {
        shouldScramble = true;
      };
    };

    if (pad.isKindOf(Array) and: lastPad.isKindOf(Array)) {
      if (pad.size == lastPad.size) {
        if (pad.sort == lastPad.sort) {
          shouldScramble = true;
        };
      };
    };

    if (shouldScramble) {
      newMidinotes = newMidinotes.scramble;
    };

    newSize = newMidinotes.size;
    minSize = min(oldSize, newSize);

    minSize.do { |i|
      padSynths[i].set(\midinote, newMidinotes[i], \lag, lag);
    };

    if (newSize > oldSize) {
      (newSize - oldSize).do { |i|
        var idx = oldSize + i;
        var midinote = newMidinotes[idx];
        var synthAmp = (event[\amp] ?? 1) / (idx + 1).sqrt;
        var wavePairs = this.prGenerateWave(event[\wave] ?? \saw);
        var newSynth = Synth(\sx, [
          \amp, synthAmp,
          \atk, event[\atk] ?? 5,
          \midinote, midinote,
          \rel, event[\rel] ?? 3,
          \seq, 0,
          \vcf, event[\vcf] ?? 1,
        ] ++ wavePairs);
        padSynths = padSynths.add(newSynth);
      };
    } {
      if (newSize < oldSize) {
        var relTime = event[\rel] ?? 3;
        (oldSize - newSize).do { |i|
          var idx = newSize + i;
          padSynths[idx].set(\gate, 0, \rel, relTime);
        };
        padSynths = padSynths[0..newSize-1];
      };
    };

    last = event.copy;
  }

  *prFreePadSynths { |releaseTime|
    padSynths.do { |synth|
      synth.set(\gate, 0, \rel, releaseTime ?? 3);
    };

    padSynths = [];
  }
}