+ Px {
  *prCreateBeat { |pattern, defaultWeight = 0.7, min = 0, max = 1|
    var seed = this.prGetPatternSeed(pattern);
    var weight = pattern[\weight] ?? defaultWeight;
    var rhythmWeight = (weight * 10).floor / 10;
    var pseqWeight = weight - rhythmWeight * 10;

    var rhythmSeq = { |weight|
      Array.fill(16, { [min, max].wchoose([1 - weight, weight]) });
    };

    if (pattern[\seed] == \rand) {
      var amp = max;
      var repeats = inf;

      if (pattern[\repeat].notNil)
      { repeats = 16 * pattern[\repeat] };

      ^Pwrand([0, amp], [1 - weight, weight], repeats);
    };

    thisThread.randSeed = seed;

    if (pseqWeight > 0) {
      var seq1 = Pseq(rhythmSeq.(rhythmWeight), 1);
      var seq2 = Pseq(rhythmSeq.(rhythmWeight + 0.1), 1);
      var repeats = pattern[\repeat] ?? inf;

      ^Pwrand([seq1, seq2], [1 - pseqWeight, pseqWeight], repeats);
    };

    ^rhythmSeq.(weight);
  }

  *prCreateBeatSet { |amp, pattern|
    var repeats = pattern[\repeat] ?? inf;

    var list = pattern[\beatSet].collect { |step|
      if (step > 0)
      { amp }
      { step};
    };

    ^Pseq(list, repeats);
  }

  *prCreateRhythmBeat { |amp, pattern|
    var beats, canResolveSnapshot;

    if (pattern[\beatSet].isNil)
    { beats = this.prCreateBeat(pattern, max: amp) }
    { beats = this.prCreateBeatSet(amp, pattern) };

    canResolveSnapshot = beats.isKindOf(Pattern) and: { pattern[\seed] != \rand };

    last[pattern[\id]][\rhythmBeats] = if (canResolveSnapshot)
    { beats.iter.loop.nextN(16) }
    { beats };

    ^beats;
  }

  *prApplyAmpPattern { |beats, ampPattern|
    ^Prout({ |inval|
      var ampStream = ampPattern.asStream;
      var beatStream = beats.asStream;
      var step = beatStream.next(inval);
      var value;

      while { step.notNil } {
        if (step.isRest or: { step <= 0 })
        { value = step }
        { value = ampStream.next(inval) };

        if (value.isNil)
        { step = nil }
        {
          inval = value.yield;
          step = beatStream.next(inval);
        };
      };
    });
  }

  *prRestCycleLength { |pattern|
    var beatSteps = 16;
    var ampSteps = 1;
    var durSteps = 1;
    var dur = pattern[\dur];

    case
    { pattern[\beatSet].notNil }
    { ampSteps = pattern[\beatSet].size }

    { pattern[\beat].notNil or: { pattern[\fill].notNil } }
    { ampSteps = beatSteps }

    { pattern[\amp].isKindOf(Pseq) }
    { ampSteps = pattern[\amp].list.size };

    case
    { pattern[\euclid].notNil }
    { durSteps = pattern[\euclid][0] }

    { dur.isKindOf(Pseq) }
    { durSteps = dur.list.size };

    ^lcm(ampSteps.max(1), durSteps.max(1));
  }

  *prSequenceSteps { |value|
    while { value.isKindOf(FilterPattern) }
    { value = value.pattern };

    if (value.isKindOf(Pseq))
    { ^value.list.size };

    ^nil;
  }

  *prCycleStepCandidates { |pattern|
    var candidates = List.new;
    var steps = 1;

    case
    { pattern[\beatSet].notNil }
    { steps = pattern[\beatSet].size }

    { pattern[\beat].notNil or: { pattern[\fill].notNil } }
    { steps = 16 }

    { pattern[\euclid].notNil }
    { steps = pattern[\euclid][1] };

    candidates.add(steps.max(1));

    pattern.values do: { |value|
      var size = this.prSequenceSteps(value);

      if (size.notNil)
      { candidates.add(size.max(1)) };
    };

    ^candidates.asArray;
  }

  *prStepsToBeats { |steps, durStep|
    if (durStep.isNumber)
    { ^steps * durStep };

    if (durStep.isKindOf(Pseq) and: { durStep.list.every(_.isNumber) }) {
      var list = durStep.list;
      ^steps.collect { |index| list.wrapAt(index) }.sum;
    };

    ^nil;
  }

  *prCycleBeats { |pattern|
    var durStep = pattern[\durStep];
    var restBeats = pattern[\rest] ?? 0;
    var cycles, fitting;

    if (durStep.isNil)
    { ^nil };

    if (pattern[\euclid].notNil) {
      if (durStep.isNumber.not)
      { ^nil };

      ^(pattern[\euclid][1] * durStep) + restBeats;
    };

    cycles = this.prCycleStepCandidates(pattern)
      .collect { |steps| this.prStepsToBeats(steps, durStep) }
      .reject(_.isNil)
      .collect { |beats| beats + restBeats };

    if (cycles.isEmpty)
    { ^nil };

    fitting = cycles.select { |beats| beats <= maxQuant };

    if (fitting.notEmpty)
    { ^fitting.maxItem };

    ^cycles.maxItem;
  }

  *prCreateRest { |pattern, pbindef|
    var restBeats = pattern[\rest];

    if (restBeats.isNil)
    { ^pbindef };

    ^Pseq([
      Pfin(this.prRestCycleLength(pattern), pbindef),
      Pbind(\dur, Pseq([Rest(restBeats)]))
    ], pattern[\repeat] ?? inf);
  }

  *prCreateFillFromBeat { |amp, pattern|
    var steps = 16;
    var invertBeat, previousBeats, totalBeat;
    var previousPattern;
    var getInvertBeat;
    var getTotalBeat;
    
    // Find previous pattern using integer ID for drum machines
    if (pattern[\drumMachineIntegerId].notNil) {
      var previousIntegerId = pattern[\drumMachineIntegerId].asInteger - 1;

      // Search for pattern with matching integer ID
      previousPattern = last.detect({ |p|
        p[\drumMachineIntegerId] == previousIntegerId
      });
    } {
      // Use regular ID for non-drum-machine patterns
      var previousId = (pattern[\id].asInteger - 1).asSymbol;
      previousPattern = last[previousId];
    };

    getInvertBeat = { |beatAmp, invertAmp = 1|
      var invertBeat = beatAmp.iter.loop.nextN(steps).linlin(0, invertAmp, invertAmp, Rest());
      var weight = pattern[\weight] ?? 1;

      thisThread.randSeed = this.prGetPatternSeed(pattern);

      invertBeat.collect { |step|
        if (step == invertAmp) {
          step = [0, invertAmp].wchoose([1 - weight, weight]);
        };

        step;
      };
    };

    getTotalBeat = { |invertBeat|
      var beat = previousPattern[\totalBeats] ?? Array.fill(steps, 0);
      (beat + invertBeat) collect: { |step| step.clip(0, 1) };
    };

    if (previousPattern.notNil)
    { previousBeats = previousPattern[\rhythmBeats] ?? previousPattern[\totalBeats] };

    if (previousBeats.isNil) {
      this.prPrint("🔴 \\" ++ pattern[\id] + "found no rhythm to fill on the previous pattern");
      ^0;
    };

    invertBeat = getInvertBeat.(previousBeats, amp);
    totalBeat = getTotalBeat.(invertBeat);

    last[pattern[\id]].putAll([\totalBeats, totalBeat]);
    ^totalBeat;
  }

  *prFindFillDependent { |pattern|
    var dependent;

    if (pattern[\drumMachineIntegerId].notNil) {
      var nextIntegerId = pattern[\drumMachineIntegerId].asInteger + 1;
      dependent = last.detect({ |p| p[\drumMachineIntegerId] == nextIntegerId });
    } {
      var nextId = (pattern[\id].asInteger + 1).asSymbol;
      dependent = last[nextId];
    };

    if (dependent.notNil and: { dependent[\fill].notNil })
    { ^dependent };

    ^nil;
  }

  *prRhythmChanged { |previousRhythm, currentRhythm|
    if (previousRhythm.isArray.not or: { currentRhythm.isArray.not })
    { ^false };

    ^previousRhythm != currentRhythm;
  }

  *prReevaluateFillDependents { |pattern|
    var dependent = this.prFindFillDependent(pattern);

    if (dependent.notNil)
    { this.prReevaluate([dependent]) };
  }
}
