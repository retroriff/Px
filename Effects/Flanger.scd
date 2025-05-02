(
Dictionary[
  \flanger -> {
    \filterIn -> { |in|
      var mod = SinOsc.kr(TempoClock.tempo / 16).range(0.001, 0.005);
      var feedback = LocalIn.ar(1);
      var delay = DelayC.ar(
        in + (feedback * 0.25),
        maxdelaytime: 0.01,
        delaytime: mod
      );
      LocalOut.ar(delay);
      in + delay;
    }
  }
];
)
