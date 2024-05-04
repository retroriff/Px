+Array {
    shuffle { |seed|
        if (seed.isNil)
        { thisThread.randSeed = this.prGenerateRandNumber }
        { thisThread.randSeed = seed };

        ^this.scramble;
    }

    prGenerateRandNumber {
        var seed = 1000.rand;
        ("🎲 Seed".scatArgs("->", seed)).postln;
        ^seed;
    }
}

