+ Px {
  *gui {
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
    .onClose_({ Px.window = nil });

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
  }

  *prGenerateSliders {
    var patterns = last;
    var patternsFormatted = lastFormatted;

    var drumMachinePatterns = patterns.select { |pattern|
      pattern[\drumMachine].notNil
    }.keys.asSortedList;
    var nonDrumMachinePatterns =  patterns.select { |pattern|
      pattern[\drumMachine].isNil
    }.keys.asSortedList;
    var sortedKeys = drumMachinePatterns ++ nonDrumMachinePatterns;

    ^sortedKeys collect: { |key|
      var pattern = patterns[key];
      var patternFormatted = patternsFormatted[key];
      var chan = pattern[\chan] !? { "chan" + pattern[\chan] };
      var play = pattern[\play] !? { pattern[\play][0] };
      var loop = pattern[\loop] !? { pattern[\loop][0] };
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
        { Color.rand }
      };

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
        pattern[\id].asInteger.set(1).amp(numberBox.value);
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
      .backColor_(backgroundColor)
      .mouseUpAction_({
        pattern[\id].asInteger.set(1).amp(slider.value);
        numberBox.value_(slider.value);
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

      VLayout(staticText, slider, numberBox, button);
    };
  }

  *prGenerateWindowWidth {
    if (Px.last.size > 0)
    { ^windowWidth }
    { ^windowHeight }
  }

  *prGetAmp { |amp|
    case
    { amp.isKindOf(Pwhite) }
    { ^amp.hi }

    { amp.isKindOf(Pattern) }
    { ^amp.list.reject { |x| x.isKindOf(Rest) }.maxItem }

    { ^amp };
  }

  *prTruncateText { |text|
    var maxChars = 8;

    if (text.size > maxChars) {
      text = text.copyRange(0, maxChars - 1) ++ "…";
    };

    ^text;
  }

  *prUpdateGui {
    var bounds;

    if (window.isNil or: { window.visible != true }) {
      ^this.prPrint("🔴 Window is closed");
    };

    bounds = window.bounds;
    window.view.removeAll;
    this.prGenerateLayout;

    bounds.width = this.prGenerateWindowWidth;
    window.bounds = bounds;
  }
}