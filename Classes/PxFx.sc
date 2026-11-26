+ Px {
  *prApplyFx { |id, fxList, isFullDeclaration, previousFxNames|
    var currentFxNames, previousSelectorNames, selector, selectorNames;

    if (id.isNil) { ^this };

    Fx.skipFlush = true;
    previousFxNames = previousFxNames ?? { Fx.prFxNames(id) };
    previousSelectorNames = Fx.prSelectorNames(id);
    selector = lastFormatted[id] !? { |pattern| pattern[\fxSelect] };
    selectorNames = (Fx.prSelectorOptions(selector) ?? []).reject { |name| name == Nil }.asSet;

    if (isFullDeclaration) {
      Fx.prClearMixStreams(id);
      Fx.prClearSelector(id);
    };

    if (selectorNames.isEmpty)
    { Fx.prClearSelector(id) };

    if (isFullDeclaration.not and: { previousSelectorNames.notEmpty }) {
      Fx.prTargetPattern(id);
      previousSelectorNames.difference(selectorNames).difference(fxList.collect { |entry| entry[0] }.asSet).do { |fxName|
        Fx.prDisableFx(fxName, immediate: true);
      };
    };

    if (fxList.isNil or: { fxList.size == 0 }) {

      if (isFullDeclaration and: { previousFxNames.notEmpty }) {
        Fx.prTargetPattern(id);
        Fx.prSuppressPrint = true;
        previousFxNames.do { |fxName| Fx.perform(fxName, nil) };
        Fx.prSuppressPrint = false;
      };

      Fx.skipFlush = false;
      ^this;
    };

    Fx.prTargetPattern(id);
    currentFxNames = fxList.collect { |entry| entry[0] }.asSet.union(selectorNames);

    if (isFullDeclaration and: { previousFxNames.notEmpty }) {
      previousFxNames.difference(currentFxNames).do { |fxName|
        Fx.prDisableFx(fxName, immediate: true);
      };
    };

    Fx.prSuppressPrint = true;

    if (selectorNames.notEmpty)
    { Fx.prRegisterSelector(id, selector, selectorNames) };

    fxList.do { |entry|
      if (entry[2] == \selector) {
        Fx.prPrepareSelector(id, entry[0]);
      } {
        Fx.perform(entry[0], *this.prResolveFxMix(id, entry));
      };
    };
    Fx.prSuppressPrint = false;

    Fx.skipFlush = false;
  }

  *prResolveFxMix { |id, entry|
    var fx = entry[0];
    var args = entry[1];
    var mix = args[0];

    if (mix.isKindOf(Pattern).not) {
      Fx.prClearMixStreams(id, [fx]);
      ^args;
    };

    Fx.prRegisterMixStream(id, fx, mix);
    args = args.copy;
    args[0] = 0;

    ^args;
  }
}

+ Number {
  blp { |mix|
    this.prFx(\blp, [mix]);
  }

  compressor { |mix, thresh, ratio, gain|
    this.prFx(\compressor, [mix, thresh, ratio, gain]);
  }

  crush { |mix, bits, rate|
    this.prFx(\crush, [mix, bits, rate]);
  }

  delay { |mix, delaytime, delayfeedback|
    this.prFx(\delay, [mix, delaytime, delayfeedback]);
  }

  dist { |mix, drive|
    this.prFx(\dist, [mix, drive]);
  }

  duck { |mix, thresh, src|
    this.prFx(\duck, [mix, thresh, src]);
  }

  flanger { |mix|
    this.prFx(\flanger, [mix]);
  }

  freqShift { |mix, freq, phase|
    this.prFx(\freqShift, [mix, freq, phase]);
  }

  gverb { |mix, roomsize, revtime|
    this.prFx(\gverb, [mix, roomsize, revtime]);
  }

  hpf { |mix, freq, gain|
    this.prFx(\hpf, [mix, freq, gain]);
  }

  lpf { |mix, freq, gain|
    this.prFx(\lpf, [mix, freq, gain]);
  }

  phaser { |mix, rate, depth|
    this.prFx(\phaser, [mix, rate, depth]);
  }

  reverb { |mix, room, size|
    this.prFx(\reverb, [mix, room, size]);
  }

  reverse { |mix, beats|
    if (mix.isArray) {
      beats = mix[1];
      mix = mix[0];
    };

    this.prFx(\reverse, [mix, beats]);
  }

  rhpf { |mix, freq, rq|
    this.prFx(\rhpf, [mix, freq, rq]);
  }

  rlpf { |mix, freq, rq|
    this.prFx(\rlpf, [mix, freq, rq]);
  }

  space { |mix, fb|
    this.prFx(\space, [mix, fb]);
  }

  tremolo { |mix, rate|
    this.prFx(\tremolo, [mix, rate]);
  }

  vibrato { |mix, rate, depth|
    this.prFx(\vibrato, [mix, rate, depth]);
  }

  vst { |mix, plugin|
    this.prFx(\vst, [mix, plugin]);
  }

  wah { |mix, rate, depth|
    this.prFx(\wah, [mix, rate, depth]);
  }

  prFx { |fx, args|
    var debouncer = this.prDebouncer;
    var id = debouncer.pattern !? { |pattern| pattern[\id] };

    if (args[0].isKindOf(Pattern) and: { id.notNil })
    { debouncer.enqueue([\fxMix, Pfunc({ Fx.prAdvanceMixStreams(id); 0 })]) };

    args = args.reject { |v| v.isNil };
    debouncer.fxList.add([fx, args]);
    debouncer.prSchedule;
  }
}
