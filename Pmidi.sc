Pmidi : Px {
    classvar <midiClient, defaultName = \pmidi;

    *new { | patterns, name |
        var createPatterns = {
            patterns.collect { |pattern|
                pattern = pattern.putAll([
                    \type: \midi,
                    \midicmd: pattern[\midicmd] ?? \noteOn,
                    \midiout: midiClient,
                    \chan, pattern[\chan] ?? 0,
                    \degree: pattern[\degree] ?? 0,
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

    *stop { | name |
        ^super.stop(name ?? defaultName);
    }
}

+Event {
    control { |ctlNum, val|
        var controlEvent = (
            \midicmd: \control,
            \ctlNum: ctlNum,
            \control: val
        );

        if (ctlNum.isInteger)
        { controlEvent = controlEvent ++ this.prSendSingleMessage };

        ^this ++ controlEvent;
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