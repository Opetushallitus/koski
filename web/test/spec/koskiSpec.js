describe('Koski', function() {
  var page = KoskiPage()
  var login = LoginPage()

  var eerola = 'Eerola, Jouni 081165-793C'

  describe('Tietoturva', function() {
    before(login.openPage)

    describe('Login-sivu', function() {
      before(login.openPage)
      it('näytetään, kun käyttäjä ei ole kirjautunut sisään', function() {
        expect(login.isVisible()).to.equal(true)
      })
      describe('Väärällä käyttäjätunnuksella', function() {
        before(login.login('fail', 'fail'))
        before(wait.until(login.isLoginErrorVisible))
        it('näytetään virheilmoitus', function() {})
      })
      describe('Väärällä salasanalla', function() {
        before(login.openPage)
        before(login.login('kalle', 'fail'))
        before(wait.until(login.isLoginErrorVisible))
        it('näytetään virheilmoitus', function() {})
      })
      describe('Onnistuneen loginin jälkeen', function() {
        before(login.openPage)
        before(login.login('kalle', 'kalle'))
        before(wait.until(page.isVisible))
        it('siirrytään Koski-etusivulle', function() {
          expect(page.isVisible()).to.equal(true)
        })
        it('näytetään kirjautuneen käyttäjän nimi', function() {
          expect(page.getUserName()).to.equal('kalle')
        })
      })
    })


    describe('Kun klikataan logout-linkkiä', function() {
      before(Authentication().login(), page.openPage, page.logout)

      it('Siirrytään login-sivulle', function() {
        expect(login.isVisible()).to.equal(true)
      })

      describe('Kun ladataan sivu uudelleen', function() {
        before(openPage('/koski', login.isVisible))

        it('Sessio on päättynyt ja login-sivu näytetään', function() {
          expect(login.isVisible()).to.equal(true)
        })
      })

      describe('Kun kirjaudutaan uudelleen sisään', function() {
        before(Authentication().login(), resetFixtures, page.openPage, page.oppijaHaku.search('jouni', [eerola]), page.logout, login.login('kalle', 'kalle'), wait.until(page.isReady))
        it ('Käyttöliittymä on palautunut alkutilaan', function() {
          expect(page.oppijaHaku.getSearchResults()).to.deep.equal([])
          expect(page.getSelectedOppija()).to.equal('')
        })
      })
    })

    describe('Session vanhennuttua', function() {
      before(Authentication().login(), page.openPage, Authentication().logout, page.oppijaHaku.search('eero', login.isVisible))

      it('Siirrytään login-sivulle', function() {
        expect(login.isVisible()).to.equal(true)
      })
    })
  })

  describe('Virhetilanteet', function() {
    before(Authentication().login())
    
    describe('Odottamattoman virheen sattuessa', function() {
      before(
        page.openPage,
        page.oppijaHaku.search('#error#', page.isErrorShown))

      it('näytetään virheilmoitus', function() {})
    })

    describe('Kun palvelimeen ei saada yhteyttä', function() {
      before(
        page.openPage,
        mockHttp('/koski/api/henkilo/search?query=blah', {}),
        page.oppijaHaku.search('blah', page.isErrorShown))

      it('näytetään virheilmoitus', function() {})
    })


    describe('Kun sivua ei löydy', function() {
      before(Authentication().login(), openPage('/koski/asdf', page.is404))

      it('näytetään 404-sivu', function() {})
    })
  })
})