(
Dictionary[
    \delay -> { |delaytime = 0.25, delayfeedback = 0.4|
        \filterIn -> { |in|
            var maxDelayTime = 4;
            var dt, fb, decayTime, signal;

            fb = \delay2.kr(delayfeedback, 0.5).clip(0, 0.99);
            dt = (\delay1.kr(delaytime, 0.5) * TempoClock.tempo.reciprocal).clip(0, maxDelayTime);
            decayTime = fb.lincurve(0, 1, 0.5, 25, 2) * dt;

            signal = CombL.ar(in, maxDelayTime, dt, decayTime);
            in + LeakDC.ar(signal);
        }
    }
];
)
