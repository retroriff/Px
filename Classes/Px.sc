/*
Fix: Px methods
Fix: Px a pattern has a out, it should remove itself from lastPatterns after playing
*/

Px {
    classvar <>chorusPatterns;
    classvar <lastName;
    classvar <>lastFormattedPatterns;
    classvar <>lastPatterns;
    classvar <pbindList;
    classvar <nodeProxy;
    classvar <samplesDict;
    classvar <seeds;

    *initClass {
        chorusPatterns = Dictionary.new;
        lastFormattedPatterns = Dictionary.new;
        lastPatterns = Dictionary.new;
        pbindList = Dictionary.new;
        nodeProxy = Dictionary.new;
        seeds = Dictionary.new;
    }

    *new { | newPattern, quant, trace |
        var pDef, pbind, ptpar;

        var handleSoloPatterns = { |patterns|
            var hasSolo = patterns any: { |pattern|
                pattern['solo'] == true;
            };

            if (hasSolo) {
                patterns = patterns select: { |pattern|
                    pattern['solo'] == true
                };
            };

            patterns;
        };

        var createPatternAmp = { |pattern|
            var amp = pattern[\amp] ?? 1;

            if (pattern[\beat].notNil) {
                amp = this.prCreatePatternBeat(amp, pattern);
            };

            if (pattern[\fill].notNil) {
                amp = this.prCreatePatternFillFromBeat(amp, pattern);
            };

            pattern[\dur] = this.prCreatePatternBeatRest(pattern);

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
                var containsString = dur any: { |item| item.isString };
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
            var dir = if (fade.isArray) { fade[0] } { fade };
            var fadeTime = if (fade.isArray) { fade[1] } { defaultFadeTime };

            if (dir == \in)
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

        var patterns, ptparList;

        if (Ndef(\px).isPlaying.not)
        { lastPatterns = Dictionary.new };

        if (newPattern.notNil)
        { lastPatterns[newPattern[\id]] = newPattern };

        patterns = handleSoloPatterns.(lastPatterns);
        patterns = this.prCreateBufIns(patterns);
        patterns = this.prCreateLoops(patterns);

        patterns do: { |pattern|
            var pbind;

            pattern = createPatternAmp.(pattern);
            pattern = createPatternDur.(pattern);
            pattern = createPatternPan.(pattern);
            pattern = this.prCreatePatternFx(pattern);

            if (pattern[\amp].isArray)
            { pattern[\amp] = Pseq(pattern[\amp], inf) };

            if (this.prHasFX(pattern) == true)
            { pbind = this.prCreatePbindFx(pattern) }
            { pbind = Pbind(*pattern.asPairs) };

            if (pattern[\fade].notNil)
            { pbind = createPatternFade.(pattern[\fade], pbind) };

            if (trace == true)
            { pbind = pbind.trace };

            ptparList = ptparList ++ [pattern[\off] ?? 0, pbind];
        };

        if (newPattern.notNil)
        { lastFormattedPatterns[newPattern[\id]] = patterns[newPattern[\id]]};

        pDef = Pdef(\px, Ptpar(ptparList)).quant_(quant ?? 4);

        if (nodeProxy[\px].isPlaying.not) {
            nodeProxy.add(\px -> Ndef(\px, pDef).play);
        };
    }

    *prCreatePatternBeatRest { |pattern|
        var dur = pattern[\dur];

        if (pattern[\rest].notNil) {
            dur = Pseq([Pn(dur, 15), pattern[\rest] + dur], inf);
        };

        ^dur;
    }

    *prCreatePatternBeat { |amp, pattern|
        var beats;

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
                beats = [Pwrand([seq1, seq2], [1 - pseqWeight, pseqWeight])];
            } {
                beats = rhythmSeq.(weight);
            };
        } {
            beats = this.prCreatePatternBeatSet(amp, pattern);
        };

        lastPatterns[pattern[\id]][\beats] = beats;
        ^beats;
    }

    *prCreatePatternBeatSet { |amp, pattern|
        var list = pattern[\beatSet].collect { |step|
            if (step >= 1)
            { step = amp };
            step;
        };

        ^Pseq(list, inf);
    }

    *prCreatePatternFillFromBeat { |amp, pattern|
        var steps = 16;
        var invertBeat, previousBeats, totalBeat;
        var previousPatternId = (pattern[\id].asInteger - 1).asSymbol;

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
            var beat = lastPatterns[previousPatternId][\totalBeat] ?? Array.fill(steps, 0);
            (beat + invertBeat) collect: { |step| step.clip(0, 1) };
        };

        if (lastPatterns[previousPatternId].notNil)
        { previousBeats = lastPatterns[previousPatternId][\beats] ?? lastPatterns[previousPatternId][\totalBeat] };

        if (previousBeats.isNil)
        { ^amp };

        invertBeat = getInvertBeat.(previousBeats, pattern[\amp]);
        totalBeat = getTotalBeat.(invertBeat);

        lastPatterns[pattern[\id]].putAll([\totalBeat, totalBeat]);
        ^totalBeat;
    }

    *chorus {
        if (chorusPatterns.isNil) {
            ^this.prPrint("💩 Chorus is empty. Please run \"save\"")
        };

        lastPatterns = chorusPatterns;
        ^this.new;
    }

    *play { |name|
        var newPattern;

        if (name.notNil)
        { newPattern = lastPatterns[name] };

        if (newPattern.isNil)
        { newPattern = (i: \bd, id: \1) };

        ^this.new(newPattern);
    }

    *release { |fadeTime = 10, name|
        if (name.isNil)
        { name = \all };

        if (name == \all) {
            Ndef(\x).proxyspace.free(fadeTime);
            nodeProxy.clear;
        } {
            nodeProxy[name].free(fadeTime);
            nodeProxy.removeAt(name);
        };
    }

    *save {
        chorusPatterns = lastPatterns;
    }

    *shuffle {
        this.prCreateNewSeeds;
        this.new;
    }

    *stop { |id|
        if (id.notNil)
        { lastPatterns.removeAt(id) };

        if (lastPatterns.size > 0)
        { ^this.new }
        { ^nodeProxy[\px].free };
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
        if (name.isNil)
        { this.prPrint("Please specify a pattern name to trace") }
        { this.prSend(lastPatterns[name], trace: true) };
    }

    *traceOff { |name|
        if (name.isNil)
        { this.prPrint("Please specify a pattern name to disable trace") }
        { this.prSend(lastPatterns[name]) };
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

    *prSend { |newPattern, quant, trace|
        var name = newPattern[\id];
        trace = trace ?? false;

        if (nodeProxy[name].isPlaying)
        { this.new(newPattern, quant, trace) }
        { this.prPrint("💩 Pdef(\\".catArgs(name, ") is not playing")) }
    }

    *prPrint { |value|
        if (~isUnitTestRunning != true)
        { value.postln };
    }
}
