Nfx {
    classvar <>effects;
    classvar <>activeEffects;
    classvar proxyName;

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
        var index, proxy;
        var hasFx = activeEffects.includes(fx);

        if (hasFx == false) {
            activeEffects = activeEffects.add(fx);
        };

        index = activeEffects.indexOf(fx) + 1;

        if (Ndef(proxyName).isPlaying)
        { proxy = Ndef(proxyName) }
        { proxy = currentEnvironment.at(proxyName) };

        if (hasFx == false or: (proxy[index].isNil)) {
            proxy[index] = effects.at(fx).value(*args);
        };

        if (fx == \vst and: (hasFx == false)) {
            ~vst = VSTPluginNodeProxyController(proxy, 1).open(
                args[0],
                editor: true
            );
            "Open the VST plugin editor:".postcln;
            "~vst.editor;".postln;
        };

        if (mix.isNil) {
            "mix is nil".postln;
            if (~vst.notNil)
            { ~vst.close; "close".postln };
            activeEffects.removeAt(index);
            activeEffects.postln;
        } {
            proxy.set((\wet ++ index).asSymbol, mix)
        };
    }
}