(
Dictionary[
  \duck -> { |pxBus = 0, duckThresh = 0.005, duckLpf = 150|
    \filterIn -> { |in|
      var pxAudio = InFeedback.ar(
        bus: \duck1.kr(pxBus),
        numChannels: 2
      );

      Compander.ar(
        in,
        control: LPF.ar(pxAudio, \duck3.kr(duckLpf)).sum,
        thresh: \duck2.kr(duckThresh, 2),
        slopeBelow: 1,
        slopeAbove: 0.01,
        clampTime: 0.5,
        relaxTime: 0.15
      );
    }
  }
];
)
