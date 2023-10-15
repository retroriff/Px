(
Px ([
    // (i: \kick, dur: 2),
    // (fx: \reverb, mix: 0.4, damp: 0.2),

    // (i: \maracas, \dur: 1/4, a: 1, \p: -1),
    // (fx: \reverb, mix: 0.3),
    // (fx: \wah),
    (i: \SN, dur: 2, off: 1),
    (i: \hat, \dur: 1/4 , a: 2),

    (i: \RM, \dur: 1/4 , a: 1),

    (loop: ["pop-2", 0], a: 2, dur: 2).wah,
    (i: \BD, \dur: 1),

    // (play: ["fm", 8], a: 0.5, \rate: 1, \dur: 4),
    (play: ["fm", 19], a: 0.7, \rate: 1, \dur: 4, \off: 0.5),
    // (fx: \reverb, mix: 0.2),
]);
)
Neu.save
Neu.chorus
Neu.solo(1);

List[1, 2, 3, 4].collect({ arg item, i; item + 10 });
(
(i: \maracas, \dur: 1/4).wah;
var result = [
    ( 'ins': ( 'dur': 0.25, 'amp': 1, 'i': "maracas" ), 'off': 0 ),
    ( 'ins': ( 'dur': 2, 'amp': 1, 'i': "SN", solo: 1 ), 'off': 1 ),
    ( 'ins': ( 'dur': 1, 'amp': 1, 'i': "BD" ), 'off': 0 )
];
var solo = result.collect { |item, i| if (item['ins']['solo'].notNil) { true } { false }; };
var kaka = [0];
solo = nil;
solo.indicesOfEqual(true);
// kaka.size;
)

(
a = (a: 1).in;
)
(
var solo = [];
// solo = solo ++ [1];
var pais = (\solo: 1);
solo = pais.asPairs;
// solo[\dur];
solo.class;
solo = [\amp: 1];
solo.value(\amp).postln;
[1, 2].reject({ arg item; item == 1 });
)
Neu.release(10);
Neu.browse
Neu.help;
Neu.help(\kick);

Neu.instruments

Document.open("~/Library/Mobile Documents/com~apple~CloudDocs/Music/SuperCollider/Setup/Setup.scd".standardizePath)
~s.("pop-2", 0).class

(
a = "pop-a".split($-);
if (a.isArray and: { a.size > 1 } and: { a[1].asInteger > 0 }) { a[1].postln } { "kaka".postln };
b = 2;
c = if (b == 1) { "Is 1".postln } { "No".postln; };
e = "pop-2";
d = ~s.("pop-");
)

\p, Pseries(0, 0.1, inf).fold(-1, 1)
\p, Pwhite(-1, 1., inf)
\rate, Pwhite(-1, 1., inf)

(
a = (fx: \reverb).parentEvents.pus;
a.keys;
)
(
a = [(fx: \reverb).eveen];
// a.putAll([\midi, 1, \mix, 1]);
a;
)

~s.("pop", 1);
~s.("cl");
~s.("cy");

SynthDescLib.global[\reverb].isNil

Neu.clear
(
[
    ( 'ins': ( 'pbj': [ 3, 4, 4 ], 'dur': a Pbinop, 'amp': 1, 'i': kick ), 'fx': [ ( 'fx': reverb ), ( 'fx': delay ) ], 'off': 0 ),
    ( 'ins': ( 'amp': a Pseg, 'i': snare, 'dur': a Pbinop, 'pbj': [ 3, 4, 4 ],  'fade': [ out, 3 ] ), 'off': 0 ) ]
)
(
var isTest = isTest == 0 and: isTest == 1;
)

(
Neu.fx([
(fx: \reverb),
])
);

(
v = [
    (off: 0, ins: (\a: 0.5), fx: [(\mix: 1)]),
    (off: 0, ins: (\a: 1), fx: [(\mix: 0.1)]),
];

r = [
    (off: 0, ins: (\ins: \kick), fx: [(\fx: \reverb), (\fx: \delay)]),
    (off: 0, ins: (\ins: \snare), fx: [(\fx: \reverb)]),
];

(1..v.size).postln;
Document.open("~/Library/Mobile Documents/com~apple~CloudDocs/Music/SuperCollider/Setup/Setup.scd".standardizePath)
~ins1 = [
    \i, \kick,
    \dur, 1/2,
    \a, Pfunc{ v[0][\ins][\a] },
    \pan, Pwhite(-1, 1).trace,(
~bpm = 120;
TempoClock.default.tempo = ~bpm / 60;
)
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
    \damp2: 0.4
];

~fx2 = [
    \fx, \reverb,
    \decayTime, 7,
    \cleanupDelay, Pkey(\decayTime)
];

~fx = [~fx1, ~fx2];

~pbind = [~ins1, ~fx.flatten];

PbindFx(~ins1, ~fx1).play;
// PbindFx(~ins2, ~fx1).play;
)

ProxySpace.browse

Pdef(\kick, Pbind(\instrument, \kick, \amp, 1, \dur, 1)).play.quant\_(4).

Synth(\reverb).

(
var synthDesc = SynthDescLib.global[\reverb];
synthDesc;
// var argNames = synthDesc.collect(_.name);
// var argDefaults = synthDesc.args.collect(_.default);
)

ServerOptions.devices
s.options.outDevice = "BlackHole 16ch"
s.options.inDevice  = "MacBook Pro Microphone"
s.options.outDevice = "BlackHole 16ch"
s.options.outDevice = "External Headphones"
s.options.outDevice = "MacBook Pro Speakers"
s.options.outDevice = "Scarlett 4i4 USB"
s.options.outDevice = "Xavi's AirPods"

Server.killAll
("~/Library/Mobile Documents/com~apple~CloudDocs/Music/SuperCollider/Setup/Setup.scd".standardizePath).load;