+ Px {
    *chorus {
        if (chorusPatterns.isNil) {
            ^this.prPrint("💩 Chorus is empty. Please run \"save\"");
        };

        this.prReevaluate(chorusPatterns);
    }

    *clear {
        this.initClass;
        Ndef(\px).clear;
    }

    *gui {
        var gui = Ndef(\x).proxyspace.gui;
        var width = gui.parent.bounds.width;
        var height = gui.parent.bounds.height;
        var x = (Window.screenBounds.width - width);
        var y = Window.screenBounds.height;
        gui.parent.bounds = [x, y, width, height];
        gui.parent.alwaysOnTop_(true);
    }

    *loadSynthDefs {
        PathName(("../SynthDefs/").resolveRelative).filesDo{ |file|
            file.fullPath.load;
        };
    }

    *mixer {
        var x, y;

        ~mixer = NdefMixer(Server.default);
        ~mixer.parent.alwaysOnTop_(true);
        ~mixer.switchSize(0);

        x = Window.screenBounds.width - ~mixer.sizes.small.x;
        y = Window.screenBounds.height - ~mixer.sizes.small.y;

        ~mixer.moveTo(x, y);
    }

    *play { |fadeTime|
        Ndef(\px).play(fadeTime: fadeTime);
    }

    *release { |time, name|
        var anyParam = [name, time];
        var fadeTime = time.isInteger.if(time, 10);

        if (anyParam.includes(\all)) {
            if (fadeTime == \all)
            { fadeTime = fadeTime };

            Ndef(\x).proxyspace.free(fadeTime);
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
        if (id.isNil) {
            Pdef.all do: { |item|
                Pdef(item.key).source = nil;
            };

            ^Ndef(\px).free
        };

        last.removeAt(id);
        ndefList.removeAt(id);
        Pdef(id).source = nil;
        soloList.remove(id);

        if (last.size > 0) {
            ^fork {
                4.wait;
                Ndef(id).free;
            }
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

        tempo = tempo.clip(10, 300) / 60;
        TempoClock.default.tempo = tempo;
        Sx.tempo(tempo);

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

