Crossfader {
    classvar defaultFadeTime;
    classvar stepSize;

    *initClass {
        defaultFadeTime = 30;
        stepSize = 0.1;
    }

    *new { |a, b, fadeDuration|
        this.fadeIn(b, fadeDuration);
        this.fadeOut(a, fadeDuration);
    }

    *fadeIn { |name, fadeTime|
        Ndef(name).play(fadeTime: fadeTime ?? defaultFadeTime);
    }

    *fadeOut { |name, fadeTime|
        Ndef(name).stop(fadeTime: fadeTime ?? defaultFadeTime);
    }
}

FadeIn {
    *new { |name, fadeTime|
        Crossfader.fadeIn(name, fadeTime);
    }
}

FadeOut {
    *new { |name, fadeTime|
        Crossfader.fadeOut(name, fadeTime);
    }
}
