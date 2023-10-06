\p, Pseries(0, 0.1, inf).fold(-1, 1)
\p, Pwhite(-1, 1., inf)
\rate, Pwhite(-1, 1., inf)

(
Neu ([
(i: \kick, a: 0.3, dur: 1/4, pbj: [3, 4, 4]),
// (fx: \reverb),
// (fx: \delay),

    (i: \reverb, a: 0.3, dur: 1/4, pbj: [3, 4, 4], fade: ["out", 3]),
    // (fx: \reverb),

]);
)

Neu.release(2)
Neu.browse
Neu.help;

Neu.instruments

(
Neu.fx([
(fx: \reverb),
])
);

(
v = [
(ins: (\a: 0.5), fx: [(\mix: 1)]),
(ins: (\a: 1), fx: [(\mix: 0.1)]),
];

~ins = [
\i, \kick,
\dur, 1/2,
\a, Pfunc{ v[0][\ins][\a] },
\fxOrder, [1],
];

(
~fx1 = [
\fx, \reverb,
\decayTime, 7,
\mix, Pfunc{ v[0][\fx][0][\mix] },
\cleanupDelay, Pkey(\decayTime)
]);

~pbind = [~ins, ~fx1];

PbindFx(\*~pbind).play;
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
