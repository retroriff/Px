/*
Fix: Fill
Fix: PxTest Fill test
Fix: Px a pattern has an out, it should remove itself from lastPatterns after playing
*/

Px {
    classvar <>chorusPatterns;
    classvar <lastName;
    classvar <>lastFormattedPatterns;
    classvar <>lastPatterns;
    classvar <pbindList;
    classvar <samplesDict;
    classvar <seeds;

    *initClass {
        chorusPatterns = Dictionary.new;
        lastFormattedPatterns = Dictionary.new;
        lastPatterns = Dictionary.new;
        pbindList = Dictionary.new;
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

        if (Ndef(\px).isPlaying.not) {
            chorusPatterns = Dictionary.new;
            lastPatterns = Dictionary.new;
        };

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

        if (Ndef(\px).isPlaying.not) {
            Ndef(\px, pDef).play;
        };
    }

    *chorus {
        if (chorusPatterns.isNil) {
            ^this.prPrint("💩 Chorus is empty. Please run \"save\"")
        };

        lastPatterns = Dictionary.newFrom(chorusPatterns);
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
        if (name.isNil) {
            Ndef(\x).proxyspace.free(fadeTime);

            fork {
                (fadeTime + 1).wait;
                Ndef.clear;
            }
        } {
            Ndef(\px).free(fadeTime);
        };
    }

    *save {
        ^chorusPatterns = Dictionary.newFrom(lastPatterns);
    }

    *stop { |id|
        if (id.notNil) {
            lastPatterns.removeAt(id);

            if (lastPatterns.size > 0)
            { ^this.new };
        };

        ^Ndef(\px).free;
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
        { this.new(lastPatterns[name], trace: true) };
    }

    *traceOff { |name|
        if (name.isNil)
        { ^this.prPrint("Please specify a pattern name to disable trace") }
        { ^this.new(lastPatterns[name]) };
    }

    *vol { |value, name|
        ^Ndef( name ?? \px).vol_(value);
    }

    *prPrint { |value|
        if (~isUnitTestRunning != true)
        { value.postln };
    }
}
