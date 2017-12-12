describe('Omat tiedot', function() {
  var omattiedot = OmatTiedotPage()
  var opinnot = OpinnotPage()
  var authentication = Authentication()

  describe("Kun oppijalla on opiskeluoikeuksia", function() {
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
})