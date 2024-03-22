+Px {
    *blp { |mix = 1|
        Nfx(currentName).blp(mix);
    }

    *delay { |mix, args|
        this.prFx(\delay, mix, args);
    }

    *hpf { |mix, args|
        this.prFx(\hpf, mix, args);
    }

    *lpf { |mix, args|
        this.prFx(\lpf, mix, args);
    }

    *reverb { |mix = 1, room = 0.7, damp = 0.7|
        Nfx(currentName).reverb(mix, room, damp);
    }

    *vst { |mix = 1, plugin|
        Nfx(currentName).vst(mix, plugin);
    }

    *wah { |mix, args|
        this.prFx(\wah, mix, args);
    }

    *prFx { |fx, mix, args|
        lastPatterns[currentName].do { |pattern|
            pattern.prFx(fx, mix, args);
        };

        this.send(lastPatterns[currentName], currentName);
    }

    *prCreatePatternFx { |pattern|
        if (pattern[\fx].notNil and: { pattern[\fx].size > 0 }) {
            pattern[\fx].do { |fx, i|
                if (SynthDescLib.global[fx[i]].notNil) {
                    if (fx[i] == \reverb)
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

    *prHasFX { |pattern|
        ^pattern[\fx].notNil;
    }
}

+Event {
    prFx { |fx, mix, args|
        if ([\hpf, \lpf].includes(fx) and: { mix == \rand }) {
            mix = 1;
            args = [\freq, this.prCreatePatternKey(\rand)] ++ args;
        };
        ^this.[\fx] = this.[\fx] ++ [[\fx, fx, \mix, this.prCreatePatternKey(mix)] ++ args];
    }

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
}