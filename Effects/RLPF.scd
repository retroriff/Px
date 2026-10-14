(
Dictionary[
  \rlpf -> { |freq = 1200, rq = 0.2|
    \filterIn -> { |in|
      var cutoff = \rlpf1.kr(freq, 0.05).clip(20, SampleRate.ir * 0.45);
      var resonance = \rlpf2.kr(rq, 0.05).max(0.1);

      RLPF.ar(in, cutoff, resonance);
    }
  }
];
)
