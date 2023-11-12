+Event {
    prCreatePattern { |value|
        if (value == \rand)
        { ^Pwhite(0.0, 1) };

        if (value.isArray) {
            if (value[0] == \wrand) {
                var item1 = value[1].clip(-1, 1);
                var item2 = value[2].clip(-1, 1);
                var weight = value[3].clip(0, 1);
                ^Pwrand([item1, item2], [1 - weight, weight], inf);
            };
            if (value[0] == \rand) {
                ^Pwhite(value[1], value[2])
            };
        };

        if (value.isNumber)
        { ^value.clip(-1, 1) };

        ^value ?? 1;
    }

    // Controls
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

    dur { |args|
        ^this.putAll([\dur, args]);
    }

    fade { |direction, time|
        var fade = if (time.isNil)
        { direction }
        { [direction, time.clip(0.1, time)] };

        ^this.putAll([\fade, fade]);
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

    rate { |args|
        ^this.putAll([\rate, this.prCreatePattern(args)]);
    }

    rotate {
        ^this.putAll([\pan, \rotate]);
    }

    seed { |seed|
        ^this.putAll([\seed, seed]);
    }

    solo {
        ^this.putAll([\solo, true]);
    }

    trim { |startPosition|
        startPosition = if (startPosition.isNil)
        { \seq }
        { startPosition.clip(0, 0.75) };

        ^this.putAll([\trim, startPosition]);
    }

    weight { |weight|
        ^this.putAll([\weight, weight.clip(0, 1)]);
    }

    // FX
    prFx { |fx, mix, args|
        ^this.[\fx] = this.[\fx] ++ [[\fx, fx, \mix, this.prCreatePattern(mix)] ++ args];
    }

    delay { |mix, args|
        this.prFx(\delay, mix, args);
    }

    reverb { |mix, args|
        this.prFx(\reverb, mix, args);
    }

    wah { |mix, args|
        this.prFx(\wah, mix, args);
    }
}
