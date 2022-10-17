describe('MyData', function () {
  var authentication = Authentication()
  var korhopankki = KorhoPankki()
  var mydata = MyDataPage()
  var tietojenkaytto = TietojenKayttoPage()

  function login(lang, hetu, sukunimi, etunimi) {
    return [
      authentication.logout,
      mydata.delAuthCookie,
      function () {
        return mydata.addLangCookie(lang)
      },
      mydata.openPage,
      wait.until(korhopankki.isReady),
      korhopankki.login(hetu, sukunimi, etunimi),
      wait.until(mydata.isVisible)
    ]
  }

  before.apply(null, login('fi', '100869-192W', 'Dippainssi', 'Dilbert'))

  describe('Kun käyttäjä on kirjautunut sisään', function () {
    it('Näytetään käyttäjälle nimi', function () {
      expect(mydata.getUserName()).equal('Dilbert Dippainssi')
    })
    it('Näytetään käyttäjälle syntymäaika', function () {
      expect(mydata.getBirthDate()).equal('s. 10.8.1969')
    })
  })

  describe('Kun ollaan kumppanin sivulla kirjautuneena sisään', function () {
    it('Näytetään kumppanin nimi', function () {
      expect(mydata.getMemberName()).equal('HSL Helsingin Seudun Liikenne')
    })
    describe('Ja sallitaan kumppanin hakea käyttäjästä tietoja', function () {
      before(mydata.clickAccept, wait.until(mydata.accepted.isVisible))

      it('Näytetään nappi josta voidaan palata palveluntarjoajan sivulle', function () {
        expect(mydata.accepted.isReturnButtonVisible()).to.equal(true)
        expect(extractAsText(S('.acceptance-return-button'))).equal(
          'Palaa palveluntarjoajan sivulle'
        )
      })

      describe('Kun klikataan hyväksy-nappia', function () {
        before(
          wait.until(mydata.accepted.isReturnButtonVisible),
          mydata.accepted.clickReturn,
          wait.forMilliseconds(1000) // will redirect automatically, but we don't test that now
        )

        it('Päädytään oikealle sivulle', function () {
          expect(
            document.getElementById('testframe').contentWindow.document.URL
          ).to.equal(mydata.callbackURL)
        })
      })
    })
  })

  describe('Kun ollaan hyväksytty tietojen jakaminen', function () {
    before.apply(null, login('fi', '100869-192W', 'Dippainssi', 'Dilbert'))
    before(
      tietojenkaytto.go,
      wait.until(tietojenkaytto.isVisible),
      wait.until(tietojenkaytto.isPermissionsVisible)
    )

    it('Käyttäjälle näytetään oma nimi', function () {
      expect(extractAsText(S('.oppija-nimi > .nimi'))).equal(
        'Dilbert Dippainssi'
      )
    })

    it('Nähdään annettu lupa', function () {
      expect(extractAsText(tietojenkaytto.firstPermission)).equal(
        'HSL Helsingin Seudun Liikenne'
      )
      expect(extractAsText(S('.voimassaolo > .teksti > span'))).equal(
        'Lupa voimassa'
      )
    })

    describe('Kun perutaan annettu lupa', function () {
      before(
        tietojenkaytto.cancelPermission.cancelFirstPermission,
        wait.until(tietojenkaytto.cancelPermission.isWaitingForVerification),
        tietojenkaytto.cancelPermission.verifyCancel
      )

      it('Lupa poistuu näkyvistä', function () {
        expect(isElementVisible(tietojenkaytto.firstPermission)).to.equal(false)
        expect(
          isElementVisible(S('ul.kayttolupa-list > li.no-permission'))
        ).to.equal(true)
      })
    })
  })

  describe('Kun klikataan peruuta-nappia', function () {
    before.apply(null, login('fi', '100869-192W', 'Dippainssi', 'Dilbert'))
    before(mydata.clickCancel)
    before(
      wait.until(function () {
        return isElementVisible(S('.statistics-wrapper'))
      })
    )

    it('Päädytään oikealle sivulle', function () {
      expect(
        document.getElementById('testframe').contentWindow.document.URL
      ).to.equal(mydata.callbackURL)
    })
  })

  describe('Kun käyttäjällä ei ole opintoja Koskessa', function () {
    before.apply(null, login('fi', '270181-5263', 'Eikoskessa', 'Eino'))
    before(
      tietojenkaytto.go,
      wait.until(tietojenkaytto.isVisible),
      wait.until(tietojenkaytto.isPermissionsVisible)
    )

    it('Näytetään käyttäjälle nimi', function () {
      expect(tietojenkaytto.getUserName()).equal('Eino EiKoskessa')
    })
    it('Näytetään käyttölupien kohdalla oikea teksti', function () {
      expect(extractAsText(S('.kayttolupa-list > .no-permission'))).equal(
        'Et ole tällä hetkellä antanut millekään palveluntarjoajalle lupaa nähdä opintotietojasi Oma Opintopolusta. Luvan myöntäminen tapahtuu kyseisen palvelutarjoajan sivun kautta.'
      )
    })
    it('Ei näytetä virheilmoitusta', function () {
      expect(tietojenkaytto.isErrorShown()).to.equal(false)
    })
  })

  describe('Käyttäjä voi vaihtaa kielen', function () {
    before.apply(null, login('fi', '100869-192W', 'Dippainssi', 'Dilbert'))
    before(mydata.clickChangeLang, wait.until(mydata.isInSwedish))

    it('Suomesta ruotsiksi', function () {
      expect(extractAsText(S('.title > h1'))).equal('Min Studieinfo')
    })

    it('Ja kumppanin nimi vaihtuu ruotsinkieliseksi', function () {
      expect(mydata.getMemberName()).equal('HRT Helsingforsregionens trafik') // Flaky?
    })
  })
})
