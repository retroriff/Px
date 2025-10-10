/*
TODO: Fix delay wthout params disabled instead of enabled
TODO: Fix when Ndef is reevaluated, proxy FXs stop
TODO: Fix error when it is started with ".hpf(1, \wave)"
*/

Fx {
  classvar <activeArgs;
  classvar <>activeEffects;
  classvar <effects;
  classvar <>mixer;
  classvar <>presetsPath;
  classvar <proxy;
  classvar <proxyName;
  classvar <vstController;

  *initClass {
    activeArgs = Dictionary.new;
    activeEffects = Dictionary.new;
    effects = Dictionary.new;
    mixer = Dictionary.new;
    proxy = Dictionary.new;

    this.loadEffects;
  }

  *new { |name|
    if (name.isNil)
    { name = \px };

    proxyName = name.asSymbol;
  }

  *blp { |mix = 0.4|
    this.prAddEffect(\blp, mix);
  }

  *clear {
    var fx = activeEffects[proxyName];

    if (fx.isArray and: { fx.isEmpty.not })
    { this.prPrint("🌵 All effects have been disabled") };

    fx do: { |fx, i|
      proxy[proxyName][i + 1] = nil;
    };

    activeArgs.clear;
    activeEffects.clear;
    mixer.clear;
  }

  *delay { |mix = 0.4, delaytime = 8, decaytime = 2|
    var postArgs = "delaytime:" +  delaytime + "decaytime:" + decaytime;
    this.prAddEffect(\delay, mix, [delaytime, decaytime], postArgs);
  }

  *flanger { |mix = 0.4|
    this.prAddEffect(\flanger, mix);
  }

  *gverb { |mix = 0.4, roomsize = 200, revtime = 5|
    var postArgs = "roomsize:" +  roomsize + "revtime:" + revtime;
    this.prAddEffect(\gverb, mix, [roomsize, revtime], postArgs);
  }

  *hpf { |mix = 1, freq = 1200|
    var postArgs = "freq:" +  freq;
    
    if (freq == \wave)
    { freq = Ndef(\hpf1, { SinOsc.kr(1/8).range(400, 1200) } ) };

    this.prAddEffect(\hpf, mix, [freq], postArgs);
  }

  *loadEffects {
    PathName(("../Effects/").resolveRelative).filesDo{ |file|
      var effect = File.readAllString(file.fullPath).interpret;
      effects.putAll(effect);
    };
  }

  *lpf { |mix = 0.4, freq = 200|
    var postArgs = "freq:" +  freq;

    if (freq == \wave)
    { freq = Ndef(\lpf1, { SinOsc.kr(1/8).range(200, 400) } ) };

    this.prAddEffect(\lpf, mix, [freq], postArgs);
  }

  *pan { |pos = 0|
    var postArgs = "pos:" +  pos;

    if (pos == \wave)
    { pos = Ndef(\pan1, { SinOsc.kr(1/8).range(-1.0, 1.0) } ) };

    if (pos == Nil)
    { pos = 0 };

    this.prAddEffect(\pan, 1, [pos], postArgs);
  }

  *reverb { |mix = 0.3, room = 0.7, damp = 0.7|
    var postArgs = "room:" +  room + "damp:" + damp;
    this.prAddEffect(\reverb, mix, [room, damp], postArgs);
  }

  *setVstPresetsPath { |path|
    presetsPath = path;
  }

  *space { |mix = 0.4, fb = 0.95|
    var postArgs = "fb:" + fb;

    if (fb == inf)
    { fb = 1 }
    { fb = fb.clip(0, 0.99) };

    this.prAddEffect(\space, mix, [fb], postArgs);
  }


  *vst { |mix = 1, plugin|
    var defaultPlugin = "ValhallaFreqEcho";

    this.prAddEffect(\vst, mix, [plugin ?? defaultPlugin]);
  }

  *vstReadProgram { |preset = 0|
    var index = this.prGetIndex(\vst);
    var path, presetName;

    if (index.isNil) {
      ^"🔴 VST is not enabled";
    };

    if (preset.isInteger) {
      var folder = PathName.new(presetsPath ++ this.prGetVstPluginName ++ "/");

      var files = folder.entries select: { |file|
        file.extension == "fxp";
      };

      if (preset >= files.size) {
        ^("Available presets for" + this.prGetVstPluginName ++ ": %")
        .format(files.size - 1);
      };

      path = files[preset].fullPath;
      presetName = files[preset].fileNameWithoutExtension;
    } {
      path = presetsPath ++ this.prGetVstPluginName ++ "/" ++ preset ++ ".fxp";
      presetName = preset;
    };

    vstController.readProgram(path);
    this.prPrint("🔥 Loaded preset:" + presetName);
  }

  // Animatron
  *vstSet { |param, value|
    ~animatronNetAddr.sendMsg("/sc/vst", value);
    vstController.set(param, value);
  }

  *vstWriteProgram { |preset|
    var path = presetsPath ++ this.prGetVstPluginName ++ "-" ++ preset ++ ".fxp";
    vstController.writeProgram(path);
  }

  *prGetVstPluginName {
    ^activeArgs[proxyName][\vst][0];
  }

  *prAddEffect { |fx, mix, args, postArgs|
    var hasFx = false;

    if (activeEffects[proxyName].isNil)
    { activeEffects[proxyName] = Array.new };

    hasFx = activeEffects[proxyName].includes(fx);

    if (hasFx == false and: (mix != Nil))
    { this.prActivateEffect(args, fx, mix, postArgs) };

    if (args != activeArgs[proxyName][fx] and: (mix != Nil))
    { this.prUpdateEffect(args, fx) };

    if (fx == \vst and: (hasFx == false))
    { this.prActivateVst(args, fx) };

    if (mix.isNil or: (mix == Nil))
    { ^this.prDisableFx(fx) };

    this.prSetMixerValue(fx, mix.clip(0, 1));
  }

  *prActivateEffect { |args, fx, mix, postArgs|
    var index;
    proxy[proxyName] = Ndef(proxyName);
    activeEffects[proxyName] = activeEffects[proxyName].add(fx);
    index = this.prGetIndex(fx);

    if (proxy[proxyName][index].isNil) {
      proxy[proxyName][index] = effects.at(fx).(*args);

      if (activeArgs[proxyName].isNil)
      { activeArgs[proxyName] = Dictionary.new };

      activeArgs[proxyName].add(fx -> args);
      if (postArgs.isNil)
      { postArgs = "no args" };
      this.prPrint("✨ Enabled" + "\\" ++ fx + "mix:" + mix + postArgs);
    };
  }

  *prActivateVst { |args, fx|
    var plugin = args[0];
    var index = this.prGetIndex(fx);

    if (index.isNil) {
      ^"🔴 VST is not enabled";
    };

    vstController = VSTPluginNodeProxyController(proxy[proxyName], index).open(
      plugin,
      editor: true
    );

    this.prPrint("👉 Open VST Editor: Fx.vstController.editor;");
    this.prPrint("👉 Set VST parameter: Fx.vstSet(1, 1);");
  }

  *prDisableFx { |fx|
    var index = this.prGetIndex(fx);
    var wetIndex = (\wet ++ index).asSymbol;

    if (index.isNil) {
      ^this.prPrint("🔴".scatArgs(("\\" ++ fx), "FX not found"));
    };

    activeArgs[proxyName].removeAt(fx);
    mixer[proxyName].removeAt(fx);

    if (activeEffects[proxyName].indexOf(fx).notNil)
    { activeEffects[proxyName].removeAt(activeEffects[proxyName].indexOf(fx)) };

    this.prFadeOutFx(index, fx, wetIndex);
  }

  *prFadeOutFx { |index, fx, wetIndex|
    var wet = proxy[proxyName].get(wetIndex, { |f| f });
    var fadeOut = wet / 25;

    fork {
      while { wet > 0.0 } {
        wet = wet - fadeOut;

        if (wet > 0)
        { proxy[proxyName].set(wetIndex, wet) }
        {
          proxy[proxyName][index] = nil;

          if (vstController.notNil)
          { vstController.close };

          if (proxy[proxyName].isPlaying)
          { this.prPrint("🔇 Disabled".scatArgs(("\\" ++ fx), "FX")) };
        };

        0.25.wait;
      }
    }
  }

  *prGetIndex { |fx|
    var index = activeEffects[proxyName].indexOf(fx);

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
      proxy[proxyName].set((fx ++ (i + 1)).asSymbol, value);
      activeArgs[proxyName].add(fx -> args);
      this.prPrint("🌶️ Updated".scatArgs(fx));
    }
  }

  *prSetMixerValue { |fx, mix|
    var index = this.prGetIndex(fx);
    var wetIndex = (\wet ++ index).asSymbol;

    if (index.isNil)
    { ^this.prPrint("🔴".scatArgs(("\\" ++ fx), "FX to mix not found")) };

    if (mixer[proxyName].isNil)
    { mixer[proxyName] = Dictionary.new };

    if (mix != mixer[proxyName][fx]) {
      proxy[proxyName].set(wetIndex, mix);
      mixer[proxyName][fx] = mix;
    };
  }
}