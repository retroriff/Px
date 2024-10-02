/*
TODO: Fix fill
TODO: Fix seed
TODO: Fix solo
*/

+Number {
    a { |value|
        this.amp(value);
    }

    amp { |value|
        this.prUpdatePattern([\amp, value]);
    }

    beat { |value|
        var pairs = Array.new;

        if (value != 0)
        { pairs = [\beat, true] };

        if (value.isArray)
        { pairs.addAll([\beatSet, value]) };

        this.prUpdatePattern(pairs);
    }

    dur { |value|
        this.prUpdatePattern([\dur, value]);
    }

    euclid { |value|
        var hits = value[0];
        var total = value[1];
        this.prUpdatePattern([\euclid, [hits, total]]);
    }

    fade { |value|
        this.prFade(value);
    }

    fill { |value|
        this.prUpdatePattern([\fill, true]);
    }

    human { |delay|
        delay = delay ?? 0.1;
        this.prUpdatePattern([\human, delay.clip(0, 1)]);
    }

    i { |value|
        Px([(i: value)], name: this.asSymbol);
    }

    in { |value|
        this.prFade("in", value);
    }

    ins { |value|
        this.i(value);
    }

    out { |value|
        this.prFade("out", value);
    }

    pan { |value|
        this.prUpdatePattern([\pan, value]);
    }

    rest { |value|
        this.prUpdatePattern([\rest, value]);
    }

    rotate { |value|
        if (value != 0)
        { this.pan(\rotate) };
    }

    seed { |value|
        this.prUpdatePattern([\seed, value]);
    }

    solo { |value|
        this.prUpdatePattern([\solo, true]);
    }

    weight { |value|
        this.prUpdatePattern([\weight, value.clip(0, 1)]);
    }

    prUpdatePattern { |pairs|
        var pattern = Px.lastFormattedPatterns[this.asSymbol][0];
        pattern = pattern.putAll(pairs);

        Px([pattern], name: this.asSymbol);
    }

    prFade { |direction, time|
        var fade;

        if (time.isNil)
        { fade = direction }
        { fade = [direction, time.clip(0.1, time)] };

        this.prUpdatePattern([\fade, fade]);
    }
}

+Symbol {
    // Prevent methods to generate errors when a Px is stopped through a symbol
    a {}
    amp {}
    beat {}
    dur {}
    fill {}
    human {}
    i {}
    in {}
    ins {}
    out {}
    pan {}
    rest {}
    rotate {}
    solo {}
    seed {}
    weight {}

    i { |value|
        Px.stop(this);
    }
}
