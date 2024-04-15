PxTest : UnitTest {
    var expectedResult;

    prGetNames {
        // We play first \px2 so classvar "lastName" is \px
        ^[\px2, \px];
    }

    setUp {
        var names = this.prGetNames;
        ~isUnitTestRunning = true;

        names do: { |name|
            if (name == \px)
            { name = nil };

            Px([
                (i: \bd).amp(0.5),
                (i: \sn, dur: 1/4),
            ], name);
        };
    }

    tearDown {
        this.prGetNames do: { |name| Px.stop(name) };
        Px.chorusPatterns = Dictionary.new;
        Px.lastPatterns = Dictionary.new;
        Ndef.clear;
        Pdef.clear;
        TempoClock.default.tempo = 110 / 60;
        ~isUnitTestRunning = false;
    }

    // Params
    test_playPx {
        var expectedResult = Dictionary.new;
        var names = this.prGetNames;

        names do: { |name|
            expectedResult.add(name -> [
                (i: \bd, amp: 0.5),
                (i: \sn, dur: 0.25)
            ]);
        };

        this.assertEquals(
            Px.lastPatterns,
            expectedResult,
            "👀 Ndef(\\px) is playing"
        );

        this.ifAsserts(
            Px.lastPatterns == expectedResult,
            "👀 Patterns are correctly generated",
            names do: { |name|
                this.assert(
                    Ndef(name).isPlaying,
                    "👀 Ndef(\\" ++ name ++ ") is playing"
                );
            };

            this.assertEquals(
                Px.nodeProxy.size,
                2,
                "👀 Dictionary nodeProxy has correct items"
            );
        );
    }

    test_lastName {
        this.assertEquals(
            Px.lastName,
            \px,
            "👀 Px.lastName stores the last played instance name",
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
    test_buf {
        var buf = Px.buf("fm", 0);
        this.assertEquals(
            buf.asString.contains("Buffer"),
            true,
            "👀 Buf returns a buffer",
        );
    }

    test_chorus {
        Px([(i: \bd).amp(0.5)]).save;
        Px.chorus;
        expectedResult = Dictionary[
            \px -> [(i: \bd, amp: 0.5)]
        ];

        this.assertEquals(
            Px.chorusPatterns,
            expectedResult,
            "👀 Chorus to default \\px is saved and played",
        );

        Px([(i: \bd).amp(0.5)], \px2).save;
        Px.chorus(\px2);
        expectedResult = Dictionary[
            \px -> [(i: \bd, amp: 0.5)],
            \px2 -> [(i: \bd, amp: 0.5)]
        ];

        this.assertEquals(
            Px.chorusPatterns,
            expectedResult,
            "👀 Chorus to \\px2 is also saved and played",
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
        Px.release(0, \px2);

        this.prGetNames do: { |name|
            this.assertEquals(
                Ndef(name).isPlaying,
                false,
                "👀 Px instance \\" ++ name ++ " has been stopped",
            );
        }
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
            1.0,
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

        this.prGetNames do: { |name|
            var volName = name;

            if (name == \px)
            { volName = nil };

            Px.vol(expectedResult, volName);

            this.assertEquals(
                Ndef(name).vol,
                expectedResult,
                "👀 Volume for \\" ++ name ++ " is " ++ expectedResult,
            );
        };
    }
}
