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

  before(Authentication().login('pää'), resetFixtures)

  describe('Kun tutkinto on valmis', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('010696-971K'))
    describe('Tutkinnon tiedot', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Ylioppilastutkinto")
        expect(OpinnotPage().getOppilaitos()).to.equal("Helsingin medialukio")
      })

      it('tila ja vahvistus', function() {
        expect(OpinnotPage().tilaJaVahvistus.tila()).to.equal('Suoritus valmis')
      })
    })

    describe('Osasuoritukset', function() {
      before(opinnot.expandAll)

      it('kaikki osasuoritukset näkyvissä', function() {
        expect(nimiForOsasuoritusInIndex(0)).to.equal('Äidinkielen koe, suomi')
        expect(nimiForOsasuoritusInIndex(1)).to.equal('Ruotsi, keskipitkä oppimäärä')
        expect(nimiForOsasuoritusInIndex(2)).to.equal('Englanti, pitkä oppimäärä')
        expect(nimiForOsasuoritusInIndex(3)).to.equal('Maantiede')
        expect(nimiForOsasuoritusInIndex(4)).to.equal('Matematiikan koe, lyhyt oppimäärä')

        expect(arvosanaForOsasuoritusInIndex(0)).to.equal('Lubenter approbatur')
        expect(arvosanaForOsasuoritusInIndex(1)).to.equal('Cum laude approbatur')
        expect(arvosanaForOsasuoritusInIndex(2)).to.equal('Cum laude approbatur')
        expect(arvosanaForOsasuoritusInIndex(3)).to.equal('Magna cum laude approbatur')
        expect(arvosanaForOsasuoritusInIndex(4)).to.equal('Laudatur')
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

  describe('Kun tutkinto on kesken', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('200695-889X'))

    describe('Tutkinnon tiedot', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Ylioppilastutkinto")
        expect(OpinnotPage().getOppilaitos()).to.equal("")
      })

      it('tila ja vahvistus', function() {
        expect(OpinnotPage().tilaJaVahvistus.tila()).to.equal('Suoritus kesken')
      })
    })
  })
})