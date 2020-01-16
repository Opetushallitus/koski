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

    describe('Kun kyseessä on tavallinen oppilaitostallentaja', function() {
      it('Varhaiskasvatus organisaatiohierarkian ulkopuolelle valitsinta ei näytetä', function() {
        expect(isElementVisible(findSingle('#varhaiskasvatus-checkbox'))).to.equal(false)
      })
    })

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

        it('koulutusmoduulia ja järjestämismuotoa ei näytetä', function () {
          expect(editor.property('koulutustoimija').isVisible()).to.equal(false)
          expect(editor.property('järjestämismuoto').isVisible()).to.equal(false)
        })
      })
    })

    describe('Lisääminen organisaatiohierarkian ulkopuolelle', function () {
      before(prepareForNewOppija('hki-tallentaja', '230872-7258'))

      describe('Kun kyseessä on koulutustoimija joka on myös varhaiskasvatuksen järjestäjä', function() {
        it('Varhaiskasvatus organisaatiohierarkian ulkopuolelle valitsin näytetään', function() {
          expect(isElementVisible(findSingle('#varhaiskasvatus-checkbox'))).to.equal(true)
        })
      })

      describe('Kun valitaan oman organisaatiohierarkian ulkopuolelle tallentaminen', function () {
        before(
          addOppija.selectVarhaiskasvatusOrganisaationUlkopuolelta(true),
          addOppija.enterOppilaitos('')
        )

        it('vain oman organisaation ulkopuoliset varhaiskasvatustoimipisteet näytetään', function () {
          expect(addOppija.oppilaitokset()).to.deep.equal([
            'Pyhtään kunta Päiväkoti Touhula',
            'Päiväkoti Touhula'
          ])
        })

        describe('Kun syötetään loput datat', function() {
          before(
            addOppija.selectJärjestämismuoto('Ostopalvelu, kunnan tai kuntayhtymän järjestämä'),
            addOppija.enterValidDataPäiväkodinEsiopetus({oppilaitos: 'Päiväkoti Touhula'})
          )

          it('lisää nappi enabloituu', function () {
            expect(addOppija.isEnabled()).to.equal(true)
          })

          describe('Kun painetaan Lisää-nappia', function () {
            before(addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Päiväkodin esiopetus'))

            it('lisätty oppija näytetään', function () {
              expect(opinnot.getTutkinto()).to.equal('Päiväkodin esiopetus')
              expect(opinnot.getOppilaitos()).to.equal('Päiväkoti Touhula')
              expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('102/011/2014')
              expect(extractAsText(S('.tunniste-koodiarvo'))).to.equal('001102')
            })

            it('koulutusmoduuli ja järjestämismuoto näytetään', function () {
              expect(editor.property('koulutustoimija').getValue()).to.equal('HELSINGIN KAUPUNKI')
              expect(editor.property('järjestämismuoto').getValue()).to.equal('Ostopalvelu, kunnan tai kuntayhtymän järjestämä')
            })

            describe('Kun vahvistetaan suoritus', function() {
              before(
                editor.edit,
                opinnot.tilaJaVahvistus.merkitseValmiiksi,
                opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).setValue('Lisää henkilö'),
                opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).propertyBySelector('.nimi').setValue('Pekka Päiväkoti'),
                opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).propertyBySelector('.titteli').setValue('päiväkodin johtaja'),
                opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
                editor.saveChanges
              )

              it('toimii', function() { })

              describe('Toinen koulutustoimija', function() {
                before(
                  prepareForNewOppija('tornio-tallentaja', '230872-7258'),
                  addOppija.selectVarhaiskasvatusOrganisaationUlkopuolelta(true),
                  addOppija.selectJärjestämismuoto('Ostopalvelu, kunnan tai kuntayhtymän järjestämä'),
                  addOppija.enterValidDataPäiväkodinEsiopetus({oppilaitos: 'Päiväkoti Touhula'}),
                  addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Päiväkodin esiopetus'),
                  editor.edit,
                  opinnot.tilaJaVahvistus.merkitseValmiiksi
                )

                it('ei näe toisten koulutustoimijoiden myöntäjiä', function() {
                  expect(opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).getOptions()).to.deep.equal(['Lisää henkilö'])
                })
              })
            })
          })
        })
      })
    })
  })

  describe('Tietojen muuttaminen', function() {
    before(Authentication().login(), page.openPage, page.oppijaHaku.searchAndSelect('300996-870E'))

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
