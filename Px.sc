Px {
    classvar chorus, defaultName = \px, <lastPatterns, <seeds;

    *new { | patterns, name, quant = 4, trace |
        var ptparList;

        var copyPatternsToLastPatterns = {
            if (lastPatterns.isNil)
            { lastPatterns = Dictionary[name -> patterns] }
            { lastPatterns[name] = patterns }
        };

        var getSoloPatterns = {
            var soloList = patterns.select { |pattern| pattern['solo'].notNil };
            if (soloList.isEmpty.not)
            { soloList }
            { patterns }
        };

        var createIds = {
            var indexDict = Dictionary.new;
            patterns = patterns.collect { |pattern|
                var patternStr = pattern.i.asString;
                indexDict[patternStr] = indexDict[patternStr].isNil.if
                { 0 }
                { indexDict[patternStr] + 1 };
                pattern = pattern ++ (id: pattern[\id] ?? (patternStr ++ "_" ++ indexDict[patternStr]));
            };
        };

        var createPatternBeat = { |amp, pattern|
            var seed = this.prGetPatternSeed(pattern);
            var weight = pattern[\weight] ?? 0.7;
            var rhythmWeight = (weight * 10).floor / 10;
            var pseqWeight = weight - rhythmWeight * 10;
            var rhythmSeq = { |weight|
                Array.fill(16, { [ 0, amp ].wchoose([1 - weight, weight]) });
            };
            thisThread.randSeed = seed;
            if (pseqWeight > 0) {
                var seq1 = Pseq(rhythmSeq.(rhythmWeight), 1);
                var seq2 = Pseq(rhythmSeq.(rhythmWeight + 0.1), 1);
                [Pwrand([seq1, seq2], [1 - pseqWeight, pseqWeight])];
            } {
                rhythmSeq.(weight);
            };
        };

        var createPatternAmp = { |pattern|
            var amp = pattern[\amp] ?? pattern[\a] ?? 1;
            pattern.removeAt(\a);
            if (pattern[\beat].notNil)
            { amp = createPatternBeat.(amp, pattern) };
            if (amp.isArray)
            { amp = Pseq(amp, inf) };
            pattern[\amp] = amp;
            pattern;
        };

        var createPatternDur = { |pattern|
            var dur = pattern[\dur] ?? 1;
            if (dur == 0) { dur = 1 };
            if (dur.isArray) {
                var containsString = dur.any { |item| item.isString };
                dur = containsString.if { 1 } { Pseq(dur, inf) };
            };
            if (dur.isString) { dur = 1 };
            if (pattern[\euclid].notNil)
            { dur = Pbjorklund2(pattern[\euclid][0], pattern[\euclid][1]) / pattern[\euclid][2] };
            pattern[\dur] = dur;
            pattern;
        };

        var createPatternFade = { |fade, pbind|
            var defaultFadeTime = 16;
            var dir = if (fade.isString) { fade } { fade[0] };
            var fadeTime = if (fade.isString) { defaultFadeTime } { fade[1] };
            if (dir == "in")
            { PfadeIn(pbind, fadeTime) }
            { PfadeOut(pbind, fadeTime) }
        };

        var createPatternPan = { |pattern|
            pattern[\pan] = switch (pattern[\pan].asSymbol)
            { \rand } { Pwhite(-1.0, 1.0, inf) }
            { \rotate } { Pwalk((0..10).normalize(-1, 1), 1, Pseq([1, -1], inf), startPos: 5) }
            { pattern[\pan] };
            pattern;
        };

        name = name ?? defaultName;
        copyPatternsToLastPatterns.value;
        patterns = getSoloPatterns.value;
        patterns = this.prCreateBufIns(patterns);
        patterns = createIds.value;
        patterns = this.prCreateLoops(patterns);

        patterns.do { |pattern, i|
            var pbind;
            pattern = createPatternAmp.(pattern);
            pattern = createPatternDur.(pattern);
            pattern = createPatternPan.(pattern);
            pattern = this.prCreatePatternFx(pattern);

            if (this.prHasFX(pattern) == true)
            { pbind = this.prCreatePbindFx(pattern) }
            { pbind = Pbind(*pattern.asPairs) };

            if (pattern[\fade].notNil)
            { pbind = createPatternFade.(pattern[\fade], pbind) };

            if (trace == true)
            { pbind = pbind.trace };

            ptparList = ptparList ++ [pattern[\off] ?? 0, pbind];
        };

        Pdef(name.asSymbol, Ptpar(ptparList)).quant_(quant).play;
    }

    *chorus { | name |
        if (chorus.isNil)
        { this.prPrint("Chorus is empty. Please run \"save\"") }
        { this.new(chorus, name) }
    }

    gui {
        PdefAllGui.new;
    }

    *release { | fadeTime, name |
        var fadeValue = if (fadeTime.isNil) { "out" } { ["out", fadeTime] };
        var fadeOutPatterns;
        name = name ?? defaultName;
        fadeOutPatterns = lastPatterns[name].collect { |pattern|
            if (pattern[\fade] == "out")
            { pattern[\amp] = 0 };
            pattern.putAll([\fade: fadeValue]);
            pattern;
        };
        this.send(fadeOutPatterns, name);
    }

    *save { | name |
        chorus = lastPatterns[name ?? defaultName];
    }

    *send { | patterns, name, quant, trace |
        name = name ?? defaultName;
        trace = trace ?? false;
        if (Pdef(name).isPlaying)
        { this.new(patterns, name, quant, trace) }
        { this.prPrint("Pdef(\\".catArgs(name, ") is not playing")) }
    }

    *shuffle { | name |
        this.prCreateNewSeeds;
        name = name ?? defaultName;
        name.postln;
        this.send(lastPatterns[name], name);
    }

    *prCreateNewSeeds {
        seeds.order.do { |id|
            var newSeed = (Date.getDate.rawSeconds % 1000).rand.asInteger;
            this.prPrint("Shuffle:".scatArgs(id, " ->", newSeed));
            seeds[id] = newSeed;
        };
    }

    *stop {  | name |
        name = name ?? defaultName;
        Pdef(name.asSymbol).stop;
    }

    *synthDef { | synthDef |
        if (synthDef.notNil)
        { this.prPrint(SynthDescLib.global[synthDef]) };
    }

    *trace { | name |
        this.send(lastPatterns[name], name ?? defaultName, trace: true);
    }

    *prGetPatternSeed { |pattern|
        var id = pattern[\id].asSymbol;
        if (pattern[\seed].isNil) {
            if (seeds.isNil)
            { seeds = Dictionary[id -> 1000.rand] }
            { seeds.add(id -> if (seeds[id].isNil) { 1000.rand } { seeds[id] }) };
            ^seeds[id];
        } {
            ^pattern[\seed]
        };
    }

    *prPrint { | value |
        value.postln;
    }
}
