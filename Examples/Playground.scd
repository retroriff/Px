(
\1 i: \bd dur: 1;
1 i: \bd beat: 1 weight: 0.23 reverb: 0.6;
\2 i: \cp dur: 2 off: 1 beat: 1 weight: 0.34 seed: 25;
\3 play: ["michael", 0] dur: 1/4 beat: 1 weight: 0.02 reverb: \rand;
\4 i: \sn dur: 2 off: 1 delay: 0.5 reverb: 0.3;
5 play: ["gl1", \rand] dur: 1/4 beat: 1 weight: 0.66 delay: \rand reverb: \rand;
6 i: \ch dur: 1/4 beat: 1 weight: 0.02;
)

(
Px ([
    // (i: \cb, \dur: 1/4).delay.reverb.wah.beat.weight(0.26),
    // (loop: ["pop-2", 1], amp: 1).wah(0.3).reverb.out,
    // (loop: ["mt-4", \jump], amp: 1).solo.seed(24),
    // (loop: ["fusion-4", 0]).rate([\wrand, 1, -1, 0.02]).reverb(0.2).delay(Pwhite(0.1, 0)).start(Pwhite(0.0, 0.75)),
    // (loop: ["fusion-4", 0], amp: 0.6).delay([\rand, 0, 0.2]).reverb([\rand, 0.1, 0.3]).start(0.16),

    // (loop: ["mt-4", 0, \scratch], amp: 1).delay(0.1).wah(0.2),
    // (play: ["abc-4", \rand]).beat.weight(0.03).reverb(0.5),

    (loop: ["gl1", \rand], dur: 16, amp: 0.6).reverb(\rand),
    // (loop: ["gl1", \rand], dur: 16, amp: 0.5, off: 2).reverb(\rand),
    // (loop: ["fm", \rand], dur: 16, amp: 0.7, off: 4).reverb(\rand).rate(\rand),
    // (loop: ["fm", \rand], dur: 16, amp: 0.4, off: 8).reverb(\rand),


    // (i: \cy, \dur: 1/2, \pan: \rand, \amp: 0.2).beat(904).reverb(\rand),
    // (play:["games", 1], dur: 8),
    // (play:["games", 1], dur: 8, off: 2),

    // (play: ["fm", \rand], dur: 1/4, off: 2, pan: \rand, amp: 0.5).beat.weight(0.12).reverb(Pwhite(0.0, 1)).delay(0.3),
    (play: ["gl1", 2], dur: 1/4, off: 0.5, pan: \rand, amp: 0.7).beat.weight(0.36).delay(\rand).reverb(\rand),
    (play:["breath", 0], \dur: 8).delay(\rand).weight(0.4).reverb(\rand),
    // (play:["breath", 1], dur: 8, off: 2),

    // (play: ["fm", \rand], dur: 1, amp: 0.5, rate: 1).delay(0.6),
    // (play: ["fm", 17], dur: 16, off: 0.5, rate: 1, amp: 0.7).delay(0.5).reverb(0.6),
]);
)
