/*
TODO: Use any bank of drum machine samples.
TODO: Tidal Drum Machines. Create Drum Selector GUI;

TODO: Solo method. Example: Dx.solo(\bd)
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

    ^super.initClass;
  }

  *new { | newPattern|
    newPattern = this.prAddDrumMachinePlayBuf(newPattern);

    ^super.new(newPattern);
  }

  *in { |fadeTime = 16|
    this.prFadeDrums(\in, fadeTime);
  }

  *loadPresets {
    hasLoadedPresets = true;
    this.prCreatePresetsDict;
  }

  *out { |fadeTime = 16|
    this.prFadeDrums(\out, fadeTime);
  }

  *play { |fadeTime = 16|
    this.prFadeDrums(\in, fadeTime);
  }

  *preset { |name, number, amp|
    var newPreset = [name.asSymbol, number, amp];

    if (instrumentFolders.isEmpty)
    { this.prGetInstrumentFolders };

    if (newPreset != lastPreset or: (hasLoadedPresets == true)) {
      this.prCreatePatternFromPreset(name, number, amp);
    };

    presetPatterns do: { |pattern, i|
      var id = this.prCreateId(i);

      if (this.prHasInstrument(pattern[\instrument]) == true) {
        this.new(pattern.copy.putAll([
          \id, id,
          \drumMachine, drumMachine,
          \dx, true,
        ]));
      }
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

    if (currentDrumMachine == newDrumMachine)
    { ^this.prPrint("🟢 Drum machine already selected") };

    drumMachine = newDrumMachine;

    last.copy do: { |pattern, i|
      if (pattern[\dx] == true) {
        Px.stop(pattern[\id]);

        if (this.prHasInstrument(pattern[\instrument]) == true) {
          var lastTwoDigits = pattern[\id] % 10;
          pattern[\id] = this.prCreateId(lastTwoDigits);

          pattern[\drumMachine] = newDrumMachine;
          this.new(pattern);
        }
      };
    }
  }

  *prAddDrumMachinePlayBuf { |pattern|
    var folder, sample, subfolder;
    var patternDrumMachine = pattern[\drumMachine].asString;
    var file = pattern[\file] ?? 0;

    if (drumMachines.includes(patternDrumMachine.asInteger)) {
      patternDrumMachine = "RolandTR" ++ patternDrumMachine;
    };

    subfolder = patternDrumMachine.toLower ++ "-" ++ pattern[\instrument].asString;
    folder = (patternDrumMachine ++ "/" ++ subfolder);
    pattern.putAll([\play: [folder, file]]);
    ^pattern;
  }

  *prCreateId { |i|
    var hundred;

    if (drumMachine.isInteger) {
      // Returns 600, 700, 800 or 900
      hundred = drumMachine - (drumMachine % 10);
    } {
      hundred = 100;
    };

    ^hundred * 100 + i;
  }

  *prCreatePatternFromPreset { |name, number, amp|
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
        var ampSeq = Pseq(pattern[\list].clip(0, amp ?? 1), inf);
        patterns = patterns.add(
          (
            instrument: pattern[\instrument],
            amp: ampSeq,
            dur: 1/4
          )
        );
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

  *prGetInstrumentFolders {
    if (drumMachinesPath.notNil) {
      var drumMachinesPathName = PathName.new(drumMachinesPath.standardizePath);
      var drumMachinesFolders = drumMachinesPathName.entries
        .select { |entry| entry.isFolder }
        .collect { |entry| entry.folderName };

      drumMachinesFolders do: { |folder|
        var folderPath, subFolders;

        folderPath = PathName(drumMachinesPath ++ folder);

        subFolders = folderPath.entries.collect { |entry|
          if (entry.isFolder)
          { entry.folderName.asSymbol };
        };

        instrumentFolders[folder] = subFolders;
      };
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

  *prHasInstrument { |instrument|
    var drumMachineName = drumMachine;

    if (drumMachines.includes(drumMachine))
    { drumMachineName = "RolandTR" ++ drumMachineName };

    ^instrumentFolders[drumMachineName].any { |folder|
      folder.asString.endsWith("-" ++ instrument.asString)
    };
  }
}
