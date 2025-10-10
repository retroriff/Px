+ Px {
  *blp { |mix = 0.4|
    Fx(\px).blp(mix);
  }

  *delay { |mix, delaytime = 8, decaytime = 2|
    Fx(\px).delay(mix, delaytime, decaytime);
  }

  *flanger { |mix = 0.4|
    Fx(\px).flanger(mix);
  }

  *gverb { |mix = 0.4, roomsize = 200, revtime = 5|
    Fx(\px).gverb(mix, roomsize, revtime);
  }

  *hpf { |mix = 1, freq = 1200|
    Fx(\px).hpf(mix, freq);
  }

  *lpf { |mix, args|
    this.prFx(\lpf, mix, args);
  }

  *reverb { |mix = 0.3, room = 0.7, damp = 0.7|
    Fx(\px).reverb(mix, room, damp);
  }

  *space { |mix = 1, fb = 0.95|
    Fx(\px).space(mix, fb);
  }

  *vst { |mix = 1, plugin|
    Fx(\px).vst(mix, plugin);
  }

  *wah { |mix, args|
    this.prFx(\wah, mix, args);
  }

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

  *prFx { |fx, mix, args|
    last[lastName].do { |pattern|
      pattern.prFx(fx, mix, args);
    };

    this.prSend(last[lastName], lastName);
  }

  *prHasFX { |pattern|
    ^pattern[\fx].notNil;
  }
}

+ Number {
  delay { |mix|
    this.prFx(\delay, mix);
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
    var pairs = this.prCreatePatternFromArray(\wah, mix);
    this.prFx(\wah, pairs[1]);
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
    var id = Px.patternState[\id];
    var lastFx = Px.patternState[\fx] ?? [];
    var allFx = lastFx ++ [[\fx, fx, \mix, this.prCreatePatternKey(mix)]];
    this.prUpdatePattern([\fx, allFx]);
  }
}

