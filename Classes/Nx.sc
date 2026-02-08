Nx {
  classvar <chords;
  classvar <circleOfFifths;
  classvar <currentChord;
  classvar <currentChordName;
  classvar <defaultOctave;
  classvar <qualityAliases;
  classvar <>octave;
  classvar <quality;
  classvar <tonic;
  classvar <tonics;

  *initClass {
    chords = Dictionary.new;
    circleOfFifths = [\C, \G, \D, \A, \E, \B, \Fs, \Db, \Ab, \Eb, \Bb, \F];
    defaultOctave = 3;
    qualityAliases = Dictionary[
      \add9 -> "add9", \aug -> "aug", \dim -> "dim", \dom7 -> "dom7",
      \m7 -> "m7", \maj7 -> "maj7", \major -> "maj", \minor -> "m",
      \sus4 -> "sus4"
    ];
    octave = defaultOctave;
    tonics = Dictionary.new;

    this.loadChords;
    this.set(\EmAdd9);
  }

  *new { |chordName, octaveArg|
    ^this.set(chordName, nil, octaveArg);
  }

  *chord {
    ^currentChordName;
  }

  *degrees {
    ^currentChord[\degree];
  }

  *guitarDegrees {
    ^currentChord[\guitarDegree];
  }

  *intervals {
	  ^currentChord[\intervals];
  }

  *loadChords {
    var chordsPath, tonicsPath;

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

  *midinotes { |octaveArg|
    var rootNote = currentChord[\root];
    var intervals = currentChord[\intervals];
    var octaveOffset, targetOctave;

    // Validate octave range if provided
    if (octaveArg.notNil) {
      if ((octaveArg < -1) or: { octaveArg > 9 }) {
        ^this.prPrint("Octave must be between -1 and 9");
      };
    };

    targetOctave = octaveArg ?? octave;
    octaveOffset = (targetOctave - defaultOctave) * 12;

    ^intervals.collect { |interval| rootNote + interval + octaveOffset };
  }

  *root {
    ^currentChord[\root];
  }

  *scale {
    ^currentChord[\scale];
  }

  *set { |chordNameOrTonic, qualityArg, octaveArg|
    var chordName, combinedChord, parsed, qualityData, qualityStr, tonicData;

    if (octaveArg.notNil) {
      octaveArg = octaveArg.clip(-1, 9);
      octave = octaveArg;
    };

    if (qualityArg.notNil) {
      // 2-arg or 3-arg form: tonic + quality (+ octave)
      qualityStr = qualityAliases[qualityArg.asSymbol] ?? { qualityArg.asString };

      tonicData = tonics[chordNameOrTonic.asSymbol];

      if (tonicData.isNil) {
        ^this.prPrint("Tonic not found:" + chordNameOrTonic);
      };

      qualityData = chords[qualityStr];

      if (qualityData.isNil) {
        ^this.prPrint("Chord quality not found:" + qualityStr);
      };

      tonic = chordNameOrTonic.asSymbol;
      quality = qualityStr;
      chordName = this.prBuildChordName(tonic, quality);
    } {
      // 1-arg form: chord name
      parsed = this.prParseChordName(chordNameOrTonic.asSymbol);

      if (parsed.isNil) {
        ^this.prPrint("Invalid chord name:" + chordNameOrTonic);
      };

      tonicData = tonics[parsed[\tonic]];

      if (tonicData.isNil) {
        ^this.prPrint("Tonic not found:" + parsed[\tonic]);
      };

      qualityData = chords[parsed[\quality]];

      if (qualityData.isNil) {
        ^this.prPrint("Chord quality not found:" + parsed[\quality]);
      };

      tonic = parsed[\tonic];
      quality = parsed[\quality];
      chordName = chordNameOrTonic.asSymbol;
    };

    combinedChord = Dictionary.new;
    combinedChord[\root] = tonicData[\root];
    combinedChord[\degree] = qualityData[\degree];
    combinedChord[\intervals] = qualityData[\intervals];
    combinedChord[\scale] = qualityData[\scale];

    currentChord = combinedChord;
    currentChordName = chordName;

    ^this;
  }

  *shuffle { |tonicArg, scale|
    var chordName, qualityPool, selectedQuality, selectedTonic, tonicPool;

    // Build tonic pool
    if (tonicArg.notNil) {
      if (tonics.includesKey(tonicArg.asSymbol).not) {
        ^this.prPrint("Invalid tonic:" + tonicArg);
      };

      tonicPool = [tonicArg.asSymbol];
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

  *fifth { |tonicArg = \C, position = 0, quality = \major|
    var chordName, pos, qualityStr, startIndex, targetIndex, targetTonic;

    if (tonicArg == \rand)
    { tonicArg = circleOfFifths.choose };

    if (tonics.includesKey(tonicArg.asSymbol).not)
    { ^this.prPrint("Invalid tonic:" + tonicArg) };

    startIndex = circleOfFifths.indexOf(tonicArg.asSymbol);

    if (startIndex.isNil) {
      startIndex = this.prEnharmonicIndex(tonicArg.asSymbol);

      if (startIndex.isNil) {
        ^this.prPrint("Tonic not in circle of fifths:" + tonicArg);
      };
    };

    pos = if (position == \rand) { 12.rand } { position };

    targetIndex = (startIndex + pos) % 12;

    if (targetIndex < 0) { targetIndex = targetIndex + 12 };

    targetTonic = circleOfFifths[targetIndex];

    qualityStr = this.prFifthQuality(quality);

    if (qualityStr.isNil) {
      ^this.prPrint("Invalid quality:" + quality ++ ". Use" + (qualityAliases.keys.asArray ++ \rand));
    };

    chordName = this.prBuildChordName(targetTonic, qualityStr);

    this.prPrint("Chord is" + chordName);
    ^this.set(chordName);
  }

  *prFifthQuality { |quality|
    if (quality == \rand)
    { quality = qualityAliases.keys.asArray.choose };

    ^qualityAliases[quality.asSymbol];
  }

  *prEnharmonicIndex { |tonicSym|
    var equiv;
    var map = Dictionary[\Cs -> \Db, \Ds -> \Eb, \Gb -> \Fs, \Gs -> \Ab, \As -> \Bb];
    equiv = map[tonicSym];
    if (equiv.notNil) { ^circleOfFifths.indexOf(equiv) };
    ^nil;
  }

  *prParseChordName { |chordSymbol|
    var input, qualityStr, qualitySym, tonicSym;

    input = chordSymbol.asString;

    // Try 2-character tonic first (Cs, Db, Eb, etc.)
    if (input.size >= 2) {
      tonicSym = input[0..1].asSymbol;
      if (tonics.includesKey(tonicSym)) {
        qualityStr = input[2..];
        qualitySym = this.prMapQuality(qualityStr);
        if (chords.includesKey(qualitySym)) {
          ^Dictionary[\tonic -> tonicSym, \quality -> qualitySym];
        };
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
