(
Dictionary[
    \gverb -> { |roomsize = 200, revtime = 0.7|
        \filterIn -> { |in|
            GVerb.ar(
                in,
                \gverb1.kr(roomsize),
                \gverb2.kr(revtime)
            );
        }
    }
];

/*Dictionary[
  \gverb -> { |roomsize = 200, revtime = 0.7|
    \filterIn -> { |in|
      var fb = 0.95;
      var fb_lag = 3;
      var amp = 1;
      var sig, wet;

      sig = in; // assuming 'in' is an audio signal

      wet = LocalIn.ar(2) + sig;
      wet = DelayN.ar(wet, 0.03, 0.03);

      "cagarro".postln;  // for debugging :)

      5.do({ |n|
        var del, coef, dec;
        del = { Rand(0.1 / (n + 2), 0.1 / (n + 1)) } ! 2;
        coef = Rand(0.6, 0.75);
        dec = del / (log10(coef) / 3.neg);
        wet = AllpassN.ar(wet, 0.1, del, dec);
      });

      LocalOut.ar(wet * fb.varlag(fb_lag, 0));
      sig + wet;
    }
  }
];*/
)
