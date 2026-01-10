Nx {
  classvar <chords;
  classvar <current;
  classvar <currentChord;

  *initClass {
    chords = Dictionary.new;

    this.loadChords;
    this.set(\emAdd9);
  }

  *chord {
	  ^currentChord[\intervals];
  }

  *degrees {
    ^currentChord[\degree];
  }

  *key {
    ^currentChord[\key];
  }

  *loadChords {
    chords = Dictionary.new;

    PathName(("../Score/").resolveRelative).filesDo { |file|
      var chordDict = File.readAllString(file.fullPath).interpret;
      chords.putAll(chordDict);
    };
  }

  *midinotes {
    var key = currentChord[\key];
    var intervals = currentChord[\intervals];

    ^intervals.collect { |interval| key + interval };
  }

  *name {
    ^current;
  }

  *root {
    ^currentChord[\root];
  }

  *scale {
    ^currentChord[\scale];
  }

	*set { |chordName|
    var chord = chords[chordName.asSymbol];

    if (chord.isNil) {
      ^this.prPrint("🔴 Chord" + chordName + "not found");
    };

    current = chordName.asSymbol;
    currentChord = chord;

    ^this.prPrint("🟢 Chord is" + chordName);
  }

  *shuffle { |note|
    var pool, selected;

    if (note.notNil) {
      var targetNote = note.asString.toLower;

      pool = chords.select { |chord, name|
        name.asString[0].toLower == targetNote[0]
      };

      if (pool.isEmpty) {
        ^this.prPrint("No chords found for note:" + note);
      };
    } {
      pool = chords;
    };

    selected = pool.keys.asArray.choose;
    ^this.set(selected);
  }

  *prPrint { |value|
    value.postln;
  }
}
