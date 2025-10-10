(
Dictionary[
  \space -> { |fb = 0.95, fbLag = 3|
    \filterIn -> { |in|
      var wet = LocalIn.ar(2) + in;
      wet = DelayN.ar(wet, 0.03, 0.03);

      5.do({ |n|
        var del = { Rand(0.1 / (n + 2), 0.1 / (n + 1)) } ! 2;
        var coef = Rand(0.6, 0.75);
        var dec = del / (log10(coef) / 3.neg);
        wet = AllpassN.ar(wet, 0.1, del, dec);
      });

      LocalOut.ar(wet * \space1.kr(fb).varlag(\space2.kr(fbLag), 0));
      in + wet;
    }
  }
];
)
