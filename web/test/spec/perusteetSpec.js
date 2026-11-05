describe('EPerusteet', function () {
  var page = KoskiPage()
  before(
    Authentication().login(),
    page.openPage,
    page.oppijaHaku.searchAndSelect('220109-784L')
  )

  describe('Kun perustetta ei löydy eperusteista', function () {
    before(
      page.oppijaHaku.searchAndSelect('Aikuinen, AikuisopiskelijaMuuKuinVos')
    )

    it('peruste näytetään ilman linkkiä', function () {
      expect(isElementVisible(S('.diaarinumero a'))).to.equal(false)
      expect(S('.diaarinumero').text()).to.equal('19/011/2015')
    })
  })
})
