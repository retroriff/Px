(
// https://roland50.studio
// 1 i: \303
// dur: 0.25
// note: ([67, 67, Rest(), 67, 67, Rest(), Rest(), Rest(), Rest(), Rest(), 67, Rest(), 68, Rest(), 62, Rest()] - 24).pseq
// amp: [0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.4, 0.2, 0.2, 0.2].pseq
// ctf: 1 res: 1 env: 1 dec: 1 acc: 1;
)


(
/*~octave = [0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1] + 5;
1 i: \303 amp: 0.5 dur: 0.25 octave: ~octave ctf: 1 env: 1 res: 1 dec: 1 acc: 1;
Fx(\1).reverb.delay
Dx.preset.use(\909)*/
)


(
SynthDef(\3032, {
    |amp = 1, ctf = 0.5, env = 0.5, freq = 440,
    gate = 1, out = 0, res = 0.2, wave = 0,
    atk = 0.003, dec = 0.5, sus = 0.3, acc = 0|

    var osc, filEnv, volEnv, sig;
    var filFreq, envAmount, negOffset, decTime;

    // --- Oscillator ---
    osc = Select.ar(wave, [
        Saw.ar(freq),
        Pulse.ar(freq, 0.5)
    ]);

    // --- Volume Envelope ---
    volEnv = EnvGen.ar(
        Env.adsr(atk, 0.2, 0.8, sus.linlin(0, 1, 0.05, 0.5), curve: -4),
        gate,
        doneAction: Done.freeSelf
    );
    volEnv = volEnv * (1 + (acc * 0.6));

    // --- Filter Envelope ---
    // Fast attack (~3ms), decay 200ms–2s controlled by dec
    // On accented notes, decay is forced to 200ms
    decTime = Select.kr(acc > 0.5, [
        dec.linlin(0, 1, 0.2, 2.0),
        0.2
    ]);

    filEnv = EnvGen.ar(
        Env.perc(0.003, decTime, curve: -4),
        gate
    );

    // --- Filter Frequency ---
    // ctf 0 = ~60Hz (dark), ctf 1 = ~4000Hz (bright) - exponential mapping
    filFreq = ctf.linexp(0, 1, 60, 4000);

    // Envelope modulation with negative DC offset (~31%)
    // Higher env darkens base tone while increasing sweep range
    negOffset = 0.3137;
    envAmount = env.linlin(0, 1, 0, 8000);
    filFreq = filFreq + ((filEnv - (negOffset * env)) * envAmount);
    filFreq = filFreq.clip(20, 18000);

    // --- 4-pole Ladder Filter ---
    // MoogFF: gain 0-4, where ~3.7 = near self-oscillation
    sig = MoogFF.ar(
        osc * volEnv,
        filFreq,
        res.linlin(0, 1, 0, 3.7)
    );

    // --- Saturation (diode ladder character) ---
    sig = (sig * 1.3).tanh;

    Out.ar(out, (sig * amp).dup);
}).add;
);
