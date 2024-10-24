/*
TODO: Controls [\rand, 0, 0.1] i [\wrand, 0, 0.1, 0.9]
TODO: MIDIOut instances
*/

+ Px {
    *initMidi { | latency, deviceName, portName |
        var midiOut;

        MIDIClient.init(verbose: false);

        if (deviceName.notNil and: (this.prDetectDevice(deviceName) == false)) {
            this.prPrint("🔴 Device not detected");
            ^this.prPrint("✅ Playing SynthDefs");
        };

        if (deviceName.notNil) {
            if (portName.isNil)
            { portName = deviceName };

            midiOut = MIDIOut.newByName(deviceName, portName);
            this.prPrint("🎛️ MIDIOut:".scatArgs(deviceName));
        } {
            midiOut = MIDIOut.new(0);
            this.prPrint("🎛️ MIDIOut:".scatArgs("port 0"));
        };

        deviceName = deviceName ?? "default";

        if (midiClient.isNil) {
            midiClient = Dictionary[deviceName -> midiOut ]
        } {
            midiClient.add(deviceName -> midiOut);
        };

        midiClient[deviceName].latency = latency ?? 0.2;
    }

    *prCreateMidiPatterns { |pattern|
        var midiout = pattern[\midiout] ?? "default";

        var isMidiControl = {
            if (pattern[\hasGate] == false or: { pattern[\midicmd] == \noteOff })
            { true }
        };

        var addMidiTypes = {
            if (MIDIClient.initialized == false)
            { this.initMidi };

            pattern.putAll([
                \type: \midi,
                \midicmd: pattern[\midicmd] ?? \noteOn,
                \midiout: midiClient[midiout],
                \chan, pattern[\chan] ?? 0,
                \ins: \midi
            ]);
        };

        var isMidi = if (pattern[\chan].notNil) { true };

        if (isMidi == true)
        { pattern = addMidiTypes.value };

        if (isMidiControl.value == true)
        { pattern ++ (\midiControl: 1) };

        ^pattern;
    }


    *prDetectDevice { |name|
        ^MIDIClient.destinations.detect({ |endpoint|
            endpoint.name == name;
        }) !== nil;
    }
}

+ Number {
    chan { |value, midiControlEvent|
        var id = this.asSymbol;
        var newPattern = (
            chan: value,
            id: id,
        );

        if (midiControlEvent.notNil)
        { newPattern = newPattern.putPairs(midiControlEvent) };

        Px(newPattern);
    }

    control { |value|
        var ctlNum = value[0];
        var control = value[1];

        var controlEvent = (
            \midicmd: \control,
            \ctlNum: ctlNum,
            \control: this.prCreateControl(control)
        ).asPairs;

        var previousPattern = Px.lastPatterns[(this  - 1).asSymbol];

        if (ctlNum.isInteger)
        { controlEvent = controlEvent ++ this.prSendSingleMessage };

        ^this.chan(previousPattern[\chan], controlEvent);
    }

    device { |value|
        this.prUpdatePattern([\midiout, value]);
    }

    hold { |value|
        if (value == 1)
        { ^this.prUpdatePattern([\hasGate, false] ++ this.prSendSingleMessage) }
        { ^this.prUpdatePattern([\midicmd, \noteOff]) };
    }

    note { |value|
        this.prUpdatePattern([\midinote, value]);
    }

    panic {
        ^this.prUpdatePattern([\midicmd, \allNotesOff] ++ this.prSendSingleMessage);
    }

    prConvertToMidiValue { |value|
        ^value.clip(0, 1) * 127 / 1;
    }

    prCreateControl { |value|
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
        ^(\dur: Pseq([1], 1)).asPairs;
    }
}

+ Symbol {
    chan {}
    control {}
    hold {}
    note {}
    midiout {}
    panic {}
}
