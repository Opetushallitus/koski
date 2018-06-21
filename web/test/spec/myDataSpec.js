describe('MyData', () => {
  const authentication = Authentication()
  const korhopankki = KorhoPankki()
  const etusivu = LandingPage()
  const omattiedot = OmatTiedotPage()

  const mydata = MyDataPage()

  before(
    authentication.logout,
    mydata.delAuthCookie,
    mydata.openPage,
    wait.until(korhopankki.isReady),
    korhopankki.login('100869-192W', 'Dippainssi', 'Dilbert'),
    wait.until(mydata.isVisible),
    //click(omattiedot.suoritusjakoButton)
  )

  describe('Kun käyttäjä on kirjautunut sisään', () => {
    it('Näytetään käyttäjälle nimi', () => {
      expect(mydata.getUserName()).equal('Dilbert Dippainssi')
    })
    it('Näytetään käyttäjälle syntymäaika', () => {
      expect(mydata.getBirthDate()).equal('10.8.1969')
    })
  })

})
