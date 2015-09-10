describe("TOR", function() {
  var page = TorPage();
  before(page.openPage)
  it("toimii", function() {
    expect(page.isVisible()).to.equal(true)
  })
})