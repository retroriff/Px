+ Px {
  *gui {
    if (window.notNil) {
      ^window.close;
    };

    window = Window(
      "🪩 The music was new, black polished chrome, and came over the summer, like liquid night.",
      Rect(0, Window.screenBounds.height, this.prGenerateWindowWidth, windowHeight)
    )
    .alwaysOnTop_(true)
    .background_(
      Color.new255(
        red: 24,
        green: 24,
        blue: 24
      )
    )
    .front
    .onClose_({
      if (Px.meterRoutine.notNil)
      { Px.meterRoutine.stop };

      Px.patternViews.clear;
      Px.window = nil;
    });

    this.prGenerateLayout;

    CmdPeriod.add {
      if (Px.window.notNil)
      { Px.window.close };
    }
  }

  *prGenerateLayout {
    var layout = HLayout();
    var sliders = this.prGenerateSliders;

    var emptyPatternsText = {
      StaticText()
      .align_(\center)
      .string_("🔴 Px is not running")
      .stringColor_(Color.white)
      .setProperty(\wordWrap, false);
    };

    if (sliders.size > 0)
    { sliders do: { |slider| layout.add(slider); } }
    { layout.add(emptyPatternsText.value); };

    window.layout_(layout);
    this.prStartMeterRoutine;
  }

  *prGenerateSliders {
    var patterns = last.reject { |pattern| pattern[\lx] == true };
    var primaryColor = Color.new255(37, 190, 106);
    var meterBgColor = Color.new255(31, 41, 55);
    var sortedKeys = Px.prSortedPatternIds(patterns);

    meterViews = Array.new;
    patternViews.clear;

    ^sortedKeys collect: { |key|
      var views = this.prBuildPatternView(key, primaryColor, meterBgColor);
      patternViews[key] = views;
      views[\layout];
    };
  }

  *prBuildPatternView { |key, primaryColor, meterBgColor|
    var pattern = last[key];
    var amp = this.prGetAmp(lastFormatted[key][\amp]);
    var backgroundColor = Color.new255(26, 29, 34);
    var meterView, numberBox, pauseButton, slider, soloButton, staticText;

    var slideAction = { |value|
      var currentPattern = last[key];
      var newAmp = this.prSetAmp(lastFormatted[key][\amp], value);

      if (currentPattern.notNil) {
        var number = key.asInteger;
        PxDebouncer.current = PxDebouncer(number, currentPattern);
        number.amp(newAmp);
      };

      numberBox.value_(value);
    };

    if (colors[pattern[\id]].isNil)
    { colors[pattern[\id]] = Color.rand };

    staticText = StaticText()
    .align_(\center)
    .background_(this.prPatternColor(key))
    .mouseDownAction_({
      (key.asString + last[key].asString).postln
    })
    .string_(this.prPatternLabel(key))
    .setProperty(\wordWrap, false);

    numberBox = NumberBox()
    .action_({
      slideAction.(numberBox.value);
      slider.value_(numberBox.value);
    })
    .backColor_(backgroundColor)
    .clipHi_(1)
    .clipLo_(0)
    .scroll_step_(0.01)
    .normalColor_(Color.white)
    .value_(amp);

    slider = Slider()
    .action_({
      numberBox.value_(slider.value);
    })
    .backColor_(backgroundColor)
    .mouseUpAction_({
      slideAction.(slider.value);
    })
    .value_(amp);

    pauseButton = Button()
    .maxWidth_(windowWidth / 2)
    .states_([
      ["🟢", Color.white, Color.new255(32, 42, 55)],
      ["⬜️", Color.grey, Color.new255(32, 42, 55)]
    ])
    .action_({ |btn|
      if (btn.value == 0)
      { Px.resume(key) }
      { Px.pause(key) };
    });

    soloButton = Button()
    .maxWidth_(windowWidth / 2)
    .states_([
      ["S", Color.white, Color.new255(32, 42, 55)],
      ["S", primaryColor, Color.new255(32, 42, 55)]
    ])
    .action_({ |btn|
      if (btn.value == 0)
      { Px.unsolo }
      { Px.solo(key) };
    });

    if (pausedPatterns.includes(key.asSymbol))
    { pauseButton.value_(1) };

    if (mutedPatterns.notNil and: { mutedPatterns.notEmpty })
    { soloButton.value_(1) };

    meterView = UserView()
    .minHeight_(20)
    .drawFunc_({ |v|
      var bounds = v.bounds.moveTo(0, 0);
      var level = meterLevels[key] ?? 0;
      var normalized = level.ampdb.linlin(-40, 0, 0, 1).clip(0, 1);
      var fillWidth = normalized * bounds.width;

      Pen.fillColor = meterBgColor;
      Pen.addRoundedRect(bounds, 3, 3);
      Pen.fill;

      if (fillWidth > 0) {
        Pen.push;
        Pen.addRoundedRect(bounds, 3, 3);
        Pen.clip;
        Pen.fillColor = primaryColor;
        Pen.fillRect(Rect(0, 0, fillWidth, bounds.height));
        Pen.pop;
      };

      Pen.strokeColor = Color.grey(0.3);
      Pen.addRoundedRect(bounds, 3, 3);
      Pen.stroke;
    });

    meterViews = meterViews.add(meterView);

    ^(
      staticText: staticText,
      slider: slider,
      numberBox: numberBox,
      pauseButton: pauseButton,
      soloButton: soloButton,
      meterView: meterView,
      layout: VLayout(
        staticText,
        slider,
        numberBox,
        meterView,
        HLayout(pauseButton, soloButton).margins_(0).spacing_(2)
      )
    );
  }

  *prPatternLabel { |key|
    var pattern = last[key];
    var chan = pattern[\chan] !? { "chan" + pattern[\chan] };
    var play = pattern[\play] !? {
      case
      { pattern[\play].isArray }
      { pattern[\play][0] }

      { pattern[\play].isKindOf(Buffer) and: { pattern[\play].path.notNil } }
      { PathName(pattern[\play].path).parentPath.basename }

      { pattern[\play].asString }
    };
    var loop = pattern[\loop] !? {
      if (pattern[\loop].isArray)
      { pattern[\loop][0] }
      { pattern[\loop].asString }
    };
    var patternLabel = pattern[\name] ?? pattern[\instrument] ?? chan ?? play ?? loop ?? key;

    if (pattern[\drumMachine].notNil)
    { ^("🛢️" + patternLabel) };

    ^this.prTruncateText(pattern[\id].asString + patternLabel);
  }

  *prPatternColor { |key|
    var pattern = last[key];

    if (pattern[\drumMachine].notNil)
    { ^Color.new255(255, 255, 122) };

    ^colors[pattern[\id]];
  }

  *prGenerateWindowWidth {
    if (Px.last.reject { |pattern| pattern[\lx] == true }.size > 0)
    { ^windowWidth }
    { ^windowHeight }
  }

  *prGetAmp { |amp|
    ^this.prExtractAmpMax(amp);
  }

  *prSetAmp { |originalAmp, newMax|
    var oldMax = this.prGetAmp(originalAmp);
    var ratio;

    if (oldMax == 0 or: { newMax == 0 }) { ^newMax };

    ratio = newMax / oldMax;

    if (originalAmp.isKindOf(Pwhite)) {
      ^Pwhite(originalAmp.lo * ratio, originalAmp.hi * ratio)
    };

    if (originalAmp.isKindOf(Pattern)) {
      originalAmp.list = originalAmp.list.collect { |x|
        if (x.isKindOf(Rest)) { x } { x * ratio }
      };
      ^originalAmp
    };

    ^newMax;
  }

  *prAutoRefreshGui {
    AppClock.sched(0, {

      if (window.notNil and: { window.visible == true }) {

        if (last.size == 0)
        { window.close }
        { this.prUpdateGui };
      };

      nil;
    });
  }

  *prTruncateText { |text|
    var maxChars = 8;

    if (text.size > maxChars) {
      text = text.copyRange(0, maxChars - 1) ++ "…";
    };

    ^text;
  }

  *prStartMeterRoutine {
    if (meterRoutine.notNil)
    { meterRoutine.stop; meterRoutine = nil };

    meterRoutine = Routine({
      inf.do {
        meterLevels.keysDo { |id|

          if (pausedPatterns.includes(id.asSymbol))
          { meterLevels[id] = 0 }
          { meterLevels[id] = meterLevels[id] * 0.7 };
        };

        meterViews.do { |v|

          if (v.isClosed.not)
          { v.refresh };
        };

        0.05.wait;
      };
    }).play(AppClock);
  }

  *prUpdateGui {
    var bounds, patterns, newKeys, existingKeys;

    if (window.isNil or: { window.visible != true }) {
      ^("🔴 Window is closed");
    };

    patterns = last.reject { |pattern| pattern[\lx] == true };
    newKeys = patterns.keys;
    existingKeys = patternViews.keys;

    if (newKeys.size > 0
      and: { newKeys.size == existingKeys.size }
      and: { newKeys.every { |k| existingKeys.includes(k) } }) {
      ^this.prRefreshPatternViews;
    };

    bounds = window.bounds;
    window.view.removeAll;
    this.prGenerateLayout;

    bounds.width = this.prGenerateWindowWidth;
    window.bounds = bounds;
  }

  *prRefreshPatternViews {
    patternViews keysValuesDo: { |key, views|
      var amp = this.prGetAmp(lastFormatted[key][\amp]);
      var paused = pausedPatterns.includes(key.asSymbol);
      var soloed = mutedPatterns.notNil and: { mutedPatterns.notEmpty };

      views[\staticText].string_(this.prPatternLabel(key));
      views[\staticText].background_(this.prPatternColor(key));
      views[\slider].value_(amp);
      views[\numberBox].value_(amp);
      views[\pauseButton].value_(if (paused) { 1 } { 0 });
      views[\soloButton].value_(if (soloed) { 1 } { 0 });
    };
  }

}