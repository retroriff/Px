(
Px ([
    // (i: \claves, \dur: 1/4, \amp: 0.7).beat(944),
    (i: \SN, dur: 2, off: 1),
    (fx: \reverb, mix: 0.5),
    (i: \hat, \dur: 1/4 , a: 1.3).beat(904).wah(0.2),

    // (i: \RM, \dur: [1/4, 1/4, 1/2], a: 0.4).beat(9),
    // (i: \BD, \dur: 1, \a: 1).solo,
    (loop: ["mt-2", 0], a: 1.5, \dur: 4).solo,
    (fx: \delay, \mix: 0),
    // (loop: ["mt-4", 2], a: 0.6).wah(0.6),

    (loop: ["fm", 17], a: 0.5, \rate: 1, \dur: 4),
    (play: ["fm", 27], a: 0.2, \rate: 1, \dur: 4, \off: 0.5).wah(0.5),
]);
)
Px.save
Px.chorus
Px.solo(1);
Px.release(10);
Px.browse
Px.help;
Px.help(\kick);
Px.instruments
Px.trace


(
var e;
x = Pbind(\i, \kick, \amp, 1);
PfadeIn(x, 10, 10).play;
)
(
var r = [
    ( 'dur': 2, 'amp': 1, 'i': \SN ),
    ( 'loop': [ "pop-2", 0 ], 'a': 2, 'fxMethod': [ \wah ], 'dur': 2, 'solo': true ),
    ( 'dur': 1, 'i': \BD ),
    ( 'rate': 1, 'a': 0, 'dur': 4, 'loop': [ \fm, 5 ] )
];
r = r.insert(2, (fx: \wah));
r;
)

(\i: \kick).wah
List[1, 2, 3, 4].collect({ arg item, i; item + 10 });
(
var ins = 2;
// ins.size;
(ins + "pattern has seed" + ins).postln;
)
(
(i: \maracas, \dur: 1/4).beat.seed;
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
a = 4;
a = a.flatten;
a.isString
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
r = Pbindef(\a, \instrument, \BD, \dur, 1, \amp, 1).play;
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

~s.("pop", 1);
~s.("cl");
~s.("cy");

SynthDescLib.global[\reverb].isNil

Px.clear
(
[
    ( 'ins': ( 'pbj': [ 3, 4, 4 ], 'dur': a Pbinop, 'amp': 1, 'i': kick ), 'fx': [ ( 'fx': reverb ), ( 'fx': delay ) ], 'off': 0 ),
    ( 'ins': ( 'amp': a Pseg, 'i': snare, 'dur': a Pbinop, 'pbj': [ 3, 4, 4 ],  'fade': [ out, 3 ] ), 'off': 0 ) ]
)
(
var isTest = isTest == 0 and: isTest == 1;
)

(
Px.fx([
(fx: \reverb),
])
);

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

Ptpar([
    0, x,
]).play
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