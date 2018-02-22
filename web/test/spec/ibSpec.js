describe('IB', function( ) {
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
          'Oppiaine Laajuus (kurssia) Arvosana (keskiarvo)\n' +
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

  describe('IB-tutkinto', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('040701-432D'), opinnot.valitseSuoritus(1, 'IB-tutkinto'))
    describe('Oppijan suorituksissa', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto(0)).to.equal("IB-tutkinto (International Baccalaureate)")
        expect(opinnot.getOppilaitos()).to.equal("Ressun lukio")
      })
    })

    describe('Oppiaineet', function () {
      before(opinnot.expandAll)
      it('ryhmitellään aineryhmittäin', function () {
        var rivit = S('.oppiaineet tbody tr')
        expect(S(rivit.get(0)).hasClass('aineryhmä')).to.equal(true)
        expect(S(rivit.get(1)).hasClass('A')).to.equal(true)
        expect(S(rivit.get(2)).hasClass('A2')).to.equal(true)
        expect(S(rivit.get(3)).hasClass('aineryhmä')).to.equal(true)
        expect(S(rivit.get(4)).hasClass('HIS')).to.equal(true)
        expect(S(rivit.get(5)).hasClass('PSY')).to.equal(true)
        expect(S(rivit.get(6)).hasClass('aineryhmä')).to.equal(true)
        expect(S(rivit.get(7)).hasClass('BIO')).to.equal(true)
        expect(S(rivit.get(8)).hasClass('aineryhmä')).to.equal(true)
        expect(S(rivit.get(9)).hasClass('MATST')).to.equal(true)
      })

      it('arvosanalle näytetään alaviite, kun arvosana on ennakkoarvosana', function() {
        expect(S('.oppiaineet tbody tr:eq(1) .arvosana .footnote-hint').text()).to.equal(' *')
        expect(S('.osasuoritukset .selitteet').text()).to.equal('* = ennustettu arvosana')
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
          'Koulutus IB-tutkinto (International Baccalaureate)\n' +
          'Oppilaitos / toimipiste Ressun lukio\n' +
          'Suorituskieli englanti\n' +
          'Theory of knowledge A\n' +
          'Extended essay B\n' +
          'Creativity action service S\n' +
          'Lisäpisteet 3\n' +
          'Suoritus valmis Vahvistus : 4.6.2016 Helsinki Reijo Reksi , rehtori')
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Kurssien määrä Arvosana (keskiarvo)\n' +
          'Studies in language and literature\n' +
          'Language A: literature, suomi\nFIN_S1\n4 FIN_S2\n4 FIN_S3\nS FIN_S4\n5 FIN_S5\n6 FIN_S6\n5 FIN_S7\n5 FIN_S8\nS FIN_S9\n5 9 4 *\n(4,9)\n' +
          'Language A: language and literature, englanti\nENG_B_H1\n6 ENG_B_H2\n7 ENG_B_H4\nS ENG_B_H5\n6 ENG_B_H6\n6 ENG_B_H8\n5 6 7 *\n(6,0)\n' +
          'Individuals and societies\n' +
          'History\nHIS_H3\n6 HIS_H4\n6 HIS_H5\n7 HIS_H6\n6 HIS_H7\n1 HIS_H9\nS 6 6 *\n(5,2)\n' +
          'Psychology\nPSY_S1\n6 PSY_S2\n6 PSY_S3\n6 PSY_S4\n5 PSY_S5\nS PSY_S6\n6 PSY_S7\n5 PSY_S8\n2 PSY_S9\nS 9 7 *\n(5,1)\n' +
          'Experimental sciences\nBiology\nBIO_H1\n5 BIO_H2\n4 BIO_H3\nS BIO_H4\n5 BIO_H5\n5 BIO_H6\n2 BIO_H7\n3 BIO_H8\n4 BIO_H9\n1 9 5 *\n(3,6)\n' +
          'Mathematics\n' +
          'Mathematical studies\nMATST_S1\n5 MATST_S2\n7 MATST_S3\n6 MATST_S4\n6 MATST_S5\n4 MATST_S6\nS 6 5 *\n(5,6)\n' +
          '* = ennustettu arvosana')
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
