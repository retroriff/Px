TR08 : Play {
    *new { | patterns, name, quant, trace|
        var drumKit = Dictionary[
            \bd -> 36,
            \rs -> 37,
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

        var isTR08Detected = {
            MIDIClient.destinations.detect({ |endpoint| endpoint.name == "TR-08" }) !== nil
        };

        var midiPairs = {
            if (isTR08Detected.value == true)
            { [\chan: 0] }
            { Array.new }
        };

        if (MIDIClient.initialized == false or: { midiClient.notNil and: { midiClient["TR-08"].isNil } } )
        { this.init(0.195) };

        patterns.collect { |pattern|
            pattern.putAll([\midinote: drumKit[pattern[\i]]] ++ midiPairs.value);
        };

        ^super.new(patterns, name ?? \tr08, quant, trace, midiout: "TR-08");
    }

    *init { |latency|
        Play.initMidi(latency, deviceName: "TR-08");
    }

    *preset { |name, number|
        var presets = Dictionary[
            \electro -> "Presets/electro.scd".resolveRelative.load;
        ];

        var createPatternFromPreset = { |preset|
            var patterns = Array.new;
            if (preset.notNil) {
               preset[\preset].do { |pattern|
                    var amp = Pseq(pattern[\list].clip(0, 1), inf);
                    patterns = patterns.add((i: pattern[\i], amp: amp, dur: 1/4));
               };
            };
            if (preset[\name].notNil)
            { super.prPrint("Preset:".scatArgs(preset[\name])) };
            patterns;
        };

        var preset = presets[name ?? \electro][number ?? 0];
        TR08(createPatternFromPreset.(preset));
    }
}
