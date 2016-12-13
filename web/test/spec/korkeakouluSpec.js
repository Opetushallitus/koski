describe('Korkeakoulutus', function() {
  var opintosuoritusote = OpintosuoritusotePage()
  var page = KoskiPage()
  var login = LoginPage()
  var opinnot = OpinnotPage()

  before(Authentication().login(), resetFixtures)

  describe('Valmis diplomi-insinööri', function() {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('290492-9455')
    )
    describe('Oppilaitos ja tutkinto', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal('Dipl.ins., konetekniikka')
        expect(opinnot.getOppilaitos()).to.equal('Aalto-yliopisto')
        //expect(opinnot.getOpintoOikeus()).to.equal('(Opiskeluoikeus päättynyt, Suoritus valmis)') // TODO: fix later
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.korkeakoulututkinnonsuoritus .korkeakoulunopintojaksonsuoritus:eq(0) .koulutusmoduuli:eq(0) .tunniste .nimi .value').text()).to.equal('Vapaasti valittavat opinnot (KON)')
      })
    })
    describe('Opintosuoritusote', function() {
      before(opinnot.avaaOpintosuoritusote(1))

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
      page.oppijaHaku.searchAndSelect('090888-929X')
    )
    describe('Oppilaitos ja tutkinto', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto(1)).to.equal('Dipl.ins., kemian tekniikka')
        expect(opinnot.getOppilaitos(1)).to.equal('Helsingin yliopisto')
        //expect(opinnot.getOpintoOikeus(1)).to.equal('(Opiskeluoikeus passivoitu, Suoritus kesken)') // TODO: fix later
      })
    })
    describe('Opiskeluoikeus', function() {
      before(opinnot.avaaOpintosuoritusote(2))
      it('näytetään', function() {
        expect(S('section.opiskeluoikeus h3').text()).to.equal('Ensisijainen opinto-oikeus')
      })
    })
  })
  describe('Keskeneräinen tutkinto', function() {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('010675-9981'),
      opinnot.avaaOpintosuoritusote(1)
    )
    it('näytetään', function() {
      expect(S('section.opiskeluoikeus h3').text()).to.equal('Ensisijainen opinto-oikeus')
    })
  })
  describe('AMK, keskeyttänyt', function() {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('100193-948U')
    )
    describe('Oppilaitos ja tutkinto', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal('Ensihoitaja (AMK)')
        expect(opinnot.getOppilaitos()).to.equal('Yrkeshögskolan Arcada')
        //expect(opinnot.getOpintoOikeus()).to.equal('(Opiskeluoikeus luopunut, Suoritus kesken)') // TODO: fix later
      })
    })
  })
})