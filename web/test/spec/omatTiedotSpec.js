describe('Omat tiedot', function() {
  var omattiedot = OmatTiedotPage()
  var opinnot = OpinnotPage()
  var authentication = Authentication()

  describe("Kun oppijalla on opiskeluoikeuksia", function() {
    before(authentication.login('Oili'), omattiedot.openPage)
    it('ne näytetään', function() {
      expect(omattiedot.oppija()).to.equal("Oppija, Oili (190751-739W)")

      expect(opinnot.opiskeluoikeudet.opiskeluoikeustyypit()).to.deep.equal([
        'Ammatillinen koulutus', 'Lukiokoulutus', 'Perusopetus'
      ])
    })
  })

  describe("Kun oppijalla ei ole opiskeluoikeuksia", function() {
    before(authentication.login(), omattiedot.openPage)
    it('näytetään viesti', function() {
      expect(omattiedot.virhe()).to.equal("Tiedoillasi ei löydy opiskeluoikeuksia")
    })
  })
})