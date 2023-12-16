// Play allow us to play notes
(
Play([
    (i: \rhodes, dur: 4, \legato: 4, octave: 5, amp: 0.5).degree(0),
    (i: \rhodes, dur: 1, \legato: 1, octave: 4).degree([0, 2b, 2s]),
]);
)

// Also random notes from a scale
(
Play([
    (i: \rhodes, dur: 4, \legato: 4, octave: 3, amp: 0.5).degree(0),
    (i: \rhodes, dur: 1, \legato: 1, octave: 4).degree(\rand, \minor, size: 3),
]);
)

// Create arpegios:
(
Play([
    (i: \rhodes, dur: 1, \legato: 1, octave: 4).degree(\rand, \minor, 3).arp,
]);
)
