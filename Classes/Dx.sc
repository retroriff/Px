Dx : Px {
  classvar <>drumMachine;
  classvar <>dxAmp;
  classvar <activeFx;
  classvar hasLoadedPresets;
  classvar <instrumentFolders;
  classvar instrumentNames;
  classvar <>lastPreset;
  classvar <presetsDict;
  classvar <presetPatterns;
  classvar aliases;

  *initClass {
    aliases = Dictionary[
      \505 -> \RolandTR505,
      \606 -> \RolandTR606,
      \626 -> \RolandTR626,
      \707 -> \RolandTR707,
      \727 -> \RolandTR727,
      \808 -> \RolandTR808,
      \909 -> \RolandTR909,
    ];
    drumMachine = \RolandTR909;
    dxAmp = 0.3;

    activeFx = Dictionary.new;
    instrumentFolders = Dictionary.new;
    instrumentNames = Dictionary.new;
    lastPreset = Array.new;
    this.prCreatePresetsDict;

    CmdPeriod.add { lastPreset = Array.new };

    ^super.initClass;
  }

  *new { | newPattern|
    newPattern = this.prAddDrumMachinePlayBuf(newPattern);

    ^super.new(newPattern);
  }

  *fx { |name, value|
    activeFx.put(name, value);
    this.prApplyFxToAll(name, value);
  }

  *fill { |instrument = \sd, repeat = 1|
    var hasCrashInstrument;

    if (instrumentFolders.isEmpty)
    { this.prGetInstrumentFolders };

    this.new((
      instrument: instrument.asSymbol,
      amp: dxAmp,
      beat: true,
      dur: 0.25,
      drumMachine: drumMachine,
      id: this.prCreateId(instrument),
      repeat: repeat,
      seed: \rand,
      weight: 0.6,
    ));

    if (instrument.asSymbol != \cr and: { this.prHasInstrument(\cr) })
    { hasCrashInstrument = true };

    fork {
      (repeat * 4).wait;

      if (lastPreset.size > 0)
      { this.preset(lastPreset[0], lastPreset[1], dxAmp) };

      if (hasCrashInstrument == true) {
        this.new((
          instrument: \cr,
          amp: dxAmp,
          drumMachine: drumMachine,
          dx: true,
          id: (instrument.asString ++ "FillInDx").asSymbol,
        ));
      };
    };
  }

  *in { |fadeTime = 16|
    this.prFadeDrums(\in, fadeTime);
  }

  *instruments { |machine|
    machine = this.prResolveAlias(machine ? drumMachine);

    if (instrumentFolders.isEmpty)
    { this.prGetInstrumentFolders };

    ^instrumentFolders[machine.asSymbol];
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
    var newIds;

    if (instrumentFolders.isEmpty)
    { this.prGetInstrumentFolders };

    if (newPreset != lastPreset or: (hasLoadedPresets == true)) {
      this.prCreatePatternFromPreset(newPreset);
    };

    newIds = Set.new;

    presetPatterns do: { |pattern|

      if (this.prHasInstrument(pattern[\instrument]) == true) {
        var id = this.prCreateId(pattern[\instrument]);
        newIds.add(id);

        this.new(pattern.putAll([
          \id, id,
          \drumMachine, drumMachine,
          \dx, true,
        ]));
      }
    };

    this.prStopRemovedPatterns(newIds);
    this.prApplyActiveFx;
  }

  *release { |fadeTime = 10|
    this.prFadeDrums(\out, fadeTime);
  }


  *shuffle {
    var folders = this.prGetDrumMachinesFolders;
    var randomIndex = folders.size.rand;

    Dx.use(folders[randomIndex]);
    this.prPrint("🎲 Drum machine:".scatArgs(folders[randomIndex]));
  }


  *solo { |instruments, ins2, ins3, ins4, ins5|
    var soloIds;

    if (instruments.isNil)
    { ^("🟡 Provide at least one instrument to solo") };

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

    if (soloIds.isEmpty)
    { ^("🔴 No matching instruments to solo") };

    last.copy do: { |pattern|
      if (soloIds.includes(pattern[\id]) == false
        and: { pattern[\dx] == true }) {
        last.removeAt(pattern[\id]);
        Pdef(pattern[\id]).source = nil;
      }
    };
  }

  *stop {
    last.copy do: { |pattern|

      if (pattern[\dx] == true)
      { Fx.prClearProxy(pattern[\id]) };
    };

    this.prStopPreset;
    activeFx.clear;
  }

  *use { |newDrumMachine|
    if (newDrumMachine.isNil)
    { ^drumMachine };

    newDrumMachine = this.prResolveAlias(newDrumMachine);

    if (drumMachine == newDrumMachine)
    { ^("🟢 Drum machine already selected") };

    if (instrumentFolders.isEmpty)
    { this.prGetInstrumentFolders };

    if (instrumentFolders[newDrumMachine.asSymbol].isNil)
    { ^("🔴 Drum machine not found:" + newDrumMachine) };

    drumMachine = newDrumMachine;

    last.copy do: { |pattern|

      if (pattern[\dx] == true) {
        if (this.prHasInstrument(pattern[\instrument]) == true) {
          pattern[\drumMachine] = newDrumMachine;
          this.new(pattern);
        } {
          Px.stop(pattern[\id]);
        }
      };
    };

    this.prApplyActiveFx;
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
    var ins = pattern[\instrument].asString;
    var parts = ins.split($:);
    var file = pattern[\file] ?? 0;

    if (parts.size > 1) {
      ins = parts[0];

      if (file == 0) {
        file = parts[1].asInteger;
      };
    };

    subfolder = patternDrumMachine.toLower ++ "-" ++ ins;
    folder = (patternDrumMachine ++ "/" ++ subfolder);

    pattern.putAll([\play, [folder, file]]);
    ^pattern;
  }

  *prApplyActiveFx {
    if (activeFx.size > 0) {
      fork {
        Server.default.sync;

        Fx.prSuppressPrint = true;

        activeFx.keysValuesDo { |fxName, value|
          this.prApplyFxToAll(fxName, value);
        };

        Fx.prSuppressPrint = false;
      };
    };
  }

  *prApplyFxToAll { |fxName, value|
    var fxValue = if ([0, nil].includes(value)) { nil } { value };
    var isFirst = true;

    last.keysValuesDo { |id, pattern|

      if (pattern[\dx] == true) {
        if (isFirst.not)
        { Fx.prSuppressPrint = true };

        Fx(id).perform(fxName, fxValue);
        isFirst = false;
      };
    };

    Fx.prSuppressPrint = false;
  }

  *prClearInstrumentFolders {
    instrumentFolders = Dictionary.new;
    instrumentNames = Dictionary.new;
  }

  *prCreateId { |instrument|
    ^(instrument.asString ++ "Dx").asSymbol;
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

  *prResolveAlias { |name|
    ^(aliases[name.asSymbol] ? name);
  }

  *prGetInstrumentFolders {
    if (drumMachinesPath.notNil) {
      var drumMachinesPathName = PathName.new(drumMachinesPath.standardizePath);
      var drumMachinesFolders = drumMachinesPathName.entries
      .select { |entry| entry.isFolder }
      .collect { |entry| entry.folderName };

      drumMachinesFolders do: { |folder|
        var folderPath, names, subFolders;

        folderPath = PathName(drumMachinesPath ++ folder);

        subFolders = folderPath.entries.collect { |entry|
          if (entry.isFolder)
          { entry.folderName.asSymbol };
        };

        instrumentFolders[folder.asSymbol] = subFolders;

        names = Set.new;

        subFolders.do { |sf|
          var str = sf.asString;
          var dashIndex = str.indexOf($-);

          if (dashIndex.notNil)
          { names.add(str[(dashIndex + 1)..]) }
          { names.add(str) };
        };

        instrumentNames[folder.asSymbol] = names;
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
    var names = instrumentNames[drumMachine.asSymbol];

    if (names.isNil) { ^false };

    ^names.includes(instrument.asString);
  }

  *prStopRemovedPatterns { |keepIds|
    last.copy do: { |pattern|

      if (pattern[\dx] == true and: { keepIds.includes(pattern[\id]).not }) {
        last.removeAt(pattern[\id]);
        Pdef(pattern[\id]).source = nil;
      };
    };
  }

  *prStopPreset {
    last.copy do: { |pattern|

      if (pattern[\dx] == true) {
        last.removeAt(pattern[\id]);
        ndefList.removeAt(pattern[\id]);
        Pdef(pattern[\id]).source = nil;
      };
    };
  }
}
