PxDebouncer {
  classvar <>current;
  var <original;
  var <pattern;
  var pending;
  var scheduled;

  init { |number, capturedPattern|
    original = number;
    pattern = capturedPattern;
    pending = List.new;
    scheduled = false;
  }

  *new { |number, capturedPattern|
    ^super.new.init(number, capturedPattern)
  }

  commit {
    var pairs = [];
    pending.do { |p| pairs = pairs ++ p };
    original.prUpdatePattern(pairs, pattern);
    pending.clear;
  }

  enqueue { |pair|
    pending.add(pair);

    if (scheduled.not) {
      scheduled = true;

      AppClock.sched(0, {
        this.commit;
        scheduled = false;
        nil
      });
    };
  }

  printOn { |stream|
    original.printOn(stream);
  }
}
