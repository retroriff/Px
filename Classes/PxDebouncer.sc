PxDebouncer {
  classvar <>current;
  classvar <pendingSyncs;
  classvar <queue;
  var <>fxList;
  var <>isFullDeclaration;
  var <original;
  var <pattern;
  var pending;
  var scheduled;

  *initClass {
    pendingSyncs = 0;
    queue = IdentitySet.new;
  }

  init { |number, capturedPattern|
    fxList = List.new;
    isFullDeclaration = false;
    original = number;
    pattern = capturedPattern;
    pending = List.new;
    scheduled = false;
  }

  *new { |number, capturedPattern|
    ^super.new.init(number, capturedPattern)
  }

  *flush {
    queue.copy.do { |debouncer|
      debouncer.commit
    };
  }

  *prResetSyncs {
    pendingSyncs = 0;
  }

  commit {
    var pairs = [];
    var capturedFxList, capturedIsFullDeclaration;

    if (pending.size == 0 and: { fxList.size == 0 } and: { isFullDeclaration.not })
    { ^this };

    pairs = pending.flatten;
    capturedFxList = fxList;
    capturedIsFullDeclaration = isFullDeclaration;

    pending.clear;
    fxList = List.new;
    isFullDeclaration = false;
    queue.remove(this);

    if (this === current)
    { current = nil };

    original.prUpdatePattern(pairs, pattern);

    if (pattern.notNil and: { Px.last[pattern[\id]].notNil }) {
      var capturedId = pattern[\id];
      var capturedPreviousFx = Fx.prFxNames(capturedId);
      var capturedGeneration = Fx.generation;

      pendingSyncs = pendingSyncs + 1;

      fork {
        Server.default.sync;

        if (capturedGeneration == Fx.generation) {
          Px.prApplyFx(capturedId, capturedFxList, capturedIsFullDeclaration, capturedPreviousFx);
          pendingSyncs = (pendingSyncs - 1).max(0);
        };
      };
    };
  }

  enqueue { |pair|
    pending.add(pair);
    this.prSchedule;
  }

  prSchedule {
    queue.add(this);

    if (scheduled.not) {
      scheduled = true;

      AppClock.sched(0, {
        this.commit;
        scheduled = false;
        nil
      });
    };
  }

  prTakePending {
    var taken = pending.copy;
    pending.clear;
    queue.remove(this);
    ^taken;
  }

  printOn { |stream|
    original.printOn(stream);
  }
}
