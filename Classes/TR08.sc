/*
TODO: Unit tests
*/

TR08 : Play {
    classvar <lastPreset;
    classvar <presetsDict;
    classvar <presetPatterns;

    *initClass {
        lastPreset = Array.new;
        presetsDict = Dictionary.new;
        ^super.initClass;
    }

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

        if (MIDIClient.initialized == false
            or: { midiClient.notNil and: { midiClient["TR-08"].isNil }}
        )
        { this.init(0.195) };

        patterns.collect { |pattern|
            pattern.putAll([\midinote: drumKit[pattern[\i].asSymbol]] ++ midiPairs.value);
        };

        ^super.new(patterns, name ?? \tr08, quant, trace, midiout: "TR-08");
    }

    *init { |latency|
        Play.initMidi(latency, deviceName: "TR-08");
    }

    *preset { |name, number|
        var newPreset = [name, number];

        var createPatternFromPreset = {
            var presetNumber, preset;
            var patterns = Array.new;
            var presetGroup = presetsDict[name ?? \electro];
            number = number ?? 1;
            presetNumber = number.clip(1, presetGroup.size) - 1;
            preset = presetGroup[presetNumber];

            if (number > presetGroup.size) {
                super.prPrint("🧩 This set has".scatArgs(presetGroup.size, "presets"));
            };

            if (preset.notNil) {
                preset[\preset].do { |pattern|
                    var amp = Pseq(pattern[\list].clip(0, 1), inf);
                    patterns = patterns.add((i: pattern[\i], amp: amp, dur: 1/4));
                };
            };

            if (preset[\name].notNil)
            { super.prPrint("🎧 Preset:".scatArgs(preset[\name])) };

            presetPatterns = patterns;
            lastPreset = [name, number];

        };

        if (presetsDict.size == 0)
        { this.prCreatePresetsDict };

        if (newPreset != lastPreset) {
           createPatternFromPreset.value;
        };

        TR08(presetPatterns);
    }

    *prCreatePresetsDict {
        PathName(("../Presets/yaml/").resolveRelative).filesDo{ |file|
            var fileName = file.fileNameWithoutExtension.asSymbol;
            var filePath = File.readAllString(file.fullPath);
            presetsDict.put(fileName, PresetsFromYAML(filePath.parseYAML))
        };
    }
}
