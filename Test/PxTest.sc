/*
TODO: Hide post messages when testing
*/

PxTest : UnitTest {
    var expectedResult;

    setUp {
        Px.lastPatterns = Dictionary.new;
        Px([
            (i: \bd).amp(0.5),
            (i: \sn, dur: 1/4),
        ]);
    }

    tearDown {
        Px.stop;
        Ndef.clear;
        Pdef.clear;
        TempoClock.default.tempo = 110 / 60;
    }

    // Params
    test_playPx {
        expectedResult = Dictionary[(\px -> [
            (i: \bd, amp: 0.5),
            (i: \sn, dur: 0.25)
        ])];

        this.ifAsserts(
            Px.lastPatterns == expectedResult,
            "👀 Patterns are correctly generated",
            this.assert(
                Ndef(\px).isPlaying,
                "👀 Ndef(\\px) is playing"
            );
            this.assert(
                Px.nodeProxy.size > 0,
                "👀 Dictionary nodeProxy is not empty"
            );
        );
    }

    test_currentName {
        Px([(i: \bd)], \test);

        this.assertEquals(
            Px.currentName,
            \test,
            "👀 Px has a custom name",
        );
    }

    test_quant {
        this.assertEquals(
            Pdef(\px).quant,
            4,
            "👀 Default quant is 4",
        );

        Px([(i: \bd)], quant: 8);

        this.assertEquals(
            Pdef(\px).quant,
            8,
            "👀 Custom quant is received",
        );
    }


    // Methods
    test_chorus {
        Px([(i: \bd).amp(0.5)]).save;
        Px.chorus;
        expectedResult = [(i: \bd, amp: 0.5)];

        this.assertEquals(
            Px.chorusPatterns,
            expectedResult,
            "👀 Chorus is saved",
        );
    }

    test_loadSamples {
        this.assert(
            Px.samplesDict.size > 0,
            "👀 Samples dictionary has been initialized by set up file",
        );
    }

    test_release {
        Px.release(0);

        this.assertEquals(
            Ndef(\px).isPlaying,
            false,
            "👀 New seed is saved",
        );
    }

    test_shuffle {
        Px([(i: \bd).beat]).shuffle;

        this.assert(
            Px.seeds.size > 0,
            "👀 New seed is saved",
        );
    }

    test_tempo {
        Px.tempo(60);

        this.assertEquals(
            TempoClock.tempo,
            1,
            "👀 Tempo has been set",
        );
    }

    test_vol {
        this.assertEquals(
            Ndef(\px).vol,
            1,
            "👀 Default volume is 1",
        );

        expectedResult = 0.5;
        Px.vol(expectedResult);

        this.assertEquals(
            Ndef(\px).vol,
            expectedResult,
            "👀 Volume is 0.5",
        );
    }
}
