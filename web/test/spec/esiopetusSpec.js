describe('Esiopetus', function() {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var addOppija = AddOppijaPage()
  var editor = opinnot.opiskeluoikeusEditor()

  before(Authentication().login(), resetFixtures)

  describe('Esiopetuksen opiskeluoikeudet', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('300996-870E'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
        expect(opinnot.getTutkinto()).to.equal('Peruskoulun esiopetus')
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.esiopetuksensuoritus .koulutusmoduuli .tunniste').text()).to.equal('Peruskoulun esiopetus')
      })
    })
  })

  describe('Opiskeluoikeuden lisääminen', function() {
    before(prepareForNewOppija('kalle', '230872-7258'))

    describe('Aluksi', function() {
      it('Lisää-nappi on disabloitu', function() {
        expect(addOppija.isEnabled()).to.equal(false)
      })
    })

    describe('Kun syötetään validit tiedot', function() {
      before(addOppija.enterValidDataEsiopetus({suorituskieli: 'ruotsi'}))

      describe('Käyttöliittymän tila', function() {
        it('Lisää-nappi on enabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(true)
        })

        it('Ei näytetä opintojen rahoitus -kenttää', function() {
          expect(addOppija.rahoitusIsVisible()).to.equal(false)
        })
      })

      describe('Kun painetaan Lisää-nappia', function() {
        before(
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Peruskoulun esiopetus')
        )

        it('lisätty oppija näytetään', function() {
          expect(opinnot.getTutkinto()).to.equal('Peruskoulun esiopetus')
          expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
          expect(opinnot.getSuorituskieli()).to.equal('ruotsi')
          expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('102/011/2014')
        })
      })
    })
  })

  describe('Opiskeluoikeuden lisääminen päiväkotiin', function() {
    before(prepareForNewOppija('kalle', '230872-7258'))

    describe('Kun syötetään validit tiedot', function() {
      before(addOppija.enterValidDataPäiväkodinEsiopetus())

      describe('Käyttöliittymän tila', function() {
        it('Lisää-nappi on enabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(true)
        })

        it('Ei näytetä opintojen rahoitus -kenttää', function() {
          expect(addOppija.rahoitusIsVisible()).to.equal(false)
        })
      })

      describe('Kun painetaan Lisää-nappia', function() {
        before(
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Päiväkodin esiopetus')
        )

        it('lisätty oppija näytetään', function() {
          expect(opinnot.getTutkinto()).to.equal('Päiväkodin esiopetus')
          expect(opinnot.getOppilaitos()).to.equal('Helsingin kaupunki toimipaikka 12241')
          expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('102/011/2014')
        })
      })
    })
  })

  describe('Tietojen muuttaminen', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('300996-870E'))

    describe('Kurssin kuvauksen ja sanallisen arvion muuttaminen', function() {
      var kuvaus = editor.subEditor('.osasuoritukset tbody').propertyBySelector('.kuvaus')

      before(editor.edit, opinnot.expandAll, kuvaus.setValue('Uusi kuvaus'), editor.saveChanges, opinnot.expandAll)
      it('Toimii', function() {
        expect(kuvaus.getValue()).to.equal('Uusi kuvaus')
      })
    })

    describe('Päätason suorituksen poistaminen', function() {
      before(editor.edit)

      describe('Mitätöintilinkki', function() {
        it('Ei näytetä', function() {
          expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(false)
        })
      })
    })
  })

  describe('Aloituspäivä', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('300996-870E'))

    describe('Opiskeluoikeuden aloituspäivän muuttaminen', function() {
      before(
        editor.edit,
        opinnot.expandAll,
        Page(function() {return S('#content')}).setInputValue('.tila .opiskeluoikeusjakso:last-child .date input', '1.1.2005')
      )
      it('Toimii', function() {
        expect(S('.tila .opiskeluoikeusjakso:last-child .date input').val()).to.equal('1.1.2005')
        expect(S('.opiskeluoikeuden-voimassaoloaika .alkamispäivä .date').html()).to.equal('1.1.2005')
      })
    })
  })
})
