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

    dur { |args|
        ^this.putAll([\dur, args]);
    }

    fade { |direction, time|
        var fade = if (time.isNil)
        { direction }
        { [direction, time.clip(0.1, time)] };

        ^this.putAll([\fade, fade]);
    }

    fill {
        ^this.putAll([\fill, true]);
    }

    in { |time|
        this.fade("in", time);
    }

    out { |time|
        this.fade("out", time);
    }

    px { |name, quant, trace|
        Px([this], name, quant, trace);
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

    weight { |weight|
        ^this.putAll([\weight, weight.clip(0, 1)]);
    }

    prCreatePatternKey { |value|
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
}
