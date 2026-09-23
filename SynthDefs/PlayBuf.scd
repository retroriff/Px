(
SynthDef(\playbuf, {
  |amp = 1, atk  = 0, buf = 0, gate = 1, out = 0, pan = 0,
  rel = 0.05, rate = 1, start = 0|

  var env, frames, isRewind, startPos, sig;

  isRewind = (rate < 0);
  frames = BufFrames.kr(buf);
  startPos = abs((frames * isRewind) - (frames * start)) - (2 * isRewind);
  rate = BufRateScale.ir(buf) * rate;

  // Envelope
  env = EnvGen.ar(
    Env.asr(
      attackTime: atk,
      sustainLevel: 1,
      releaseTime: rel,
      curve: \lin
    ),
    gate,
    doneAction: 2
  );

  // Signal
  sig = Mix.ar(PlayBuf.ar(
    numChannels: 2,
    bufnum: buf,
    rate: rate,
    trigger: 1,
    startPos: startPos,
    loop: 0,
    doneAction: 2
  ));
  sig = sig * env * amp;
  sig = Pan2.ar(sig, pan);

  Out.ar(out, sig);
}).add;

)