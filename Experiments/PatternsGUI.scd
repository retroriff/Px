(
var windowHeight = 350.min(Window.screenBounds.height / 4);

var getAmp = { |amp|
  case
  { amp.isKindOf(Pwhite) }
  { amp.hi }

  { amp.isKindOf(Pattern) }
  { amp.list.reject { |x| x.isKindOf(Rest) }.maxItem }

  { amp };
};

var truncateText = { |text|
  var maxChars = 8;

  if (text.size > maxChars) {
    text = text.copyRange(0, maxChars - 1) ++ "…";
  };

  text;
};

var colors = Dictionary.new;

~emptyPatternsText = StaticText()
.align_(\center)
.string_("🔴 Px is not running")
.stringColor_(Color.white)
;

~generateWindowWidth = {
  if (Px.last.size > 0)
  { 68 }
  { windowHeight }
};

~pxGui = Window(
  "🪩 The music was new, black polished chrome, and came over the summer, like liquid night.",
  Rect(0, Window.screenBounds.height, ~generateWindowWidth.value, windowHeight)
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

~sliderStaticTextColor = Color.rand;

~generateSliders = {
  var patterns = Px.last;
  var patternsFormatted = Px.lastFormatted;

  var drumMachinePatterns = patterns.select { |pattern|
    pattern[\drumMachine].notNil
  }.keys.asSortedList;
  var nonDrumMachinePatterns =  patterns.select { |pattern|
    pattern[\drumMachine].isNil
  }.keys.asSortedList;
  var sortedKeys = drumMachinePatterns ++ nonDrumMachinePatterns;

  sortedKeys collect: { |key|
    var pattern = patterns[key];
    var patternFormatted = patternsFormatted[key];
    var chan = pattern[\chan] !? { "chan" + pattern[\chan] };
    var play = pattern[\play] !? { pattern[\play][0] };
    var loop = pattern[\loop] !? { pattern[\loop][0] };
    var patternLabel = pattern[\name] ?? pattern[\instrument] ?? chan ?? play ?? loop ?? key;
    var amp = getAmp.(patternFormatted[\amp]);
    var backgroundColor = Color.new255(26, 29, 34);
    var button, numberBox, slider, staticText;

    var label = {
      if (pattern[\drumMachine].notNil)
      { "🛢️" + patternLabel }
      { truncateText.(pattern[\id].asString + patternLabel) };
    };

    var staticTextColor = {
      if (pattern[\drumMachine].notNil)
      { Color.new255(255, 255, 122) }
      { ~sliderStaticTextColor }
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
    .action_({
      pattern[\id].asInteger.set(1).amp(slider.value);
      numberBox.value_(slider.value);
    })
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
      { Px.new(pattern) }
      { Px.stop(pattern[\id]) };
    });

    VLayout(staticText, slider, numberBox, button);
  };
};

~generateLayout = {
  var layout = HLayout();
  var sliders = ~generateSliders.();

  if (sliders.size > 0)
  { sliders do: { |slider| layout.add(slider); } }
  { layout.add(~emptyPatternsText) };

  ~pxGui.layout_(layout);
};

~updateLayout = {
  if (~pxGui.visible == true) {
    var bounds = ~pxGui.bounds;
    ~pxGui.view.removeAll;
    ~generateLayout.();

    bounds.width = ~generateWindowWidth.();
    ~pxGui.bounds = bounds;
  }
};

~generateLayout.();
)