describe('TELMA', function() {
  var page = KoskiPage()

  describe('Työhön ja itsenäiseen elämään valmentava koulutus', function() {
    before(page.openPage, page.oppijaHaku.search('170696-986C', page.isOppijaSelected('Tuula')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Työhön ja itsenäiseen elämään valmentava koulutus")
        expect(OpinnotPage().getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
      })
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in TelmaSpec.scala
        expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })
})