Crossfader {
    classvar defaultDuration;
    classvar stepSize;

    *initClass {
        defaultDuration = 20;
        stepSize = 0.1;
    }

    *new { |a, b, fadeDuration|
        this.fadeIn(b, fadeDuration);
        this.fadeOut(a, fadeDuration);
    }

    *fadeIn { |name, fadeDuration|
        this.prCreateFade(name, "in", fadeDuration);
    }

    *fadeOut { |name, fadeDuration|
        this.prCreateFade(name, "out", fadeDuration);
    }

    *prCreateFade { |name, fadeType = "in", customDuration|
        var currentVol = Ndef(name).vol;
        var newVol;
        var fade = 0;
        var fadeDuration = customDuration ?? defaultDuration;
        var fadeStep = stepSize / fadeDuration;
        var numSteps = fadeDuration / stepSize;
        var waitTime = fadeDuration / numSteps;

        fork {
            while { fade < 1 } {
                fade = fade + fadeStep;

                case
                { fadeType == "in" }
                { newVol = (currentVol + fade) }

                { fadeType == "out" }
                { newVol = (currentVol - fade) };

                Ndef(name).vol_(newVol.clip(0, 1));

                waitTime.wait;
            };
            "🎚️ Fade".scatArgs(fadeType, "complete").postln;
        };

    }
}

FadeIn {
    *new { |name, fadeDuration|
        Crossfader.fadeIn(name, fadeDuration);
    }
}

FadeOut {
    *new { |name, fadeDuration|
        Crossfader.fadeOut(name, fadeDuration);
    }
}