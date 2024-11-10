// Let's start with basic examples:
1 i: \bd dur: 1;
2 i: \sn dur: 2 off: 1

// Stop it
\1 i: \bd dur: 1;
\2 i: \sn dur: 2 off: 1

// We can make it more wet:
1 i: \bd dur: 1 reverb: 0.2 wah: 0.1
2 i: \sn dur: 2 off: 1 delay: 0.2

// And create some random or Euclidian rhythms:
3 i: \ch dur: 0.25 beat: 1
4 i: \ma dur: 0.25 euclid: [3, 5]

// Or mute the rest while the soloists play:
(
1 i: \bd dur: 1 reverb: 0.2 wah: 0.1;
2 i: \sn dur: 2 off: 1 delay: 0.2;
3 i: \ch dur: 0.25 beat: 1 solo: 1;
4 i: \ma dur: 0.25 euclid: [3, 5];
)

// We can control the balance of hits and rests with weight:
(
1 i: \bd dur: 1;
2 i: \sn dur: 2 off: 1;
3 i: \cp dur: 0.25 beat: 1 weight: 0.2;
4 i: \ma dur: 0.25 beat: 1;
)

// Add some rest time
(
1 i: \bd dur: 1;
2 i: \sn dur: 2 off: 1 delay: 0.2;
3 i: \cp dur: 0.25 beat: 1 weight: 0.2 rest: 4;
4 i: \ma dur: 0.25 beat: 1 weight: 0.25;
)

// And add random variations with another weight defined as a hundredth:
(
1 i: \bd dur: 1;
2 i: \sn dur: 2 off: 1 delay: 0.2;
3 i: \ch dur: 0.25 beat: 1 weight: 0.12;
4 i: \ma dur: 0.25 beat: 1 weight: 0.26;
)

// And shuffle a new beat
Px.shuffle;

// We can set up our own rhythm:
1 i: \bd dur: 0.25 beat: [1, 0, 1, 0, 0, 1, 0, 1];

// We can fill the gaps of a pattern beat
1 i: \bd dur: 0.25 beat: [1, 0, 1, 0, 0, 1, 0, 1];
2 i: \sn dur: 0.5 fill: 1

// And set up a fill weight
(
1 i: \lc dur: 0.25 a: 0.4 beat: 1 weight: 0.42;
2 i: \lc dur: 0.25 a: 0.5 fill: 1 weight: 0.52 args: (freq: 200);
3 i: \lc dur: 0.25 fill: 1 weight: 0.63 args: (freq: 160);
4 i: \lc dur: 0.25 fill: 1 weight: 0.62 args: (freq: 180);
5 i: \ht dur: 0.25 fill: 1 weight: 0.3;
Px reverb: 0.1 delay: 0.1;
)

// Fill also accepts a rest argument:
(
1 i: \bd dur: 0.25 beat: 1 seed: 857;
2 i: \lt dur: 0.25 fill: 1 weight: 0.4 rest: 4;
3 i: \sn dur: 0.25 fill: 8 weight: 0.2 seed: 405;
Px reverb: 0.4;
)

// We can keep our favorite beats providing a seed number:
(
1 i: \bd dur: 1 reverb: 0.2;
2 i: \sn dur: 2 off: 1 delay: 0.2;
3 i: \ma dur: 0.25 beat: 1 weight: 0.2 seed: 250;
4 i: \oh dur: 0.25 beat: 1 weight: 0.26;
Px.shuffle;
)

// And check what's going on:
Px.trace(\1);
Px.traceOff(\1);

// Fade in and fade out tracks:
(
1 i: \bd dur: 1 reverb: 0.2 wah: 0.1 fade: \out;
2 i: \sn dur: 2 off: 1 delay: 0.3;
3 i: \ch dur: 0.25 beat: 1 weight: 0.12 seed: 250 in: 20;
4 i: \ma dur: 0.25 beat: 1 weight: 0.26;
)

// Control the balance with pan and rotate
(
1 i: \bd dur: 1 reverb: 0.2 wah: 0.1 pan: 1;
2 i: \sn dur: 2 off: 1 delay: 0.3 pan: -1;
3 i: \ch dur: 0.25 pan: \rand beat: 1 weight: 0.12 seed: 250;
4 i: \ma dur: 0.25 beat: 1 weight: 0.26 rotate: 1;
Px.shuffle;
)

// Save a chorus
Px.save

// And play it later
1 i: \bd dur: 1 reverb: 0.2 wah: 0.1 pan: 1 dur: 0.5;
2 i: \sn dur: 2 off: 1 delay: 0.3 pan: -1 dur: 0.25;
Px.chorus;

// Slide series to create dynamic transitions or fills:
2 i: \sn dur: 0.25 amp: [\exp, 0, 1, 8, 1]
3 i: \303 dur: 0.25 octave: 5 ctf: [\lin, 0, 1, 8]

// Change the tempo
Px.tempo(100);

// And finalize the session with a default fade out:
Px.release;

// Or define the release time:
Px.release(20);
