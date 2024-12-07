// TR-08 has MIDI a specific config for this device that will simplify our code:
Px.initMidi(deviceName: "TR-08");
808 i: \bd dur: 1;

// Which is the same as (default latency is 0.195):
TR08((i: \bd, dur: 1));

// Let's sync the device with SC setting up a different latency:
(
TR08.init(0.195);
/* TODO: 8808 i: \bd dur: 1; */
1 i: \bd dur: 1;
)

// TODO: Now we can start to play with our TR-08:
/*
(
8808 i: \bd dur: 1;
8808 i: \rs dur: 0.25 beat: 1 weight: 0.4;
8808 i: \sn dur: 0.25 fill: 1 weight: 0.42;
8808 i: \ch dur: 0.25 beat: 1 weight: 0.82;
8808 i: \ma dur: 0.25 fill: 1 weight: 0.82;
)
*/

// Or play a preset:
TR08.preset(\electro);

// If we edit the YAML presets file, we can reload them:
TR08.loadPresets;

// Two methods to stop it:
TR08.stop;
\808 i: \all;
