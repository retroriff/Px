Pmidi : Px {
    classvar <midiClient, defaultName = \pmidi;

    *new { | patterns, name |
        var degreesWithVariations = { |pattern, degreesList, numOctaves = 1|
            if (pattern[\arp].notNil) {
                degreesList.postln;
                degreesList = degreesList.collect { |degree|
                    degree + (0..numOctaves).flat.collect { |oct| oct * 7 };
                };
                degreesList = degreesList.as(Array).flat;
            };

            degreesList;
        };

        var createRandomDegrees = { |pattern, size, degrees|
            thisThread.randSeed = super.prGetPatternSeed(pattern);
            Array.rand(size, 0, degrees[degrees.size - 1]);
        };

        var composeMelody = { |pattern|
            if (pattern[\degree].isArray and: { pattern[\degree][0] == \rand }) {
                var scaleDegrees = Scale.at(pattern[\degree][1].asSymbol).degrees;
                var degreesList = createRandomDegrees.(pattern, pattern[\degree][2], scaleDegrees);
                pattern[\degree] = Pseq(degreesWithVariations.(pattern, degreesList), inf).trace;
            };

            pattern[\degree];
        };

        var createPatterns = {
            patterns.collect { |pattern|
                pattern.putAll([
                    \type: \midi,
                    \midicmd: pattern[\midicmd] ?? \noteOn,
                    \midiout: midiClient,
                    \chan, pattern[\chan] ?? 0,
                    \degree: composeMelody.(pattern) ?? 0,
                    \ins: \midi,
                ]);
            };
            ^super.new(patterns, name ?? defaultName);
        };

        if (MIDIClient.initialized == true)
        { createPatterns.value }
        { "MIDIClient not initialized. Use Pmidi.init".postln; }
    }

    *init { | latency |
        MIDIClient.init;
        midiClient = MIDIOut.new(0);
        midiClient.latency = latency ?? 0.2;
    }

    *shuffle { | name |
        ^super.shuffle(name ?? defaultName);
    }

    *stop { | name |
        ^super.stop(name ?? defaultName);
    }
}

+Event {
    prConvertToMidiValue { |value|
        ^value.clip(0, 1) * 127 / 1;
    }

    prCreateControl { |value |
        var createPwhite = { |lower, upper|
            Pwhite(this.prConvertToMidiValue(lower), this.prConvertToMidiValue(upper));
        };

        var createPwrand = { |item1, item2, weight|
            Pwrand(
                list: [this.prConvertToMidiValue(item1), this.prConvertToMidiValue(item2)],
                weights: [1 - weight, weight],
                repeats: inf
            );
        };

        case
        { value == \rand }
        { ^createPwhite.(0, 1) }

        { value.isArray and: { value[0] == \rand} }
        { ^createPwhite.(value[1], value[2]) }

        { value.isArray and: { value[0] == \wrand} }
        { ^createPwrand.(value[1], value[2], value[3].clip(0, 1)) }

        { value.isNumber }
        { ^this.prConvertToMidiValue(value) };

        ^value ?? 0;
    }

    arp {
        ^this ++ (arp: true);
    }

    control { |ctlNum, value|
        var controlEvent = (
            \midicmd: \control,
            \ctlNum: ctlNum,
            \control: this.prCreateControl(value)
        );

        if (ctlNum.isInteger)
        { controlEvent = controlEvent ++ this.prSendSingleMessage };

        ^this ++ controlEvent;
    }

    degree { |value, scale, size|
        var degreeValue;

        if (value.isInteger)
        { degreeValue = value };

        if (value.isArray)
        { degreeValue = Pseq(value, inf) };

        ^this ++ (\degree: degreeValue ?? [value, scale, size]);
    }

    hold {
        ^this ++ (\hasGate: false) ++ this.prSendSingleMessage;
    }

    holdOff {
        ^this ++ (\midicmd: \noteOff) ++ this.prSendSingleMessage;
    }

    prSendSingleMessage {
        (\dur: Pseq([1], 1));
    }
}