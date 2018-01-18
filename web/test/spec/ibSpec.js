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
      it('näyttää opiskeluoikeuden tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 4.6.2016\n' +
          'Tila 4.6.2016 Valmistunut\n' +
          '1.9.2012 Läsnä')
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Pre-IB luokan oppimäärä\n' +
          'Oppilaitos / toimipiste Ressun lukio\n' +
          'Suorituskieli englanti\n' +
          'Suoritus valmis Vahvistus : 4.6.2016 Helsinki Reijo Reksi , rehtori')
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Kurssien määrä Arvosana (keskiarvo)\n' +
          'Äidinkieli ja kirjallisuus\n' +
          'ÄI1\n8 ÄI2\n8 ÄI3\n8 3 8\n(8,0)\n' +
          'A1-kieli, englanti\nENA1\n10 ENA2\n10 ENA5\n10 3 10\n(10,0)\n' +
          'B1-kieli, ruotsi\nRUB11\n8 RUB12\n7 2 7\n(7,5)\n' +
          'B2-kieli, ranska\nRAN3\n9 1 9\n(9,0)\n' +
          'B3-kieli, espanja\nES1\nS 1 6\n' +
          'Matematiikka, pitkä oppimäärä\nMAA11\n7 MAA12\n7 MAA13\n7 MAA2\n7 4 7\n(7,0)\n' +
          'Biologia\nBI1\n8 BI10\nS 2 8\n(8,0)\n' +
          'Maantieto\nGE2\n10 1 10\n(10,0)\n' +
          'Fysiikka\nFY1\n7 1 7\n(7,0)\n' +
          'Kemia\nKE1\n8 1 8\n(8,0)\n' +
          'Uskonto\nUK4\n10 1 10\n(10,0)\n' +
          'Filosofia\nFI1\nS 1 7\n' +
          'Psykologia\nPS1\n8 1 8\n(8,0)\n' +
          'Historia\nHI3\n9 HI4\n8 HI10\nS 3 8\n(8,5)\n' +
          'Yhteiskuntaoppi\nYH1\n8 1 8\n(8,0)\n' +
          'Liikunta\nLI1\n8 1 8\n(8,0)\n' +
          'Musiikki\nMU1\n8 1 8\n(8,0)\n' +
          'Kuvataide\nKU1\n9 1 9\n(9,0)\n' +
          'Terveystieto\nTE1\n7 1 7\n(7,0)\n' +
          'Opinto-ohjaus\nOP1\nS 1 7')
      })
    })
  })

  describe('IB-tutkintotodistus', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('040701-432D'), opinnot.valitseSuoritus(1, 'IB-tutkinto'))
    describe('Oppijan suorituksissa', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto(0)).to.equal("IB-tutkinto (International Baccalaureate)")
        expect(opinnot.getOppilaitos()).to.equal("Ressun lukio")
      })
    })

    describe('Kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)
      it('toimii', function () {
        expect(S('.ibkurssinsuoritus:eq(0) .kuvaus .value').text()).to.equal('TOK1')
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
