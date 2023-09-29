Neu {
    classvar instruments;

    *new { |patterns|
        var ptparList = patterns.collect { |pattern|
            var pbind = Pbind(
                \instrument, pattern[\i],
                \amp, pattern[\a] ?? pattern[\amp] ?? 1,
                \dur, pattern[\dur] ?? 1,
            );
            var offset = pattern[\off] ?? 0;
            patterns.remove(\a, \amp, \dur, \off);
            pattern.keys.do { |key|
                pbind = Pchain(pbind, Pbind(key, pattern[key]));
            };
            [
                offset,
                pbind,
            ]
        }.flatten;
        instruments = patterns;
        ^Pdef(\neu, Ptpar(ptparList)).play;
    }

    *release { |durs|
        var keysToReplace = ["amp", "a"];

        var fadeOutInstruments = instruments.collect { |pattern|
            pattern.keys.do { |key|
                if ([\a, \amp].includes(key)) {
                    key.postln;
                    pattern[key] = Pseg(Pseq([1, Pn(0)]), durs, curves: 0);
                }
            };
            pattern;
        };

        ^this.new(fadeOutInstruments)
    }
}

