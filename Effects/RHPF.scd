(
Dictionary[
  \rhpf -> { |freq = 1200, rq = 0.2|
    \filterIn -> { |in|
      var cutoff = \rhpf1.kr(freq, 0.05).clip(20, SampleRate.ir * 0.45);
      var resonance = \rhpf2.kr(rq, 0.05).max(0.1);

      RHPF.ar(in, cutoff, resonance);
    }
  }
];
)
