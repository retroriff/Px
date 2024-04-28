/*
TODO: Initialize Ndef(\ns);
TODO: Add \root support;
TODO: Set initial default note;
*/

Ns {
    classvar waveList;

    *initClass {
        waveList = [\pulse, \saw, \sine, \triangle];
    }

    *new { |pattern|
        var createDefaults = {
            var defaultPattern = (
                amp: 1,
                degree: [0],
                dur: [1],
                env: 0,
                octave: [0],
                scale: \dorian,
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

        var setControl = { |key, value|
            Ndef(\ns).set(key, value);
        };

        var setDefaultControls = {
            var keys = [\amp, \degree, \degreeSize, \dur, \durSize, \env, \vcf];
            pattern[\degree].postln;
            keys do: { |key| setControl.(key, pattern[key]) };
        };

        var setWaveControl = {
            waveList do: { |wave|
                var value = 0;

                if (pattern[\wave] == wave)
                { value = 1 };

                setControl.(wave, value)
            };
        };

        createDefaults.value;
        [\degree, \dur, \octave] do: { |key| convertToArray.(key) };
        calculateOctaves.value;
        [\degree, \dur] do: { |key| setArraySize.(key) };
        createEuclid.value;
        setWaveControl.value;
        setDefaultControls.value;
        Ndef(\ns).play;
    }

    *release { |fadeTime = 10, name|
        name = name ?? \ns;
        Ndef(\ns).free(fadeTime);
    }
}