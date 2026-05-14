// TR-08 has MIDI a specific config for this device that will simplify our code (default latency is 0.195):
TR08.init;
808 i: \bd dur: 1;

// Let's sync the device with SC setting up a different latency:
(
TR08.init(0.195);
1 i: \bd dur: 1;
TR08.preset(\core).solo(\bd);
Px.midiClient["TR-08"].latency.postln;
)

// Or play a preset:
TR08.preset(\electro);

// If we edit the YAML presets file, we can reload them:
TR08.loadPresets;

// Two methods to stop it:
TR08.stop;
\808 i: \all;
