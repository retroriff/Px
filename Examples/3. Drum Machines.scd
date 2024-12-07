// We have 4 available drum machines: 606, 707, 808, 909:
(
707 i: \bd dur: 1;
707 i: \sn dur: 2 off: 1;
707 i: \oh dur: 0.25 beat: 1 amp: 0.4 degree: \rand length: 3 rotate: 1;
707 i: \rd dur: 0.25 beat: 1 amp: 0.3;
\707 i: \ss dur: 0.25 off: 0.5 beat: 1;
)

\707 i: \all

(
909 i: \bd dur: 1;
909 i: \sn dur: 2 off: 1;
909 i: \oh dur: 0.25 beat: 1 amp: 0.4 degree: \rand length: 3 rotate: 1;
909 i: \rd dur: 0.25 beat: 1 amp: 0.3 rotate: 1;
909 i: \oh dur: 1 amp: 0.6 off: 0.5 pan: \rand;
909 i: \cr dur: 16 off: 4;
)

\909 i: \all

(
909 i: \bd dur: 0.25 beat: 1 weight: 0.3;
909 i: \sn dur: 0.25 fill: 1 weight: 0.3;
909 i: \ht dur: 0.25 fill: 1 weight: 0.3;
909 i: \lt dur: 0.25 fill: 1 weight: 0.5;
909 i: \rd dur: 0.25 beat: 1 weight: 0.3 amp: 0.5 reverb: 0.5;
909 i: \oh dur: 0.25 fill: 1 weight: 0.2 amp: 0.5;
)

\909 i: \all

// Dx allows to play presets:
Dx.preset(\electro, 2);

// If we edit the YAML presets file, we can reload them:
Dx.loadPresets;

// The default drum machine is 808, but we can change it:
Dx use: 909;

// Methods to stop it:
Dx.release;
Dx.stop;
\808 i: \all;

// TB-303 example:
(
1 i: \303 dur: 0.25 amp: 0.9 octave: [\beat, 5] seed: 200 ctf: 0.1 env: 0.9 res: 0.4;
2 i: \303 dur: 0.25 amp: 0.6 octave: [\beat, 5] seed: 200 degree: [0, 1, 5, 3] scale: \scriabin;
3 i: \303 dur: Pseq([0.5, 1, 2, 4, 8], inf) sus: Pkey(\dur) ctf: Pwhite(0.1, 1) env: Pwhite(0.1, 1) res: Pwhite(0.1, 1) delay: 0.2 reverb: 0.2;
)

Px root: 3
