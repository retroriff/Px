+ Px {
    *seed { |value|
        last do: { |pattern|
            pattern[\seed] = value;
        };

        this.prReevaluate;
    }

    *shuffle {
        this.prCreateNewSeeds;
        this.prReevaluate;
    }

    *prCreateNewSeeds {
        seeds.order do: { |id|
            var newSeed = (Date.getDate.rawSeconds % 1000).rand.asInteger;
            this.prPrint("🎲 Shuffle:".scatArgs(id, "->", newSeed));
            seeds[id] = newSeed;
        };
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
