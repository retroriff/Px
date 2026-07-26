+ Px {
  *gui { |value|
    if (value == 0) {
      window.close;
    };

    if (window.notNil) {
      ^this.prUpdateGui;
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
      .stringColor_(Color.white);
    };

    if (sliders.size > 0)
    { sliders do: { |slider| layout.add(slider); } }
    { layout.add(emptyPatternsText.value); };

    window.layout_(layout);
    this.prStartMeterRoutine;
  }

  *prGenerateSliders {
    var patterns = last.reject { |pattern| pattern[\lx] == true };
    var patternsFormatted = lastFormatted;
    var meterColor = Color.new255(37, 190, 106);
    var meterBgColor = Color.new255(31, 41, 55);
    var sortedKeys = Px.prSortedPatternIds(patterns);

    meterViews = Array.new;

    ^sortedKeys collect: { |key|
      var pattern = patterns[key];
      var patternFormatted = patternsFormatted[key];
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
      var amp = this.prGetAmp(patternFormatted[\amp]);
      var backgroundColor = Color.new255(26, 29, 34);
      var button, numberBox, slider, staticText;

      var label = {
        if (pattern[\drumMachine].notNil)
        { "🛢️" + patternLabel }
        { this.prTruncateText(pattern[\id].asString + patternLabel) };
      };

      var staticTextColor = {
        if (pattern[\drumMachine].notNil)
        { Color.new255(255, 255, 122) }
        { colors[pattern[\id]] }
      };

      var slideAction = { |value|
        var newAmp = this.prSetAmp(patternFormatted[\amp], value);
        pattern[\id].asInteger.set(1).amp(newAmp);
        numberBox.value_(value);
      };

      if (colors[pattern[\id]].isNil)
      { colors[pattern[\id]] = Color.rand };

      // StaticText
      staticText = StaticText()
      .align_(\center)
      .background_(staticTextColor.value)
      .mouseDownAction_({
        (pattern[\id].asString + pattern.asString).postln
      })
      .string_(label.value);

      // NumberBox
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

      // Slider
      slider = Slider()
      .action_({
        numberBox.value_(slider.value);
      })
      .backColor_(backgroundColor)
      .mouseUpAction_({
        slideAction.(slider.value);
      })
      .value_(amp);

      // Button
      button = Button()
      .states_([
        ["🟢", Color.white, Color.new255(32, 42, 55)],
        ["⬜️", Color.grey, Color.new255(32, 42, 55)]
      ])
      .action_({ |btn|
        if (btn.value == 0)
        { Px.resume(pattern[\id]) }
        { Px.pause(pattern[\id]) };
      });

      if (pausedPatterns.includes(pattern[\id].asSymbol))
      { button.value_(1) };

      {
        var meterView;

        meterView = UserView()
        .minHeight_(20)
        .drawFunc_({ |v|
          var bounds = v.bounds.moveTo(0, 0);
          var level = meterLevels[pattern[\id]] ?? 0;
          var normalized = level.ampdb.linlin(-40, 0, 0, 1).clip(0, 1);
          var fillWidth = normalized * bounds.width;

          Pen.fillColor = meterBgColor;
          Pen.addRoundedRect(bounds, 3, 3);
          Pen.fill;

          if (fillWidth > 0) {
            Pen.push;
            Pen.addRoundedRect(bounds, 3, 3);
            Pen.clip;
            Pen.fillColor = meterColor;
            Pen.fillRect(Rect(0, 0, fillWidth, bounds.height));
            Pen.pop;
          };

          Pen.strokeColor = Color.grey(0.3);
          Pen.addRoundedRect(bounds, 3, 3);
          Pen.stroke;
        });

        meterViews = meterViews.add(meterView);

        VLayout(staticText, slider, numberBox, meterView, button);
      }.value;
    };
  }

  *prGenerateWindowWidth {
    if (Px.last.reject { |pattern| pattern[\lx] == true }.size > 0)
    { ^windowWidth }
    { ^windowHeight }
  }

  *prGetAmp { |amp|
    var hasPwrand = {
      amp.list[0].isKindOf(Pwrand);
    };

    case
    { amp.isKindOf(Pwhite) }
    { ^amp.hi }

    { amp.isKindOf(Pseq) and: { hasPwrand.value == true } }
    { ^amp.list[0].list.reject { |x| x.isKindOf(Rest) }.maxItem }

    { amp.isKindOf(Pattern) }
    { ^amp.list.reject { |x| x.isKindOf(Rest) }.maxItem }

    { ^amp };
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
    var maxChars = 9;

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
    var bounds;

    if (window.isNil or: { window.visible != true }) {
      ^("🔴 Window is closed");
    };

    bounds = window.bounds;
    window.view.removeAll;
    this.prGenerateLayout;

    bounds.width = this.prGenerateWindowWidth;
    window.bounds = bounds;
  }

}