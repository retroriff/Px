Px {
    classvar chorus, defaultName = \px, <lastPatterns, <seeds;

    *new { | patterns, name, trace |
        var ptparList;

        var copyPatternsToLastPatterns = {
            if (lastPatterns.isNil)
            { lastPatterns = Dictionary[name -> patterns] }
            { lastPatterns[name] = patterns }
        };

        var getSoloPatterns = {
            var soloList = patterns.select { |pattern| pattern['solo'].notNil };
            if (soloList.isEmpty.not)
            { soloList }
            { patterns }
        };

        var createBufIns = {
            patterns.collect { |pattern|
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
        };

        var createIds = {
            var indexDict = Dictionary.new;
            patterns.collect { |pattern|
                var patternStr = pattern.i.asString;
                indexDict[patternStr] = indexDict[patternStr].isNil.if
                { 0 }
                { indexDict[patternStr] + 1 };
                pattern = pattern ++ (id: pattern[\id] ?? (patternStr ++ "_" ++ indexDict[patternStr]));
            };

            patterns;
        };

        var createPatternBeat = { |amp, pattern|
            var seed = this.prGetPatternSeed(pattern);
            var weight = pattern[\weight] ?? 0.7;
            var rhythmWeight = (weight * 10).floor / 10;
            var pseqWeight = weight - rhythmWeight * 10;
            var rhythmSeq = { |weight|
                Array.fill(16, { [ 0, amp ].wchoose([1 - weight, weight]) });
            };
            thisThread.randSeed = seed;
            if (pseqWeight > 0) {
                var seq1 = Pseq(rhythmSeq.(rhythmWeight), 1);
                var seq2 = Pseq(rhythmSeq.(rhythmWeight + 0.1), 1);
                [Pwrand([seq1, seq2], [1 - pseqWeight, pseqWeight])];
            } {
                rhythmSeq.(weight);
            };
        };

        var createPatternAmp = { |pattern|
            var amp = pattern[\amp] ?? pattern[\a] ?? 1;
            pattern.removeAt(\a);
            if (pattern[\beat].notNil)
            { amp = createPatternBeat.(amp, pattern) };
            if (amp.isArray)
            { amp = Pseq(amp, inf) };
            pattern[\amp] = amp;
            pattern;
        };

        var createPatternDur = { |pattern|
            var dur = pattern[\dur] ?? 1;
            if (dur == 0) { dur = 1 };
            if (dur.isArray) {
                var containsString = dur.any { |item| item.isString };
                dur = containsString.if { 1 } { Pseq(dur, inf) };
            };
            if (dur.isString) { dur = 1 };
            if (pattern[\euclid].notNil)
            { dur = Pbjorklund2(pattern[\euclid][0], pattern[\euclid][1]) / pattern[\euclid][2] };
            pattern[\dur] = dur;
            pattern;
        };

        var createPatternFade = { |fade, pbind|
            var defaultFadeTime = 16;
            var dir = if (fade.isString) { fade } { fade[0] };
            var fadeTime = if (fade.isString) { defaultFadeTime } { fade[1] };
            if (dir == "in")
            { PfadeIn(pbind, fadeTime) }
            { PfadeOut(pbind, fadeTime) }
        };

        var createLoops = {
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

                        var getRandBufs = {
                            thisThread.randSeed = this.prGetPatternSeed(pattern);
                            Pseq(~buf.(pattern[\buf][0], Array.rand(8, 0, filesCount - 1)), inf);
                        };

                        if (pattern[\i] == \lplay) {
                            var sampleLength = pattern[\buf][0].split($-);
                            if (sampleLength.isArray and: { sampleLength.size > 1 } and: { sampleLength[1].asInteger > 0 })
                            { pattern[\dur] = pattern[\dur] ?? sampleLength[1].asInteger };
                        };

                        buf = switch (pattern[\buf][1])
                        { \rand } { getRandBufs.value }
                        { \jump } { getJumpBufs.value }
                        { ~buf.(pattern[\buf][0], pattern[\buf][1]) };

                        if (pattern[\buf][1].isNil)
                        {
                            thisThread.randSeed = this.prGetPatternSeed(pattern);
                            buf = ~buf.(pattern[\buf][0], (~buf.(pattern[\buf][0]).size).rand);
                        };

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
        };

        var createPatternPan = { |pattern|
            pattern[\pan] = switch (pattern[\pan].asSymbol)
            { \rand } { Pwhite(-1.0, 1.0, inf) }
            { \rotate } { Pwalk((0..10).normalize(-1, 1), 1, Pseq([1, -1], inf), startPos: 5) }
            { pattern[\pan] };
            pattern;
        };

        name = name ?? defaultName;
        copyPatternsToLastPatterns.value;
        patterns = getSoloPatterns.value;
        patterns = createBufIns.value;
        patterns = createIds.value;
        patterns = createLoops.value;

        patterns.do { |pattern, i|
            var pbind;

            pattern = createPatternAmp.(pattern);
            pattern = createPatternDur.(pattern);
            pattern = createPatternPan.(pattern);
            pattern = this.prCreatePatternFx(pattern);

            if (this.prHasFX(pattern) == true)
            { pbind = this.prCreatePbindFx(pattern) }
            { pbind = Pbind(*pattern.asPairs) };

            if (pattern[\fade].notNil)
            { pbind = createPatternFade.(pattern[\fade], pbind) };

            if (trace == true)
            { pbind = pbind.trace };

            ptparList = ptparList ++ [pattern[\off] ?? 0, pbind];
        };

        Pdef(name.asSymbol, Ptpar(ptparList)).quant_(4).play;
    }

    *chorus { | name |
        if (chorus.isNil)
        { "Chorus is empty. Please run \"save\"".postln; }
        { this.new(chorus, name) }
    }

    gui {
        PdefAllGui.new;
    }

    *release { | fadeTime, name |
        var fadeValue = if (fadeTime.isNil) { "out" } { ["out", fadeTime] };
        var fadeOutPatterns;
        name = name ?? defaultName;
        fadeOutPatterns = lastPatterns[name].collect { |pattern|
            if (pattern[\fade] == "out")
            { pattern[\amp] = 0 };
            pattern.putAll([\fade: fadeValue]);
            pattern;
        };
        this.send(fadeOutPatterns, name);
    }

    *save { | name |
        chorus = lastPatterns[name ?? defaultName];
    }

    *send { | patterns, name, trace |
        name = name ?? defaultName;
        trace = trace ?? false;
        if (Pdef(name).isPlaying)
        { this.new(patterns, name, trace) }
        { "Pdef(\\".catArgs(name, ") is not playing").postln; }
    }

    *shuffle { | name |
        seeds.order.do { |id|
            var newSeed = (Date.getDate.rawSeconds % 1000).rand.asInteger;
            id.post; " ->".scatArgs(newSeed).postln;
            seeds[id] = newSeed;
        };
        name = name ?? defaultName;
        this.send(lastPatterns[name], name);
    }

    *stop {  | name |
        name = name ?? defaultName;
        Pdef(name.asSymbol).stop;
    }

    *synthDef { | synthDef |
        if (synthDef.notNil)
        { SynthDescLib.global[synthDef].postln };
    }

    *trace { | name |
        this.send(lastPatterns[name], name ?? defaultName, trace: true);
    }

    *prGetPatternSeed { |pattern|
        var id = pattern[\id].asSymbol;
        if (pattern[\seed].isNil) {
            if (seeds.isNil)
            { seeds = Dictionary[id -> 1000.rand] }
            { seeds.add(id -> if (seeds[id].isNil) { 1000.rand } { seeds[id] }) };
            ^seeds[id];
        } {
            ^pattern[\seed]
        };
    }
}
