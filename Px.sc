Px {
    classvar <instruments, chorus;

    const defaultFadeTime = 10;

    *new { |patterns|
        var pbind, result;

        var ptparList = patterns.collect { |pattern, i|
            var createAmp = { |amp|
                pattern.removeAt(\a);
                if (pattern[\beat].notNil) { amp = createRhythm.(amp) };
                if (amp.isArray) { amp = Pseq(amp, inf) };
                if (pattern[\fade].notNil)
                { amp = createFade.(amp, pattern[\fade]) };
                amp;
            };

            var createDur = { |dur = 1|
                if (dur.isArray) {
                    var containsString = dur.any { |item| item.isString };
                    dur = if (containsString == true) { 1 } { Pseq(dur, inf) };
                };
                if (dur.isString) { dur = 1 };
                if (pattern[\pbj].notNil)
                { dur = Pbjorklund2(pattern[\pbj][0], pattern[\pbj][1]) / pattern[\pbj][2] };
                dur;
            };

            var createFade = { |amp, fade|
                var dir, durs, start, end;
                dir = if (fade.isString) { fade } { fade[0] };
                durs = if (fade.isString) { defaultFadeTime } { fade[1] };
                start = if (dir == "in") { 0 } { amp };
                end = if (dir == "in") { amp } { 0 };
                Pseg(Pseq([start, Pn(end)]), durs, curves: 0);
            };

            var createRhythm = { |amp|
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

            var createSampleLoop = {
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

            var addIns = {
                var offset = pattern[\off] ?? 0;
                pattern[\amp] = createAmp.(pattern[\amp] ?? pattern[\a] ?? 1);

                if (pattern[\loop].notNil or: (pattern[\play].notNil))
                { createSampleLoop.() };

                pattern.removeAt(\off);
                pattern[\dur] = createDur.(pattern[\dur]);
                result = result.add((off: offset, \ins: pattern));
            };

            var addFx = {
                var decayPairs = [\decayTime, pattern[\decayTime] ?? 7, \cleanupDelay, Pkey(\decayTime)];
                if (SynthDescLib.global[pattern[\fx]].notNil)
                { result[result.size - 1][\fx] = result[result.size - 1][\fx] ++ [pattern.asPairs ++ decayPairs]; }
            };

            var getFxmethods = {
                var insertIndex = i;
                if (pattern['fxMethod'].notNil and: { pattern['fxMethod'].size > 0 })
                {
                    pattern['fxMethod'].size.do { |i|
                        patterns.insert(insertIndex + 1, (fx: pattern['fxMethod'][i]));
                    };
                    pattern.removeAt(\fxMethod);
                };
            };

            if (pattern[\fx].notNil) { addFx.() } { getFxmethods.(); addIns.() };
        };

        var soloList = result.collect { |item, i| if (item['ins']['solo'].notNil) { true } { false } };

        result.size.do { |i|
            if ( soloList.includes(true) and: { soloList[i].not } ) { result[i][\ins][\amp] = 0 };
            result[i][\ins] = result[i][\ins].asPairs;
            if (result[i][\fx].isArray)
            {
                result[i][\ins] = result[i][\ins] ++ [\fxOrder, (1..result[i][\fx].size)];
                pbind = pbind ++ [result[i][\off], PbindFx(result[i][\ins], *result[i][\fx])];
            }
            {
                pbind = pbind ++ [result[i][\off], Pbind(*result[i][\ins])];
            }
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
