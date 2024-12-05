// We can send arguments to the \sx nodeproxy synth:
(
Sx(
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
Sx.set(\wave, \pulse);
Sx.set(\wave, \sine);
Sx.set(\wave, \triangle);
Sx.set(\chord, [0, 3, 5]);

// Define a lag2 time with the third argument:
Sx.set(\chord, [0, 3, 5], 1);

// Using shuffle we can specify a random seed for the scramble method
Sx.set(\degree, [0, 1, 2, 3].shuffle(71));

// We can check what has been set:
Sx.last;

// And stop it with a fade out
Sx.release(5);