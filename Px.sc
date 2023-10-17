Px {
    classvar chorus, <instruments;

    *new { |patterns, trace|
        var insIndex = -1, pbind, result, soloList;

        var createRhythm = { |amp, i, pattern|
            var seed = pattern[\seed] ?? 1000.rand;
            var createArray = {
                thisThread.randSeed = seed;
                Array.fill(16, { [ 0, amp ].wchoose([0.3, 0.7]) });
            };
            if (pattern[\showSeed] == true) { ("Seed is" + seed).postln };
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
            var filesCount = ~s.(loop[0]).size;

            if (filesCount > 0 and: { loop.isArray }) {
                if (pattern[\loop].notNil) {
                    var sampleLength = pattern[\loop][0].split($-);
                    if (sampleLength.isArray and: { sampleLength.size > 1 } and: { sampleLength[1].asInteger > 0 })
                    { pattern[\dur] = pattern[\dur] ?? sampleLength[1].asInteger };
                    pattern.removeAt(\loop);
                } {
                    loopSynthDef = "playbuf";
                    pattern.removeAt(\play);
                };

                if (loop[1] == \rand)
                {
                    var randomFiles = ~s.(loop[0], Array.rand(8, 0, filesCount - 1));
                    buf = Pseq(randomFiles, inf);
                }
                { buf = ~s.(loop[0], loop[1]); };

                if ([Buffer, Pseq].includes(buf.class))
                { pattern.putAll([i: loopSynthDef, buf: buf]) }
                { pattern[\amp] = 0 };
            };
        };

        var insList = patterns.select { |pattern, insertIndex|
            if (pattern[\fx].isNil) {
                pattern[\amp] = createAmp.(insertIndex, pattern);

                if (pattern[\loop].notNil or: (pattern[\play].notNil))
                { createSampleLoop.(pattern) };

                pattern[\dur] = createDur.(pattern);
            };
            if (pattern['fxMethod'].notNil and: { pattern['fxMethod'].size > 0 }) {
                pattern['fxMethod'].size.do { |i|
                    var mix = pattern['fxMethod'][i][1] ?? 1;
                    patterns = patterns.insert(insertIndex + 1, (fx: pattern['fxMethod'][i][0], mix: mix));
                };
            };
            pattern['fx'].isNil;
        };

        var fxList = patterns.select { |pattern, i|
            if (pattern[\fx].isNil)
            { insIndex = insIndex + 1 }
            { pattern.putAll([\insIndex, insIndex]) };
            pattern['fx'].notNil;
        };

        var createFade = { |fade, pbind|
            var defaultFadeTime = 16, dir, fadeTime;
            dir = if (fade.isString) { fade } { fade[0] };
            fadeTime = if (fade.isString) { defaultFadeTime } { fade[1] };
            if (dir == "in") { PfadeIn(pbind, fadeTime) } { PfadeOut(pbind, fadeTime) };
        };

        var addIns = { |pattern|
            var offset = pattern[\off] ?? 0;
            result = result.add((off: offset, \ins: pattern));
        };

        var addFx = { |pattern|
            var decayPairs = [\decayTime, pattern[\decayTime] ?? 7, \cleanupDelay, Pkey(\decayTime)];
            var insIndex = pattern[\insIndex];
            if (SynthDescLib.global[pattern[\fx]].notNil)
            { result[insIndex][\fx] = result[insIndex][\fx] ++ [pattern.asPairs ++ decayPairs]; }
        };

        insList.do { |pattern, i| addIns.(pattern) };
        fxList.do { |pattern, i| addFx.(pattern) };
        soloList = result.collect { |item, i| if (item['ins']['solo'].notNil) { true } { false } };

        result.size.do { |i|
            var fade = result[i][\ins][\fade], pattern;
            if ( soloList.includes(true) and: { soloList[i].not } ) { result[i][\ins][\amp] = 0 };
            result[i][\ins] = result[i][\ins].asPairs;

            if (result[i][\fx].isArray)
            {
                result[i][\ins] = result[i][\ins] ++ [\fxOrder, (1..result[i][\fx].size)];
                pattern = PbindFx(result[i][\ins], *result[i][\fx]);
            }
            { pattern = Pbind(*result[i][\ins]) };

            if (fade.notNil) { pattern = createFade.(fade, pattern) };

            if (trace == true) { pattern = pattern.trace };

            pbind = pbind ++ [result[i][\off], pattern];
        };

        instruments = patterns;
        Pdef(\neu, Ptpar(pbind)).quant_(4).play;
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
