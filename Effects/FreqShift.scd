(
Dictionary[
  \freqShift -> { |freq = 0, phase = 0|
    \filterIn -> { |in|
      FreqShift.ar(in, \freqShift1.kr(freq, 0.5), \freqShift2.kr(phase, 0.5));
    }
  }
];
)
