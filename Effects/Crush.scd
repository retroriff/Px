(
Dictionary[
    \crush -> { |bits = 4, rate = 10000|
        \filterIn -> { |in|
            var b = \crush1.kr(bits, 0.5);
            var r = \crush2.kr(rate, 0.5);
            var crushed = Latch.ar(in, Impulse.ar(r));
            var levels = 2.pow(b);
            (crushed * levels).round / levels;
        }
    }
];
)
