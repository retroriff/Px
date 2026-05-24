(
Dictionary[
  \hpf -> { |freq = 1200, gain = 1|
    \filterIn -> { |in|
      HPF.ar(in, \hpf1.kr(freq, 1)) * \hpf2.kr(gain, 0.5);
    }
  }
];
)