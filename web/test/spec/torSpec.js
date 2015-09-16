describe("TOR", function() {
  var page = TorPage();
  var login = LoginPage();

  describe("Login-sivu", function() {
    before(login.openPage)
    it("näytetään, kun käyttäjä ei ole kirjautunut sisään", function() {
      expect(login.isVisible()).to.equal(true)
    })
    describe("Väärällä käyttäjätunnuksella", function() {
      before(login.login("fail", "fail"))
      before(wait.until(login.isLoginErrorVisible))
      it("näytetään virheilmoitus", function() {})
    })
    describe("Onnistuneen loginin jälkeen", function() {
      before(login.login("kalle", "asdf"))
      before(wait.until(page.isVisible))
      it("siirrytään TOR-etusivulle", function() {
        expect(page.isVisible()).to.equal(true)
      })
    })
  })
})