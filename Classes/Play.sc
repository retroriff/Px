+ Px {
    *prGenerateDegrees { |pattern|
        var createRandomDegrees, degreesWithVariations;
        if (pattern[\degree].isNil)
        { ^pattern };

        createRandomDegrees = {
            var length = pattern[\length] ?? 1;
            var scale = pattern[\scale] ?? \phrygian;
            var scaleDegrees = Scale.at(scale.asSymbol).degrees;
            var randomDegrees = Array.newClear(length);
            thisThread.randSeed = this.prGetPatternSeed(pattern);
            randomDegrees = length.collect { scaleDegrees.choose };
        };

        degreesWithVariations = { |degrees, numOctaves = 1|
            if (pattern[\arp].notNil) {
                degrees = degrees.collect { |degree|
                    degree + (0..numOctaves).flat.collect { |oct| oct * 7 };
                };

                degrees = degrees.as(Array).flat;
            };

            degrees;
        };

        if (pattern[\degree].isKindOf(Pattern).not) {
            var degrees = pattern[\degree];
            var length = pattern[\midiControl] ?? inf;
            if (degrees == \rand) {
                degrees = createRandomDegrees.value;
            };

            pattern[\degree] = Pseq(degreesWithVariations.(degrees), length);
        };

        if (pattern[\scale].notNil)
        { pattern[\scale] = Scale.at(pattern[\scale]).semitones };

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

    legato { |value|
        ^this.prUpdatePattern([\legato, value]);
    }

    length { |value|
        ^this.prUpdatePattern([\length, value]);
    }

    octave { |value|
        if (value.isArray) {
            value = Pseq(value, inf);
        }

        ^this.prUpdatePattern([\octave, value]);
    }

    root { |value|
        ^this.prUpdatePattern([\root, value]);
    }

    scale { |value|
        ^this.prUpdatePattern([\scale, value]);
    }
}

+ Symbol {
    arp {}
    degree {}
    legato {}
    length {}
    octave {}
    root {}
    scale {}
}

+ Event {
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

    root { |key|
        ^this ++ (\root: key);
    }

    scale { |key|
        ^this ++ (\scale: Scale.at(key).semitones);
    }
}