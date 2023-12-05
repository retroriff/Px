+Px {
    *prCreateBufIns { |patterns|
        patterns = patterns.collect { |pattern|
            pattern[\play].notNil.if {
                pattern = pattern ++ (i: \playbuf, buf: pattern[\play]);
                pattern.removeAt(\play);
            };

            pattern[\loop].notNil.if {
                pattern = pattern ++ (i: \lplay, buf: pattern[\loop]);
                pattern.removeAt(\loop);
            };

            pattern;
        };

        ^patterns;
    }

    *prCreateLoops { |patterns|
        patterns.do { |pattern|
            if (pattern[\buf].notNil) {
                var filesCount = ~buf.(pattern[\buf][0]).size;

                if (filesCount > 0 and: { pattern[\buf].isArray }) {
                    var buf;

                    var getJumpBufs = {
                        var minLength = 1, mixLength = pattern[\dur], steps = 16;
                        var mixBuf = {
                            var initialBuf = (~buf.(pattern[\buf][0]).size).rand;
                            var buf = Array.fill(minLength, initialBuf);
                            var rest = (steps - minLength) / minLength;
                            thisThread.randSeed = this.prGetPatternSeed(pattern);
                            rest.do({
                                var newBuf = (~buf.(pattern[\buf][0]).size).rand;
                                buf = buf ++ Array.fill(minLength, newBuf);
                            });
                            buf;
                        };
                        pattern[\dur] = mixLength / steps;
                        pattern[\beats] = mixLength;
                        pattern[\start] = Pseq((0..steps - 1) / steps, inf);
                        Pseq(~buf.(pattern[\buf][0], mixBuf.value), inf);
                    };

                    var getRandSeqBufs = {
                        thisThread.randSeed = this.prGetPatternSeed(pattern);
                        Pseq(~buf.(pattern[\buf][0], Array.rand(8, 0, filesCount - 1)), inf);
                    };

                    var getRandBuf = {
                        thisThread.randSeed = this.prGetPatternSeed(pattern);
                        ~buf.(pattern[\buf][0], (~buf.(pattern[\buf][0]).size).rand);
                    };

                    if (pattern[\i] == \lplay) {
                        var sampleLength = pattern[\buf][0].split($-);
                        if (sampleLength.isArray and: { sampleLength.size > 1 } and: { sampleLength[1].asInteger > 0 })
                        { pattern[\dur] = pattern[\dur] ?? sampleLength[1].asInteger };
                    };

                    if (pattern[\degree].notNil) {
                        var degree = Play.prGenerateDegrees(pattern);
                        pattern[\rate] = degree.midiratio;
                    };

                    buf = switch (pattern[\buf][1])
                    { \rand } { getRandSeqBufs.value }
                    { \jump } { getJumpBufs.value }
                    { nil } { getRandBuf.value }
                    { ~buf.(pattern[\buf][0], pattern[\buf][1]) };

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
        };

        ^patterns;
    }
}

+Event {
    rand { |folder|
        ^this.putAll([\buf, [folder, \rand]]);
    }

    rate { |args|
        ^this.putAll([\rate, this.prCreatePatternKey(args)]);
    }

    trim { |startPosition|
        startPosition = if (startPosition.isNil)
        { \seq }
        { startPosition.clip(0, 0.75) };

        ^this.putAll([\trim, startPosition]);
    }
}