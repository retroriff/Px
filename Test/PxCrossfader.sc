CrossfaderTest : PxTest {
    test_fx {
        Px([(i: \bd, amp: 0)], \a);
        Px([(i: \sn, amp: 1)], \b);
        Crossfader(\a, \b, 0.1);

        0.2.wait;

        this.assertEquals(
            Ndef(\a).vol,
            0,
            "👀 First item faded out",
        );

        this.assertEquals(
            Ndef(\b).vol,
            1,
            "👀 Second item faded in",
        );
    }
}
