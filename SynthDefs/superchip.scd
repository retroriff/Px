/*
uses the Atari ST emulation UGen with 3 oscillators
"slide" is for a linear frequency glide that will repeat "rate" times (can be fractional or negative)
"accelerate" is for an overall glide,
"pitch2" and "pitch3" control the ratio of harmonics
"voice" causes variations in the levels of the 3 oscillators

Example:

(
1
i: \superchip
amp: 0.7
degree: \rand
dur: 0.25
beat: 0.6
root: Nx.root
length: 12
octave: 3
sustain: 0.1
scale: Nx.scale
pitch2: [1.2, 1.5, 2, 1].pseq
pitch3: [1.44, 1.25, 4, 9].pseq
voice: [0, 0.25, 0.25, 0.75].pseq
slide: [0, 0.4, 0].pseg(\lin, 16, inf)
seed: 200
)
*/

(
SynthDef(\superchip, {
  | amp = 0.3, out, sustain = 1, pan, freq, rate = 1,
  accelerate, slide = 0, pitch2 = 2, pitch3 = 3, voice = 0, speed = 1|

  var basefreq, env, sound, va, vb, vc;

  env = EnvGen.ar(
    Env.linen(0.01, 0.98, 0.01, 1, -1),
    timeScale: sustain,
    doneAction: 2
  );

  basefreq = freq + wrap2(slide * 100 * Line.kr(-1, 1 + (2 * rate - 2), sustain), slide * 100);
  basefreq = basefreq * DirtFreqScale.kr(speed, accelerate, sustain);

  va = (voice < 0.5) * 15;
  vb = ((2 * voice) % 1 < 0.5) * 15;
  vc = ((4 * voice) % 1 < 0.5) * 15;

  sound = AY.ar(
    tonea: AY.freqtotone(basefreq),
    toneb: AY.freqtotone(pitch2 * basefreq),
    tonec: AY.freqtotone(pitch3*basefreq),
    vola: va,
    volb: vb,
    volc: vc
  ) / 2;
  sound = tanh(sound)*2;

  Out.ar(out, DirtPan.ar(sound * amp, ~dirt.numChannels, pan, env));
}).add;
);