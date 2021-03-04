describe('Maksuttomuus', function() {
  var addOppija = AddOppijaPage()
  var opinnot = OpinnotPage()

  describe('Uusi opiskeluoikeus voidaan luoda', function() {
    describe('Maksuttomuus-tiedolla', function () {
      before(
        prepareForNewOppija('pää', '010104A6094'),
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectAloituspäivä('1.8.2021'),
        addOppija.selectMaksuttomuus(1),
        addOppija.submit,
        opinnot.expandAll
      )

      it('Lisätiedoissa on maksuttomuus-tieto', function() {
        expect(extractAsText(S('.lisätiedot'))).to.equal(
          'Lisätiedot\n' +
          'Maksuttomuus 1.8.2021 —\n' +
          'Maksuton kyllä')
      })
    })
    describe('Ilman maksuttomuus-tietoa', function () {
      before(
        prepareForNewOppija('pää', '311203A1454'),
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectAloituspäivä('1.8.2021'),
        addOppija.selectMaksuttomuus(0),
        addOppija.submit,
        opinnot.expandAll
      )
      it('Lisätiedot ovat tyhjät', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.not.include(
          'Lisätiedot'
        )
      })
    })
  })

  describe('Maksuttomuus tieto ei näytetä valittavaksi', function () {
    describe('Jos opiskeluoikeus alkaa ennen 1.8.2021', function () {
      before(
        prepareForNewOppija('pää', '311203A1454'),
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectAloituspäivä('31.7.2021'),
      )
      it('On piilotettu', function () {
        expect(S('.opiskeluoikeuden-tiedot').length).to.equal(0)
      })
    })
    describe('Jos opiskeluoikeuden suoritus ei oikeuta tiedon lisäämistä', function () {
      before(
        prepareForNewOppija('pää', '311203A1454'),
        addOppija.enterValidDataMuuAmmatillinen(),
        addOppija.selectAloituspäivä('1.8.2021'),
      )
      it('On piilotettu', function () {
        expect(S('.opiskeluoikeuden-tiedot').length).to.equal(0)
      })
    })
  })
})
