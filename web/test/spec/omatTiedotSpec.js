describe('Omat tiedot', function() {
  var omattiedot = OmatTiedotPage()
  var opinnot = OpinnotPage()
  var authentication = Authentication()

  describe("Kun virkailijalla on opiskeluoikeuksia", function() {
    before(authentication.login('Oili'), omattiedot.openPage)
    it('ne näytetään', function() {
      expect(omattiedot.oppija()).to.equal("Opintosuorituksesi")
      expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal([
        'Stadin ammattiopisto', 'Jyväskylän normaalikoulu' ])
    })
  })

  describe("Kun oppijalla ei ole opiskeluoikeuksia", function() {
    before(authentication.login(), omattiedot.openPage)
    it('näytetään viesti', function() {
      expect(omattiedot.virhe()).to.equal("Tiedoillasi ei löydy opiskeluoikeuksia")
    })
  })

  var etusivu = LandingPage()
  var korhopankki = KorhoPankki()
  describe("Kun kansalainen kirjautuu ulos", function() {
    before(authentication.logout, etusivu.openPage, etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('290492-9455'), wait.until(omattiedot.isVisible) , click(findSingle('#logout')), wait.until(etusivu.isVisible))
    it("Näytetään etusivu", function() { })
  })
})