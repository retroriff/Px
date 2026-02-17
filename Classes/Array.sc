+ Array {
  pseq { |dur|
    ^Pseq(this, dur ?? inf);
  }

  pseg { |curve = \lin, beats = 8, repeats|
    var curvesDict = Dictionary[
      \exp -> \exponential,
      \lin -> \linear
    ];
    var levels, numSegs, segDur;

    levels = if (curve == \exp)
    { this.collect { |v| if (v == 0) { 0.01 } { v } } }
    { this };

    numSegs = levels.size - 1;
    segDur = beats / numSegs;

    if (repeats.notNil) {
      if (repeats == \inf) { repeats = inf };
      ^Pseg(levels, segDur, curvesDict[curve], repeats);
    };

    ^Pseg(
      levels ++ [levels.last],
      Array.fill(numSegs, segDur) ++ [inf],
      curvesDict[curve]
    );
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

