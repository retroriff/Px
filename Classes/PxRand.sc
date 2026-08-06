+ Px {
  *shuffle { |id, history|
    if (history.notNil) {
      var key = history.asSymbol;
      var snapshot = shuffleHistory[key];

      if (snapshot.isNil)
      { ^("Shuffle history" + key + "not found") }
      {
        seeds = snapshot.copy;
        this.prPrint("Shuffle restored:" + key);
        if (this.prIsStopped.not) { this.prReevaluate };
      };

      ^this;
    };

    if (last.isEmpty) {
      ^"💩 Nothing to shuffle";
    };

    if (id.isNil) {
      seeds.order do: { |seedId|
        this.prCreateNewSeeds(seedId)
      };

      this.prSaveShuffleHistory;
      if (this.prIsStopped.not) { this.prReevaluate };
      ^this;
    };

    id = id.asSymbol;

    if (last.keys.includes(id)) {
      this.prCreateNewSeeds(id);
      this.prSaveShuffleHistory;
      if (this.prIsStopped.not) { this.prReevaluate([last[id]]) };
    }
  }

  *prIsStopped {
    if (last.isEmpty) { ^true };

    ^last.keys.every { |id| Pdef(id).source.isNil };
  }

  *prSaveShuffleHistory {
    var key = (shuffleHistory.size + 1).asSymbol;
    shuffleHistory[key] = seeds.copy;
    this.prPrint("Shuffle history:" + key);
  }

  *prCreateNewSeeds { |id|
    var newSeed = Date.seed.abs % 1000;
    this.prPrint("🎲 Shuffle" + ("\\" ++ id ++ ":") + newSeed);
    seeds[id] = newSeed;
  }

  *prGenerateRandNumber { |id|
    var seed = Date.seed.abs % 1000;
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
