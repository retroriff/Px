/*
TODO: Unit tests
🔴 Event methods
*/

PxEventTest : UnitTest {
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
}
