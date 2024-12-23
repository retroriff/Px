TestPxArray : UnitTest {
    var expectedResult, result;

    test_shuffle {
        expectedResult = [1, 2, 3].shuffle;

        this.assert(
            expectedResult != [1, 2, 3],
            "👀 Shuffle scrambles the array",
        );
    }
}
