/*
TODO: When we use TR08 we should set drum machines to 808. E.g. Routine 03 - Electro.scd
TODO: We should be able to play TR08 using numbers like 8008 i: \bd
*/

TR08 : Dx {
  classvar <drumKit;
  classvar <>latency;

  *initClass {
    latency = 0.195;
    drumKit = Dictionary[
      \bd -> 36,
      \sd -> 38,
      \lc -> 64,
      \lt -> 43,
      \mc -> 63,
      \mt -> 47,
      \hc -> 62,
      \ht -> 50,
      \cl -> 75,
      \rs -> 37,
      \ma -> 70,
      \cp -> 39,
      \cb -> 56,
      \cy -> 49,
      \oh -> 46,
      \hh -> 42,
    ];
  }

  *new { | newPattern|
    this.prInitializeMIDIDevice(newPattern);

    if (this.prIsTR08Detected.value == true)
    { newPattern = this.prAddTR08Pairs(newPattern) }
    { newPattern = this.prAddDrumMachinePlayBuf(newPattern) };

    ^super.new(newPattern);
  }

  *init { |argLatency, drumMachine|
    if (argLatency.notNil)
    { latency = argLatency };

    Px.initMidi(latency, deviceName: "TR-08");
  }

  *preset { |name, number, amp|
    drumMachine = \RolandTR808;
    ^super.preset(name, number, amp);
  }

  *play {
    ^super.preset(lastPreset[0], lastPreset[1]);
  }

  *release {
    ^\808.i(\all);
  }

  *stop {
    ^\808.i(\all);
  }

  *prAddTR08Pairs { |pattern|
    var midinote = drumKit[pattern[\instrument].asSymbol];
    pattern.putAll([\chan, 1]);
    pattern.putAll([\midinote, midinote]);
    pattern.putAll([\midiout, "TR-08"]);
    ^pattern;
  }

  *prInitializeMIDIDevice { |pattern|
    if (midiClient.isNil or: { midiClient["TR-08"].isNil })
    { this.init(drumMachine: pattern[\drumMachine]) };
  }

  *prIsTR08Detected {
    ^MIDIClient.destinations.detect({ |endpoint|
      endpoint.name == "TR-08"
    }) !== nil;
  }
}
