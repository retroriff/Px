+Event {
    a { |args|
        this.amp(args);
    }

    amp { |args|
        ^this.putAll([\amp, args]);
    }

    beat { |seed|
        var pairs = [\beat, true];
        if (seed.notNil and: seed.isInteger)
        { pairs = pairs ++ [\seed, seed] };
        ^this.putAll(pairs);
    }

    delay { |mix|
        this.fx(\delay, mix);
    }

    dur { |args|
        ^this.putAll([\dur, args]);
    }

    fx { |fx, mix|
        ^this.[\fxMethod] = this.[\fxMethod] ++ [[fx, mix.clip(0, 1)]];
    }

    in {
        ^this.putAll([\fade, "in"]);
    }

    out  {
        ^this.putAll([\fade, "out"]);
    }

    rand { |folder|
        ^this.putAll([\buf, [folder, \rand]]);
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
