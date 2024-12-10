(
SynthDef(\playbuf, {
    |amp = 1, atk  = 0, buf = 0, dur = 1, out = 0, pan = 0,
    rel = 0.05, rate = 1, start = 0, sus = 1|

    var beatDur, env, frames, isRewind, numChannels = 2, startPos, sig;

    isRewind = (rate < 0);
    frames = BufFrames.kr(buf);
    startPos = abs((frames * isRewind) - (frames * start)) - (2 * isRewind);

    rate = BufRateScale.ir(buf) * rate;

    sus = (BufDur.kr(buf) * sus);
    beatDur = TempoClock.beatDur;
    // Prevents that sample length is longer than the pattern's length
    sus = Select.kr(((beatDur * dur) > sus), [beatDur * dur, sus]);

    // Envelope
    env = EnvGen.ar(
        Env.linen(
            attackTime: atk,
            sustainTime: sus,
            releaseTime: rel
        ),
        gate: 1,
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