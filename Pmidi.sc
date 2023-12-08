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
