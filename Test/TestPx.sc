TestPx : UnitTest {
    var expectedResult;

    setUp {
        ~isUnitTestRunning = true;
        1 i: \bd dur: 1 amp: 0.5;
        2 i: \sn dur: 0.25;
    }

    tearDown {
        Px.stop;
        Ndef.clear;
        Pdef.clear;
        Px.chorusPatterns = Dictionary.new;
        Px.last = Dictionary.new;
        Px.lastFormatted = Dictionary.new;
        ~isUnitTestRunning = false;
    }

    // Params
    test_playPx {
        var expectedResult = Dictionary[
            \1 -> (i: \bd, id: \1, dur: 1, amp: 0.5),
            \2 -> (i: \sn, id: \2, dur: 0.25)
        ];

        this.assertEquals(
            Px.last,
            expectedResult,
            "👀 Ndef(\\px) is playing"
        );

        this.ifAsserts(
            Px.last == expectedResult,
            "👀 Patterns are correctly generated",
            this.assert(
                Ndef(\px).isPlaying,
                "👀 Ndef(\\px) is playing"
            );
        );
    }

    test_quant {
        this.assertEquals(
            Pdef(\px).quant,
            4,
            "👀 Default quant is 4",
        );

        Px((i: \bd, dur: 1), quant: 8);

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
        1 i: \cy amp: 0.5;
        Px.save;
        Px.chorus;
        expectedResult = Dictionary[
            \1 -> (i: \cy, amp: 0.5, id: \1),
            \2 -> (i: \sn, dur: 0.25, id: \2),
        ];

        this.assertEquals(
            Px.chorusPatterns,
            expectedResult,
            "👀 Chorus is saved and played",
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

        this.assertEquals(
            Ndef(\px).isPlaying,
            false,
            "👀 Px has been stopped",
        );
    }

    test_shuffle {
        1 i: \bd beat: 0.7;
        Px.shuffle;

        this.assert(
            Px.seeds.size > 0,
            "👀 New seed is saved",
        );
    }

    test_synthDef {
        var synthDef = Px.synthDef(\bd).asString;

        this.assertEquals(
            synthDef.contains("SynthDesc 'bd'"),
            true,
            "👀 Method synthDef returns control details",
        );
    }

    test_tempo {
        Px.tempo(60);

        this.assertEquals(
            TempoClock.tempo,
            1.0,
            "👀 Tempo has been set",
        );

        Px.tempo(117);
        Px.loadSynthDefs;
    }

    test_vol {
        expectedResult = 0.1;
        Px.vol(expectedResult);

        this.assertEquals(
            Ndef(\px).vol,
            expectedResult,
            "👀 Volume has been set",
        );
    }

    // Event functions
    test_amp {
        1 i: \bd dur: 1;

        this.assertEquals(
            Px.lastFormatted[\1][\amp],
            1,
            "👀 Default \\amp has been added",
        );
    }

    test_beat {
        1 i: \bd dur: 1 beat: 0.7;

        this.assertEquals(
            Px.lastFormatted[\1][\amp].class,
            Pseq,
            "👀 Beat generates an \\amp Pseq",
        );
    }

    test_dur {
        1 i: \bd dur: 1;

        this.assertEquals(
            Px.lastFormatted[\1][\dur],
            1,
            "👀 Default \\dur has been added",
        );
    }

    test_fill {
        1 i: \bd dur: 1 beat: 0.7;
        2 i: \sn fill: 1;

        Px.last[\2][\totalBeats];
        this.assertEquals(
            Px.last[\2][\totalBeats].isArray,
            true,
            "👀 Fill generates a \\totalBeats array",
        );
    }

    test_fade {
        1 i: \bd dur: 1 out: 10;
        2 i: \sn dur: 1 in: 0;

        expectedResult = Dictionary[
            \2 -> (i: \sn, id: \2, 'fade': [\in, 0.1]),
        ];

        this.assertEquals(
            Px.last,
            expectedResult,
            "👀 Fades in and out is deleted from last patterns.",
        );
    }

    test_human {
        1 i: \bd dur: 1 human: 1;

        this.assertEquals(
            Px.lastFormatted[\1][\lag].class,
            Pwhite,
            "👀 Human adds a lag pair to pattern",
        );
    }

    test_ids {
        1 i: \bd dur: 1;

        this.assertEquals(
            Px.lastFormatted[\1][\id],
            \1,
            "👀 Px generates ids",
        );
    }

    test_loop {
        1 loop: ["fm", 0];
        expectedResult = Px.lastFormatted[\1];

        this.assert(
            expectedResult[\buf].asString.contains("Buffer"),
            "👀 Loop calls a buffer",
        );

        this.assertEquals(
            expectedResult[\instrument],
            \loop,
            "👀 SynthDef is \\loop",
        );
    }

    test_play {
        1 play: ["fm", 0];
        expectedResult = Px.lastFormatted[\1];

        this.assert(
            expectedResult[\buf].asString.contains("Buffer"),
            "👀 Play calls a buffer",
        );

        this.assertEquals(
            expectedResult[\instrument],
            \playbuf,
            "👀 SynthDef is \\playbuf",
        );
    }


    test_solo {
        1 i: \bd dur: 1 solo: 1;
        2 i: \sn dur: 1;
        expectedResult = (i: \bd, dur: 1, id: \1, solo: true);

        this.assertEquals(
            Px.last[\1],
            expectedResult,
            "👀 Px contains correct solo data",
        );

        this.assertEquals(
            Px.lastFormatted.size,
            1,
            "👀 Px only plays solo patterns",
        );
    }
}
