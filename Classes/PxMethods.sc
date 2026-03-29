+ Px {
  *chop { |dur, drop|
    last do: { |pattern|
      pattern[\chop] = [dur ?? 1, drop ?? 0];
    };

    this.prReevaluate;
  }

  *chorus {
    if (chorusPatterns.isNil) {
      ^("💩 Chorus is empty. Please run \"save\"");
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
    id = id.asSymbol;
    pausedPatterns.add(id);
    fork {
      TempoClock.default.timeToNextBeat(quant).wait;
      Ndef(id).pause;
    };
  }

  *play { |fadeTime|
    Ndef(\px).play(fadeTime: fadeTime);
  }

  *release { |time, id|
    var anyParam = [id, time];
    var fadeTime = time.isInteger.if(time, 10);

    if (anyParam.includes(\all)) {
      var tdefs = Tdef.all.select { |t| t.isPlaying }.keys;
      tdefs do: { |key| Tdef(key).stop };

      if (~isAnimatronEnabled == true)
      { ~animatronNetAddr.sendMsg("/sc/hush", fadeTime) };

      if (fadeTime == \all)
      { fadeTime = fadeTime };

      Ndef(\x).proxyspace.free(fadeTime);
      Fx.activeEffects = Dictionary.new;

      if (midiOut.notNil) {
        Px.panic;
      };
    };

    if (id.notNil) {
      id = id.asSymbol;
      colors.removeAt(id);
      last.removeAt(id);
      lastFormatted.removeAt(id);
      pausedPatterns.remove(id);
      Fx.clear(id);
      this.prAutoRefreshGui;
      ^Ndef(id).free(fadeTime)
    };

    Ndef(\px).free(fadeTime);
    colors = Dictionary.new;
    last = Dictionary.new;
    lastFormatted = Dictionary.new;
    pausedPatterns = IdentitySet.new;
    this.prAutoRefreshGui;

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
    id = id.asSymbol;
    pausedPatterns.remove(id);
    Ndef(id).resume;
  }

  *save {
    chorusPatterns = last.copy;
  }

  *set { |key, value|
    last do: { |pattern|
      pattern[key] = value;
    };

    this.prReevaluate;
  }

  *solo { |soloIds, id2, id3, id4, id5|
    var hasCommon;

    if (soloIds == false)
    { ^this.unsolo };

    if (soloIds.isNil)
    { ^("🟡 Provide at least one instrument to solo") };

    if (soloIds.isArray == false) {
      soloIds = [soloIds, id2, id3, id4, id5];
      soloIds = soloIds.reject(_.isNil).collect(_.asSymbol);
    };

    soloIds = soloIds.collect { |id| id.asSymbol };
    hasCommon = soloIds.any { |id| last.keys.includes(id) };

    if (hasCommon == false)
    { ^("🔴 No matching instruments to solo") };

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
      ^("🟡 No muted patterns to restore");
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
      pausedPatterns = IdentitySet.new;
      this.prAutoRefreshGui;
      ^Ndef(\px).free
    };

    if (idArray.isArray.not)
    { idArray = [idArray] };

    idArray do: { |id|
      id = id.asSymbol;

      if (last[id].notNil) {
        if (last[id][\hasGate] == false) {
          this.prChannelNoteOff(last[id][\chan]);
        };

        last.removeAt(id);
        lastFormatted.removeAt(id);
        ndefList.removeAt(id);
        pausedPatterns.remove(id);
        Pdef(id).source = nil;
      } {
        this.prPrint("🔴 Pattern" + id + "does not exist");
      };
    };

    this.prAutoRefreshGui;

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
      ^("🕰️ Current tempo is" + (TempoClock.tempo * 60));
    };

    tempo = tempo.clip(1, 300) / 60;
    TempoClock.default.tempo = tempo;
    Sx.tempo(tempo);
    thisProcess.interpreter.t = tempo;

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

  *trace { |id|
    if (id.isNil)
    { this.prPrint("🔴 Please specify a pattern id to trace") }
    { Pdef(id).source = Pdef(id).source.trace };
  }

  *traceOff { |id|
    if (id.isNil)
    { ^("🔴 Please specify a pattern id to disable trace") }
    { ^this.new(last[id]) };
  }

  *vol { |value, id|
    var ndef = id ?? \px;

    if (value.isNil) {
      var vol = Ndef(ndef).vol;
      ^("🔈 Px vol is" + vol);
    } {
      ^Ndef(ndef).vol_(value.clip(0, 3));
    }
  }
}

