/*
TODO: Multi instance support (should work with Px + TR08)
*/

Nfx {
    classvar <>effects;
    classvar <>activeEffects;
    classvar proxyName;
    classvar <vstController;

    *initClass {
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

        effects.add(\reverb -> { |room, damp|
            \filterIn -> { |in|
                FreeVerb.ar(in, 1, room, damp);
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

    *blp { |mix = 1|
        this.prAddEffect(\blp, mix);
    }

    *reverb { |mix = 1, room = 0.7, damp = 0.7|
        this.prAddEffect(\reverb, mix, [room, damp]);
    }

    *vst { |mix = 1, plugin|
        this.prAddEffect(\vst, mix, [plugin]);
    }

    *prAddEffect { |fx, mix, args|
        var index, proxy, wetIndex;
        var hasFx = activeEffects.includes(fx);

        if (hasFx == false) {
            activeEffects = activeEffects.add(fx);
        };

        index = activeEffects.indexOf(fx) + 1;
        wetIndex = (\wet ++ index).asSymbol;

        if (this.prIsNdef)
        { proxy = Ndef(proxyName) }
        { proxy = currentEnvironment[proxyName] };

        if (hasFx == false or: (proxy[index].isNil)) {
            proxy[index] = effects.at(fx).value(*args);
        };

        if (fx == \vst and: (hasFx == false)) {
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
        };

        if (mix.isNil or: (mix == Nil)) {
            activeEffects.removeAt(activeEffects.indexOf(fx));
            this.prFadeOutFx(index, proxy, wetIndex);
        } {
            proxy.set(wetIndex, mix)
        };
    }

    *prIsNdef {
       ^Ndef(proxyName).isPlaying;
    }

    *prFadeOutFx { |index, proxy, wetIndex|
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
}