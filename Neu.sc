Neu {
    classvar <instruments;

    const defaultFadeTime = 10;

    *new { |patterns|
        var ptparList = patterns.collect { |pattern, i|
            var pbind = Pbind();
            var createFade = { |amp, fade|
                var dir, durs, start, end;
                dir = if (fade.isString, { fade }, { fade[0] });
                durs = if (fade.class == Array
                    and: fade[1].isNumber
                    and: fade[1] != 0,
                    { fade[1] },
                    { defaultFadeTime }
                );
                start = if (dir == "in", { 0 }, { amp });
                end = if (dir == "in", { amp }, { 0 });
                Pseg(Pseq([start, Pn(end)]), durs, curves: 0);
            };

            pattern[\amp] = pattern[\amp] ?? pattern[\a] ?? 1;
            pattern[\dur] = pattern[\dur] ?? 1;
            pattern[\off] = pattern[\off] ?? 0;
            pattern.removeAt(\a);
            if (pattern[\fade] != nil) {
                pattern[\amp] = createFade.value(pattern[\amp], pattern[\fade]);
            };
            pattern.keys.do { |key|
                if (key != \off) {
                    pbind = Pchain(pbind, Pbind(key, pattern[key]));
                };
            };
            [pattern[\off], pbind];
        }.flatten;
        instruments = patterns;
        Pdef(\neu, Ptpar(ptparList)).play;
    }

    *release { |durs|
        var hasFade = false;
        var fadeOutInstruments = instruments.collect { |pattern|
            durs = if (durs.class == Integer and: durs != 0, { durs }, { defaultFadeTime });
            if (pattern[\fade] == nil,
                {
                    pattern.keys.do { |key|
                        var amp = pattern[\amp] ?? pattern[\a] ?? 1;
                        if ([\a, \amp].includes(key)) {
                            pattern[key] = Pseg(Pseq([amp, Pn(0)]), durs, curves: 0);
                        };
                    }
                },
                {
                    hasFade = true;
                };
            );
            pattern;
        };

        if (hasFade, {
            "Please remove fade keys".postln;
        }, {
            this.new(fadeOutInstruments);
        });
    }

    help {
        this.class.asString.help
    }
}
