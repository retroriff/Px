Nx {
  classvar <chords;
  classvar <currentChord;
  classvar currentChordName;

  *initClass {
    chords = Dictionary.new;

    this.loadChords;
    this.set(\EmAdd9);
  }

  *chord {
    ^currentChordName;
  }

  *degrees {
    ^currentChord[\degree];
  }

  *intervals {
	  ^currentChord[\intervals];
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

    currentChord = chord;
    currentChordName = chordName.asSymbol;
  }

  *shuffle { |note, quality|
    var pool = chords;
    var selected;

    if (note.notNil) {
      var rootNote = note.asString[0].toUpper;
      pool = pool.select { |chord, name|
        name.asString[0] == rootNote
      };
    };

    if (quality.notNil) {
      pool = pool.select { |chord, name|
        chord[\scale] == quality
      };
    };

    if (pool.isEmpty) {
      ^this.prPrint("No chords found for:" + note + quality);
    };

    selected = pool.keys.asArray.choose;
    this.prPrint("Chord is" + selected);
    ^this.set(selected);
  }

  *prPrint { |value|
    value.postln;
  }
}
