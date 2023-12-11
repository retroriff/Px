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

// They also accept \rand:

(
Px([
    (i: \bd),
    (i: \sn, dur: 2, off: 1),
    (i: \cy, dur: 1/4).beat,
]).reverb(\rand);
)

// And \wrand:

(
Px([
    (i: \bd),
    (i: \sn, dur: 2, off: 1),
    (i: \cy, dur: 1/4).beat,
]).reverb([\wrand, 0.2, 0.8, 0.4]);
)

