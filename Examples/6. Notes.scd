// We can play notation degrees directly, combined with scale, root and octave:
1 i: \rhodes amp: 0.5 dur: 4 degree: 0 octave: 3 legato: 1;
2 i: \rhodes amp: 0.5 dur: 1 degree: [0, 2b, 2s] octave: 4 legato: 1;

// Also random notes from a scale
1 i: \rhodes dur: 1 octave: 3 amp: 0.5 degree: 0 pan: \rotate;
2 i: \rhodes dur: 0.5 octave: 4 amp: 0.5 degree: \rand scale: \scriabin length: 3;
3 i: \rhodes dur: 0.25 octave: 4 amp: 0.5 degree: \rand scale: \scriabin length: 3;
Px.shuffle;

// Octaves can also have beats that alternate between a given value and one octave higher:
1 i: \303 dur: 0.25 amp: 0.9 octave: [\beat, 5] ctf: 0.1 env: 0.9 res: 0.4 wave: 0 weight: 0.3;

// We can send midinotes directly:
1 i: \wobble dur: 0.5 note: [56, 53, 60, 53, 60, 63].pseq;

// Create arpegios:
1 i: \rhodes dur: 1 legato: 4 amp: 0.5 octave: 3 degree: \rand scale: \minor length: 3 arp: 1;
Fx(\px).gverb(0.2).hpf(0.2);
