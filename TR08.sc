TR08 : Play {
    *new { | patterns, name, quant, trace|
        var drumKit = Dictionary[
            \bd -> 36,
            \sn -> 38,
            \lc -> 64,
            \lt -> 43,
            \mc -> 63,
            \mt -> 47,
            \hc -> 62,
            \ht -> 50,
            \cl -> 75,
            \rs -> 37,
            \ma -> 70,
            \cp -> 39,
            \cb -> 56,
            \cy -> 49,
            \oh -> 46,
            \ch -> 42,
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
        var preset, presetsDict = Dictionary();

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

        // TODO: Decide if we want presets to be YAML, JSON or events
        var presetFormat = "yaml";

        PathName(("Presets/" ++ presetFormat ++ "/").resolveRelative).filesDo{ |file|
            var fileName = file.fileNameWithoutExtension;
            var filePath = File.readAllString(file.fullPath);

            case
            { presetFormat == "json" }
            { presetsDict.put(fileName.asSymbol, PresetsFromJSON(filePath.parseJSON)) }

            { presetFormat == "scd" }
            { presetsDict.put(fileName.asSymbol, filePath.load ) }

            { presetFormat == "yaml" }
            { presetsDict.put(fileName.asSymbol, PresetsFromYAML(filePath.parseYAML)) }
        };

        preset = presetsDict[name ?? \electro][number ?? 0];
        TR08(createPatternFromPreset.(preset));
    }
}
