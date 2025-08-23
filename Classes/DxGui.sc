+ Dx {
  *gui {
    var drumMachinesList;
    var folders = this.prGetDrumMachinesFolders;
    var firstCol, secondCol, thirdCol, mainView, row;
    var width = 420, height = 350;

    var w = Window(
      name: "🛢️ Dancing To The Drum Machine",
      bounds: Rect(
        left: Window.screenBounds.width - width,
        top: Window.screenBounds.height - height,
        width: width,
        height: height
      )
    )
    .alwaysOnTop_(true)
    .background_(Color(
      red: 0.46,
      green: 0.64,
      blue: 0.82)
    )
    .front;

    mainView = CompositeView(w, w.view.bounds);
    mainView.decorator = FlowLayout(mainView.bounds);
    mainView.decorator.gap = 0@0;

    // Row container for the columns
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

    // Set the initial value of the drum machine list
    if (Dx.drumMachine.notNil) {
      var idx = this.prGetDrumMachinesListIndex(folders);

      if (idx.notNil)
      { drumMachinesList.value = idx };
    };

    // 📶 Amp Knob
    Knob(secondCol, 80@80)
    .mode_(\vert)
    .value_(Dx.vol)
    .mouseUpAction_({ |v| Dx.vol(v.value) });

    // ✨ Delay Knob
    StaticText(secondCol, 80@20).align_(\center).string_("Delay");
    Knob(secondCol, 80@80)
    .mode_(\vert)
    .value_(Dx.fx[\delay])
    .mouseUpAction_({ |v| Dx.delay(v.value) });

    // ✨ Reverb Knob
    StaticText(secondCol, 80@20).align_(\center).string_("Reverb");
    Knob(secondCol, 80@80)
    .mode_(\vert)
    .value_(Dx.fx[\reverb])
    .mouseUpAction_({ |v| Dx.reverb(v.value) });

    // 🔀 Random button
    Button(thirdCol, 80@145)
    .states_([["Random", Color.white, Color.blue(0.27)]])
    .action_({
      var idx;
      this.shuffle;
      idx = this.prGetDrumMachinesListIndex(folders);
      drumMachinesList.value = idx;
    });

    // 🔴 Stop button
    Button(thirdCol, 80@145)
    .states_([["Stop", Color.white, Color.red(0.8)]])
    .action_({ Px.stop; });

    w.front;
  }

  *shuffle {
    var folders = this.prGetDrumMachinesFolders;
    var randomIndex = folders.size.rand;

    Dx.use(folders[randomIndex]);
  }

  *prGetDrumMachinesFolders {
    ^PathName(drumMachinesPath).folders.collect(_.folderName);
  }

  *prGetDrumMachinesListIndex { |folders|
    ^folders.detectIndex { |x| x == Dx.drumMachine.asString };
  }
}
