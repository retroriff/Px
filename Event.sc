+Event {
    a { |args|
        this.amp(args);
    }

    amp { |args|
        ^this.putAll([\amp, args]);
    }

    beat { |seed, rest, set|
        var pairs = [\beat, true, \beatSet, set];

        if (seed.notNil and: seed.isInteger)
        { pairs = pairs ++ [\seed, seed] };

        ^this.putAll(this.prAddRest(pairs, rest));
    }

    dur { |args|
        ^this.putAll([\dur, args]);
    }

    euclid { |hits, total|
        ^this.putAll([\euclid, [hits, total]]);
    }

    fade { |direction, time|
        var fade = if (time.isNil)
        { direction }
        { [direction, time.clip(0.1, time)] };

        ^this.putAll([\fade, fade]);
    }

    fill { |rest|
        ^this.putAll(this.prAddRest([\fill, true], rest));
    }

    human { |delay|
        delay = delay ?? 0.5;
        ^this.putAll([\human, delay.clip(0, 1)]);
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

    prAddRest { |pairs, rest|
        if (rest.notNil)
        { pairs = pairs ++ [\rest, rest] };
        ^pairs;
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
