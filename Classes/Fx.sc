/*
TODO: Fix when Ndef is reevaluated, proxy FXs stop
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

  *crush { |mix = 0.5, bits = 4|
    var postArgs = "bits:" + bits;
    this.prAddEffect(\crush, mix, [bits], postArgs);
  }

  *clear { |singleProxy|
    var fx = activeEffects[proxyName];

    if (fx.notNil and: { fx.size > 0 } and: { singleProxy.isNil })
    { this.prPrint("🌵 All effects have been disabled") };

    if (singleProxy.notNil) {
      proxyName = singleProxy.asSymbol;
      fx = activeEffects[proxyName];

      ^fx.copy do: { |slotIndex, fxName|
        this.prDisableFx(fxName, noPostln: true);
      }
    };

    if (fx.notNil) {
      fx do: { |slotIndex, fxName|
        proxy[proxyName][slotIndex] = nil;
      };
    };

    activeArgs.clear;
    activeEffects.clear;
    mixer.clear;
  }

  *delay { |mix = 0.4, delaytime = 0.5, delayfeedback = 0.5|
    var postArgs = "delaytime:" +  delaytime + "delayfeedback:" + delayfeedback;
    this.prAddEffect(\delay, mix, [delaytime, delayfeedback], postArgs);
  }

  *duck { |mix = 0.5, thresh = 0.005|
    if (mix.isNil or: { mix == Nil })
    { ^this.prAddEffect(\duck, nil) };

    if (Ndef(\px).isPlaying.not)
    { ^this.prPrint("🔴 No patterns playing") };

    this.prAddEffect(\duck, mix, [Ndef(\px).bus.index, thresh]);
  }

  *distort { |mix = 0.5, drive = 0.5|
    var postArgs = "drive:" + drive;
    this.prAddEffect(\distort, mix, [drive], postArgs);
  }

  *flanger { |mix = 0.3|
    this.prAddEffect(\flanger, mix);
  }

  *gverb { |mix = 0.4, roomsize = 200, revtime = 5|
    var postArgs = "roomsize:" +  roomsize + "revtime:" + revtime;
    this.prAddEffect(\gverb, mix, [roomsize, revtime], postArgs);
  }

  *hpf { |mix = 1, freq = 1200|
    var postArgs = "freq:" + freq;
    this.prAddEffect(\hpf, mix, [freq], postArgs);
  }

  *loadEffects {
    PathName(("../Effects/").resolveRelative).filesDo{ |file|
      var effect = File.readAllString(file.fullPath).interpret;
      effects.putAll(effect);
    };
  }

  *lpf { |mix = 0.4, freq = 200|
    var postArgs = "freq:" + freq;
    this.prAddEffect(\lpf, mix, [freq], postArgs);
  }

  *pan { |pos = 0|
    var postArgs = "pos:" + pos;

    if (pos.isNil)
    { pos = 0 };

    this.prAddEffect(\pan, 1, [pos], postArgs);
  }

  *phaser { |mix = 0.5, rate = 1.0, depth = 1|
    var postArgs = "rate:" + rate + "depth:" + depth;
    this.prAddEffect(\phaser, mix, [rate, depth], postArgs);
  }

  *reverb { |mix = 0.5, room = 0.7, size = 0.5|
    var postArgs = "room:" +  room + "size:" + size;
    this.prAddEffect(\reverb, mix, [room, size], postArgs);
  }

  *setVstPresetsPath { |path|
    presetsPath = path;
  }

  *space { |mix = 0.2, fb = 0.95|
    var postArgs = "fb:" + fb;

    if (fb == inf)
    { fb = 1 }
    { fb = fb.clip(0, 0.99) };

    this.prAddEffect(\space, mix, [fb], postArgs);
  }

  *tremolo { |mix = 0.6, rate = 1|
    var postArgs = "rate:" + rate;
    this.prAddEffect(\tremolo, mix, [rate], postArgs);
  }

  *vst { |mix = 0.4, plugin|
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
      var folder = PathName.new(presetsPath +/+ this.prGetVstPluginName);

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
      path = presetsPath +/+ this.prGetVstPluginName +/+ preset ++ ".fxp";
      presetName = preset;
    };

    vstController.readProgram(path);
    this.prPrint("🔥 Loaded preset:" + presetName);
  }

  // Animatron
  *vstSet { |param, value|

    if (~isAnimatronEnabled == true)
    { ~animatronNetAddr.sendMsg("/sc/vst", value) };

    vstController.set(param, value);
  }

  *vstWriteProgram { |preset|
    var path = presetsPath +/+ this.prGetVstPluginName +/+ preset ++ ".fxp";
    vstController.writeProgram(path);
  }

  *prGetVstPluginName {
    ^activeArgs[proxyName][\vst][0];
  }

  *prAddEffect { |fx, mix, args, postArgs|
    var hasFx = false;

    PxDebouncer.flush;

    if (args.notNil) {
      args.do { |value|

        if (value.notNil 
          and: { value.isNumber.not }
          and: { value.isFunction.not }
          and: { value.isString.not }
          and: { value.isKindOf(Symbol).not }) {
          ^(
            "🔴 Invalid argument type. Use numbers or wrap UGens in { }, 
            e.g. { SinOsc.kr(t / 16).range(200, 4000) }"
          );
        };
      };
    };

    if (mix.notNil and: { mix != Nil } and: { mix.isNumber.not }) {
      ^("🔴 Invalid mix value. Must be a number (0-1) or Nil.");
    };

    if (activeEffects[proxyName].isNil)
    { activeEffects[proxyName] = Dictionary.new };

    if (activeArgs[proxyName].isNil)
    { activeArgs[proxyName] = Dictionary.new };

    hasFx = activeEffects[proxyName][fx].notNil;

    if (hasFx == false and: { mix.isNil.not } and: { mix != Nil })
    { this.prActivateEffect(args, fx, mix, postArgs) };

    if (args != activeArgs[proxyName][fx] and: { mix.isNil.not } and: { mix != Nil })
    { this.prUpdateEffect(args, fx) };

    if (fx == \vst and: (hasFx == false))
    { this.prActivateVst(args, fx) };

    if (mix.isNil or: { mix == Nil })
    { ^this.prDisableFx(fx) };

    this.prMapModulationArgs(fx, args);
    this.prSetMixerValue(fx, mix.clip(0, 1));
  }

  *prActivateEffect { |args, fx, mix, postArgs|
    var index, buildArgs;

    proxy[proxyName] = Ndef(proxyName);
    index = (activeEffects[proxyName].values.maxItem ?? 0) + 1;
    activeEffects[proxyName][fx] = index;

    if (proxy[proxyName][index].isNil) {
      buildArgs = args.collect { |v| if (v.isFunction) { 0 } { v } };
      proxy[proxyName][index] = effects.at(fx).(*buildArgs);

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

    {
      vstController = VSTPluginNodeProxyController(proxy[proxyName], index).open(
        plugin,
        editor: true
      );

      this.prPrint("👉 Open VST Editor: Fx.vstController.editor;");
      this.prPrint("👉 Set VST parameter: Fx.vstSet(1, 1);");
    }.defer(1);
  }

  *prDisableFx { |fx, noPostln|
    var index = this.prGetIndex(fx);
    var wetIndex = (\wet ++ index).asSymbol;

    if (index.isNil) {
      ^("🔴".scatArgs(("\\" ++ fx), "FX not found"));
    };

    this.prFreeModulationNdefs(fx);

    activeArgs[proxyName].removeAt(fx);
    mixer[proxyName].removeAt(fx);
    activeEffects[proxyName].removeAt(fx);

    this.prFadeOutFx(index, fx, wetIndex, noPostln);
  }

  *prFadeOutFx { |index, fx, wetIndex, noPostln|
    var wet = proxy[proxyName].get(wetIndex, { |f| f });
    var fadeOut = wet / 25;
    var fadedProxyName = proxyName;

    fork {
      while { wet > 0.0 } {
        wet = wet - fadeOut;

        if (wet > 0)
        { proxy[fadedProxyName].set(wetIndex, wet) }
        {
          proxy[fadedProxyName][index] = nil;

          if (vstController.notNil)
          { vstController.close };

          if (proxy[fadedProxyName].isPlaying and: (noPostln != true))
          { this.prPrint("🔇 Disabled".scatArgs(("\\" ++ fx), "FX")) };
        };

        0.25.wait;
      }
    }
  }

  *prGetIndex { |fx|
    if (activeEffects[proxyName].isNil) { ^nil };

    ^activeEffects[proxyName][fx];
  }

  *prPrint { |value|
    if (~isUnitTestRunning != true)
    { value.postln };
  }

  *prUpdateEffect { |args, fx|
    args do: { |value, i|
      if (value.isFunction.not) {
        proxy[proxyName].set((fx ++ (i + 1)).asSymbol, value);
      };

      activeArgs[proxyName].add(fx -> args);
    }
  }

  *prFreeModulationNdefs { |fx|
    var args = activeArgs[proxyName][fx];

    if (args.isNil) { ^nil };

    args.do { |value, i|
      if (value.isFunction) {
        var ndefName = (fx ++ "Mod" ++ (i + 1)).asSymbol;
        Ndef(ndefName).clear;
      };
    };
  }

  *prMapModulationArgs { |fx, args|
    if (args.isNil) { ^nil };

    args.do { |value, i|
      var controlName = (fx ++ (i + 1)).asSymbol;

      if (value.isFunction) {
        var ndefName = (fx ++ "Mod" ++ (i + 1)).asSymbol;
        var ndef = Ndef(ndefName, value);
        proxy[proxyName].map(controlName, ndef);
      };
    };
  }

  *prSetMixerValue { |fx, mix|
    var index = this.prGetIndex(fx);
    var wetIndex = (\wet ++ index).asSymbol;

    if (index.isNil)
    { ^("🔴".scatArgs(("\\" ++ fx), "FX to mix not found")) };

    if (mixer[proxyName].isNil)
    { mixer[proxyName] = Dictionary.new };

    if (mix != mixer[proxyName][fx]) {

      if (fx != \vst)
      { proxy[proxyName].lag(wetIndex, 1) };

      proxy[proxyName].set(wetIndex, mix);
      mixer[proxyName][fx] = mix;
    };
  }
}