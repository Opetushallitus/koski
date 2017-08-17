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
    describe('Esiopetus', function() {
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
        })

        describe('Kun painetaan Lisää-nappia', function() {
          before(addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Peruskoulun esiopetus'))

          it('lisätty oppija näytetään', function() {})

          describe('Käyttöliittymän tila', function() {
            it('Lisätty opiskeluoikeus näytetään', function() {
              expect(opinnot.getTutkinto()).to.equal('Peruskoulun esiopetus')
              expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
              expect(opinnot.getSuorituskieli()).to.equal('ruotsi')
              expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('102/011/2014')
            })
          })
        })
      })
    })
  })
})