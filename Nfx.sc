/*
TODO: Multi instance support (should work with Px + TR08)
TODO: HPF when I swtich to wave false it should change (preguntar Roger)
*/

Nfx {
    classvar <effects;
    classvar <activeArgs;
    classvar <activeEffects;
    classvar proxy;
    classvar proxyName;
    classvar <vstController;

    *initClass {
        activeArgs = Dictionary.new;
        activeEffects = Array.new;
        effects = Dictionary.new;

        effects.add(\blp -> {
            \filterIn -> { |in|
                BLowPass4.ar(
                    in: in,
                    freq: SinOsc.kr(1/8).range(700, 20000),
                    rq: 0.1
                );
            }
        });

        effects.add(\hpf -> { |wave|
            \filterIn -> { |in|
                if (wave == true)
                { RHPF.ar(in, SinOsc.kr(1/8).range(0, 1200), rq: 0.1) }
                { RHPF.ar(in, 1200, rq: 0.1) }
            }
        });

        effects.add(\reverb -> { |room = 0.7, damp = 0.7|
            \filterIn -> { |in|
                FreeVerb.ar(
                    in,
                    mix: 1,
                    room: Ndef(\reverb1, room),
                    damp: Ndef(\reverb2, damp)
                );
            }
        });

        effects.add(\vst -> {
            \vstFilter -> { |in|
                VSTPlugin.ar(in, 2);
            }
        });
    }

    *new { |name|
        proxyName = name ?? \px;
    }

    *hpf { |mix = 1, wave|
        this.prAddEffect(\hpf, mix, [wave]);
    }

    *blp { |mix = 0.4|
        this.prAddEffect(\blp, mix);
    }

    *reverb { |mix = 0.3, room = 0.7, damp = 0.7|
        this.prAddEffect(\reverb, mix, [room, damp]);
    }

    *vst { |mix = 1, plugin|
        this.prAddEffect(\vst, mix, [plugin]);
    }

    *prAddEffect { |fx, mix, args|
        var index, wetIndex;
        var hasFx = activeEffects.includes(fx);

        if (hasFx == false) {
            activeEffects = activeEffects.add(fx);
        };

        index = activeEffects.indexOf(fx) + 1;

        this.prCreateProxy;

        if (hasFx == false or: (proxy[index].isNil)) {
            proxy[index] = effects.at(fx).value(*args);
            activeArgs = activeArgs.add(fx -> args);
        };

        if (args != activeArgs[fx]) {
            this.prUpdateEffect(args, fx);
        };

        if (fx == \vst and: (hasFx == false)) {
            this.prActivateVst(args);
        };

        this.prSetMixerValue(fx, index, mix);
    }

    *prActivateVst { |args|
        vstController = VSTPluginNodeProxyController(proxy, 1).open(
            args[0],
            editor: true
        );

        // Avoids globals variables on ProxySpace
        if (this.prIsNdef)
        { ~vst = vstController };

        "Px and Ndef: ~vst.editor; ~vst.set(1, 1);".postln;
        "ProxySpace: Ndef.vstController.editor; Ndef.vstController.set(1, 1);".postln;
        "ProxySpace instances of NodeProxy: ~m[0] = ...".postln;
    }


    *prCreateProxy {
        if (this.prIsNdef)
        { proxy = Ndef(proxyName) }
        { proxy = currentEnvironment[proxyName] };
    }


    *prIsNdef {
        ^Ndef(proxyName).isPlaying;
    }

    *prFadeOutFx { |index, wetIndex|
        var wet = proxy.get(wetIndex, { |f| f });
        var fadeOut = wet / 25;

        fork {
            while { wet > 0.0 } {
                wet = wet - fadeOut;

                if (wet > 0)
                { proxy.set(wetIndex, wet) }
                {
                    proxy[index] = nil;
                    if (vstController.notNil)
                    { vstController.close };
                };
                0.25.wait;
            }
        }
    }

    *prUpdateEffect { |args, fx|
        args do: { |value, i|
            Ndef((fx ++ (i + 1)).asSymbol, value);
            activeArgs = activeArgs.add(fx -> args);
        }
    }

    *prSetMixerValue { |fx, index, mix|
        var wetIndex = (\wet ++ index).asSymbol;

        if (mix.isNil or: (mix == Nil)) {
            activeEffects.removeAt(activeEffects.indexOf(fx));
            this.prFadeOutFx(index, wetIndex);
        } {
            proxy.set(wetIndex, mix)
        };
    }
}