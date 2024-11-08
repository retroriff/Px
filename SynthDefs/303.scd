(
SynthDef(\303, {
    |amp = 1, ctf = 0.1, env = 0.01, freq = 440,
    gate = 1, out = 0, res = 0.2, wave = 0,
    atk = 0.01, dec = 0, sus = 0.3|

    var getValue = { |initialValue, minValue, maxValue|
        minValue + (initialValue.clip(0, 1) * (maxValue - minValue));
    };

    var c2Freq = freq / 4;
    var ctfFreq = getValue.(ctf, 100, 10000);
    var envFreq = getValue.(env, 0, 10000);
    var release = getValue.(sus, 1, 5);

    var volEnv = EnvGen.ar(
        Env.new(
            levels: [10e-10, 1, 1, 10e-10],
            times: [ atk, dec, release],
            curve: 'exp',
        ),
        gate,
        doneAction: Done.freeSelf
    );

    var filEnv = EnvGen.ar(
        Env.new(
            levels: [10e-10, 1, 10e-10],
            times: [0.01, sus],
            curve: 'exp',
        ),
        gate,
        doneAction: Done.freeSelf
    );

    var waves = [
        Saw.ar(c2Freq, mul: volEnv),
        Pulse.ar(c2Freq, width: 0.5, mul: volEnv * -3.dbamp)
    ];

    var sig = RLPF.ar(
        in: Select.ar(wave, waves),
        freq: ctfFreq.value + (filEnv * envFreq),
        rq: res.clip(0.1, 1);
    ).dup;

    Out.ar(out, sig * amp * -2.dbamp);
}).add;
);
