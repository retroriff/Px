(
Dictionary[
  \rhpf -> { |freq = 1200, rq = 0.2|
    \filterIn -> { |in|
      var cutoff = \rhpf1.kr(freq, lag: 0.5).clip(20, SampleRate.ir * 0.45);
      var resonance = \rhpf2.kr(rq, lag: 0.5).max(0.1);

      RHPF.ar(in, cutoff, resonance);
    }
  }
];
)
