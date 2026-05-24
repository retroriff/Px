(
Dictionary[
  \lpf -> { |freq = 200, gain = 1|
    \filterIn -> { |in|
      LPF.ar(in, \lpf1.kr(freq, 1)) * \lpf2.kr(gain, 0.5);
    }
  }
];
)