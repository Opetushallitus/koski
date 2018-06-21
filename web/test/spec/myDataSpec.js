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

  describe('Kun foo', () => {
    it('Onnistuu, näyttää henkilöpalvelussa olevat nimitiedot', () => {
      expect("foo").equal("fuzz")
    })
  })

  describe('Kun bar', () => {
    it('Ei muuten onnistu', () => {
      expect('bar').equal('foo')
    })
  })
})
