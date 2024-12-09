/*
TODO: Refactor Ndef(\sx) so it does not consume resorces when it it initialized.
It can be tested adding "poll" to any trigger.
A solution could be to convert to SynthDef.
*/
Sx {
    classvar <defaultEvent;
    classvar <defaultScale;
    classvar <>last;
    classvar synth;
    classvar <waveList;

    *initClass {
        defaultScale = \scriabin;
        defaultEvent = (
            amp: 1,
            chord: [0],
            degree: [0],
            dur: [1],
            env: 0,
            octave: [0],
            root: 0,
            scale: defaultScale,
            vcf: 1,
            wave: \saw;
        );
        last = Event.new;
        waveList = [\pulse, \saw, \sine, \triangle];
    }

    *new { |event, fadeTime|
        event = this.prCreateDefaultArgs(event ?? Event.new);
        last = event.copy;

        event.keysValuesDo { |key, value|
            this.set(key, value);
        };

        if (Ndef(\sx).isPlaying.not)
        { this.play(fadeTime) };

        // Ndef(\sx).play(fadeTime: fadeTime);
/*        Ndef(\sx, {
            In.ar(~sxBus, 2);
        }).play(fadeTime: fadeTime);*/
    }

    *clear {
        this.initClass;
    }

    *loadSynth {
        var path = "../SynthDefs/Sx.scd";
        var file = PathName((path).resolveRelative);
        File.readAllString(file.fullPath);
        file.fullPath.load;
    }

    *play { |fadeTime|
        synth = Synth(\sx);
        Ndef(\sx, { In.ar(~sxBus, 2) }).play(fadeTime: fadeTime);
    }

    *qset { |key, value, lag|
        this.set(key: key, value: value, lag: lag, quant: true);
    }

    *release { |fadeTime = 10|
        Ndef(\sx).free(fadeTime);
    }

    *set { |key, value, lag, quant|
        var arraySizePair = Array.new;
        var pairs;

        last.putAll([key, value]);
        value = this.prConvertToArray(key, value);
        pairs = [key, value];

        case
        { key == \degree } {
            var octave = last[\octave] ?? defaultEvent[\octave];
            octave = this.prConvertToArray(\octave, octave);
            pairs = this.prGenerateDegree(value, octave);
        }

        { key == \euclid }
        { pairs = this.prGenerateEuclid(value) }

        { key == \octave } {
            var degree = last[\degree] ?? defaultEvent[\degree];
            degree = this.prConvertToArray(\degree, degree);
            pairs = this.prGenerateDegree(degree, value);
        }

        { key == \root } {
            var degree = this.prConvertToArray(\degree, last[\degree]);
            var octave = this.prConvertToArray(\octave, last[\octave]);
            pairs = this.prGenerateDegree(degree, octave, value);
        }

        { key == \scale }
        { pairs = this.prGenerateScale(value) }

        { key == \wave }
        { pairs = this.prGenerateWave(value) };

        arraySizePair = this.prGenerateArraySize(pairs[0], pairs[1]);
        pairs = pairs ++ [\lag, lag ?? 0] ++ arraySizePair;

        if (quant.isNil)
        { ^this.prSet(pairs) }
        { ^this.prCreateQuantizedSet(pairs) };
    }

    *stop { |fadeTime|
        Ndef(\sx).stop(fadeTime);
    }

    *vol { |value|
        ^Ndef(\sx).vol_(value);
    }

    *prCreateQuantizedSet { |pairs|
        var clock = TempoClock.default;
        var nextBeat = clock.nextTimeOnGrid(4);

        clock.schedAbs(nextBeat, {
            this.prSet(pairs);
        });
    }

    *prGenerateDegree { |degree, octave, root|
        var maxLen = max(degree.size, octave.size);
        var result = Array.new;
        var degIndex = 0;
        var octIndex = 0;
        var dur = last[\dur] ?? defaultEvent[\dur];

        while ({ result.size < maxLen }) {
            var deg = degree[degIndex];
            var oct = octave[octIndex].clip(-2, 2);

            result = result.add(deg + (oct * 12));
            if (oct == -0)
            { oct = 0 };

            degIndex = (degIndex + 1) % degree.size;
            octIndex = (octIndex + 1) % octave.size;
        };

        root = root ?? last[\root] ?? defaultEvent[\root];
        ^[\degree, result + root.clip(-12, 12), \dur, dur];
    }

    *prCreateDefaultArgs { |event|
        defaultEvent.keys do: { |key|
            event[key] = event[key] ?? defaultEvent[key];
        };

        ^event;
    }

    *prGenerateEuclid { |value|
        var dur = this.prConvertToArray(\dur, last[\dur]);
        var euclid = Bjorklund2(*value) * dur[0];

        ^[\dur, euclid];
    }

    *prConvertToArray { |key, value|
        if (this.prShouldBeArray(key) and: value.isNumber)
        { value = [value] };

        ^value;
    }

    *prGenerateArrayName { |key|
        ^(key ++ "Size").asSymbol;
    }

    *prGenerateArraySize { |key, value|
        var pairs = Array.new;

        if (this.prShouldBeArray(key))
        { pairs = [this.prGenerateArrayName(key), value.size] };

        ^pairs;
    }

    *prGenerateScale { |value|
        var buffer;

        if (value == \default)
        { value = defaultScale };

        buffer = Buffer.loadCollection(Server.default, Scale.at(value));

        ^[\scale, buffer];
    }

    *prGenerateWave { |value|
        var pairs = Array.new;

        waveList do: { |wave|
            var waveValue = 0;

            if (value == wave)
            { waveValue = 1 };

            pairs = pairs ++ [wave, waveValue];
        };

        ^pairs;
    }

    *prSet { |pairs|
        synth.set(*pairs);
    }

    *prShouldBeArray { |key|
        var arrayKeys = [\chord, \degree, \dur, \octave];

        ^arrayKeys.includes(key);
    }

    *prUpdateLast { |key, value|
        last.putAll([key, value]);
    }
}