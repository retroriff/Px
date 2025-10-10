(
Dictionary[
  \tremolo -> { |rate = 1|
    \filterIn -> { |in|
      var freq = TempoClock.tempo * (4 * \tremolo1.kr(rate));
      var mod = SinOsc.kr(freq).range(0, 1);
      in * mod;
    }
  }
];
)