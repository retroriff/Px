/*
TODO: Add \root support;
*/

Ns {
    classvar defaultScale;
    classvar <lastControls;
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

            lastControls = defaultPattern;
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
                var oct = octaves[octIndex].clip(-3, 3);
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

            keys do: { |key| this.prSetControl(key, pattern[key]) };
        };

        createDefaults.value;
        [\chord, \degree, \dur, \octave] do: { |key| convertToArray.(key) };
        calculateOctaves.value;
        [\chord, \degree, \dur] do: { |key| setArraySize.(key) };
        createEuclid.value;
        this.prSetScaleControl(pattern[\scale]);
        this.prSetWaveControl(pattern[\wave]);
        setDefaultControls.value;
        Ndef(\ns).play;
    }

    *loadSynth {
        var path = "../SynthDefs/NdefNs.scd";
        var file = PathName((path).resolveRelative);
        File.readAllString(file.fullPath);
        file.fullPath.load;
    }

    *release { |fadeTime = 10, name|
        name = name ?? \ns;
        Ndef(\ns).free(fadeTime);
    }

    *set { |key, value|
        case
        { key == \scale }
        { ^this.prSetScaleControl(value) }

        { key == \wave }
        { ^this.prSetWaveControl(value) };

        ^this.prSetControl(key, value);
    }

    *prSetControl { |key, value|
        this.prUpdateLastControls(key, value);
        Ndef(\ns).set(key, value);
    }

    *prSetScaleControl { |value|
        var buffer;

        if (value == \default)
        { value = defaultScale };

        buffer = Buffer.loadCollection(Server.default, Scale.at(value));
        this.prUpdateLastControls(\scale, value);
        Ndef(\ns).set(\scale, buffer);
    }

    *prSetWaveControl { |patternWave|
        waveList do: { |wave|
            var value = 0;

            if (patternWave == wave)
            { value = 1 };

            this.prSetControl(wave, value);
        };
    }

    *prUpdateLastControls { |key, value|
        lastControls.putAll([key, value]);
    }
}