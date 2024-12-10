(
SynthDef(\lplay, {
    |amp = 1, atk = 0.01, beats = 0, buf = 0, decay = 0.3, dur = 4, gate = 1,
    out = 0, pan = 0, rel = 0.05, rate = 1, start = 0, sus = 1, tempo|

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

    env = EnvGen.ar(
        Env.linen(atk, sus, rel),
        gate,
        doneAction: 2
    );

    sig = Mix.ar(
        PlayBuf.ar(
            numChannels: 2,
            bufnum: buf,
            rate: rate,
            startPos: startPos,
            doneAction: 2,
    ));

    sig = (sig * env) * amp;
    sig = Pan2.ar(sig, pan);

    Out.ar(out, sig);
}).add;
)