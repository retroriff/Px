+ Px {
    *prGenerateDegrees { |pattern|
        var createRandomDegrees, degreesWithVariations;

        if (pattern[\degree].isNil)
        { ^pattern };

        createRandomDegrees = { |size = 1|
            var scale = pattern[\scale] ?? \phrygian;
            var scaleDegrees = Scale.at(scale.asSymbol).degrees;
            var randomDegrees = Array.newClear(size);

            thisThread.randSeed = super.prGetPatternSeed(pattern);
            randomDegrees = size.collect { scaleDegrees.choose };
        };

        degreesWithVariations = { |degrees, numOctaves = 1|
            "kaka3".postln;
            if (pattern[\arp].notNil) {
                degrees = degrees.collect { |degree|
                    degree + (0..numOctaves).flat.collect { |oct| oct * 7 };
                };

                degrees = degrees.as(Array).flat;
            };

            "kaka4".postln;
            degrees.postln;
            degrees;
        };


        if (pattern[\degree].isKindOf(Event)) {
            var length = pattern[\midiControl] ?? inf;
            var degrees = pattern[\degree];

            "kaka1".postln;

            if (degrees == \rand) {
                degrees = createRandomDegrees.(pattern[\size]);
            };

            pattern[\degree] = Pseq(degreesWithVariations.(degrees), length);
        };

        "kaka2".postln;

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
        ^this.prUpdatePattern([\scale, Scale.at(value).semitones]);
    }

    size { |value|
        ^this.prUpdatePattern([\size, value]);
    }
}

+ Symbol {
    arp {}
    degree {}
    octave {}
    root {}
    scale {}
    size {}
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