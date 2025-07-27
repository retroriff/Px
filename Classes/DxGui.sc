+ Dx {
  *gui {
    var drumMachinesList;
    var folders = PathName(drumMachinesPath).folders.collect(_.folderName);
    var firstCol, secondCol, thirdCol, mainView, row;
    var ampKnob, delayKnob, lastActionTime = 0.0, reverbKnob;

    var w = Window("🛢️ Dancing To The Drum Machine", Rect(left: 0, top: 0, width: 420, height: 350));
    w.background = Color(0.46045410633087, 0.64358153343201, 0.82325148582458);

    mainView = CompositeView(w, w.view.bounds);
    mainView.decorator = FlowLayout(mainView.bounds);
    mainView.decorator.gap = 0@0;

    // Row container for both columns
    row = CompositeView(mainView, 500@340);
    row.decorator = FlowLayout(row.bounds);
    row.decorator.gap = 10@10;

    // Three columns inside the row
    firstCol = CompositeView(row, 200@320);
    firstCol.decorator = FlowLayout(firstCol.bounds);

    secondCol = CompositeView(row, 90@320);
    secondCol.decorator = FlowLayout(secondCol.bounds);

    thirdCol = CompositeView(row, 100@320);
    thirdCol.decorator = FlowLayout(thirdCol.bounds);

    // Labels
    StaticText(firstCol, 200@20).align_(\center).string_("Drum Machines");
    StaticText(secondCol, 80@20).align_(\center).string_("Amp");
    StaticText(thirdCol, 80@20).align_(\center).string_("");

    // 🥁 Drum machine list
    drumMachinesList = EZListView(
      parentView: firstCol,
      bounds: 200@300,
      globalAction: { |ez| Dx.use(folders[ez.value]) },
      items: folders,
      initVal: folders[0],
      initAction: false
    );

    if (Dx.drumMachine.notNil) {
      var idx = folders.detectIndex { |x| x == Dx.drumMachine.asString };

      if (idx.notNil)
      { drumMachinesList.value = idx };
    } {
      folders[0]; // clears selection
    };

    // Right column

    // 📶 Amp Knob
    ampKnob = Knob(secondCol, 80@80)
    .mode_(\vert)
    .value_(Dx.vol)
    .mouseUpAction_({ |v| Dx.vol(v.value) });

    StaticText(secondCol, 80@20).align_(\center).string_("Delay");

    delayKnob = Knob(secondCol, 80@80)
    .mode_(\vert)
    .value_(Dx.fx[\delay])
    .mouseUpAction_({ |v| Dx.delay(v.value) });

    StaticText(secondCol, 80@20).align_(\center).string_("Reverb");

    reverbKnob = Knob(secondCol, 80@80)
    .mode_(\vert)
    .value_(Dx.fx[\reverb])
    .mouseUpAction_({ |v| Dx.reverb(v.value) });

    // 🔀 Random button
    Button(thirdCol, 80@145)
    .states_([["Random", Color.white, Color.blue(0.27)]])
    .action_({
      var idx;
      this.shuffle;
      idx = folders.detectIndex { |x| x == Dx.drumMachine.asString };
      drumMachinesList.value = idx;
    });

    Button(thirdCol, 80@145)
    .states_([["Stop", Color.white, Color.red(0.8)]])
    .action_({ Px.stop; });

    w.front;
  }

  *shuffle {
    var folders = PathName(drumMachinesPath).folders.collect(_.folderName);
    var randomIndex = folders.size.rand;
    
    Dx.use(folders[randomIndex]);
  }
}
