(
Dictionary[
    \delay -> { |delaytime = 8, decaytime = 2|
        \filterIn -> { |in|
            in + CombN.ar(in,
                maxdelaytime: TempoClock.tempo / 4,
                delaytime: TempoClock.tempo / \delay1.kr(delaytime),
                decaytime: \delay2.kr(decaytime),
                mul: -12.dbamp);
        }
    }
];
)