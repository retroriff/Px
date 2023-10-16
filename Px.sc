Px {
    classvar chorus, <instruments, fadeIn, fadeOut;

    const defaultFadeTime = 10;

    *new { |patterns|
        var insIndex = -1, pbind, result, soloList;

        var createAmp = { |i, pattern|
            var amp = pattern[\amp] ?? pattern[\a] ?? 1;
            ("amp" + i).postln;
            pattern.removeAt(\a);
            if (pattern[\beat].notNil) { amp = createRhythm.(amp, i, pattern) };
            if (amp.isArray) { amp = Pseq(amp, inf) };
            if (pattern[\fade].notNil)
            { amp = createFade.(amp, pattern[\fade]) };
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

            if (pattern[\loop].notNil) {
                var sampleLength = pattern[\loop][0].split($-);
                buf = ~s.(pattern[\loop][0], pattern[\loop][1]);
                if (sampleLength.isArray and: { sampleLength.size > 1 } and: { sampleLength[1].asInteger > 0 })
                { pattern[\dur] = pattern[\dur] ?? sampleLength.asInteger };
                pattern.removeAt(\loop);
            } {
                buf = ~s.(pattern[\play][0], pattern[\play][1]);
                loopSynthDef = "playbuf";
                pattern.removeAt(\play);
            };

            if (buf.class == Buffer)
            { pattern.putAll([i: loopSynthDef, buf: buf]) }
            { pattern[\amp] = 0 };
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
                    patterns = patterns.insert(insertIndex + 1, (fx: pattern['fxMethod'][i]));
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

        var createFade = { |amp, fade|
            var dir, durs, start, end;
            dir = if (fade.isString) { fade } { fade[0] };
            durs = if (fade.isString) { defaultFadeTime } { fade[1] };
            start = if (dir == "in") { 0 } { amp };
            end = if (dir == "in") { amp } { 0 };
            Pseg(Pseq([start, Pn(end)]), durs, curves: 0);
        };

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

        var addIns = { |pattern|
            var offset = pattern[\off] ?? 0;
            pattern.removeAt(\off);
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
            var pattern;
            if ( soloList.includes(true) and: { soloList[i].not } ) { result[i][\ins][\amp] = 0 };
            result[i][\ins] = result[i][\ins].asPairs;
            if (result[i][\fx].isArray)
            {
                result[i][\ins] = result[i][\ins] ++ [\fxOrder, (1..result[i][\fx].size)];
                pattern = PbindFx(result[i][\ins], *result[i][\fx]);
            }
            {
                pattern = Pbind(*result[i][\ins])
            };
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

    *release { |durs|
        var hasFade = false;
        var fadeOutInstruments = instruments.collect { |pattern|
            durs = if (durs > 0) { durs } { defaultFadeTime };
            if (pattern[\fade].isNil)
            { pattern.keys.do { |key|
                var amp = pattern[\amp] ?? pattern[\a] ?? 1;
                if ([\a, \amp].includes(key))
                { pattern[key] = Pseg(Pseq([amp, Pn(0)]), durs, curves: 0) }
            }}
            { hasFade = true };
            pattern;
        };

        if (hasFade)
        { "Please remove fade keys".postln }
        { this.new(fadeOutInstruments) };
    }

    *help { |synthDef|
        if (synthDef.isNil)
        { this.class.asString.help }
        { SynthDescLib.global[synthDef].postln };
    }
}
