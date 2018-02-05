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
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal('Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 8.6.2016\n' +
          'Tila 8.6.2016 Valmistunut\n' +
          '1.9.2012 Läsnä\n' +
          'Lisätiedot\n' +
          'Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa Pikkuvanha yksilö\n' +
          'Erityisen koulutustehtävän jaksot 1.9.2012 — 1.9.2012 Tehtävä Erityisenä koulutustehtävänä taide\n' +
          'Ulkomaanjaksot 1.9.2012 — 1.9.2013 Maa Ruotsi Kuvaus Harjoittelua ulkomailla\n' +
          'Oikeus maksuttomaan asuntolapaikkaan kyllä\n' +
          'Sisäoppilaitosmainen majoitus 1.9.2012 — 1.9.2013')
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Lukion oppimäärä 60/011/2015\n' +
          'Opetussuunnitelma Lukio suoritetaan nuorten opetussuunnitelman mukaan\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Suorituskieli suomi\n' +
          'Todistuksella näkyvät lisätiedot Ruotsin opinnoista osa hyväksiluettu Ruotsissa suoritettujen lukio-opintojen perusteella\n' +
          'Ryhmä 12A\n' +
          'Suoritus valmis Vahvistus : 8.6.2016 Jyväskylä Reijo Reksi , rehtori')
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Kurssien määrä Arvosana (keskiarvo)\n' +
          'Äidinkieli ja kirjallisuus\n' +
          'ÄI1\n8 ÄI2\n8 ÄI3\n8 ÄI4\n8 ÄI5\n9 ÄI6\n9 ÄI8\n9 ÄI9\n9 8 9\n(8,5)\n' +
          'A1-kieli, englanti\nENA1\n10 ENA2\n10 ENA3\n9 ENA4\n9 ENA5\n9 ENA6\n8 ENA7\n8 ENA8\n9 ENA 10\nS 9 9\n(9,0)\n' +
          'B1-kieli, ruotsi\nRUB11\n9 RUB12\n8 RUB13\n7 RUB14\n7 RUB15\n6 5 7\n(7,4)\n' +
          'B3-kieli, latina\nLAB31\n9 LAB32\n8 2 9\n(8,5)\n' +
          'Matematiikka, pitkä oppimäärä\nMAA1\n9 MAA2\n10 MAA3\n8 MAA4\n10 MAA5\n7 MAA6\n9 MAA7\n8 MAA8\n7 MAA9\n9 MAA10\n8 MAA11\n8 MAA12\n10 MAA13\n8 MAA14\n9 MAA16\n9 15 9\n(8,6)\n' +
          'Biologia\nBI1\n8 BI2\n9 BI3\n8 BI4\n9 BI5\n10 BI6\nS BI7\nS BI8\nS 8 9\n(8,8)\n' +
          'Maantieto\nGE1\n9 GE2\n7 2 8\n(8,0)\nFysiikka\nFY1\n8 FY2\n9 FY3\n9 FY4\n7 FY5\n8 FY6\n7 FY7\n8 FY8\n7 FY9\n7 FY10\nS FY11\nS FY12\nS FY13\nS 13 8\n(7,8)\n' +
          'Kemia\nKE1\n8 KE2\n9 KE3\n9 KE4\n5 KE5\n7 KE6\n5 KE7\nS KE8\nS 8 8\n(7,2)\n' +
          'Uskonto\nUE1\n8 UE2\n7 UE3\n8 3 8\n(7,7)\nFilosofia\nFI1\n8 1 8\n(8,0)\n' +
          'Psykologia\nPS1\n9 1 9\n(9,0)\n' +
          'Historia\nHI1\n7 HI2\n8 HI3\n7 HI4\n6 4 7\n(7,0)\n' +
          'Yhteiskuntaoppi\nYH1\n8 YH2\n8 2 8\n(8,0)\n' +
          'Liikunta\nLI1\n8 LI2\n9 LI12\nS 3 9\n(8,5)\n' +
          'Musiikki\nMU1\n8 1 8\n(8,0)\n' +
          'Kuvataide\nKU1\n8 KU2\n9 2 9\n(8,5)\n' +
          'Terveystieto\nTE1\n8 1 9\n(8,0)\n' +
          'Tanssi ja liike\nITT1\n10 1 10\n(10,0)\n' +
          'Teemaopinnot\nMTA\nS 1 S\n' +
          'Oman äidinkielen opinnot\nOA1\nS 1 S')
      })
    })

    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in LukioSpec.scala
        expect(todistus.vahvistus()).to.equal('Jyväskylä 8.6.2016 Reijo Reksi rehtori')
      })
    })

    describe('Kurssin tiedot', function() {
      var kurssi = opinnot.oppiaineet.oppiaine('MA').kurssi('MAA16')
      before(page.openPage, page.oppijaHaku.searchAndSelect('020655-2479'))
      describe('Kun klikataan', function() {
        before(kurssi.toggleDetails)
        it('näyttää kurssin tiedot', function() {
          expect(kurssi.detailsText()).to.equal(
            'Tunniste MAA16\n' +
            'Nimi Analyyttisten menetelmien lisäkurssi, ksy, vuositaso 2\n' +
            'Laajuus 1 kurssia\n' +
            'Kuvaus Kurssilla syvennetään kurssien MAA4, MAA5 ja MAA7 sisältöjä.\n' +
            'Kurssin tyyppi Syventävä')
        })
      })
      describe('Kun klikataan uudestaan', function() {
        before(kurssi.toggleDetails)
        it('piilottaa kurssin tiedot', function() {
          expect(kurssi.detailsText()).to.equal('')
        })
      })
      describe('Kaikkien kurssien tiedot', function() {
        it('voidaan avata yksitellen virheettömästi', function() {
          Kurssi.findAll().forEach(function(kurssi) {
            expect(kurssi.detailsText()).to.equal('')
            kurssi.toggleDetails()
            expect(kurssi.detailsText().length > 10).to.equal(true)
          })
        })
      })
      describe('Tietojen muokkaaminen', function() {
        var opiskeluoikeusEditor = opinnot.opiskeluoikeusEditor()
        before(
          opiskeluoikeusEditor.edit,
          opiskeluoikeusEditor.property('tila').removeItem(0),
          opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi
        )
        describe('Arvosanan muuttaminen', function() {
          var kurssi = opinnot.oppiaineet.oppiaine('MA').kurssi('MAA16')
          before(kurssi.arvosana.selectValue('6'), opiskeluoikeusEditor.saveChanges, wait.until(page.isSavedLabelShown))
          it('Toimii', function() {
            expect(kurssi.arvosana.getText()).to.equal('6')
          })
        })
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

  describe('Lukion oppiaineen oppimäärän suoritus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('210163-2367'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal("Historia")
        expect(opinnot.getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Kurssien määrä Arvosana (keskiarvo)\n' +
          'Historia\n' +
          'HI1\n7 HI2\n8 HI3\n7 HI4\n6 4 9\n(7,0)')
      })
    })
  })

  describe('Lukioon valmistava koulutus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('211007-442N'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal("Lukiokoulutukseen valmistava koulutus")
        expect(opinnot.getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('näyttää oppiaineiden ja kurssien arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Lukioon valmistavat opinnot\n' +
          'Oppiaine Kurssien määrä Arvosana (keskiarvo)\n' +
          'Äidinkieli ja kirjallisuus, Suomi toisena kielenä ja kirjallisuus\n' +
          'STK\n' +
          'S 1 S\n' +
          'Muut kielet, ruotsi\n' +
          'RU1\n' +
          'S 1 S\n' +
          'Matemaattiset ja luonnontieteelliset opinnot\n' +
          'MAT1\n' +
          'S 1 S\n' +
          'Yhteiskuntatietous ja kulttuurintuntemus\n' +
          'YHKU1\n' +
          'S 1 S\n' +
          'Opinto-ohjaus\n' +
          'OPO1\n' +
          'S 1 S\n' +
          'Tietojenkäsittely\n' +
          'ATK1\n' +
          'S 1 S\n' +
          'Valinnaisena suoritetut lukiokurssit\n' +
          'Oppiaine Kurssien määrä Arvosana (keskiarvo)\n' +
          'A1-kieli, englanti\n' +
          'ENA1\n' +
          '8 1 S\n' +
          '(8,0)'
        )
      })
    })
    describe('Kurssin tiedot', function() {
      var kurssi = opinnot.oppiaineet.oppiaine('LVMALUO').kurssi('MAT1')
      describe('Kun klikataan', function() {
        before(kurssi.toggleDetails)
        it('näyttää kurssin tiedot', function() {
          expect(kurssi.detailsText()).to.equal(
            'Tunniste MAT1\n' +
            'Nimi Matematiikan kertauskurssi\n' +
            'Laajuus 1 kurssia\n' +
            'Kuvaus Matematiikan kertauskurssi'
          )
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
})
