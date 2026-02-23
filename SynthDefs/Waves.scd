(
SynthDef(\sine, {
  |amp=0.2, freq=440, out=0,
  atk=0.001, dec=0.05, slev=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, slev, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = SinOsc.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\square, {
  |amp=0.2, freq=440, out=0,
  atk=0.001, dec=0.05, slev=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, slev, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = Pulse.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\saw, {
  |amp=0.2, freq=440, out=0,
  atk=0.001, dec=0.05, slev=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, slev, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = Saw.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\triangle, {
  |amp=0.2, freq=440, out=0,
  atk=0.001, dec=0.05, slev=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, slev, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = LFTri.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\sine_perc, { |out=0, freq=440, amp=0.2, sustain=1|
  var env = EnvGen.kr(Env.perc(0.01, sustain), doneAction:2);
  var sig = SinOsc.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\square_perc, { |out=0, freq=440, amp=0.2, sustain=1|
  var env = EnvGen.kr(Env.perc(0.01, sustain), doneAction:2);
  var sig = Pulse.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\saw_perc, { |out=0, freq=440, amp=0.2, sustain=1|
  var env = EnvGen.kr(Env.perc(0.01, sustain), doneAction:2);
  var sig = Saw.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\triangle_perc, { |out=0, freq=440, amp=0.2, sustain=1|
  var env = EnvGen.kr(Env.perc(0.01, sustain), doneAction:2);
  var sig = LFTri.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;
)
