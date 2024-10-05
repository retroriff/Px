+Number {
    a { |value|
        this.amp(value);
    }

    amp { |value|
        this.prUpdatePattern([\amp, value]);
    }

    args { |value|
        if (value.class == Event) {
            this.prUpdatePattern(value.asPairs);
        }
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
        this.prFade(value.asSymbol);
    }

    fill { |value|
        this.prUpdatePattern([\fill, true]);
    }

    human { |delay|
        delay = delay ?? 0.1;
        this.prUpdatePattern([\human, delay.clip(0, 1)]);
    }

    i { |value|
        this.prPlay(i: value);
    }

    in { |value|
        this.prFade(\in, value);
    }

    ins { |value|
        this.i(value);
    }

    off { |value|
        this.prUpdatePattern([\off, value]);
    }

    loop { |value|
        this.prPlay(loop: value);
    }

    out { |value|
        this.prFade(\out, value);
    }

    pan { |value|
        this.prUpdatePattern([\pan, value]);
    }

    play { |value|
        this.prPlay(play: value);
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
        var pattern = Px.lastPatterns[this.asSymbol];
        pattern = pattern.putAll(pairs);

        Px(pattern);
    }

    prFade { |direction, time|
        var fade;

        if (time.isNil)
        { fade = direction }
        { fade = [direction, time.clip(0.1, time)] };

        this.prUpdatePattern([\fade, fade]);
    }

    prPlay { |i, play, loop|
        var id = this.asSymbol;
        Px(
            newPattern: (
                i: i,
                id: id,
                play: play,
                loop: loop
            )
        );
    }
}

+Symbol {
    // Prevent methods to generate errors when a Px is stopped through a symbol
    a {}
    amp {}
    args {}
    beat {}
    delay {}
    dur {}
    euclid {}
    fill {}
    hpf {}
    human {}
    i {}
    in {}
    ins {}
    lpf {}
    off {}
    out {}
    pan {}
    rest {}
    reverb {}
    rotate {}
    seed {}
    solo {}
    wah {}
    weight {}

    i { |value|
        Px.stop(this);
    }

    loop { |value|
        Px.stop(this);
    }
}
