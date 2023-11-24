Pmidi {
    *init { | latency |
        Pnotes.initMidi(latency);
    }
}

+Pnotes {
    *prCreateMidiPatterns { | patterns|
        var isMidiControl = { |pattern|
            if (pattern[\hasGate] == false or: { pattern[\midicmd] == \noteOff })
            { true }
        };

        var addMidiTypes = { |pattern|
            if (MIDIClient.initialized == true)
            {
                pattern.putAll([
                    \type: \midi,
                    \midicmd: pattern[\midicmd] ?? \noteOn,
                    \midiout: midiClient,
                    \chan, pattern[\chan] ?? 0,
                    \ins: \midi
                ]);
            }
            { ^super.prPrint("MIDIClient not initialized. Use Pmidi.init") }
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

    *initMidi { | latency |
        MIDIClient.init;
        midiClient = MIDIOut.new(0);
        midiClient.latency = latency ?? 0.2;
    }
}
