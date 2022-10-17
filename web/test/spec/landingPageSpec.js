describe('Etusivu', function () {
  var etusivu = LandingPage()
  var authentication = Authentication()
  var virkailijaPage = KoskiPage()
  var korhopankki = KorhoPankki()

  describe('Kun käyttäjä ei ole kirjautunut sisään', function () {
    before(authentication.logout, etusivu.openPage)
    it('näytetään sisäänkirjautumisnappi', function () {
      expect(isElementVisible(S('.lander button'))).to.equal(true)
    })
  })

  describe('Kun virkailija on kirjautunut sisään', function () {
    before(
      authentication.login(),
      etusivu.go,
      wait.until(virkailijaPage.oppijataulukko.isReady)
    )
    it('näytetään oppilaslistaus', function () {
      expect(virkailijaPage.oppijataulukko.isVisible()).to.equal(true)
    })
    after(authentication.logout)
  })

  describe('Kun kansalainen on kirjautunut sisään', function () {
    before(
      authentication.logout,
      etusivu.openPage,
      etusivu.login(),
      wait.until(korhopankki.isReady),
      korhopankki.login('100869-192W'),
      wait.until(OmatTiedotPage().isVisible)
    )

    it('näytetään omattiedot sivu', function () {
      expect(extractAsText(S('.user-info .name'))).to.equal(
        'Dilbert Dippainssi'
      )
      expect(extractAsText(S('.oppija-content header h1'))).to.equal('Opintoni')
    })
  })
})
