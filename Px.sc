Px {
    classvar chorus, <instruments;

    *new { |patterns, trace|
        var insIndex = -1, pbind;

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
            if (pattern[\beat].notNil) { amp = createRhythm.(amp, i, pattern) };
            if (amp.isArray) { amp = Pseq(amp, inf) };
            amp;
        };

        var createDur = { |pattern|
            var dur = pattern[\dur] ?? 1;
            if (dur.isArray) {
                var containsString = dur.any { |item| item.isString };
                dur = if (containsString == true) { 1 } { Pseq(dur, inf) };
            };
            if (dur.isString) { dur = 1 };
            if (pattern[\pbj].notNil)
            { dur = Pbjorklund2(pattern[\pbj][0], pattern[\pbj][1]) / pattern[\pbj][2] };
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

        var getSoloPatterns = {
            var soloList = patterns.select { |pattern| pattern['solo'].notNil };
            if (soloList.isEmpty.not)
            { soloList }
            { patterns }
        };

        instruments = patterns;
        patterns = getSoloPatterns.();

        patterns.do { |pattern, i|
            var fade = pattern[\fade];
            var fx = pattern[\fxMethod];
            var offset = pattern[\off] ?? 0;
            var mute;
            var pbindResult;

            pattern[\amp] = createAmp.(i, pattern);

            if (pattern[\loop].notNil or: pattern[\play].notNil)
            { createSampleLoop.(pattern) };

            pattern[\dur] = createDur.(pattern);

            if (pattern[\fxMethod].notNil and: { pattern[\fxMethod].size > 0 }) {
                var decayPairs = [\decayTime, pattern[\decayTime] ?? 7, \cleanupDelay, Pkey(\decayTime)];
                if (SynthDescLib.global[pattern[\fx]].notNil) {
                    pattern[\fx] = pattern[\fx] ++ decayPairs;
                }
            };

            pattern = pattern.asPairs;

            if (fx.isArray and: { fx.size > 0 }) {
                var patternFx;
                fx.do { |fxItem|
                    patternFx = patternFx.add([\fx: fxItem[0], \mix: fxItem[1] ?? 1]);
                };
                pattern = pattern ++ [\fxOrder, (1..fx.size)];
                pbindResult = PbindFx(pattern, *patternFx);
            } {
                pbindResult = Pbind(*pattern);
            };

            if (fade.notNil)
            { pbindResult = createFade.(fade, pbindResult) };

            pbind = pbind ++ [offset, pbindResult];

            if (trace == true)
            { pbind = pbind.trace };
        };

        Pdef(\px, Ptpar(pbind)).quant_(4).play;
    }

    *chorus {
        if (chorus.isNil)
        { "Chorus is empty.Run \"save\" method first".postln; }
        { this.new(chorus) }
    }

    *save {
        chorus = instruments;
    }

    *release { |fadeTime|
        var fade;
        var fadeOutInstruments = instruments.collect { |pattern|
            if (pattern[\fx].isNil)
            {
                if (pattern[\fade].notNil and: { pattern[\fade] == "out" })
                { pattern[\amp] = 0 };
                pattern.putAll([\fade: ["out", fadeTime]]);
            };
            pattern;
        };
        this.new(fadeOutInstruments);
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
