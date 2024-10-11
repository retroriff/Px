// We can initialize our MIDI output with 0 latency:
Pmidi.init;

// Or define a latency:
Pmidi.init(0.1);

// But Pmidi can be omitted when we send our MIDI output to port 0:
1 chan: 0 dur: 1 octave: 4 degree: [\rand, \phrygian, 3];

// We can send controller messages
1 control: [0, 0.8];
1 control: [1, 0.2];
(
Play([
    (chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3),
    (\chan: 0).control(0, 0.8),
    (\chan: 0).control(1, 0.2),
]);
)

// Send random controllers:
1 control: [0, \rand];
1 control: [1, [\wrand, 0, 0.2, 0.3]];

(
Play([
    (chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3),
    (\chan: 0).control(0, \rand),
    (\chan: 0).control(1, [\wrand, 0, 0.2, 0.3]),
]);
)

// Use hold and to keep a key press:
1 chan: 0 dur: 1 octave: 4 degree: [\rand, \phrygian, 3] seed: 345;
1 chan: 1 octave: 8 degree: [\rand, \minor, 1] hold: 1;
(
Play([
    (chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3).seed(345),
    (id: \drone, \chan: 1, octave: 8).degree(\rand, \minor, 1).hold,
]);
)

// Shuffle it
Px.shuffle

// And holdOff to relased a holded note:
1 chan: 1 octave: 8 degree: [\rand, \minor, 1] hold: 0;
(
Play([
    (id: \drone, \chan: 1, octave: 8).degree(0).hold,
]);
)


// And user "panic" message to kill all notes on the channel pattern
1 chan: 1 octave: 8 degree: [\rand, \minor, 1] panic: 1;
(
Play([
    (chan: 0, \dur: 1, octave: 4).degree(\rand, \phrygian, 3).seed(345),
    (id: \drone, \chan: 1, octave: 8).degree(\rand, \minor, 1).panic,
]);
)

// Pmidi can create more MIDIOut instances:
Pmidi.init(deviceName: "TR-08");

// And play patterns as usual:
1 chan: 0 note: 36 beat: 1 weight: 0.3
(
Pmidi([(chan: 0, \midinote: 36).beat.weight(0.3)], "TR-08");
)

