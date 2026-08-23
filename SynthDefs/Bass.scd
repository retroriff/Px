(
SynthDef(\bass, {
	|out = 0, freq = 32.7, gate = 1, atk = 0.01, decay = 0.08,
	level = 0.65, rel = 0.1, filterDecay = 0.31, drive = 3,
	amp = 0.3, pan = 0, cutoff = 180, envAmount = 600|

	var env, filterEnv, sub, body, sig;

	env = EnvGen.kr(
		Env.adsr(atk, decay, level, rel, curve: [-4, -2, -5]),
		gate,
		doneAction: 2
	);

	filterEnv = EnvGen.kr(
		Env(
			[0, 1, 0.2, 0],
			[atk, decay, filterDecay],
			[-4, -3, -5]
		)
	);

	sub = SinOsc.ar(freq);

	body = Saw.ar(freq * 2) * 0.12;

	body = LPF.ar(
		body,
		cutoff + (filterEnv * envAmount)
	);

	sig = (sub + body) * env;
	sig = (sig * drive).tanh * amp;

	Out.ar(out, Pan2.ar(sig, pan));
}).add;
)
