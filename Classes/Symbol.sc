+Symbol {
    fadeTo { |b|
        ^Crossfader(this, b);
    }

    in { |fadeTime|
        ^FadeIn(this, fadeTime);
    }

    play {
        ^Ndef(this).play;
    }

    out { |fadeTime|
        ^FadeOut(this, fadeTime);
    }

    stop {
        ^Ndef(this).stop;
    }
}
