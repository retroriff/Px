PxDebouncer {
  classvar <>current;
  classvar <queue;
  var <original;
  var <pattern;
  var pending;
  var scheduled;

  *initClass {
    queue = IdentitySet.new;
  }

  init { |number, capturedPattern|
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

  commit {
    var pairs = [];

    if (pending.size == 0)
    { ^this };

    pending.do { |p| pairs = pairs ++ p };
    original.prUpdatePattern(pairs, pattern);
    pending.clear;
    queue.remove(this);

    if (this === current)
    { current = nil };
  }

  enqueue { |pair|
    pending.add(pair);
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
