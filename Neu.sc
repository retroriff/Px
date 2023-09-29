Neu {
    classvar instruments;

    *new { |patterns|
        var ptparList = patterns.collect { |pattern, i|
            var offset = pattern[\off] ?? 0;
            var pbind = Pbind();
            var fade = { |amp, dir, durs|
                var start = if (dir == "in", { 0 }, { amp });
                var end = if (dir == "in", { amp }, { 0 });
                Pseg(Pseq([start, Pn(end)]), durs, curves: 0);
            };
            pattern[\amp] = pattern[\amp] ?? pattern[\a] ?? 1;
            pattern[\dur] = pattern[\dur] ?? 1;
            pattern.removeAt(\a);
            if (pattern[\fade] != nil) {
                pattern[\amp] = fade.value(pattern[\amp], pattern[\fade], 10);
            };
            pattern.keys.do { |key|
                if (key != \off) {
                    pbind = Pchain(pbind, Pbind(key, pattern[key]));
                };
            };
            [offset, pbind];
        }.flatten;
        instruments = patterns;
        ^Pdef(\neu, Ptpar(ptparList)).play;
    }

    *release { |durs|
        var hasFade = false;
        var fadeOutInstruments = instruments.collect { |pattern|
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
            ^this.new(fadeOutInstruments);
        });
    }
}
