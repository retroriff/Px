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
    }

    test_playPx {
        expectedResult = Dictionary[(\px -> [
            (i: \bd, amp: 0.5),
            (i: \sn, dur: 0.25)
        ])];

        this.ifAsserts(
            Px.lastPatterns == expectedResult,
            "👀 Patterns are correctly generated",
            this.assert(Ndef(\px).isPlaying, "👀 Ndef(\\px) is playing");
        );
    }

    test_samplesDict {
        this.assert(
            Px.samplesDict.size > 0,
            "👀 Samples dictionary has been generated",
        );
    }

    test_nodeProxy {
        this.assert(
            Px.nodeProxy.size > 0,
            "👀 Dictionary nodeProxy is not empty",
        );
    }

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

    test_seeds {
        Px([(i: \bd).beat]).shuffle;

        this.assert(
            Px.seeds.size > 0,
            "👀 Seed is saved",
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
}

