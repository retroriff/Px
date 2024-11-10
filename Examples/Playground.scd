(
\1 i: \bd dur: 1;
1 i: \bd dur: 1 beat: 1 weight: 0.23 reverb: 0.6;
\2 i: \cp dur: 2 off: 1 beat: 1 weight: 0.34 seed: 25;
\3 play: ["michael", 0] dur: 1/4 beat: 1 weight: 0.02 reverb: \rand;
\4 i: \sn dur: 2 off: 1 delay: 0.5 reverb: 0.3;
5 play: ["gl1", \rand] dur: 1/4 beat: 1 weight: 0.66 delay: \rand reverb: \rand;
6 i: \ch dur: 1/4 beat: 1 weight: 0.02;

11 loop: ["fusion-4", 0] r: [\wrand, 1, -1, 0.02] reverb: 0.2 delay: Pwhite(0.1, 0) start: Pwhite(0.0, 0.75);
\11 loop: ["fusion-4", 0] delay: [\rand, 0, 0.4] reverb: [\rand, 0.1, 0.3] start: 0.16;
\12 play: ["abc-4", \rand] reverb: 0.5 beat: 1 weight: 0.03;

13 loop: ["gl1", \rand] dur: 16 reverb: \rand off: 2;
14 loop: ["fm", \rand] dur: 16 reverb: \rand off: 4 r: \rand amp: 0.5;
15 loop: ["fm", \rand] dur: 16 reverb: \rand off: 8 amp: 0.6;

\16 play: ["games", 1] dur: 8;
\17 play: ["games", 1] dur: 8 off: 2;

18 play: ["fm", \rand] dur: 0.25 off: 2 pan: \rand amp: 0.5 beat: 1 weight: 0.12 reverb: Pwhite(0.0, 1) delay: 0.3;
19 play: ["gl1", 2] dur: 0.25 off: 0.5 pan: \rand amp: 0.5 beat: 1 weight: 0.36 reverb: \rand delay: \rand;
20 play: ["breath", 0] dur: 8 weight: 0.4 reverb: \rand delay: \rand;

21 play: ["fm", 17] dur: 16 off: 0.5 delay: 0.5 reverb: 0.6;
)
