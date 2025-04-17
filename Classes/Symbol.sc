+ Symbol {
  doesNotUnderstand {}

  fadeTo { |b|
    ^Crossfader(this, b);
  }

  get { |key|
    Ndef(this).get(key);
  }

  i { |value|
    var number = this.asInteger;
    var id = number.createId(value);

    if (this.prHasDrumMachine and: (value == \all))
    { this.prStopDrumMachineInstruments }
    { Px.stop(id) };
  }

  in { |fadeTime|
    ^FadeIn(this, fadeTime);
  }

  loop { |value|
    Px.stop(this);
  }

  play { |value|
    if (value.isNil)
    { ^Ndef(this).play }
    { Px.stop(this) };
  }

  out { |fadeTime|
    ^FadeOut(this, fadeTime);
  }

  qset { |key, value|
    var clock = TempoClock.default;
    var nextBeat = clock.nextTimeOnGrid(4);

    clock.schedAbs(nextBeat, {
      Ndef(this).set(key, value);
    });
  }

  rebuild {
    Ndef(this).rebuild;
  }

  set { |key, value|
    Ndef(this).set(key, value);
  }

  stop { |fadeTime|
    Ndef(this).stop(fadeTime);
  }

  prHasDrumMachine {
    var drumMachines = [\606, \707, \808, \909];
    ^drumMachines.includes(this);
  }

  prStopDrumMachineInstruments {
    var patterns = Px.last.copy;

    patterns.do({ |pattern|
      if (pattern[\drumMachine] == this.asInteger)
      { Px.stop(pattern[\id]) };
    });
  }
}
