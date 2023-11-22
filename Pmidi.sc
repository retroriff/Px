Pmidi : Px {
    classvar <midiClient, defaultName = \pmidi;

    *new { | patterns, name, quant, trace|
        var degreesWithVariations = { |pattern, degrees, numOctaves = 1|
            if (pattern[\arp].notNil) {
                degrees = degrees.collect { |degree|
                    degree + (0..numOctaves).flat.collect { |oct| oct * 7 };
                };
                degrees = degrees.as(Array).flat;
            };
            degrees;
        };

        var createRandomDegrees = { |pattern, size, degrees|
            var randomDegrees = Array.newClear(size);
            thisThread.randSeed = super.prGetPatternSeed(pattern);
            randomDegrees = size.collect { degrees.choose };
        };

        var isMidiControl = { |pattern|
            if (pattern[\hasGate] == false or: { pattern[\midicmd] == \noteOff })
            { true }
        };

        var getLength = { |pattern|
            if (isMidiControl.(pattern) == true)
            { 1 }
            { inf }
        };

        var createDegrees = { |pattern|
            if (pattern[\degree].isArray) {
                var degrees = pattern[\degree][0];
                var length = getLength.(pattern);
                if (degrees == \rand) {
                    var scaleDegrees = Scale.at(pattern[\degree][1].asSymbol).degrees;
                    degrees = createRandomDegrees.(pattern, pattern[\degree][2], scaleDegrees);
                };
                pattern[\degree] = Pseq(degreesWithVariations.(pattern, degrees), length);
            };

            pattern[\degree];
        };

        var createPatterns = {
            patterns.collect { |pattern|
                pattern.putAll([\degree: createDegrees.(pattern) ?? 0 ]);
                if (pattern[\chan].notNil) {
                    pattern.putAll([
                        \type: \midi,
                        \midicmd: pattern[\midicmd] ?? \noteOn,
                        \midiout: midiClient,
                        \chan, pattern[\chan] ?? 0,
                        \degree: createDegrees.(pattern) ?? 0,
                        \ins: \midi
                    ]);
                }
            };
            ^super.new(patterns, name ?? defaultName, quant, trace);
        };

        if (MIDIClient.initialized == true)
        { createPatterns.value }
        { super.prPrint("MIDIClient not initialized. Use Pmidi.init") }
    }

    *init { | latency |
        MIDIClient.init;
        midiClient = MIDIOut.new(0);
        midiClient.latency = latency ?? 0.2;
    }

    *release { | fadeTime, name |
        ^super.release(fadeTime, name ?? defaultName);
    }

    *shuffle { | name |
        ^super.shuffle(name ?? defaultName);
    }

    *stop { | name |
        ^super.stop(name ?? defaultName);
    }

    *trace { | name |
        ^super.trace(name ?? defaultName);
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
        var pattern;

        if (value.isInteger)
        { value = [value] };

        if (value.isKindOf(Pattern))
        { pattern = value };

        ^this ++ (\degree: pattern ?? [value, scale, size]);
    }

    hold {
        ^this ++ (\hasGate: false) ++ this.prSendSingleMessage;
    }

    holdOff {
        ^this ++ (\midicmd: \allNotesOff) ++ this.prSendSingleMessage;
    }

    prSendSingleMessage {
        (\dur: Pseq([1], 1));
    }
}