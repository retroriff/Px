/*
TODO: Bug when a holded note with same note is reevaluated
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

  *panic { |chan|
    var chans;

    if (chan.isNil)
    { chans = last.collect { |value| value[\chan] }.reject(_.isNil) }
    { chans = [chan] };

    if (chans.size == 0)
    { chans = (0..15) };

    if (chans.isArray) {
      ^chans do: { |ch|
        this.prChannelNoteOff(ch);
      }
    };

    if (chans.isEmpty.not) {
      chans keysValuesDo: { |key, chan|
        this.prChannelNoteOff(chan);
        last.removeAt(key);
        lastFormatted.removeAt(key);
        ndefList.removeAt(key);
        Ndef(key).free;
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

  *control { |chan, ctlNum, value|
    var suffix = ("_cc" ++ ctlNum).asString;

    if (midiClient.isNil)
    { this.initMidi };

    ndefList.keys do: { |key|
      if (key.asString.endsWith(suffix) and: { last[key].notNil and: { last[key][\chan] == chan } }) {
        Pdef(key).stop;
        Ndef(key).free;
        last.removeAt(key);
        ndefList.removeAt(key);
        Ndef(\px)[0] = { Mix.new(ndefList.values) };
      };
    };

    midiOut.control(chan, ctlNum, value.clip(0, 127));
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

    PxDebouncer.current = PxDebouncer(this, newPattern);
  }

  control { |value|
    var chan;
    var ctlNum = value[0];
    var control = value[1];
    var previousPattern = Px.last[this.asSymbol];

    if (previousPattern.isNil)
    { ^"Pattern % not found".format(this).warn; };

    chan = previousPattern[\chan] ?? 0;

    if (control.isNumber) {
      var controlId = (this.asString ++ "_cc" ++ ctlNum).asSymbol;

      if (Px.ndefList[controlId].notNil) {
        Pdef(controlId).stop;
        Ndef(controlId).free;
        this.last.removeAt(controlId);
        this.ndefList.removeAt(controlId);
        Ndef(\px)[0] = { Mix.new(Px.ndefList.values) };
      };

      Px.control(chan, ctlNum, control);
    } {
      var controlId = (this.asString ++ "_cc" ++ ctlNum).asSymbol;

      this.prPlayClass((
        id: controlId,
        chan: chan,
        midicmd: \control,
        ctlNum: ctlNum,
        control: control,
        dur: previousPattern[\dur] ?? 1,
      ));
    };
  }

  device { |value|
    this.prDebouncer.enqueue([\midiout, value]);
  }

  hold { |value|
    if (value == 1 or: (value == true))
    { this.prDebouncer.enqueue([\hasGate, false] ++ this.prSendSingleMessage) }
    { this.prDebouncer.enqueue([\midicmd, \noteOff]) };
  }

  note { |value|
    var pattern;

    if (value.isInteger)
    { value = [value] };

    if (value.isKindOf(Pattern))
    { pattern = value };

    this.prDebouncer.enqueue([\midinote, pattern ?? value]);
  }

  panic {
    this.prDebouncer.enqueue([\midicmd, \allNotesOff] ++ this.prSendSingleMessage);
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
