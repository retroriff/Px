+ Px {
  *prCreateSwing { |pattern, pbindef|
    var offset, period, swing = pattern[\swing];

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

    if (period.isNumber.not or: { period <= 0 })
    { ^pbindef };

    if (swing.isKindOf(Pattern).not and: { offset.isNumber.not or: { offset <= 0 } })
    { ^pbindef };

    ^Prout({ |inval|
      var appendEvent, elapsed = 0, queue = List.new, scheduledElapsed = 0, source = pbindef.asStream;
      var sourceEnded = false, swingStream;

      if (swing.isKindOf(Pattern))
      { swingStream = swing.asStream };

      appendEvent = { |input|
        var amp, channelCount = 1, currentOffset = 0, delta, dur, event = source.next(input), nextElapsed, nextPeriods, onsets, periods, start = scheduledElapsed, timingOffset;

        if (event.notNil) {
          dur = event[\dur];

          if (dur.isKindOf(Rest))
          { dur = dur.value };

          if (dur.isNumber) {
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
          timingOffset = event[\timingOffset] ?? 0;

          if (amp.isArray)
          { channelCount = channelCount.max(amp.size) };

          if (timingOffset.isArray)
          { channelCount = channelCount.max(timingOffset.size) };

          onsets = Array.fill(channelCount, { |index|
            var onset, value = timingOffset;

            if (timingOffset.isArray)
            { value = timingOffset.wrapAt(index) };

            if (value.isNumber)
            { onset = start + value };

            onset;
          });

          queue.add((event: event, onsets: onsets, swingOffset: currentOffset));
        };

        event;
      };

      appendEvent.(inval);

      while { queue.notEmpty } {
        var current = queue.first;

        if (current[\swingOffset].isNumber and: { current[\swingOffset] > 0 }
          and: { this.prSwingEventAudible(current[\event]) }) {
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

              if (onset.isNumber and: { this.prSwingEventAudible(current[\event], index) }) {
                collides = future.any { |next|
                  next[\onsets].any { |nextOnset, nextIndex|
                    nextOnset.isNumber
                    and: { (nextOnset - onset).abs < 0.000001 }
                    and: { this.prSwingEventAudible(next[\event], nextIndex) };
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
