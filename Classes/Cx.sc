Cx {
  classvar <>assignments;
  classvar <>config;
  classvar <>configName;
  classvar <>debug;
  classvar <>midiFuncs;

  *initClass {
    assignments = Dictionary.new;
    debug = true;
    midiFuncs = Array.new;

    CmdPeriod.add { this.stop };
  }

  *assign { |slot, patternId|
    assignments[slot.asInteger] = patternId.asSymbol;
  }

  *clear {
    this.stop;
    assignments = Dictionary.new;
    config = nil;
    configName = nil;
  }

  *list {
    var sortedIds;

    if (config.isNil)
    { ^"🔴 Cx: no config loaded".postln };

    sortedIds = Px.prSortedPatternIds;

    ("🎛️ Cx" + configName + "→" + config[\controls].size + "controls").postln;

    config[\controls] do: { |row, index|
      var slot = index + 1;
      var pinned = assignments[slot];
      var targetId = pinned ?? sortedIds[index] ?? "(empty)";

      ("  slot" + slot + "cc" + row[\cc] + "→" + targetId + row[\param]).postln;
    };
  }

  *load { |name|
    var path = ("../Presets/cx/" ++ name ++ ".yaml").resolveRelative;

    if (File.exists(path).not)
    { ^("🔴 Cx: config not found:" + path).warn };

    config = PresetsFromYAML(File.readAllString(path).parseYAML);
    configName = name.asSymbol;

    if (config[\encoderMode].isNil)
    { config[\encoderMode] = '2s_comp' }
    { config[\encoderMode] = config[\encoderMode].asSymbol };

    config[\controls] = config[\controls] collect: { |row|
      row[\param] = row[\param].asSymbol;
      if (row[\mode].notNil)
      { row[\mode] = row[\mode].asSymbol };
      row;
    };

    ^("✅ Cx: loaded" + name);
  }

  *play { |name|
    var srcID;

    if (name.notNil or: { config.isNil })
    { this.load(name ?? \midilab3) };

    this.stop;

    MIDIClient.init(verbose: false);
    MIDIIn.connectAll;
    srcID = this.prSourceId(config[\device], config[\port]);

    config[\controls] do: { |row, index|
      var slot = index + 1;

      midiFuncs = midiFuncs.add(MIDIFunc.cc({ |value|
        this.prHandle(slot, row, value);
      }, row[\cc], srcID: srcID));
    };

    ^("✅ Cx: playing" + configName + "(" ++ midiFuncs.size + "controls)");
  }

  *stop {
    midiFuncs do: { |func| func.free };
    midiFuncs = Array.new;
  }

  *prExtractScalar { |x|
    var scalars;

    if (x.isNumber)
    { ^x };

    if (x.isKindOf(Rest))
    { ^nil };

    if (x.isKindOf(Pwhite))
    { ^x.hi };

    if (x.isKindOf(Pattern) and: { x.respondsTo(\list) and: { x.list.notNil } }) {
      scalars = x.list.collect { |item| this.prExtractScalar(item) }.reject { |v| v.isNil };

      if (scalars.size > 0)
      { ^scalars.maxItem };
    };

    ^nil;
  }

  *prDecodeRelative { |mode, value|
    mode.switch(
      '2s_comp',    { if (value <= 64) { ^value } { ^value - 128 } },
      'signed_bit', { if (value < 64) { ^value.neg } { ^value - 64 } },
      'bin_offset', { ^value - 64 },
      'absolute',   { ^value }
    );

    ^value;
  }

  *prHandle { |slot, row, value|
    var mode = row[\mode] ?? config[\encoderMode];
    var step = this.prDecodeRelative(mode, value);
    var targetId = this.prResolveTarget(slot);
    var param = row[\param];
    var pattern, source, current, next, newValue;

    if (targetId.isNil) {
      if (debug == true)
      { ("🎛️ cc" + row[\cc] + "raw" + value + "step" + step + "→ slot" + slot + "(no target)").postln };
      ^this;
    };

    pattern = Px.last[targetId];
    if (pattern.isNil) {
      if (debug == true)
      { ("🎛️ cc" + row[\cc] + "raw" + value + "step" + step + "→ slot" + slot + targetId + "(no pattern)").postln };
      ^this;
    };

    source = (Px.lastFormatted[targetId] !? { |p| p[param] }) ?? pattern[param] ?? row[\default];
    current = if (param == \amp) { this.prExtractScalar(source) } { source };

    if (current.isNumber.not)
    { current = row[\default] ?? 0 };

    next = (current + (step * row[\delta])).clip(row[\min], row[\max]);
    newValue = next;

    if (debug == true)
    { ("🎛️ cc" + row[\cc] + "raw" + value + "step" + step + "→ slot" + slot + targetId + param + current.round(0.001) + "→" + next.round(0.001)).postln };

    this.prPerform(targetId, param, newValue);
  }

  *prPerform { |id, param, value|
    var pattern = Px.last[id];

    if (pattern.isNil)
    { ^this };

    pattern[param] = value;

    if (pattern[\drumMachine].notNil)
    { Dx.new(pattern) }
    { Px.new(pattern) };

    Px.prAutoRefreshGui;
  }

  *prResolveTarget { |slot|
    var pinned = assignments[slot];
    var row;

    if (pinned.notNil)
    { ^pinned };

    row = config[\controls][slot - 1];

    if (row[\patternId].notNil)
    { ^row[\patternId] };

    ^Px.prSortedPatternIds[slot - 1];
  }

  *prSourceId { |device, port|
    var source;

    if (device.isNil)
    { ^nil };

    source = MIDIClient.sources.detect({ |endpoint|
      endpoint.device == device and: { port.isNil or: { endpoint.name == port } };
    });

    if (source.isNil) {
      ("🟡 Cx: source not found:" + device + port + "(listening to all sources)").warn;
      ^nil;
    };

    ^source.uid;
  }
}
