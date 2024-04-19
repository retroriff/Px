/*
TODO: Unit tests
🔴 Px methods: PbindFx
🔴 FX event methods
*/

PxFxTest : PxTest {
    test_fx {
        Px([(i: \bd).reverb.hpf]);

        this.assertEquals(
            Px.lastFormattedPatterns[\px][0][\fx][0].includes(\reverb),
            true,
            "👀 Pattern first FX is enabled",
        );

        this.assertEquals(
            Px.lastFormattedPatterns[\px][0][\fx][1].includes(\hpf),
            true,
            "👀 Pattern second FX is enabled",
        );


        this.assertEquals(
            Px.lastFormattedPatterns[\px][0][\fx][0].includes(\decayTime),
            true,
            "👀 Pattern reverb FX contains \\decaytime",
        );
    }
}
