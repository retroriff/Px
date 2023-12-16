// We can separately filter our patterns using these FX filters:
(
Px([
    (i: \bd).reverb(0.4).delay(0.2),
    (i: \sn, dur: 2, off: 1).reverb(0.3).delay(0.2),
    (i: \cy, dur: 1/4).beat.wah(0.6).reverb(0.3),
]);
)

// Or filter them all together:
(
Px([
    (i: \bd),
    (i: \sn, dur: 2, off: 1),
    (i: \cy, dur: 1/4).beat,
]).reverb(0.5).delay(0.2);
)

// Add a random filter value:
(
Px([
    (i: \bd),
    (i: \sn, dur: 2, off: 1),
    (i: \cy, dur: 1/4).beat,
]).reverb(\rand);
)

// Or define the probability of each item with weight:
(
Px([
    (i: \bd),
    (i: \sn, dur: 2, off: 1),
    (i: \cy, dur: 1/4).beat,
]).reverb([\wrand, 0.2, 0.8, 0.4]);
)

// Stop it:
Px.stop;