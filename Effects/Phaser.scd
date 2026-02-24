(
// Based on SuperDirt's effect
Dictionary[
    \phaser -> { |rate = 1.0, depth = 0.5|
        \filterIn -> { |in|
            var prate = \phaser1.kr(rate, 0.5);
            var pdepth = \phaser2.kr(depth, 0.5).clip(0, 1);
            var phase = LFPar.kr(prate).range(0.0088, 0.01);
            var phaserwave = AllpassL.ar(in, 4, phase, 0, pdepth);
            in + phaserwave;
        }
    }
];
)
