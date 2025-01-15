+ Number {
    a { |value|
        this.amp(value);
    }

    amp { |value|
        var pairs = this.prCreatePatternFromArray(\amp, value);
        this.prUpdatePattern(pairs);
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
        { pairs.addAll([\beatSet, value]) }
        { this.prRemoveBeatSetWhenSet };

        this.prUpdatePattern(pairs);
    }

    doesNotUnderstand { |selector, args|
        var parentEventsKeys = Event.parentEvents.keys.collect { |key| Event.parentEvents[key].keys.asArray };
        var partialEventsKeys = Event.partialEvents.keys.collect { |key| Event.partialEvents[key].keys.asArray };
        var allEventKeys = parentEventsKeys ++ partialEventsKeys;
        var loopKeys = SynthDescLib.global[\loop].controlNames.asSet;
        var playbufKeys = SynthDescLib.global[\playbuf].controlNames.asSet;
        var synthDefControlNames = (loopKeys ++ playbufKeys);

        Px.last.do { |event|
            if (event[\instrument].notNil and: (event[\play].isNil) and: (event[\loop].isNil)) {
                var instrumentControlNames = SynthDescLib.global[event[\instrument]].controlNames;
                synthDefControlNames = synthDefControlNames ++ instrumentControlNames;
            };
        };

        allEventKeys = (allEventKeys ++ synthDefControlNames).asArray.flat ++ \finish;

        if (allEventKeys.includes(selector))
        { this.prUpdatePattern([selector, args]) }
        { ("🔴 Method not understood:" + selector).postln };
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
        this.prUpdatePattern([\timingOffset, value]);
    }

    loop { |value|
        this.prPlay(loop: value);
    }

    out { |value|
        this.prFade(\out, value);
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
        var isSolo = value != 0;
        this.prUpdatePattern([\solo, isSolo]);
    }

    weight { |value|
        this.prUpdatePattern([\weight, value.clip(0, 1)]);
    }

    // 303 SynthDef methods with arrays to be patterns
    ctf { |value|
        var pairs = this.prCreatePatternFromArray(\ctf, value);
        this.prUpdatePattern(pairs);
    }

    env { |value|
        var pairs = this.prCreatePatternFromArray(\env, value);
        this.prUpdatePattern(pairs);
    }

    res { |value|
        var pairs = this.prCreatePatternFromArray(\res, value);
        this.prUpdatePattern(pairs);
    }

    set {
        Px.patternState = Px.last[this.asSymbol];
    }

    // Functions
    createId { |ins|
        if (this.prShouldGenerateDrumMachineId(ins)) {
            ^this.prGenerateDrumMachineId(ins);
        }

        ^this.asSymbol;
    }

    prCreatePatternFromArray { |key, value|
        var curves, isCurve;
        var pairs = [key, value];

        if (value.isArray.not)
        { ^pairs };

        isCurve = [\exp, \lin].includes(value[0]);

        case
        { isCurve }
        { ^this.prCreatePseg(key, value) };

        ^pairs;
    }

    prPreventNonZeroExponential { |curve, value|
        if (curve == \exp and: (value == 0))
        { ^0.01 }
        { ^value };
    }

    prRemoveBeatSetWhenSet {
        var id = Px.patternState[\id];
        Px.last[id].removeAt(\beatSet);
    }

    prCreatePseg { |key, value|
        var curve = value[0];
        var start = this.prPreventNonZeroExponential(value[0], value[1]);
        var end = this.prPreventNonZeroExponential(value[0], value[2]);
        var beats = value[3] ?? 8;
        var dur = value[4] ?? inf;
        var hasRepeats = dur.isInteger;
        var curvesDict = Dictionary[
            \exp -> \exponential,
            \lin -> \linear
        ];
        var durs, levels, pseg;
        var repeats = Array.new;

        if (hasRepeats) {
            levels = [start, end];
            durs = [beats, dur];
            repeats = [\repeats, dur];
        } {
            levels = [start, end, end];
            durs = [beats, inf];
        };

        pseg = Pseg(levels, durs, curvesDict[curve]);

        ^[key, pseg] ++ repeats;
    }

    prShouldGenerateDrumMachineId { |ins|
        ^this.prHasDrumMachine and: (ins.notNil);
    }

    prGenerateDrumMachineId { |ins|
        var findExistingPatternForIns = Px.last.detect({ |pattern|
            pattern[\drumMachine] == this and: (pattern[\instrument] == ins);
        });

        var drumMachinesPatternsExcludingIns = Px.last.select({ |pattern|
            pattern[\drumMachine] == this and: (pattern[\instrument] != ins)
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
            id: this.createId(i),
            instrument: i,
            loop: loop,
            play: play,
        );

        this.prPlayClass(newPattern);
    }

    prPlayClass { |newPattern|
        Px.patternState = newPattern;

        if (this.prHasDrumMachine)
        { ^Dx(newPattern.putAll([\drumMachine, this])) }
        { ^Px(newPattern) };
    }

    prUpdatePattern { |pairs|
        var pattern = Px.patternState;

        if (pattern.notNil) {
            this.prPlayClass(pattern.putAll(pairs));
        }
    }
}
