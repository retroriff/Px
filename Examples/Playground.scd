(
Px ([
    // (i: \bd, \dur: 1, amp: 1),
    (i: \bd, \dur: 1/2, id: \bd, amp: 1).beat.weight(0.23).reverb(0.6),
    // (i: \cp, \dur: 2, off: 1).beat(25).weight(0.34).delay(0),
    // (play: ["michael", 0], \dur: 1/4).beat().weight(0.02).reverb(\rand),
    // (i: \sn, dur: 2, off: 1).delay(mix: 0.5, args: [\decay: 8]).reverb(0.3),
    // (i: \sn, dur: 2).delay,
    // (i: \sn, \dur: 2, off: 1)
    // (play: ["gl1", \rand], \dur: 1/4, \amp: 1).beat.weight(0.66).delay(\rand).reverb(\rand),
    // (i: \ch, \dur: 1/4).beat.weight(0.02).solo,

    // (i: \cb, \dur: 1/4).delay.reverb.wah.beat.weight(0.26),
    // (loop: ["pop-2", 1], amp: 1).wah(0.3).reverb.out,
    // (loop: ["mt-4", \jump], amp: 1).solo.seed(24),
    // (loop: ["fusion-4", 0]).rate([\wrand, 1, -1, 0.02]).reverb(0.2).delay(Pwhite(0.1, 0)).trim(Pwhite(0.0, 0.75)),
    // (loop: ["fusion-4", 0], amp: 0.6).delay([\rand, 0, 0.2]).reverb([\rand, 0.1, 0.3]).trim(0.16),

    // (loop: ["mt-4", 0, \scratch], amp: 1).delay(0.1).wah(0.2),
    // (play: ["abc-4", \rand]).beat.weight(0.03).reverb(0.5),

    (loop: ["gl1", \rand], dur: 16, amp: 0.6).reverb(\rand),
    // (loop: ["gl1", \rand], dur: 16, amp: 0.5, off: 2).reverb(\rand),
    // (loop: ["fm", \rand], dur: 16, amp: 0.7, off: 4).reverb(\rand).rate(\rand),
    // (loop: ["fm", \rand], dur: 16, amp: 0.4, off: 8).reverb(\rand),


    // (i: \cy, \dur: 1/2, \pan: \rand, \amp: 0.2).beat(904).reverb(\rand),
    // (play:["games", 1], dur: 8),
    // (play:["games", 1], dur: 8, off: 2),

    // (play: ["fm", \rand], dur: 1/4, off: 2, pan: \rand, amp: 0.5).beat.weight(0.12).reverb(Pwhite(0.0, 1)).delay(0.3),
    (play: ["gl1", 2], dur: 1/4, off: 0.5, pan: \rand, amp: 0.7).beat.weight(0.36).delay(\rand).reverb(\rand),
    (play:["breath", 0], \dur: 8).delay(\rand).weight(0.4).reverb(\rand),
    // (play:["breath", 1], dur: 8, off: 2),

    // (play: ["fm", \rand], dur: 1, amp: 0.5, rate: 1).delay(0.6),
    // (play: ["fm", 17], dur: 16, off: 0.5, rate: 1, amp: 0.7).delay(0.5).reverb(0.6),
]);
)
MIDIClient.init

TR08.preset(\industrial, 0);

(
TR08([
    (i: \bd, dur: 1, amp: 1).beat.weight(0.4),
    (i: \sn, dur: 1/4).fill.weight(0.18),
    (i: \ch, dur: 1/4).beat.weight(0.62),
    (i: \ma, dur: 1/4).fill.weight(0.87),
    (i: \cp, dur: 1/4).fill.weight(0.28),
], "drumkit2");
)

(
TR08([
    (i: \lt, dur: 1/4, amp:0).beat.weight(0.3),
    (i: \mt, dur: 1/4).fill.weight(0.3),
    (i: \ht, dur: 1/4).beat.weight(0.6),
    (i: \mc, dur: 1/4).fill.weight(0.6),
    (i: \hc, dur: 1/4).fill.weight(0.8),
], "drumkit").release;
)

