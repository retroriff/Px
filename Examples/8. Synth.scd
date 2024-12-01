// We can send arguments to the \ns nodeproxy synth:
(
Ns(
    (
        amp: 1,
        chord: [0, 2, 4],
        degree: [0, 1, 2, 3],
        dur: 1/4,
        env: 1,
        euclid: [3, 5],
        octave: [0, 0, 0, 1],
        root: 0,
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

// Define a lag2 time with the third argument:
Ns.set(\chord, [0, 3, 5], 1);

// Using shuffle we can specify a random seed for the scramble method
Ns.set(\degree, [0, 1, 2, 3].shuffle(71));

// We can check what has been set:
Ns.last;

// And stop it with a fade out
Ns.release(5);