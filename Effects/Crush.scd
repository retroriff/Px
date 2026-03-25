(
Dictionary[
    \crush -> { |bits = 4|
        \filterIn -> { |in|
            var b = \crush1.kr(bits, 0.5);
            in.round(0.5 ** (b - 1));
        }
    }
];
)
