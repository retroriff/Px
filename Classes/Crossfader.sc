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

  *fadeIn { |id, fadeTime|
    var seconds = fadeTime ?? defaultFadeTime;
    ~animatronNetAddr.sendMsg("/sc/start", id, seconds);
    Ndef(id).play(fadeTime: seconds);
  }

  *fadeOut { |id, fadeTime|
    var seconds = fadeTime ?? defaultFadeTime;
    ~animatronNetAddr.sendMsg("/sc/stop", id, seconds);
    Ndef(id).clear(seconds);
  }
}

FadeIn {
  *new { |id, fadeTime|
    Crossfader.fadeIn(id, fadeTime);
  }
}

FadeOut {
  *new { |id, fadeTime|
    Crossfader.fadeOut(id, fadeTime);
  }
}

+ Ndef {
  in { |fadeTime|
    Crossfader.fadeIn(this.key, fadeTime);
  }
}