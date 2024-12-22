// Ndefs

a = Ndef(\bd, Pbindef(\bd, \instrument, \lplay, \buf, ~buf.("mt-8", 0), \dur, 8, \amp, 0.4));
b = Ndef(\sn, Pbindef(\sn, \instrument, \playbuf, \buf, ~buf.("fm", 17), \dur, 16, \amp, 1));
c = Dictionary.newFrom([\sn, b]);

Ndef(\px, { Mix(c) }).quant_(4).play;

// Reevaluations can be executed with Pbindef:
Pbindef(\bd, \instrument, \lplay, \buf, ~buf.("mt-8", 0), \dur, 16, \amp, 0.4);

// And additions or removals are reevaluated from \px:
c.removeAt(\sn);
Ndef(\px, { Mix(c) });

// Pbindef Ndef can be controlled independently:
Ndef(\sn).release(10);

// Fx can apply FX to \px and to each item:
Fx(\px).reverb(Nil)
Fx(\sn).reverb(0.5)

// We can use Pdef utilities:
Pdef.gui
a = [\a, \b]
a.add(\c)

Ndef(\px, Pbindef(\bd, \instrument, \playbuf, \buf, ~buf.("fm", 17), \dur, 16, \amp, 1)).play
13 play: ["fm", 17] dur: 16 amp: 1;


(
\1 i: \bd dur: 1;
1 i: \bd dur: 16;
\2 i: \cp dur: 2 off: 1 beat: 1 weight: 0.34 seed: 25;
\3 play: ["michael", 0] dur: 1/4 beat: 1 weight: 0.02 reverb: \rand;
\4 i: \sn dur: 2 off: 1 delay: 0.5 reverb: 0.3;
5 play: ["gl1", \rand] dur: 1/4 beat: 1 weight: 0.66 delay: \rand reverb: \rand;
6 i: \ch dur: 1/4 beat: 1 weight: 0.02;

11 loop: ["fusion-4", 0] r: [\wrand, 1, -1, 0.02] reverb: 0.2 delay: Pwhite(0.1, 0) start: Pwhite(0.0, 0.75);
\11 loop: ["fusion-4", 0] delay: [\rand, 0, 0.4] reverb: [\rand, 0.1, 0.3] start: 0.16;
\12 play: ["abc-4", \rand] reverb: 0.5 beat: 1 weight: 0.03;

13 loop: ["gl1", \rand] dur: 16 reverb: \rand off: 2;
14 loop: ["fm", \rand] dur: 16 reverb: \rand off: 4 r: \rand amp: 0.5;
15 loop: ["fm", \rand] dur: 16 reverb: \rand off: 8 amp: 0.6;

\16 play: ["games", 1] dur: 8;
\17 play: ["games", 1] dur: 8 off: 2;

18 play: ["fm", \rand] dur: 0.25 off: 2 pan: \rand amp: 0.5 beat: 1 weight: 0.12 reverb: Pwhite(0.0, 1) delay: 0.3;
19 play: ["gl1", 2] dur: 0.25 off: 0.5 pan: \rand amp: 0.5 beat: 1 weight: 0.36 reverb: \rand delay: \rand;
20 play: ["breath", 0] dur: 8 weight: 0.4 reverb: \rand delay: \rand;

21 play: ["fm", 17] dur: 16 off: 0.5 delay: 0.5 reverb: 0.6;
)

s.plotTree