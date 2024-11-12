+ Px {
    *prCreateBeat { |pattern, defaultWeight = 0.7, min = 0, max = 1|
        var seed = this.prGetPatternSeed(pattern);
        var weight = pattern[\weight] ?? defaultWeight;
        var rhythmWeight = (weight * 10).floor / 10;
        var pseqWeight = weight - rhythmWeight * 10;
        var test;

        var rhythmSeq = { |weight|
            Array.fill(16, { [min, max].wchoose([1 - weight, weight]) });
        };

        thisThread.randSeed = seed;

        if (pseqWeight > 0) {
            var seq1 = Pseq(rhythmSeq.(rhythmWeight), 1);
            var seq2 = Pseq(rhythmSeq.(rhythmWeight + 0.1), 1);

            ^[Pwrand([seq1, seq2], [1 - pseqWeight, pseqWeight])];
        };

        ^rhythmSeq.(weight);
    }

    *prCreateBeatSet { |amp, pattern|
        var list = pattern[\beatSet].collect { |step|
            if (step >= 0)
            { step = amp };

            step;
        };

        ^Pseq(list, inf);
    }

    *prCreateRhythmBeat { |amp, pattern|
        var beats;

        if (pattern[\beatSet].isNil)
        { beats = this.prCreateBeat(pattern, max: amp) }
        { beats = this.prCreateBeatSet(amp, pattern) };

        last[pattern[\id]][\beats] = beats;
        ^beats;
    }

    *prCreateBeatRest { |pattern|
        var dur = pattern[\dur];

        if (pattern[\rest].notNil) {
            dur = Pseq([Pn(dur, repeats: 15), pattern[\rest] + dur], inf);
        };

        ^dur;
    }

    *prCreateFillFromBeat { |amp, pattern|
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
            var beat = last[previousPatternId][\totalBeats] ?? Array.fill(steps, 0);
            (beat + invertBeat) collect: { |step| step.clip(0, 1) };
        };

        if (last[previousPatternId].notNil)
        { previousBeats = last[previousPatternId][\beats] ?? last[previousPatternId][\totalBeats] };

        if (previousBeats.isNil)
        { ^amp };

        invertBeat = getInvertBeat.(previousBeats, pattern[\amp]);
        totalBeat = getTotalBeat.(invertBeat);

        last[pattern[\id]].putAll([\totalBeats, totalBeat]);
        ^totalBeat;
    }
}
