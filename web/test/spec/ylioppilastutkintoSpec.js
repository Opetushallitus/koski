describe('Ylioppilastutkinto', function( ){
  var page = KoskiPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()

  function osasuoritusSelectorStringForIndex(i) {
    return `.ylioppilastutkinnonsuoritus .osasuoritukset .suoritus-taulukko .tutkinnon-osa:eq(${i})`
  }

  function nimiForOsasuoritusInIndex(i) {
    return S(`${osasuoritusSelectorStringForIndex(i)} .suoritus .nimi`).text()
  }

  function arvosanaForOsasuoritusInIndex(i) {
    return S(`${osasuoritusSelectorStringForIndex(i)} .arvosana`).text()
  }

  before(Authentication().login(), resetFixtures)
  before(page.openPage, page.oppijaHaku.searchAndSelect('010696-971K'))
  describe('Oppijan suorituksissa', function() {
    it('näytetään', function() {
      expect(OpinnotPage().getTutkinto()).to.equal("Ylioppilastutkinto")
      expect(OpinnotPage().getOppilaitos()).to.equal("Helsingin medialukio")
    })
  })

  describe('Osasuoritukset', function() {
    before(opinnot.expandAll)

    it('osasuorituksen nimi ja arvosana näkyvissä', function() {
      expect(nimiForOsasuoritusInIndex(0)).to.equal('Maantiede')
      expect(arvosanaForOsasuoritusInIndex(0)).to.equal('Magna cum laude approbatur')
    })

    it('kaikki osasuoritukset näkyvissä', function() {
      expect(nimiForOsasuoritusInIndex(0)).to.equal('Maantiede')
      expect(nimiForOsasuoritusInIndex(1)).to.equal('Englanti, pitkä')
      expect(nimiForOsasuoritusInIndex(2)).to.equal('Äidinkieli, suomi')
      expect(nimiForOsasuoritusInIndex(3)).to.equal('Matematiikka, lyhyt')
      expect(nimiForOsasuoritusInIndex(4)).to.equal('Ruotsi, keskipitkä')

      expect(arvosanaForOsasuoritusInIndex(0)).to.equal('Magna cum laude approbatur')
      expect(arvosanaForOsasuoritusInIndex(1)).to.equal('Cum laude approbatur')
      expect(arvosanaForOsasuoritusInIndex(2)).to.equal('Lubenter approbatur')
      expect(arvosanaForOsasuoritusInIndex(3)).to.equal('Laudatur')
      expect(arvosanaForOsasuoritusInIndex(4)).to.equal('Cum laude approbatur')
    })

    it('ei ylimääräisiä osasuorituksia näkyvissä', function() {
      expect(S(osasuoritusSelectorStringForIndex(5)).length).to.equal(0)
    })
  })

  describe('Tulostettava todistus', function() {
    before(opinnot.avaaTodistus(0))
    it('näytetään', function() {
      // See more detailed content specification in YlioppilastutkintoSpec.scala
    })
  })
})