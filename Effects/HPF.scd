(
Dictionary[
    \hpf -> { |freq = 1200|
        \filterIn -> { |in|
            RHPF.ar(in, \hpf1.kr(freq), rq: 0.1);
        }
    }
];
)