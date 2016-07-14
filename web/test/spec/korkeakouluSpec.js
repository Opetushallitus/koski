describe('Korkeakoulutus', function() {
  var opintosuoritusote = OpintosuoritusotePage()
  var page = KoskiPage()
  var login = LoginPage()

  before(Authentication().login(), resetFixtures)

  describe('Valmis diplomi-insinööri', function() {
    before(
      page.openPage,
      page.oppijaHaku.search('290492-9455', page.isOppijaSelected('Dilbert'))
    )
    describe('Oppilaitos ja tutkinto', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal('Dipl.ins., konetekniikka')
        expect(OpinnotPage().getOppilaitos()).to.equal('Aalto-yliopisto')
        //expect(OpinnotPage().getOpintoOikeus()).to.equal('(Opiskeluoikeus päättynyt, Suoritus valmis)') // TODO: fix later
      })
    })
    describe('Opintosuoritusote', function() {
      before(OpinnotPage().avaaOpintosuoritusote(1))

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
      page.oppijaHaku.search('090888-929X', page.isOppijaSelected('Harri'))
    )
    describe('Oppilaitos ja tutkinto', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto(1)).to.equal('Dipl.ins., kemian tekniikka')
        expect(OpinnotPage().getOppilaitos(1)).to.equal('Helsingin yliopisto')
        //expect(OpinnotPage().getOpintoOikeus(1)).to.equal('(Opiskeluoikeus passivoitu, Suoritus kesken)') // TODO: fix later
      })
    })
    describe('Opiskeluoikeus', function() {
      before(OpinnotPage().avaaOpintosuoritusote(2))
      it('näytetään', function() {
        expect(S('section.opiskeluoikeus h3').text()).to.equal('Ensisijainen opinto-oikeus')
      })
    })
  })
  describe('Keskeneräinen tutkinto', function() {
    before(
      page.openPage,
      page.oppijaHaku.search('010675-9981', page.isOppijaSelected('Kikka')),
      OpinnotPage().avaaOpintosuoritusote(1)
    )
    it('näytetään', function() {
      expect(S('section.opiskeluoikeus h3').text()).to.equal('Ensisijainen opinto-oikeus')
    })
  })
  describe('AMK, keskeyttänyt', function() {
    before(
      page.openPage,
      page.oppijaHaku.search('100193-948U', page.isOppijaSelected('Valtteri'))
    )
    describe('Oppilaitos ja tutkinto', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal('Ensihoitaja (AMK)')
        expect(OpinnotPage().getOppilaitos()).to.equal('Yrkeshögskolan Arcada')
        //expect(OpinnotPage().getOpintoOikeus()).to.equal('(Opiskeluoikeus luopunut, Suoritus kesken)') // TODO: fix later
      })
    })
  })
})