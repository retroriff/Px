/*
TODO: Initialize Ndef(\ns);
TODO: Add \root support;
TODO: Trace debug
TODO: Unit tests
*/

Ns {
    classvar defaultScale;
    classvar waveList;

    *initClass {
        defaultScale = \scriabin;
        waveList = [\pulse, \saw, \sine, \triangle];
    }

    *new { |pattern|
        var createDefaults = {
            var defaultPattern = (
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

            defaultPattern.keys do: { |key|
                pattern[key] = pattern[key] ?? defaultPattern[key];
            };
        };

        var calculateOctaves = {
            var degrees = pattern[\degree];
            var octaves = pattern[\octave];
            var maxLen = max(degrees.size, octaves.size);
            var results = [];
            var degIndex = 0;
            var octIndex = 0;

            while ({ results.size < maxLen }) {
                var deg = degrees[degIndex];
                var oct = octaves[octIndex];
                results = results.add(deg + (oct * 12));

                degIndex = (degIndex + 1) % degrees.size;
                octIndex = (octIndex + 1) % octaves.size;
            };

            pattern[\degree] = results;
        };

        var convertToArray = { |key|
            if (pattern[key].isNumber)
            { pattern[key] = [pattern[key]] };
        };

        var createEuclid = {
            if (pattern[\euclid].notNil) {
                pattern[\dur] = Bjorklund2(*pattern[\euclid]) * pattern[\dur][0]
            };
        };

        var setArraySize = { |key|
            var keySizeName = (key ++ "Size").asSymbol;
            pattern.putAll([keySizeName, pattern[key].size]);
        };

        var setDefaultControls = {
            var keys = [
                \amp,
                \chord,
                \chordSize,
                \degree,
                \degreeSize,
                \dur,
                \durSize,
                \env,
                \vcf
            ];

            keys do: { |key| this.setControl(key, pattern[key]) };
        };

        createDefaults.value;
        [\chord, \degree, \dur, \octave] do: { |key| convertToArray.(key) };
        calculateOctaves.value;
        [\chord, \degree, \dur] do: { |key| setArraySize.(key) };
        createEuclid.value;
        this.setScaleControl(pattern[\scale]);
        this.setWaveControl(pattern[\wave]);
        setDefaultControls.value;
        Ndef(\ns).play;
    }

    *release { |fadeTime = 10, name|
        name = name ?? \ns;
        Ndef(\ns).free(fadeTime);
    }

    *set { |key, value|
        case
        { key == \scale }
        { ^this.setScaleControl(value) }

        { key == \wave }
        { ^this.setWaveControl(value) };

        ^this.setControl(key, value);
    }

    *setControl { |key, value|
        Ndef(\ns).set(key, value);
    }

    *setScaleControl { |value|
        var buffer;

        if (value == \default)
        { value = defaultScale };

        buffer = Buffer.loadCollection(Server.default, Scale.at(value));
        Ndef(\ns).set(\scale, buffer);
    }

    *setWaveControl { |patternWave|
        waveList do: { |wave|
            var value = 0;

            if (patternWave == wave)
            { value = 1 };

            this.setControl(wave, value);
        };
    }
}