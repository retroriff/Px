(
SynthDef(\grainLoop, {
    |amp = 1, atk = 0.01, beats = 0, buf = 0, density = 10, dur = 4,
    freeze = 0, gate = 1, grainDur = 0.1, out = 0, pan = 0,
    rate = 1, rel = 0.05, scatter = 0, spread = 0, start = 0,
    sus = 1, tempo|

    var env, sig, pos, grainRate, trig;
    var scanRate, sustainTime;

    beats = Select.kr((beats > 0), [dur, beats]);
    tempo = Select.kr((tempo > 0), [TempoClock.default.tempo, tempo]);

    sustainTime = ((beats / tempo) * sus - atk - rel).max(0.01);

    scanRate = (tempo / (beats * ControlRate.ir)) * (1 - freeze);
    pos = Phasor.kr(0, scanRate, 0, 1);
    pos = (pos + start).wrap(0, 1);
    pos = (pos + (LFNoise1.kr(density) * scatter * 0.25)).wrap(0, 1);

    trig = Impulse.ar(density);
    grainRate = rate * BufRateScale.kr(buf) * (1 + (spread * TRand.ar(-1, 1, trig)));

    env = EnvGen.ar(
        Env.linen(atk, sustainTime, rel),
        gate,
        doneAction: 2
    );

    sig = GrainBuf.ar(2, trig, grainDur, buf, grainRate, pos, 2, pan);
    sig = sig / (density * grainDur).max(1);
    sig = sig * env * amp;

    Out.ar(out, sig);
}).add;
)
