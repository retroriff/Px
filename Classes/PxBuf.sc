+ Px {
  *buf { |folder, file|
    if (samplesDict[folder].size == 0) {
      this.prPrint("🔴 Folder doesn't exist or empty:" + folder);
      ^samplesDict[folder].size;
    };

    if (file.isNil) {
      ^samplesDict[folder]
    };

    if (file.isArray.not and: { file >= samplesDict[folder].size }) {
      file = samplesDict[folder].size - 1;
      this.prPrint("🔴 Folder" + folder + "maximum number is" + (samplesDict[folder].size - 1));
    };

    ^samplesDict[folder][file];
  }


  *loadSamples { |pxPath|
    var addFileToDictionary = { |folderName, files|
      var audioFiles = files.select { |file|
        file.extension.toLower == "wav" or: { file.extension.toLower == "aiff" }
      };

      samplesDict[folderName] = audioFiles.collect { |file|
        var sf = SoundFile.openRead(file.fullPath);
        var channels = sf.numChannels;
        sf.close;

        if (channels == 1)
        { Buffer.readChannel(Server.default, file.fullPath, channels: [0, 0]) }
        { Buffer.read(Server.default, file.fullPath) };
      };
    };

    var pathsArray;

    drumMachinesPath = Quarks.folder +/+ "tidal-drum-machines/machines/";
    pathsArray = [pxPath, drumMachinesPath];
    samplesDict = Dictionary.new;
    samplesPath = pxPath;
    Dx.prClearInstrumentFolders;

    pathsArray.do { |path|
      var root = PathName(path.standardizePath);
      var folders = root.entries;

      if (File.exists(root.fullPath)) {
        for (0, folders.size - 1, { |i|
          var folder = folders[i];
          var hasFiles = folder.files.size;

          if (hasFiles > 0) {
            addFileToDictionary.(folder.folderName, folder.files);
          } {
            folder.entries.do { |entry|
              var entryHasFiles = entry.files.size;

              if (entryHasFiles > 0) {
                var subFolderName = folder.folderName ++ "/" ++ entry.folderName;
                addFileToDictionary.(subFolderName, entry.files);
              }
            };
          }
        });
      } {
        this.prPrint("🔴 Path does not exist: " ++ root.fullPath);
      }
    };
  }

  *reloadSamples {
    if (samplesPath.isNil) {
      this.prPrint("🔴 No samples loaded yet. Call Px.loadSamples first");
      ^this;
    };

    samplesDict.do { |bufArray| bufArray.do(_.free) };
    this.loadSamples(samplesPath);
    this.prPrint("🔄 Samples reloaded from" + samplesPath);
  }

  *prCreateBufInstruments { |pattern|
    if (pattern[\play].notNil) {
      var folder, file;

      if (pattern[\play].isArray) {
        folder = pattern[\play][0];
        file = pattern[\play][1];
      };

      if (file.isInteger) {
        var resolvedBuf = this.buf(folder, file);

        if (resolvedBuf.class != Buffer)
        { pattern[\bufMissing] = true };

        pattern[\play] = resolvedBuf;
      };

      if (pattern[\dur].isNil and: { pattern[\play].class == Buffer }) {
        var bufDur = pattern[\play].duration * TempoClock.default.tempo;
        pattern[\dur] = Pseq([bufDur], pattern[\repeat] ?? 1);
      };

      pattern = pattern ++ (instrument: \playbuf, buf: pattern[\play]);
      pattern.removeAt(\play);
    };

    if (pattern[\loop].notNil) {
      pattern = pattern ++ (instrument: \loop, buf: pattern[\loop], sendGate: false);
      pattern.removeAt(\loop);
    };

    if (pattern[\grain].notNil) {
      pattern = pattern ++ (instrument: \grainLoop, buf: pattern[\grain], sendGate: false);
      pattern.removeAt(\grain);
    };

    if (pattern[\dur].isNumber and: { pattern[\dur] < 0 }) {
      pattern[\rate] = -1;
      pattern[\dur] = pattern[\dur].abs;
    };

    ^pattern ++ (fix: 1);

  }

  *prCreateLoops { |pattern|
    if (pattern[\buf].notNil and: { pattern[\buf].class != Buffer }) {
      var filesCount = this.buf(pattern[\buf][0]).size;

      if (filesCount > 0 and: { pattern[\buf].isArray }) {
        var buf;

        var getJumpBufs = {
          var minLength = 1, mixLength = pattern[\dur], steps = 16;

          var mixBuf = {
            var initialBuf = (this.buf(pattern[\buf][0]).size).rand;
            var buf = Array.fill(minLength, initialBuf);
            var rest = (steps - minLength) / minLength;

            thisThread.randSeed = this.prGetPatternSeed(pattern);

            rest.do({
              var newBuf = (this.buf(pattern[\buf][0]).size).rand;
              buf = buf.addAll(Array.fill(minLength, newBuf));
            });

            buf;
          };

          pattern[\dur] = mixLength / steps;
          pattern[\beats] = mixLength;
          pattern[\start] = Pseq((0..steps - 1) / steps, inf);
          Pseq(this.buf(pattern[\buf][0], mixBuf.value), inf);
        };

        var getRandSeqBufs = {
          var files;
          thisThread.randSeed = this.prGetPatternSeed(pattern);

          if (pattern[\seed] == \rand) {
            files = (0..filesCount - 1);
            Pxrand(this.buf(pattern[\buf][0], files), inf);
          } {
            files = Array.rand(8, 0, filesCount - 1);
            Pseq(this.buf(pattern[\buf][0], files), inf);
          };
        };

        var getRandBuf = {
          thisThread.randSeed = this.prGetPatternSeed(pattern);
          this.buf(pattern[\buf][0], (this.buf(pattern[\buf][0]).size).rand);
        };

        if (pattern[\instrument] == \loop or: { pattern[\instrument] == \grainLoop }) {
          var sampleLength = pattern[\buf][0].split($-);
          var folderBeats = 1;

          if (sampleLength.isArray and: { sampleLength.size > 1 } and: { sampleLength[1].asInteger > 0 })
          { folderBeats = sampleLength[1].asInteger };

          if (pattern[\length].notNil) {
            pattern[\beats] = pattern[\dur] ?? folderBeats;
            pattern[\dur] = pattern[\length];
            pattern.removeAt(\length);
          } {
            if (pattern[\dur].isNil)
            { pattern[\dur] = Pseq([folderBeats], pattern[\repeat] ?? 1) };
          };
        };

        if (pattern[\degree].notNil) {
          var patternWithdegrees = this.prCreateDegrees(pattern, midiratio: true);
          pattern[\rate] = patternWithdegrees[\degree];
        };

        case
        { pattern[\buf][1] == \rand }
        { buf = getRandSeqBufs.value }

        { pattern[\buf][1] == \jump }
        { buf = getJumpBufs.value }

        { pattern[\buf][1].isNil }
        { buf = getRandBuf.value }

        { pattern[\buf][1].isArray }
        { buf = Pseq(this.buf(pattern[\buf][0], pattern[\buf][1]), inf) }

        { buf = this.buf(pattern[\buf][0], pattern[\buf][1]) };

        if (pattern[\trim].notNil) {
          if (pattern[\trim] == \seq)
          { pattern[\trim] = (Pseed(Pdup(4, Pseq((0..10), inf)), Prand((0..3), 4) / 4)) };

          pattern[\beats] = pattern[\dur];
          pattern[\dur] = pattern[\dur] / 4;
          pattern[\start] = pattern[\trim];
        };

        if ([Buffer, Pseq, Pxrand].includes(buf.class))
        { pattern[\buf] = buf }
        { pattern[\amp] = 0 };
      }
      { pattern[\amp] = 0 };
    };

    ^pattern;
  }
}

+ Number {
  r { |value|
    this.prDebouncer.enqueue([\rate, value]);
  }

  start { |value|
    this.prDebouncer.enqueue([\start, value]);
  }

  trim { |startPosition|
    case
    { startPosition.isNil or: (startPosition == 1) }
    { startPosition = \seq }

    { startPosition.isArray }
    { startPosition = Pseq(startPosition, inf) }

    { startPosition = startPosition.clip(0, 0.75) };

    this.prDebouncer.enqueue([\trim, startPosition]);
  }
}

+ Symbol {
  // Prevent methods to generate errors when a Px is stopped through a symbol
  r {}
  start {}
  trim {}
}
