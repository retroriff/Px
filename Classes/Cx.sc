Cx {
  classvar <>assignments;
  classvar <>config;
  classvar <>configName;
  classvar <>debug;
  classvar <>midiFuncs;
  classvar <tempoPending;
  classvar <tempoToken;

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
    { ^"🔴 Cx: no config loaded" };

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
      if (row[\target].notNil)
      { row[\target] = row[\target].asSymbol };
      row;
    };

    if (config[\pads].notNil) {
      config[\pads] = config[\pads] collect: { |row|
        row[\action] = row[\action].asSymbol;
        row[\num] = (row[\num] ?? row[\cc]).asInteger;
        row[\type] = (row[\type] ?? \cc).asSymbol;
        row;
      };
    };

    ^("✅ Cx: loaded" + name);
  }

  *play { |name|
    var srcID;

    if (name.notNil) { this.load(name) };

    if (config.isNil) { ^"⚠️ Cx: no config loaded — pass a name (e.g. Cx.play(\\minilab3))" };

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

    (config[\pads] ? []) do: { |row|
      if (row[\type] == \note) {
        midiFuncs = midiFuncs.add(MIDIFunc.noteOn({
          this.prHandlePad(row);
        }, row[\num], srcID: srcID));
      } {
        midiFuncs = midiFuncs.add(MIDIFunc.cc({ |value|
          if (value > 0)
          { this.prHandlePad(row) };
        }, row[\num], srcID: srcID));
      };
    };

    CmdPeriod.add { this.play };
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
    var targetId, param, pattern, source, current, next, newValue;

    if (row[\target] == \global)
    { ^this.prHandleGlobal(row, step, value) };

    targetId = this.prResolveTarget(slot);
    param = row[\param];

    if (targetId.isNil) {
      if (debug == true)
      { ("🎛️ no target:" + step + "(cc" + row[\cc] ++ ", raw" + value ++ ")").warn };
      ^this;
    };

    pattern = Px.last[targetId];
    if (pattern.isNil) {
      if (debug == true)
      { ("🎛️ \\" ++ targetId + "(no pattern):" + step + "(cc" + row[\cc] ++ ", raw" + value ++ ")").warn };
      ^this;
    };

    source = (Px.lastFormatted[targetId] !? { |p| p[param] }) ?? pattern[param] ?? row[\default];
    current = if (param == \amp) { this.prExtractScalar(source) } { source };

    if (current.isNumber.not)
    { current = row[\default] ?? 0 };

    next = (current + (step * row[\delta])).clip(row[\min], row[\max]);
    newValue = next;

    if (debug == true)
    { ("🎛️ \\" ++ targetId ++ ":"  + param + current.round(0.001) + "→" + next.round(0.001) + "(cc" + row[\cc] ++ ", raw" + value ++ ")").postln };

    this.prPerform(targetId, param, newValue);
  }

  *prHandleGlobal { |row, step, value|
    var param = row[\param];
    var delta = row[\delta] ? 1;
    var amount = step * delta;

    param.switch(
      \tempo, { this.prDebounceTempo(amount, row, value) },
      { ("🟡 Cx: unknown global param:" + param).warn }
    );
  }

  *prDebounceTempo { |amount, row, value|
    var myToken;
    var debounceTime = 0.25;

    tempoPending = (tempoPending ? 0) + amount;
    tempoToken = (tempoToken ? 0) + 1;
    myToken = tempoToken;

    AppClock.sched(debounceTime, {
      var total;

      if (myToken == tempoToken) {
        total = tempoPending;
        tempoPending = 0;
        Px.tempo(add: total);

        if (debug == true)
        { ("🕰️ Tempo" + (TempoClock.default.tempo * 60).round(1) + "(cc" + row[\cc] ++ ", raw" + value ++ ")").postln };
      };

      nil;
    });
  }

  *prHandlePad { |row|
    var action = row[\action];

    if (debug == true)
    { ("🎛️ pad" + row[\type] + row[\num] + "→ Px." ++ action).postln };

    { Px.perform(action) }.defer;
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
