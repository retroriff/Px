Nx {
  classvar <chords;
  classvar <circleOfFifths;
  classvar <current;
  classvar <defaultOctave;
  classvar <qualityAliases;
  classvar <>octave;
  classvar <tonics;

  var <chord, <chordOctave, <data, <quality, <tonic;

  *initClass {
    chords = Dictionary.new;
    circleOfFifths = [\C, \G, \D, \A, \E, \B, \Fs, \Db, \Ab, \Eb, \Bb, \F];
    defaultOctave = 2;
    qualityAliases = Dictionary[
      \add9 -> "add9", \aug -> "aug", \dim -> "dim", \dom7 -> "dom7",
      \m7 -> "m7", \maj7 -> "maj7", \major -> "maj", \minor -> "m",
      \sus4 -> "sus4"
    ];
    octave = defaultOctave;
    tonics = Dictionary.new;

    this.loadChords;
    this.set(\Cmaj);
  }

  *new { |chordName, octaveArg|
    var resolved = this.prResolve(chordName);

    if (resolved.isString) { ^resolved };

    ^this.prInstance(resolved, octaveArg !? { octaveArg.clip(-1, 9) });
  }

  *all {
    ^current.all;
  }

  *chord {
    ^current.chord;
  }

  *currentChord {
    ^current.data;
  }

  *currentChordName {
    ^current.chord;
  }

  *degrees {
    ^current.degrees;
  }

  *guitarDegrees {
    ^current.guitarDegrees;
  }

  *intervals {
    ^current.intervals;
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
    ^current.midinotes(octaveArg);
  }

  *midiRoot {
    ^current.midiRoot;
  }

  *parse { |chordName|
    var parsed = this.prParseChordName(chordName.asSymbol);

    if (parsed.isNil or: { chords.includesKey(parsed[\quality]).not })
    { ^nil };

    ^parsed;
  }

  *quality {
    ^current.quality;
  }

  *root {
    ^current.root;
  }

  *scale {
    ^current.scale;
  }

  *set { |chordNameOrTonic, qualityArg, octaveArg|
    var resolved;

    if (octaveArg.notNil) {
      octave = octaveArg.clip(-1, 9);
    };

    resolved = this.prResolve(chordNameOrTonic, qualityArg);

    if (resolved.isString) { ^resolved };

    current = this.prInstance(resolved);

    ^this;
  }

  *shuffle { |tonicArg, scale|
    var chordName, qualityPool, selectedQuality, selectedTonic, tonicPool;

    // Build tonic pool
    if (tonicArg.notNil) {
      if (tonics.includesKey(tonicArg.asSymbol).not) {
        ^("Invalid tonic:" + tonicArg);
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
        ^("No chord qualities found for scale:" + scale);
      };
    } {
      qualityPool = chords.keys.asArray;
    };

    // Random selection
    selectedTonic = tonicPool.choose;
    selectedQuality = qualityPool.choose;

    // Build chord name
    chordName = this.prBuildChordName(selectedTonic, selectedQuality);

    this.set(chordName);
    ^("Chord is" + chordName);
  }

  *tonic {
    ^current.tonic;
  }

  *fifth { |tonicArg = \C, position = 0, quality = \major|
    var chordName, pos, qualityStr, startIndex, targetIndex, targetTonic;

    if (tonicArg == \rand)
    { tonicArg = circleOfFifths.choose };

    if (tonics.includesKey(tonicArg.asSymbol).not)
    { ^("Invalid tonic:" + tonicArg) };

    startIndex = circleOfFifths.indexOf(tonicArg.asSymbol);

    if (startIndex.isNil) {
      startIndex = this.prEnharmonicIndex(tonicArg.asSymbol);

      if (startIndex.isNil) {
        ^("Tonic not in circle of fifths:" + tonicArg);
      };
    };

    pos = if (position == \rand) { 12.rand } { position };

    targetIndex = (startIndex + pos) % 12;

    if (targetIndex < 0) { targetIndex = targetIndex + 12 };

    targetTonic = circleOfFifths[targetIndex];

    qualityStr = this.prFifthQuality(quality);

    if (qualityStr.isNil) {
      ^("Invalid quality:" + quality ++ ". Use" + (qualityAliases.keys.asArray ++ \rand));
    };

    chordName = this.prBuildChordName(targetTonic, qualityStr);

    this.set(chordName);
    ^("Chord is" + chordName);
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
    var input, qualityStr, tonicSym;

    input = chordSymbol.asString;

    // Try 2-character tonic first (Cs, Db, Eb, etc.)
    if (input.size >= 2) {
      tonicSym = input[0..1].asSymbol;
      if (tonics.includesKey(tonicSym)) {
        qualityStr = input[2..];
        qualityStr = this.prMapQuality(qualityStr);
        if (chords.includesKey(qualityStr)) {
          ^Dictionary[\tonic -> tonicSym, \quality -> qualityStr];
        };
      };
    };

    // Try 1-character tonic
    if (input.size >= 1) {
      tonicSym = input[0].asSymbol;
      if (tonics.includesKey(tonicSym)) {
        qualityStr = input[1..];
        qualityStr = this.prMapQuality(qualityStr);
        ^Dictionary[\tonic -> tonicSym, \quality -> qualityStr];
      };
    };

    ^nil;  // Invalid tonic
  }

  *prMapQuality { |qualityStr|
    if (qualityStr.isEmpty) { ^"maj" };

    ^qualityAliases[qualityStr.asSymbol] ?? { qualityStr };
  }

  *prBuildChordName { |tonicSym, qualityStr|
    var suffix = if (qualityStr == "maj") { "" } { qualityStr };
    ^(tonicSym.asString ++ suffix).asSymbol;
  }

  *prInstance { |resolved, octaveArg|
    ^super.new.prInit(resolved, octaveArg);
  }

  *prPrint { |value|
    value.postln;
  }

  *prResolve { |chordNameOrTonic, qualityArg|
    var chordName, parsed, qualityData, qualityStr, tonicData, tonicSym;

    if (qualityArg.notNil) {
      tonicSym = chordNameOrTonic.asSymbol;
      qualityStr = qualityAliases[qualityArg.asSymbol] ?? { qualityArg.asString };
      chordName = this.prBuildChordName(tonicSym, qualityStr);
    } {
      parsed = this.prParseChordName(chordNameOrTonic.asSymbol);

      if (parsed.isNil) {
        ^("Invalid chord name:" + chordNameOrTonic);
      };

      tonicSym = parsed[\tonic];
      qualityStr = parsed[\quality];
      chordName = chordNameOrTonic.asSymbol;
    };

    tonicData = tonics[tonicSym];

    if (tonicData.isNil) {
      ^("Tonic not found:" + tonicSym);
    };

    qualityData = chords[qualityStr];

    if (qualityData.isNil) {
      ^("Chord quality not found:" + qualityStr);
    };

    ^Dictionary[
      \chord -> chordName,
      \data -> Dictionary[
        \degree -> qualityData[\degree],
        \intervals -> qualityData[\intervals],
        \midiRoot -> tonicData[\midiRoot],
        \root -> tonicData[\root],
        \scale -> qualityData[\scale]
      ],
      \quality -> qualityStr,
      \tonic -> tonicSym
    ];
  }

  all {
    ^Dictionary[
      \chord -> chord,
      \degrees -> this.degrees,
      \guitarDegrees -> this.guitarDegrees,
      \intervals -> this.intervals,
      \midiRoot -> this.midiRoot,
      \midinotes -> this.midinotes,
      \root -> this.root,
      \scale -> this.scale
    ];
  }

  degrees {
    ^data[\degree];
  }

  guitarDegrees {
    ^data[\guitarDegree];
  }

  intervals {
    ^data[\intervals];
  }

  midinotes { |octaveArg|
    var octaveOffset, targetOctave;

    if (octaveArg.notNil and: { (octaveArg < -1) or: { octaveArg > 9 } }) {
      ^("Octave must be between -1 and 9");
    };

    targetOctave = octaveArg ?? { chordOctave ?? octave };
    octaveOffset = (targetOctave - defaultOctave) * 12;

    ^data[\intervals].collect { |interval| data[\midiRoot] + interval + octaveOffset };
  }

  midiRoot {
    ^data[\midiRoot];
  }

  printOn { |stream|
    stream << "Nx(\\" << chord;

    if (chordOctave.notNil) {
      stream << ", " << chordOctave;
    };

    stream << ")";
  }

  prInit { |resolved, octaveArg|
    chord = resolved[\chord];
    chordOctave = octaveArg;
    data = resolved[\data];
    quality = resolved[\quality];
    tonic = resolved[\tonic];
  }

  root {
    ^data[\root];
  }

  scale {
    ^data[\scale];
  }
}
