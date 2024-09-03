describe('Esiopetus', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var addOppija = AddOppijaPage()
  var editor = opinnot.opiskeluoikeusEditor()
  const varhaiskasvatusOrganisaationUlkopuolellaCbSelector =
    '[data-testid="uusiOpiskeluoikeus.modal.hankintakoulutus.esiopetus"]'

  before(Authentication().login(), resetFixtures)

  describe('Esiopetuksen opiskeluoikeudet', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('300996-870E'))
    describe('Oppijan suorituksissa', function () {
      it('näytetään', function () {
        expect(opinnot.getOppilaitos(1)).to.equal('Jyväskylän normaalikoulu')
        expect(opinnot.getTutkinto(1)).to.equal('Peruskoulun esiopetus')
      })
    })
    describe('Kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)
      it('toimii', function () {
        expect(
          S('.esiopetuksensuoritus .koulutusmoduuli .tunniste').text()
        ).to.equal('Peruskoulun esiopetusPeruskoulun esiopetus')
      })
    })
  })

  describe('Opiskeluoikeuden lisääminen', function () {
    before(prepareForNewOppija('kalle', '230872-7258'))

    describe('Aluksi', function () {
      it('Lisää-nappi on disabloitu', function () {
        expect(addOppija.isEnabled()).to.equal(false)
      })
    })

    describe('Kun syötetään validit tiedot', function () {
      before(addOppija.enterValidDataEsiopetus({ suorituskieli: 'ruotsi' }))

      describe('Käyttöliittymän tila', function () {
        it('Lisää-nappi on enabloitu', function () {
          expect(addOppija.isEnabled()).to.equal(true)
        })

        it('Ei näytetä opintojen rahoitus -kenttää', function () {
          expect(addOppija.rahoitusIsVisible()).to.equal(false)
        })
      })

      describe('Kun painetaan Lisää-nappia', function () {
        before(
          addOppija.submitAndExpectSuccess(
            'Tyhjä, Tero (230872-7258)',
            'Peruskoulun esiopetus'
          )
        )

        it('lisätty oppija näytetään', function () {
          expect(opinnot.getTutkinto()).to.equal('Peruskoulun esiopetus')
          expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
          expect(opinnot.getSuorituskieli()).to.equal('ruotsi')
          expect(
            editor.propertyBySelector('.diaarinumero').getValue()
          ).to.equal('102/011/2014')
        })
      })
    })
  })

  describe('Opiskeluoikeuden lisääminen päiväkotiin', function () {
    before(prepareForNewOppija('kalle', '230872-7258'))

    describe('Kun kyseessä on tavallinen oppilaitostallentaja', function () {
      it('Varhaiskasvatus organisaatiohierarkian ulkopuolelle valitsinta ei näytetä', function () {
        expect(
          isElementVisible(
            findSingle(varhaiskasvatusOrganisaationUlkopuolellaCbSelector)
          )
        ).to.equal(false)
      })
    })

    describe('Kun syötetään validit tiedot', function () {
      before(addOppija.enterValidDataPäiväkodinEsiopetus())

      describe('Käyttöliittymän tila', function () {
        it('Lisää-nappi on enabloitu', function () {
          expect(addOppija.isEnabled()).to.equal(true)
        })

        it('Ei näytetä opintojen rahoitus -kenttää', function () {
          expect(addOppija.rahoitusIsVisible()).to.equal(false)
        })
      })

      describe('Kun painetaan Lisää-nappia', function () {
        before(
          addOppija.submitAndExpectSuccess(
            'Tyhjä, Tero (230872-7258)',
            'Päiväkodin esiopetus'
          )
        )

        it('lisätty oppija näytetään', function () {
          expect(opinnot.getTutkinto()).to.equal('Päiväkodin esiopetus')
          expect(opinnot.getOppilaitos()).to.equal('PK Vironniemi')
          expect(
            editor.propertyBySelector('.diaarinumero').getValue()
          ).to.equal('102/011/2014')
        })

        it('koulutusmoduulia ja järjestämismuotoa ei näytetä', function () {
          expect(editor.property('koulutustoimija').isVisible()).to.equal(false)
          expect(editor.property('järjestämismuoto').isVisible()).to.equal(
            false
          )
        })

        describe('Editointitilassa', function () {
          before(editor.edit)

          it('koulutusmoduulia ja järjestämismuotoa ei näytetä', function () {
            expect(editor.property('koulutustoimija').isVisible()).to.equal(
              false
            )
            expect(editor.property('järjestämismuoto').isVisible()).to.equal(
              false
            )
          })

          after(editor.cancelChanges)
        })
      })
    })

    describe('Lisääminen organisaatiohierarkian ulkopuolelle', function () {
      before(prepareForNewOppija('hki-tallentaja', '230872-7258'))

      describe('Kun kyseessä on koulutustoimija joka on myös varhaiskasvatuksen järjestäjä', function () {
        it('Varhaiskasvatus organisaatiohierarkian ulkopuolelle valitsin näytetään', function () {
          expect(
            isElementVisible(
              findSingle(varhaiskasvatusOrganisaationUlkopuolellaCbSelector)
            )
          ).to.equal(true)
        })
      })

      describe('Kun valitaan oman organisaatiohierarkian ulkopuolelle tallentaminen', function () {
        before(
          addOppija.selectVarhaiskasvatusOrganisaationUlkopuolelta(true),
          addOppija.enterOppilaitos('kou')
        )

        it('vain oman organisaation ulkopuolisia varhaiskasvatustoimipisteitä näytetään', function () {
          expect(addOppija.toimipisteet()).to.deep.equal([
            'Helsingin yliopiston Viikin normaalikoulu',
            'Helsingin eurooppalainen koulu',
            'Joensuun normaalikoulu (lakkautettu)',
            'Rantakylän normaalikoulu',
            'Savonlinnan normaalikoulu (lakkautettu)',
            'Tulliportin normaalikoulu',
            'Jyväskylän normaalikoulu',
            'Helsingin Saksalainen koulu',
            'Hirvikosken koulu',
            'Huutjärven koulu',
            'Purolan koulu (lakkautettu)',
            'Suur-Ahvenkosken koulu',
            'Hämeenlinnan normaalikoulu (lakkautettu)',
            'Aapajoen koulu (lakkautettu)',
            'Aapajärven koulu (lakkautettu)',
            'Arpelan koulu',
            'HANNULAN KOULUN ESKARI',
            'Hannulan koulu',
            'Kaakamon koulu',
            'Kantojärven koulu (lakkautettu)',
            'Karungin koulu',
            'Kivirannan koulu',
            'Kokkokankaan koulu',
            'Kukkolan koulu (lakkautettu)',
            'Kyläjoen koulu',
            'Liakan koulu (lakkautettu)',
            'Mustarannan koulu (lakkautettu)',
            'Nikunmäen koulu (lakkautettu)',
            'Näätsaaren koulu',
            'Pirkkiön koulu',
            'Putaan koulu',
            'Raumon koulu',
            'Sattajärven koulu (lakkautettu)',
            'Suensaaren koulu (lakkautettu)',
            'Tornion Seminaarin koulu',
            'Tornionseudun koulu (lakkautettu)',
            'Vojakkalan koulu (lakkautettu)',
            'Yliliakan koulu (lakkautettu)',
            'Yliraumon koulu (lakkautettu)'
          ])
        })

        describe('Kun syötetään loput datat', function () {
          before(
            addOppija.selectJärjestämismuoto(
              'Ostopalvelu, kunnan tai kuntayhtymän järjestämä'
            ),
            addOppija.enterValidDataPäiväkodinEsiopetus({
              oppilaitos: 'Päiväkoti Touhula'
            })
          )

          it('lisää nappi enabloituu', function () {
            expect(addOppija.isEnabled()).to.equal(true)
          })

          describe('Kun painetaan Lisää-nappia', function () {
            before(
              addOppija.submitAndExpectSuccess(
                'Tyhjä, Tero (230872-7258)',
                'Päiväkodin esiopetus'
              )
            )

            it('lisätty oppija näytetään', function () {
              expect(opinnot.getTutkinto()).to.equal('Päiväkodin esiopetus')
              expect(opinnot.getOppilaitos()).to.equal('Päiväkoti Touhula')
              expect(
                editor.propertyBySelector('.diaarinumero').getValue()
              ).to.equal('102/011/2014')
              expect(extractAsText(S('.tunniste-koodiarvo'))).to.equal('001102')
            })

            it('koulutusmoduuli ja järjestämismuoto näytetään', function () {
              expect(editor.property('koulutustoimija').getValue()).to.equal(
                'Helsingin kaupunki'
              )
              expect(editor.property('järjestämismuoto').getValue()).to.equal(
                'Ostopalvelu, kunnan tai kuntayhtymän järjestämä'
              )
            })

            describe('Kun vahvistetaan suoritus', function () {
              before(
                editor.edit,
                opinnot.tilaJaVahvistus.merkitseValmiiksi,
                opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                  .itemEditor(0)
                  .setValue('Lisää henkilö'),
                opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                  .itemEditor(0)
                  .propertyBySelector('.nimi')
                  .setValue('Pekka Päiväkoti'),
                opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                  .itemEditor(0)
                  .propertyBySelector('.titteli')
                  .setValue('päiväkodin johtaja'),
                opinnot.tilaJaVahvistus.merkitseValmiiksiDialog
                  .merkitseValmiiksi,
                editor.saveChanges
              )

              it('toimii', function () {})

              describe('Toinen koulutustoimija', function () {
                before(
                  prepareForNewOppija('tornio-tallentaja', '230872-7258'),
                  addOppija.selectVarhaiskasvatusOrganisaationUlkopuolelta(
                    true
                  ),
                  addOppija.selectJärjestämismuoto(
                    'Ostopalvelu, kunnan tai kuntayhtymän järjestämä'
                  ),
                  addOppija.enterValidDataPäiväkodinEsiopetus({
                    oppilaitos: 'Päiväkoti Touhula'
                  }),
                  addOppija.submitAndExpectSuccess(
                    'Tyhjä, Tero (230872-7258)',
                    'Päiväkodin esiopetus'
                  ),
                  editor.edit,
                  opinnot.tilaJaVahvistus.merkitseValmiiksi
                )

                it('ei näe toisten koulutustoimijoiden myöntäjiä', function () {
                  expect(
                    opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                      .itemEditor(0)
                      .getOptions()
                  ).to.deep.equal(['Lisää henkilö'])
                })
                after(editor.cancelChanges)
              })

              describe('Päiväkodin virkailija', function () {
                before(
                  Authentication().login('touhola-tallentaja'),
                  page.openPage,
                  page.oppijaHaku.searchAndSelect('230872-7258')
                )

                it('ei voi muokata tai mitätöidä toisten luomaa opiskeluoikeutta', function () {
                  expect(editor.canSave()).to.equal(false)
                  expect(opinnot.invalidateOpiskeluoikeusIsShown()).to.equal(
                    false
                  )
                })
              })
            })
          })
        })
      })
    })

    describe('Lisääminen organisaatiohierarkian ulkopuolelle peruskoulun esiopetukselle', function () {
      before(prepareForNewOppija('hki-tallentaja', '230872-7258'))

      describe('Kun kyseessä on koulutustoimija joka on myös varhaiskasvatuksen järjestäjä', function () {
        it('Varhaiskasvatus organisaatiohierarkian ulkopuolelle valitsin näytetään', function () {
          expect(
            isElementVisible(
              findSingle(varhaiskasvatusOrganisaationUlkopuolellaCbSelector)
            )
          ).to.equal(true)
        })
      })

      describe('Kun valitaan oman organisaatiohierarkian ulkopuolelle tallentaminen', function () {
        before(
          addOppija.selectVarhaiskasvatusOrganisaationUlkopuolelta(true),
          addOppija.enterOppilaitos('')
        )

        describe('Kun syötetään loput datat peruskoulun esiopetukselle', function () {
          before(
            addOppija.selectJärjestämismuoto(
              'Ostopalvelu, kunnan tai kuntayhtymän järjestämä'
            ),
            addOppija.enterValidDataEsiopetus({
              oppilaitos: 'Hirvikosken koulu'
            })
          )

          it('lisää nappi enabloituu', function () {
            expect(addOppija.isEnabled()).to.equal(true)
          })

          describe('Kun painetaan Lisää-nappia', function () {
            before(
              addOppija.submitAndExpectSuccess(
                'Tyhjä, Tero (230872-7258)',
                'Peruskoulun esiopetus'
              )
            )

            it('lisätty oppija näytetään', function () {
              expect(opinnot.getTutkinto()).to.equal('Peruskoulun esiopetus')
              expect(opinnot.getOppilaitos()).to.equal('Hirvikosken koulu')
              expect(
                editor.propertyBySelector('.diaarinumero').getValue()
              ).to.equal('102/011/2014')
              expect(extractAsText(S('.tunniste-koodiarvo'))).to.equal('001101')
            })

            it('koulutusmoduuli ja järjestämismuoto näytetään', function () {
              expect(editor.property('koulutustoimija').getValue()).to.equal(
                'Helsingin kaupunki'
              )
              expect(editor.property('järjestämismuoto').getValue()).to.equal(
                'Ostopalvelu, kunnan tai kuntayhtymän järjestämä'
              )
            })
          })
        })
      })
    })
  })

  describe('Tietojen muuttaminen', function () {
    var indexEditor = opinnot.opiskeluoikeusEditor(1)

    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('300996-870E')
    )

    describe('Kurssin kuvauksen ja sanallisen arvion muuttaminen', function () {
      var kuvaus = indexEditor
        .subEditor('.osasuoritukset tbody')
        .propertyBySelector('.kuvaus')

      before(
        click('.toggle-edit'),
        opinnot.expandAll,
        kuvaus.setValue('Uusi kuvaus'),
        indexEditor.saveChanges,
        opinnot.expandAll
      )
      it('Toimii', function () {
        expect(kuvaus.getValue()).to.equal('Uusi kuvaus')
      })
    })

    describe('Päätason suorituksen poistaminen', function () {
      before(indexEditor.edit)

      describe('Mitätöintilinkki', function () {
        it('Ei näytetä', function () {
          expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(false)
        })
      })
    })
  })

  describe('Aloituspäivä', function () {
    var indexEditor = opinnot.opiskeluoikeusEditor(1)

    before(page.openPage, page.oppijaHaku.searchAndSelect('300996-870E'))

    describe('Opiskeluoikeuden aloituspäivän muuttaminen', function () {
      before(
        indexEditor.edit,
        opinnot.expandAll,
        Page(function () {
          return S('#content')
        }).setInputValue(
          '.tila .opiskeluoikeusjakso:last-child .date input',
          '1.1.2005'
        )
      )
      it('Toimii', function () {
        expect(
          S('.tila .opiskeluoikeusjakso:last-child .date input').val()
        ).to.equal('1.1.2005')
        expect(
          S(
            'li:nth-child(2) .opiskeluoikeuden-voimassaoloaika .alkamispäivä .date'
          ).html()
        ).to.equal('1.1.2005') // li:nth-child(2)
      })
    })
  })
})
