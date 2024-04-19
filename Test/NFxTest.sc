/*
TODO: Unit tests
🔴 Px methods: blp, clear, delay, hpf, lpf, wah, prCreatePatternFx, PbindFx
🔴 Event methods: delay, hpf, lpf, reverb, wah
*/

NfxTest : PxTest {
    tearDown {
        Nfx.mixer = Dictionary.new;
        ^super.tearDown;
    }

    test_fx {
        Px.reverb(1, 1, 1);

        this.assertEquals(
            Ndef(\px)[1].key,
            \filterIn,
            "👀 Reverb FX is enabled",
        );

        this.assertEquals(
            Nfx(\px).activeArgs[\reverb],
            [1, 1],
            "👀 FX receives args",
        );

        this.assertEquals(
            Nfx(\px).mixer[\reverb],
            1,
            "👀 FX sets mixer",
        );

        Px.reverb(Nil);

        this.assertEquals(
            Nfx.activeEffects.size,
            0,
            "👀 FX deleted from activeEffects",
        );
    }

    test_vst {
        Px.vst(0.3, "ValhallaFreqEcho");

        this.assertEquals(
            Nfx.activeEffects,
            [\vst],
            "👀 VST FX is enabled",
        );

        this.assertEquals(
            Nfx.vstController.class,
            VSTPluginNodeProxyController,
            "👀 VST controller can receive params",
        );

        Px.vst(Nil);

        this.assertEquals(
            Nfx.activeEffects.size,
            0,
            "👀 VST FX deleted from activeEffects",
        );
    }

    test_presetsPath {
        this.assertEquals(
            Nfx.presetsPath.isString,
            true,
            "👀 Presets path has been initialized",
        );
    }
}