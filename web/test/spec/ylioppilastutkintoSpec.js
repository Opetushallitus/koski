describe('Ylioppilastutkinto', function( ){
  var page = KoskiPage()
  var todistus = TodistusPage()
  before(resetFixtures, Authentication().login())
  before(page.openPage, page.oppijaHaku.search('010696-971K', page.isOppijaSelected('Ynjevi')))
  describe('Oppijan suorituksissa', function() {
    it('näytetään', function() {
      expect(OpinnotPage().getTutkinto()).to.equal("Ylioppilastutkinto")
      expect(OpinnotPage().getOppilaitos()).to.equal("Helsingin medialukio")
    })
  })

  describe('Tulostettava todistus', function() {
    before(OpinnotPage().avaaTodistus)
    it('näytetään', function() {
      // See more detailed content specification in YlioppilastutkintoSpec.scala
    })
  })
})