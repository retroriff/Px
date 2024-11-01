+ Px {
    *root { |value|
        lastPatterns do: { |pattern|
            pattern[\root] = value;
        };

        ^this.new;
    }

    *prGenerateDegrees { |pattern, midiratio|
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
        { pattern[\scale] = Scale.at(pattern[\scale]).semitones };

        if (pattern[\degree].isNil)
        { ^pattern };

        if (pattern[\degree].isKindOf(Pattern).not) {
            var degrees = pattern[\degree];
            var length = pattern[\midiControl] ?? inf;

            if (degrees == \rand)
            { degrees = createRandomDegrees.value };

            if (midiratio == true)
            { degrees = degrees.midiratio };

            pattern[\degree] = Pseq(degreesWithVariations.(degrees), length);
        };

        ^pattern;
    }

    *prGenerateOctaves { |pattern|
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

        if (octave.isArray)
        { pattern[\octave] = Pseq(octave, inf) };

        ^pattern;
    }
}

+ Number {
    arp { |value|
        this.prUpdatePattern([\arp, value]);
    }

    degree { |value|
        var pattern;

        if (value.isInteger)
        { value = [value] };

        if (value.isKindOf(Pattern))
        { pattern = value };

        ^this.prUpdatePattern([\degree, pattern ?? value]);
    }

    detune { |value|
        ^this.prUpdatePattern([\detune, value]);
    }

    legato { |value|
        ^this.prUpdatePattern([\legato, value]);
    }

    length { |value|
        ^this.prUpdatePattern([\length, value]);
    }

    octave { |value|
        ^this.prUpdatePattern([\octave, value]);
    }

    root { |value|
        ^this.prUpdatePattern([\root, value]);
    }

    scale { |value|
        ^this.prUpdatePattern([\scale, value]);
    }

    sus { |value|
        ^this.prUpdatePattern([\sus, value]);
    }
}

+ Symbol {
    arp {}
    degree {}
    detune {}
    legato {}
    length {}
    octave {}
    root {}
    scale {}
    sus {}
}
