/*
TODO: Normalize 626, 727
TODO: Solo method. Example: Dx.solo(\bd)
TODO: Normalize sound (909)
TODO: Intro / Fill in
*/

Dx : Px {
  classvar <>drumMachine;
  classvar <>drumMachines;
  classvar <>dxAmp;
  classvar <fx;
  classvar hasLoadedPresets;
  classvar <instrumentFolders;
  classvar <>lastPreset;
  classvar <presetsDict;
  classvar <presetPatterns;

  *initClass {
    drumMachine = 808;
    drumMachines = [505, 606, 626, 707, 727, 808, 909];
    dxAmp = 0.6;

    fx = Dictionary.new;
    instrumentFolders = Dictionary.new;
    lastPreset = Array.new;
    this.prCreatePresetsDict;

    ^super.initClass;
  }

  *new { | newPattern|
    newPattern = this.prAddDrumMachinePlayBuf(newPattern);

    ^super.new(newPattern);
  }

  *delay { |value = 0.3|
    fx.put(\delay, value);
    this.preset(lastPreset[0], lastPreset[1], dxAmp);
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
      this.prCreatePatternFromPreset(newPreset);
    };

    presetPatterns do: { |pattern, i|
      var id = this.prCreateId(i);

      if (this.prHasInstrument(pattern[\instrument]) == true) {
        var newPattern = this.prAddFxToPattern(pattern);

        this.new(newPattern.putAll([
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

  *reverb { |value = 0.3|
    fx.put(\reverb, value);
    this.preset(lastPreset[0], lastPreset[1], dxAmp);
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
        // Px.stop has a fork that kills the Ndefs
        var stopPattern = { |id|
          last.removeAt(id);
          ndefList.removeAt(id);
          Pdef(id).source = nil;
          soloList.remove(id);
        };

        stopPattern.(pattern[\id]);

        if (this.prHasInstrument(pattern[\instrument]) == true) {
          var lastTwoDigits = pattern[\id] % 10;
          pattern[\id] = this.prCreateId(lastTwoDigits);

          pattern[\drumMachine] = newDrumMachine;
          this.new(pattern);
        } {
          last[pattern[\id]] = pattern;
        }
      };
    }
  }

  *vol { |amp|
    if (amp.isNil)
    { ^dxAmp };
    
    dxAmp = amp;

    if (lastPreset.notEmpty)
    { this.preset(lastPreset[0], lastPreset[1], amp) };
  }

  *prAddDrumMachinePlayBuf { |pattern|
    var folder, sample, subfolder;
    var patternDrumMachine = pattern[\drumMachine].asString;
    var file = pattern[\file] ?? 0;

    subfolder = patternDrumMachine.toLower ++ "-" ++ pattern[\instrument].asString;
    folder = (patternDrumMachine ++ "/" ++ subfolder);
    pattern.putAll([\play: [folder, file]]);
    ^pattern;
  }

  *prAddFxToPattern { |pattern|
    var allFx = Array.new;

    if (fx.notNil and: { fx.size > 0 }) {
      fx keysValuesDo: { |key, value|
        if ([0, Nil].includes(value))
        { fx.removeAt(key) }
        { allFx = allFx ++ [[\fx, key, \mix, value]]; };
      };

      if (allFx.size > 0) {
        pattern.put(\fx, allFx);
      };
    };
    
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

  *prCreatePatternFromPreset { |newPreset|
    var newName = newPreset[0] ?? \electro;
    var newNumber = newPreset[1] ?? 1;
    var newAmp = newPreset[2] ?? dxAmp;

    var patterns = Array.new;
    var presetNumber, preset;
    var presetGroup = presetsDict[newName];

    var hasNewName = newName != lastPreset[0];
    var hasNewNumber = newNumber != lastPreset[1];
    var hasNewPreset = hasNewName == true or: { hasNewNumber == true };

    presetNumber = newNumber.clip(1, presetGroup.size) - 1;
    preset = presetGroup[presetNumber];

    if (newNumber > presetGroup.size) {
      super.prPrint("🧩 This set has".scatArgs(presetGroup.size, "presets"));
    };

    if (preset.notNil) {
      preset[\preset].do { |pattern|
        var ampSeq = Pseq(pattern[\list].clip(0, newAmp), inf);
        patterns = patterns.add(
          (
            instrument: pattern[\instrument],
            amp: ampSeq,
            dur: 1/4
          )
        );
      };
    };

    if (preset[\name].notNil and: { hasNewPreset == true })
    { super.prPrint("🎧 Preset:".scatArgs(preset[\name])) };

    dxAmp = newAmp;
    hasLoadedPresets = false;
    lastPreset = [newName, newNumber, newAmp];
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
    ^instrumentFolders[drumMachine.asString].any { |folder|
      folder.asString.endsWith("-" ++ instrument.asString)
      or: { folder.asString == instrument.asString }
    };
  }
}
