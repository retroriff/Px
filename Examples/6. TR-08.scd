// TR-08 has MIDI a specific config for this device that will simplify our code:

(
Pmidi.init(deviceName: "TR-08");
Play([(chan: 0, \midinote: 36)], midiout: "TR-08");
)

// Which is the same as (default latency is 0.195):
(
TR08([(i: \bd)]);
)

// Let's sync the device with SC setting up a different latency:
(
TR08.init(0.195);
TR08([(i: \bd)]);
Px([(i: \bd)]);
)

// Now we can start to play with our TR-08:
(
TR08([
    (i: \bd, dur: 1),
    (i: \rm, dur: 1).beat.weight(0.4),
    (i: \sn, dur: 1/4).fill.weight(0.42),
    (i: \ch, dur: 1/4).beat.weight(0.82),
    (i: \ma, dur: 1/4).fill.weight(0.82),
]);
)

// Or play a preset:
TR08.preset(\electro, 0);
