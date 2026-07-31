Fx {
  classvar <>chains;
  classvar <effects;
  classvar <>presetsPath;
  classvar <proxyName;
  classvar <>prSuppressPrint;
  classvar <>skipFlush;
  classvar <vstController;
  classvar <vstPresets;

  *initClass {
    chains = Dictionary.new;
    effects = Dictionary.new;
    prSuppressPrint = false;
    skipFlush = false;
    vstPresets = Dictionary.new;

    this.loadEffects;

    CmdPeriod.add {
      Fx.prSuppressPrint = true;
      this.clear;
      Fx.prSuppressPrint = false;
    };
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
    var chain = chains[proxyName];

    if ((proxyName == \lx or: { proxyName == \dx }) and: { singleProxy.isNil }) {
      var ids = this.prGroupIds(proxyName);
      var groupName = proxyName;

      ids.do { |id| this.clear(id) };
      proxyName = groupName;
      this.prPrint("🌵 All effects disabled on" + groupName);
      ^this;
    };

    if (chain.notNil and: { chain.effects.size > 0 } and: { singleProxy.isNil })
    { this.prPrint("🌵 All effects have been disabled") };

    if (singleProxy.notNil) {
      proxyName = singleProxy.asSymbol;
      chain = chains[proxyName];

      if (chain.isNil) { ^this };

      ^chain.effects.copy do: { |slotIndex, fxName|
        this.prDisableFx(fxName, noPostln: true);
      }
    };

    if (vstController.notNil) {
      vstController.close;
      vstController = nil;
    };

    if (chain.notNil) {
      chain.effects do: { |slotIndex, fxName|
        Ndef(proxyName)[slotIndex] = nil;
      };
    };

    chains.clear;
  }

  *delay { |mix = 0.3, delaytime = 0.25, delayfeedback = 0.4|
    var postArgs = "delaytime:" +  delaytime + "delayfeedback:" + delayfeedback;
    this.prAddEffect(\delay, mix, [delaytime, delayfeedback], postArgs);
  }

  *delay2 { |mix = 0.4, delaytime = 0.5, delayfeedback = 0.5|
    var postArgs = "delaytime:" +  delaytime + "delayfeedback:" + delayfeedback;
    this.prAddEffect(\delay2, mix, [delaytime, delayfeedback], postArgs);
  }

  *duck { |mix = 0.5, thresh = 0.005, src|
    var busIndex, lpf = 150;
    var postArgs = "thresh:" + thresh;

    if (mix.isNil or: { mix == Nil })
    { this.prAddEffect(\duck, nil); ^this };

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

  *dist { |mix = 0.5, drive = 0.5|
    var postArgs = "drive:" + drive;
    this.prAddEffect(\dist, mix, [drive], postArgs);
  }

  *activeEffects {
    ^chains.collect { |chain| chain.effects };
  }

  *activeArgs {
    ^chains.collect { |chain| chain.args };
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

  *hpf { |mix = 1, freq = 1200, gain = 1|
    var postArgs = "freq:" + freq + "gain:" + gain;
    this.prAddEffect(\hpf, mix, [freq, gain], postArgs);
  }

  *loadEffects {
    PathName(("../Effects/").resolveRelative).filesDo{ |file|
      var effect = File.readAllString(file.fullPath).interpret;
      effects.putAll(effect);
    };
  }

  *lpf { |mix = 0.4, freq = 200, gain = 1|
    var postArgs = "freq:" + freq + "gain:" + gain;
    this.prAddEffect(\lpf, mix, [freq, gain], postArgs);
  }

  *pan { |pos = 0|
    var postArgs = "pos:" + pos;

    if (pos == \rand) {
      var trig = LFNoise1.kr(0.5).range(0.3, 2);
      pos = { LFNoise1.kr(trig).range(-0.6, 0.6) };
    };

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

  *remove { |id|
    var chain;

    id = id.asSymbol;
    chain = chains[id];

    if (chain.isNil) { ^this };

    chain.effects.keys.do { |fxName|
      var args = chain.args[fxName];

      if (args.notNil) {
        args.do { |value, i|
          if (value.isFunction) {
            var ndefName = (fxName ++ "Mod" ++ (i + 1)).asSymbol;
            Ndef(ndefName).clear;
          };
        };
      };
    };

    chains.removeAt(id);
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
    ^chains[proxyName].args[\vst][0];
  }

  *prEnsureChain {
    ^chains[proxyName] ?? {
      var chain = FxChain.new;
      chains[proxyName] = chain;
      chain;
    };
  }

  *prAddEffect { |fx, mix, args, postArgs|
    var chain, hasFx = false;

    if (proxyName == \lx or: { proxyName == \dx }) {
      var ids = this.prGroupIds(proxyName);
      var groupName = proxyName;

      if (ids.isEmpty)
      { ^this.prPrint("🔴 No" + groupName + "patterns playing") };

      prSuppressPrint = true;
      ids.do { |id|
        proxyName = id;
        this.prAddEffect(fx, mix, args, postArgs);
      };
      prSuppressPrint = false;
      proxyName = groupName;

      if (mix.isNil or: { mix == Nil })
      { this.prPrint("🌵 Disabled" + "\\" ++ fx + "on" + groupName) }
      { this.prPrint("✨ Enabled" + "\\" ++ fx + "on" + groupName + "mix:" + mix + (postArgs ?? "")) };

      ^this;
    };

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

    chain = this.prEnsureChain;
    hasFx = chain.effects[fx].notNil
      and: { Ndef(proxyName).objects[chain.effects[fx]].notNil };

    if (fx == \vst
      and: { vstController.notNil }
      and: { mix.notNil }
      and: { mix != Nil }) {
      Ndef(proxyName).set(\vstBypass, 0);
      this.prSetMixerValue(fx, mix.clip(0, 1));
      this.prPrint("✨ Enabled" + "\\vst" + "mix:" + mix + this.prGetVstPluginName);
    };

    if (hasFx == false and: { mix.isNil.not } and: { mix != Nil })
    { this.prActivateEffect(args, fx, mix, postArgs) };

    if (args != chain.args[fx] and: { mix.isNil.not } and: { mix != Nil })
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
    { this.prPrint("✨ Enabled" + "\\" ++ fx + "mix:" + mix + (postArgs ?? "")) };
  }

  *prActivateEffect { |args, fx, mix, postArgs|
    var chain = chains[proxyName];
    var index, buildArgs;

    index = chain.effects[fx] ?? { (chain.effects.values.maxItem ?? 0) + 1 };
    chain.effects[fx] = index;

    if (Ndef(proxyName)[index].isNil) {
      buildArgs = args.collect { |v| if (v.isFunction) { 0 } { v } };
      Ndef(proxyName)[index] = effects.at(fx).(*buildArgs);
      chain.args.add(fx -> args);

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
      vstController = VSTPluginNodeProxyController(Ndef(vstProxyName), index).open(
        plugin,
        editor: true,
        action: { |ctrl, ok|

          if (ok) {
            var folder = PathName.new(presetsPath +/+ plugin);
            var files = folder.entries select: { |file| file.extension == "fxp" };

            Ndef(vstProxyName).set(\vstBypass, 0);
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
    var chain = chains[proxyName];
    var index = this.prGetIndex(fx);
    var wetIndex = (\wet ++ index).asSymbol;

    if (index.isNil) {
      if (immediate) { ^nil };
      ^("🔴".scatArgs(("\\" ++ fx), "FX not found"));
    };

    this.prFreeModulationNdefs(fx);

    chain.args.removeAt(fx);
    chain.mixer.removeAt(fx);
    chain.effects.removeAt(fx);

    if (immediate)
    { Ndef(proxyName)[index] = nil }
    { this.prFadeOutFx(index, fx, wetIndex, noPostln) };
  }

  *prFadeOutVst {
    var chain = chains[proxyName];
    var index = this.prGetIndex(\vst);
    var wetIndex = (\wet ++ index).asSymbol;
    var wet = chain.mixer[\vst] ? 1;

    chain.args.removeAt(\vst);
    chain.effects.removeAt(\vst);
    chain.mixer.removeAt(\vst);
    vstController.close;
    vstController = nil;
    this.prRampWet(wetIndex, wet, 0, { |p|
      p.set(\vstBypass, 1);
      p[index] = nil;
    });
  }

  *prFadeOutFx { |index, fx, wetIndex, noPostln|
    var chain = chains[proxyName];

    this.prRampWet(wetIndex, chain.mixer[fx] ? 1, 0, { |p|
      p[index] = nil;
    });
  }

  *prRampWet { |wetIndex, from, to, onComplete|
    var targetProxy = Ndef(proxyName);
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
    var chain = chains[proxyName];

    if (chain.isNil) { ^nil };

    ^chain.effects[fx];
  }

  *prPrint { |value|
    if (prSuppressPrint.not)
    { value.postln };
    value;
  }


  *prUpdateEffect { |args, fx|
    var chain = chains[proxyName];

    args do: { |value, i|
      if (value.isFunction.not) {
        Ndef(proxyName).set((fx ++ (i + 1)).asSymbol, value);
      };

      chain.args.add(fx -> args);
    }
  }

  *prFreeModulationNdefs { |fx|
    var chain = chains[proxyName];
    var args = chain.args[fx];

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
        Ndef(proxyName).map(controlName, ndef);
      };
    };
  }

  *prGroupIds { |group|
    var key = if (group == \lx) { \lx } { \dx };
    ^Px.last.keys.select { |id| Px.last[id][key] == true }.asArray;
  }

  *prSetMixerValue { |fx, mix|
    var chain = chains[proxyName];
    var index = this.prGetIndex(fx);
    var wetIndex = (\wet ++ index).asSymbol;

    if (index.isNil)
    { ^("🔴".scatArgs(("\\" ++ fx), "FX to mix not found")) };

    if (mix != chain.mixer[fx]) {
      var from = chain.mixer[fx] ? 1;

      chain.mixer[fx] = mix;
      this.prRampWet(wetIndex, from, mix);
    } {
      Ndef(proxyName).set(wetIndex, mix);
    };
  }
}

FxChain {
  var <>effects;
  var <>args;
  var <>mixer;

  *new {
    ^super.new.init;
  }

  init {
    effects = Dictionary.new;
    args = Dictionary.new;
    mixer = Dictionary.new;
  }

  isEmpty {
    ^effects.isEmpty;
  }

  fxNames {
    ^effects.keys;
  }
}
