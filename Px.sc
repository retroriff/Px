Px {
    classvar chorus, <instruments, seedList;

    *new { |patterns, trace|
        var ptparList;

        var getSeed = { |pattern|
            var id = pattern[\id].asSymbol;
            if (seedList.isNil)
            { seedList = Dictionary[id -> 1000.rand] }
            { seedList.add(id -> if (seedList[id].isNil) { 1000.rand } { seedList[id] }) };
            seedList[id];
        };

        var createRhythm = { |amp, pattern|
            var seed = pattern[\seed];
            var weight = pattern[\weight] ?? 0.7;
            pattern.postln;
            if (pattern[\seed].isNil) { seed = getSeed.(pattern) };
            thisThread.randSeed = seed;
            Array.fill(16, { [ 0, amp ].wchoose([1 - weight, weight]) });
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
                        fx = fx ++ [\decayTime, pattern[\decayTime] ?? 7, \cleanupDelay, Pkey(\decayTime)];
                        pattern[\fx][i] = fx;
                        pattern = pattern ++ [\fxOrder, (1..pattern[\fx].size)];
                    }
                }
            };
            pattern;
        };

        var createIds = {
            var indexDict = Dictionary.new;
            patterns = patterns.collect { |item|
                var itemStr = item.i.asString;
                indexDict[itemStr] = indexDict[itemStr].isNil.if
                { 0 }
                { indexDict[itemStr] + 1 };
                item = item ++ (id: itemStr ++ "_" ++ indexDict[itemStr]);
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

                        var getRandBufs = {
                            thisThread.randSeed = getSeed.(pattern);
                            buf = Pseq(~buf.(pattern[\buf][0], Array.rand(8, 0, filesCount - 1)), inf);
                        };

                        var getTrimmedBufs = {
                            var randomFiles = ~buf.(pattern[\buf][0], Array.rand(8, 0, filesCount - 1));
                            buf = Pseq(randomFiles, inf);
                        };

                        if (pattern[\i] == \lplay) {
                            var sampleLength = pattern[\buf][0].split($-);
                            if (sampleLength.isArray and: { sampleLength.size > 1 } and: { sampleLength[1].asInteger > 0 })
                            { pattern[\dur] = pattern[\dur] ?? sampleLength[1].asInteger };
                        };

                        buf = switch (pattern[\buf][1])
                        { \rand } { getRandBufs.() }
                        { \trim } { getTrimmedBufs.() }
                        { ~buf.(pattern[\buf][0], pattern[\buf][1]); };

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

        instruments = patterns;
        patterns = getSoloPatterns.();
        patterns = createIns.();
        patterns = createIds.();
        patterns = createLoops.();

        patterns.do { |pattern, i|
            var pbind;

            pattern[\amp] = createAmp.(pattern);
            pattern[\dur] = createDur.(pattern);
            pattern[\pan] = createPan.(pattern);

            pattern = createFx.(pattern);

            if (pattern[\fxOrder].notNil)
            { pbind = PbindFx(pattern.asPairs, *pattern[\fx]) }
            { pbind = Pbind(*pattern.asPairs) };

            if (pattern[\fade].notNil)
            { pbind = createFade.(pattern[\fade], pbind) };

            if (trace == true)
            { pbind = pbind.trace };

            ptparList = ptparList ++ [pattern[\off] ?? 0, pbind];
        };

        Pdef(\px, Ptpar(ptparList)).quant_(4).play;
    }

    *chorus {
        if (chorus.isNil)
        { "Chorus is empty.Run \"save\" method first".postln; }
        { this.new(chorus) }
    }

    *save {
        chorus = instruments;
    }

    *release { |fadeTime, fade|
        var fadeValue = if (fadeTime.isNil) { "out" } { ["out", fadeTime] };
        var fadeOutPatterns = instruments.collect { |pattern|
            if (pattern[\fade] == "out")
            { pattern[\amp] = 0 };
            pattern.putAll([\fade: fadeValue]);
            pattern;
        };
        this.new(fadeOutPatterns);
    }

    *shuffle {
        seedList.order.do { |id|
            var newSeed = (Date.getDate.rawSeconds % 1000).rand.asInteger;
            id.post; " ->".scatArgs(newSeed).postln;
            seedList[id] = newSeed;
        };
        this.new(instruments);
    }

    *trace {
        this.new(instruments, true);
    }

    *help { |synthDef|
        if (synthDef.isNil)
        { this.class.asString.help }
        { SynthDescLib.global[synthDef].postln };
    }
}
