describe('MyData', () => {
  const authentication = Authentication()
  const korhopankki = KorhoPankki()
  const mydata = MyDataPage()

  before(
    authentication.logout,
    mydata.delAuthCookie,
    mydata.openPage,
    wait.until(korhopankki.isReady),
    korhopankki.login('100869-192W', 'Dippainssi', 'Dilbert'),
    wait.until(mydata.isVisible),
  )

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
      before(seq(
          mydata.clickAccept,
          wait.until(mydata.accepted.isVisible)
      ))
      it('Näytetään nappi josta voidaan palata palveluntarjoajan sivulle', () => {
        expect(mydata.accepted.isReturnButtonVisible()).to.equal(true)
        expect(extractAsText(S('a[href^="/koski/user/logout?target=http://example.org"]'))).equal('Palaa palveluntarjoajan sivulle')
      })
    })
  })
})
