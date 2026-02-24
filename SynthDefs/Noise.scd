(
SynthDef(\white, {
  |amp=0.2, out=0,
  atk=0.001, dec=0.05, slev=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, slev, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = WhiteNoise.ar(mul: 0.5);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\pink, {
  |amp=0.2, out=0,
  atk=0.001, dec=0.05, slev=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, slev, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = PinkNoise.ar(mul: 0.5);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\brown, {
  |amp=0.2, out=0,
  atk=0.001, dec=0.05, slev=0.5, rel=0.01, sustain=0.5|
  
  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, slev, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = BrownNoise.ar(mul: 0.5);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;
)
