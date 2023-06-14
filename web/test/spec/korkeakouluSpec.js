describe('Korkeakoulutus', function () {
  var page = KoskiPage()
  var login = LoginPage()
  var opinnot = OpinnotPage()

  before(Authentication().login(), resetFixtures)

  describe('Valmis diplomi-insinööri', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('100869-192W'))
    describe('Oppilaitos ja tutkinto', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto('konetekniikka')).to.equal(
          'Dipl.ins., konetekniikka'
        )
        expect(opinnot.getOppilaitos('konetekniikka')).to.equal(
          'Aalto-yliopisto'
        )
        expect(
          opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()
        ).to.deep.equal([
          'korkeakoulututkinto 2013—2016, valmistunut',
          'korkeakoulunopintojakso'
        ])
      })
    })
    describe('Kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)
      it('toimii', function () {
        expect(
          S(
            '.korkeakoulututkinnonsuoritus .tutkinnon-osa:eq(0) .suoritus:eq(0) .nimi'
          ).text()
        ).to.equal('Vapaasti valittavat opinnot (KON)')
      })
    })
  })

  describe('Maisteri, jolla ensisijainen opiskeluoikeus', function () {
    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('250668-293Y')
    )
    describe('Oppilaitos ja tutkinto', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto('Dipl.ins., kemian tekniikka')).to.equal(
          'Dipl.ins., kemian tekniikka'
        )
        expect(opinnot.getOppilaitos('Dipl.ins., kemian tekniikka')).to.equal(
          'Aalto-yliopisto'
        )
      })
    })
  })

  describe('Keskeneräinen tutkinto', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('150113-4146'))

    it('näytetään välilehtipainike oikein', function () {
      expect(
        opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()
      ).to.deep.equal([
        'korkeakoulunopintojakso',
        'korkeakoulututkinto 2011—2019, aktiivinen',
        'muukorkeakoulunsuoritus 2004—2004, aktiivinen'
      ])
    })
  })

  describe('AMK, keskeyttänyt', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('170691-3962'))
    describe('Oppilaitos ja tutkinto', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto()).to.equal('Ensihoitaja (AMK)')
        expect(opinnot.getOppilaitos()).to.equal('Yrkeshögskolan Arcada')
      })
    })
  })

  describe('AMK, valmis', function () {
    describe('Opiskeluoikeuden otsikko kun opintojaksot siirretty päätason suorituksen alle', function () {
      before(page.openPage, page.oppijaHaku.searchAndSelect('250686-102E'))
      it('näytetään', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal([
          'Yrkeshögskolan Arcada, Fysioterapeutti (AMK) (2011—2015, valmistunut)'
        ])
      })
    })
    describe('Haku toimii myös muuttuneella hetulla', function () {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('250686-6493', '250686-102E')
      )
      it('näytetään', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal([
          'Yrkeshögskolan Arcada, Fysioterapeutti (AMK) (2011—2015, valmistunut)'
        ])
      })
    })
    describe('Opiskeluoikeuden otsikko kun opintojaksot sekaisin päätason suorituksen kanssa', function () {
      before(page.openPage, page.oppijaHaku.searchAndSelect('090992-3237'))
      it('näytetään', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal([
          'Yrkeshögskolan Arcada, Fysioterapeutti (AMK) (2011—2015, valmistunut)'
        ])
      })
    })
  })

  describe('Kaksi päätason suoritusta', function () {
    before(
      Authentication().login('pää'),
      page.openPage,
      page.oppijaHaku.searchAndSelect('270680-459P')
    )

    it('Otsikkona näytetään se jolla viimeisin vahvistus', function () {
      expect(
        opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
      ).to.deep.equal([
        'Aalto-yliopisto, Fil. maist., englannin kieli (2000—, valmistunut)'
      ])
    })
  })

  describe('Oppilaitoksen nimi', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('060458-331R'))
    it('Näytetään oppilaitoksen nimi', function () {
      expect(OpinnotPage().getOppilaitos(0)).to.equal('Aalto-yliopisto')
    })

    describe('Kun nimi on muuttunut', function () {
      before(page.oppijaHaku.searchAndSelect('030199-3419'))
      it('Näytetään oppilaitoksen nimi valmistumishetkellä', function () {
        expect(OpinnotPage().getOppilaitos(1)).to.equal(
          'Aalto-yliopisto -vanha'
        )
      })
    })
  })

  describe('Siirto opiskelija', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('141199-418X'))
    it('Näytetään nykyinen oppilaitos', function () {
      expect(OpinnotPage().getOppilaitos(0)).to.equal('Aalto-yliopisto')
    })
  })

  describe('Hyväksiluettu suoritus', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('141199-418X'),
      opinnot.expandAll
    )
    it('Näytetään nykyinen oppilaitos', function () {
      expect(
        extractAsText(
          S('.suoritus-taulukko tbody:nth-child(3) .suoritus .nimi')
        )
      ).to.equal('Opiskelutaidot')
      expect(
        extractAsText(
          S('.suoritus-taulukko tbody:nth-child(3) .toimipiste .value')
        )
      ).to.equal('Aalto-yliopisto')
    })
  })

  describe('Järjestävä organisaatio', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('150113-4146'),
      opinnot.expandAll
    )
    it('Näytetään lisätiedoissa', function () {
      expect(extractAsText(S('.lisätiedot'))).to.include(
        'Järjestävä organisaatio Yrkeshögskolan Arcada\n'
      )
    })
  })

  describe('Sisältyvyys on vahvempi sidos kuin opiskeluoikeusAvain valittaessa mihin tutkintoon opintosuoritus kuuluu', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('200990-228R'))

    describe('Molemmat tutkinnot', function () {
      it('näkyvät välilehden alaotsikossa', function () {
        expect(
          opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()
        ).to.deep.equal([
          'korkeakoulututkinto 2014—2016, valmistunut',
          'korkeakoulututkinto 2014—2015, valmistunut'
        ])
      })
    })
    describe('Maisterin tutkinto', function () {
      it('näytetään oppilaitos ja tutkinto', function () {
        expect(opinnot.getTutkinto(0)).to.equal(
          'Dipl.ins., konetekniikka'
        )
        expect(opinnot.getTutkintoKoodi(0)).to.equal(
          '751101'
        )
        expect(opinnot.getVirtaNimi(0)).to.equal(
          'Tekniikan koulutus (UUSI2005), Diplomi-insinöörin tutkinto Konetekniikan koulutusohjelma'
        )
        expect(opinnot.getOppilaitos(0)).to.equal('Aalto-yliopisto')
      })
    })
    describe('Kandin tutkinto', function () {
      it('näytetään oppilaitos ja tutkinto', function () {
        expect(opinnot.getTutkinto(1)).to.equal(
          'Tekn. kand., konetekniikka'
        )
        expect(opinnot.getTutkintoKoodi(1)).to.equal(
          '655101'
        )
        expect(opinnot.getVirtaNimi(1)).to.equal(
          'Tekniikan koulutus (UUSI2005), Tekniikan kandidaatin tutkinto Konetekniikan koulutusohjelma'
        )
        expect(opinnot.getOppilaitos(1)).to.equal('Aalto-yliopisto')
      })
    })
    describe('Sisältyvä toisen opiskeluoikeusavaimen kurssi näkyvissä maisterin tutkinnossa', function () {
      before(opinnot.expandAll)
      it('toimii', function () {
        expect(
          S(
            '.korkeakoulututkinnonsuoritus:eq(0) .tutkinnon-osa:eq(0) .suoritus:eq(0) .nimi'
          ).text()
        ).to.equal('Simuloinnin ja optimoinnin peruskurssi')
        expect(
          S(
            '.korkeakoulututkinnonsuoritus:eq(0) .tutkinnon-osa:eq(1) .suoritus:eq(0) .nimi'
          ).text()
        ).to.equal('Diplomityö')
      })
    })
  })

  describe('Fuusioitunut myöntäjä', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('080795-884U'),
      opinnot.expandAll
    )

    describe('ja hyväksiluettu opintosuoritus', function () {
      it('näkyy oikein', function () {
        expect(
          extractAsText(
            S('.suoritus-taulukko tbody:nth-child(4) .suoritus .nimi')
          )
        ).to.equal('Insinöörimatematiikka C 2')
        expect(
          extractAsText(
            S('.suoritus-taulukko tbody:nth-child(4) .toimipiste .value')
          )
        ).to.equal('Lahden ammattikorkeakoulu')
      })
    })
  })
})
