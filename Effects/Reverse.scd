(
Dictionary[
  \reverse -> { |beats = 2|
    \filterIn -> { |in|
      var buf0, buf1, cycleFrames, env, phasor, readBuf, reversed, toggle, trig, writeBuf;

      cycleFrames = beats * TempoClock.tempo.reciprocal * SampleRate.ir;
      buf0 = LocalBuf(cycleFrames, 2).clear;
      buf1 = LocalBuf(cycleFrames, 2).clear;
      phasor = Phasor.ar(0, 1, 0, cycleFrames);
      trig = phasor < Delay1.ar(phasor);
      toggle = A2K.kr(ToggleFF.ar(trig));
      writeBuf = Select.kr(toggle, [buf0, buf1]);
      readBuf = Select.kr(toggle, [buf1, buf0]);
      env = (phasor / cycleFrames).sqrt;

      BufWr.ar(in, writeBuf, phasor);
      reversed = BufRd.ar(2, readBuf, cycleFrames - 1 - phasor) * env;
      reversed;
    }
  }
];
)
