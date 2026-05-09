+ Lx {
  *gui {
    var channelViews, mainView, bottomRow;
    var colWidth = 100;
    var margin = 10, gap = 5;
    var width = (colWidth * channelCount) + (gap * (channelCount - 1)) + (margin * 2);
    var hasGrain = modes.any { |m| m == \grain };
    var colHeight = if (hasGrain) { 985 } { 510 };
    var height = colHeight + 75;
    var bgColor = Color.new255(26, 29, 34);
    var linkColor = Color.new255(31, 41, 55);
    var durSteps = [-16, -8, -4, -2, -1, -0.5, -0.25, -0.125, 0.125, 0.25, 0.5, 1, 2, 4, 8, 16];

    if (bufs.isEmpty)
    { ^this.prPrint("🔴 No samples loaded. Call Lx.loadSamples first") };

    if (window.notNil and: { window.isClosed.not })
    { window.close };

    window = Window(
      name: "🔄 Repeat Or Die",
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
      var maxBeats = bufs[i][tracks[i]].duration * TempoClock.default.tempo;

      col = CompositeView(mainView, colWidth@colHeight);
      col.decorator = FlowLayout(col.bounds, 5@5, 2@2);

      if (colors[id].isNil)
      { colors[id] = Color.rand };

      UserView(col, (colWidth - 10)@20)
      .drawFunc_({
        var bounds = Rect(0, 0, colWidth - 10, 20);

        Pen.fillColor = colors[id];
        Pen.addRoundedRect(bounds, 4, 4);
        Pen.fill;
        Pen.color = Color.white;
        Pen.font = Font.default.boldVariant;
        Pen.stringCenteredIn(channelNames[i], bounds);
      });

      CompositeView(col, (colWidth - 10)@3);

      {
        var contentWidth = colWidth - 10;
        var btnWidth = 20;
        var textWidth = contentWidth - (btnWidth * 2) - 4;

        sampleRow = CompositeView(col, contentWidth@25);
        sampleRow.decorator = FlowLayout(sampleRow.bounds, 0@0, 2@0);

        Button(sampleRow, btnWidth@20)
        .states_([["<", Color.white, linkColor]])
        .action_({ Lx.prev(i) });

        StaticText(sampleRow, textWidth@20)
        .align_(\center)
        .string_(tracks[i].asString ++ "/" ++ bufs[i].size)
        .stringColor_(Color.white)
        .background_(linkColor);

        Button(sampleRow, btnWidth@20)
        .states_([[">", Color.white, linkColor]])
        .action_({ Lx.next(i) });
      }.value;

      this.prCreateGuiKnob(col, "Amp",
        existing !? { existing[\amp] } ?? 0.3,
        existing !? { existing[\amp] } ?? 0.3,
        { |v| Lx.amp(i, v.value) },
        { |v| v.value });

      this.prCreateGuiKnob(col, "Dur",
        this.prDurToKnob(existing !? { existing[\dur] } ?? 4, durSteps),
        existing !? { existing[\dur] } ?? 4,
        { |v|
          var index = (v.value * (durSteps.size - 1)).round.asInteger;
          var durValue = durSteps[index];
          Lx.dur(i, durValue);
        },
        { |v|
          var index = (v.value * (durSteps.size - 1)).round.asInteger;
          durSteps[index];
        });

      this.prCreateGuiKnob(col, "Start",
        existing !? { existing[\start] } ?? 0,
        existing !? { existing[\start] } ?? 0,
        { |v| Lx.start(i, v.value) },
        { |v| v.value.round(0.01) });

      if (modes[i] == \grain) {
        this.prCreateGuiKnob(col, "Freeze",
          existing !? { existing[\freeze] } ?? 0,
          existing !? { existing[\freeze] } ?? 0,
          { |v| Lx.freeze(i, v.value.round(0.01)) },
          { |v| v.value.round(0.01) });
      } {
        this.prCreateGuiKnob(col, "Trim",
          this.prTrimToKnob(existing !? { existing[\trim] } ?? maxBeats, maxBeats),
          existing !? { existing[\trim] } ?? maxBeats,
          { |v|
            var trimValue = ((v.value * (maxBeats - 0.125)) + 0.125).round(0.125);
            Lx.trim(i, trimValue);
          },
          { |v|
            ((v.value * (maxBeats - 0.125)) + 0.125).round(0.125);
          });
      };

      if (modes[i] == \grain) {
        {
          var densitySteps = [0.5, 1, 2, 3, 4, 5, 6, 7, 8, 12, 16, 24, 32];
          var currentHz = existing !? { existing[\density] } ?? 10;
          var currentBeats = currentHz / TempoClock.default.tempo;
          var closestIndex = densitySteps.collect { |s| (s - currentBeats).abs }.minIndex;

          this.prCreateGuiKnob(col, "Density",
            closestIndex / (densitySteps.size - 1),
            densitySteps[closestIndex],
            { |v|
              var index = (v.value * (densitySteps.size - 1)).round.asInteger;
              var hz = densitySteps[index] * TempoClock.default.tempo;
              Lx.density(i, hz);
            },
            { |v|
              var index = (v.value * (densitySteps.size - 1)).round.asInteger;
              densitySteps[index];
            });
        }.value;

        this.prCreateGuiKnob(col, "GrainDur",
          existing !? { existing[\grainDur] } ?? 0.1,
          existing !? { existing[\grainDur] } ?? 0.1,
          { |v| Lx.grainDur(i, v.value.max(0.005).round(0.005)) },
          { |v| v.value.max(0.005).round(0.005) });

        this.prCreateGuiKnob(col, "Scatter",
          existing !? { existing[\scatter] } ?? 0,
          existing !? { existing[\scatter] } ?? 0,
          { |v| Lx.scatter(i, v.value.round(0.01)) },
          { |v| v.value.round(0.01) });

        this.prCreateGuiKnob(col, "Spread",
          existing !? { existing[\spread] } ?? 0,
          existing !? { existing[\spread] } ?? 0,
          { |v| Lx.spread(i, v.value.round(0.01)) },
          { |v| v.value.round(0.01) });

        {
          var lengthSteps = [0.25, 0.5, 1, 2, 4, 8, 16];
          var currentLength = existing !? { existing[\length] };
          var closestIndex = if (currentLength.notNil) {
            lengthSteps.collect { |s| (s - currentLength).abs }.minIndex;
          } { 0 };

          this.prCreateGuiKnob(col, "Length",
            closestIndex / (lengthSteps.size - 1),
            if (currentLength.notNil) { lengthSteps[closestIndex] } { "off" },
            { |v|
              var index = (v.value * (lengthSteps.size - 1)).round.asInteger;
              Lx.length(i, lengthSteps[index]);
            },
            { |v|
              var index = (v.value * (lengthSteps.size - 1)).round.asInteger;
              lengthSteps[index];
            });
        }.value;
      };

      {
        var buttonRow, btnWidth;

        buttonRow = CompositeView(col, (colWidth - 10)@30);
        buttonRow.decorator = FlowLayout(buttonRow.bounds, 0@5, 2@0);
        btnWidth = ((colWidth - 10 - 4) / 3).floor;

        Button(buttonRow, btnWidth@25)
        .states_([
          ["🟢", Color.white, Color.new255(32, 42, 55)],
          ["⬜️", Color.grey, Color.new255(32, 42, 55)],
        ])
        .action_({ |btn|
          if (btn.value == 1)
          { mutedChannels.add(i) }
          { mutedChannels.remove(i) };

          Lx.prApplyMuteState(i);
        })
        .value_(
          if (mutedChannels.includes(i)) { 1 } { 0 }
        );

        Button(buttonRow, btnWidth@25)
        .states_([
          ["S", Color.white, Color.new255(32, 42, 55)],
          ["S", Color.new255(32, 42, 55), Color.white],
        ])
        .action_({ |btn|
          if (btn.value == 1)
          { soloedChannels.add(i) }
          { soloedChannels.remove(i) };

          channelCount.do { |j|
            Lx.prApplyMuteState(j);
          };
        })
        .value_(
          if (soloedChannels.includes(i)) { 1 } { 0 }
        );

        Button(buttonRow, btnWidth@25)
        .states_([
          ["G", Color.white, Color.new255(32, 42, 55)],
          ["G", Color.new255(32, 42, 55), Color.new255(61, 219, 217)],
        ])
        .action_({ Lx.grain(i) })
        .value_(
          if (modes[i] == \grain) { 1 } { 0 }
        );
      }.value;
    };

    {
      var rowWidth = width - (margin * 2);
      var buttonGap = 5;
      var buttonWidth = ((rowWidth - (buttonGap * 2)) / 3).floor;

      bottomRow = CompositeView(mainView, rowWidth@50);
      bottomRow.decorator = FlowLayout(bottomRow.bounds, 0@0, buttonGap@0);

      {
        var playBtn;

        playBtn = Button(bottomRow, buttonWidth@50)
        .states_([
          ["Play", Color.white, linkColor],
          ["Play", bgColor, Color.new255(37, 190, 106)],
        ])
        .action_({
          if (isPlaying.not) { Lx.play };
          playBtn.value_(if (isPlaying) { 1 } { 0 });
        })
        .value_(
          if (isPlaying) { 1 } { 0 }
        );

        Button(bottomRow, buttonWidth@50)
        .states_([
          ["Stop", Color.white, linkColor],
          ["Stop", Color.white, Color.new255(238, 83, 150)],
        ])
        .mouseDownAction_({ |btn| btn.value_(1) })
        .action_({
          Lx.stop;
          playBtn.value_(0);
        });
      }.value;

      Button(bottomRow, buttonWidth@50)
      .states_([
        ["Shuffle", Color.white, linkColor],
        ["Shuffle", bgColor, Color.new255(61, 219, 217)],
      ])
      .mouseDownAction_({ |btn| btn.value_(1) })
      .action_({ Lx.shuffle });
    }.value;

    window.front;
  }

  *prCreateGuiKnob { |parent, label, knobValue, displayValue, action, displayFunc|
    var knob, knobColor, knobRow, valueBox;
    var colWidth = 100;
    var knobSize = colWidth - 40;

    StaticText(parent, (colWidth - 10)@15)
    .align_(\center)
    .string_(label)
    .stringColor_(Color.grey(0.7))
    .font_(Font.default.size_(10));

    knobRow = CompositeView(parent, (colWidth - 10)@knobSize);
    knobRow.decorator = FlowLayout(knobRow.bounds, ((colWidth - 10 - knobSize) / 2)@0, 0@0);

    knob = Knob(knobRow, knobSize@knobSize)
    .mode_(\vert)
    .value_(knobValue)
    .action_({ |v|
      valueBox.string_(displayFunc.(v).asString);
    })
    .mouseUpAction_(action);
    knobColor = knob.color;
    knobColor[1] = Color.cyan;
    knob.color = knobColor;

    valueBox = StaticText(parent, (colWidth - 10)@16)
    .align_(\center)
    .string_(displayValue.asString)
    .stringColor_(Color.grey(0.6))
    .font_(Font.default.size_(9));
  }

  *prDurToKnob { |dur, steps|
    var closest = steps.collect { |s| (s - dur).abs }.minIndex;
    ^closest / (steps.size - 1);
  }

  *prTrimToKnob { |trim, maxBeats|
    ^((trim - 0.125) / (maxBeats - 0.125)).clip(0, 1);
  }
}
