/*
TODO: Fix Ns.set(\euclid, [3, 5]);
TODO: Add \root support;
*/

Ns {
    classvar <arrayKeys;
    classvar <defaultEvent;
    classvar <defaultScale;
    classvar <>last;
    classvar <waveList;

    *initClass {
        arrayKeys = [\chord, \degree, \dur, \octave];
        defaultScale = \scriabin;
        defaultEvent = (
            amp: 1,
            chord: [0],
            degree: [0],
            dur: [1],
            env: 0,
            octave: [0],
            scale: defaultScale,
            vcf: 1,
            wave: \saw;
        );
        last = Event.new;
        waveList = [\pulse, \saw, \sine, \triangle];
    }

    *new { |pattern|
        pattern = this.prCreateDefaultArgs(pattern);

        pattern.keys do: { |key|
            this.set(key);
        };

        Ndef(\ns).play;
    }

    *loadSynth {
        var path = "../SynthDefs/NdefNs.scd";
        var file = PathName((path).resolveRelative);
        File.readAllString(file.fullPath);
        file.fullPath.load;
    }

    *play { |fadeTime|
        Ndef(\ns).play(fadeTime: fadeTime);
    }

    *release { |fadeTime = 10, name|
        name = name ?? \ns;
        Ndef(name).free(fadeTime);
    }

    *set { |key, value|
        var arraySizePair = Array.new;
        var setPair = [key, value];

        last.putAll([key, value]);

        value = this.prConvertToArray(key, value);

        case
        { key == \degree }
        { setPair = this.prGenerateDegree(degree: value, octave: last[\octave]) }

        { key == \euclid }
        { setPair = this.prGenerateEuclid(value) }

        { key == \octave }
        { setPair = this.prGenerateDegree(degree: last[\degree], octave: value) }

        { key == \scale }
        { setPair = this.prGenerateScale(value) }

        { key == \wave }
        { setPair = this.prGenerateWave(value) };

        arraySizePair = this.prGenerateArraySize(key, value);

        ^this.prSetControl(setPair ++ arraySizePair);
    }

    *stop { |name|
        name = name ?? \ns;
        Ndef(name).stop;
    }

    *prGenerateDegree { |degree, octave|
        var maxLen = max(degree.size, octave.size);
        var result = Array.new;
        var degIndex = 0;
        var octIndex = 0;

        while ({ result.size < maxLen }) {
            var deg = degree[degIndex];
            var oct = octave[octIndex].clip(-2, 2);

            result = result.add(deg + (oct * 12));

            if (oct == -0)
            { oct = 0 };

            degIndex = (degIndex + 1) % degree.size;
            octIndex = (octIndex + 1) % octave.size;
        };

        ^[\degree, result];
    }

    *prCreateDefaultArgs { |event|
        defaultEvent.keys do: { |key|
            event[key] = event[key] ?? defaultEvent[key];
        };

        ^event;
    }

    *prGenerateEuclid { |value|
        var dur = Bjorklund2(*value) * last[\dur][0];

        ^[\dur, dur];
    }

    *prConvertToArray { |key, value|
        var shouldBeArray = arrayKeys.includes(key);

        if (shouldBeArray and: value.isNumber)
        { value = [value] };

        ^value;
    }

    *prGenerateArrayName { |key|
        ^(key ++ "Size").asSymbol;
    }

    *prGenerateArraySize { |key, value|
        ^[this.prGenerateArrayName(key), value.size];
    }

    *prSetControl { |key, value|
        this.prUpdateLast(key, value);

        Ndef(\ns).set(key, value);
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

            if (value == waveValue)
            { waveValue = 1 };

            pairs ++ [wave, waveValue];
        };

        ^pairs;
    }

    *prUpdateLast { |key, value|
        last.putAll([key, value]);
    }
}