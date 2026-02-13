+ Array {
  pseq { |dur|
    ^Pseq(this, dur ?? inf);
  }

  pseg { |curve = \lin, beats = 8, repeats|
    var curvesDict = Dictionary[
      \exp -> \exponential,
      \lin -> \linear
    ];
    var durs, levels;

    levels = if (curve == \exp)
    { this.collect { |v| if (v == 0) { 0.01 } { v } } }
    { this };

    if (repeats.notNil) {
      durs = [beats, repeats];
    } {
      levels = levels ++ [levels.last];
      durs = [beats, inf];
    };

    ^Pseg(levels, durs, curvesDict[curve]);
  }

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

