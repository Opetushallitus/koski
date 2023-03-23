describe('Koski', function () {
  var page = KoskiPage()
  var login = LoginPage()

  var eerola = 'Çelik-Eerola, Jouni (081165-793C)'

  describe('Tietoturva', function () {
    before(login.openPage)

    describe('Kun avataan jokin ääkkösiä sisältävä sivu', function () {
      before(openPage('/koski/bää'))

      it('näytetään login sivu, kun käyttäjä ei ole kirjautunut sisään', function () {
        expect(login.isVisible()).to.equal(true)
      })
    })

    describe('Login-sivu', function () {
      it('näytetään, kun käyttäjä ei ole kirjautunut sisään', function () {
        expect(login.isVisible()).to.equal(true)
      })
      describe('Väärällä käyttäjätunnuksella', function () {
        before(login.login('fail', 'fail'))
        before(wait.until(login.isLoginErrorVisible))
        it('näytetään virheilmoitus', function () {})
      })
      describe('Väärällä salasanalla', function () {
        before(login.openPage)
        before(login.login('omnia-tallentaja', 'fail'))
        before(wait.until(login.isLoginErrorVisible))
        it('näytetään virheilmoitus', function () {})
      })
      describe('Onnistuneen loginin jälkeen', function () {
        before(login.openPage)
        before(login.login('kalle', 'kalle'))
        before(wait.until(page.isVisible))
        it('siirrytään Koski-etusivulle', function () {
          expect(page.isVisible()).to.equal(true)
        })
        it('näytetään kirjautuneen käyttäjän nimi', function () {
          expect(page.getUserName()).to.equal('kalle käyttäjä')
        })
      })
    })

    describe('Turvakielto', function () {
      before(
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect('151067-2193')
      )

      it('Turvakieltosymboli näytetään henkilölle jolla on turvakielto', function () {
        expect(isElementVisible(S('.turvakielto'))).to.equal(true)
      })
    })

    describe('Kun klikataan logout-linkkiä', function () {
      before(Authentication().login(), page.openPage, page.logout)

      it('Siirrytään login-sivulle', function () {
        expect(login.isVisible()).to.equal(true)
      })

      describe('Kun ladataan sivu uudelleen', function () {
        before(openPage('/koski/virkailija', login.isVisible))

        it('Sessio on päättynyt ja login-sivu näytetään', function () {
          expect(login.isVisible()).to.equal(true)
        })
      })

      describe('Kun kirjaudutaan uudelleen sisään', function () {
        before(
          Authentication().login(),
          resetFixtures,
          page.openPage,
          page.oppijaHaku.search('jouni', [eerola]),
          page.logout,
          login.login('kalle', 'kalle'),
          wait.until(page.isReady)
        )
        it('Käyttöliittymä on palautunut alkutilaan', function () {
          expect(page.oppijaHaku.getSearchResults()).to.deep.equal([])
          expect(page.getSelectedOppija()).to.equal('')
        })
      })
    })

    var etusivu = LandingPage()
    describe('Session vanhennuttua', function () {
      before(
        Authentication().login(),
        page.openPage,
        Authentication().logout,
        page.oppijaHaku.search('eero', etusivu.isVisible)
      )

      it('Siirrytään etusivulle', function () {
        expect(etusivu.isVisible()).to.equal(true)
      })
    })
  })

  describe('Yksilöintitieto', function () {
    before(
      Authentication().login('pää'),
      page.openPage,
      page.oppijaHaku.searchAndSelect('1.2.246.562.24.99999999123', 'Hetuton')
    )

    it('näytetään ilmoitus jos oppija on yksilöimätön', function () {
      expect(isElementVisible(S('.yksilöimätön'))).to.equal(true)
      expect(S('.oppijanumerorekisteri-link').attr('href')).to.equal(
        '/henkilo-ui/oppija/1.2.246.562.24.99999999123?permissionCheckService=KOSKI'
      )
    })
  })

  describe('Virhetilanteet', function () {
    before(Authentication().login())

    describe('Odottamattoman virheen sattuessa', function () {
      before(
        page.openPage,
        page.oppijaHaku.search('#error#', page.isErrorShown)
      )

      it('näytetään virheilmoitus', function () {})
    })

    describe('Kun palvelimeen ei saada yhteyttä', function () {
      before(
        page.openPage,
        mockHttp('/koski/api/henkilo/search', {}),
        page.oppijaHaku.search('blah', page.isErrorShown)
      )

      it('näytetään virheilmoitus', function () {})
    })

    describe('Kun sivua ei löydy', function () {
      before(Authentication().login(), openPage('/koski/asdf', page.is404))
      it('näytetään 404-sivu', function () {})
    })

    describe('Kun käyttäjällä ei ole Koski-oikeuksia', function () {
      before(login.openPage)
      before(login.login('Otto', 'Otto'))
      before(wait.untilVisible(findSingle('.no-access')))
      it('Näytetään Ei käyttöoikeuksia-teksti', function () {
        expect(S('h1').text()).to.equal('Koski')
        expect(S('h2').text()).to.equal('Ei käyttöoikeuksia')
      })
    })
  })

  describe('Sivun latauksessa ei tapahdu virheitä', function () {
    before(Authentication().login())

    it('ok', function () {
      expect(page.getErrorMessage()).to.equal('')
    })
  })
})
