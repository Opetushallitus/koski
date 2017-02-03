describe('Omat tiedot', function() {
  var omattiedot = OmatTiedotPage()
  var authentication = Authentication()

  describe("Kun oppijalla on opiskeluoikeuksia", function() {
    before(authentication.login('Oili'), omattiedot.openPage)
    it('ne näytetään', function() {
      expect(omattiedot.oppija()).to.equal("Oppija, Oili (190751-739W)")
    })
  })

  describe("Kun oppijalla ei ole opiskeluoikeuksia", function() {
    before(authentication.login(), omattiedot.openPage)
    it('näytetään viesti', function() {
      expect(omattiedot.virhe()).to.equal("Tiedoillasi ei löydy opiskeluoikeuksia")
    })
  })
})