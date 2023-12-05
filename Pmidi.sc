Pmidi {
    *init { | latency, deviceName, portName |
        Play.initMidi(latency, deviceName, portName);
    }
}

+Play {
    *prCreateMidiPatterns { | patterns|
        var isMidiControl = { |pattern|
            if (pattern[\hasGate] == false or: { pattern[\midicmd] == \noteOff })
            { true }
        };

        var addMidiTypes = { |pattern|
            if (MIDIClient.initialized == false)
            { this.initMidi };

            pattern.putAll([
                \type: \midi,
                \midicmd: pattern[\midicmd] ?? \noteOn,
                \midiout: midiClient,
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
        MIDIClient.init(verbose: false);
        if (deviceName.notNil) {
            if (portName.isNil)
            { portName = deviceName };
            midiClient = MIDIOut.newByName(deviceName, deviceName);
            super.prPrint("MIDIOut:".scatArgs(deviceName, deviceName));
        } {
            midiClient = MIDIOut.new(0);
            super.prPrint("MIDIOut:".scatArgs("port 0"));
        };
        midiClient.latency = latency ?? 0.2;
    }
}
