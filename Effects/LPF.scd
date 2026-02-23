(
Dictionary[
    \lpf -> { |freq = 200|
        \filterIn -> { |in|
            LPF.ar(in, \lpf1.kr(freq, 0.5));
        }
    }
];
)