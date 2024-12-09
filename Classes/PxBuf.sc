+ Px {
    *buf { |folder, file|
        if (samplesDict[folder].size == 0) {
            this.prPrint("🔴 Folder doesn't exist or empty");
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


    *loadSamples { |samplesPath|
        samplesDict = Dictionary.new;
        samplesDict.add(\foldernames -> PathName(samplesPath).entries);

        for (0, samplesDict[\foldernames].size - 1, { |i|
            var folder = samplesDict[\foldernames][i];

            var addFileToDictionary = { |folderName, files|
                samplesDict[folderName] = files.collect({ |file|
                    Buffer.read(Server.default, file.fullPath)
                });
            };

            var hasFiles = folder.files.size;

            if (hasFiles > 0) {
                addFileToDictionary.(folder.folderName, folder.files);
            } {
                folder.entries do: { |entry|
                    var entryHasFiles = entry.files.size;

                    if (entryHasFiles > 0) {
                        var subFolderName = folder.folderName ++ "/" ++ entry.folderName;
                        addFileToDictionary.(subFolderName, entry.files);
                    }
                };
            }
        });
    }

    *prCreateBufInstruments { |pattern|
        pattern[\play].notNil.if {
            pattern = pattern ++ (i: \playbuf, buf: pattern[\play]);
            pattern.removeAt(\play);
        };

        pattern[\loop].notNil.if {
            pattern = pattern ++ (i: \lplay, buf: pattern[\loop]);
            pattern.removeAt(\loop);
        };

        ^pattern ++ (fix: 1);

    }

    *prCreateLoops { |pattern|
        if (pattern[\buf].notNil) {
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
                    files = Array.rand(8, 0, filesCount - 1);
                    Pseq(this.buf(pattern[\buf][0], files), inf);
                };

                var getRandBuf = {
                    thisThread.randSeed = this.prGetPatternSeed(pattern);
                    this.buf(pattern[\buf][0], (this.buf(pattern[\buf][0]).size).rand);
                };

                if (pattern[\i] == \lplay) {
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

                if ([Buffer, Pseq].includes(buf.class))
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
        ^this.prUpdatePattern([\rate, this.prCreatePatternKey(args)]);
    }

    start { |value|
        ^this.prUpdatePattern([\start, value]);
    }

    trim { |startPosition|
        case
        { startPosition.isNil or: (startPosition == 1) }
        { startPosition = \seq }

        { startPosition.isArray }
        { startPosition = Pseq(startPosition, inf) }

        { startPosition = startPosition.clip(0, 0.75) };

        ^this.prUpdatePattern([\trim, startPosition]);
    }
}

+ Symbol {
    // Prevent methods to generate errors when a Px is stopped through a symbol
    r {}
    start {}
    trim {}
}
