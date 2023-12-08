TR08 : Play {
    *new { | patterns, name, quant, trace|
        var drumKit = Dictionary[
            \bd -> 36,
            \rm -> 37,
            \sn -> 38,
            \cp -> 39,
            \lt -> 43,
            \ch -> 42,
            \oh -> 46,
            \mt -> 47,
            \cy -> 49,
            \ht -> 50,
            \cb -> 56,
            \ht -> 62,
            \mt -> 63,
            \lt -> 64,
            \ma -> 70,
            \cl -> 75,
        ];

        if (MIDIClient.initialized == false or: { midiClient.notNil and: midiClient["TR-08"].isNil } )
        { this.init(0.195) };

        patterns.collect { |pattern|
            pattern.putAll([\chan: 0, \midinote: drumKit[pattern[\i]]]);
        };
        ^super.new(patterns, name ?? defaultName, quant, trace, midiout: "TR-08");
    }

    *init { |latency|
        Play.initMidi(latency, deviceName: "TR-08");
    }

    *preset {
        var presets = Dictionary[
            \electro -> [
                [
                    (\bd: [1, 2, 0, 0, 5, 0, 0, 1, 0, 0, 0, 0, 5, 0, 7, 0]),
                    (\sn: [0, 0, 0, 0, 5, 0, 0, 0]),
                    (\ma: [1, 0, 0, 4, 0, 0, 7, 0, 0, 0, 3, 0, 0, 6, 0, 0]),
                    (\oh: [0, 0, 3, 0, 0, 6, 0, 0, 1, 0, 0, 4, 0, 0, 7, 0]),
                    (\ch: [1, 2, 3, 4, 5, 0, 7, 0, 1, 2, 0, 4, 5, 0, 7, 8]),
                ],
                [
                    (\bd: [1, 2, 0, 0, 5, 0, 0, 1, 0, 0, 0, 0, 5, 0, 7, 0]),
                    (\sd: [0, 0, 0, 0, 5, 0, 0, 0]),
                    (\rs: [1, 2, 3, 0, 5, 0, 7, 8, 0, 2, 3, 0, 0, 6, 7, 8]),
                    (\ma: [1, 0, 0, 4, 0, 0, 7, 0, 0, 0, 3, 0, 0, 6, 0, 0]),
                    (\cy: [0, 0, 3, 0, 0, 6, 0, 0, 0, 2, 0, 4, 0, 0, 7, 0]),
                    (\oh: [0, 0, 3, 0, 0, 6, 0, 0, 1, 0, 0, 4, 0, 0, 7, 0]),
                    (\ch: [1, 2, 3, 4, 5, 0, 7, 0, 1, 2, 0, 4, 5, 0, 7, 8]),
                ],
            ]
        ];
    }
}
