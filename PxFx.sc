+Px {
    *delay { |mix, args|
        this.prFx(\delay, mix, args);
    }

    *hpf { |mix, args|
        this.prFx(\hpf, mix, args);
    }

    *lpf { |mix, args|
        this.prFx(\lpf, mix, args);
    }
    *reverb { |mix, args|
        this.prFx(\reverb, mix, args);
    }

    *wah { |mix, args|
        this.prFx(\wah, mix, args);
    }

    *prFx { |fx, mix, args|
        var name = this.prGetName(currentName);
        lastPatterns[name].do { |pattern|
            pattern.prFx(fx, mix, args);
        };
        this.send(lastPatterns[name], name);
    }

    *prCreatePatternFx { |pattern|
        if (pattern[\fx].notNil and: { pattern[\fx].size > 0 }) {
            pattern[\fx].do { |fx, i|
                if (SynthDescLib.global[fx[1]].notNil) {
                    if (fx == \reverb)
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
        if (pattern[\fxOrder].notNil)
        { ^true };
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