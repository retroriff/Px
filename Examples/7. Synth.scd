// We can send arguments to the \ns nodeproxy synth:

(
Ns(
    (
        amp: 1,
        chord: [0, 2, 4],
        dur: 1/4,
        euclid: [3, 5],
        degree: [0, 1, 2, 3],
        octave: [0, 0, 0, 1],
        env: 1,
        scale: \dorian,
        vcf: 1,
        wave: \saw,
    )
);
)

// Or set individual values

Ns.set(\wave, \pulse);
Ns.set(\wave, \sine);
Ns.set(\wave, \triangle);
Ns.set(\chord, [0, 3, 5]);

// We can check what has been set:

Ns.lastControls

// And stop it with a fade out

Ns.release(5)