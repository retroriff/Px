(
Dictionary[
    \delay2 -> { |delaytime = 0.5, delayfeedback = 0.5|
        \filterIn -> { |in|
            var maxDelayTime = 4;
            var dt, fb, feedbackSig, delayed;

            fb = \delay22.kr(delayfeedback, 0.5).clip(0, 0.99);
            dt = (\delay21.kr(delaytime, 0.5) * TempoClock.tempo.reciprocal).clip(0, maxDelayTime);

            feedbackSig = LocalIn.ar(2);
            delayed = DelayL.ar(in + (feedbackSig * fb), maxDelayTime, dt);
            delayed = LPF.ar(delayed, 8000);
            LocalOut.ar(delayed);

            in + LeakDC.ar(delayed);
        }
    }
];
)
