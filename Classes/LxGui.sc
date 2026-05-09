+ Lx {
  *gui {
    var channelViews, mainView, bottomRow;
    var colWidth = 100;
    var margin = 10, gap = 5;
    var width = (colWidth * channelCount) + (gap * (channelCount - 1)) + (margin * 2);
    var colHeight = 694;
    var height = colHeight + 75;
    var bgColor = Color.new255(26, 29, 34);
    var linkColor = Color.new255(31, 41, 55);
    var durSteps = [-16, -8, -4, -2, -1, -0.5, -0.25, -0.125, 0.125, 0.25, 0.5, 1, 2, 4, 8, 16];

    if (bufs.isEmpty)
    { ^this.prPrint("🔴 No samples loaded. Call Lx.loadSamples first") };

    if (meterRoutine.notNil)
    { meterRoutine.stop; meterRoutine = nil };

    meterViews = Array.new;

    if (window.notNil and: { window.isClosed.not }) {
      window.view.removeAll;
    } {
      window = Window(
        name: "🪓 Repeat Or Die",
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
    };

    mainView = CompositeView(window, window.view.bounds);
    mainView.decorator = FlowLayout(mainView.bounds, margin@margin, gap@gap);

    channelCount.do { |i|
      var col, sampleRow;
      var id = this.prCreateId(i);
      var existing = last[id];

      col = CompositeView(mainView, colWidth@colHeight);
      col.decorator = FlowLayout(col.bounds, 5@5, 2@2);

      UserView(col, (colWidth - 10)@26)
      .drawFunc_({
        var bounds = Rect(0, 0, colWidth - 10, 26);

        Pen.fillColor = Color.white;
        Pen.addRoundedRect(bounds, 4, 4);
        Pen.fill;
        Pen.color = bgColor;
        Pen.font = Font.default.boldVariant;
        Pen.stringCenteredIn(channelNames[i], bounds);
      });

      CompositeView(col, (colWidth - 10)@3);

      {
        var meterView;
        var meterColor = Color.new255(37, 190, 106);

        meterView = UserView(col, (colWidth - 10)@26)
        .drawFunc_({ |v|
          var bounds = v.bounds.moveTo(0, 0);
          var level = if (i < meterLevels.size) { meterLevels[i] } { 0 };
          var normalized = level.ampdb.linlin(-40, 0, 0, 1).clip(0, 1);
          var fillWidth = normalized * bounds.width;

          Pen.fillColor = Color.new255(31, 41, 55);
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
      }.value;

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

      this.prCreateGuiSlider(col, "Dur",
        this.prDurToSlider(existing !? { existing[\dur] } ?? 4, durSteps),
        existing !? { existing[\dur] } ?? 4,
        { |val|
          var index = (val * (durSteps.size - 1)).round.asInteger;
          Lx.dur(i, durSteps[index]);
        },
        { |val|
          var index = (val * (durSteps.size - 1)).round.asInteger;
          durSteps[index];
        });

      this.prCreateGuiSlider(col, "Start",
        existing !? { existing[\start] } ?? 0,
        (existing !? { existing[\start] } ?? 0).round(0.01),
        { |val| Lx.start(i, val) },
        { |val| val.round(0.01) });

      this.prCreateGuiSlider(col, "GrainDur",
        existing !? { existing[\grainDur] } ?? 0.1,
        (existing !? { existing[\grainDur] } ?? 0.1).round(0.005),
        { |val| Lx.grainDur(i, val.max(0.005).round(0.005)) },
        { |val| val.max(0.005).round(0.005) });

      {
        var densitySteps = [0.5, 1, 2, 3, 4, 5, 6, 7, 8, 12, 16, 24, 32];
        var currentHz = existing !? { existing[\density] } ?? 10;
        var currentBeats = currentHz / TempoClock.default.tempo;
        var closestIndex = densitySteps.collect { |s| (s - currentBeats).abs }.minIndex;

        this.prCreateGuiSlider(col, "Density",
          closestIndex / (densitySteps.size - 1),
          densitySteps[closestIndex],
          { |val|
            var index = (val * (densitySteps.size - 1)).round.asInteger;
            Lx.density(i, densitySteps[index] * TempoClock.default.tempo);
          },
          { |val|
            var index = (val * (densitySteps.size - 1)).round.asInteger;
            densitySteps[index];
          });
      }.value;

      this.prCreateGuiSlider(col, "Freeze",
        existing !? { existing[\freeze] } ?? 0,
        (existing !? { existing[\freeze] } ?? 0).round(0.01),
        { |val| Lx.freeze(i, val.round(0.01)) },
        { |val| val.round(0.01) });

      this.prCreateGuiSlider(col, "Scatter",
        existing !? { existing[\scatter] } ?? 0,
        (existing !? { existing[\scatter] } ?? 0).round(0.01),
        { |val| Lx.scatter(i, val.round(0.01)) },
        { |val| val.round(0.01) });

      this.prCreateGuiSlider(col, "Spread",
        existing !? { existing[\spread] } ?? 0,
        (existing !? { existing[\spread] } ?? 0).round(0.01),
        { |val| Lx.spread(i, val.round(0.01)) },
        { |val| val.round(0.01) });

      {
        var lengthSteps = [0.25, 0.5, 1, 2, 4, 8, 16];
        var currentLength = existing !? { existing[\length] };
        var closestIndex = if (currentLength.notNil) {
          lengthSteps.collect { |s| (s - currentLength).abs }.minIndex;
        } { 0 };

        this.prCreateGuiSlider(col, "Length",
          closestIndex / (lengthSteps.size - 1),
          if (currentLength.notNil) { lengthSteps[closestIndex] } { "off" },
          { |val|
            var index = (val * (lengthSteps.size - 1)).round.asInteger;
            Lx.length(i, lengthSteps[index]);
          },
          { |val|
            var index = (val * (lengthSteps.size - 1)).round.asInteger;
            lengthSteps[index];
          });
      }.value;

      CompositeView(col, (colWidth - 10)@5);

      {
        var randomLabel;

        randomLabel = StaticText(col, (colWidth - 10)@21)
        .align_(\left)
        .string_("Random: " ++ shuffleAmounts[i].round(0.01))
        .stringColor_(Color.grey(0.7))
        .font_(Font.default.size_(12));

        Slider(col, (colWidth - 10)@26)
        .value_(shuffleAmounts[i].linlin(0.05, 1, 0, 1))
        .background_(bgColor)
        .action_({ |v|
          var val = v.value.linlin(0, 1, 0.05, 1);
          shuffleAmounts.put(i, val);
          randomLabel.string_("Random: " ++ val.round(0.01));
        });
      }.value;

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
        .states_([["R", Color.white, Color.new255(32, 42, 55)]])
        .action_({ Lx.shuffle(i) });
      }.value;

      CompositeView(col, (colWidth - 10)@5);

      {
        var ampLabel, currentAmp;
        var contentWidth = colWidth - 10;

        currentAmp = existing !? { existing[\amp] } ?? 0.3;

        ampLabel = StaticText(col, contentWidth@21)
        .align_(\center)
        .string_("Amp: " ++ currentAmp.round(0.01))
        .stringColor_(Color.grey(0.7))
        .font_(Font.default.size_(12));

        {
          var knob, knobColor;

          knob = Knob(col, contentWidth@60)
          .mode_(\vert)
          .value_(currentAmp)
          .action_({ |k|
            ampLabel.string_("Amp: " ++ k.value.round(0.01));
          })
          .mouseUpAction_({ |v|
            Lx.amp(i, v.value);
          });

          knobColor = knob.color;
          knobColor[1] = Color.cyan;
          knob.color = knobColor;
        }.value;
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

    window.onClose_({
      if (meterRoutine.notNil)
      { meterRoutine.stop; meterRoutine = nil };

      meterViews = Array.new;
    });

    meterRoutine = Routine({
      inf.do {
        meterLevels.size.do { |i| meterLevels[i] = meterLevels[i] * 0.7 };

        meterViews.do { |v|

          if (v.isClosed.not)
          { v.refresh };
        };
        0.05.wait;
      };
    }).play(AppClock);

    window.front;
  }

  *prCreateGuiSlider { |parent, label, sliderValue, displayValue, action, displayFunc|
    var labelView, slider, currentValue;
    var colWidth = 100;
    var contentWidth = colWidth - 10;
    var fillColor = Color.cyan;
    var bgSliderColor = Color.new255(31, 41, 55);

    currentValue = sliderValue;

    labelView = StaticText(parent, contentWidth@21)
    .align_(\left)
    .string_(label ++ ": " ++ displayValue.asString)
    .stringColor_(Color.grey(0.7))
    .font_(Font.default.size_(12));

    slider = UserView(parent, contentWidth@26)
    .drawFunc_({ |v|
      var bounds = v.bounds.moveTo(0, 0);
      var fillWidth = currentValue * bounds.width;

      Pen.fillColor = bgSliderColor;
      Pen.addRoundedRect(bounds, 4, 4);
      Pen.fill;

      if (fillWidth > 0) {
        Pen.push;
        Pen.addRoundedRect(bounds, 4, 4);
        Pen.clip;
        Pen.fillColor = fillColor;
        Pen.fillRect(Rect(0, 0, fillWidth, bounds.height));
        Pen.pop;
      };

      Pen.strokeColor = Color.grey(0.45);
      Pen.addRoundedRect(bounds, 4, 4);
      Pen.stroke;
    })
    .mouseDownAction_({ |v, x|
      currentValue = (x / v.bounds.width).clip(0, 1);
      labelView.string_(label ++ ": " ++ displayFunc.(currentValue).asString);
      v.refresh;
    })
    .mouseMoveAction_({ |v, x|
      currentValue = (x / v.bounds.width).clip(0, 1);
      labelView.string_(label ++ ": " ++ displayFunc.(currentValue).asString);
      v.refresh;
    })
    .mouseUpAction_({ |v|
      action.(currentValue);
    });
  }

  *prDurToSlider { |dur, steps|
    var closest = steps.collect { |s| (s - dur).abs }.minIndex;
    ^closest / (steps.size - 1);
  }

}
