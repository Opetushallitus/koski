describe('Maksuttomuus', function () {
  var addOppija = AddOppijaPage()
  var opinnot = OpinnotPage()
  var page = KoskiPage()
  var editor = opinnot.opiskeluoikeusEditor()

  describe('Uusi opiskeluoikeus voidaan luoda', function () {
    describe('Maksuttomana', function () {
      before(
        prepareForNewOppija('pää', '010104A6094'),
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectAloituspäivä('1.1.2021'),
        addOppija.selectMaksuttomuus(1),
        addOppija.submit,
        opinnot.expandAll
      )

      it('Lisätiedoissa on maksuttomuus-tieto', function () {
        expect(extractAsText(S('.lisätiedot'))).to.equal(
          'Lisätiedot\n' + 'Koulutuksen maksuttomuus 1.1.2021 — Maksuton'
        )
      })

      describe('Maksuttomuus-tietoon voidaan muokata', function () {
        before(
          editor.edit,
          editor
            .property('maksuttomuus')
            .propertyBySelector('.calendar-input')
            .setValue('10.8.2021'),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )

        it('Maksuttomuus jaksojen lisäys toimii', function () {
          expect(extractAsText(S('.lisätiedot'))).to.equal(
            'Lisätiedot\n' + 'Koulutuksen maksuttomuus 10.8.2021 — Maksuton'
          )
        })
      })
      describe('Oikeutta maksuttomuuteen pidennetty -tieto voidaan lisätä', function () {
        before(
          editor.edit,
          editor.property('oikeuttaMaksuttomuuteenPidennetty').addItem,
          editor
            .property('oikeuttaMaksuttomuuteenPidennetty')
            .propertyBySelector('.alku')
            .setValue('1.1.2025'),
          editor
            .property('oikeuttaMaksuttomuuteenPidennetty')
            .propertyBySelector('.loppu')
            .setValue('12.12.2025'),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )

        it('Lisäys toimii', function () {
          expect(extractAsText(S('.lisätiedot'))).to.equal(
            'Lisätiedot\n' +
              'Koulutuksen maksuttomuus 10.8.2021 — Maksuton\n' +
              'Oikeutta maksuttomuuteen pidennetty 1.1.2025 — 12.12.2025'
          )
        })
      })
    })
    describe('Maksullisena', function () {
      before(
        prepareForNewOppija('pää', '010104A6094'),
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectAloituspäivä('1.8.2021'),
        addOppija.selectMaksuttomuus(2),
        addOppija.submit,
        opinnot.expandAll
      )
      it('Lisätiedot ovat tyhjät', function () {
        expect(extractAsText(S('.lisätiedot'))).to.equal(
          'Lisätiedot\n' + 'Koulutuksen maksuttomuus 1.8.2021 — Maksullinen'
        )
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
      it('Lisätiedot ovat tyhjät', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.not.include(
          'Lisätiedot'
        )
      })
    })
  })

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
