describe('Lukiokoulutus', function( ){
  var page = KoskiPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()
  before(Authentication().login(), resetFixtures)

  describe('Lukion päättötodistus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('020655-2479'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto(0)).to.equal("Lukion oppimäärä")
        expect(opinnot.getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('näyttää opiskeluoikeuden tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal('Alkamispäivä : 1.9.2012 — Päättymispäivä : 1.6.2016\n' +
          'Tila 1.6.2016 Valmistunut\n' +
          '1.9.2012 Läsnä\n' +
          'Lisätiedot\n' +
          'Pidennetty päättymispäivä ei\n' +
          'Ulkomainen vaihto-opiskelija ei\n' +
          'Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa Pikkuvanha yksilö\n' +
          'Yksityisopiskelija ei\n' +
          'Erityisen koulutustehtävän jaksot 1.9.2012 — 1.9.2012\n' +
          'Tehtävä Erityisenä koulutustehtävänä taide\n' +
          'Ulkomaanjaksot 1.9.2012 — 1.9.2013\n' +
          'Maa Ruotsi\n' +
          'Kuvaus Harjoittelua ulkomailla')
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Lukion oppimäärä 60/011/2015\n' +
          'Oppimäärä Lukio suoritetaan nuorten opetussuunnitelman mukaan\n' +
          'Toimipiste Jyväskylän normaalikoulu\n' +
          'Suorituskieli suomi\n' +
          'Todistuksella näkyvät lisätiedot Ruotsin opinnoista osa hyväksiluettu Ruotsissa suoritettujen lukio-opintojen perusteella\n' +
          'Suoritus: VALMIS Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi')
      })

      it('näyttää oppiaineiden arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Kurssien määrä Arvosana (keskiarvo)\n' +
          'Äidinkieli ja kirjallisuus\n' +
          'ÄI1\n8 ÄI2\n8 ÄI3\n8 ÄI4\n8 ÄI5\n9 ÄI6\n9 ÄI8\n9 ÄI9\n9 8 9\n(8.5)\n' +
          'A1-kieli, englanti\nENA1\n10 ENA2\n10 ENA3\n9 ENA4\n9 ENA5\n9 ENA6\n8 ENA7\n8 ENA8\n9 ENA 10\nS 9 9\n(9.0)\n' +
          'B1-kieli, ruotsi\nRUB11\n9 RUB12\n8 RUB13\n7 RUB14\n7 RUB15\n6 5 7\n(7.4)\n' +
          'B3-kieli, latina\nLAB31\n9 LAB32\n8 2 9\n(8.5)\n' +
          'Matematiikka, pitkä oppimäärä\nMAA1\n9 MAA2\n10 MAA3\n8 MAA4\n10 MAA5\n7 MAA6\n9 MAA7\n8 MAA8\n7 MAA9\n9 MAA10\n8 MAA11\n8 MAA12\n10 MAA13\n8 MAA14\n9 MAA16\n9 15 9\n(8.6)\n' +
          'Biologia\nBI1\n8 BI2\n9 BI3\n8 BI4\n9 BI5\n10 BI6\nS BI7\nS BI8\nS 8 9\n(8.8)\n' +
          'Maantieto\nGE1\n9 GE2\n7 2 8\n(8.0)\nFysiikka\nFY1\n8 FY2\n9 FY3\n9 FY4\n7 FY5\n8 FY6\n7 FY7\n8 FY8\n7 FY9\n7 FY10\nS FY11\nS FY12\nS FY13\nS 13 8\n(7.8)\n' +
          'Kemia\nKE1\n8 KE2\n9 KE3\n9 KE4\n5 KE5\n7 KE6\n5 KE7\nS KE8\nS 8 8\n(7.2)\n' +
          'Uskonto tai elämänkatsomustieto\nUE1\n8 UE2\n7 UE3\n8 3 8\n(7.7)\nFilosofia\nFI1\n8 1 8\n(8.0)\n' +
          'Psykologia\nPS1\n9 1 9\n(9.0)\n' +
          'Historia\nHI1\n7 HI2\n8 HI3\n7 HI4\n6 4 7\n(7.0)\n' +
          'Yhteiskuntaoppi\nYH1\n8 YH2\n8 2 8\n(8.0)\n' +
          'Liikunta\nLI1\n8 LI2\n9 LI12\nS 3 9\n(8.5)\n' +
          'Musiikki\nMU1\n8 1 8\n(8.0)\n' +
          'Kuvataide\nKU1\n8 KU2\n9 2 9\n(8.5)\n' +
          'Terveystieto\nTE1\n8 1 9\n(8.0)')
      })
    })
    describe('Kurssin tiedot', function() {
      function kurssi(koodi) { return S(".kurssi:contains(" + koodi +")") }
      function details(kurssi) { return S(kurssi).find(".details") }

      describe('Kun klikataan', function() {
        before(function() { triggerEvent(kurssi('MAA16'), 'click') })
        it('näyttää kurssin tiedot', function() {
          expect(extractAsText(details(kurssi('MAA16')))).to.equal(
            'Tunniste MAA16\n' +
            'Nimi Analyyttisten menetelmien lisäkurssi, ksy, vuositaso 2\n' +
            'Laajuus 1 kurssia\n' +
            'Kuvaus Kurssilla syvennetään kurssien MAA4, MAA5 ja MAA7 sisältöjä.\n' +
            'Kurssin tyyppi Syventävä\n' +
            'Tila Suoritus valmis\n' +
            'Arvosana 9\n' +
            'Arviointipäivä 4.6.2016')
        })
      })
      describe('Kun klikataan uudestaan', function() {
        before(function() { triggerEvent(kurssi('MAA16'), 'click') })
        it('piilottaa kurssin tiedot', function() {
          expect(details(kurssi('MAA16')).is(':visible')).to.equal(false)
        })
      })
      describe('Kaikkien kurssien tiedot', function() {
        it('voidaan avata yksitellen virheettömästi', function() {
          toArray(S(".kurssi")).forEach(function(kurssi) {
            expect(details(kurssi).is(':visible')).to.equal(false)
            triggerEvent(kurssi, 'click')
            expect(details(kurssi).is(':visible')).to.equal(true)
            expect(extractAsText(details(kurssi)).length > 10).to.equal(true)
          })
        })
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in LukioSpec.scala
        expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })

  describe('Opintosuoritusote', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('020655-2479'))
    before(opinnot.avaaOpintosuoritusote(1))

    describe('Kun klikataan linkkiä', function() {
      it('näytetään', function() {
      })
    })
  })

  describe('Lukioon valmistava koulutus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('211007-442N'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal("Maahanmuuttajien ja vieraskielisten lukiokoulutukseen valmistava koulutus")
        expect(opinnot.getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.lukioonvalmistavankoulutuksensuoritus .osasuoritukset .koulutusmoduuli:eq(0) .nimi .value').text()).to.equal('Suomi toisena kielenä ja kirjallisuus')
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in LukioSpec.scala
        expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })

  // TODO: lukion oppiaineen oppimäärän suoritus
  // TODO: luva
  // TODO: kurssien detskut
})