+ Symbol {
  clear {
    if (this.prNdefExists)
    { Ndef(this).clear }
    { ^this.prNdefNotFound };
  }

  doesNotUnderstand {}

  edit {
    if (this.prNdefExists)
    { Ndef(this).edit }
    { ^this.prNdefNotFound };
  }

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

  grain { |value|
    Px.stop(this);
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
    var ndef = Ndef.dictFor(Server.default).at(this);

    ^ndef.notNil and: { ndef.source.notNil };
  }

  prNdefNotFound {
    ^("🟠 Ndef" + this + "doesn't exist");
  }

  prHasDrumMachine {
    ^Dx.prResolveAlias(this.asInteger) != this.asInteger;
  }

  prGetControls {
    var controls, fxNames, filtered;

    if (this.prNdefExists.not) { ^this.prNdefNotFound };

    controls = Ndef(this).controlNames;
    fxNames = Fx.effects.keys;

    if (controls.isNil) { ^"No controls" };

    filtered = controls.reject { |ctrl|
      var name = ctrl.name.asString;

      name.beginsWith("wet") or:
      { fxNames.any { |fx| name.beginsWith(fx.asString) } }
    };

    filtered = filtered.sort { |a, b| a.name < b.name };

    if (filtered.isEmpty) { ^"🟠 \\" ++ this + "has no controls" };

    ^"🎛️" + "\\" ++ this + "controls:" + filtered.collect { |ctrl|
      ctrl.name.asString + "=" + ctrl.defaultValue
    }.join(", ");
  }

  prStopDrumMachineInstruments {
    var patterns = Px.last.copy;

    patterns.do({ |pattern|
      if (pattern[\drumMachine] == Dx.prResolveAlias(this.asInteger))
      { Px.stop(pattern[\id]) };
    });
  }

  midinote {
    var str = this.asString;
    var idx, modifier, note, noteMap, octave;

    noteMap = IdentityDictionary[
      \c -> 0, \d -> 2, \e -> 4, \f -> 5,
      \g -> 7, \a -> 9, \b -> 11
    ];

    idx = 0;
    note = noteMap[str[idx].asSymbol];

    if (note.isNil)
    { ^nil };

    idx = idx + 1;
    modifier = 0;

    if (idx < str.size and: { str[idx] == $s })
    { modifier = 1; idx = idx + 1 };

    if (idx < str.size and: { str[idx] == $b })
    { modifier = -1; idx = idx + 1 };

    octave = str[idx..].asInteger;
    ^((octave + 2) * 12) + note + modifier;
  }
}
