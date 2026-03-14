+ Symbol {
  clear {
    if (this.prNdefExists)
    { Ndef(this).clear }
    { ^this.prNdefNotFound };
  }

  doesNotUnderstand {}

  free {
    if (~isAnimatronEnabled == true)
    { ~animatronNetAddr.sendMsg("/sc/stop", this, 0) };

    if (this.prNdefExists)
    { Ndef(this).free }
    { ^this.prNdefNotFound };
  }

  get { |key|
    if (this.prNdefExists) {
      if (key.notNil)
      { ^Ndef(this).get(key) }
      { ^this.prGetControls };
    }
    { ^this.prNdefNotFound };
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

  i { |value|
    var number = this.asInteger;
    var id = number.createId(value);

    if (this.prHasDrumMachine and: (value == \all))
    { this.prStopDrumMachineInstruments }
    { Px.stop(id) };
  }

  in { |fadeTime|
    FadeIn(this, fadeTime);
  }

  loop { |value|
    Px.stop(this);
  }

  play { |value|
    if (value.isNil) {
      if (~isAnimatronEnabled == true)
      { ~animatronNetAddr.sendMsg("/sc/start", this, 0) };

      ^Ndef(this).play(fadeTime: 0);
    } {
      Px.stop(this);
    };
  }

  out { |fadeTime|
    FadeOut(this, fadeTime);
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
    if (this.prNdefExists)
    { Ndef(this).set(key, value) }
    { ^this.prNdefNotFound };
  }

  stop { |fadeTime|
    var isNdef = this.prNdefExists;
    var isTdef = Tdef.all.at(this).notNil;

    if (~isAnimatronEnabled == true)
    { ~animatronNetAddr.sendMsg("/sc/stop", this, fadeTime ?? 0) };

    if (isNdef)
    { ^Ndef(this).stop(fadeTime ?? 0) };

    if (isTdef)
    { ^Tdef(this).stop };

    ^this.prNdefNotFound;
  }

  to { |b|
    Crossfader(this, b);
  }

  xset { |key, value|
    if (this.prNdefExists)
    { Ndef(this).xset(key, value) }
    { ^this.prNdefNotFound };
  }
 
  prNdefExists {
    ^Ndef.dictFor(Server.default).at(this).notNil;
  }

  prNdefNotFound {
    ^("🟠 Ndef" + this + "doesn't exist");
  }

  prHasDrumMachine {
    var drumMachines = [505, 606, 626, 707, 727, 808, 909];
    ^drumMachines.includes(this.asInteger);
  }

  prGetControls {
    var controls = Ndef(this).controlNames;
    var fxNames = Fx.effects.keys;
    var filtered;

    if (controls.isNil) { ^"No controls" };

    filtered = controls.reject { |ctrl|
      var name = ctrl.name.asString;

      name.beginsWith("wet") or:
      { fxNames.any { |fx| name.beginsWith(fx.asString) } }
    };

    filtered = filtered.sort { |a, b| a.name < b.name };

    ^"Controls:" + filtered.collect { |ctrl|
      ctrl.name.asString + ctrl.defaultValue
    }.join(", ");
  }

  prStopDrumMachineInstruments {
    var patterns = Px.last.copy;

    patterns.do({ |pattern|
      if (pattern[\drumMachine] == this.asInteger)
      { Px.stop(pattern[\id]) };
    });
  }
}
