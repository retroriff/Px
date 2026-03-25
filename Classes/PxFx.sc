+ Px {
  *prApplyFx { |id, fxList, isFullDeclaration|
    var currentFxNames;

    if (id.isNil) { ^this };

    Fx.skipFlush = true;

    if (fxList.isNil or: { fxList.size == 0 }) {

      if (isFullDeclaration and: { fxState[id].notNil }) {
        Fx(id);
        fxState[id].do { |fxName| Fx.perform(fxName, nil) };
        fxState[id] = nil;
      };

      Fx.skipFlush = false;
      ^this;
    };

    Fx(id);
    currentFxNames = fxList.collect { |entry| entry[0] }.asSet;

    if (isFullDeclaration and: { fxState[id].notNil }) {
      (fxState[id] -- currentFxNames).do { |fxName|
        Fx.prDisableFx(fxName, immediate: true);
      };
    };

    fxList.do { |entry|
      Fx.perform(entry[0], *entry[1]);
    };

    fxState[id] = currentFxNames;
    Fx.skipFlush = false;
  }
}

+ Number {
  blp { |mix|
    this.prFx(\blp, [mix]);
  }

  crush { |mix, bits|
    this.prFx(\crush, [mix, bits]);
  }

  delay { |mix, delaytime, delayfeedback|
    this.prFx(\delay, [mix, delaytime, delayfeedback]);
  }

  distort { |mix, drive|
    this.prFx(\distort, [mix, drive]);
  }

  duck { |mix, thresh|
    this.prFx(\duck, [mix, thresh]);
  }

  flanger { |mix|
    this.prFx(\flanger, [mix]);
  }

  gverb { |mix, roomsize, revtime|
    this.prFx(\gverb, [mix, roomsize, revtime]);
  }

  hpf { |mix, freq|
    this.prFx(\hpf, [mix, freq]);
  }

  lpf { |mix, freq|
    this.prFx(\lpf, [mix, freq]);
  }

  phaser { |mix, rate, depth|
    this.prFx(\phaser, [mix, rate, depth]);
  }

  reverb { |mix, room, size|
    this.prFx(\reverb, [mix, room, size]);
  }

  space { |mix, fb|
    this.prFx(\space, [mix, fb]);
  }

  tremolo { |mix, rate|
    this.prFx(\tremolo, [mix, rate]);
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
