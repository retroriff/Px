(
Dictionary[
    \lpf -> { |freq = 200|
        \filterIn -> { |in|
            RLPF.ar(in, \lpf1.kr(freq), rq: 0.1);
        }
    }
];
)