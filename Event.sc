+Event {
    beat { |seed|
        var pairs = [\beat, true];
        if (seed.notNil and: seed.isInteger)
        { pairs = pairs ++ [\seed, seed] };
        ^this.putAll(pairs);
    }

    delay { |pairs|
        this.fx(\delay, pairs);
    }

    fx { |fx, pairs|
        ^this.[\fxMethod] = this.[\fxMethod] ++ [fx];
    }

    in {
        ^this.putAll([\fade, "in"]);
    }

    out  {
        ^this.putAll([\fade, "out"]);
    }

    reverb { |pairs|
        this.fx(\reverb, pairs);
    }

    seed {
        ^this.putAll([\showSeed, true]);
    }

    solo {
        ^this.putAll([\solo, true]);
    }

    wah { |pairs|
        this.fx(\wah, pairs);
    }
}
