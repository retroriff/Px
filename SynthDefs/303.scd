(
SynthDef(\303, {
    |amp = 0.5, ctf = 0.1, dec = 0.3, env = 0.01, freq = 440,
    gate = 1, out = 0, res = 0.2, sus = 0, wave = 0|

    var getValue = { |initialValue, minValue, maxValue|
        minValue + (initialValue.clip(0, 1) * (maxValue - minValue));
    };

    var decay = getValue.(dec, 1, 5);
    var ctfFreq = getValue.(ctf, 100, 10000);
    var envFreq = getValue.(env, 0, 10000);
    var c2Freq = freq / 4;

    var volEnv = EnvGen.ar(
        Env.new(
            levels: [10e-10, 1, 1, 10e-10],
            times: [0.01, sus, decay],
            curve: 'exp'),
        gate,
        doneAction: Done.freeSelf
    );

    var filEnv = EnvGen.ar(
        Env.new(
            levels: [10e-10, 1, 10e-10],
            times: [0.01, dec],
            curve: 'exp' ),
        gate,
        doneAction: Done.freeSelf
    );

    var waves = [Saw.ar(c2Freq, mul: volEnv), Pulse.ar(c2Freq, width: 0.5, mul: volEnv)];

    var sig = RLPF.ar(
        in: Select.ar(wave, waves),
        freq: ctfFreq.value + (filEnv * envFreq),
        rq: res
    ).dup;

    Out.ar(out, sig * amp);
}).add;
);
