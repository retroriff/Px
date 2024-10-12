// Play allow us to play notes
1 i: \rhodes dur: 4 legato: 1 octave: 3 amp: 0.5 degree: 0;
2 i: \rhodes dur: 1 legato: 1 octave: 4 amp: 0.5 degree: [0, 2b, 2s];

// Also random notes from a scale
1 i: \rhodes dur: 1 octave: 3 amp: 0.5 degree: 0 rotate: 1;
2 i: \rhodes dur: 0.5 octave: 4 amp: 0.5 degree: \rand scale: \scriabin length: 3;
3 i: \rhodes dur: 0.25 octave: 4 amp: 0.5 degree: \rand scale: \scriabin length: 3;
Px.shuffle

// Create arpegios:
1 i: \rhodes dur: 1 legato: 4 amp: 0.5 octave: 3 degree: \rand scale: \minor length: 3 arp: 1;
Nfx(\px).gverb(0.5).hpf(0.3)