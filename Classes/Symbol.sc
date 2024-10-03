+Symbol {
    fadeTo { |b|
        ^Crossfader(this, b);
    }

    in { |fadeTime|
        ^FadeIn(this, fadeTime);
    }

    play { |value|
        if (value.isNil)
        { ^Ndef(this).play }
        { Px.stop(this) };
    }

    out { |fadeTime|
        ^FadeOut(this, fadeTime);
    }

    stop {
        ^Ndef(this).stop;
    }
}
