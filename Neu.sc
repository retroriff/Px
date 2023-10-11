Neu {
    classvar <instruments;

    const defaultFadeTime = 10;

    *new { |patterns|
        var pbind, result;
        var ptparList = patterns.collect { |pattern, i|
            var createFade = { |amp, fade|
                var dir, durs, start, end;
                dir = if (fade.isString) { fade } { fade[0] };
                durs = if (fade.isString) { defaultFadeTime } { fade[1] };
                start = if (dir == "in") { 0 } { amp };
                end = if (dir == "in") { amp } { 0 };
                Pseg(Pseq([start, Pn(end)]), durs, curves: 0);
            };

            var createAmp = { |amp|
                pattern.removeAt(\a);
                if (pattern[\fade].notNil)
                { amp = createFade.(amp, pattern[\fade]) };
                amp;
            };

            var createDur = { |dur|
                dur = if (dur.isArray) { Pseq(dur, inf) } { dur ?? 1 };
                if (pattern[\pbj].notNil)
                { dur = Pbjorklund2(pattern[\pbj][0], pattern[\pbj][1]) / pattern[\pbj][2] };
                dur;
            };

            var isFx = pattern[\fx].notNil and: pattern[\i].isNil and: pattern[\ins].isNil;

            if (isFx)
            {
                var decayPairs = [\decayTime, pattern[\decayTime] ?? 7, \cleanupDelay, Pkey(\decayTime)];
                result[result.size - 1][\fx] = result[result.size - 1][\fx] ++ [pattern.asPairs ++ decayPairs];
            }
            {
                var offset = pattern[\off] ?? 0;
                pattern.removeAt(\off);
                pattern[\amp] = createAmp.(pattern[\amp] ?? pattern[\a] ?? 1);
                pattern[\dur] = createDur.(pattern[\dur]);
                result = result.add((off: offset, \ins: pattern.asPairs));
            };
        };

        result.size.do { |i|
            if (result[i][\fx].isArray)
            {
                result[i][\ins] = result[i][\ins] ++ [\fxOrder, (1..result[i][\fx].size)];
                pbind = pbind ++ [result[i][\off], PbindFx(result[i][\ins], *result[i][\fx]).trace];
            }
            {
                pbind = pbind ++ [result[i][\off], Pbind(*result[i][\ins]).trace];
            }
        };

        instruments = patterns;
        Pdef(\neu, Ptpar(pbind)).quant_(4).play;
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
