describe('Korkeakoulutus', function() {
  var opintosuoritusote = OpintosuoritusotePage()
  var page = KoskiPage()
  var login = LoginPage()
  var opinnot = OpinnotPage()

  before(Authentication().login(), resetFixtures)

  describe('Valmis diplomi-insinööri', function() {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('100869-192W')
    )
    describe('Oppilaitos ja tutkinto', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto('konetekniikka')).to.equal('Dipl.ins., konetekniikka')
        expect(opinnot.getOppilaitos('konetekniikka')).to.equal('Aalto-yliopisto')
        expect(opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()).to.deep.equal([
          'korkeakoulututkinto 2013—2016, päättynyt',
          'korkeakoulunopintojakso'
        ])
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.korkeakoulututkinnonsuoritus .tutkinnon-osa:eq(0) .suoritus:eq(0) .nimi').text()).to.equal('Vapaasti valittavat opinnot (KON)')
      })
    })
    describe('Opintosuoritusote', function() {
      before(opinnot.avaaOpintosuoritusote('konetekniikka'))

      describe('Kun klikataan linkkiä', function() {
        it('näytetään', function() {
        })
      })

      describe('Opintosuoritusotteen avaaminen, kun käyttäjä ei ole kirjautunut', function() {
        before(Authentication().logout,  reloadTestFrame, wait.until(login.isVisible))
        it('Näytetään login-sivu', function() {
          expect(login.isVisible()).to.equal(true)
        })
      })
    })
  })
  describe('Maisteri, jolla ensisijainen opiskeluoikeus', function() {
    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('250668-293Y')
    )
    describe('Oppilaitos ja tutkinto', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto('Dipl.ins., kemian tekniikka')).to.equal('Dipl.ins., kemian tekniikka')
        expect(opinnot.getOppilaitos('Dipl.ins., kemian tekniikka')).to.equal('Aalto-yliopisto')
      })
    })
    describe('Opiskeluoikeus', function() {
      before(opinnot.avaaOpintosuoritusote('Dipl.ins., kemian tekniikka'))
      it('näytetään', function() {
        expect(S('section.opiskeluoikeus h3').text()).to.equal('Ensisijainen opinto-oikeus')
      })
    })
  })
  describe('Keskeneräinen tutkinto', function() {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('150113-4146')
    )

    it('näytetään välilehtipainike oikein', function() {
      expect(opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()).to.deep.equal([
        'korkeakoulunopintojakso',
        'korkeakoulututkinto 2011—2019, aktiivinen',
        'muukorkeakoulunsuoritus 2004—2004, aktiivinen'
      ])
    })

    describe('opintosuoritusote', function() {
      before(opinnot.avaaOpintosuoritusote('Lääketieteen'))
      it('näytetään', function() {
        expect(S('section.opiskeluoikeus h3').text()).to.equal('Ensisijainen opinto-oikeus')
      })
    })
  })
  describe('AMK, keskeyttänyt', function() {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('170691-3962')
    )
    describe('Oppilaitos ja tutkinto', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal('Ensihoitaja (AMK)')
        expect(opinnot.getOppilaitos()).to.equal('Yrkeshögskolan Arcada')
      })
    })
  })

  describe('AMK, valmis', function() {
    describe('Opiskeluoikeuden otsikko kun opintojaksot siirretty päätason suorituksen alle', function() {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('250686-102E')
      )
      it('näytetään', function() {
        expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.deep.equal(['Yrkeshögskolan Arcada, Fysioterapeutti (AMK) (2011—2015, päättynyt)'])
      })
    })
    describe('Haku toimii myös muuttuneella hetulla', function() {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('250686-6493', '250686-102E')
      )
      it('näytetään', function() {
        expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.deep.equal(['Yrkeshögskolan Arcada, Fysioterapeutti (AMK) (2011—2015, päättynyt)'])
      })
    })
    describe('Opiskeluoikeuden otsikko kun opintojaksot sekaisin päätason suorituksen kanssa', function() {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('090992-3237')
      )
      it('näytetään', function() {
        expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.deep.equal(['Yrkeshögskolan Arcada, Fysioterapeutti (AMK) (2011—2015, päättynyt)'])
      })
    })
  })

  describe('Tampereen duplikaattisuoritukset', function() {
    before(
      Authentication().login('pää'),
      page.openPage,
      page.oppijaHaku.searchAndSelect('060609-638L')
    )
    it('näytetään oikein', function() {
      expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.deep.equal( [ 'Tampereen yliopisto, Farmasian kandidaatti', 'Tampereen yliopisto, Proviisori (2016—2019, päättynyt)' ])
    })
  })
})
