Ns {
    classvar <defaultEvent;
    classvar <defaultScale;
    classvar <>last;
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
        event = this.prCreateDefaultArgs(event);
        last = event.copy;

        event.keysValuesDo { |key, value|
            this.set(key, value);
        };

        Ndef(\ns).play(fadeTime: fadeTime);
    }

    *loadSynth {
        var path = "../SynthDefs/Ns.scd";
        var file = PathName((path).resolveRelative);
        File.readAllString(file.fullPath);
        file.fullPath.load;
    }

    *play { |fadeTime|
        Ndef(\ns).play;
        this.new(last ?? defaultEvent, fadeTime);
    }

    *qset { |key, value, lag|
        this.set(key: key, value: value, lag: lag, quant: true);
    }

    *release { |fadeTime = 10, name|
        name = name ?? \ns;
        Ndef(name).free(fadeTime);
    }

    *set { |key, value, lag, quant|
        var arraySizePair = Array.new;
        var pairs;

        last.putAll([key, value]);
        value = this.prConvertToArray(key, value);
        pairs = [key, value];

        case
        { key == \degree } {
            var octave = this.prConvertToArray(\octave, last[\octave]);
            pairs = this.prGenerateDegree(value, octave, last[\root]);
        }

        { key == \euclid }
        { pairs = this.prGenerateEuclid(value) }

        { key == \octave } {
            var degree = this.prConvertToArray(\degree, last[\degree]);
            pairs = this.prGenerateDegree(degree, value, last[\root]);
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
        pairs = pairs ++ [\lag, lag] ++ arraySizePair;

        if (quant.isNil)
        { ^this.prSet(pairs) }
        { ^this.prCreateQuantizedSet(pairs) };
    }

    *stop { |fadeTime|
        Ndef(\ns).stop(fadeTime);
    }

    *vol { |value|
        ^Ndef(\ns).vol_(value);
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

        while ({ result.size < maxLen }) {
            var deg = degree[degIndex];
            var oct = octave[octIndex].clip(-2, 2);

            result = result.add(deg + (oct * 12));
            if (oct == -0)
            { oct = 0 };

            degIndex = (degIndex + 1) % degree.size;
            octIndex = (octIndex + 1) % octave.size;
        };

        ^[\degree, result + root.clip(-12, 12)];
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
        Ndef(\ns).set(*pairs);
    }

    *prShouldBeArray { |key|
        var arrayKeys = [\chord, \degree, \dur, \octave];

        ^arrayKeys.includes(key);
    }

    *prUpdateLast { |key, value|
        last.putAll([key, value]);
    }
}