/*
TODO: Channel + 1 to match receiver number. Ex. chan: 1 instead of chan: 0
TODO: Controls [\rand, 0, 0.1] i [\wrand, 0, 0.1, 0.9]
TODO: MIDIOut instances
*/

+ Px {
  *initMidi { | latency, deviceName, portName |
    // Ableton Live > Preferences > Audio > Output Latency
    var abletonLiveOutputLatency = 26.3;
    var abletonLiveLatencyMs = Server.default.latency - (abletonLiveOutputLatency / 1000);

    MIDIClient.init(verbose: false);

    if (deviceName.notNil and: (this.prDetectDevice(deviceName) == false)) {
      this.prPrint("🔴 Device not detected");
      ^this.prPrint("✅ Playing SynthDefs");
    };

    if (deviceName.notNil) {
      if (portName.isNil)
      { portName = deviceName };

      midiOut = MIDIOut.newByName(deviceName, portName);
      this.prPrint("🎛️ MIDIOut:".scatArgs(deviceName));
    } {
      midiOut = MIDIOut.new(0);
      this.prPrint("🎛️ MIDIOut:".scatArgs("port 0"));
    };

    deviceName = deviceName ?? "default";

    if (midiClient.isNil) {
      midiClient = Dictionary[deviceName -> midiOut]
    } {
      midiClient.add(deviceName -> midiOut);
    };

    CmdPeriod.add { this.panic };
    midiClient[deviceName].latency = latency ?? abletonLiveLatencyMs;
  }

  *panic {
    var chans = last.collect { |value| value[\chan] }.reject(_.isNil);

    if (chans.isEmpty.not) {
      chans do: { |chan|
        this.prChannelNoteOff(chan);
      };
    }
  }

  *prChannelNoteOff { |chan|
    (0..127) do: { |note|
      Px.midiOut.noteOff(chan, note);
    };
  }

  *prCreateMidi { |pattern|
    var midiout, isMidiControl, addMidiTypes, isMidi;

    isMidi = if (pattern[\chan].notNil) { true };

    if (isMidi != true)
    { ^pattern };

    midiout = pattern[\midiout] ?? "default";

    if (pattern[\hasGate] == false
      or: { pattern[\midicmd] == \noteOff }
      or: { pattern[\midicmd] == \control })
    { isMidiControl = true };
    
    if (pattern[\hasGate] == false) {
      var hasSameNotes = { |holdedPattern|
        [\degreeRaw, \midinote, \octave, \root, \scale] every: { |key|
            holdedPattern[key] == pattern[key]
        }
      };

      var stopHoldedNotes = {
        if (midiHoldedNotes[pattern[\id]].notNil) {
          var holdedPattern = midiHoldedNotes[pattern[\id]];

          if (hasSameNotes.(holdedPattern) == false) {
            holdedPattern.putAll([\dur, Pseq([1], 1), \midicmd, \noteOff]);
            Pbind(*holdedPattern.asPairs).play(quant: 4);
          };
        };
      };

      stopHoldedNotes.();
      midiHoldedNotes[pattern[\id]] = pattern;
    };

    addMidiTypes = {
      if (midiClient.isNil)
      { this.initMidi };

      pattern.putAll([
        \type: \midi,
        \midicmd: pattern[\midicmd] ?? \noteOn,
        \midiout: midiClient[midiout],
        \chan, pattern[\chan] ?? 0,
        \instrument: \midi,
      ]);
    };


    if (isMidi == true)
    { pattern = addMidiTypes.value };

    if (isMidiControl == true)
    { pattern = pattern ++ (\midiControl: 1) };

    ^pattern;
  }

  *prDetectDevice { |name|
    ^MIDIClient.destinations.detect({ |endpoint|
      endpoint.name == name;
    }) !== nil;
  }
}

+ Number {
  chan { |value, midiControlEvent|
    var id = this.asSymbol;
    var newPattern = (
      chan: value,
      id: id,
    );

    if (midiControlEvent.notNil)
    { newPattern = newPattern.putPairs(midiControlEvent) };

    this.prPlayClass(newPattern);
  }

  control { |value|
    var ctlNum = value[0];
    var control = value[1];

    var controlEvent = (
      \midicmd: \control,
      \ctlNum: ctlNum,
      \control: this.prCreateControl(control)
    ).asPairs;

    var previousPattern = Px.last[(this  - 1).asSymbol];

    if (ctlNum.isInteger)
    { controlEvent = controlEvent ++ this.prSendSingleMessage };

    ^this.chan(previousPattern[\chan], controlEvent);
  }

  device { |value|
    this.prUpdatePattern([\midiout, value]);
  }

  hold { |value|
    if (value == 1)
    { this.prUpdatePattern([\hasGate, false] ++ this.prSendSingleMessage) }
    { this.prUpdatePattern([\midicmd, \noteOff]) };
  }

  note { |value|
    this.prUpdatePattern([\midinote, value]);
  }

  panic {
    this.prUpdatePattern([\midicmd, \allNotesOff] ++ this.prSendSingleMessage);
  }

  prConvertToMidiValue { |value|
    ^value.clip(0, 1) * 127 / 1;
  }

  prCreateControl { |value|
    var createPwhite = { |lower, upper|
      Pwhite(this.prConvertToMidiValue(lower), this.prConvertToMidiValue(upper));
    };

    var createPwrand = { |item1, item2, weight|
      Pwrand(
        list: [this.prConvertToMidiValue(item1), this.prConvertToMidiValue(item2)],
        weights: [1 - weight, weight],
        repeats: inf
      );
    };

    case
    { value == \rand }
    { ^createPwhite.(0, 1) }

    { value.isArray and: { value[0] == \rand } }
    { ^createPwhite.(value[1], value[2]) }

    { value.isArray and: { value[0] == \wrand } }
    { ^createPwrand.(value[1], value[2], value[3].clip(0, 1)) }

    { value.isNumber }
    { ^this.prConvertToMidiValue(value) };

    ^value ?? 0;
  }

  prSendSingleMessage {
    ^(\dur: Pseq([1], 1)).asPairs;
  }
}

+ Symbol {
  chan {
    Px.stop(this.asSymbol);
  }

  control {}
  hold {}
  note {}
  midiout {}
  panic {}
}
