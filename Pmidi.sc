Pmidi {
    *init { | latency, deviceName, portName |
        Play.initMidi(latency, deviceName, portName);
    }
}

+Play {
    *prCreateMidiPatterns { | patterns, midiout|
        var isMidiControl = { |pattern|
            if (pattern[\hasGate] == false or: { pattern[\midicmd] == \noteOff })
            { true }
        };

        var addMidiTypes = { |pattern|
            if (MIDIClient.initialized == false)
            { this.initMidi };

            midiout = midiout ?? "default";

            pattern.putAll([
                \type: \midi,
                \midicmd: pattern[\midicmd] ?? \noteOn,
                \midiout: midiClient[midiout],
                \chan, pattern[\chan] ?? 0,
                \ins: \midi
            ]);
        };

        patterns.collect { |pattern|
            var isMidi = if (pattern[\chan].notNil) { true };

            if (isMidi == true)
            { pattern = addMidiTypes.(pattern) };

            if (isMidiControl.(pattern)  == true)
            { pattern ++ (\midiControl: 1) };
        };

        ^patterns;
    }

    *initMidi { | latency, deviceName, portName |
        var midiOut;

        MIDIClient.init(verbose: false);

        if (deviceName.notNil) {
            if (portName.isNil)
            { portName = deviceName };
            midiOut = MIDIOut.newByName(deviceName, portName);
            super.prPrint("MIDIOut:".scatArgs(deviceName, deviceName));
        } {
            midiOut = MIDIOut.new(0);
            super.prPrint("MIDIOut:".scatArgs("port 0"));
        };

        deviceName = deviceName ?? "default";

        if (midiClient.isNil) {
            midiClient = Dictionary[deviceName -> midiOut ]
        } {
            midiClient.add(deviceName -> midiOut);
        };

        midiClient[deviceName].latency = latency ?? 0.2;
    }
}

+Event {
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

    hold {
        ^this ++ (\hasGate: false) ++ this.prSendSingleMessage;
    }

    holdOff {
        ^this ++ (\midicmd: \allNotesOff) ++ this.prSendSingleMessage;
    }

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

    prSendSingleMessage {
        (\dur: Pseq([1], 1));
    }
}