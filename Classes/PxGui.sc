+ Px {
  *gui {
    if (window.notNil) {
      ^window.close;
    };

    window = Window(
      "🪩 The music was new, black polished chrome, and came over the summer, like liquid night.",
      this.prWindowBounds
    )
    .alwaysOnTop_(true)
    .background_(this.prWindowBgColor)
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

  *meter {
    var width;

    if (meterWindow.notNil)
    { ^meterWindow.close };

    meterWindow = Window("", Rect(0, 0, windowWidth, windowHeight))
    .alwaysOnTop_(true)
    .background_(this.prWindowBgColor)
    .onClose_({
      Px.prStopMasterMeterRoutine;
      Px.prStopMasterMeter;
      Px.masterMeterViews = [];
      Px.meterWindow = nil;
    });

    meterWindow.layout_(this.prGenerateServerMeterLayout);

    width = meterWindow.view.sizeHint.width;
    meterWindow.view.fixedWidth_(width);
    meterWindow.bounds = this.prMeterWindowBounds(width);
    meterWindow.front;

    this.prStartMasterMeter;
    this.prStartMasterMeterRoutine;

    CmdPeriod.add {
      if (Px.meterWindow.notNil)
      { Px.meterWindow.close };
    }
  }

  *prPrimaryColor {
    ^Color.new255(37, 190, 106);
  }

  *prControlBgColor {
    ^Color.new255(26, 29, 34);
  }

  *prMeterBgColor {
    ^Color.new255(31, 41, 55);
  }

  *prBorderColor {
    ^Color.grey(0.6);
  }

  *prButtonBgColor {
    ^Color.new255(32, 42, 55);
  }

  *prMutedColor {
    ^this.prControlBgColor.blend(this.prWindowBgColor, 0.5);
  }

  *prWindowBgColor {
    ^Color.new255(24, 24, 24);
  }

  *prDrumMachineColor {
    ^Color.new255(255, 255, 122);
  }

  *prWarningColor {
    ^Color.new255(250, 204, 21);
  }

  *prCriticalColor {
    ^Color.new255(239, 68, 68);
  }

  *prServerMeterBarWidth {
    ^24;
  }

  *prServerMeterDbLow {
    ^-80;
  }

  *prServerMeterTickWidth {
    ^6;
  }

  *prServerMeterSpacing {
    ^4;
  }

  *prServerMeterWidth {
    var channels = masterLevels.size;

    ^(channels * this.prServerMeterBarWidth) + ((channels - 1) * this.prServerMeterSpacing);
  }

  *prMasterVolumeRange {
    ^[-10, 10];
  }

  *prMeterWindowBounds { |width|
    var screen = Window.availableBounds;
    var height = windowHeight + this.prServerMeterWidth + this.prServerMeterSpacing;

    ^Window.flipY(Rect(screen.right - width, screen.top, width, height));
  }

  *prGenerateServerMeterLayout {
    var meters = HLayout().margins_(0).spacing_(this.prServerMeterSpacing);

    masterMeterViews = masterLevels.collect { this.prGenerateServerMeterBar };

    masterMeterViews do: { |bar, channel|
      meters.add(this.prGenerateServerMeterColumn(bar, channel));
    };

    ^VLayout(
      [meters, stretch: 1],
      [this.prGenerateMasterVolumeKnob, align: \center]
    ).spacing_(this.prServerMeterSpacing);
  }

  *prGenerateMasterVolumeKnob {
    var range = this.prMasterVolumeRange;
    var size = this.prServerMeterWidth;
    var volume = Server.default.volume;
    var knob, knobColor;

    knob = Knob()
    .centered_(true)
    .fixedSize_(size @ size)
    .mode_(\vert)
    .value_(volume.volume.linlin(range[0], range[1], 0, 1))
    .action_({ |view|
      volume.volume_(view.value.linlin(0, 1, range[0], range[1]));
    })
    .mouseDownAction_({ |view, x, y, modifiers, buttonNumber, clickCount|

      if (clickCount == 2) {
        view.valueAction_(0.5);
        true;
      };
    });

    knobColor = knob.color;
    knobColor[1] = Color.cyan;
    knob.color = knobColor;

    ^knob;
  }

  *prGenerateServerMeterBar {
    ^LevelIndicator()
    .background_(this.prMeterBgColor)
    .critical_(1.0)
    .criticalColor_(this.prCriticalColor)
    .drawsPeak_(true)
    .fixedWidth_(this.prServerMeterBarWidth)
    .meterColor_(this.prPrimaryColor)
    .numMajorTicks_(3)
    .numTicks_(9)
    .palette_(QPalette.new.windowText_(Color.white))
    .warning_(0.9)
    .warningColor_(this.prWarningColor);
  }

  *prGenerateServerMeterColumn { |bar, channel|
    var frame = UserView()
    .drawFunc_({ |view|
      var rect = view.bounds.moveTo(0, 0).insetBy(0.5, 0.5);

      rect.width = rect.width - this.prServerMeterTickWidth;

      Pen.strokeColor = Color.white;
      Pen.addRoundedRect(rect, 3, 3);
      Pen.stroke;
    });

    var label = StaticText()
    .align_(\center)
    .font_(Font.sansSerif(9).boldVariant)
    .string_(channel.asString)
    .stringColor_(Color.white);

    ^VLayout(
      [StackLayout(bar, frame).mode_(\stackAll).margins_(0), stretch: 1],
      label
    ).margins_(0).spacing_(2);
  }

  *prVisiblePatterns {
    ^(last ++ mutedPatterns).reject { |pattern| pattern[\lx] == true };
  }

  *prPatternAt { |key|
    ^last[key] ?? mutedPatterns[key];
  }

  *prGenerateSliders {
    var patterns = this.prVisiblePatterns;
    var sortedKeys = Px.prSortedPatternIds(patterns);

    meterViews = Array.new;
    patternViews.clear;

    ^sortedKeys collect: { |key|
      var views = this.prBuildPatternView(key);
      patternViews[key] = views;
      views[\layout];
    };
  }

  *prToggleSolo { |key|
    var id = key.asSymbol;

    if (mutedPatterns[id].notNil)
    { ^this.unsolo(id) };

    if (mutedPatterns.notEmpty)
    { ^this.unsolo };

    ^this.solo(id);
  }

  *prTogglePause { |key|
    var id = key.asSymbol;

    if (mutedPatterns[key].notNil)
    { ^nil };

    if (pausedPatterns.includes(id))
    { this.resume(id) }
    { this.pause(id) };

    patternViews[key] !? { |views| this.prApplyPatternState(views, key) };
  }

  *prMutedAlpha {
    ^0.3;
  }

  *prMutedTone { |color, muted|
    if (muted)
    { ^color.copy.alpha_(this.prMutedAlpha) };

    ^color;
  }

  *prPauseIconSize {
    ^16;
  }

  *prControlHeight {
    ^24;
  }

  *prButtonSpacing {
    ^2;
  }

  *prLabelFont {
    ^Font.default;
  }

  *prLabelTextColor {
    ^Color.black;
  }

  *prLabelVerticalPadding {
    ^2;
  }

  *prLabelHorizontalPadding {
    ^4;
  }

  *prDrawnViewInset {
    ^1;
  }

  *prPauseIcon { |paused, muted|
    var resolution = 2;
    var size = this.prPauseIconSize * resolution;
    var bounds = Rect(0, 0, size, size).insetBy(resolution);
    var color = if (paused) { Color.white } { this.prPrimaryColor };
    var image = Image(size);

    image.fill(Color.clear);
    image.draw {
      Pen.fillColor = this.prMutedTone(color, muted);

      if (paused)
      { Pen.addRoundedRect(bounds.insetBy(resolution), 2 * resolution, 2 * resolution) }
      { Pen.addOval(bounds) };

      Pen.fill;
    };

    ^image;
  }

  *prApplyPatternState { |views, key|
    var muted = mutedPatterns[key].notNil;
    var paused = pausedPatterns.includes(key.asSymbol);
    var controlBgColor = this.prMutedTone(this.prControlBgColor, muted);
    var label = this.prPatternLabel(key);

    views[\soloButton].value_(
      if (muted)
      { 2 }
      { if (mutedPatterns.notEmpty) { 1 } { 0 } }
    );

    views[\label] = label;
    views[\labelBgColor] = this.prMutedTone(this.prPatternColor(key), muted);
    views[\labelTextColor] = this.prMutedTone(this.prLabelTextColor, muted);

    views[\labelView]
    .minWidth_(label.bounds(this.prLabelFont).width + this.prLabelHorizontalPadding)
    .refresh;

    views[\slider].backColor_(controlBgColor);
    views[\numberBox].normalColor_(this.prMutedTone(Color.white, muted));
    views[\numberBoxFrame].refresh;

    views[\pauseButton].icon_(this.prPauseIcon(paused, muted));

    views[\slider].enabled_(muted.not);
    views[\numberBox].enabled_(muted.not);
  }

  *prBuildPatternView { |key|
    var pattern = this.prPatternAt(key);
    var formatted = lastFormatted[key] ?? Dictionary.new;
    var amp = this.prGetAmp(formatted[\amp]);
    var backgroundColor = this.prControlBgColor;
    var borderColor = this.prBorderColor;
    var buttonBgColor = this.prButtonBgColor;
    var meterBgColor = this.prMeterBgColor;
    var mutedColor = this.prMutedColor;
    var primaryColor = this.prPrimaryColor;
    var labelFont = this.prLabelFont;
    var midiFont = Font.sansSerif(10);
    var buttonSpacing = this.prButtonSpacing;
    var controlHeight = this.prControlHeight;
    var inset = this.prDrawnViewInset;
    var buttonRow, labelView, meterView, numberBox, numberBoxFrame, pauseButton, slider, soloButton, views;

    var slideAction = { |value|
      var currentPattern = last[key];
      var newAmp = this.prSetAmp((lastFormatted[key] ?? Dictionary.new)[\amp], value);

      if (currentPattern.notNil) {
        var number = key.asInteger;
        PxDebouncer.current = PxDebouncer(number, currentPattern);
        number.amp(newAmp);
      };

      numberBox.value_(value);
    };

    if (colors[pattern[\id]].isNil)
    { colors[pattern[\id]] = Color.rand };

    labelView = UserView()
    .fixedHeight_("Ag".bounds(labelFont).height + (this.prLabelVerticalPadding * 2))
    .mouseDownAction_({
      (key.asString + this.prPatternAt(key).asString).postln
    })
    .drawFunc_({ |view|
      var bounds = view.bounds.moveTo(0, 0).insetBy(inset, 0);

      Pen.fillColor = views[\labelBgColor];
      Pen.addRoundedRect(bounds, 3, 3);
      Pen.fill;

      Pen.stringCenteredIn(views[\label], bounds, labelFont, views[\labelTextColor]);
    });

    numberBox = NumberBox()
    .action_({
      slideAction.(numberBox.value);
      slider.value_(numberBox.value);
    })
    .backColor_(Color.clear)
    .clipHi_(1)
    .clipLo_(0)
    .scroll_step_(0.01)
    .setProperty(\frame, false)
    .value_(amp);

    numberBoxFrame = UserView()
    .minHeight_(controlHeight)
    .layout_(HLayout(numberBox).margins_([inset + 1, 1, inset + 1, 1]))
    .drawFunc_({ |view|
      var muted = mutedPatterns[key].notNil;
      var bounds = view.bounds.moveTo(0, 0).insetBy(inset + 0.5, 0.5);

      Pen.fillColor = this.prMutedTone(backgroundColor, muted);
      Pen.strokeColor = this.prMutedTone(borderColor, muted);
      Pen.addRoundedRect(bounds, 3, 3);
      Pen.fillStroke;
    });

    slider = Slider()
    .action_({
      numberBox.value_(slider.value);
    })
    .backColor_(backgroundColor)
    .mouseUpAction_({
      slideAction.(slider.value);
    })
    .value_(amp);

    soloButton = Button()
    .minSize_(1 @ controlHeight)
    .states_([
      ["S", Color.white, buttonBgColor],
      ["S", buttonBgColor, primaryColor],
      ["S", Color.white, buttonBgColor]
    ])
    .action_({ Px.prToggleSolo(key) });

    pauseButton = Button()
    .minSize_(1 @ controlHeight)
    .iconSize_(this.prPauseIconSize)
    .states_([["", Color.white, buttonBgColor]])
    .action_({ Px.prTogglePause(key) });

    buttonRow = View()
    .minWidth_(windowWidth + buttonSpacing)
    .layout_(
      HLayout([pauseButton, stretch: 1], [soloButton, stretch: 1])
      .margins_(0)
      .spacing_(buttonSpacing)
    )
    .onResize_({ |view|
      var buttonWidth = ((view.bounds.width - buttonSpacing) / 2).floor;

      pauseButton.maxWidth_(buttonWidth);
      soloButton.maxWidth_(buttonWidth);
    });

    meterView = UserView()
    .minHeight_(controlHeight)
    .drawFunc_({ |v|
      var bounds = v.bounds.moveTo(0, 0);
      var level = meterLevels[key] ?? 0;
      var normalized = level.ampdb.linlin(-40, 0, 0, 1).clip(0, 1);
      var fillWidth = normalized * bounds.width;
      var midiChan = this.prPatternAt(key) !? { |pattern| pattern[\chan] };
      var isMuted = mutedPatterns[key].notNil;
      var isPaused = pausedPatterns.includes(key.asSymbol);
      var textColor = this.prMutedTone(if (isPaused) { Color.white } { primaryColor }, isMuted);
      var levelColor = this.prMutedTone(primaryColor, isMuted);
      var strokeColor = this.prMutedTone(borderColor, isMuted);

      Pen.fillColor = if (isMuted) { mutedColor } { meterBgColor };
      Pen.addRoundedRect(bounds, 3, 3);
      Pen.fill;

      case
      { midiChan.notNil } {
        Pen.stringCenteredIn(
          "CHAN" + midiChan,
          bounds.moveBy(0, midiFont.size * 0.15),
          midiFont,
          textColor
        );
      }

      { fillWidth > 0 } {
        Pen.push;
        Pen.addRoundedRect(bounds, 3, 3);
        Pen.clip;
        Pen.fillColor = levelColor;
        Pen.fillRect(Rect(0, 0, fillWidth, bounds.height));
        Pen.pop;
      };

      Pen.strokeColor = strokeColor;
      Pen.addRoundedRect(bounds.insetBy(0.5, 0.5), 3, 3);
      Pen.stroke;
    });

    meterViews = meterViews.add(meterView);

    views = (
      labelView: labelView,
      slider: slider,
      numberBox: numberBox,
      numberBoxFrame: numberBoxFrame,
      pauseButton: pauseButton,
      soloButton: soloButton,
      meterView: meterView,
      layout: VLayout(
        labelView,
        slider,
        numberBoxFrame,
        meterView,
        buttonRow
      )
    );

    this.prApplyPatternState(views, key);

    ^views;
  }

  *prPatternLabel { |key|
    var pattern = this.prPatternAt(key);
    var chan = pattern[\chan] !? { "chan" + pattern[\chan] };
    var play = pattern[\play] !? { this.prSampleLabel(pattern[\play]) };
    var loop = pattern[\loop] !? { this.prSampleLabel(pattern[\loop]) };
    var patternLabel = pattern[\name] ?? pattern[\instrument] ?? chan ?? play ?? loop ?? key;

    if (pattern[\drumMachine].notNil)
    { ^("🛢️" + patternLabel) };

    ^this.prTruncateText(pattern[\id].asString + patternLabel);
  }

  *prSampleLabel { |sample|
    ^case
    { sample.isArray }
    { sample[0] }

    { sample.isKindOf(ListPattern) }
    { this.prSampleLabel(sample.list[0]) }

    { sample.isKindOf(Buffer) and: { sample.path.notNil } }
    { PathName(sample.path).parentPath.basename }

    { sample.asString };
  }

  *prPatternColor { |key|
    var pattern = this.prPatternAt(key);

    if (pattern[\drumMachine].notNil)
    { ^this.prDrumMachineColor };

    ^colors[pattern[\id]];
  }

  *prGenerateWindowWidth {
    if (this.prVisiblePatterns.size > 0)
    { ^windowWidth }
    { ^windowHeight }
  }

  // Top-left corner for a window of the given size placed at the cursor.
  // Window bounds use a bottom-left origin while the cursor is reported from
  // the top-left, so the y coordinate has to be flipped. The flip cancels out
  // for cursors outside the primary screen, but clamping would drag the window
  // back onto it — so only clamp when the cursor is on the primary screen.
  *prMouseOrigin { |width, height|
    var screen = Window.screenBounds;
    var cursor = QtGUI.cursorPosition;
    var left = cursor.x;
    var bottom = screen.height - cursor.y - height;

    if (screen.containsPoint(cursor)) {
      left = left.clip(0, screen.width - width);
      bottom = bottom.clip(0, screen.height - height);
    };

    ^Point(left, bottom);
  }

  *prWindowBounds {
    var width = this.prGenerateWindowWidth;
    var origin = this.prMouseOrigin(width, windowHeight);

    ^Rect(origin.x, origin.y, width, windowHeight);
  }

  *prGetAmp { |amp|
    ^this.prExtractAmpMax(amp);
  }

  *prSetAmp { |originalAmp, newMax|
    var oldMax = this.prGetAmp(originalAmp);
    var ratio;

    if (oldMax == 0 or: { newMax == 0 }) { ^newMax };

    ratio = newMax / oldMax;

    if (originalAmp.isKindOf(Pattern))
    { ^this.prScaleAmp(originalAmp, ratio) };

    ^newMax;
  }

  *prAutoRefreshGui {
    AppClock.sched(0, {

      if (window.notNil and: { window.visible == true }) {

        if (last.size == 0 and: { mutedPatterns.size == 0 })
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

  *prStartMasterMeterRoutine {
    var dbLow = this.prServerMeterDbLow;

    this.prStopMasterMeterRoutine;

    masterMeterRoutine = Routine({
      inf.do {
        masterMeterViews do: { |view, channel|

          if (view.isClosed.not) {
            view.value = (masterLevels[channel] ?? 0).ampdb.linlin(dbLow, 0, 0, 1);
            view.peakLevel = (masterPeaks[channel] ?? 0).ampdb.linlin(dbLow, 0, 0, 1, \min);
          };
        };

        0.05.wait;
      };
    }).play(AppClock);
  }

  *prStopMasterMeterRoutine {
    masterMeterRoutine !? { masterMeterRoutine.stop };
    masterMeterRoutine = nil;
  }

  *prUpdateGui {
    var bounds, patterns, newKeys, existingKeys;

    if (window.isNil or: { window.visible != true }) {
      ^("🔴 Window is closed");
    };

    patterns = this.prVisiblePatterns;
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
      var formatted = lastFormatted[key] ?? Dictionary.new;
      var amp = this.prGetAmp(formatted[\amp]);

      views[\slider].value_(amp);
      views[\numberBox].value_(amp);

      this.prApplyPatternState(views, key);
    };
  }

}
