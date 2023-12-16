// We can initialize our MIDI output with 0 latency:
Pmidi.init;

// Or define a latency:
Pmidi.init(0.1);

// But Pmidi can be omitted when we send our MIDI output to port 0:
(
Play([
    (id: \bass, chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3),
]);
)

// We can send controller messages
(
Play([
    (chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3),
    (\chan: 0).control(0, 0.8),
    (\chan: 0).control(1, 0.2),
]);
)

// Send random controllers:
(
Play([
    (chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3),
    (\chan: 0).control(0, \rand),
    (\chan: 0).control(1, [\wrand, 0, 0.2, 0.3]),
]);
)

// Use hold and to keep a key press:
(
Play([
    (chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3).seed(345),
    (id: \drone, \chan: 1, octave: 8).degree(\rand, \minor, 1).hold,
]);
)

// Shuffle it
(
Play([
    (chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3).seed(345),
    (id: \drone, \chan: 1, octave: 8).degree(\rand, \minor, 1).hold
]).shuffle;
)

// And holdOff as a "Panic" message, kills all notes on the channel pattern
(
Play([
    (chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3).seed(345),
    (id: \drone, \chan: 1, octave: 8).degree(\rand, \minor, 1).holdOff,
]);
)

// Pmidi can create more MIDIOut instances:
Pmidi.init(deviceName: "TR-08");

// And play patterns as usual:
(
Pmidi([(chan: 0, \midinote: 36).beat.weight(0.3)], "TR-08");
)

