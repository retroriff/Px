+ Number {
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

    // 303 SynthDef methods
    atk { |value|
        this.prUpdatePattern([\atk, value]);
    }

    ctf { |value|
        this.prUpdatePattern([\ctf, value]);
    }

    env { |value|
        this.prUpdatePattern([\env, value]);
    }

    rel { |value|
        this.prUpdatePattern([\rel, value]);
    }

    res { |value|
        this.prUpdatePattern([\res, value]);
    }

    wave { |value|
        this.prUpdatePattern([\wave, value]);
    }

    // Functions
    createId { |ins|
        if (this.prShouldGenerateDrumMachineId(ins)) {
            ^this.prGenerateDrumMachineId(ins);
        }

        ^this.asSymbol;
    }

    prShouldGenerateDrumMachineId { |ins|
        ^this.prHasDrumMachine and: (ins.notNil);
    }

    prGenerateDrumMachineId { |ins|
        var findExistingPatternForIns = Px.lastPatterns.detect({ |pattern|
            pattern[\drumMachine] == this and: (pattern[\i] == ins);
        });

        var drumMachinesPatternsExcludingIns = Px.lastPatterns.select({ |pattern|
            pattern[\drumMachine] == this and: (pattern[\i] != ins)
        });

        var getMaximumId = drumMachinesPatternsExcludingIns
        .collect({ |pattern| pattern[\id].asInteger })
        .maxItem;

        var generateNewDrumMachineId = {
            if (drumMachinesPatternsExcludingIns.isEmpty)
            { this * 100 + 1 }
            { getMaximumId + 1 };
        };

        if (findExistingPatternForIns.isNil)
        { ^generateNewDrumMachineId.value.asSymbol }
        { ^findExistingPatternForIns[\id] };
    }

    prHasDrumMachine {
        var drumMachines = [606, 707, 808, 909];
        ^drumMachines.includes(this);
    }

    prFade { |direction, time|
        var fade;

        if (time.isNil)
        { fade = direction }
        { fade = [direction, time.clip(0.1, time)] };

        this.prUpdatePattern([\fade, fade]);
    }

    prPlay { |i, play, loop|
        var newPattern = (
            i: i,
            id: this.createId(i),
            play: play,
            loop: loop
        );

        this.prPlayClass(newPattern);
    }

    prPlayClass { |newPattern|
        Px.patternState = newPattern;

        if (this.prHasDrumMachine)
        { ^TR08(newPattern.putAll([\drumMachine, this])) }
        { ^Px(newPattern) };
    }

    prUpdatePattern { |pairs|
        var pattern = Px.patternState;

        if (pattern.notNil) {
            this.prPlayClass(pattern.putAll(pairs));
        }
    }
}

+ Symbol {
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
    ins {}
    lpf {}
    off {}
    pan {}
    rest {}
    reverb {}
    rotate {}
    seed {}
    solo {}
    wah {}
    weight {}
    // 303 SynthDef
    atk {}
    ctf {}
    env {}
    rel {}
    res {}
    wave {}

    i { |value|
        var number = this.asInteger;
        var id = number.createId(value);

        if (this.prHasDrumMachine and: (value == \all))
        { this.prStopDrumMachineInstruments }
        { Px.stop(id) };
    }

    loop { |value|
        Px.stop(this);
    }

    play { |value|
        Px.stop(this);
    }

    prHasDrumMachine {
        var drumMachines = [\606, \707, \808, \909];
        ^drumMachines.includes(this);
    }

    prStopDrumMachineInstruments {
        var patterns = Px.lastPatterns.copy;

        patterns.do({ |pattern|
            if (pattern[\drumMachine] == this.asInteger)
            { Px.stop(pattern[\id]) };
        });
    }
}
