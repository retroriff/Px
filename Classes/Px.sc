/*
Fix: Solo doesn't work properly
Fix: Weight doesn't work properly: 3 i: \ch dur: 0.25 beat: 1 weight: 0.1;
Fix: Px methods
Fix: Ptpar per a off
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

    *new { | newPattern, quant, trace |
        var pDef, pbind, ptpar;

        var name = newPattern[\id];

        var handleSoloPatterns = {
            var hasSolo = lastPatterns any: { |pattern|
                pattern['solo'] == true;
            };

            if (hasSolo) {
                var muteList = lastPatterns.select { |pattern|
                    pattern['solo'] != true
                };

                muteList do: { |pattern|
                    lastPatterns[pattern[\id]][\solo] = false;
                    Px.stop(pattern[\id]);
                }
            } {
                var muteList = lastPatterns.select { |pattern|
                    pattern['solo'] == false
                };

                muteList do: { |pattern|
                    lastPatterns[pattern[\id]].removeAt(\solo);

                    if (nodeProxy[name].isPlaying == false)
                    { Px.play(pattern[\id]) };
                }
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
            var beats;

            if (pattern[\beatSet].isNil) {
                var seed = this.prGetPatternSeed(pattern);
                var weight = pattern[\weight] ?? 0.7;
                var rhythmWeight = (weight * 10).floor / 10;
                var pseqWeight = weight - rhythmWeight * 10;
                var rhythmSeq = { |weight|
                    Array.fill(16, { [ 0, amp ].wchoose([1 - weight, weight]) });
                };

                weight.postln;
                rhythmSeq.(rhythmWeight).postln;

                thisThread.randSeed = seed;

                if (pseqWeight > 0) {
                    var seq1 = Pseq(rhythmSeq.(rhythmWeight), 1);
                    var seq2 = Pseq(rhythmSeq.(rhythmWeight + 0.1), 1);
                    beats = [Pwrand([seq1, seq2], [1 - pseqWeight, pseqWeight])];
                } {
                    beats = rhythmSeq.(weight);
                };
            } {
                beats = createPatternBeatSet.(amp, pattern);
            };

            lastPatterns[name][\beats] = beats;
            beats;
        };

        var createPatternBeatSet = { |amp, pattern|
            var list = pattern[\beatSet].collect { |step|
                if (step >= 1)
                { step = amp };
                step;
            };

            Pseq(list, inf);
        };

        var createPatternFillFromBeat = { |amp, pattern|
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

            var previousPatternId = (pattern[\id].asInteger - 1).asSymbol;
            var invertBeat = getInvertBeat.(lastPatterns[previousPatternId][\beats], pattern[\amp]);
            var totalBeat = getTotalBeat.(invertBeat);

            lastPatterns[name][\beats] = invertBeat;
            newPattern.putAll([\totalBeat, totalBeat]);
            totalBeat;
        };

        var createPatternAmp = { |pattern|
            var amp = pattern[\amp] ?? pattern[\a] ?? 1;
            pattern.removeAt(\a);

            if (pattern[\beat].notNil) {
                amp = createPatternBeat.(amp, pattern);
            };

            if (pattern[\fill].notNil) {
                amp = createPatternFillFromBeat.(amp, pattern);
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

        if (newPattern.notNil) {
            lastPatterns[name] = newPattern;
            handleSoloPatterns.value;
            newPattern = this.prCreateBufIns(newPattern);
            newPattern = this.prCreateLoops(newPattern);
            newPattern = createPatternAmp.(newPattern);
            newPattern = createPatternDur.(newPattern);
            newPattern = createPatternPan.(newPattern);
            newPattern = this.prCreatePatternFx(newPattern);

            if (this.prHasFX(newPattern) == true)
            { pbind = this.prCreatePbindFx(newPattern) }
            { pbind = Pbind(*newPattern.asPairs) };

            if (newPattern[\fade].notNil)
            { pbind = createPatternFade.(newPattern[\fade], pbind) };

            if (trace == true)
            { pbind = pbind.trace };

            ptpar = [newPattern[\off] ?? 0, pbind];
            lastFormattedPatterns[name] = newPattern;
            pDef = Pdef(name.asSymbol, Ptpar(ptpar)).quant_(quant ?? 4);

            if (nodeProxy[name].isPlaying.not and: (newPattern[\solo] != false)) {
                nodeProxy.add(name -> Ndef(name, pDef).play);
            };
        }
    }

    *chorus { |name|
        if (chorusPatterns[name].isNil)
        { this.prPrint("💩 Chorus is empty. Please run \"save\"") }
        { this.new(chorusPatterns[name], name) }
    }

    *play { |name|
        var newPattern;

        if (name.notNil)
        { newPattern = lastPatterns[name] };

        if (newPattern.isNil)
        { newPattern = (i: \bd, id: \1) };

        this.new(newPattern);
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
        nodeProxy[name].free;
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
