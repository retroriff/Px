(
var gridGap = 5, itemWidth = 68, newSlider, padding = 10, sliders;
var patterns = Px.last;
var patternsFormatted = Px.lastFormatted;
var windowWidth = patterns.size * (itemWidth + gridGap) + (padding * 2) + 5;

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

var w = Window(
  name: "Px",
  bounds: Rect(
    left: 0,
    top: Window.screenBounds.height,

    width: windowWidth,
    height: 375
  )
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

var inner = CompositeView(
  parent: w,
  bounds: w.view.bounds.insetBy(padding, padding)
);
inner.decorator = FlowLayout(inner.bounds);
inner.decorator.gap = 5@5;

patterns.keys.asSortedList collect: { |key|
  var pattern = patterns[key];
  var patternFormatted = patternsFormatted[key];
  var chan = pattern[\chan] !? { "chan" + pattern[\chan] };
  var play = pattern[\play] !? { pattern[\play][0] };
  var label = pattern[\name] ?? pattern[\instrument] ?? chan ?? play ?? key;

  var track = VLayoutView(inner, Rect(0, 0, itemWidth, 360));

  var slider = EZSlider(
    track,
    itemWidth@320,
    controlSpec: ControlSpec(0.0, 1.0, \lin, 0.01),
    label: truncateText.(pattern[\id].asString + label),
    layout: \vert,
    initVal: getAmp.(patternFormatted[\amp]);
  );

  slider
  .labelView
  .align_(\center)
  .background_(Color.rand)
  .mouseDownAction_({
    (pattern[\id].asString + pattern.asString).postln
  });

  slider
  .sliderView
  .mouseUpAction_({
    pattern[\id].asInteger.set(1).amp(slider.value);
  });

  track.decorator = FlowLayout(inner.bounds);
  track.decorator.gap = 50@05;

  Button(
    track,
    Rect(0, 100, 68, 28),
  )
  .states_([
    ["play", Color.black, Color.green],
    ["stop", Color.black, Color.grey]
  ])
  .action_({ arg butt;
    butt.string.postln;
  });
};

// newSlider = VLayoutView(inner, Rect(10,10,100,100)).background_(Color.rand);
//
// Button(
//   newSlider,
//   68@28,
// )
// .string_(truncateText.("marimba"))
// .action_({ arg butt;
//   butt.string.postln;
// });
// Button(
//   newSlider,
//   68@28,
// )
// .string_(truncateText.("xilofon"))
// .action_({ arg butt;
//   butt.string.postln;
// });

w;
)

a = 1

Px.last[\1]
Px.last[10001][\amp].list.maxItem