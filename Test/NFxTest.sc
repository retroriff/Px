FxTest : PxTest {
    tearDown {
        Fx.mixer = Dictionary.new;
        ^super.tearDown;
    }

    test_clear {
        Px.blp.reverb;
        Fx.clear;

        this.assertEquals(
            Fx.activeEffects.size,
            0,
            "👀 All FX have been deleted from activeEffects",
        );
    }

    text_name {
        Fx(\px2).reverb(1, 1, 1);

        this.assertEquals(
            Fx.activeEffects[\px2],
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
            Fx(\px).activeArgs[\px][\reverb],
            [1, 1],
            "👀 FX receives args",
        );

        this.assertEquals(
            Fx(\px).mixer[\px][\reverb],
            1,
            "👀 FX sets mixer",
        );

        Px.reverb(Nil);

        this.assertEquals(
            Fx.activeEffects[\px].size,
            0,
            "👀 FX deleted from activeEffects",
        );
    }

    test_vst {
        Px.vst(0.3, "ValhallaFreqEcho");

        this.assertEquals(
            Fx.activeEffects[\px],
            [\vst],
            "👀 VST FX is enabled",
        );

        this.assertEquals(
            Fx.vstController.class,
            VSTPluginNodeProxyController,
            "👀 VST controller can receive params",
        );

        Px.vst(Nil);

        this.assertEquals(
            Fx.activeEffects[\px].size,
            0,
            "👀 VST FX deleted from activeEffects",
        );
    }

    test_presetsPath {
        this.assertEquals(
            Fx.presetsPath.isString,
            true,
            "👀 Presets path has been initialized",
        );
    }
}