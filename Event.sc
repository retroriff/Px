+Event {
    delay {
        this.fx(\delay);
    }

    fx { |fx|
        ^this.[\fxMethod] = this.[\fxMethod] ++ [fx];
    }

    in {
        ^this.putAll([\fade, "in"]);
    }

    out  {
        ^this.putAll([\fade, "out"]);
    }

    reverb {
        this.fx(\reverb);
    }

    solo {
        ^this.putAll([\solo, true]);
    }

    wah {
        this.fx(\wah);
    }
}
