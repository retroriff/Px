Nx {
  classvar <chords;
  classvar <currentChord;
  classvar <currentChordName;
  classvar = defaultOctave;
  classvar <>octave;
  classvar <tonics;

  *initClass {
    chords = Dictionary.new;
    defaultOctave = 3;
    octave = defaultOctave;
    tonics = Dictionary.new;

    this.loadChords;
    this.set(\EmAdd9);
  }

  *new { |chordName, octave|
    ^this.set(chordName, octave);
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

  *midinotes {
    var key = currentChord[\key];
    var intervals = currentChord[\intervals];

    ^intervals.collect { |interval| key + interval };
  }

  *loadChords {
    var tonicsPath, chordsPath;

    // Load tonics.scd
    tonicsPath = ("../Score/tonics.scd").resolveRelative;
    tonics = File.readAllString(tonicsPath).interpret;

    // Load chords.scd (qualities)
    chordsPath = ("../Score/chords.scd").resolveRelative;
    chords = File.readAllString(chordsPath).interpret;

    if (tonics.isNil or: { tonics.isEmpty }) {
      this.prPrint("ERROR: Failed to load tonics.scd");
    };

    if (chords.isNil or: { chords.isEmpty }) {
      this.prPrint("ERROR: Failed to load chords.scd");
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

  *set { |chordName, octave|
    var parsed, tonicData, qualityData, combinedChord;

    // Parse the chord name
    parsed = this.prParseChordName(chordName.asSymbol);
    if (parsed.isNil) {
      ^this.prPrint("Invalid chord name:" + chordName);
    };

    // Look up tonic data
    tonicData = tonics[parsed[\tonic]];
    if (tonicData.isNil) {
      ^this.prPrint("Tonic not found:" + parsed[\tonic]);
    };

    // Look up quality data
    qualityData = chords[parsed[\quality]];
    if (qualityData.isNil) {
      ^this.prPrint("Chord quality not found:" + parsed[\quality]);
    };

    // Combine into current chord
    combinedChord = Dictionary.new;
    combinedChord[\key] = tonicData[\key];
    combinedChord[\root] = tonicData[\root];
    combinedChord[\degree] = qualityData[\degree];
    combinedChord[\intervals] = qualityData[\intervals];
    combinedChord[\scale] = qualityData[\scale];

    // Update state
    currentChord = combinedChord;
    currentChordName = chordName.asSymbol;
  }

  *shuffle { |tonic, scale|
    var tonicPool, qualityPool, selectedTonic, selectedQuality, chordName;

    // Build tonic pool
    if (tonic.notNil) {
      if (tonics.includesKey(tonic.asSymbol).not) {
        ^this.prPrint("Invalid tonic:" + tonic);
      };
      tonicPool = [tonic.asSymbol];
    } {
      tonicPool = tonics.keys.asArray;
    };

    // Build quality pool (filter by scale if specified)
    if (scale.notNil) {
      qualityPool = chords.select { |qual, name|
        qual[\scale] == scale.asSymbol
      }.keys.asArray;

      if (qualityPool.isEmpty) {
        ^this.prPrint("No chord qualities found for scale:" + scale);
      };
    } {
      qualityPool = chords.keys.asArray;
    };

    // Random selection
    selectedTonic = tonicPool.choose;
    selectedQuality = qualityPool.choose;

    // Build chord name
    chordName = this.prBuildChordName(selectedTonic, selectedQuality);

    this.prPrint("Chord is" + chordName);
    ^this.set(chordName);
  }

  *prParseChordName { |chordSymbol|
    var input, tonicSym, qualitySym, qualityStr;

    input = chordSymbol.asString;

    // Try 2-character tonic first (Cs, Db, Eb, etc.)
    if (input.size >= 2) {
      tonicSym = input[0..1].asSymbol;
      if (tonics.includesKey(tonicSym)) {
        qualityStr = input[2..];
        qualitySym = this.prMapQuality(qualityStr);
        ^Dictionary[\tonic -> tonicSym, \quality -> qualitySym];
      };
    };

    // Try 1-character tonic
    if (input.size >= 1) {
      tonicSym = input[0].asSymbol;
      if (tonics.includesKey(tonicSym)) {
        qualityStr = input[1..];
        qualitySym = this.prMapQuality(qualityStr);
        ^Dictionary[\tonic -> tonicSym, \quality -> qualitySym];
      };
    };

    ^nil;  // Invalid tonic
  }

  *prMapQuality { |qualityStr|
    // Empty = major default (only special case)
    if (qualityStr.isEmpty) { ^"maj" };

    // Everything else: return as string
    ^qualityStr;
  }

  *prBuildChordName { |tonicSym, qualityStr|
    var suffix = if (qualityStr == "maj") { "" } { qualityStr };
    ^(tonicSym.asString ++ suffix).asSymbol;
  }

  *prPrint { |value|
    value.postln;
  }
}
