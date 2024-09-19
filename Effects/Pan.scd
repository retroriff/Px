(
Dictionary[
    \pan -> { |center = 0|
        \filterIn -> { |in|
            center.postln;
            Splay.ar(
                in,
                center: \pan1.kr(center)
            );
        }
    };
]
)