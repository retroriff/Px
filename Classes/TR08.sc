/*
TODO: We should be able to play TR08 using numbers like 8808 i: \bd
*/

TR08 : Dx {
    classvar <drumKit;

    *initClass {
        drumKit = Dictionary[
            \bd -> 36,
            \sn -> 38,
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
            \ch -> 42,
        ];
    }

    *new { | newPattern|
        this.prInitializeMIDIDevice(newPattern);

        if (this.prIsTR08Detected.value == true)
        { newPattern = this.prAddTR08Pairs(newPattern) }
        { newPattern = this.prAddDrumMachinePlayBuf(newPattern) };

        ^super.new(newPattern);
    }

    *init { |latency, drumMachine|
        if (drumMachine == 808)
        { Px.initMidi(latency, deviceName: "TR-08") };
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
        var midinote = drumKit[pattern[\i].asSymbol];
        pattern.putAll([\chan, 0]);
        pattern.putAll([\midinote, midinote]);
        pattern.putAll([\midiout, "TR-08"]);
        ^pattern;
    }

    *prInitializeMIDIDevice { |pattern|
        if (MIDIClient.initialized == false or: { midiClient.notNil and: { midiClient["TR-08"].isNil }})
        { this.init(0.195, pattern[\drumMachine]) };
    }

    *prIsTR08Detected {
        ^MIDIClient.destinations.detect({ |endpoint|
            endpoint.name == "TR-08"
        }) !== nil;
    }
}
