describe("TOR", function() {
  var page = TorPage()
  var login = LoginPage()
  var authentication = Authentication()
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
      expect(page.isNoResultsLabelShown()).to.equal(false)
    })
    describe("Hakutulos-lista", function() {
      it("on aluksi tyhjä", function() {
        expect(page.getSearchResults().length).to.equal(0)
      })
    })
    describe("Kun haku tuottaa yhden tuloksen", function() {
      before(page.search("esimerkki", 1))
      it("ensimmäinen tulos näytetään", function() {
        expect(page.getSearchResults()).to.deep.equal([eero])
        expect(page.isNoResultsLabelShown()).to.equal(false)
      })

      it("ensimmäinen tulos valitaan automaattisesti", wait.until(function() { return page.getSelectedOppija() == eero }))

      describe("Kun haku tuottaa uudestaan yhden tuloksen", function() {
        before(page.search("teija", 1))
        it("tulosta ei valita automaattisesti", function() {
          expect(page.getSelectedOppija()).to.equal(eero)
        })
      })
    })
    describe("Haun tyhjentäminen", function() {
      before(page.search("esimerkki", 1))
      before(page.search("", 0))

      it("säilyttää oppijavalinnan", function() {
        expect(page.getSelectedOppija()).to.equal(eero)
      })

      it("tyhjentää hakutulos-listauksen", function() {
        expect(page.getSearchResults().length).to.equal(0)
        expect(page.isNoResultsLabelShown()).to.equal(false)
      })
    })
    describe("Kun haku tuottaa useamman tuloksen", function() {
      before(page.search("eero", 3))

      it("Hakutulokset näytetään", function() {
        expect(page.getSearchResults()).to.deep.equal([eero, eerola, markkanen])
      })

      before(page.selectOppija("markkanen"))

      it("valitsee oppijan", function() {
        expect(page.getSelectedOppija()).to.equal(markkanen)
      })
    })

    describe("Kun haku ei tuota tuloksia", function() {
      before(page.search("asdf", page.isNoResultsLabelShown))

      it("Näytetään kuvaava teksti", function() {
        expect(page.isNoResultsLabelShown()).to.equal(true)
      })
    })
  })
  describe("Uuden oppijan lisääminen", function() {
    var addOppija = AddOppijaPage()

    before(
      resetMocks,
      authentication.login,
      page.openPage,
      page.search("asdf", page.isNoResultsLabelShown),
      page.addNewOppija
    )

    describe("Aluksi", function() {
      it("Lisää-nappi on disabloitu", function() {
        expect(addOppija.isEnabled()).to.equal(false)
      })
    })

    describe("Kun syötetään validit tiedot", function() {
      before(addOppija.enterValidData)

      it("Lisää-nappi on enabloitu", function() {
        expect(addOppija.isEnabled()).to.equal(true)
      })

      describe("Kun painetaan Lisää-nappia", function() {
        before(addOppija.submit)
        before(wait.until(function() { return page.getSelectedOppija() === "Oppija, Ossi Olavi 300994-9694"}))

        it("lisätty oppija näytetään", function() {})
      })
    })
  })
  describe("Navigointi suoraan oppijan sivulle", function() {
    before(authentication.login)
    before(openPage("/tor/oppija/1.2.246.562.24.00000000001", page.isOppijaSelected("eero")))

    it("Oppijan tiedot näytetään", function() {
      expect(page.getSelectedOppija()).to.equal(eero)
    })

    it("Hakutulos näytetään", function() {
      expect(page.getSearchResults()).to.deep.equal([eero])
    })
  })
  describe("Tietoturva", function() {
    before(login.openPage)


    describe("Oppijarajapinta", function() {
      before(openPage("/tor/api/oppija?query=eero", authenticationErrorIsShown))

      it("vaatii autentikaation", function () {
        expect(authenticationErrorIsShown()).to.equal(true)
      })
    })

    describe("Suoritusrajapinta", function() {
      before(openPage("/tor/api/suoritus", authenticationErrorIsShown))

      it("vaatii autentikaation", function () {
        expect(authenticationErrorIsShown()).to.equal(true)
      })
    })

    describe("Kun klikataan logout-linkkiä", function() {
      before(authentication.login, page.openPage, page.logout)

      it("Siirrytään login-sivulle", function() {
        expect(login.isVisible()).to.equal(true)
      })

      describe("Kun ladataan sivu uudelleen", function() {
        before(openPage("/tor", login.isVisible))

        it("Sessio on päättynyt ja login-sivu näytetään", function() {
          expect(login.isVisible()).to.equal(true)
        })
      })
    })

    describe("Session vanhennuttua", function() {
      before(authentication.login, page.openPage, authentication.logout, page.search("eero", login.isVisible))

      it("Siirrytään login-sivulle", function() {
        expect(login.isVisible()).to.equal(true)
      })
    })
  })
})

function authenticationErrorIsShown() {
  return S('body').text() === 'Not authenticated'
}

function resetMocks() {
  return Q($.ajax({ url: "/tor/fixtures/reset", method: "post"}))
}