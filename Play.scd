(
Px ([
    (i: \BD, \dur: 1),
    // (play: ["michael", 0], \dur: 4),
    // (i: \SN, dur: 2, off: 1), (fx: \delay).wah(0.2),
    // (i: \CL, \dur: 1/4),
    // (i: \CH, \dur: 2),

    (i: \MC, \dur: 1/4).beat,
    // (loop: ["mt-2", \rand]).wah(0.2),

    (i: \CH, \dur: 1/4 ).beat(904).wah(1),
    (loop: ["break-4", 1]),
    (play: ["breath", 0], \dur: 2),
]);
)


(
Px ([
    (i: \CL, \dur: 1/4).beat,
    (i: \SN, dur: 2, off: 1),
    (i: \CH, \dur: 1/4).beat(904).wah(0.7),

    // (i: \RM, \dur: [1/4, 1/4, 1/2]).beat(95),
    (i: \BD, \dur: 1, \a: 0.5),
    // (play: ["abc-4", \rand]),
    (loop: ["abc-2", 1], amp: 0.5),
    // (play: ["sn", \rand]).reverb(0.3),
    // (play: ["ki", \rand], pbj: [3, 5, 4]),
    (play: ["cy", \rand], pbj: [5, 8, 4]),
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
    (i: \CH, \dur: 1/4 , a: 1.3).beat(904).wah(0.7),

    (i: \RM, \dur: [1/4, 1/4, 1/2], a: 0.2).beat(95).wah(0.6),
    // (i: \BD, \dur: 1, \a: 0.5),
    // (play: ["abc-4", \rand]).beat.wah(0.8),
    (play: ["sn", \rand], dur: 2, off: 1).reverb(0.3),
    // (play: ["ki", \rand], pbj: [3, 5, 4]),
    (play: ["cy", \rand], pbj: [5, 8, 4], amp: 0.3),
    // (fx: \delay, \mix: 0),
    // (loop: ["break-2", 0], a: 0.8).out,
    (loop: ["break-4", 5], a: 1),

    // (play: ["fm", 4], a: 0.3, \dur: 4).wah(0.5),"/Users/xavi/Library/Application Support/SuperCollider/Help/Overviews/Methods.html"
    (play: ["fm", \rand], a: 0.2, \off: 0.5).wah(0.5).seed(30),
    (play: ["pop", 0], a: 0.4, \off: 0.5, \dur: 16),
]);
)

Platform.userConfigDir.catArgs("/")
Document.open(Platform.userConfigDir.catArgs("/startup.scd"));

Platform.userConfigDir
Px.save
Px.chorus
Px.solo(1);
Px.release;
Px.browse
Px.help;
Px.help(\kick);
Px.instruments
Px.trace:

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