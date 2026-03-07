(
Dictionary[
    \hpf -> { |freq = 1200|
        \filterIn -> { |in|
            HPF.ar(in, \hpf1.kr(freq, 1));
        }
    }
];
)