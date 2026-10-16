(
Dictionary[
  \wah -> { |rate = 1.5, depth = 0.8|
    \filterIn -> { |in|
      var wrate = \wah1.kr(rate, lag: 0.5);
      var wdepth = \wah2.kr(depth, lag: 0.5).clip(0, 1);
      var freq = LFPar.kr(wrate).range(200, 4000);
      RLPF.ar(in, freq, 0.2 + (1 - wdepth * 0.8));
    }
  }
];
)
