(
SynthDef(\lplay, {
    |amp = 0.5, atk = 0.01, beats = 0, buf = 0, decay = 0.3, dur = 4, gate = 1,
    out = 0, pan = 0, rel = 1, rate = 1, start = 0, sus = 1, tempo|

    var env, sig;
    var frames = BufFrames.kr(buf);
    var isRewind = (rate < 0);
    var startPos = abs((frames * isRewind) - (frames * start)) - (2 * isRewind);

    // Rate & tempo
    beats = Select.kr((beats > 0), [dur, beats]);
    tempo = Select.kr((tempo > 0), [TempoClock.default.tempo, tempo]);
    rate = ((BufFrames.ir(buf) / s.sampleRate) * tempo / beats);
    rate = Select.kr((isRewind < 1), [rate.neg, rate]);

    // Sustain
    dur = Select.kr((dur < beats), [1, dur/beats]);
    sus = (BufDur.kr(buf) * dur / rate.abs) * sus;

    sig = Mix.ar(PlayBuf.ar(2, buf, rate, 1, startPos, 0, doneAction: 2));
    env = EnvGen.ar(Env.linen(atk, sus, rel), gate, doneAction: 2);
    Out.ar(out, Pan2.ar(sig * env * amp, pan));
}).add;
)