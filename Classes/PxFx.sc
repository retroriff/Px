+ Px {
  *prCreateFx { |pattern|
    if (pattern[\fx].notNil and: { pattern[\fx].size > 0 }) {
      pattern[\fx].do { |fx, i|
        if (SynthDescLib.global[fx[1]].notNil) {
          if (fx[1] == \reverb)
          { fx = fx ++ [\decayTime, pattern[\decayTime] ?? 7, \cleanupDelay, 1] };

          pattern[\fx][i] = fx;
          pattern = pattern ++ [\fxOrder, (1..pattern[\fx].size)];
        }
      };
    };

    ^pattern;
  }

  *prCreatePbindFx { |pattern|
    ^PbindFx(pattern.asPairs, *pattern[\fx]);
  }

  *prHasFX { |pattern|
    ^pattern[\fx].notNil;
  }
}

+ Number {
  delay { |mix|
    this.prFx(\delay, mix);
  }

  distort { |mix|
    this.prFx(\distort, mix);
  }

  hpf { |mix|
    this.prFx(\hpf, mix);
  }

  lpf { |mix|
    this.prFx(\lpf, mix);
  }

  reverb { |mix|
    this.prFx(\reverb, mix);
  }


  wah { |mix|
    this.prFx(\wah, mix);
  }

  prCreatePatternKey { |value|
    if (value == \rand)
    { ^Pwhite(0.0, 1) };

    if (value.isArray) {
      if (value[0] == \wrand) {
        var item1 = value[1].clip(-1, 1);
        var item2 = value[2].clip(-1, 1);
        var weight = value[3].clip(0, 1);
        ^Pwrand([item1, item2], [1 - weight, weight], inf);
      };
      if (value[0] == \rand) {
        ^Pwhite(value[1], value[2])
      };
    };

    if (value.isNumber)
    { ^value.clip(-1, 1) };

    ^value ?? 1;
  }

  prFx { |fx, mix|
    var debouncer = this.prDebouncer;
    var lastFx = [];
    var allFx;

    if (debouncer.pattern.notNil and: { debouncer.pattern[\fx].notNil })
    { lastFx = debouncer.pattern[\fx] };

    allFx = lastFx ++ [[\fx, fx, \mix, this.prCreatePatternKey(mix)]];

    if (debouncer.pattern.notNil)
    { debouncer.pattern[\fx] = allFx };

    debouncer.enqueue([\fx, allFx]);
  }
}

