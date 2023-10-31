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

	rotate {
        ^this.putAll([\pan, \rotate]);
	}

	seed { |seed|
		^this.putAll([\seed, seed]);
	}

	solo {
		^this.putAll([\solo, true]);
	}

    weight { |weight|
        ^this.putAll([\weight, weight.clip(0, 1)]);
	}

	// FX
	delay { |mix, args|
		this.fx(\delay, mix, args);
	}

	fx { |fx, mix, args|
		mix = mix ?? 1;
		^this.[\fx] = this.[\fx] ++ [[\fx, fx, \mix, mix.clip(0, 1)] ++ args];
	}

	reverb { |mix, args|
		this.fx(\reverb, mix, args);
	}

	wah { |mix, args|
		this.fx(\wah, mix, args);
	}
}
