Px {
    classvar chorus, <instruments;

    *new { |patterns, trace|
        var ptparList;

        var getSeed = { |pattern|
            pattern[\seed] ?? 1000.rand;
        };

        var createRhythm = { |amp, i, pattern|
            var seed = getSeed.(pattern);
            var createArray = {
                thisThread.randSeed = seed;
                Array.fill(16, { [ 0, amp ].wchoose([0.3, 0.7]) });
            };
            // Copy patterns to avoid error about diferent array size
            instruments = patterns;
            if (instruments.isArray.not)
            { createArray.(); }
            { if (instruments[i][\beat].isNil or: { instruments[i][\beat] != pattern[\seed] })
                { createArray.(); }
                { instruments[i][\amp]; }
            };
        };

        var createAmp = { |i, pattern|
            var amp = pattern[\amp] ?? pattern[\a] ?? 1;
            pattern.removeAt(\a);
            if (pattern[\beat].notNil)
            { amp = createRhythm.(amp, i, pattern) };
            if (amp.isArray)
            { amp = Pseq(amp, inf) };
            amp;
        };

        var createDur = { |pattern|
            var dur = pattern[\dur] ?? 1;
            if (dur.isArray) {
                var containsString = dur.any { |item| item.isString };
                dur = containsString.if { 1 } { Pseq(dur, inf) };
            };
            if (dur.isString) { dur = 1 };
            if (pattern[\euc].notNil)
            { dur = Pbjorklund2(pattern[\euc][0], pattern[\euc][1]) / pattern[\euc][2] };
            dur;
        };

        var createSampleLoop = { |pattern|
            var buf, loopSynthDef = "lplay";
            var loop = pattern[\loop] ?? pattern[\play];
            var filesCount = ~buf.(loop[0]).size;

            if (filesCount > 0 and: { loop.isArray }) {
                var getRandBufs = {
                    thisThread.randSeed = getSeed.(pattern);
                    buf = Pseq(~buf.(loop[0], Array.rand(8, 0, filesCount - 1)), inf);
                };

                var getTrimmedBufs = {
                    var randomFiles = ~buf.(loop[0], Array.rand(8, 0, filesCount - 1));
                    buf = Pseq(randomFiles, inf);
                };

                if (pattern[\loop].notNil) {
                    var sampleLength = pattern[\loop][0].split($-);
                    if (sampleLength.isArray and: { sampleLength.size > 1 } and: { sampleLength[1].asInteger > 0 })
                    { pattern[\dur] = pattern[\dur] ?? sampleLength[1].asInteger };
                    pattern.removeAt(\loop);
                } {
                    loopSynthDef = "playbuf";
                    pattern.removeAt(\play);
                };

                buf = switch (loop[1])
                { \rand } { getRandBufs.() }
                { \trim } { getTrimmedBufs.() }
                { ~buf.(loop[0], loop[1]); };

                if ([Buffer, Pseq].includes(buf.class))
                { pattern.putAll([i: loopSynthDef, buf: buf]) }
                { pattern[\amp] = 0 };
            }
            { pattern[\amp] = 0 };
        };

        var createFade = { |fade, pbind|
            var defaultFadeTime = 16;
            var dir = if (fade.isString) { fade } { fade[0] };
            var fadeTime = if (fade.isString) { defaultFadeTime } { fade[1] };
            if (dir == "in")
            { PfadeIn(pbind, fadeTime) }
            { PfadeOut(pbind, fadeTime) }
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

        var getFx = { |pattern|
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

        instruments = patterns;
        patterns = getSoloPatterns.();

        patterns.do { |pattern, i|
            var pbind;

            pattern[\amp] = createAmp.(i, pattern);
            if (pattern[\loop].notNil or: pattern[\play].notNil)
            { createSampleLoop.(pattern) };
            pattern[\dur] = createDur.(pattern);
            pattern[\pan] = createPan.(pattern);
            pattern = getFx.(pattern);

            if (pattern[\fxOrder].notNil) {
                pbind = PbindFx(pattern.asPairs, *pattern[\fx]);
            } {
                pbind = Pbind(*pattern.asPairs);
            };

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

    *trace {
        this.new(instruments, true);
    }

    *help { |synthDef|
        if (synthDef.isNil)
        { this.class.asString.help }
        { SynthDescLib.global[synthDef].postln };
    }
}
