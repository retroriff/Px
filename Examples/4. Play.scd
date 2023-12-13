// Play allow us to play notes
(
Play([
    (i: \rhodes, dur: 4, \legato: 4, octave: 5, amp: 0.5).degree(0),
    (i: \rhodes, dur: 1, \legato: 1, octave: 4).degree([0, 2b, 2s]),
]);
)

// Also random notes
(
Play([
    (i: \rhodes, dur: 4, \legato: 4, octave: 3, amp: 0.5).degree(0),
    (i: \rhodes, dur: 1, \legato: 1, octave: 4).degree(\rand, \minor, 3),
]);
)

// Arpegios
// Also random notes
(
Play([
    (i: \rhodes, dur: 1, \legato: 1, octave: 4).degree(\rand, \minor, 3).arp,
]);
)

// We also can send MIDI cc messages with Play

Pmidi.init;
(
Play([
    (id: \bass, chan: 0, \dur: 1/4, octave: 4, a: 0.2).degree(\rand, \phrygian, 4).beat.weight(0.8).seed(22),
    (\chan: 0).control(0, 0.8),

    (id: \drone, \chan: 1, octave: 8).degree(\rand, \minor, 1),
    (\chan: 1).control(0, 0.35),
]);
)

// Or use Pmidi class, which automatically initializes MIDI output to port 0
(
Pmidi([
    (
        \chan: 0,
        \degree: Prand(Scale.dorian.degrees, inf),
        \dur: Pseq([1, 1/2, 2] * 4, inf),
        \legato: Pkey(\dur) * 2,
    )
], \pmidi2);
)

// Pmidi can create more MIDIOut instances
Pmidi.init(deviceName: "TR-08");

(
Pmidi([(chan: 0, \midinote: 36).beat.weight(0.3)], "TR-08");
)

// Use hold and to keep a key press:
(
Pmidi([
    (id: \drone, \chan: 1, octave: 4).degree(\rand, \minor, 1).hold,
]);
)

// And holdOff as a "Panic" message, kills all notes on the channel pattern
(
Pmidi([
    (id: \drone, \chan: 1, octave: 4).degree(\rand, \minor, 1).holdOff,
]);
)

// We can send controller messages
(
Pmidi([
    (\chan: 1).control(0, 0.35),
]);
)

// Also with rand and wrand
(
Pmidi([
    (\chan: 1).control(),
]);
)