(
Play([
    // (i: \everythingrhodes, octave: 4, dur: 1, \legato: 1, amp: 0.4).degree(\rand, \dorian, 2).weight(0.34).arp,
    (id: \bass, chan: 0, \dur: 1/4, octave: 4, a: 0.2).degree(\rand, \phrygian, 4).beat.weight(0.8).seed(22),
    (\chan: 0).control(0, 0),

    (id: \drone, \chan: 1, octave: 8).degree(\rand, \minor, 1),
    (\chan: 1).control(0, 0.35),
]);
)


MIDIClient.init
MIDIClient.destinations
MIDIClient.destinations[port].uid
Pmidi.init

MIDIOut.newByName("TR-08", "TR-08");

Px.lastPatterns
Px.release
Px.save
Px.stop
Px.seeds
Px.shuffle
Px.synthDef(\bd)

SynthDescLib.all.postln
MIDIClient.sources
MIDIClient.destinations
MIDIClient.myoutports

a = Scale.at(\minor).degreesDocument.open("~/Library/Mobile Documents/com~apple~CloudDocs/Music/SuperCollider/Setup/Setup.scd".standardizePath)
Scale.minor.degrees.postln;


(
var length = inf;
MIDIClient.init;
v = MIDIOut.newByName("TR-08", "TR-08");
// v.noteOn(0, note: 36);

Pdef(\test, Pbind(
    \type, \midi,
    \midicmd, \noteOn,
    \midiout, v,
    \chan, 0,
    \midinote, Pseq([36], inf),
    \dur, 1
)).stop;
)

Px([(i: \LT, dur: 8)])

(
Px([
    (i: \bd, dur: 1/4).beat(rest: 8),
    // (play: ["fm"], dur: 1/4).beat(rest: 4),
    // (i: \sn, dur: 2, off: 1),
    // (play: ["fm"], dur: Pseq([Pn(1/4, 16), Rest(4)], inf)),
]);
)

Pseq([Pseq([1], 4), Rest(4)], inf)
A better way to do it?


(
// Define the MIDI output port for the TR-08
// Replace the 'portName' with your specific MIDI port name
// Check available MIDI outputs using MIDIOut: MIDIOut.list

// Find and select your MIDI output
MIDIClient.init;
MIDIClient.myoutports
MIDIClient.externalDestinations
a = MIDIOut.newByName("TR-08", "TR-08");

// Send a MIDI note to the TR-08

a.noteOn(0, 60, 100);  // Channel 1, note 60 (C4), velocity 100
// You can use different notes, channels, and velocities as needed
)

)

Play.lastPatterns
Play.release
Play.shuffle
Play.stop

(i: \ins)
(

Pwhite().isKindOf(Pattern)
)

m = MIDIOut.new(0);
(
Pdef(\play, Pbind(\type, \midi, \midicmd, \control, \midiout, m, \chan, 0, \degree, 0, \octave, 4)).play;
Pdef(\control, Pbind(\type, \midi, \midicmd, \control, \midiout, m, \chan, 0, \control, 0, \ctlNum, 0)).play
)

Pbindef(\test, \chan, 0, \control, 0).play




Pmidi([
    (
        \id: \synth,
        \chan: 2,
        \degree: Prand(Scale.dorian.degrees, inf),
        \dur: Pseq([1, 1/2, 2] * 4, inf),
        \legato: Pkey(\dur) * 2,
    )
], \pmidi2);

Pmidi([
    (
        \id: \synth,
        \chan: 3,
        \degree: Prand(Scale.dorian.degrees, inf),
        \dur: Pseq([1, 1/2, 2] * 4, inf),
        \legato: Pkey(\dur) * 2,
    )
], \pmidi3);
)

Pmidi.stop(\pmidi);



Px.stop;
Pmidi.stop;
Pmidi.release(20);

Pmidi.lastPatterns


Px.shuffle

