TestPxEvent : UnitTest {
    var expectedResult;
    var result;

    test_a {
        result = (i: \bd).a(0.5);
        expectedResult = (i: \bd, amp: 0.5);

        this.assertEquals(
            result,
            expectedResult,
            "👀 A adds amp to event",
        );
    }

    test_amp {
        result = (i: \bd).amp(0.5);
        expectedResult = (i: \bd, amp: 0.5);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Amp is added to event",
        );
    }

    test_beat {
        result = (i: \bd).beat(76, 4, [0, 1, 0, 1]);
        expectedResult = (i: \bd, \beat: true, \beatSet: [0, 1, 0, 1], \rest: 4, \seed: 76);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Beat is added to event",
        );
    }

    test_dur {
        result = (i: \bd).dur(4);
        expectedResult = (i: \bd, \dur: 4);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Dur is added to event",
        );
    }

    test_euclid {
        result = (i: \bd).euclid(3, 5);
        expectedResult = (i: \bd, \euclid: [3, 5]);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Euclid ryhthm is added to event",
        );
    }

    test_fill {
        result = (i: \bd).fill(4);
        expectedResult = (i: \bd, \fill: true, \rest: 4);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Fill is added to event",
        );
    }

    test_human {
        result = (i: \bd).human;
        expectedResult = (i: \bd, \human: 0.1);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Default human is added to event",
        );

        result = (i: \bd).human(0.5);
        expectedResult = (i: \bd, \human: 0.5);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Custom human is added to event",
        );
    }

    test_fade {
        result = (i: \bd).in(10);
        expectedResult = (i: \bd, \fade: [\in, 10]);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Fade \\in is added to event",
        );

        result = (i: \bd).out(5);
        expectedResult = (i: \bd, \fade: [\out, 5]);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Fade \\out is added to event",
        );
    }

    test_pan {
        result = (i: \bd).pan(1);
        expectedResult = (i: \bd, \pan: 1);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Fade in is added to event",
        );
    }

    test_rotate {
        result = (i: \bd).rotate;
        expectedResult = (i: \bd, \pan: \rotate);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Rotate pan is added to event",
        );
    }

    test_seed {
        result = (i: \bd).seed(76);
        expectedResult = (i: \bd, \seed: 76);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Seed is added to event",
        );
    }

    test_solo {
        result = (i: \bd).solo;
        expectedResult = (i: \bd, \solo: true);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Solo is added to event",
        );
    }

    test_weight {
        result = (i: \bd).weight(0.5);
        expectedResult = (i: \bd, \weight: 0.5);

        this.assertEquals(
            result,
            expectedResult,
            "👀 Weight is added to event",
        );
    }
}
