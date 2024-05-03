Play : Px {
    classvar <midiClient;

    *new { | patterns, name, quant, trace, midiout|
        patterns.collect { |pattern|
            pattern.putAll([\degree: this.prGenerateDegrees(pattern) ?? 0 ]);
        };
        patterns = this.prCreateMidiPatterns(patterns, midiout);
        name = super.prGetName(name);
        ^super.new(patterns, name, quant, trace);
    }

    *prGenerateDegrees { |pattern|
        var degreesWithVariations = { |pattern, degrees, numOctaves = 1|
            if (pattern[\arp].notNil) {
                degrees = degrees.collect { |degree|
                    degree + (0..numOctaves).flat.collect { |oct| oct * 7 };
                };
                degrees = degrees.as(Array).flat;
            };
            degrees;
        };

        var createRandomDegrees = { |pattern, size = 1|
            var scale = pattern[\degree][1] ?? \phrygian;
            var scaleDegrees = Scale.at(scale.asSymbol).degrees;
            var randomDegrees = Array.newClear(size);
            thisThread.randSeed = super.prGetPatternSeed(pattern);
            randomDegrees = size.collect { scaleDegrees.choose };
        };

        if (pattern[\degree].isArray) {
            var degrees = pattern[\degree][0];
            var length = pattern[\midiControl] ?? inf;
            if (degrees == \rand) {
                degrees = createRandomDegrees.(pattern, size: pattern[\degree][2]);
            };
            pattern[\degree] = Pseq(degreesWithVariations.(pattern, degrees), length);
        };

        ^pattern[\degree];
    }
}

+Event {
    arp {
        ^this ++ (arp: true);
    }

    degree { |value, scale, size|
        var pattern;

        if (value.isInteger)
        { value = [value] };

        if (value.isKindOf(Pattern))
        { pattern = value };

        ^this ++ (\degree: pattern ?? [value, scale, size]);
    }

    octave { |value|
        if (value.isArray) {
            value = Pseq(value, inf);
        }
        ^this ++ (\octave: value);
    }
}