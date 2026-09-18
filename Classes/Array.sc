+ Array {
  pexprand { |repeats|
    ^Pexprand(this[0], this[1], repeats ?? inf);
  }

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

    if (repeats == inf or: { repeats == \inf }) {
      ^Pseg(levels, segDur, curvesDict[curve], inf);
    };

    ^Pseg(
      levels ++ [levels.last],
      Array.fill(numSegs, segDur) ++ [inf],
      curvesDict[curve]
    );
  }

  prand { |repeats|
    ^Prand(this, repeats ?? inf);
  }

  pxrand { |repeats|
    ^Pxrand(this, repeats ?? inf);
  }

  pwhite { |repeats|
    if (this.size != 2) {
      Error("pwhite requires an Array of exactly 2 elements [lo, hi]").throw;
    };

    ^Pwhite(this[0], this[1], repeats ?? inf);
  }

  pwrand { |weights, repeats|
    if (weights.isNumber) {
        if (this.size != 2) {
            Error("pwrand number weights require an Array of 2 items").throw;
        };

        if (weights < 0 or: { weights > 1 }) {
            Error("pwrand number must be between 0 and 1").throw;
        };

        weights = [1 - weights, weights];
    } {
        if (weights.isArray.not) {
            Error("pwrand requires an Array of weights").throw;
        };
    };

    ^Pwrand(this, weights.normalizeSum, repeats ?? inf);
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

