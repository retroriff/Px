+Event {
	// Controls
	a { |args|
		this.amp(args);
	}

	amp { |args|
		^this.putAll([\amp, args]);
	}

	beat { |seed|
		var pairs = [\beat, true];
		if (seed.notNil and: seed.isInteger)
		{ pairs = pairs ++ [\seed, seed] };
		^this.putAll(pairs);
	}

	dur { |args|
		^this.putAll([\dur, args]);
	}

	fade { |direction, time|
		var fade = if (time.isNil) { direction } { [direction, time.clip(0.1, time)] };
		^this.putAll([\fade, fade]);
	}

	in { |time|
		this.fade("in", time);
	}

	out { |time|
		this.fade("out", time);
	}

	rand { |folder|
		^this.putAll([\buf, [folder, \rand]]);
	}

	seed { |seed|
		^this.putAll([\seed, seed]);
	}

	solo {
		^this.putAll([\solo, true]);
	}

	// FX
	delay { |mix|
		this.fx(\delay, mix);
	}

	fx { |fx, mix|
		mix = mix ?? 1;
		^this.[\fxEvents] = this.[\fxEvents] ++ [[fx, mix.clip(0, 1)]];
	}

	reverb { |mix|
		this.fx(\reverb, mix);
	}

	wah { |mix|
		this.fx(\wah, mix);
	}
}
