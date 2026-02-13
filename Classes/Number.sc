+ Number {
  a { |value|
    this.amp(value);
  }

  beat { |value|
    var pairs = Array.new;

    if (value.isNumber)
    { pairs = [\beat, true, \weight, value] };

    if (value.isArray)
    { pairs = [\beat, true, \beatSet, value] }
    { this.prRemoveBeatSetWhenSet };

    this.prDebouncer.enqueue(pairs);
  }

  doesNotUnderstand { |selector, args|
    var parentEventsKeys = Event.parentEvents.keys.collect { |key| Event.parentEvents[key].keys.asArray };
    var partialEventsKeys = Event.partialEvents.keys.collect { |key| Event.partialEvents[key].keys.asArray };
    var allEventKeys = parentEventsKeys ++ partialEventsKeys;
    var loopKeys = SynthDescLib.global[\loop].controlNames.asSet;
    var playbufKeys = SynthDescLib.global[\playbuf].controlNames.asSet;
    var synthDefControlNames = (loopKeys ++ playbufKeys);
    var customMethods = [\callback, \finish, \length, \name];
    var currentInstrument;

    if (PxDebouncer.current.notNil and: { PxDebouncer.current.pattern.notNil })
    { currentInstrument = PxDebouncer.current.pattern[\instrument] };

    if (currentInstrument.notNil and: { SynthDescLib.global[currentInstrument].notNil }) {
      synthDefControlNames = synthDefControlNames ++ SynthDescLib.global[currentInstrument].controlNames;
    };

    Px.last.do { |event|
      if (event[\instrument].notNil and: (event[\play].isNil) and: (event[\loop].isNil)) {
        var instrumentControlNames = SynthDescLib.global[event[\instrument]].controlNames;
        synthDefControlNames = synthDefControlNames ++ instrumentControlNames;
      };
    };

    allEventKeys = (allEventKeys ++ synthDefControlNames).asArray.flat ++ customMethods;

    if (allEventKeys.includes(selector))
    { this.prDebouncer.enqueue([selector, args]) }
    { ("🔴 Method not understood:" + selector).postln };
  }

  dur { |value|
    this.prDebouncer.enqueue([\dur, value]);
  }

  euclid { |value|
    var hits = value[0];
    var total = value[1];
    this.prDebouncer.enqueue([\euclid, [hits, total]]);
  }

  fade { |value|
    this.prFade(value.asSymbol);
  }

  fill { |value|
    var pairs = [\fill, true, \weight, value];
    this.prDebouncer.enqueue(pairs);
  }

  gui { |value|
    if (value != 0) {
      Px.gui
    } {
      if (Px.window.notNil) {
        Px.window.close
      }
    }
  }

  human { |delay|
    delay = delay ?? 0.1;
    this.prDebouncer.enqueue([\human, delay.clip(0, 1)]);
  }

  i { |value|
    this.prPlay(i: value.asSymbol);
  }

  in { |value|
    this.prFade(\in, value);
  }

  ins { |value|
    this.i(value);
  }

  off { |value|
    this.prDebouncer.enqueue([\timingOffset, value]);
  }

  loop { |value|
    this.prPlay(loop: value);
  }

  out { |value|
    if (value.class == Bus)
    { this.prDebouncer.enqueue([\out, value]) }
    { this.prFade(\out, value) };
  }

  play { |value|
    this.prPlay(play: value);
  }

  repeat { |value|
    this.prDebouncer.enqueue([\repeat, value]);
  }

  rest { |value|
    this.prDebouncer.enqueue([\rest, value]);
  }

  seed { |value|
    this.prDebouncer.enqueue([\seed, value]);
  }

  set { |setId|
    var id = this.asSymbol;

    if (this.prHasDrumMachine and: { setId != true }) {
      var pattern = Px.last.detect { |p|
        (p[\drumMachine] == this) and: { p[\instrument].asSymbol == setId }
      };

      if (pattern.notNil)
      { id = pattern[\id] };
    };

    PxDebouncer.current = PxDebouncer(this, Px.last[id]);
  }

  solo { |value|
    var isSolo = value != 0;
    this.prDebouncer.enqueue([\solo, isSolo]);
  }

  weight { |value|
    this.prDebouncer.enqueue([\weight, value.clip(0, 1)]);
  }

  // Functions
  createId { |ins|
    var instrumentWithoutSufix = this.prRemoveSufix(ins);

    if (this.prShouldGenerateDrumMachineId(instrumentWithoutSufix)) {
      ^this.prGenerateDrumMachineId(instrumentWithoutSufix);
    }

    ^this.asSymbol;
  }

  prCreateArrayFromSample { |sample|
    if (sample.isString) {
      var parts = sample.asString.split($:);

      if (parts.size > 1) {
        var file = parts[1];

        case
        { file.asSymbol == \jump }
        { file = \jump }

        { file.asSymbol == \rand }
        { file = \rand }

        { file = file.asInteger };

        ^[parts[0], file];
      }

      ^[parts[0], 0];
    };

    ^sample;
  }

  prShouldGenerateDrumMachineId { |ins|
    ^this.prHasDrumMachine and: (ins.notNil);
  }

  prGenerateDrumMachineId { |ins|
    var drumMachineId = (ins.asString ++ this.asString).asSymbol;

    var findExistingPatternForIns = Px.last.detect({ |pattern|
      pattern[\drumMachine] == this and: (pattern[\instrument] == ins);
    });

    if (findExistingPatternForIns.isNil)
    { ^drumMachineId }
    { ^findExistingPatternForIns[\id] };
  }

  prGenerateDrumMachineIntegerId { |drumMachineNumber, patternId|
    var existingPattern = Px.last.detect({ |pattern|
      pattern[\drumMachine] == drumMachineNumber and: (pattern[\id] == patternId)
    });

    var existingIds = Px.last
      .select({ |pattern| pattern[\drumMachine] == drumMachineNumber })
      .collect({ |pattern| pattern[\drumMachineIntegerId] })
      .reject(_.isNil);

    if (existingPattern.notNil and: { existingPattern[\drumMachineIntegerId].notNil })
    { ^existingPattern[\drumMachineIntegerId] };

    if (existingIds.isEmpty)
    { ^(drumMachineNumber * 100) + 1 }
    { ^existingIds.maxItem + 1 };
  }

  prExtractSufix { |value|
    var parts = value.asString.split($:);

    if (parts.size > 1) {
      ^parts[1].asInteger;
    }

    ^nil;
  }

  prHasDrumMachine {
    var drumMachines = [505, 606, 626, 707, 727, 808, 909];
    var isDxPreset = Px.last.any { |pattern|
      pattern[\drumMachine].notNil and: (pattern[\id] == this)
    };

    ^drumMachines.includes(this) or: (isDxPreset == true);
  }

  prFade { |direction, time|
    var fade;

    if (time.isNil)
    { fade = direction }
    { fade = [direction, time.clip(0.1, time)] };

    this.prDebouncer.enqueue([\fade, fade]);
  }

  prPlay { |i, play, loop|
    var instrumentWithoutSufix = this.prRemoveSufix(i);
    var oldPending;

    var newPattern = (
      id: this.createId(i),
      instrument: instrumentWithoutSufix,
      loop: this.prCreateArrayFromSample(loop),
      play: this.prCreateArrayFromSample(play),
    );

    if (i.asString != instrumentWithoutSufix.asString and: { this.prExtractSufix(i).notNil})
    { newPattern.putAll([\file, this.prExtractSufix(i)]) };

    if (PxDebouncer.current.notNil and: { PxDebouncer.current.pattern.isNil })
    { oldPending = PxDebouncer.current.prTakePending };

    PxDebouncer.current = PxDebouncer(this, newPattern);

    if (oldPending.notNil)
    { oldPending.do { |p| PxDebouncer.current.enqueue(p) } };
  }

  prPlayClass { |newPattern|
    var drumMachinePattern, drumMachineIntegerId;

    if (newPattern[\drumMachine].notNil)
    { ^Dx(newPattern) };

    drumMachinePattern = Px.last.detect { |pattern|
      pattern[\id] == this.asSymbol and: (pattern[\drumMachine].notNil)
    };

    if (drumMachinePattern.notNil) {
      drumMachineIntegerId = this.prGenerateDrumMachineIntegerId(drumMachinePattern[\drumMachine], newPattern[\id]);

      newPattern.putAll([
        \drumMachine, drumMachinePattern[\drumMachine],
        \drumMachineIntegerId, drumMachineIntegerId
      ]);

      ^Dx(newPattern);
    };

    if (this.prHasDrumMachine) {
      drumMachineIntegerId = this.prGenerateDrumMachineIntegerId(this, newPattern[\id]);

      newPattern.putAll([
        \drumMachine, this,
        \drumMachineIntegerId, drumMachineIntegerId
      ]);

      ^Dx(newPattern);
    }

    ^Px(newPattern);
  }

  prRemoveBeatSetWhenSet {
    var id;

    if (PxDebouncer.current.notNil and: { PxDebouncer.current.pattern.notNil })
    { id = PxDebouncer.current.pattern[\id] };

    if (id.notNil and: { Px.last[id].notNil })
    { Px.last[id].removeAt(\beatSet) };
  }

  prRemoveSufix { |name|
    var parts = name.asString.split($:);

    if (parts.size > 1) {
      ^parts[0];
    }

    ^name;
  }

  prUpdatePattern { |pairs, pattern|
    pattern = pattern ?? Px.patternState;

    if (pattern.notNil)
    { ^this.prPlayClass(pattern.putAll(pairs)) };
  }

  prDebouncer {
    if (PxDebouncer.current.isNil)
    { PxDebouncer.current = PxDebouncer(this) };

    ^PxDebouncer.current;
  }
}
