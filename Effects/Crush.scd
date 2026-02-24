(
// Based on SuperDirt's effect
Dictionary[
    \crush -> { |bits = 8|
        \filterIn -> { |in|
            var b = \crush1.kr(bits, 0.5);
            var crushed = in.round(0.5 ** (b - 1));
            SelectX.ar(1, [in, crushed]);
        }
    }
];
)
