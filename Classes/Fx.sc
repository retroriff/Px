Fx {
  classvar <activeArgs;
  classvar <>activeEffects;
  classvar <effects;
  classvar <>mixer;
  classvar <>presetsPath;
  classvar <proxy;
  classvar <proxyName;
  classvar <>prSuppressPrint;
  classvar <>skipFlush;
  classvar <vstController;
  classvar <vstPresets;

  *initClass {
    activeArgs = Dictionary.new;
    activeEffects = Dictionary.new;
    effects = Dictionary.new;
    mixer = Dictionary.new;
    proxy = Dictionary.new;
    prSuppressPrint = false;
    skipFlush = false;
    vstPresets = Dictionary.new;

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

  *compressor { |mix = 0.5, thresh = 0.1, ratio = 4, gain = 1|
    var postArgs = "thresh:" + thresh + "ratio:" + ratio + "gain:" + gain;
    this.prAddEffect(\compressor, mix, [thresh, ratio, gain], postArgs);
  }

  *crush { |mix = 0.5, bits = 4, rate = 10000|
    var postArgs = "bits:" + bits + "rate:" + rate;
    this.prAddEffect(\crush, mix, [bits, rate], postArgs);
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

    if (vstController.notNil) {
      vstController.close;
      vstController = nil;
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

  *duck { |mix = 0.5, thresh = 0.005, src|
    var busIndex, lpf = 150;
    var postArgs = "thresh:" + thresh;

    if (mix.isNil or: { mix == Nil })
    { ^this.prAddEffect(\duck, nil) };

    if (src.notNil) {
      busIndex = Ndef(src).bus.index;
      lpf = 20000;
      postArgs = postArgs + "src:" + src;
    } {

      if (Ndef(\px).isPlaying.not)
      { ^this.prPrint("🔴 No patterns playing") };

      busIndex = Ndef(\px).bus.index;
    };

    this.prAddEffect(\duck, mix, [busIndex, thresh, lpf], postArgs);
  }

  *distort { |mix = 0.5, drive = 0.5|
    var postArgs = "drive:" + drive;
    this.prAddEffect(\distort, mix, [drive], postArgs);
  }

  *effectNames {
    ^effects.keys.asArray.sort;
  }

  *flanger { |mix = 0.3|
    this.prAddEffect(\flanger, mix);
  }

  *freqShift { |mix = 0.5, freq = 0, phase = 0|
    var postArgs = "freq:" + freq + "phase:" + phase;
    this.prAddEffect(\freqShift, mix, [freq, phase], postArgs);
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

  *reverse { |mix = 0.4|
    this.prAddEffect(\reverse, mix);
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

  *vibrato { |mix = 0.5, rate = 4, depth = 0.2|
    var postArgs = "rate:" + rate + "depth:" + depth;
    this.prAddEffect(\vibrato, mix, [rate, depth], postArgs);
  }

  *vst { |mix = 0.4, plugin|
    var defaultPlugin = "ValhallaFreqEcho";

    plugin = plugin ?? defaultPlugin;
    this.prAddEffect(\vst, mix, [plugin], plugin);
  }

  *wah { |mix = 0.5, rate = 1.5, depth = 0.8|
    var postArgs = "rate:" + rate + "depth:" + depth;
    this.prAddEffect(\wah, mix, [rate, depth], postArgs);
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

  *prClearProxy { |name|
    activeArgs.removeAt(name);
    activeEffects.removeAt(name);
    mixer.removeAt(name);
  }

  *prAddEffect { |fx, mix, args, postArgs|
    var hasFx = false;

    if (skipFlush.not)
    { PxDebouncer.flush };

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

    if (fx == \vst
      and: { vstController.notNil }
      and: { mix.notNil }
      and: { mix != Nil }) {
      proxy[proxyName].set(\vstBypass, 0);
      this.prSetMixerValue(fx, mix.clip(0, 1));
      this.prPrint("✨ Enabled" + "\\vst" + "mix:" + mix + this.prGetVstPluginName);
    };

    if (hasFx == false and: { mix.isNil.not } and: { mix != Nil })
    { this.prActivateEffect(args, fx, mix, postArgs) };

    if (args != activeArgs[proxyName][fx] and: { mix.isNil.not } and: { mix != Nil })
    { this.prUpdateEffect(args, fx) };

    if (fx == \vst and: (hasFx == false))
    { this.prActivateVst(args, fx) };

    if (mix.isNil or: { mix == Nil }) {
      if (fx == \vst and: { vstController.notNil }) {
        this.prFadeOutVst;
        ^this;
      };

      ^this.prDisableFx(fx);
    };

    this.prMapModulationArgs(fx, args);
    this.prSetMixerValue(fx, mix.clip(0, 1));

    if (fx == \vst)
    { this.prPrint("✨ Enabled" + "\\" ++ fx + "mix:" + mix + postArgs) };
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

      if (fx == \vst) { postArgs = args[0] } {
        this.prPrint("✨ Enabled" + "\\" ++ fx + "mix:" + mix + (postArgs ?? ""));
      };
    };
  }

  *prActivateVst { |args, fx|
    var plugin = args[0];
    var index = this.prGetIndex(fx);
    var vstProxyName = proxyName;

    if (index.isNil) {
      ^"🔴 VST is not enabled";
    };

    {
      vstController = VSTPluginNodeProxyController(proxy[vstProxyName], index).open(
        plugin,
        editor: true,
        action: { |ctrl, ok|

          if (ok) {
            var folder = PathName.new(presetsPath +/+ plugin);
            var files = folder.entries select: { |file| file.extension == "fxp" };

            proxy[vstProxyName].set(\vstBypass, 0);
            this.prPrint("👉 Open VST Editor: Fx.vstController.editor;");
            this.prPrint("👉 Set VST parameter: Fx.vstSet(1, 1);");

            vstPresets[plugin] = files.collect { |file| file.fileNameWithoutExtension };

            if (vstPresets[plugin].size > 0) {
              this.prPrint("📋 Available presets:");
              vstPresets[plugin].do { |name, i|
                this.prPrint("   " ++ i ++ ":" + name);
              };
            };
          };
        }
      );
    }.defer(1);
  }

  *prDisableFx { |fx, noPostln, immediate = false|
    var index = this.prGetIndex(fx);
    var wetIndex = (\wet ++ index).asSymbol;

    if (index.isNil) {
      if (immediate) { ^nil };
      ^("🔴".scatArgs(("\\" ++ fx), "FX not found"));
    };

    this.prFreeModulationNdefs(fx);

    activeArgs[proxyName].removeAt(fx);
    mixer[proxyName].removeAt(fx);
    activeEffects[proxyName].removeAt(fx);

    if (immediate)
    { proxy[proxyName][index] = nil }
    { this.prFadeOutFx(index, fx, wetIndex, noPostln) };
  }

  *prFadeOutVst {
    var index = this.prGetIndex(\vst);
    var wetIndex = (\wet ++ index).asSymbol;

    activeArgs[proxyName].removeAt(\vst);
    activeEffects[proxyName].removeAt(\vst);
    mixer[proxyName].removeAt(\vst);
    vstController.close;
    vstController = nil;
    this.prRampWet(wetIndex, mixer[proxyName][\vst] ? 1, 0, { |p|
      p.set(\vstBypass, 1);
      p[index] = nil;
    });
  }

  *prFadeOutFx { |index, fx, wetIndex, noPostln|
    this.prRampWet(wetIndex, mixer[proxyName][fx] ? 1, 0, { |p|
      p[index] = nil;
    });
  }

  *prRampWet { |wetIndex, from, to, onComplete|
    var targetProxy = proxy[proxyName];
    var steps = 30;

    fork {
      steps.do { |i|
        targetProxy.set(wetIndex, from.blend(to, (i + 1) / steps));
        (1/steps).wait;
      };

      if (onComplete.notNil) { onComplete.value(targetProxy) };
    };
  }

  *prGetIndex { |fx|
    if (activeEffects[proxyName].isNil) { ^nil };

    ^activeEffects[proxyName][fx];
  }

  *prPrint { |value|
    if (prSuppressPrint.not)
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
      var from = mixer[proxyName][fx] ? 1;

      mixer[proxyName][fx] = mix;
      this.prRampWet(wetIndex, from, mix);
    };
  }
}