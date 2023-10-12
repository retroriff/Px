(
Neu ([
    // (i: \kick, dur: 2),
    // (fx: \reverb, mix: 0.4, damp: 0.2),

    (i: \BD, \dur: 1),
    // (i: \hat, \dur: 1/4), (fx: \reverb, mix: 0.3),

    (i: \SN, dur: 2, off: 1),
    // (fx: \reverb, mix: 0.4),

    // (loop: ["pop4", 0], a: 1, \rate: 1),
    // (fx: \wah, mix: 1, resHi: 5000),

    (play: ["fm", 26], a: 1, \rate: 1, \dur: 4),
    // (play: ["fm", 5], a: 1, \rate: 1, \dur: 4, \off: 0.5),
    // (fx: \reverb, mix: 0.2),
]);
)







\p, Pseries(0, 0.1, inf).fold(-1, 1)
\p, Pwhite(-1, 1., inf)
\rate, Pwhite(-1, 1., inf)


(
a = (fx: \reverb);
a.putAll([\midi, 1, \mix, 1]);
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

Neu.release(2)
Neu.browse
Neu.help;
Neu.help(\kick);

Neu.instruments

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

~ins1 = [
    \i, \kick,
    \dur, 1/2,
    \a, Pfunc{ v[0][\ins][\a] },
    \pan, Pwhite(-1, 1).trace,
    \swan, 1,
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
