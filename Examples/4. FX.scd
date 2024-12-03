// We can separately filter our patterns using these FX filters:
1 i: \bd dur: 1 reverb: 0.4 delay: 0.2;
2 i: \sn dur: 2 off: 1 reverb: 0.3 delay: 0.2;
3 i: \cy dur: 0.25 beat: 1 wah: 0.6 reverb: 0.3;

// Add a random filter value:
2 i: \sn dur: 2 off: 1 reverb: \rand;

// Or define the probability of each item with weight:
2 i: \sn dur: 2 off: 1 reverb: [\wrand, 0.2, 0.8, 0.4];

// Filter them all together with global FX:
Px reverb: 0.5;

// FX also can be loaded with the Fx class:
Fx(\px).reverb(1, 1, 1);
Fx(\px).reverb(Nil);
Fx(\px).reverb(0.5).hpf(0.5);
Fx(\px).hpf(1, \wave);

// And kill'em all 🤘🏼:
Fx.clear

// Fx can be used with any Ndef:
Ndef(\x, { SinOsc.ar([200, 201]) }).play;
Fx(\x).reverb(1, 1, 1);

// Global FX also can load a VST plugin:
(
1 i: \bd dur: 1;
2 i: \sn dur: 2 off: 1;
3 i: \cy dur: 0.25 beat: 1;
Px.vst(0.4, "ValhallaFreqEcho");
)

// And we can control it using the Fx class:
Fx.vstController.set(1, 0.9);
Fx.vstController.editor;
Fx.vst(0.6);

// Save and load presets:
Fx.vstReadProgram(\mdosc);
Fx.vstWriteProgram;

// Stop it:
Px.stop;
