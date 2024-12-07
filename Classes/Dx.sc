/*
TODO: Normalize sound
TODO: All devices should have the same instruments or avoid error?
TODO: Intro / Fill in
TODO: Unit tests
*/

Dx : Px {
    classvar <>drumMachine;
    classvar hasLoadedPresets;
    classvar <>lastPreset;
    classvar <presetsDict;
    classvar <presetPatterns;

    *initClass {
        drumMachine = 808;
        lastPreset = Array.new;
        this.prCreatePresetsDict;

        ^super.initClass;
    }

    *new { | newPattern|
        newPattern = this.prAddDrumMachinePlayBuf(newPattern);

        ^super.new(newPattern);
    }

    *loadPresets {
        hasLoadedPresets = true;
        this.prCreatePresetsDict;
    }

    *play {  |fadeTime = 10|
        this.prFadeDrums(\in, fadeTime);
    }

    *use { |newDrumMachine|
        var drumMachines = [606, 707, 808, 909];
        var currentDrumMachine = drumMachine;

        if (drumMachines.includes(newDrumMachine).not)
        { ^this.prPrint("🔴 Drum machine not found") };

        drumMachine = newDrumMachine;

        last do: { |pattern, i|
            if (pattern[\dx] == true) {
                pattern[\drumMachine] = newDrumMachine;
                this.new(pattern);
            };
        }
    }

    *preset { |name, number|
        var newPreset = [name, number];

        if (newPreset != lastPreset or: (hasLoadedPresets == true)) {
            this.prCreatePatternFromPreset(name, number);
        };

        presetPatterns do: { |pattern, i|
            // Returns 600, 700, 800 or 900
            var hundred = drumMachine - (drumMachine % 10);
            var id = hundred * 100 + i;
            this.new(pattern.copy.putAll([\id, id, \drumMachine, drumMachine, \dx, true]));
        }
    }

    *release { |fadeTime = 10|
        this.prFadeDrums(\out, fadeTime);
    }

    *stop {
        ^drumMachine.asSymbol.i(\all);
    }

    *prAddDrumMachinePlayBuf { |pattern|
        var folder = pattern[\drumMachine].asString.catArgs("/", pattern[\i].asString);
        pattern.putAll([\play: [folder, 0]]);
        ^pattern;
    }

    *prCreatePatternFromPreset { |name, number|
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
    }

    *prCreatePresetsDict {
        presetsDict = Dictionary.new;

        PathName(("../Presets/yaml/").resolveRelative).filesDo{ |file|
            var fileName = file.fileNameWithoutExtension.asSymbol;
            var filePath = File.readAllString(file.fullPath);
            presetsDict.put(fileName, PresetsFromYAML(filePath.parseYAML))
        };
    }

    *prFadeDrums { |direction, fadeTime|
        var fade = [direction, fadeTime.clip(0.1, fadeTime)];

        last do: { |pattern|
            if (pattern['drumMachine'] == drumMachine) {
                pattern.putAll([\fade, fade, direction, fadeTime]);
                super.new(pattern);
            };
        };
    }
}
