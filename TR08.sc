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
        patterns.collect { |pattern|
            pattern.putAll([\chan: 0, \midinote: drumKit[pattern[\i]]]);
        };
        ^super.new(patterns, name ?? defaultName, quant, trace);
    }

    *init { |latency|
        Play.initMidi(latency, deviceName: "TR-08");
    }
}
