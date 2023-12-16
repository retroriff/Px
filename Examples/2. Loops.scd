// We can play a audio file or use it as a loop:
(
Px ([
    (loop: ["pop-2", 1]),
    (play: ["fm", 17], dur: 4).delay(0.2).reverb(0.2),
]);
)

// Or generate a sequence of random files:
(
Px ([
    (loop: ["pop-2", 1]),
    (play: ["fm", \rand], dur: 1).delay(0.2).reverb(0.2),
]);
)

// Change the random sequence:
Px.shuffle

// And make it groove with beats, weights and seeds:
(
Px ([
    (loop: ["pop-2", 1]),
    (play: ["fm", \rand], dur: 1/4).delay(0.2).reverb(0.2).beat.weight(0.66).seed(619),
]);
)

// We can also play random loops:
(
Px ([
    (loop: ["mt-4", \rand]),
]);
)

// As usual, we can generate a new sequence:
Px.shuffle;

// Crossfade them:
(
Px ([
    (loop: ["mt-4", \jump]),
]);
)

// Trim them randomly
(
Px ([
    (loop: ["mt-4", 0]).trim,
]);
)

// From a start position
(
Px ([
    (loop: ["mt-4", 0]).trim(0.5),
]);
)

// And define it with patterns
(
Px ([
    (loop: ["mt-4", 0]).trim(Pwhite(0.0, 0.75)),
]);
)

// The buffer rate can also be changed
(
Px ([
    (loop: ["gl1", 0]).rate([\rand, 1, -1]),
    (loop: ["gl1", 1]).rate([\wrand, 1, -1, 0.52]),
]);
)

// We can play the buffer's rate with degree notes:
(
Px([
    (play: ["fm"]).degree(\rand, \minor, 3),
]);
)

// That's all for now
Px.stop;