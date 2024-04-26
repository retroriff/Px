Ns {
    classvar waveList;

    *initClass {
        waveList = [\saw, \pulse];
    }

    *new { |pattern|
        var createDefaults = {
            var defaultPattern = (
                i: \saw,
                amp: 1,
                degree: [0],
                dur: [1],
                env: 0,
                scale: \dorian
            );

            defaultPattern.keys do: { |key|
                pattern[key] = pattern[key] ?? defaultPattern[key];
            };
        };

        var createDur = {
            if (pattern[\dur].isNumber)
            { pattern[\dur] = [pattern[\dur]] };
        };

        var createEuclid = {
            if (pattern[\euclid].notNil) {
                pattern[\dur] = Bjorklund2(*pattern[\euclid]) * pattern[\dur][0]
            };
        };

        var createWave = {
            waveList do: { |wave|
                var value = 0;

                if (pattern[\wave] == wave)
                { value = 1 };

                Ndef(\ns).set(wave, value);
            };
        };

        var setControls = {
            Ndef(\ns).set(\amp, pattern[\amp]);
            Ndef(\ns).set(\degree, pattern[\degree]);
            Ndef(\ns).set(\degreeSize, pattern[\degree].size);
            Ndef(\ns).set(\dur, pattern[\dur]);
            Ndef(\ns).set(\durSize, pattern[\dur].size);
            Ndef(\ns).set(\env, pattern[\env]);
        };

        createDefaults.value;
        createDur.value;
        createEuclid.value;
        createWave.value;
        setControls.value;
        Ndef(\ns).play;
    }

    *release { |fadeTime = 10, name|
        name = name ?? \ns;
        Ndef(\ns).free(fadeTime);
    }
}