(
Dictionary[
    \reverse -> {
        \filterIn -> { |in|
            var buf0 = LocalBuf(SampleRate.ir * 4, 2).clear;
            var buf1 = LocalBuf(SampleRate.ir * 4, 2).clear;
            var cycleFrames = 2 * TempoClock.tempo.reciprocal * SampleRate.ir;
            var phasor = Phasor.ar(0, 1, 0, cycleFrames);
            var trig = phasor < Delay1.ar(phasor);
            var toggle = A2K.kr(ToggleFF.ar(trig));
            var writeBuf = Select.kr(toggle, [buf0, buf1]);
            var readBuf = Select.kr(toggle, [buf1, buf0]);
            var env = (phasor / cycleFrames).sqrt;
            var reversed;

            BufWr.ar(in, writeBuf, phasor);
            reversed = BufRd.ar(2, readBuf, cycleFrames - 1 - phasor) * env;
            reversed;
        }
    }
];
)
