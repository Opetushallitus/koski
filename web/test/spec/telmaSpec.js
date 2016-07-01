describe('Telma', function() {
  var page = KoskiPage()
  var todistus = TodistusPage()
  before(resetFixtures, Authentication().login())

  describe('Työhön ja itsenäiseen elämään valmentava koulutus', function() {
    before(page.openPage, page.oppijaHaku.search('170696-986C', page.isOppijaSelected('Tuula')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
        expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
      })
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in TelmaSpec.scala
        expect(todistus.vahvistus()).to.equal('Helsinki 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })
})