describe('MyData', () => {
  const authentication = Authentication()
  const korhopankki = KorhoPankki()
  const mydata = MyDataPage()

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
          expect(document.getElementById('testframe').contentWindow.document.URL).to.equal('http://localhost:7021/koski/pulssi')
        })

      })
    })
  })

  describe('Kun klikataan logout-nappia', () => {
    before(...login('fi'))
    before(() => click('.user .logout > a > span')())
    before(wait.until(() => isElementVisible(S('.statistics-wrapper'))))

    it('Päädytään oikealle sivulle', () => {
      expect(document.getElementById('testframe').contentWindow.document.URL).to.equal('http://localhost:7021/koski/pulssi')
    })
  })


  describe('Ruotsinkielisenä voidaan kirjautua sisään', () => {
    before(...login('sv'))

    it('Voidaan kirjautua sisään', () => {
    })
  })
})
