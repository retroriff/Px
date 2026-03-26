(
Dictionary[
  \vibrato -> { |rate = 4, depth = 0.2|
    \filterIn -> { |in|
      var freq = \vibrato1.kr(rate, 0.5);
      var d = \vibrato2.kr(depth, 0.5);
      DelayC.ar(in, 0.01, SinOsc.kr(freq).range(0, d * 0.01));
    }
  }
];
)
