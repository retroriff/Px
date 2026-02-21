(
Dictionary[
    \reverb -> { |room = 0.7, size = 0.5|
        \filterIn -> { |in|
            var loop, depth;

            in = in * \reverb1.kr(room).lag(LFNoise1.kr(1).range(0.01, 0.02));

            4.do { in = AllpassN.ar(in, 0.03, { Rand(0.005, 0.02) }.dup(2), 1) };

            depth = \reverb2.kr(size).lag(0.02).linexp(0, 1, 0.01, 0.98);
            loop = LocalIn.ar(2) * { depth + Rand(0, 0.05) }.dup(2);
            loop = OnePole.ar(loop, 0.5);
            loop = AllpassN.ar(loop, 0.05, { Rand(0.01, 0.05) }.dup(2), 2);
            loop = DelayN.ar(loop, 0.3, [0.19, 0.26] + { Rand(-0.003, 0.003) }.dup(2));
            loop = AllpassN.ar(loop, 0.05, { Rand(0.03, 0.15) }.dup(2), 2);

            loop = loop + in;
            loop = LeakDC.ar(loop);

            LocalOut.ar(loop);

            loop;
        }
    };
]
)
