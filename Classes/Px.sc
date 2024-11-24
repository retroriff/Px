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
    classvar <>ndefList;
    classvar <>patternState;
    classvar <samplesDict;
    classvar <seeds;
    classvar <>soloList;

    *initClass {
        chorusPatterns = Dictionary.new;
        last = Dictionary.new;
        lastFormatted = Dictionary.new;
        ndefList = Dictionary.new;
        seeds = Dictionary.new;
        soloList = Array.new;
    }

    *new { |newPattern|
        var pattern, pbindef, playList;

        this.prInitializeDictionaries(newPattern);
        this.prHandleSoloPattern(newPattern);

        pattern = this.prCreateBufInstruments(newPattern);
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

        pbindef = this.prCreateFade(pbindef, pattern[\fade]);
        pbindef = Pdef(pattern[\id], pbindef).quant_(4);

        if (ndefList[pattern[\id]].isNil)
        { ndefList.put(pattern[\id], Ndef(pattern[\id], pbindef)) };

        lastFormatted[newPattern[\id]] = pattern;
        playList = this.prCreatePlayList;

        if (Ndef(\px).isPlaying)
        { Ndef(\px).source = { Mix.new(playList.values) } }
        { Ndef(\px, { Mix.new(playList.values) }).play };

        this.prRemoveFinitePatternFromLast(newPattern);
    }

    *prCreatePlayList {
        if (soloList.isEmpty)
        { ^ndefList.copy };

        ^ndefList.copy.select { |value, key|
            soloList.includes(key)
        };
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

    *prCreateFade { |pbindef, fade|
        var defaultFadeTime = 16;
        var direction, fadeTime;

        if (fade.isNil)
        { ^pbindef };

        if (fade.isArray) {
            direction = fade[0];
            fadeTime = fade[1];
        } {
            direction = fade;
            fadeTime = defaultFadeTime;
        };

        if (direction == \in)
        { ^PfadeIn(pbindef, fadeTime) }
        { ^PfadeOut(pbindef, fadeTime) };
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

    *prHandleSoloPattern { |pattern|
        var hasSolo = pattern['solo'] == true;

        if (hasSolo)
        { soloList.add(pattern[\id]) }
        { soloList.remove(pattern[\id]) };
    }

    *prHumanize { |pattern|
        if (pattern[\human].notNil) {
            var delay = pattern[\human] * 0.04;
            pattern[\lag] = Pwhite(delay.neg, delay);
        };

        ^pattern;
    }

    *prInitializeDictionaries { |pattern|
        if (Ndef(\px).isPlaying.not) {
            chorusPatterns = Dictionary.new;
            last = Dictionary.new;
            ndefList = Dictionary.new;
        };

        last[pattern[\id]] = pattern;
    }

    *prReevaluate { |patterns|
        patterns = patterns ?? last;

        ^patterns do: { |value, key|
            this.new(value);
        }
    }

    *prPrint { |value|
        if (~isUnitTestRunning != true)
        { value.postln };
    }
}