(
var weight = 0.92;
var weight1;
var weight2;

weight1 = (weight * 10).floor / 10;
weight2 = weight - weight1 * 10;

weight1.postln; // Output: 0.4
weight2.postln; // Output: 0.01
)
Pdef(\test).gui

(
Pdef(\test, PbindFx(
    [\degree, Pwhite(0, 7, inf), \dur, 0.25, \legato, 1, \fxOrder, [1]],
    [\fx, \hpf]
)).play.quant_(4);
)
Pdef(\test, Pbind(\i, \bd, \amp, Pseq([Pwrand([Pseq([0, 0, 0, 0.5, 0.6], 1), Pseq([0.1, 0.1], 1)], [0.3, 0.7])], inf).trace, \dur, 1)).play.quant_(4);

[3, 7].normalizeSum

Pdef(\kick, Pseq([Pbind(\i, \kick, \a, 1, \dur, Pseq([0.5, 0.5, 0.5, 1.5], inf)), Pbind(\dur, 3, \a, \rest)], inf)).play.quant_(4);
Pdef(\kick, Pbind(\instrument, \kick, \amp, 1, \dur, 1, \pan, Pwalk((0..10).normalize(-1, 1), 1, Pseq([1, -1], inf)).trace)).play.quant_(4);

0..1
Pslide([1, 10], 1, 5)
(
PbindFx([\i, \bd, \amp, 1, \dur, 1, \fxOrder: [1]],
    [\fx, \reverb, \mix, 1, \decayTime, 7, \cleanupDelay, Pkey(\decayTime)]).play;
)


(
b = (i: \kick);
b.values;
"".asSymbol
// a = [\fx: \reverb];
// a[\fx];
)
(
Px ([
    (i: \cl, \dur: 1/4).beat,
    (i: \sn, dur: 2, off: 1),
    (i: \ch, \dur: 1/4).beat(904).wah(0.7),

    // (i: \RM, \dur: [1/4, 1/4, 1/2]).beat(95),
    (i: \bd, \dur: 1, \a: 0.5),
    // (play: ["abc-4", \rand]),
    (loop: ["abc-2", 1], amp: 0.5),
    // (play: ["sn", \rand]).reverb(0.3),
    // (play: ["ki", \rand], euc: [3, 5, 4]),
    (play: ["cy", \rand], euc: [5, 8, 4]),
    // (loop: ["break-2", 0]),
    (loop: ["break-4", 2]),

    // (play: ["fm", 4], \dur: 1, \amp: 0.5),
    (play: ["fm", \rand], \off: 0.5, \amp: 0.4).seed(30),
    // (play: ["pop", 0], \off: 0.5, \dur: 16).wah(0.2),
]);
)

(
a = true;
a.if { "Is 1".postln } { "Is 2".postln }
)

(
Px ([
    // (i: \claves, \dur: 1/4, \amp: 0.5),
    // (i: \SN, dur: 2, off: 1),
    (i: \ch, \dur: 1/4 , a: 1.3).beat(904).wah(0.7),

    (i: \rm, \dur: [1/4, 1/4, 1/2], a: 0.2).beat(95).wah(0.6),
    // (i: \bd, \dur: 1, \a: 0.5),
    // (play: ["abc-4", \rand]).beat.wah(0.8),
    (play: ["sn", \rand], dur: 2, off: 1).reverb(0.3),
    // (play: ["ki", \rand], euc: [3, 5, 4]),
    (play: ["cy", \rand], euc: [5, 8, 4], amp: 0.3),
    // (fx: \delay, \mix: 0),
    // (loop: ["break-2", 0], a: 0.8).out,
    (loop: ["break-4", 5], a: 1),

    // (play: ["fm", 4], a: 0.3, \dur: 4).wah(0.5)
    (play: ["fm", \rand], a: 0.2, \off: 0.5).wah(0.5).seed(30),
    (play: ["pop", 0], a: 0.4, \off: 0.5, \dur: 16),
]);
)

Platform.userConfigDir.catArgs("/")
Document.open(Platform.userConfigDir.catArgs("/startup.scd"));

