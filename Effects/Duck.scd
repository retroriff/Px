(
Dictionary[
  \duck -> { |pxBus = 0|
    \filterIn -> { |in|
      var pxAudio = InFeedback.ar(
        bus: \duck1.kr(pxBus),
        numChannels: 2
      );

      var ctrl = LPF.ar(pxAudio, 150).sum;

      Compander.ar(in, ctrl,
        thresh: 0.005, // Main control, e.g. 0.05
        slopeBelow: 1,
        slopeAbove: 0.01,
        clampTime: 0.5,
        relaxTime: 0.15
      );
    }
  }
];
)
