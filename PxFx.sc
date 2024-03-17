+Px {
    *initClass {
        var flanger = \filterIn -> { |in|
            AnalogPhaser.ar(
                in,
                SinOsc.ar(0.22),
                skew: SinOsc.kr(0.059),
                feedback: SinOsc.kr(0.005, 1pi).range(0.0,0.85),
                modulation: SinOsc.kr(0.0192, 2pi).unipolar,
                stages: 50
            );
        };
        var freeVerb = \filterIn -> { |in|
            FreeVerb.ar(in, mix: 1, room: 1);
        };
        nodeProxy = Dictionary.new;
        nodeProxyFxOrder = Dictionary.new;
        nodeProxySynthDefControls = Dictionary[
            \flanger -> flanger,
            \freeverb -> freeVerb
        ];
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

    *flanger { |mix = 1|
        this.prProxyFx(\flanger, mix);
    }

    *freeverb { |mix = 1|
        this.prProxyFx(\freeverb, mix);
    }

    *reverb { |mix, args|
        this.prFx(\reverb, mix, args);
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

    *prProxyFx { |fx, mix, args|
        var index;

        if (mix.isNil) {
            nodeProxy[currentName][index] = nil;
            nodeProxyFxOrder[currentName].remove(fx);
        } {
            if (nodeProxyFxOrder[currentName].isArray.not)
            { nodeProxyFxOrder.add(currentName -> Array.new) };

            nodeProxyFxOrder[currentName] = nodeProxyFxOrder[currentName].add(fx);
            index = nodeProxyFxOrder[currentName].indexOf(fx) + 1;

            if (nodeProxy[currentName][index] != nodeProxySynthDefControls[fx]) {
                nodeProxy[currentName][index] = nodeProxySynthDefControls[fx];
            };

            nodeProxy[currentName].set((\wet ++ index).asSymbol, mix ?? 0.7);
        };

        currentName.postln;

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