Platform.userConfigDir
Px.save
Px.chorus
Px.release(20);
Px.browse
Px.help;
Px.help(\kick);
Px.chorus
Px.trace;

(
var mix = 2;
mix = mix.clip(0, 1);
mix;
)
(
var sampleLength = ("mt-4").split($-);
if (sampleLength.isArray and: { sampleLength.size > 1 } and: { sampleLength[1].asInteger > 0 })
{ sampleLength[1].postln; }
)

~s.("mt").size;

k = Array.rand(4, 0, ~s.["sn"] - 1);

d["sn", [0]][0].numChannels
~s.("mt-2", [0, 1]).size

~s.("mt-2", 0)
d["ki"][~bufKi]

~bufKi = Array.rand(1, 0, ~s.("sn").size);
~ka = ~s.("ki", ~bufKi)
~ka

Pseq(~ka, inf)

(
var e;
x = Pbind(\i, \kick, \amp, 1);
PfadeIn(x, 10, 10).play;
x.asString.postln
)
(
var r = [
    ( 'dur': 2, 'amp': 1, 'i': \SN ),
    ( 'loop': [ "pop-2", 0 ], 'a': 2, 'fxMethod': [ \wah ], 'dur': 2, 'solo': true ),
    ( 'dur': 1, 'i': \bd ),
    ( 'rate': 1, 'a': 0, 'dur': 4, 'loop': [ \fm, 5 ] )
];
r = r.insert(2, (fx: \wah));
r;
)

(
r = Pbindef(\a, \instrument, \bd, \dur, 1, \amp, 1).play;
r = r * 0.5;
Pbindef(\a, \amp, 0.5);
z.set(\amp, 211)
)
(
a = (fx: \reverb).parentEvents.pus;
a.keys;
)
(
a = [(fx: \reverb).eveen];
// a.putAll([\midi, 1, \mix, 1]);
a;
)

SynthDescLib.global[\reverb].isNil

Px.clear
(
[
    ( 'ins': ( 'euc': [ 3, 4, 4 ], 'dur': a Pbinop, 'amp': 1, 'i': kick ), 'fx': [ ( 'fx': reverb ), ( 'fx': delay ) ], 'off': 0 ),
    ( 'ins': ( 'amp': a Pseg, 'i': snare, 'dur': a Pbinop, 'euc': [ 3, 4, 4 ],  'fade': [ out, 3 ] ), 'off': 0 ) ]
)



(
v = [
    (off: 0, ins: (\a: 1), fx: [(\mix: 1)]),
    (off: 0, ins: (\a: 1), fx: [(\mix: 0.1)]),
];

r = [
    (off: 0, ins: (\ins: \kick), fx: [(\fx: \reverb), (\fx: \delay)]),
    (off: 0, ins: (\ins: \snare), fx: [(\fx: \reverb)]),
];

~ins1 = [
    \i, \kick,
    \dur, 1/2,
    \a, Pfunc{ v[0][\ins][\a] },
    \pan, Pwhite(-1, 1).trace,
    \fxOrder, [1],
];

~ins2 = [
    \i, \snare,
    \dur, 2,
    \a, Pfunc{ v[0][\ins][\a] },
    \fxOrder, [1],
];

~fx1 = [
    \fx: \reverb,
    \decayTime: 7,
    \cleanupDelay: Pkey(\decayTime),
    \damp2: 0.4,
    \mix: 0
];

~fx2 = [
    \fx, \reverb,
    \decayTime, 7,
    \cleanupDelay, Pkey(\decayTime),
    \mix: 0
];

~fx = [~fx1, ~fx2];

~pbind = [~ins1, ~fx.flatten];

x = PfadeOut.new(PbindFx(~ins1, ~fx1), fadeTime: 10);
)

(
Ptpar([
    0, Pbind(\instrument, \kick, \amp, 1, \dur, 4),
    0, Pbind(\instrument, \OH, \amp, 1, \dur, 1/4),
    0, Pbind(\ins, \kick, amp: 0, \dur, Pseq([4], )
]).play
)