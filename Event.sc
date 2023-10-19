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

    beatSeed {
        ^this.putAll([\beatSeed, true]);
    }

    delay { |mix = 1|
        this.fx(\delay, mix);
    }

    dur { |args|
        ^this.putAll([\dur, args]);
    }

    fade { |direction, time|
        var fade = if (time.isNil) { direction } { [direction, time.clip(0.1, time)] };
        ^this.putAll([\fade, fade]);
    }

    fx { |fx, mix|
        ^this.[\fxMethod] = this.[\fxMethod] ++ [[fx, mix.clip(0, 1)]];
    }

    in { |time|
        this.fade("in", time);
    }

    out { |time|
        this.fade("out", time);
    }

    rand { |folder|
        ^this.putAll([\buf, [folder, \rand]]);
    }

    reverb { |mix = 1|
        this.fx(\reverb, mix);
    }

    seed { |seed|
        ^this.putAll([\seed, seed]);
    }

    solo {
        ^this.putAll([\solo, true]);
    }

    wah { |mix = 1|
        this.fx(\wah, mix);
    }
}
