(
Dictionary[
    \blp -> {
        \filterIn -> { |in|
            BLowPass4.ar(
                in: in,
                freq: SinOsc.kr(1/16).range(5000, 20000),
                rq: 0.1
            );
        }
    }
];
)