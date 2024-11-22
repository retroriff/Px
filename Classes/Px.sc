/*
TODO: hasEmptyDur, solution to play single hits
TODO: Bug when dur after beat 1 play: ["electro/zap", 0] dur: 0.25 beat: 1;
TODO: Make fill work with hundreth weighted beats
*/

Px {
    classvar <>chorusPatterns;
    classvar <>last;
    classvar <>lastFormatted;
    classvar <lastName;
    classvar <midiClient;
    classvar <>patternState;
    classvar <>ndefList;
    classvar <samplesDict;
    classvar <seeds;

    *initClass {
        chorusPatterns = Dictionary.new;
        last = Dictionary.new;
        lastFormatted = Dictionary.new;
        ndefList = Dictionary.new;
        seeds = Dictionary.new;
    }

    *new { | newPattern, quant, trace |
        var patterns;

        this.prInitializeDictionaries(newPattern);

        if (newPattern.notNil)
        { last[newPattern[\id]] = newPattern };

        patterns = this.prHandleSoloPatterns;
        patterns = this.prCreateBufInstruments(patterns);

        patterns do: { |pattern|
            var pbindef;

            pattern = this.prCreateLoops(pattern);
            pattern = this.prCreateAmp(pattern);
            pattern = this.prCreateDur(pattern);
            pattern = this.prCreatePan(pattern);
            pattern = this.prCreateDegrees(pattern);
            pattern = this.prCreateOctaves(pattern);
            pattern = this.prCreateMidi(pattern);
            pattern = this.prCreateFx(pattern);

            if (this.prHasFX(pattern) == true)
            { pbindef = this.prCreatePbindFx(pattern) }
            { pbindef = Pbind(*pattern.asPairs) };

            if (pattern[\fade].notNil)
            { pbindef = this.prCreateFade(pattern[\fade], pbindef) };

            if (trace == true)
            { pbindef = pbindef.trace };

            pbindef = Pdef(pattern[\id], pbindef).quant_(4);

            if (ndefList[pattern[\id]].isNil)
            { ndefList.put(pattern[\id], Ndef(pattern[\id], pbindef)) };
        };

        if (newPattern.notNil)
        { lastFormatted[newPattern[\id]] = patterns[newPattern[\id]]};

        if (Ndef(\px).isPlaying)
        { Ndef(\px).source = { Mix.new(ndefList.values) } }
        { Ndef(\px, { Mix.new(ndefList.values) }).play };

        if (newPattern.notNil)
        { this.prRemoveFinitePatternFromLast(newPattern) };
    }

    *prRemoveFinitePatternFromLast { |pattern|
        var hasFadeIn = pattern[\fade].isArray
        and: { pattern[\fade][0] == \in };

        var hasFadeOut = pattern[\fade].isArray
        and: { pattern[\fade][0] == \out };

        var hasRepeats = pattern[\repeats].notNil;

        // TODO
        // var hasEmptyDur = pattern[\dur].isNil;;
        var hasEmptyDur = false;

        case
        { hasFadeOut or: hasRepeats or: hasEmptyDur }
        {
            last.removeAt(pattern[\id]);
            ndefList.removeAt(pattern[\id]);
        }

        { hasFadeIn }
        { last[pattern[\id]].removeAt(\fade) };
    }

    *chorus {
        if (chorusPatterns.isNil) {
            ^this.prPrint("💩 Chorus is empty. Please run \"save\"")
        };

        last = Dictionary.newFrom(chorusPatterns);

        ^this.new;
    }

    *play { |name|
        var newPattern;

        if (name.notNil)
        { newPattern = last[name] };

        if (newPattern.isNil)
        { newPattern = (i: \bd, id: \1, dur: 1) };

        ^this.new(newPattern);
    }

    *release { |fadeTime = 10, name|
        if (name == \all) {
            Ndef(\x).proxyspace.free(fadeTime);

            ^fork {
                (fadeTime + 5).wait;
                Ndef.clear;
            }
        }

        ^Ndef(\px).free(fadeTime);
    }

    *save {
        ^chorusPatterns = Dictionary.newFrom(last);
    }

    *stop { |id|
        if (id.notNil) {
            last.removeAt(id);
            ndefList.removeAt(id);

            if (last.size > 0)
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
        if (tempo.isNil) {
            ^this.prPrint("🕰️ Current tempo is" + (TempoClock.tempo * 60));
        };

        TempoClock.default.tempo = tempo.clip(10, 300) / 60;

        ^this.loadSynthDefs;
    }

    *trace { |name|
        if (name.isNil)
        { this.prPrint("Please specify a pattern name to trace") }
        { this.new(last[name], trace: true) };
    }

    *traceOff { |name|
        if (name.isNil)
        { ^this.prPrint("Please specify a pattern name to disable trace") }
        { ^this.new(last[name]) };
    }

    *vol { |value, name|
        ^Ndef( name ?? \px).vol_(value);
    }

    *prCreateAmp { |pattern|
        var amp = pattern[\amp] ?? 1;

        if (pattern[\beat].notNil)
        { amp = this.prCreateRhythmBeat(amp, pattern) };

        if (pattern[\fill].notNil)
        { amp = this.prCreateFillFromBeat(amp, pattern) };

        pattern[\dur] = this.prCreateBeatRest(pattern);
        pattern[\amp] = amp;

        if (pattern[\amp].isArray)
        { pattern[\amp] = Pseq(pattern[\amp], inf) };

        ^pattern;
    }

    *prCreateDur { |pattern|
        var dur = pattern[\dur];

        if (dur.isNil or: (dur == 0))
        { dur = Pseq([8], 1) };

        if (dur.isArray) {
            var containsString = dur any: { |item| item.isString };
            dur = containsString.if { 1 } { Pseq(dur, inf) };
        };

        if (dur.isString)
        { dur = 1 };

        if (pattern[\euclid].notNil)
        { dur = Pbjorklund2(pattern[\euclid][0], pattern[\euclid][1]) * dur };

        pattern[\dur] = dur;

        ^this.prHumanize(pattern);
    }

    *prCreateFade { |fade, pbind|
        var defaultFadeTime = 16;
        var direction, fadeTime;

        if (fade.isArray) {
            direction = fade[0];
            fadeTime = fade[1];
        } {
            direction = fade;
            fadeTime = defaultFadeTime;
        };

        if (direction == \in)
        { ^PfadeIn(pbind, fadeTime) }
        { ^PfadeOut(pbind, fadeTime) };
    }

    *prCreatePan { |pattern|
        pattern[\pan] = switch (pattern[\pan].asSymbol)

        { \rand }
        { Pwhite(-1.0, 1.0, inf) }

        { \rotate }
        { Pwalk((0..10).normalize(-1, 1), 1, Pseq([1, -1], inf), startPos: 5) }

        { pattern[\pan] };

        ^pattern;
    }

    *prHandleSoloPatterns {
        var patterns = last.copy;

        var hasSolo = patterns any: { |pattern|
            pattern['solo'] == true;
        };

        if (hasSolo) {
            var filteredIds;

            patterns = patterns select: { |pattern|
                pattern['solo'] == true;
            };

            filteredIds = patterns.collect { |pattern| pattern[\id] };

            ndefList = ndefList.select { |key, value|
                filteredIds.includes(key)
            };
        };

        ^patterns;
    }

    *prHumanize { |pattern|
        if (pattern[\human].notNil) {
            var delay = pattern[\human] * 0.04;
            pattern[\lag] = Pwhite(delay.neg, delay);
        };

        ^pattern;
    }

    *prInitializeDictionaries {
        if (Ndef(\px).isPlaying.not) {
            chorusPatterns = Dictionary.new;
            last = Dictionary.new;
            ndefList = Dictionary.new;
        };
    }

    *prPrint { |value|
        if (~isUnitTestRunning != true)
        { value.postln };
    }
}
