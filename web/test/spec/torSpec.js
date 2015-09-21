describe("TOR", function() {
  var page = TorPage()
  var login = LoginPage()
  var eero = 'esimerkki, eero 010101-123N'
  var markkanen = 'markkanen, eero '
  var eerola = 'eerola, jouni '
  var teija = 'tekijä, teija 150995-914X'

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

  describe("OppijaHaku", function() {
    before(login.openPage)
    before(page.loginAndOpen)
    it("näytetään, kun käyttäjä on kirjautunut sisään", function() {
      expect(page.isVisible()).to.equal(true)
    })
    describe("Hakutulos-lista", function() {
      it("on aluksi tyhjä", function() {
        expect(page.getSearchResults().length).to.equal(0)
      })
    })
    describe("Kun haku tuottaa yhden tuloksen", function() {
      before(page.search("esimerkki"))
      it("ensimmäinen tulos näytetään", function() {
        expect(page.getSearchResults()).to.deep.equal([eero])
      })
      it("ensimmäinen tulos valitaan automaattisesti", function() {
        expect(page.getSelectedOppija()).to.equal(eero)
      })
      describe("Kun haku tuottaa uudestaan yhden tuloksen", function() {
        before(page.search("teija"))
        it("tulosta ei valita automaattisesti", function() {
          expect(page.getSelectedOppija()).to.equal(eero)
        })
      })
    })
    describe("Haun tyhjentäminen", function() {
      before(page.search("eero"))
      before(page.search(""))
      it("säilyttää oppijavalinnan", function() {
        expect(page.getSelectedOppija()).to.equal(eero)
      })
      it("tyhjentää hakutulos-listauksen", function() {
        expect(page.getSearchResults().length).to.equal(0)
      })
    })
    describe("Kun haku tuottaa useamman tuloksen", function() {
      before(page.search("eero"))

      it("Hakutulokset näytetään", function() {
        expect(page.getSearchResults()).to.deep.equal([eero, eerola, markkanen])
      })


      before(page.selectOppija("markkanen"))

      it("valitsee oppijan", function() {
        expect(page.getSelectedOppija()).to.equal(markkanen)
      })
    })

  })
})