+ Px {
  *chop { |dur, drop|
    last do: { |pattern|
      pattern[\chop] = [dur ?? 1, drop ?? 0];
    };

    this.prReevaluate;
  }

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

  *pause { |id|
    Ndef(id).pause;
  }

  *play { |fadeTime|
    Ndef(\px).play(fadeTime: fadeTime);
  }

  *release { |time, name|
    var anyParam = [name, time];
    var fadeTime = time.isInteger.if(time, 10);

    if (anyParam.includes(\all)) {
      var tdefs = Tdef.all.select { |t| t.isPlaying }.keys;
      tdefs do: { |key| Tdef(key).stop };

      ~animatronNetAddr.sendMsg("/sc/hush", fadeTime);

      if (fadeTime == \all)
      { fadeTime = fadeTime };

      Ndef(\x).proxyspace.free(fadeTime);
      Fx.activeEffects = Dictionary.new;

      if (midiOut.notNil) {
        Px.panic;
      };
    };

    if (name.notNil) {
      colors.removeAt(name);
      last.removeAt(name);
      lastFormatted.removeAt(name);
      ndefList.removeAt(name);
      Fx.clear(name.asSymbol);
      ^Ndef(name.asSymbol).free(fadeTime)
    };

    Ndef(\px).free(fadeTime);
    colors = Dictionary.new;
    last = Dictionary.new;
    lastFormatted = Dictionary.new;

    fork {
      (fadeTime * 2).wait;

      ndefList.keys do: { |key|
        Fx.activeEffects.removeAt(key);
        Ndef(key).free(fadeTime);
      };

      Pdef.all do: { |item|
        Pdef(item.key).source = nil;
      };

      if (anyParam.includes(\all)) {
        this.prPrint("When the music is over\nTurn out the lights\nMusic is your only friend\nUntil the end 💀");
      };
    }
  }

  *resume { |id|
    Ndef(id).resume;
  }

  *save {
    chorusPatterns = last.copy;
  }

  *solo { |soloIds, id2, id3, id4, id5|
    var hasCommon;

    if (soloIds == false)
    { ^this.unsolo };

    if (soloIds.isNil)
    { ^this.prPrint("🟡 Provide at least one instrument to solo") };

    if (soloIds.isArray == false) {
      soloIds = [soloIds, id2, id3, id4, id5];
      soloIds = soloIds.reject(_.isNil).collect(_.asSymbol);
    };

    soloIds = soloIds.collect { |id| id.asSymbol };
    hasCommon = soloIds.any { |id| last.keys.includes(id) };

    if (hasCommon == false)
    { ^this.prPrint("🔴 No matching instruments to solo") };

    last.copy do: { |event|
      if (soloIds.includes(event[\id]) == false) {
        mutedPatterns.put(event[\id], event);

        if (event[\hasGate] == false) {
          this.prChannelNoteOff(event[\chan]);
        };

        Px.stop(event[\id]);
      }
    };
  }

  *unsolo {
    if (mutedPatterns.isNil || mutedPatterns.isEmpty) {
      ^this.prPrint("🟡 No muted patterns to restore");
    };

    mutedPatterns.keysValuesDo { |id, event|
      last.putAll([id, event]);
    };

    mutedPatterns = Dictionary.new;

    ^this.prReevaluate;
  }

  *stop { |idArray|
    if (idArray.isNil) {
      Pdef.all do: { |item|
        Pdef(item.key).source = nil;
      };

      ndefList = Dictionary.new;
      ^Ndef(\px).free
    };

    if (idArray.isArray.not)
    { idArray = [idArray] };

    idArray do: { |id|
      id = id.asSymbol;
      
      if(last[id].notNil) {
        if (last[id][\hasGate] == false) {
          this.prChannelNoteOff(last[id][\chan]);
        };

        last.removeAt(id);
        lastFormatted.removeAt(id);
        ndefList.removeAt(id);
        Pdef(id).source = nil;
      } {
        this.prPrint("🔴 Pattern" + id + "does not exist");
      };
    };

    idArray.do { |id|
      if (last.size > 0) {
        ^fork {
          4.wait;
          Ndef(id).free;
        }
      } {
        ^Ndef(\px).free
      };
    };
  }

  *synthDef { |synthDef|
    if (synthDef.isNil)
    { SynthDescLib.global.browse }
    { ^SynthDescLib.global[synthDef] };
  }

  *tempo { |tempo, withNdef|
    if (tempo.isNil) {
      ^this.prPrint("🕰️ Current tempo is" + (TempoClock.tempo * 60));
    };

    tempo = tempo.clip(1, 300) / 60;
    TempoClock.default.tempo = tempo;
    Sx.tempo(tempo);
    ~updateSingleLetterTempoVariable.(tempo);

    if (withNdef == true) {
      Ndef.all do: { |ndefs|
        ndefs do: { |ndef|
          var isPxNdef = ndef.key != \px and: (Px.last.keys.includes(ndef.key));

          if (isPxNdef == false and: (ndef.key != \x))
          { ndef.rebuild }
        }
      }
    };

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
    var ndef = name ?? \px;

    if (value.isNil) {
      var vol = Ndef(ndef).vol;
      this.prPrint("Px vol is" + vol);
    } {
      ^Ndef(ndef).vol_(value.clip(0, 3));
    }
  }
}

