(
var gridGap = 5, itemWidth = 68, padding = 10;
var patterns = Px.last;
var patternsFormatted = Px.lastFormatted;

var windowWidth = {
  if (patterns.size > 0)
  { patterns.size * (itemWidth + gridGap) + (padding * 2) + 5 }
  { 400 }
};

var getAmp = { |amp|
  if (amp.class == Pseq) {
    amp = amp.list.reject { |x| x.isKindOf(Rest) };
    amp.maxItem;
  } {
    amp;
  }
};

var truncateText = { |text|
  var maxChars = 8;

  if (text.size > maxChars) {
    text = text.copyRange(0, maxChars - 1) ++ "…";
  };

  text;
};

var window = Window(
  "🪩 Px",
  Rect(0, Window.screenBounds.height, windowWidth.value, 400)
)
.alwaysOnTop_(true)
.background_(
  Color.new255(
    red: 117,
    green: 163,
    blue: 209
  )
)
.front;

var firstRow = HLayout();

var sliders = patterns.keys.asSortedList collect: { |key|
  var pattern = patterns[key];
  var patternFormatted = patternsFormatted[key];
  var chan = pattern[\chan] !? { "chan" + pattern[\chan] };
  var play = pattern[\play] !? { pattern[\play][0] };
  var label = pattern[\name] ?? pattern[\instrument] ?? chan ?? play ?? key;
  var amp = getAmp.(patternFormatted[\amp]);

  var staticTextColor = {
    if (pattern[\drumMachine].notNil)
    { Color.new255(245, 245, 155) }
    { Color.rand }
  };

  var staticText = StaticText()
  .align_(\center)
  .background_(staticTextColor.value)
  .mouseDownAction_({
    (pattern[\id].asString + pattern.asString).postln
  })
  .string_(truncateText.(pattern[\id].asString + label));

  var slider = Slider()
  .mouseUpAction_({
    pattern[\id].asInteger.set(1).amp(slider.value);
  })
  .value_(amp);

  var numberBox = NumberBox()
  .value_(amp);

  var button = Button()
  .states_([
    ["play", Color.black, Color.green],
    ["stop", Color.black, Color.grey]
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
{ firstRow.add(StaticText().string_("🔴 Px is not running").align_(\center)) };
window.layout_(firstRow);
)