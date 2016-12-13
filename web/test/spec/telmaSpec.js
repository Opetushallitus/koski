describe('Telma', function() {
  var page = KoskiPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()
  before(Authentication().login(), resetFixtures)

  describe('Työhön ja itsenäiseen elämään valmentava koulutus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('021080-725C'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
        expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.tyohonjaitsenaiseenelamaanvalmentavankoulutuksensuoritus .osasuoritukset .koulutusmoduuli:eq(0) .tunniste .nimi .value').text()).to.equal('Toimintakyvyn vahvistaminen')
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in TelmaSpec.scala
        expect(todistus.vahvistus()).to.equal('Helsinki 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })
})