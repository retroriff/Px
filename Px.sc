Px {
    classvar chorus, <lastPatterns, seeds;

    *new { | patterns, name, trace |
        var ptparList;

        var getSeed = { |pattern|
            var id = pattern[\id].asSymbol;
            if (pattern[\seed].isNil) {
                if (seeds.isNil)
                { seeds = Dictionary[id -> 1000.rand] }
                { seeds.add(id -> if (seeds[id].isNil) { 1000.rand } { seeds[id] }) };
                seeds[id];
            } { pattern[\seed] };
        };

        var createRhythm = { |amp, pattern|
            var seed = getSeed.(pattern);
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

        var createAmp = { |pattern|
            var amp = pattern[\amp] ?? pattern[\a] ?? 1;
            pattern.removeAt(\a);
            if (pattern[\beat].notNil)
            { amp = createRhythm.(amp, pattern) };
            if (amp.isArray)
            { amp = Pseq(amp, inf) };
            amp;
        };

        var createDur = { |pattern|
            var dur = pattern[\dur] ?? 1;
            if (dur == 0) { dur = 1 };
            if (dur.isArray) {
                var containsString = dur.any { |item| item.isString };
                dur = containsString.if { 1 } { Pseq(dur, inf) };
            };
            if (dur.isString) { dur = 1 };
            if (pattern[\euclid].notNil)
            { dur = Pbjorklund2(pattern[\euclid][0], pattern[\euclid][1]) / pattern[\euclid][2] };
            dur;
        };

        var createFade = { |fade, pbind|
            var defaultFadeTime = 16;
            var dir = if (fade.isString) { fade } { fade[0] };
            var fadeTime = if (fade.isString) { defaultFadeTime } { fade[1] };
            if (dir == "in")
            { PfadeIn(pbind, fadeTime) }
            { PfadeOut(pbind, fadeTime) }
        };

        var createFx = { |pattern|
            if (pattern[\fx].notNil and: { pattern[\fx].size > 0 }) {
                pattern[\fx].do { |fx, i|
                    if (SynthDescLib.global[fx[1]].notNil) {
                        if (fx == \reverb)
                        { fx = fx ++ [\decayTime, pattern[\decayTime] ?? 7, \cleanupDelay, 1] };
                        pattern[\fx][i] = fx;
                        pattern = pattern ++ [\fxOrder, (1..pattern[\fx].size)];
                    }
                }
            };
            pattern;
        };

        var createIds = {
            var indexDict = Dictionary.new;
            patterns = patterns.collect { |pattern|
                var patternStr = pattern.i.asString;
                indexDict[patternStr] = indexDict[patternStr].isNil.if
                { 0 }
                { indexDict[patternStr] + 1 };
                pattern = pattern ++ (id: pattern[\id] ?? (patternStr ++ "_" ++ indexDict[patternStr]));
            };
            patterns;
        };

        var createIns = {
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
        };

        var createLoops = {
            patterns = patterns.collect { |pattern|
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
                                thisThread.randSeed = getSeed.(pattern);
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
                            thisThread.randSeed = getSeed.(pattern);
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
                        { ~buf.(pattern[\buf][0], pattern[\buf][1]); };

                        if (pattern[\trim].notNil) {
                            if (pattern[\trim] == \random)
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

                pattern;
            };
        };

        var createPan = { |pattern|
            case
            { pattern[\pan].asSymbol == \rand }
            { Pwhite(-1.0, 1.0, inf) }
            { pattern[\pan].asSymbol == \rotate }
            { Pwalk((0..10).normalize(-1, 1), 1, Pseq([1, -1], inf), startPos: 5) }
            { pattern[\pan] };
        };

        var getSoloPatterns = {
            var soloList = patterns.select { |pattern| pattern['solo'].notNil };
            if (soloList.isEmpty.not)
            { soloList }
            { patterns }
        };

        name = name ?? \px;
        lastPatterns = if (lastPatterns.isNil)
        { Dictionary[name -> patterns] }
        { lastPatterns[name] = patterns };

        patterns = getSoloPatterns.value;
        patterns = createIns.value;
        patterns = createIds.value;
        patterns = createLoops.value;

        patterns.do { |pattern, i|
            var hasFx, pbind;

            pattern[\amp] = createAmp.(pattern);
            pattern[\dur] = createDur.(pattern);
            pattern[\pan] = createPan.(pattern);

            pattern = createFx.(pattern);
            if (pattern[\fxOrder].notNil) { hasFx = true };

            if (hasFx == true)
            { pbind = PbindFx(pattern.asPairs, *pattern[\fx]) }
            { pbind = Pbind(*pattern.asPairs) };

            if (pattern[\fade].notNil)
            { pbind = createFade.(pattern[\fade], pbind) };

            if (trace == true)
            { pbind = pbind.trace };

            ptparList = ptparList ++ [pattern[\off] ?? 0, pbind];
        };
        Pdef(name.asSymbol, Ptpar(ptparList)).quant_(4).play;
    }

    *chorus { | name |
        if (chorus.isNil)
        { "Chorus is empty.Run \"save\" method first".postln; }
        { this.new(chorus, name) }
    }

    *gui {
        PdefAllGui.new;
    }

    *help { | synthDef |
        if (synthDef.isNil)
        { this.class.asString.help }
        { SynthDescLib.global[synthDef].postln };
    }

    *save { | name = \px |
        chorus = lastPatterns[name];
    }

    *send { | patterns, name, trace |
        name = name ?? \px;
        trace = trace ?? false;
        if (Pdef(name).isPlaying)
        { this.new(patterns, name, trace) }
        { "Pdef(\\".catArgs(name, ") is not playing").postln; }
    }

    *release { | fadeTime, name = \px |
        var fadeValue = if (fadeTime.isNil) { "out" } { ["out", fadeTime] };
        var fadeOutPatterns = lastPatterns[name].collect { |pattern|
            if (pattern[\fade] == "out")
            { pattern[\amp] = 0 };
            pattern.putAll([\fade: fadeValue]);
            pattern;
        };
        this.send(fadeOutPatterns, name);
    }

    *shuffle { | name = \px |
        seeds.order.do { |id|
            var newSeed = (Date.getDate.rawSeconds % 1000).rand.asInteger;
            id.post; " ->".scatArgs(newSeed).postln;
            seeds[id] = newSeed;
        };
        this.send(lastPatterns[name], name);
    }

    *stop {  | name = \px |
        Pdef(name.asSymbol).stop;
    }

    *trace { | name = \px |
        this.send(lastPatterns[name], name, trace: true);
    }
}
