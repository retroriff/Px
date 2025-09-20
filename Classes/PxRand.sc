+ Px {
  *seed { |value|
    last do: { |pattern|
      pattern[\seed] = value;
    };

    this.prReevaluate;
  }

  *shuffle { |id|
    if (id.isNil) {
      seeds.order do: { |seedId|
        this.prCreateNewSeeds(seedId)
      };
      ^this.prReevaluate;
    };

    id = id.asSymbol;

    if (last.keys.includes(id)) {
      this.prCreateNewSeeds(id)
      ^this.prReevaluate([last[id]]);
    }
  }

  *prCreateNewSeeds { |id|
    var newSeed = (Date.getDate.rawSeconds % 1000).rand.asInteger;
    this.prPrint("🎲 Shuffle:".scatArgs(id, "->", newSeed));
    seeds[id] = newSeed;
  }

  *prGenerateRandNumber { |id|
    var seed = 1000.rand;
    this.prPrint("🎲 Seed:".scatArgs(id, "->", seed));
    ^seed;
  }

  *prGetNumericSeed { |seed|
    if (seed.isInteger.not)
    { seed = seed.ascii.join.asInteger };

    ^seed;
  }

  *prGetPatternSeed { |pattern|
    var id = pattern[\id].asSymbol;

    if (pattern[\seed].isNil) {
      var seed;

      if (seeds[id].isNil)
      { seed = this.prGenerateRandNumber(id) }
      { seed = seeds[id] };

      seeds.add(id -> seed);
      ^seeds[id];
    } {
      ^this.prGetNumericSeed(pattern[\seed]);
    };
  }
}
