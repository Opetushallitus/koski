describe('Ylioppilastutkinto', function( ){
  var page = KoskiPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()

  before(Authentication().login(), resetFixtures)
  before(page.openPage, page.oppijaHaku.search('010696-971K', page.isOppijaSelected('Ynjevi')))
  describe('Oppijan suorituksissa', function() {
    it('näytetään', function() {
      expect(OpinnotPage().getTutkinto()).to.equal("Ylioppilastutkinto")
      expect(OpinnotPage().getOppilaitos()).to.equal("Helsingin medialukio")
    })
  })

  describe('Kaikki tiedot näkyvissä', function() {
    before(opinnot.expandAll)
    it('toimii', function() {
      expect(S('.ylioppilastutkinnonsuoritus .osasuoritukset .koulutusmoduuli:eq(0) .value').text()).to.equal('Maantiede')
    })
  })

  describe('Tulostettava todistus', function() {
    before(opinnot.avaaTodistus(0))
    it('näytetään', function() {
      // See more detailed content specification in YlioppilastutkintoSpec.scala
    })
  })
})