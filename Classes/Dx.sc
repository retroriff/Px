/*
TODO: prGetInstrumentFolders to verify that when we use a different device the folder exist
TODO: Normalize sound (909)
TODO: All devices should have the same instruments or avoid error?
TODO: Intro / Fill in
TODO: Unit tests
*/

Dx : Px {
  classvar <>drumMachine;
  classvar <>drumMachines;
  classvar hasLoadedPresets;
  classvar <instrumentFolders;
  classvar <>lastPreset;
  classvar <presetsDict;
  classvar <presetPatterns;

  *initClass {
    drumMachine = 808;
    drumMachines = [606, 707, 808, 909];
    instrumentFolders = Dictionary.new;
    lastPreset = Array.new;
    this.prCreatePresetsDict;
    this.prGetInstrumentFolders;

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

  *use { |newDrumMachine|
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

  *prAddDrumMachinePlayBuf { |pattern|
    var folder = pattern[\drumMachine].asString.catArgs("/", pattern[\instrument].asString);
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
        patterns = patterns.add((instrument: pattern[\instrument], amp: amp, dur: 1/4));
      };
    };

    if (preset[\name].notNil)
    { super.prPrint("🎧 Preset:".scatArgs(preset[\name])) };

    hasLoadedPresets = false;
    lastPreset = [name, number];
    presetPatterns = patterns;
  }

  *prGetInstrumentFolders {
    if (samplesPath.notNil) {
      drumMachines do: { |folder|
        var folderPath = PathName(samplesPath ++ folder);
        var subFolders = folderPath.entries.collect { |entry|
          if (entry.isFolder)
          { entry.folderName };
        };
        instrumentFolders[folder] = subFolders;
        super.prPrint(subFolders);
      };
    }
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
