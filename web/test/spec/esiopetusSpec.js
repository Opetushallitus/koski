describe('Esiopetus', function() {
  var page = KoskiPage()
  var opinnot = OpinnotPage()

  before(Authentication().login(), resetFixtures)

  describe('Esiopetuksen opiskeluoikeudet', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('300996-870E'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
        expect(opinnot.getTutkinto()).to.equal('Peruskoulun esiopetus')
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.esiopetuksensuoritus:eq(0) .koulutusmoduuli .inline').text()).to.equal('Peruskoulun esiopetus')
      })
    })
  })
})