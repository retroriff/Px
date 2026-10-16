(
SynthDef(\sine, {
  |amp=0.2, freq=440, out=0,
  atk=0.001, dec=0.05, sus=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, sus, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = SinOsc.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\square, {
  |amp=0.2, freq=440, out=0, width=0.5,
  atk=0.001, dec=0.05, sus=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, sus, rel),
    gate: gate,
    doneAction: 2
  );
  var sig = Pulse.ar(freq, width.clip(0, 1));
  Out.ar(out, (sig * env * amp) ! 2);
}).add;

SynthDef(\saw, {
  |amp=0.2, freq=440, out=0, gate=1, pan=0,
  atk=0.005, dec=0.1, sus=0.7, rel=0.01,
  cutoff=4500, filterDecay=0.15, filterOctaves=1.5, rq=0.8|

  var ampEnv, filterCutoff, filterSweep, sig;

  ampEnv = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, doneAction: 2);
  filterSweep = EnvGen.kr(Env.perc(0.001, filterDecay.max(0.001)));
  filterCutoff = (cutoff * (2 ** (filterSweep * filterOctaves))).clip(100, 18000);
  sig = RLPF.ar(Saw.ar(freq), filterCutoff, rq.clip(0.2, 1));

  Out.ar(out, Pan2.ar(sig * ampEnv * amp, pan.clip(-1, 1)));
}).add;

SynthDef(\supersaw, {
  |amp=0.2, freq=440, out=0, gate=1, pan=0,
  atk=0.001, dec=0.05, sus=0.6, rel=0.01,
  cutoff=0, detune=0.18, filterDecay=0.15, filterOctaves=1.5,
  rq=0.8, spread=0.6|

  var ampEnv, filterCutoff, filtered, filterSweep, sig;

  ampEnv = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, doneAction: 2);
  filterSweep = EnvGen.kr(Env.perc(0.001, filterDecay.max(0.001)));
  filterCutoff = (cutoff * (2 ** (filterSweep * filterOctaves))).clip(100, 18000);

  sig = Mix.fill(5, { |index|
    var phaseOffset, position, semitones, voice, voiceFreq;

    semitones = (index - 2) * detune.max(0) / 4;
    phaseOffset = [0.08, 0.18, 0, 0.27, 0.39][index];
    position = ([-1, 1, -1, 1, -1][index] * spread.clip(0, 1) + pan).clip(-1, 1);
    voiceFreq = freq * (2 ** (semitones / 12));
    voice = DelayC.ar(Saw.ar(voiceFreq), 0.005, (phaseOffset / voiceFreq.max(20)).clip(0, 0.005));

    Pan2.ar(voice, position);
  }) / 5.sqrt;

  filtered = RLPF.ar(sig, filterCutoff, rq.clip(0.2, 1));
  sig = [0, 1].collect { |channel|
    Select.ar(cutoff > 0, [sig[channel], filtered[channel]])
  };

  Out.ar(out, sig * ampEnv * amp);
}).add;

SynthDef(\tri, {
  |amp=0.2, freq=440, out=0,
  atk=0.001, dec=0.05, sus=0.5, rel=0.01, sustain=0.5|

  var gate = EnvGen.kr(Env.new([1, 1, 0], [sustain, 0]));
  var env = EnvGen.kr(
    Env.adsr(atk, dec, sus, rel),
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

SynthDef(\tri_perc, { |out=0, freq=440, amp=0.2, sustain=1|
  var env = EnvGen.kr(Env.perc(0.01, sustain), doneAction:2);
  var sig = LFTri.ar(freq);
  Out.ar(out, (sig * env * amp) ! 2);
}).add;
)
