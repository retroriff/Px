(
// Guitar amp style distortion: tight low end, cascaded soft-clipping
// gain stages and a speaker cabinet voicing to tame digital fizz.
Dictionary[
  \dist -> { |drive = 0.5|
    // \filter, not \filterIn: filterIn applies the mix to the effect input,
    // which for a clipper changes the tone instead of the wet/dry balance.
    \filter -> { |in|
      var amount = \dist1.kr(drive, 0.5).clip(0, 1);
      var gain = amount.linexp(0, 1, 1, 120);
      var bias = 0.15;
      var dryAmp, wetAmp, sig;

      sig = HPF.ar(in, 160);
      sig = BPeakEQ.ar(sig, 800, 0.8, 4);

      sig = (sig * gain).tanh;
      sig = LPF.ar(sig, 7000);

      sig = ((sig * amount.linlin(0, 1, 1, 5)) + bias).tanh - bias.tanh;
      sig = LeakDC.ar(sig);

      sig = LPF.ar(sig, 4500);
      sig = LPF.ar(sig, 4500);
      sig = HPF.ar(sig, 100);
      sig = BPeakEQ.ar(sig, 120, 1.5, 3);
      sig = BPeakEQ.ar(sig, 1000, 1.0, -5);
      sig = BPeakEQ.ar(sig, 2800, 1.2, 4);

      // Clipping makes the output level independent of the input, so both
      // sides are measured and the wet path is matched back to the dry one.
      dryAmp = Amplitude.kr(in, 0.01, 0.2);
      wetAmp = Amplitude.kr(sig, 0.01, 0.2);

      sig * (dryAmp / wetAmp.max(0.0001)).clip(0, 8);
    }
  }
];
)
