(
var gridGap = 5, itemWidth = 68, newSlider, padding = 10, sliders;
var patterns = Px.last;
var windowWidth = patterns.size * (itemWidth + gridGap) + (padding * 2) + 5;

var getAmp = { |amp|
  if (amp.class == Pseq) {
    amp.list.maxItem;
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
    height: 350
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

var inner = CompositeView(w, w.view.bounds.insetBy(padding, padding));
inner.decorator = FlowLayout(inner.bounds);
inner.decorator.gap = 5@5;

windowWidth.postln;
patterns.size.postln;
sliders = patterns.keys.asSortedList collect: { |key|
  var pattern = patterns[key];
  var chan = pattern[\chan] !? { "chan" + pattern[\chan] };
  var play = pattern[\play] !? { pattern[\play][0] };
  var label = pattern[\name] ?? pattern[\instrument] ?? chan ?? play ?? key;

  var slider = EZSlider(
    inner,
    itemWidth@320,
    label: truncateText.(label),
    layout: \vert,
    initVal: getAmp.(pattern[\amp]);
  )
  .labelView
  .mouseDownAction_({ label.postln })
  .align_(\center)
  .background_(Color.rand);
};

newSlider = VLayoutView(inner, Rect(10,10,100,100)).background_(Color.rand);

Button(
  newSlider,
  68@28,
)
.string_(truncateText.("marimba"))
.action_({ arg butt;
  butt.string.postln;
});
Button(
  newSlider,
  68@28,
)
.string_(truncateText.("xilofon"))
.action_({ arg butt;
  butt.string.postln;
});

w;
)

a = 1

Px.last[\1]
Px.last[10001][\amp].list.maxItem