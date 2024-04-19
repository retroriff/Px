/*
TODO: Multiname
TODO: Fix when Ndef is reevaluated, proxy FXs stop
TODO: Fix error when it is started with ".hpf(1, \wave)"
TODO: Fix Px.release disables FX before releasing
*/

Nfx {
    classvar <activeArgs;
    classvar <>activeEffects;
    classvar <effects;
    classvar <>mixer;
    classvar <>presetsPath;
    classvar <proxy;
    classvar proxyName;
    classvar <vstController;

    *initClass {
        activeArgs = Dictionary.new;
        activeEffects = Array.new;
        effects = Dictionary.new;
        mixer = Dictionary.new;

        effects.add(\blp -> {
            \filterIn -> { |in|
                BLowPass4.ar(
                    in: in,
                    freq: SinOsc.kr(1/8).range(700, 20000),
                    rq: 0.1
                );
            }
        });

        effects.add(\hpf -> { |freq = 1200|
            \filterIn -> { |in|
                RHPF.ar(in, \hpf1.kr(freq).poll, rq: 0.1);
            }
        });

        effects.add(\reverb -> { |room = 0.7, damp = 0.7|
            \filterIn -> { |in|
                FreeVerb.ar(
                    in,
                    mix: 1,
                    room: \reverb1.kr(room),
                    damp: \reverb2.kr(damp)
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

    *clear {
        activeArgs = activeArgs.clear;
        activeEffects do: { |fx, i|
            proxy[i + 1] = nil;
        };
        activeEffects = Array.new;
        this.prPrint("All effects have been disabled");
    }

    *hpf { |mix = 1, freq = 1200|
        if (freq == \wave)
        { freq = Ndef(\hpf1, { SinOsc.kr(1/8).range(400, 1200) } ) };
        this.prAddEffect(\hpf, mix, [freq]);
    }

    *blp { |mix = 0.4|
        this.prAddEffect(\blp, mix);
    }

    *reverb { |mix = 0.3, room = 0.7, damp = 0.7|
        this.prAddEffect(\reverb, mix, [room, damp]);
    }

    *setPresetsPath { |path|
        presetsPath = path;
    }

    *vst { |mix = 1, plugin|
        this.prAddEffect(\vst, mix, [plugin]);
    }

    *vstReadProgram { |preset|
        var index = this.prGetIndex(\vst);
        var path;

        if (index.isNil) {
            ^"🔴 VST is not enabled";
        };

        path = presetsPath ++ this.prGetVstPluginName ++ "/" ++ preset ++ ".fxp";
        vstController.readProgram(path);
        this.prPrint("🔥 Loaded".scatArgs(("\\" ++ preset), "preset"));
    }

    *vstWriteProgram { |preset|
        var path = presetsPath ++ this.prGetVstPluginName ++ "-" ++ preset ++ ".fxp";
        vstController.writeProgram(path);
    }

    *prGetVstPluginName {
        ^activeArgs[\vst][0];
    }

    *prAddEffect { |fx, mix, args|
        var hasFx = activeEffects.includes(fx);

        if (hasFx == false and: (mix != Nil)) {
            this.prActivateEffect(args, fx);
        };

        if (args != activeArgs[fx]) {
            this.prUpdateEffect(args, fx);
        };

        if (fx == \vst and: (hasFx == false)) {
            this.prActivateVst(args, fx);
        };

        if (mix.isNil or: (mix == Nil)) {
            ^this.prDisableFx(fx, mix);
        };

        this.prSetMixerValue(fx, mix);
    }

    *prActivateEffect { |args, fx|
        var index;
        proxy = Ndef(proxyName);
        activeEffects = activeEffects.add(fx);
        index = this.prGetIndex(fx);

        if (proxy[index].isNil) {
            proxy[index] = effects.at(fx).(*args);
            activeArgs = activeArgs.add(fx -> args);
            this.prPrint("✨ Enabled".scatArgs(("\\" ++ fx), "FX"));
        };
    }

    *prActivateVst { |args, fx|
        var plugin = args[0];
        var index = this.prGetIndex(fx);

        if (index.isNil) {
            ^"🔴 VST is not enabled";
        };

        vstController = VSTPluginNodeProxyController(proxy, index).open(
            plugin,
            editor: true
        );

        this.prPrint("✨ Enabled".scatArgs("\\vst", plugin));
        this.prPrint("👉 Open VST Editor: Nfx.vstController.editor;");
        this.prPrint("👉 Set VST parameter: Nfx.vstController.set(1, 1);");
    }

    *prDisableFx { |fx, mix|
        var index = this.prGetIndex(fx);
        var wetIndex = (\wet ++ index).asSymbol;

        if (index.isNil) {
            ^this.prPrint("🔴".scatArgs(("\\" ++ fx), "FX not found"));
        };

        activeArgs.removeAt(fx);
        activeEffects.removeAt(activeEffects.indexOf(fx));
        this.prFadeOutFx(index, fx, wetIndex);
    }

    *prFadeOutFx { |index, fx, wetIndex|
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

                    if (proxy.isPlaying)
                    { this.prPrint("🔇 Disabled".scatArgs(("\\" ++ fx), "FX")) };
                };
                0.25.wait;
            }
        }
    }

    *prGetIndex { |fx|
        var index = activeEffects.indexOf(fx);
        if (index.notNil)
        { index = index + 1 };
        ^index;
    }

    *prPrint { |value|
        if (~isUnitTestRunning != true)
        { value.postln };
    }

    *prUpdateEffect { |args, fx|
        args do: { |value, i|
            proxy.set((fx ++ (i + 1)).asSymbol, value);
            activeArgs = activeArgs.add(fx -> args);
        }
    }

    *prSetMixerValue { |fx, mix|
        var index = this.prGetIndex(fx);
        var wetIndex = (\wet ++ index).asSymbol;

        if (index.isNil)
        { ^this.prPrint("🔴".scatArgs(("\\" ++ fx), "FX not found")) };

        if (mix != mixer[fx]) {
            proxy.set(wetIndex, mix);
            mixer[fx] = mix;
        };
    }
}