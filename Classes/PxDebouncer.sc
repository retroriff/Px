PxDebouncer {
  var <original;
  var pending;
  var scheduled;

  init { |number|
    original = number;
    pending = List.new;
    scheduled = false;
  }

  *new { |number|
    ^super.new.init(number)
  }

  commit {
    var pairs = [];
    pending.do { |p| pairs = pairs ++ p };
    original.prUpdatePattern(pairs);
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

    ^this
  }

  doesNotUnderstand { |selector, args|
    var pair;

    if (args.size == 1) {
      pair = [selector, args[0]];
    } {
      pair = [selector, args];
    };

    ^this.enqueue(pair);
  }

  printOn { |stream|
    original.printOn(stream);
  }
}
