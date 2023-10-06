Neu {
    classvar <instruments;

    const defaultFadeTime = 10;

    *new { |patterns|
        var result;
        var ptparList = patterns.collect { |pattern, i|
            var createFade = { |amp, fade|
                var dir, durs, start, end;

                dir = if (fade.isString, { fade }, { fade[0] });
                durs = if (fade.class == Array and: fade[1] > 0,
                    { fade[1] },
                    { defaultFadeTime }
                );
                start = if (dir == "in", { 0 }, { amp });
                end = if (dir == "in", { amp }, { 0 });
                Pseg(Pseq([start, Pn(end)]), durs, curves: 0);
            };

            var addFx = { |fx, pattern|
            };

            var createAmp = { |amp|
                pattern.removeAt(\a);
                if (pattern[\fade].notNil) {
                    amp = createFade.value(amp, pattern[\fade]);
                };
                amp;
            };

            var createDur = { |dur|
                dur = if (dur.isArray, { Pseq(dur, inf) }, dur ?? 1);
                if (pattern[\pbj].notNil) {
                    dur = Pbjorklund2(pattern[\pbj][0], pattern[\pbj][1]) / pattern[\pbj][2];
                };
                dur;
            };

            if (pattern[\fx].notNil and: pattern[\i].isNil and: pattern[\ins].isNil, {
                var fxName = pattern[\fx];
                pattern.removeAt(\fx);
                result[result.size - 1][1] = Pbus(Pfx(result[result.size - 1][1], fxName).pairs_(pattern));
            }, {
                var offset = pattern[\off] ?? 0;
                pattern[\amp] = createAmp.value(pattern[\amp] ?? pattern[\a] ?? 1);
                pattern[\dur] = createDur.value(pattern[\dur]);
                pattern.removeAt(\off);
                result = result.add([offset, Pbind().patternpairs_(pattern.asPairs)]);
            });
        };

        instruments = patterns;
        Pdef(\neu, Ptpar(result.flatten)).quant_(4).play;
    }

    *release { |durs|
        var hasFade = false;
        var fadeOutInstruments = instruments.collect { |pattern|
            durs = if (durs > 0, { durs }, { defaultFadeTime });
            if (pattern[\fade].isNil,
                {
                    pattern.keys.do { |key|
                        var amp = pattern[\amp] ?? pattern[\a] ?? 1;
                        if ([\a, \amp].includes(key)) {
                            pattern[key] = Pseg(Pseq([amp, Pn(0)]), durs, curves: 0);
                        };
                    }
                },
                {
                    hasFade = true;
                };
            );
            pattern;
        };

        if (hasFade, {
            "Please remove fade keys".postln;
        }, {
            this.new(fadeOutInstruments);
        });
    }

    *help { |synthDef|
        if (synthDef.isNil,
            { this.class.asString.help; },
            {
                SynthDescLib.global[synthDef].postln;
            }
        );
    }
}
