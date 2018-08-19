describe('MyData', () => {
  const authentication = Authentication()
  const korhopankki = KorhoPankki()
  const mydata = MyDataPage()
  const tietojenkaytto = TietojenKayttoPage()

  const login = (lang) => [
    authentication.logout,
    mydata.delAuthCookie,
    () => mydata.addLangCookie(lang),
    mydata.openPage,
    wait.until(korhopankki.isReady),
    korhopankki.login('100869-192W', 'Dippainssi', 'Dilbert'),
    wait.until(mydata.isVisible)
  ]

  before(...login('fi'))

  describe('Kun käyttäjä on kirjautunut sisään', () => {
    it('Näytetään käyttäjälle nimi', () => {
      expect(mydata.getUserName()).equal('Dilbert Dippainssi')
    })
    it('Näytetään käyttäjälle syntymäaika', () => {
      expect(mydata.getBirthDate()).equal('10.8.1969')
    })
  })

  describe('Kun ollaan kumppanin sivulla kirjautuneena sisään', () => {
    it('Näytetään kumppanin nimi', () => {
      expect(mydata.getMemberName()).equal('HSL Helsingin Seudun Liikenne')
    })
    describe('Ja sallitaan kumppanin hakea käyttäjästä tietoja', () => {
      before(
        mydata.clickAccept,
        wait.until(mydata.accepted.isVisible),
    )

      it('Näytetään nappi josta voidaan palata palveluntarjoajan sivulle', () => {
        expect(mydata.accepted.isReturnButtonVisible()).to.equal(true)
        expect(extractAsText(S('.acceptance-return-button'))).equal('Palaa palveluntarjoajan sivulle')
      })

      describe('Kun klikataan hyväksy-nappia', () => {
        before(
          wait.until(mydata.accepted.isReturnButtonVisible),
          mydata.accepted.clickReturn,
          wait.forMilliseconds(1000), // will redirect automatically, but we don't test that now
        )

        it('Päädytään oikealle sivulle', () => {
          expect(document.getElementById('testframe').contentWindow.document.URL).to.equal(mydata.callbackURL)
        })

      })
    })
  })

  describe('Kun ollaan hyväksytty tietojen jakaminen', () => {
    before(
      ...login('fi'),
      tietojenkaytto.go,
      wait.until(tietojenkaytto.isVisible),
      tietojenkaytto.expandPermissions,
      wait.until(tietojenkaytto.isPermissionsExpanded)
    )

    it('Käyttäjälle näytetään oma nimi', () => {
      expect(extractAsText(S('.oppija-nimi > .nimi'))).equal('Dilbert Dippainssi')
    })

    it('Nähdään annettu lupa', () => {
      expect(extractAsText(tietojenkaytto.firstPermission)).equal('HSL Helsingin Seudun Liikenne')
      expect(extractAsText(S('.voimassaolo > .teksti > span'))).equal('Lupa voimassa')
    })

    describe('Kun perutaan annettu lupa', () => {
      before(
        tietojenkaytto.cancelPermission.cancelFirstPermission,
        wait.until(tietojenkaytto.cancelPermission.isWaitingForVerification),
        tietojenkaytto.cancelPermission.verifyCancel
      )

      it('Lupa poistuu näkyvistä', () => {
        expect(isElementVisible(tietojenkaytto.firstPermission)).to.equal(false)
        expect(isElementVisible(S('ul.kayttolupa-list > li.no-permission'))).to.equal(true)
      })
    })
  })

  describe('Kun klikataan logout-nappia', () => {
    before(...login('fi'))
    before(mydata.clickLogout)
    before(wait.until(() => isElementVisible(S('.statistics-wrapper'))))

    it('Päädytään oikealle sivulle', () => {
      expect(document.getElementById('testframe').contentWindow.document.URL).to.equal(mydata.callbackURL)
    })
  })

  describe('Kun klikataan peruuta-nappia', () => {
    before(...login('fi'))
    before(mydata.clickCancel)
    before(wait.until(() => isElementVisible(S('.statistics-wrapper'))))

    it('Päädytään oikealle sivulle', () => {
      expect(document.getElementById('testframe').contentWindow.document.URL).to.equal(mydata.callbackURL)
    })
  })


  describe('Ruotsinkielisenä voidaan kirjautua sisään', () => {
    before(
      ...login('sv'),
      wait.until(mydata.isVisible)
    )

    /*
    it('Ja sivusto on ruotsiksi', () => {
      expect(mydata.isInSwedish()).equal(true) // Korhopankki forces lang cookie to 'fi'
    })*/
  })

  describe('Käyttäjä voi vaihtaa kielen', () => {
    before(
      ...login('fi'),
      mydata.clickChangeLang,
      wait.until(mydata.isInSwedish),
    )

    it('Suomesta ruotsiksi', () => {
      expect(extractAsText(S('.title > h1'))).equal('Min Studieinfo')
    })
  })
})
