+Event {
    beat { |seed|
        var pairs = [\beat, true];
        if (seed.notNil and: seed.isInteger)
        { pairs = pairs ++ [\seed, seed] };
        ^this.putAll(pairs);
    }

    delay { |mix|
        this.fx(\delay, mix);
    }

    fx { |fx, mix|
        ^this.[\fxMethod] = this.[\fxMethod] ++ [[fx, mix]];
    }

    in {
        ^this.putAll([\fade, "in"]);
    }

    out  {
        ^this.putAll([\fade, "out"]);
    }

    reverb { |mix|
        this.fx(\reverb, mix);
    }

    seed {
        ^this.putAll([\showSeed, true]);
    }

    solo {
        ^this.putAll([\solo, true]);
    }

    wah { |mix|
        this.fx(\wah, mix);
    }
}
