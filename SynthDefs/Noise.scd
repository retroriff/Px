(
SynthDef(\white, {
  |amp=0.2, out=0, pan=0,
  atk=0.001, dec=0.05, sus=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, sus, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = WhiteNoise.ar(mul: 0.2);
  Out.ar(out, Pan2.ar(sig * env * amp, pan.clip(-1, 1)));
}).add;

SynthDef(\pink, {
  |amp=0.2, out=0, pan=0,
  atk=0.001, dec=0.05, sus=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, sus, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = PinkNoise.ar(mul: 0.833333);
  Out.ar(out, Pan2.ar(sig * env * amp, pan.clip(-1, 1)));
}).add;

SynthDef(\brown, {
  |amp=0.2, out=0, pan=0,
  atk=0.001, dec=0.05, sus=0.5, rel=0.01, sustain=0.5|
  
  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, sus, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = BrownNoise.ar(mul: 0.316667);
  Out.ar(out, Pan2.ar(sig * env * amp, pan.clip(-1, 1)));
}).add;
)
