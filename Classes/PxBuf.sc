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
        Buffer.read(Server.default, file.fullPath)
      };
    };

    var pathsArray;

    drumMachinesPath = Quarks.folder +/+ "tidal-drum-machines/machines/";
    pathsArray = [pxPath, drumMachinesPath];
    samplesDict = Dictionary.new;
    samplesPath = pxPath;

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

  *prCreateBufInstruments { |pattern|
    pattern[\play].notNil.if {
      var numChannels = 2;
      var playBuf = \playbuf;
      var folder = pattern[\play][0];
      var file = pattern[\play][1];

      if (file.isInteger) {
        numChannels = Px.samplesDict[folder][file].numChannels;
        playBuf = (numChannels == 1).if(\playbufMono, \playbuf);
      };

      pattern = pattern ++ (instrument: playBuf, buf: pattern[\play]);
      pattern.removeAt(\play);
    };

    pattern[\loop].notNil.if {
      pattern = pattern ++ (instrument: \loop, buf: pattern[\loop]);
      pattern.removeAt(\loop);
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
              buf = buf ++ Array.fill(minLength, newBuf);
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
            Prand(this.buf(pattern[\buf][0], files), inf);
          } {
            files = Array.rand(8, 0, filesCount - 1);
            Pseq(this.buf(pattern[\buf][0], files), inf);
          };
        };

        var getRandBuf = {
          thisThread.randSeed = this.prGetPatternSeed(pattern);
          this.buf(pattern[\buf][0], (this.buf(pattern[\buf][0]).size).rand);
        };

        if (pattern[\instrument] == \loop) {
          var sampleLength = pattern[\buf][0].split($-);
          if (sampleLength.isArray and: { sampleLength.size > 1 } and: { sampleLength[1].asInteger > 0 })
          { pattern[\dur] = pattern[\dur] ?? sampleLength[1].asInteger };
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

        if ([Buffer, Pseq, Prand].includes(buf.class))
        { pattern[\buf] = buf }
        { pattern[\amp] = 0 };
      }
      { pattern[\amp] = 0 };
    };

    ^pattern;
  }
}

+ Number {
  r { |args|
    this.prUpdatePattern([\rate, this.prCreatePatternKey(args)]);
  }

  start { |value|
    this.prUpdatePattern([\start, value]);
  }

  trim { |startPosition|
    case
    { startPosition.isNil or: (startPosition == 1) }
    { startPosition = \seq }

    { startPosition.isArray }
    { startPosition = Pseq(startPosition, inf) }

    { startPosition = startPosition.clip(0, 0.75) };

    this.prUpdatePattern([\trim, startPosition]);
  }
}

+ Symbol {
  // Prevent methods to generate errors when a Px is stopped through a symbol
  r {}
  start {}
  trim {}
}
