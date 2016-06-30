describe('Dokumentaatio', function() {
  var page = DocumentationPage()
  describe('Dokumentaatio-sivu', function() {
    before(page.openPage)
    it('näytetään', function() {
      expect(page.isVisible()).to.equal(true)
    })
  })
})