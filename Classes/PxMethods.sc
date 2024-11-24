+ Px {
    *chorus {
        if (chorusPatterns.isNil) {
            ^this.prPrint("💩 Chorus is empty. Please run \"save\"");
        };

        this.prReevaluate(chorusPatterns);
    }

    *play { |name|
        var newPattern;

        if (name.notNil)
        { newPattern = last[name] };

        if (newPattern.isNil)
        { newPattern = (i: \bd, id: \1, dur: 1) };

        ^this.new(newPattern);
    }

    *release { |fadeTime = 10, name|
        if (name == \all) {
            Ndef(\x).proxyspace.free(fadeTime);

            ^fork {
                (fadeTime + 5).wait;
                Ndef.clear;
            }
        };

        Ndef(\px).free(fadeTime);

        ^fork {
            (fadeTime * 2).wait;

            ndefList.keys do: { |key|
                Ndef(key).free(fadeTime);
            };
        }
    }

    *save {
        chorusPatterns = last.copy;
    }

    *stop { |id|
        if (id.isNil)
        { ^Ndef(\px).free };

        last.removeAt(id);
        ndefList.removeAt(id);
        soloList.remove(id);

        if (last.size > 0) {
            this.prReevaluate;
            ^Ndef(id).free;
        } {
            ^Ndef(\px).free
        };

    }

    *synthDef { |synthDef|
        if (synthDef.isNil)
        { SynthDescLib.global.browse }
        { ^SynthDescLib.global[synthDef] };
    }

    *tempo { |tempo|
        if (tempo.isNil) {
            ^this.prPrint("🕰️ Current tempo is" + (TempoClock.tempo * 60));
        };

        TempoClock.default.tempo = tempo.clip(10, 300) / 60;

        ^this.loadSynthDefs;
    }

    *trace { |name|
        if (name.isNil)
        { this.prPrint("🔴 Please specify a pattern name to trace") }
        { Pdef(name).source = Pdef(name).source.trace };
    }

    *traceOff { |name|
        if (name.isNil)
        { ^this.prPrint("🔴 Please specify a pattern name to disable trace") }
        { ^this.new(last[name]) };
    }

    *vol { |value, name|
        ^Ndef( name ?? \px).vol_(value);
    }
}

