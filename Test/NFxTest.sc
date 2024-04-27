NfxTest : PxTest {
    tearDown {
        Nfx.mixer = Dictionary.new;
        ^super.tearDown;
    }

    test_clear {
        Px.blp.reverb;
        Nfx.clear;

        this.assertEquals(
            Nfx.activeEffects.size,
            0,
            "👀 All FX have been deleted from activeEffects",
        );
    }

    text_name {
        Nfx(\px2).reverb(1, 1, 1);

        this.assertEquals(
            Nfx.activeEffects[\px2],
            [\gverb],
            "👀 Enables FX to custom proxy name",
        );
    }

    test_fx {
        Px.reverb(1, 1, 1);

        this.assertEquals(
            Ndef(\px)[1].key,
            \filterIn,
            "👀 Reverb FX is enabled",
        );

        this.assertEquals(
            Nfx(\px).activeArgs[\px][\reverb],
            [1, 1],
            "👀 FX receives args",
        );

        this.assertEquals(
            Nfx(\px).mixer[\px][\reverb],
            1,
            "👀 FX sets mixer",
        );

        Px.reverb(Nil);

        this.assertEquals(
            Nfx.activeEffects[\px].size,
            0,
            "👀 FX deleted from activeEffects",
        );
    }

    test_vst {
        Px.vst(0.3, "ValhallaFreqEcho");

        this.assertEquals(
            Nfx.activeEffects[\px],
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
            Nfx.activeEffects[\px].size,
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