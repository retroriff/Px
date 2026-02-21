(
Dictionary[
    \delay -> { |delaytime = 0.5, delayfeedback = 0.5|
        \filterIn -> { |in|
            var maxDelayTime = 4;
            var dt, fb, decayTime, signal;

            fb = \delay2.kr(delayfeedback).clip(0, 0.99);
            dt = (\delay1.kr(delaytime) * TempoClock.tempo.reciprocal).clip(0, maxDelayTime);
            decayTime = (log2(-60.dbamp) / log2(fb) * dt).clip(0, 20);

            signal = CombL.ar(in, maxDelayTime, dt.lag(1), decayTime);
            in + LeakDC.ar(signal);
        }
    }
];
)
