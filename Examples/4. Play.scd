// Play allow us to play notes
1 i: \rhodes dur: 4 legato: 4 octave: 5 amp: 0.5 degree: 0;
2 i: \rhodes dur: 1 legato: 1 octave: 4 degree: [0, 2b, 2s];

// Also random notes from a scale
1 i: \rhodes dur: 1 legato: 4 octave: 3 amp: 0.5 degree: 0;
2 i: \rhodes dur: 1 legato: 1 octave: 4 degree: \rand scale: \minor length: 3;
Px.shuffle
// Create arpegios:
1 i: \rhodes dur: 1 legato: 4 octave: 4 degree: \rand \scale: \minor length: 3 arp: 1;
