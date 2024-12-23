TestPxOsc : TestPx {
    test_listen {
        Px.listen;

        this.assertEquals(
            OSCdef.all[\px].notNil,
            true,
            "👀 Listening OSC messages",
        );
    }

    test_listenOff {
        Px.listenOff;

        this.assertEquals(
            OSCdef.all[\px].isNil,
            true,
            "👀 Stopped listening OSC messages",
        );
    }
}
