Dx : Px {
  classvar <>drumMachine;
  classvar <>dxAmp;
  classvar <fx;
  classvar hasLoadedPresets;
  classvar <instrumentFolders;
  classvar <>lastPreset;
  classvar <presetsDict;
  classvar <presetPatterns;

  *initClass {
    drumMachine = 808;
    dxAmp = 0.3;

    fx = Dictionary.new;
    instrumentFolders = Dictionary.new;
    lastPreset = Array.new;
    this.prCreatePresetsDict;

    CmdPeriod.add { lastPreset = Array.new };

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

  *fill { |instrument = \sd|
    var hasCrashInstrument, savedBeat, sdPattern;

    if (instrumentFolders.isEmpty)
    { this.prGetInstrumentFolders };

    this.new((
      instrument: instrument.asSymbol,
      amp: dxAmp,
      beat: true,
      dur: 0.25,
      drumMachine: drumMachine,
      id: this.prCreateId(instrument),
      repeat: 1,
      seed: \rand,
      weight: 0.6,
    ));

    if (instrument.asSymbol != \cr and: { this.prHasInstrument(\cr) })
    { hasCrashInstrument = true };


    fork {
      4.wait;

      if (lastPreset.size > 0)
      { this.preset(lastPreset[0], lastPreset[1], dxAmp) };

      if (hasCrashInstrument == true) {
        this.new((
          instrument: \cr,
          amp: dxAmp,
          drumMachine: drumMachine,
          dx: true,
          id: (instrument.asString ++ "FillIn" ++ drumMachine.asString).asSymbol,
        ));
      };
    };
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

  *preset { |name = \core, number, amp|
    var newPreset = [name.asSymbol, number, amp];

    if (instrumentFolders.isEmpty)
    { this.prGetInstrumentFolders };

    if (newPreset != lastPreset or: (hasLoadedPresets == true)) {
      this.prCreatePatternFromPreset(newPreset);
    };

    presetPatterns do: { |pattern, i|
      if (this.prHasInstrument(pattern[\instrument]) == true) {
        var id = this.prCreateId(pattern[\instrument]);
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

  *shuffle {
    var folders = this.prGetDrumMachinesFolders;
    var randomIndex = folders.size.rand;

    Dx.use(folders[randomIndex]);
    this.prPrint("🎲 Drum machine:".scatArgs(folders[randomIndex]));
  }


  *solo { |instruments, ins2, ins3, ins4, ins5|
    var hasCommon, lastInstruments, soloIds;

    if (instruments.isNil)
    { ^this.prPrint("🟡 Provide at least one instrument to solo") };

    if (instruments.isArray == false) {
      instruments = [instruments, ins2, ins3, ins4, ins5];
      instruments = instruments.reject(_.isNil).collect(_.asSymbol);
    };

    soloIds = last.asArray
    .select { |pattern|
      pattern[\dx] == true
      and: (instruments.includes(pattern[\instrument].asSymbol))
    }
    .collect { |pattern| pattern[\id] };

    lastInstruments = last.asArray
    .select { |pattern|
      pattern[\dx] == true
      and: (instruments.includes(pattern[\instrument].asSymbol))
    }
    .collect { |pattern| pattern[\instrument].asSymbol };

    hasCommon = lastInstruments.any { |id| instruments.includes(id) };

    if (hasCommon == false)
    { ^this.prPrint("🔴 No matching instruments to solo") };

    last.copy do: { |pattern|
      if (soloIds.includes(pattern[\id]) == false
        and: { pattern[\dx] == true }) {
        Px.stop(pattern[\id]);
      }
    };
  }

  *stop {
    ^this.prStopPreset;
  }

  *use { |newDrumMachine|
    var currentDrumMachine = drumMachine;
    var lastPatterns = Px.last.copy;

    if (newDrumMachine.isNil)
    { ^currentDrumMachine };

    if (currentDrumMachine == newDrumMachine)
    { ^this.prPrint("🟢 Drum machine already selected") };

    drumMachine = newDrumMachine;

    this.prStopPreset;

    lastPatterns do: { |pattern, i|
      if (pattern[\dx] == true) {
        if (this.prHasInstrument(pattern[\instrument]) == true) {
          pattern[\id] = this.prCreateId(pattern[\instrument]);

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

  *prCreateId { |instrument|
    ^(instrument.asString ++ drumMachine.asString).asSymbol;
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

    if (lastPreset.notNil) {
      this.prStopPreset;
    };

    if (preset.notNil) {
      preset[\preset].do { |pattern|
        var beats = pattern[\list].clip(0, newAmp);

        patterns = patterns.add(
          (
            instrument: pattern[\instrument],
            amp: newAmp,
            beat: true,
            beatSet: beats,
            dur: 1/4
          )
        );
      };
    };

    if (preset.notNil and: { preset[\name].notNil } and: { hasNewPreset == true })
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
      if (pattern[\drumMachine] == drumMachine) {
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

  *prStopPreset {
    // Px.stop has a fork that kills the Ndefs
    var stopPattern = { |id|
      last.removeAt(id);
      ndefList.removeAt(id);
      Pdef(id).source = nil;
    };

    last.copy do: { |pattern, i|
      if (pattern[\dx] == true) {
        stopPattern.(pattern[\id]);
      }
    };
  }
}
