describe('Maksuttomuus', function () {
  var addOppija = AddOppijaPage()

  describe('Maksuttomuus tieto ei näytetä valittavaksi', function () {
    describe('Jos opiskeluoikeus alkaa ennen 1.1.2021', function () {
      before(
        prepareForNewOppija('pää', '311203A1454'),
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectAloituspäivä('31.12.2020')
      )
      it('On piilotettu', function () {
        expect(S('.opiskeluoikeuden-tiedot').length).to.equal(0)
      })
    })
    describe('Jos opiskeluoikeuden suoritus ei oikeuta tiedon lisäämistä', function () {
      before(
        prepareForNewOppija('pää', '311203A1454'),
        addOppija.enterValidDataMuuAmmatillinen(),
        addOppija.selectAloituspäivä('1.1.2021')
      )
      it('On piilotettu', function () {
        expect(S('.opiskeluoikeuden-tiedot').length).to.equal(0)
      })
    })
  })
})
