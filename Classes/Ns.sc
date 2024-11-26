/*
TODO: Fix Ns.set(\euclid, [3, 5]);
TODO: Add \root support;
*/

Ns {
    classvar <defaultEvent;
    classvar <defaultScale;
    classvar <last;
    classvar waveList;

    *initClass {
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
        last = pattern;
        pattern = this.prCreateDefaultArgs(pattern);
        pattern[\degree] = this.prCalculateOctaves(pattern[\octave], pattern[\degree]);

        [\chord, \degree, \dur] do: { |key|
            pattern.putAll(this.prSetArraySize(key, pattern[key]));
        };

        pattern = this.prCreateEuclid(pattern);

        this.prSetScaleControl(pattern[\scale]);
        this.prSetWaveControl(pattern[\wave]);
        this.prSetDefaultControls(pattern);

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
        last.putAll([key, value]);

        case
        { key == \degree }
        { value = this.prCalculateOctaves(octaves: last[\octave], degrees: value) }

        { key == \octave } {
            var octavesArray;
            key = \degree;
            value = this.prCalculateOctaves(octaves: value, degrees: last[\degree]);
            octavesArray = this.prSetArraySize(key, value);
            this.prSetControl(octavesArray[0], octavesArray[1]);
        }

        { key == \scale }
        { ^this.prSetScaleControl(value) }

        { key == \wave }
        { ^this.prSetWaveControl(value) };

        ^this.prSetControl(key, value);
    }

    *stop { |name|
        name = name ?? \ns;
        Ndef(name).stop;
    }

    *prCalculateOctaves { |octaves, degrees|
        var degreesArray = this.prConvertToArray(degrees);
        var octavesArray = this.prConvertToArray(octaves);
        var maxLen = max(degreesArray.size, octavesArray.size);
        var results = Array.new;
        var degIndex = 0;
        var octIndex = 0;

        while ({ results.size < maxLen }) {
            var deg = degreesArray[degIndex];
            var oct = octavesArray[octIndex].clip(-2, 2);

            results = results.add(deg + (oct * 12));

            if (oct == -0)
            { oct = 0 };

            degIndex = (degIndex + 1) % degreesArray.size;
            octIndex = (octIndex + 1) % octavesArray.size;
        };

        ^results;
    }

    *prCreateDefaultArgs { |event|
        defaultEvent.keys do: { |key|
            event[key] = event[key] ?? defaultEvent[key];
        };

        [\chord, \dur] do: { |key|
            event[key] = this.prConvertToArray(event[key]);
        };

        ^event;
    }

    *prCreateEuclid { |pattern|
        if (pattern[\euclid].notNil) {
            pattern[\dur] = Bjorklund2(*pattern[\euclid]) * pattern[\dur][0]
        };

        ^pattern;
    }

    *prConvertToArray { |key|
        if (key.isNumber)
        { key = [key] };

        ^key;
    }

    *prSetArrayName { |key|
        ^(key ++ "Size").asSymbol;
    }

    *prSetArraySize { |key, value|
        ^[this.prSetArrayName(key), value.size];
    }

    *prSetControl { |key, value|
        this.prUpdateLast(key, value);

        Ndef(\ns).set(key, value);
    }

    *prSetDefaultControls { |pattern|
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

        pattern.keys.postln;

        keys do: { |key|
            this.prSetControl(key, pattern[key]);
        };
    }

    *prSetScaleControl { |value|
        var buffer;

        if (value == \default)
        { value = defaultScale };

        buffer = Buffer.loadCollection(Server.default, Scale.at(value));
        this.prUpdateLast(\scale, value);

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

    *prUpdateLast { |key, value|
        last.putAll([key, value]);
    }
}