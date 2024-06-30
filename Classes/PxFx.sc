+Px {
    *blp { |mix = 0.4|
        Nfx(lastName).blp(mix);
    }

    *delay { |mix, delaytime = 8, decaytime = 2|
        Nfx(lastName).delay(mix, delaytime, decaytime);
    }

    *gverb { |mix = 0.4, roomsize = 200, revtime = 5|
        Nfx(lastName).gverb(mix, roomsize, revtime);
    }

    *hpf { |mix = 1, freq = 1200|
        Nfx(lastName).hpf(mix, freq);
    }

    *lpf { |mix, args|
        this.prFx(\lpf, mix, args);
    }

    *reverb { |mix = 0.3, room = 0.7, damp = 0.7|
        Nfx(lastName).reverb(mix, room, damp);
    }

    *vst { |mix = 1, plugin|
        Nfx(lastName).vst(mix, plugin);
    }

    *wah { |mix, args|
        this.prFx(\wah, mix, args);
    }

    *prCreatePatternFx { |pattern|
        if (pattern[\fx].notNil and: { pattern[\fx].size > 0 }) {
            pattern[\fx].do { |fx, i|
                if (SynthDescLib.global[fx[1]].notNil) {
                    if (fx[1] == \reverb)
                    { fx = fx ++ [\decayTime, pattern[\decayTime] ?? 7, \cleanupDelay, 1] };
                    pattern[\fx][i] = fx;
                    pattern = pattern ++ [\fxOrder, (1..pattern[\fx].size)];
                }
            }
        };
        ^pattern;
    }

    *prCreatePbindFx { |pattern|
        ^PbindFx(pattern.asPairs, *pattern[\fx]);
    }

    *prFx { |fx, mix, args|
        lastPatterns[lastName].do { |pattern|
            pattern.prFx(fx, mix, args);
        };

        this.prSend(lastPatterns[lastName], lastName);
    }

    *prHasFX { |pattern|
        ^pattern[\fx].notNil;
    }
}

+Event {
    delay { |mix, args|
        this.prFx(\delay, mix, args);
    }

    hpf { |mix, args|
        this.prFx(\hpf, mix, args);
    }

    lpf { |mix, args|
        this.prFx(\lpf, mix, args);
    }

    reverb { |mix, args|
        this.prFx(\reverb, mix, args);
    }

    wah { |mix, args|
        this.prFx(\wah, mix, args);
    }

    prFx { |fx, mix, args|
        if ([\hpf, \lpf].includes(fx) and: { mix == \rand }) {
            mix = 1;
            args = [\freq, this.prCreatePatternKey(\rand)] ++ args;
        };
        ^this.[\fx] = this.[\fx] ++ [[\fx, fx, \mix, this.prCreatePatternKey(mix)] ++ args];
    }
}