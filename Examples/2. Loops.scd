// We can play a audio file or use it as a loop:
1 loop: ["pop-2", 1];
2 play: ["fm", 17] dur: 4 delay: 0.2 reverb: 0.2;

// Or generate a sequence of random files:
3 play: ["fm", \rand] dur: 1 delay: 0.2 reverb: 0.2;

// Change the random sequence:
Px.shuffle

// And make it groove with beats, weights and seeds:
2 play: ["fm", \rand] dur: 0.25 beat: 1 weight: 0.66 seed: 619 delay: 0.2 reverb: 0.2;

// We can also play random loops:
\2 play: 1;
\3 play: 1;
1 loop: ["mt-4", \rand];

// As usual, we can generate a new sequence:
Px.shuffle;

// Crossfade them:
1 loop: ["mt-4", \jump];

// Trim them randomly
1 loop: ["mt-4", 0] trim: 1;

// Or from a start position
1 loop: ["mt-4", 0] trim: 0.5;

// A sequence
1 loop: ["mt-4", 0] trim: [0, 0.25, 0.5];

// Or defined by patterns
1 loop: ["mt-4", 0] trim: Pwhite(0.0, 0.75);

// The buffer rate can also be changed
1 loop: ["gl1", 0] r: [\rand, 1, -1];
1 loop: ["gl1", 1] r: [\wrand, 1, -1, 0.52];

// The loop duration can be stretched:
1 loop: ["mt-2", 0] dur: 4;

// We can play the buffer's rate with degree notes:
2 play: ["fm"] degree: \rand length: 3 beat: 1 dur: 0.25 amp: 0.5;
Px.reverb(0.2).delay(0.2).blp;

// That's all
Px.stop;
