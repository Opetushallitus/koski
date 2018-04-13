describe('EPerusteet', function() {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  before(page.openPage, page.oppijaHaku.searchAndSelect('220109-784L'))

  describe('Kun peruste löytyy eperusteista', function() {
    before(opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus'), wait.until(function() { return isElementVisible(S('.diaarinumero a'))}))

    it('linkki eperusteisiin näytetään', function() { })
  })

  describe('Kun perustetta ei löydy eperusteista', function() {
    before(opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetukseenvalmistavaopetus'))

    it('linkki eperusteisiin näytetään', function() {
      expect(isElementVisible(S('.diaarinumero a'))).to.equal(false)
      expect(S('.diaarinumero').text()).to.equal('57/011/2015')
    })
  })
})
