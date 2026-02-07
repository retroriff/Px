+ Px {
  *ctranspose { |value|
    last do: { |pattern|
      pattern[\ctranspose] = value;
    };

    this.prReevaluate;
  }

  *root { |value|
    last do: { |pattern|
      pattern[\root] = value;
    };

    this.prReevaluate;
  }

  *scale { |value|
    last do: { |pattern|
      pattern[\scale] = value;
    };

    this.prReevaluate;
  }

  *prCreateDegrees { |pattern, midiratio|
    var createRandomDegrees = {
      var length, scale, scaleDegrees, randomDegrees;
      length = pattern[\length] ?? 1;
      scale = pattern[\scale] ?? \phrygian;

      if (scale.isArray)
      { scaleDegrees = scale }
      { scaleDegrees = Scale.at(scale.asSymbol).degrees };

      randomDegrees = Array.newClear(length);
      thisThread.randSeed = this.prGetPatternSeed(pattern);
      randomDegrees = length.collect { scaleDegrees.choose };
    };

    var degreesWithVariations = { |degrees, numOctaves = 1|
      if (pattern[\arp].notNil) {
        degrees = degrees.collect { |degree|
          degree + (0..numOctaves).flat.collect { |oct| oct * 7 };
        };

        degrees = degrees.as(Array).flat;
      };

      degrees;
    };

    if (pattern[\scale].notNil and: (pattern[\scale].isArray.not))
    { pattern[\scale] = Scale.at(pattern[\scale].asSymbol).semitones };

    if (pattern[\degree].isNil)
    { ^pattern };

    if (pattern[\degree].isKindOf(Pattern).not) {
      var degrees = pattern[\degree];
      var length = pattern[\midiControl] ?? inf;
      var isRandomDegrees = (degrees == \rand);
      var processedDegrees;

      if (degrees == \rand)
      { degrees = createRandomDegrees.value };

      if (degrees.isArray.not)
      { degrees = [degrees] };

      if (midiratio == true)
      { degrees = degrees.midiratio };

      pattern[\degreeRaw] = degrees;
      processedDegrees = degreesWithVariations.(degrees);

      if (pattern[\arp].notNil || isRandomDegrees || (midiratio == true)) {
        pattern[\degree] = Pseq(processedDegrees, length);
      } {
        pattern[\degree] = processedDegrees;
      };
    };

    ^pattern;
  }

  *prCreateOctaves { |pattern|
    var octave = pattern[\octave];
    var isBeat = octave.isArray and: { octave[0] == \beat };

    if (isBeat) {
      var octaveBeat = this.prCreateBeat(
        pattern,
        defaultWeight: 0.3,
        min: octave[1],
        max: octave[1] + 1
      );

      octave = octaveBeat;
    };

    if (pattern[\midinote].notNil and: { octave.notNil }) {
      pattern = this.prApplyOctaveToMidinote(pattern, octave);
    };

    if (octave.isArray)
    { pattern[\octave] = Pseq(octave, inf) };

    if (pattern[\octaveTranspose].notNil) {
      var oct = pattern[\octaveTranspose];
      var ct = pattern[\ctranspose] ?? 0;
      pattern[\ctranspose] = oct + ct;
      pattern.removeAt(\octaveTranspose);
    };

    ^pattern;
  }

  *prApplyOctaveToMidinote { |pattern, octave|
    if (octave.isArray) {
      pattern[\octaveTranspose] = Pseq(octave.collect { |o| o * 12 }, inf);
    } {
      pattern[\octaveTranspose] = octave * 12;
    };

    pattern.removeAt(\octave);
    ^pattern;
  }
}

+ Number {
  arp { |value|
    this.prDebouncer.enqueue([\arp, value]);
  }

  degree { |value|
    var pattern;

    if (value.isInteger)
    { value = [value] };

    if (value.isKindOf(Pattern))
    { pattern = value };

    this.prDebouncer.enqueue([\degree, pattern ?? value]);
    ^this
  }

  scale { |value|
    this.prDebouncer.enqueue([\scale, value.asSymbol]);
  }

  sus { |value|
    this.prDebouncer.enqueue([\sus, value]);
  }
}

+ Symbol {
  arp {}
  degree {}
  sus {}
}
