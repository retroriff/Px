(
Dictionary[
  \compressor -> { |thresh = 0.1, ratio = 4, gain = 1|
    \filterIn -> { |in|
      Compander.ar(
        in, in,
        thresh: \compressor1.kr(thresh, 0.5),
        slopeBelow: 1,
        slopeAbove: 1 / \compressor2.kr(ratio, 0.5),
        clampTime: 0.01,
        relaxTime: 0.1
      ) * \compressor3.kr(gain, 0.5);
    }
  }
];
)
