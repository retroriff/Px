(
Dictionary[
    \pan -> { |pos = 0|
        \filterIn -> { |in|
            Pan2.ar(
                in,
                pos: \pan1.kr(pos)
            ).sum;
        }
    };
]
)