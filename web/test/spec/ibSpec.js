describe('IB-tutkinto', function( ) {
  var page = KoskiPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()
  before(Authentication().login(), resetFixtures)

  describe('Pre-IB', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('040701-432D'))
    describe('Oppijan suorituksissa', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto()).to.equal("Pre-IB luokan oppimäärä")
        expect(opinnot.getOppilaitos()).to.equal("Ressun lukio")
      })
    })

    describe('Kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)
      it('toimii', function () {
        expect(S('.preibkurssinsuoritus:eq(0) .koulutusmoduuli .tunniste .value').text()).to.equal('Tekstit ja vuorovaikutus')
      })
    })
  })

  describe('IB-tutkintotodistus', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('040701-432D'), opinnot.valitseSuoritus('IB-tutkinto'))
    describe('Oppijan suorituksissa', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto(0)).to.equal("IB-tutkinto (International Baccalaureate)")
        expect(opinnot.getOppilaitos()).to.equal("Ressun lukio")
      })
    })

    describe('Kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)
      it('toimii', function () {
        expect(S('.ibkurssinsuoritus:eq(0) .koulutusmoduuli .kuvaus .value').text()).to.equal('TOK1')
      })
    })

    describe('Tulostettava todistus', function () {
      before(opinnot.avaaTodistus())
      it('näytetään', function () {
        // See more detailed content specification in IBTutkintoSpec.scala
        expect(todistus.vahvistus()).to.equal('Helsinki 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })


  describe('Opintosuoritusote', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('040701-432D'))
    before(opinnot.avaaOpintosuoritusote(1))
    describe('Kun klikataan linkkiä', function () {
      it('näytetään', function () {
      })
    })
  })
})
