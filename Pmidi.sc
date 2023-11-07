Pmidi : Px {
    classvar <midiClient, name = \pmidi;

    *new { | patterns |
        patterns.collect { |pattern|
            pattern = pattern.putAll([
                \chan, pattern[\chan] ?? 0,
                \degree: pattern[\degree] ?? 0,
                \ins: \midi,
                \midicmd: \noteOn,
                // \midinote: pattern[\note] ?? 69, // A4
                \midiout: midiClient,
                \legato: pattern[\legato] ?? pattern[\dur] ?? 1,
                // \sustain: pattern[\sus] ?? pattern[\dur] ?? 1/4,
                \type: \midi,
            ]);
        };
        patterns.postln;
        ^super.new(patterns, name)
    }

    *init { | latency |
        var tempo;
        MIDIClient.init;
        midiClient = MIDIOut.new(0);
        midiClient.latency = latency ?? 0.2;
        tempo = LinkClock(85/60).latency_(Server.default.latency);
        tempo.tempo = 85/60;
    }

    *stop {
        ^super.stop(name);
    }
}
