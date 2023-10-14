+Event {
    in {
        ^this.putAll([\fade, "in"]);
    }

    out  {
        ^this.putAll([\fade, "out"]);
    }

    reverb {

    }

    solo {
        ^this.putAll([\solo, true]);
    }
}
