+ Px {
  *prApplyFx { |id, fxList, isFullDeclaration|
    var chain, previousFxNames, currentFxNames;

    if (id.isNil) { ^this };

    Fx.skipFlush = true;
    chain = Fx.chains[id];
    previousFxNames = if (chain.notNil) { chain.fxNames.asSet } { Set.new };

    if (fxList.isNil or: { fxList.size == 0 }) {

      if (isFullDeclaration and: { previousFxNames.notEmpty }) {
        Fx(id);
        Fx.prSuppressPrint = true;
        previousFxNames.do { |fxName| Fx.perform(fxName, nil) };
        Fx.prSuppressPrint = false;
      };

      Fx.skipFlush = false;
      ^this;
    };

    Fx(id);
    currentFxNames = fxList.collect { |entry| entry[0] }.asSet;

    if (isFullDeclaration and: { previousFxNames.notEmpty }) {
      (previousFxNames -- currentFxNames).do { |fxName|
        Fx.prDisableFx(fxName, immediate: true);
      };
    };

    Fx.prSuppressPrint = true;
    fxList.do { |entry|
      Fx.perform(entry[0], *entry[1]);
    };
    Fx.prSuppressPrint = false;

    Fx.skipFlush = false;
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

  reverse { |mix|
    this.prFx(\reverse, [mix]);
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
    args = args.reject { |v| v.isNil };
    debouncer.fxList.add([fx, args]);
    debouncer.prSchedule;
  }
}
