(
Dictionary[
  \rlpf -> { |freq = 1200, rq = 0.2|
    \filterIn -> { |in|
      var cutoff = \rlpf1.kr(freq, lag: 0.5).clip(20, SampleRate.ir * 0.45);
      var resonance = \rlpf2.kr(rq, lag: 0.5).max(0.1);

      RLPF.ar(in, cutoff, resonance);
    }
  }
];
)
