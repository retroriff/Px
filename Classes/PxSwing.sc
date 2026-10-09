+ Px {
  *prCreateSwing { |pattern, pbindef|
    var clone = pattern[\clone], cloneEnabled, offset, period, swing = pattern[\swing], swingEnabled = false;

    cloneEnabled = clone.isKindOf(Pattern) or: { clone.isNumber and: { clone > 0 } };

    if (swing.isNumber) {
      offset = swing;
      period = 1;
    };

    if (swing.isArray) {
      offset = swing[0];
      period = swing[1] ?? 1;
    };

    if (swing.isKindOf(Pattern)) {
      period = 1;
    };

    if (period.isNumber and: { period > 0 }) {
      swingEnabled = swing.isKindOf(Pattern)
        or: { offset.isNumber and: { offset > 0 } };
    };

    if (cloneEnabled.not and: { swingEnabled.not })
    { ^pbindef };

    ^Prout({ |inval|
      var appendEvent, elapsed = 0, queue = List.new, scheduledElapsed = 0, source = pbindef.asStream;
      var cloneStream, sourceEnded = false, swingStream;

      if (clone.isKindOf(Pattern))
      { cloneStream = clone.asStream };

      if (swingEnabled and: { swing.isKindOf(Pattern) })
      { swingStream = swing.asStream };

      appendEvent = { |input|
        var amp, channelCount = 1, cloneOffset = 0, cloneStart, currentOffset = 0, delayedFrom, delta, dur, event = source.next(input), lag, nextElapsed, nextPeriods, offsets, onsets, periods, start = scheduledElapsed, timingOffset;

        if (event.notNil) {
          dur = event[\dur];

          if (dur.isKindOf(Rest))
          { dur = dur.value };

          if (swingEnabled and: { dur.isNumber }) {
            nextElapsed = elapsed + dur;
            periods = ((elapsed / period) + 0.000001).floor;
            nextPeriods = ((nextElapsed / period) + 0.000001).floor;

            if (periods < nextPeriods) {
              if (swingStream.notNil)
              { currentOffset = swingStream.next(event) }
              { currentOffset = offset };

              if (currentOffset.isNumber and: { currentOffset > 0 })
              { event[\timingOffset] = (event[\timingOffset] ?? 0) + currentOffset };
            };

            elapsed = nextElapsed;
          };

          delta = event.delta;

          if (delta.isKindOf(Rest))
          { delta = delta.value };

          if (delta.isNumber)
          { scheduledElapsed = scheduledElapsed + delta };

          amp = event[\amp];
          lag = event[\lag] ?? 0;
          timingOffset = event[\timingOffset] ?? 0;

          if (amp.isArray)
          { channelCount = channelCount.max(amp.size) };

          if (lag.isArray)
          { channelCount = channelCount.max(lag.size) };

          if (timingOffset.isArray)
          { channelCount = channelCount.max(timingOffset.size) };

          if (cloneEnabled and: { this.prSwingEventAudible(event) }) {
            if (cloneStream.notNil)
            { cloneOffset = cloneStream.next(event) }
            { cloneOffset = clone };

            if (cloneOffset.isNumber and: { cloneOffset > 0 }) {
              cloneStart = channelCount;
              offsets = Array.fill(channelCount, { |index|
                var value = timingOffset;

                if (timingOffset.isArray)
                { value = timingOffset.wrapAt(index) };

                value;
              });
              timingOffset = offsets ++ offsets.collect { |value| value + cloneOffset };
              event[\timingOffset] = timingOffset;
              channelCount = channelCount * 2;
            };
          };

          if (currentOffset.isNumber and: { currentOffset > 0 })
          { delayedFrom = 0 }
          { delayedFrom = cloneStart };

          onsets = Array.fill(channelCount, { |index|
            var lagValue = lag, onset, value = timingOffset;

            if (lag.isArray)
            { lagValue = lag.wrapAt(index) };

            if (timingOffset.isArray)
            { value = timingOffset.wrapAt(index) };

            if (value.isNumber and: { lagValue.isNumber })
            { onset = start + value + (lagValue * TempoClock.default.tempo) };

            onset;
          });

          queue.add((cloneStart: cloneStart, delayedFrom: delayedFrom, event: event, onsets: onsets));
        };

        event;
      };

      appendEvent.(inval);

      while { queue.notEmpty } {
        var current = queue.first;

        if (current[\delayedFrom].notNil and: { this.prSwingEventAudible(current[\event]) }) {
          var amp, collisions, future = Array.new;
          var maxOnset = current[\onsets].select(_.isNumber).maxItem;

          if (maxOnset.notNil) {
            while { sourceEnded.not and: { scheduledElapsed <= (maxOnset + 0.000001) } } {
              if (appendEvent.(inval).isNil)
              { sourceEnded = true };
            };

            if (queue.size > 1)
            { future = queue.copyRange(1, queue.size - 1) };

            collisions = current[\onsets].collect { |onset, index|
              var collides = false;

              if (index >= current[\delayedFrom]
                and: { onset.isNumber }
                and: { this.prSwingEventAudible(current[\event], index) }) {
                if (current[\cloneStart].notNil and: { index >= current[\cloneStart] }) {
                  collides = (0 .. (index - 1)).any { |previousIndex|
                    var previousOnset = current[\onsets][previousIndex];

                    previousOnset.isNumber
                    and: { (previousOnset - onset).abs < 0.000001 }
                    and: { this.prSwingEventAudible(current[\event], previousIndex) };
                  };
                };

                if (collides.not) {
                  collides = future.any { |next|
                    next[\onsets].any { |nextOnset, nextIndex|
                      nextOnset.isNumber
                      and: { (nextOnset - onset).abs < 0.000001 }
                      and: { this.prSwingEventAudible(next[\event], nextIndex) };
                    };
                  };
                };
              };

              collides;
            };

            if (collisions.any { |collision| collision }) {
              if (collisions.every { |collision| collision })
              { current[\event][\type] = \rest }
              {
                amp = current[\event][\amp] ?? defaultAmp;
                current[\event][\amp] = collisions.collect { |collision, index|
                  var value = amp;

                  if (amp.isArray)
                  { value = amp.wrapAt(index) };

                  if (collision)
                  { value = 0 };

                  value;
                };
              };
            };
          };
        };

        inval = current[\event].yield;
        queue.removeAt(0);

        if (queue.isEmpty and: { sourceEnded.not }) {
          if (appendEvent.(inval).isNil)
          { sourceEnded = true };
        };
      };
    });
  }

  *prSwingAmpAudible { |amp|
    if (amp.isKindOf(Rest))
    { ^false };

    if (amp.isNumber)
    { ^amp != 0 };

    ^true;
  }

  *prSwingEventAudible { |event, index|
    var amp = event[\amp];

    if (event[\type] == \rest or: { event[\dur].isKindOf(Rest) })
    { ^false };

    if (amp.isArray) {
      if (index.isNumber)
      { ^this.prSwingAmpAudible(amp.wrapAt(index)) };

      ^amp.any { |value| this.prSwingAmpAudible(value) };
    };

    ^this.prSwingAmpAudible(amp);
  }
}
