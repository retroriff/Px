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

    *new { |pattern|
        pattern = this.prCreateDefaultArgs(pattern);
        last = pattern.copy;

        pattern.keysValuesDo { |key, value|
            this.set(key, value);
        };

        Ndef(\ns).play;
    }

    *loadSynth {
        var path = "../SynthDefs/Ns.scd";
        var file = PathName((path).resolveRelative);
        File.readAllString(file.fullPath);
        file.fullPath.load;
    }

    *play { |fadeTime|
        Ns(last);
        Ndef(\ns).play(fadeTime: fadeTime);
    }

    *release { |fadeTime = 10, name|
        name = name ?? \ns;
        Ndef(name).free(fadeTime);
    }

    *set { |key, value, lag|
        var arraySizePair = Array.new;
        var setPair;

        last.putAll([key, value]);
        value = this.prConvertToArray(key, value);
        setPair = [key, value];

        case
        { key == \degree } {
            var octave = this.prConvertToArray(\octave, last[\octave]);
            setPair = this.prGenerateDegree(value, octave, last[\root]);
        }

        { key == \euclid }
        { setPair = this.prGenerateEuclid(value) }

        { key == \octave } {
            var degree = this.prConvertToArray(\degree, last[\degree]);
            setPair = this.prGenerateDegree(degree, value, last[\root]);
        }

        { key == \root } {
            var degree = this.prConvertToArray(\degree, last[\degree]);
            var octave = this.prConvertToArray(\octave, last[\octave]);
            setPair = this.prGenerateDegree(degree, octave, value);
        }

        { key == \scale }
        { setPair = this.prGenerateScale(value) }

        { key == \wave }
        { setPair = this.prGenerateWave(value) };

        arraySizePair = this.prGenerateArraySize(setPair[0], setPair[1]);

        ^this.prSetControl(setPair ++ [\lag, lag] ++ arraySizePair);
    }

    *stop { |name|
        name = name ?? \ns;
        Ndef(name).stop;
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

    *prSetControl { |pairs|
        Ndef(\ns).set(*pairs);
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

    *prShouldBeArray { |key|
        var arrayKeys = [\chord, \degree, \dur, \octave];

        ^arrayKeys.includes(key);
    }

    *prUpdateLast { |key, value|
        last.putAll([key, value]);
    }
}