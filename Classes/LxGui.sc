+ Lx {
  *gui {
    var channelViews, mainView, bottomRow;
    var colWidth = 100;
    var margin = 10, gap = 5;
    var width = (colWidth * channelCount) + (gap * (channelCount - 1)) + (margin * 2);
    var colHeight = 450;
    var height = colHeight + 55;
    var bgColor = Color.new255(26, 29, 34);
    var linkColor = Color.new255(31, 41, 55);

    if (bufs.isEmpty)
    { ^this.prPrint("🔴 No samples loaded. Call Lx.loadSamples first") };

    if (window.notNil and: { window.isClosed.not })
    { window.close };

    window = Window(
      name: "🔁 Lx Loops",
      bounds: Rect(
        left: Window.screenBounds.width - width,
        top: Window.screenBounds.height - height,
        width: width,
        height: height
      )
    )
    .alwaysOnTop_(true)
    .background_(bgColor)
    .front;

    mainView = CompositeView(window, window.view.bounds);
    mainView.decorator = FlowLayout(mainView.bounds, margin@margin, gap@gap);

    channelCount.do { |i|
      var col, sampleRow;
      var id = this.prCreateId(i);
      var existing = last[id];

      col = CompositeView(mainView, colWidth@colHeight);
      col.decorator = FlowLayout(col.bounds, 5@5, 2@2);

      StaticText(col, (colWidth - 10)@20)
      .align_(\center)
      .string_(channelNames[i])
      .stringColor_(Color.white)
      .font_(Font.default.boldVariant);

      sampleRow = CompositeView(col, (colWidth - 10)@25);
      sampleRow.decorator = FlowLayout(sampleRow.bounds, 0@0, 2@0);

      Button(sampleRow, 20@20)
      .states_([["<", Color.white, linkColor]])
      .action_({ Lx.prev(i) });

      StaticText(sampleRow, 40@20)
      .align_(\center)
      .string_(tracks[i].asString)
      .stringColor_(Color.white)
      .background_(linkColor);

      Button(sampleRow, 20@20)
      .states_([[">", Color.white, linkColor]])
      .action_({ Lx.next(i) });

      this.prCreateGuiKnob(col, "Amp",
        existing !? { existing[\amp] } ?? 0.3,
        { |v| Lx.amp(i, v.value) });

      this.prCreateGuiKnob(col, "Rate",
        existing !? { existing[\rate] } ?? 1 / 2,
        { |v| Lx.rate(i, v.value * 2) });

      this.prCreateGuiKnob(col, "Start",
        existing !? { existing[\start] } ?? 0,
        { |v| Lx.start(i, v.value) });

      this.prCreateGuiKnob(col, "Dur",
        existing !? { existing[\dur] } ?? 4 / 16,
        { |v| Lx.dur(i, (v.value * 16).max(0.25)) });

      Button(col, (colWidth - 10)@25)
      .states_([
        ["Mute", Color.white, linkColor],
        ["Unmute", Color.white, Color.red(0.7)],
      ])
      .action_({ |butt|
        var id = ("lx" ++ i).asSymbol;

        if (butt.value == 1)
        { Px.pause(id) }
        { Px.resume(id) };
      });
    };

    bottomRow = CompositeView(mainView, (width - 30)@35);
    bottomRow.decorator = FlowLayout(bottomRow.bounds, 0@5, 10@0);

    Button(bottomRow, 100@30)
    .states_([["Play All", Color.white, Color.new255(0, 193, 137)]])
    .action_({ Lx.play });

    Button(bottomRow, 100@30)
    .states_([["Stop All", Color.white, Color.red(0.8)]])
    .action_({ Lx.stop });

    window.front;
  }

  *prCreateGuiKnob { |parent, label, value, action|
    var knob, knobColor, knobRow;
    var colWidth = 100;
    var knobSize = colWidth - 30;

    StaticText(parent, (colWidth - 10)@15)
    .align_(\center)
    .string_(label)
    .stringColor_(Color.grey(0.7))
    .font_(Font.default.size_(10));

    knobRow = CompositeView(parent, (colWidth - 10)@knobSize);
    knobRow.decorator = FlowLayout(knobRow.bounds, ((colWidth - 10 - knobSize) / 2)@0, 0@0);

    knob = Knob(knobRow, knobSize@knobSize)
    .mode_(\vert)
    .value_(value)
    .mouseUpAction_(action);
    knobColor = knob.color;
    knobColor[1] = Color.cyan;
    knob.color = knobColor;
  }
}
