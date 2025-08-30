/*
TODO: Update Pseq amp with Slider
TODO: Refresh Window
*/

(
var gridGap = 5, itemWidth = 68, padding = 10, windowHeight = 350;
var patterns = Px.last;
var patternsFormatted = Px.lastFormatted;

var windowWidth = {
  if (patterns.size > 0)
  { patterns.size * (itemWidth + gridGap) + (padding * 2) + 5 }
  { windowHeight }
};

var getAmp = { |amp|
  case
  { amp.isKindOf(Pwhite) }
  {  amp.hi }

  { amp.isKindOf(Pattern) }
  {
    amp = amp.list.reject { |x| x.isKindOf(Rest) };
    amp.maxItem;
  }

  { amp }
};

var truncateText = { |text|
  var maxChars = 8;

  if (text.size > maxChars) {
    text = text.copyRange(0, maxChars - 1) ++ "…";
  };

  text;
};

var firstRow = HLayout();
var sliders, drumMachinePatterns, nonDrumMachinePatterns, sortedKeys;

~pxGui = Window(
  "🪩 The music was new, black polished chrome, and came over the summer, like liquid night.",
  Rect(0, Window.screenBounds.height, windowWidth.value, windowHeight)
)
.alwaysOnTop_(true)
.background_(
  Color.new255(
    red: 24,
    green: 24,
    blue: 24
  )
)
.front;

drumMachinePatterns = patterns.select { |pattern| pattern[\drumMachine].notNil }.keys.asSortedList;
nonDrumMachinePatterns =  patterns.select { |pattern| pattern[\drumMachine].isNil }.keys.asSortedList;
sortedKeys = drumMachinePatterns ++ nonDrumMachinePatterns;

sliders = sortedKeys collect: { |key|
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

if (sliders.size > 0)
{ sliders do: { |slider| firstRow.add(slider)} }
{ firstRow.add(
  StaticText()
  .align_(\center)
  .string_("🔴 Px is not running")
  .stringColor_(Color.white)
) };
~pxGui.layout_(firstRow);
)