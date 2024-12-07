/*
TODO: Intro / Fill in
TODO: Unit tests
*/

TR08 : Px {
    classvar <drumKit;
    classvar hasLoadedPresets;
    classvar <>lastPreset;
    classvar <presetsDict;
    classvar <presetPatterns;

    *initClass {
        drumKit = Dictionary[
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
        lastPreset = Array.new;
        presetsDict = Dictionary.new;

        ^super.initClass;
    }

    *new { | newPattern|
        this.prInitializeMIDIDevice(newPattern);

        if (this.prIsTR08Detected.value == true)
        { newPattern = this.prAddTR08Pairs(newPattern) }
        { newPattern = this.prAddDrumMachinePlayBuf(newPattern) };

        ^super.new(newPattern);
    }

    *init { |latency, drumMachine|
        if (drumMachine == 808)
        { Px.initMidi(latency, deviceName: "TR-08") };
    }

    *loadPresets {
        hasLoadedPresets = true;
        this.prCreatePresetsDict;
    }

    *play { |fadeTime|
        ^super.play(\tr08, fadeTime);
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

            hasLoadedPresets = false;
            lastPreset = [name, number];
            presetPatterns = patterns;
        };

        if (presetsDict.size == 0)
        { this.prCreatePresetsDict };

        if (newPreset != lastPreset or: (hasLoadedPresets == true)) {
            createPatternFromPreset.value;
        };

        presetPatterns do: { |pattern, i|
            var id = 800 * 100 + i;
            TR08(pattern.copy.putAll([\id, id, \drumMachine, 808]));
        }
    }

    *release { |time = 10|
        var fade = [\out, time.clip(0.1, time)];

        last do: { |pattern|
            if (pattern['drumMachine'] == 808) {
                pattern.putAll([\fade, fade, \out, time]);
            };
        };

        super.new;

        last do: { |pattern|
            if (pattern['drumMachine'] == 808) {
                this.prRemoveFinitePatternFromLast(pattern);
            };
        };
    }

    *stop {
        ^\808.i(\all);
    }

    *prAddDrumMachinePlayBuf { |pattern|
        var folder = pattern[\drumMachine].asString.catArgs("/", pattern[\i].asString);
        pattern.putAll([\play: [folder, 0]]);
        ^pattern;
    }

    *prAddTR08Pairs { |pattern|
        var midinote = drumKit[pattern[\i].asSymbol];
        pattern.putAll([\chan, 0]);
        pattern.putAll([\midinote, midinote]);
        pattern.putAll([\midiout, "TR-08"]);
        ^pattern;
    }

    *prCreatePresetsDict {
        PathName(("../Presets/yaml/").resolveRelative).filesDo{ |file|
            var fileName = file.fileNameWithoutExtension.asSymbol;
            var filePath = File.readAllString(file.fullPath);
            presetsDict.put(fileName, PresetsFromYAML(filePath.parseYAML))
        };
    }

    *prInitializeMIDIDevice { |pattern|
        if (MIDIClient.initialized == false or: { midiClient.notNil and: { midiClient["TR-08"].isNil }})
        { this.init(0.195, pattern[\drumMachine]) };
    }

    *prIsTR08Detected {
        ^MIDIClient.destinations.detect({ |endpoint|
            endpoint.name == "TR-08"
        }) !== nil;
    }
}
