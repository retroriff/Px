Crossfader {
  classvar defaultFadeTime;
  classvar stepSize;

  *initClass {
    defaultFadeTime = 16;
  }

  *new { |a, b, fadeDuration|
    this.fadeIn(b, fadeDuration);
    this.fadeOut(a, fadeDuration);
  }

  *fadeIn { |name, fadeTime|
    var seconds = fadeTime ?? defaultFadeTime;
    ~animatronNetAddr.sendMsg("/sc/start", name, seconds);
    Ndef(name).play(fadeTime: seconds);
  }

  *fadeOut { |name, fadeTime|
    var seconds = fadeTime ?? defaultFadeTime;
    ~animatronNetAddr.sendMsg("/sc/stop", name, seconds);
    Ndef(name).clear(seconds);
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
