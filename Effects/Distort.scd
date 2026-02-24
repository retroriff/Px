(
// Based on SuperDirt's dirt_distort effect
Dictionary[
    \distort -> { |drive = 0.5|
        \filterIn -> { |in|
            var amount = \distort1.kr(drive, 0.5);
            var mod = CrossoverDistortion.ar(in, amp: 0.2, smooth: 0.01);
            mod = mod + (0.1 * amount * DynKlank.ar(
                `[
                    [60, 61, 240, 3000 + SinOsc.ar(62, mul: 100)],
                    nil,
                    [0.1, 0.1, 0.05, 0.01]
                ],
                in
            ));
            mod = (mod.cubed * 8).softclip * 0.5;
            SelectX.ar(amount, [in, mod]);
        }
    }
];
)
