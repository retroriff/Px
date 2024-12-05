SxTest : UnitTest {
    var expectedResult;

    setUp {
        Sx(
            (
                amp: 0.5,
                chord: [0, 1, 2],
                degree: [0, 1, 2],
                dur: 1/4,
                env: 1,
                euclid: [3, 5],
                octave: [0, 1, -1],
                scale: \dorian,
                vcf: 0,
                wave: \pulse,
            )
        );
    }

    tearDown {
        Ndef(\sx).free;
    }

    test_controls {
        var expectedResult = (
            \amp: 0.5,
            \chord: [ 0, 1, 2 ],
            \chordSize: 3,
            \degree: [ 0, 13, -10 ],
            \degreeSize: 3,
            \dur: [ 0.5, 0.5, 0.25 ],
            \durSize: 1,
            \env: 1,
            \octave: [0],
            \pulse: 1,
            \saw: 0,
            \scale: \dorian,
            \sine: 0,
            \triangle: 0,
            \vcf: 0,
            \wave: \saw
        );

        this.assertEquals(
            Sx.last,
            expectedResult,
            "👀 Controls are generated correctly",
        );
    }

    test_ndef_isPlaying {
        this.assertEquals(
            Ndef(\sx).isPlaying,
            true,
            "👀 Synth is playing"
        );
    }


    test_ndef_parameters {
        var parameters = Dictionary[
            \amp -> 0.5,
            \chord -> [ 0, 1, 2 ],
            \chordSize -> 3,
            \degree -> [ 0, 13, -10 ],
            \degreeSize -> 3,
            \dur -> [ 0.5, 0.5, 0.25 ],
            \durSize -> 1,
            \env -> 1,
            \pulse -> 1,
            \saw -> 0,
            \sine -> 0,
            \triangle -> 0,
            \vcf -> 0,
        ];

        parameters.keys do: { |key|
            this.assertEquals(
                Ndef(\sx).get(key),
                parameters[key],
                "👀 Ndef(\\" ++ key ++ ") has correct value"
            );
        };
    }

    test_release {
        Sx.release;

        this.assertEquals(
            Ndef(\sx).isPlaying,
            false,
            "👀 Synth is released by 'release' method"
        );
    }

    test_set {
        Sx.set(\wave, \sine);

        this.assertEquals(
            Ndef(\sx).get(\sine),
            1,
            "👀 Synth receives 'set' method"
        );
    }
}
