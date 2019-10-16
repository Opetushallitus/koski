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

    it('peruste näytetään ilman linkkiä', function() {
      expect(isElementVisible(S('.diaarinumero a'))).to.equal(false)
      expect(S('.diaarinumero').text()).to.equal('57/011/2015')
    })
  })

  describe('Tutkinnon nimi', function() {
    describe('haetaan e-perusteista', function () {
      before(page.openPage, page.oppijaHaku.searchAndSelect('160525-780Y'), opinnot.valitseSuoritus(undefined, 'Liiketalouden perustutkinto'))
      var koulutus = opinnot.opiskeluoikeusEditor().property('koulutusmoduuli')
      it('toimii', function() {
        expect(koulutus.getText()).to.equal('Koulutus Liiketalouden perustutkinto 331101 59/011/2014')
      })
    })
  })
})
