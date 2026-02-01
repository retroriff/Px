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
    if (pending.notEmpty) {
      original.prUpdatePattern(pending.flat);
      pending.clear;
    };
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

  *wrap { |obj|
    // If obj is already a wrapper, return it
    obj.isKindOf(this).if { ^obj };

    // Otherwise create wrapper
    ^this.new(obj)
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
