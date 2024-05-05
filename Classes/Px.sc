/*
TODO: Classvar "seeds" should also be multiname
*/

Px {
    classvar <>chorusPatterns;
    classvar <lastName;
    classvar <>lastFormattedPatterns;
    classvar <>lastPatterns;
    classvar <nodeProxy;
    classvar <samplesDict;
    classvar <seeds;

    *initClass {
        chorusPatterns = Dictionary.new;
        lastFormattedPatterns = Dictionary.new;
        lastPatterns = Dictionary.new;
        nodeProxy = Dictionary.new;
        seeds = Dictionary.new;
    }

    *new { | patterns, name, quant, trace |
        var pDef, ptparList;

        var getSoloPatterns = {
            var soloList = patterns.select { |pattern|
                pattern['solo'].notNil
            };

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
                pattern[\id] = pattern[\id].asSymbol;
            };
        };

        var createPatternBeatRest = { |pattern|
            var dur = pattern[\dur];
            if (pattern[\rest].notNil) {
                dur = Pseq([Pn(dur, 15), pattern[\rest] + dur], inf);
            };
            dur;
        };

        var createPatternBeat = { |amp, pattern|
            if (pattern[\beatSet].isNil) {
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
            } {
                createPatternBeatSet.(amp, pattern);
            };
        };

        var createPatternBeatSet = { |amp, pattern|
            var list = pattern[\beatSet].collect { |step|
                if (step >= 1)
                { step = amp };
                step;
            };
            Pseq(list, inf);
        };

        var createPatternFillFromBeat = { |amp, i, pattern|
            var steps = 16;
            var getInvertBeat = { |beatAmp, invertAmp = 1|
                var invertBeat = beatAmp.iter.loop.nextN(steps).linlin(0, amp, amp, Rest());
                var weight = pattern[\weight] ?? 1;
                thisThread.randSeed = this.prGetPatternSeed(pattern);
                invertBeat.collect { |step|
                    if (step == amp) {
                        step = [0, amp].wchoose([1 - weight, weight]);
                    };
                    step;
                };
            };
            var getTotalBeat = { |invertBeat|
                var beat = pattern[\totalBeat] ?? Array.fill(steps, 0);
                (beat + invertBeat).collect { |step| step.clip(0, 1) };
            };
            var invertBeat = getInvertBeat.(patterns[i - 1][\amp], pattern[\amp]);
            var totalBeat = getTotalBeat.(invertBeat);
            patterns[i].putAll([\totalBeat, totalBeat]);
            totalBeat;
        };

        var createPatternAmp = { |pattern, i|
            var amp = pattern[\amp] ?? pattern[\a] ?? 1;
            pattern.removeAt(\a);
            if (pattern[\beat].notNil) {
                amp = createPatternBeat.(amp, pattern);
            };
            if (pattern[\fill].notNil) {
                amp = createPatternFillFromBeat.(amp, i, pattern);
            };
            pattern[\dur] = createPatternBeatRest.(pattern);
            if (amp.isArray)
            { amp = Pseq(amp, inf) };
            pattern[\amp] = amp;
            pattern;
        };

        var humanize = { |pattern|
            if (pattern[\human].notNil) {
                var delay = pattern[\human] * 0.04;
                pattern[\lag] = Pwhite(delay.neg, delay);
            };
            pattern;
        };

        var createPatternDur = { |pattern|
            var dur = pattern[\dur] ?? 1;

            if (dur == 0)
            { dur = 1 };

            if (dur.isArray) {
                var containsString = dur.any { |item| item.isString };
                dur = containsString.if { 1 } { Pseq(dur, inf) };
            };

            if (dur.isString)
            { dur = 1 };

            if (pattern[\euclid].notNil)
            { dur = Pbjorklund2(pattern[\euclid][0], pattern[\euclid][1]) * dur };

            pattern[\dur] = dur;
            humanize.(pattern);
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

        name = this.prGetName(name);
        patterns = getSoloPatterns.value;
        lastPatterns[name] = patterns;

        patterns = createIds.value;
        patterns = this.prCreateBufIns(patterns);
        patterns = this.prCreateLoops(patterns);

        patterns do: { |pattern, i|
            var pbind;
            pattern = createPatternAmp.(pattern, i);
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

        lastFormattedPatterns[name] = patterns;
        pDef = Pdef(name.asSymbol, Ptpar(ptparList)).quant_(quant ?? 4);

        if (nodeProxy[name].isPlaying.not) {
            nodeProxy.add(name -> Ndef(name, pDef).play);
        };
    }

    *chorus { |name|
        name = name ?? lastName;

        if (chorusPatterns[name].isNil)
        { this.prPrint("💩 Chorus is empty. Please run \"save\"") }
        { this.new(chorusPatterns[name], name) }
    }

    *play { |name|
        var patterns;
        name = name ?? lastName;
        patterns = lastPatterns[name] ?? [(i: \bd)];
        this.new(patterns, name);
    }

    *release { |fadeTime = 10, name|
        name = this.prGetName(name);
        if (name == \all) {
            Ndef(\x).proxyspace.free(fadeTime);
            nodeProxy.clear;
        } {
            nodeProxy[name].free(fadeTime);
            nodeProxy.removeAt(name);
        };
    }

    *save { |name|
        name = name ?? lastName;

        chorusPatterns[name] = lastPatterns[name ?? lastName];
    }

    *shuffle { |name|
        name = name ?? lastName;
        this.prCreateNewSeeds;
        this.prSend(lastPatterns[name], name);
    }

    *stop { |name|
        name = name ?? lastName;
        nodeProxy[name].stop;
        nodeProxy.removeAt(name);
    }

    *synthDef { |synthDef|
        if (synthDef.isNil)
        { SynthDescLib.global.browse }
        { ^SynthDescLib.global[synthDef] };
    }

    *tempo { |tempo|
        TempoClock.default.tempo = tempo.clip(10, 300) / 60;
        this.loadSynthDefsAfterUpdatingTempo;
    }

    *trace { |name|
        name = this.prGetName(name);
        this.prSend(lastPatterns[name], name, trace: true);
    }

    *vol { |value, name|
        name = name ?? lastName;
        nodeProxy[name].vol_(value);
    }

    *prCreateNewSeeds {
        seeds.order do: { |id|
            var newSeed = (Date.getDate.rawSeconds % 1000).rand.asInteger;
            this.prPrint("🎲 Shuffle:".scatArgs(id, "->", newSeed));
            seeds[id] = newSeed;
        };
    }

    *prGenerateRandNumber { |id|
        var seed = 1000.rand;
        this.prPrint("🎲 Seed:".scatArgs(id, "->", seed));
        ^seed;
    }

    *prGetName { | name |
        name = name ?? this.name.asString.toLower.asSymbol;
        lastName = name;
        ^name;
    }

    *prGetPatternSeed { |pattern|
        var id = pattern[\id].asSymbol;
        if (pattern[\seed].isNil) {
            var seed;

            if (seeds[id].isNil)
            { seed = this.prGenerateRandNumber(id) }
            { seed = seeds[id] };

            seeds.add(id -> seed);
            ^seeds[id];
        } {
            ^pattern[\seed];
        };
    }

    *prSend { |patterns, name, quant, trace|
        name = name ?? lastName;
        trace = trace ?? false;

        if (nodeProxy[name].isPlaying)
        { this.new(patterns, name, quant, trace) }
        { this.prPrint("💩 Pdef(\\".catArgs(name, ") is not playing")) }
    }

    *prPrint { |value|
        if (~isUnitTestRunning != true)
        { value.postln };
    }
}

