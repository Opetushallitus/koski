describe('Perusopetus 2', function () {
  var page = KoskiPage()
  var login = LoginPage()
  var opinnot = OpinnotPage()
  var tilaJaVahvistus = opinnot.tilaJaVahvistus
  var addOppija = AddOppijaPage()
  var opiskeluoikeus = OpiskeluoikeusDialog()
  var editor = opinnot.opiskeluoikeusEditor()
  var currentMoment = moment()

  function currentDatePlusYears(years) {
    return currentMoment.clone().add(years, 'years').format('D.M.YYYY')
  }

  var currentDateStr = currentDatePlusYears(0)
  var date2017Str = '1.1.2017'
  var date2018Str = '1.1.2018'
  var date2019Str = '1.1.2019'

  before(Authentication().login(), resetFixtures)

  describe('Tietojen muuttaminen', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('220109-784L'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus')
    )

    describe('Opiskeluoikeuden tiedot', function () {
      var opintojenTilaJaVahvistus = opinnot.tilaJaVahvistus

      it('Alkutila', function () {
        expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal(
          '4.6.2016'
        )
        expect(
          opinnot
            .opiskeluoikeusEditor()
            .subEditor('.property.tila')
            .propertyBySelector('label.tila:contains("Valmistunut")')
            .isVisible()
        ).to.equal(true)
      })

      describe('Opiskeluoikeuden tila', function () {
        before(
          editor.edit,
          editor.property('tila').removeItem(0),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )

        describe('Perusopetuksen tilat', function () {
          before(editor.edit, opinnot.avaaLisaysDialogi)
          it('Ei sisällä loma-tilaa', function () {
            expect(opiskeluoikeus.tilat()).to.deep.equal([
              'koskiopiskeluoikeudentila_eronnut',
              'koskiopiskeluoikeudentila_katsotaaneronneeksi',
              'koskiopiskeluoikeudentila_lasna',
              'koskiopiskeluoikeudentila_peruutettu',
              'koskiopiskeluoikeudentila_valmistunut',
              'koskiopiskeluoikeudentila_valiaikaisestikeskeytynyt'
            ])
          })
        })

        describe('Eronnut', function () {
          it('Alkutila', function () {
            expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal('')
          })

          describe('Kun lisätään', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('eronnut'),
              opintojenTilaJaVahvistus.merkitseKeskeneräiseksi,
              opiskeluoikeus.tallenna,
              editor.saveChanges
            )

            it('Opiskeluoikeuden päättymispäivä asetetaan', function () {
              expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal(
                currentDateStr
              )
            })

            it('Opiskeluoikeuden tilaa ei voi lisätä kun opiskeluoikeus on päättynyt', function () {
              expect(
                isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))
              ).to.equal(false)
            })
          })

          describe('Kun poistetaan', function () {
            before(
              editor.edit,
              editor.property('tila').removeItem(0),
              editor.saveChanges
            )

            it('Opiskeluoikeuden päättymispäivä poistetaan', function () {
              expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal(
                ''
              )
            })

            describe('editoitaessa', function () {
              before(editor.edit)
              it('Opiskeluoikeuden tilan voi lisätä', function () {
                expect(
                  isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))
                ).to.equal(true)
              })
              after(editor.cancelChanges)
            })
          })
        })

        describe('Valmistunut', function () {
          it('Alkutila', function () {
            expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal('')
          })

          describe('Kun lisätään', function () {
            before(
              editor.edit,
              opintojenTilaJaVahvistus.merkitseValmiiksi,
              opintojenTilaJaVahvistus.lisääVahvistus('1.1.2021'),
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('valmistunut'),
              opiskeluoikeus.tallenna,
              editor.saveChanges
            )

            it('Opiskeluoikeuden päättymispäivä asetetaan', function () {
              expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal(
                currentDateStr
              )
            })

            describe('editoitaessa', function () {
              before(editor.edit)
              it('Opiskeluoikeuden tilaa ei voi lisätä kun opiskeluoikeus on päättynyt', function () {
                expect(
                  isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))
                ).to.equal(false)
              })
              after(editor.cancelChanges)
            })
          })

          describe('Kun poistetaan', function () {
            before(
              editor.edit,
              editor.property('tila').removeItem(0),
              editor.saveChanges
            )

            it('Opiskeluoikeuden päättymispäivä poistetaan', function () {
              expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal(
                ''
              )
            })

            describe('editoitaessa', function () {
              before(editor.edit)
              it('Opiskeluoikeuden tilan voi lisätä', function () {
                expect(
                  isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))
                ).to.equal(true)
              })
              after(editor.cancelChanges)
            })
          })

          describe('Kun suoritus on kesken', function () {
            before(
              page.openPage,
              page.oppijaHaku.searchAndSelect('160932-311V'),
              opinnot.valitseSuoritus(undefined, 'Päättötodistus'),
              opinnot.opiskeluoikeusEditor().edit,
              opinnot.avaaLisaysDialogi
            )

            it('Valmistunut-tilaa ei voi lisätä', function () {
              expect(
                OpiskeluoikeusDialog().radioEnabled('valmistunut')
              ).to.equal(false)
            })

            describe('Kun suorituksella on vahvistus tulevaisuudessa', function () {
              var opintojenTilaJaVahvistus2 = opinnot.tilaJaVahvistus
              before(
                opiskeluoikeus.peruuta,
                opinnot.oppiaineet.merkitseOppiaineetValmiiksi(),
                opintojenTilaJaVahvistus2.merkitseValmiiksi,
                opintojenTilaJaVahvistus2.lisääVahvistus('11.4.2117'),
                opinnot.avaaLisaysDialogi
              )

              it('Valmistunut-tilan voi lisätä', function () {
                expect(
                  OpiskeluoikeusDialog().radioEnabled('valmistunut')
                ).to.equal(true)
              })
            })

            after(opiskeluoikeus.peruuta, editor.cancelChanges)
          })
        })

        describe('Peruutettu', function () {
          before(
            page.oppijaHaku.searchAndSelect('220109-784L'),
            opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
              'perusopetus'
            ),
            editor.edit
          )
          it('Alkutila', function () {
            expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal('')
          })

          describe('Kun lisätään', function () {
            before(
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('peruutettu'),
              opiskeluoikeus.tallenna,
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )

            it('Opiskeluoikeuden päättymispäivä asetetaan', function () {
              expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal(
                currentDateStr
              )
            })

            it('Opiskeluoikeuden tilaa ei voi lisätä kun opiskeluoikeus on päättynyt', function () {
              expect(
                isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))
              ).to.equal(false)
            })
          })

          describe('Kun poistetaan', function () {
            before(
              editor.edit,
              editor.property('tila').removeItem(0),
              editor.saveChanges
            )

            it('Opiskeluoikeuden päättymispäivä poistetaan', function () {
              expect(
                opinnot
                  .opiskeluoikeusEditor()
                  .property('päättymispäivä')
                  .isVisible()
              ).to.equal(false)
            })
          })
        })

        describe('Läsnä', function () {
          it('Alkutila', function () {
            expect(
              opinnot
                .opiskeluoikeusEditor()
                .property('päättymispäivä')
                .isVisible()
            ).to.equal(false)
          })
          describe('Kun lisätään', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.alkuPaiva().setValue('26.12.2018'),
              opiskeluoikeus.tila().aseta('lasna'),
              opiskeluoikeus.tallenna,
              editor.saveChanges
            )

            it('Opiskeluoikeuden päättymispäivää ei aseteta', function () {
              expect(
                opinnot
                  .opiskeluoikeusEditor()
                  .property('päättymispäivä')
                  .isVisible()
              ).to.equal(false)
            })

            describe('editoitaessa', function () {
              before(editor.edit)
              it('Opiskeluoikeuden tilan voi lisätä', function () {
                expect(
                  isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))
                ).to.equal(true)
              })
              after(editor.cancelChanges)
            })
          })
        })

        describe('Väliaikaisesti keskeytynyt', function () {
          it('Alkutila', function () {
            expect(
              opinnot
                .opiskeluoikeusEditor()
                .property('päättymispäivä')
                .isVisible()
            ).to.equal(false)
          })
          describe('Kun lisätään', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('valiaikaisestikeskeytynyt'),
              opiskeluoikeus.tallenna,
              editor.saveChanges
            )

            it('Opiskeluoikeuden päättymispäivää ei aseteta', function () {
              expect(
                opinnot
                  .opiskeluoikeusEditor()
                  .property('päättymispäivä')
                  .isVisible()
              ).to.equal(false)
            })
          })
        })

        describe('Päivämäärän validointi', function () {
          before(
            editor.edit,
            editor.property('tila').removeItem(0),
            opinnot.avaaLisaysDialogi
          )
          describe('Virheellinen päivämäärä', function () {
            before(
              opiskeluoikeus.tila().aseta('eronnut'),
              opiskeluoikeus.alkuPaiva().setValue('11.1.200')
            )
            it('Tallennus on estetty', function () {
              expect(opiskeluoikeus.isEnabled()).to.equal(false)
            })
          })

          describe('Virheellinen päivämäärä ja tilan muutos', function () {
            before(
              opiskeluoikeus.tila().aseta('eronnut'),
              opiskeluoikeus.alkuPaiva().setValue('11.1.200'),
              opiskeluoikeus.tila().aseta('valmistunut')
            )
            it('Tallennus on estetty', function () {
              expect(opiskeluoikeus.isEnabled()).to.equal(false)
            })
          })

          describe('Uusi alkupäivä on aikaisempi kuin viimeisen tilan alkupäivämäärä', function () {
            before(
              opiskeluoikeus.tila().aseta('eronnut'),
              opiskeluoikeus.alkuPaiva().setValue('14.8.2008')
            )
            it('Tallennus on estetty', function () {
              expect(opiskeluoikeus.isEnabled()).to.equal(false)
            })
          })

          describe('Uusi alkupäivä on sama kuin viimeisen tilan alkupäivämäärä', function () {
            before(
              opiskeluoikeus.tila().aseta('eronnut'),
              opiskeluoikeus.alkuPaiva().setValue('15.8.2008')
            )
            it('Tallennus on estetty', function () {
              expect(opiskeluoikeus.isEnabled()).to.equal(false)
            })
          })

          describe('Virheellinen päivämäärä korjattu oikeelliseksi', function () {
            before(
              opiskeluoikeus.tila().aseta('eronnut'),
              opiskeluoikeus.alkuPaiva().setValue('11.1.200'),
              opiskeluoikeus.alkuPaiva().setValue(currentDateStr)
            )
            it('Tallennus on sallittu', function () {
              expect(opiskeluoikeus.isEnabled()).to.equal(true)
            })
          })
        })

        describe('Aktiivinen tila', function () {
          var tila = opinnot.opiskeluoikeusEditor().property('tila')
          it('Viimeinen tila kun päivämäärä ei ole tulevaisuudessa', function () {
            expect(
              findSingle(
                '.opiskeluoikeusjakso',
                tila.getItems()[0].elem
              )().hasClass('active')
            ).to.equal(true)
          })

          describe('Kun lisätään useita tähän päivään', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.alkuPaiva().setValue(date2017Str),
              opiskeluoikeus.tila().aseta('valiaikaisestikeskeytynyt'),
              opiskeluoikeus.tallenna,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('lasna'),
              opiskeluoikeus.tallenna,
              editor.saveChanges
            )

            it('Viimeinen tila on aktiivinen', function () {
              var jaksoElems = tila.elem().find('.opiskeluoikeusjakso')
              toArray(jaksoElems)
                .slice(1)
                .forEach(function (e) {
                  expect(S(e).hasClass('active')).to.equal(false)
                })
              expect(S(jaksoElems[0]).hasClass('active')).to.equal(true)
            })
          })

          describe('Kun lisätään tulevaisuuteen "väliaikaisesti keskeytynyt"', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('valiaikaisestikeskeytynyt'),
              opiskeluoikeus.alkuPaiva().setValue('9.5.2117'),
              opiskeluoikeus.tallenna,
              editor.saveChanges
            )

            describe('Käyttöliittymän tila', function () {
              it('Nykyinen tila "läsnä" on aktiivinen', function () {
                expect(
                  findSingle(
                    '.opiskeluoikeusjakso',
                    tila.getItems()[1].elem
                  )().hasClass('active')
                ).to.equal(true)
              })
            })

            describe('Oppijataulukossa', function () {
              before(
                syncPerustiedot,
                opinnot.backToList,
                page.oppijataulukko.filterBy('nimi', 'Koululainen Kaisa'),
                page.oppijataulukko.filterBy('tyyppi', 'Perusopetus')
              )

              it('Näytetään nykyinen tila "läsnä" (flaky)', function () {
                expect(page.oppijataulukko.isVisible()).to.equal(true)
                expect(
                  page.oppijataulukko.findOppija(
                    'Koululainen, Kaisa',
                    'Perusopetus'
                  )
                ).to.deep.equal([
                  'Koululainen, Kaisa',
                  'Perusopetus',
                  'Perusopetuksen oppimäärä',
                  'Perusopetus',
                  'Läsnä',
                  'Jyväskylän normaalikoulu',
                  '15.8.2008',
                  '',
                  '9C'
                ])
              })
            })
          })
        })
      })

      describe('Opiskeluoikeuden lisätiedot', function () {
        before(
          page.oppijaHaku.selectOppija('220109-784L'),
          opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus')
        )
        before(
          editor.edit,
          opinnot.expandAll,
          editor.property('vuosiluokkiinSitoutumatonOpetus').setValue(true),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )
        describe('Lisätietojen lisäys', function () {
          it('Toimii', function () {
            expect(
              editor.property('vuosiluokkiinSitoutumatonOpetus').getValue()
            ).to.equal('kyllä')
          })
        })

        describe('Kun lisätiedot piilotetaan ja näytetään uudestaan', function () {
          before(opinnot.collapseAll, opinnot.expandAll)
          it('Toimii', function () {
            expect(
              editor.property('vuosiluokkiinSitoutumatonOpetus').getValue()
            ).to.equal('kyllä')
          })
        })

        describe('Kun lisätiedot piilotetaan, siirrytään muokkaukseen, avataan lisätiedot, poistutaan muokkauksesta', function () {
          before(
            opinnot.collapseAll,
            editor.edit,
            opinnot.expandAll,
            editor.cancelChanges
          )
          it('Toimii', function () {
            expect(
              editor.property('vuosiluokkiinSitoutumatonOpetus').getValue()
            ).to.equal('kyllä')
          })
          after(
            editor.edit,
            opinnot.expandAll,
            editor.property('vuosiluokkiinSitoutumatonOpetus').setValue(false),
            editor.saveChanges
          )
        })

        // Disabloitu, koska tämä hajoaa uusien validointien myötä, ja koska tämä testaa lähinnä vain vanhan kälin itemeiden lisäystä ja poistoa.
        describe.skip('Erityisen tuen päätös', function () {
          describe('lisätään, kun valinnainen malli lisätään', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              editor.property('erityisenTuenPäätökset').addItem,
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )
            it('Toimii', function () {
              expect(
                isElementVisible(S('.property.erityisenTuenPäätökset'))
              ).to.equal(true)
              expect(
                editor.property('opiskeleeToimintaAlueittain').getValue()
              ).to.equal('ei')
            })
          })
          describe('päivittyy, kun erityisen tuen tietoja muutetaan', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              editor.property('opiskeleeToimintaAlueittain').setValue(true),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )
            it('Toimii', function () {
              expect(
                editor.property('opiskeleeToimintaAlueittain').getValue()
              ).to.equal('kyllä')
            })
          })
          describe('poistetaan, kun valinnainen malli poistetaan', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              editor.property('erityisenTuenPäätökset').removeItem(0),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )
            it('Toimii', function () {
              expect(
                isElementVisible(S('.property.erityisenTuenPäätökset'))
              ).to.equal(false)
            })
          })
        })

        describe('Päivämäärän syöttö', function () {
          var joustavaPerusopetusProperty = editor.property(
            'joustavaPerusopetus'
          )
          var joustavaPerusopetus =
            joustavaPerusopetusProperty.toPäivämääräväli()

          before(
            editor.edit,
            opinnot.expandAll,
            joustavaPerusopetusProperty.addValue
          )

          describe('Virheellinen päivämäärä', function () {
            before(joustavaPerusopetus.setAlku('34.9.2000'))
            it('Estää tallennuksen', function () {
              expect(opinnot.onTallennettavissa()).to.equal(false)
            })
          })
          describe('Oikeellinen päivämäärä', function () {
            before(
              joustavaPerusopetus.setAlku(currentDateStr),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )
            it('Tallennus onnistuu', function () {
              expect(joustavaPerusopetus.getAlku()).to.equal(currentDateStr)
            })
          })

          after(
            editor.edit,
            joustavaPerusopetusProperty.removeValue,
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )
        })

        describe('Virheellinen päivämääräväli', function () {
          var joustavaPerusopetusProperty = editor.property(
            'joustavaPerusopetus'
          )
          var joustavaPerusopetus =
            joustavaPerusopetusProperty.toPäivämääräväli()

          before(
            editor.edit,
            opinnot.expandAll,
            joustavaPerusopetusProperty.addValue,
            joustavaPerusopetus.setAlku(currentDateStr),
            joustavaPerusopetus.setLoppu('1.2.2008')
          )

          it('Estää tallennuksen', function () {
            expect(joustavaPerusopetus.isValid()).to.equal(false)
            expect(opinnot.onTallennettavissa()).to.equal(false)
          })
          after(
            joustavaPerusopetus.setLoppu(currentDateStr),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )
        })

        // Ks. https://github.com/Opetushallitus/koski/pull/876 : väliaikaisesti disabloitu
        describe.skip('Tukimuodot', function () {
          describe('Lisättäessä ensimmäinen', function () {
            before(
              editor.edit,
              editor.propertyBySelector('.erityisenTuenPäätökset').addItem,
              editor
                .propertyBySelector(
                  '.erityisenTuenPäätökset .tukimuodot .add-item'
                )
                .setValue('Erityiset apuvälineet'),
              editor.saveChanges
            )
            it('Toimii', function () {
              expect(editor.property('tukimuodot').getValue()).to.equal(
                'Erityiset apuvälineet'
              )
            })

            describe('Lisättäessä toinen', function () {
              before(
                editor.edit,
                editor
                  .propertyBySelector('.tukimuodot .add-item')
                  .setValue('Tukiopetus'),
                editor.saveChanges
              )
              it('Toimii', function () {
                expect(editor.property('tukimuodot').getValue()).to.equal(
                  'Erityiset apuvälineetTukiopetus'
                )
              })

              describe('Poistettaessa ensimmäinen', function () {
                before(
                  editor.edit,
                  editor.property('tukimuodot').removeItem(0),
                  editor.saveChanges
                )
                it('Toimii', function () {
                  expect(editor.property('tukimuodot').getValue()).to.equal(
                    'Tukiopetus'
                  )
                })

                describe('Poistettaessa viimeinen', function () {
                  before(
                    editor.edit,
                    editor.property('tukimuodot').removeItem(0),
                    editor.saveChanges
                  )
                  it('Toimii', function () {
                    expect(editor.property('tukimuodot').isVisible()).to.equal(
                      false
                    )
                  })
                })
              })
            })
          })
        })
      })
    })

    describe('Suoritusten tiedot', function () {
      describe('Luokan muuttaminen', function () {
        before(
          opinnot.valitseSuoritus(undefined, '9. vuosiluokka'),
          editor.edit
        )
        describe('Tyhjäksi', function () {
          before(editor.property('luokka').setValue(''))
          it('Näyttää validaatiovirheen', function () {
            expect(editor.property('luokka').isValid()).to.equal(false)
          })
          it('Tallennus on estetty', function () {
            expect(editor.canSave()).to.equal(false)
          })
          it('Näytetään virheviesti myös tallennuspalkissa', function () {
            expect(editor.getEditBarMessage()).to.equal(
              'Korjaa virheelliset tiedot.'
            )
          })
        })
        describe('Ei tyhjäksi', function () {
          before(editor.property('luokka').setValue('9C'))
          it('Poistaa validaatiovirheen', function () {
            expect(editor.property('luokka').isValid()).to.equal(true)
          })
          it('Tallennus on sallittu', function () {
            expect(editor.canSave()).to.equal(true)
          })
          it('Poistetaan virheviesti myös tallennuspalkista', function () {
            expect(editor.getEditBarMessage()).to.equal(
              'Tallentamattomia muutoksia'
            )
          })

          describe('Tallennus', function () {
            before(editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('Onnistuu', function () { })
          })
        })
      })
      describe('Tutkinnon perusteen diaarinumero', function () {
        var diaarinumero = editor.propertyBySelector('.diaarinumero')
        before(
          editor.edit,
          diaarinumero.setValue(
            '1/011/2004 Perusopetuksen opetussuunnitelman perusteet 2004'
          ),
          editor.saveChanges
        )
        it('toimii', function () {
          expect(diaarinumero.getValue()).to.equal('1/011/2004')
        })
      })
      describe('Suorituskielen lisäys', function () {
        before(
          opinnot.valitseSuoritus(undefined, 'Päättötodistus'),
          editor.edit
        )
        describe('Lisättäessä', function () {
          it('Näytetään oletus', function () {
            expect(editor.property('suorituskieli').getValue()).to.equal(
              'suomi'
            )
          })
        })
        describe('Lisäyksen jälkeen', function () {
          before(
            editor.property('suorituskieli').selectValue('suomi'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )
          it('muutettu suorituskieli näytetään', function () {
            expect(editor.property('suorituskieli').getValue()).to.equal(
              'suomi'
            )
          })
        })
      })
      describe('Todistuksella näkyvät lisätiedot', function () {
        describe('lisäys', function () {
          var lisätiedot = editor.property('todistuksellaNäkyvätLisätiedot')
          before(
            opinnot.valitseSuoritus(undefined, 'Päättötodistus'),
            editor.edit,
            lisätiedot.setValue('Testitesti'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )
          it('Uudet lisätiedot näytetään', function () {
            expect(lisätiedot.getValue()).to.equal('Testitesti')
          })
        })
        describe('poisto', function () {
          var lisätiedot = editor.property('todistuksellaNäkyvätLisätiedot')
          before(
            opinnot.valitseSuoritus(undefined, 'Päättötodistus'),
            editor.edit,
            lisätiedot.setValue('Testitesti2'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown),
            editor.edit,
            lisätiedot.setValue(''),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )
          it('Lisätiedot piilotetaan', function () {
            expect(lisätiedot.isVisible()).to.equal(false)
          })
        })
        describe('lisäys ja poisto kerralla', function () {
          var lisätiedot = editor.property('todistuksellaNäkyvätLisätiedot')
          before(
            opinnot.valitseSuoritus(undefined, 'Päättötodistus'),
            editor.edit,
            lisätiedot.setValue('Testitesti'),
            wait.forAjax,
            lisätiedot.setValue(''),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )
          it('Lisätiedot piilotetaan', function () {
            expect(lisätiedot.isVisible()).to.equal(false)
          })
        })
      })
      describe('Vieraan kielen valinta', function () {
        describe('kielivalinnan muuttaminen', function () {
          var b1kieli = opinnot.oppiaineet.oppiaine('valinnainen.B1')
          var kieli = b1kieli.propertyBySelector('.oppiaine')
          before(
            editor.edit,
            editor.property('laajuus').setValue('1'),
            kieli.selectValue('saksa'),
            editor.saveChanges
          )
          it('muutettu kielivalinta näytetään', function () {
            expect(kieli.getValue()).to.equal('saksa')
          })
        })
        describe('kielien järjestys listassa', function () {
          before(editor.edit)
          it('on oikein', function () {
            expect(
              textsOf(S('.oppiaine.B1.pakollinen .oppiaine .options li'))
            ).to.deep.equal([
              'suomi',
              'ruotsi',
              'englanti',
              'Dari',
              'Ei suoritusta',
              'afrikaans',
              'albania',
              'amhara',
              'arabia',
              'armenia',
              'azeri',
              'bengali',
              'bosnia',
              'bulgaria',
              'espanja',
              'eteläsotho',
              'filipino',
              'georgia',
              'heprea',
              'hindi',
              'hollanti',
              'igbo',
              'inarinsaame',
              'indonesia',
              'islanti',
              'italia',
              'japani',
              'joruba',
              'kannada',
              'kantoninkiina',
              'katalaani',
              'keskikhmer',
              'kiina',
              'koltansaame',
              'korea',
              'kreikka',
              'kroatia',
              'kurdi',
              'latina',
              'latvia, lätti',
              'liettua',
              'makedonia',
              'malaiji',
              'malajalam',
              'mandariinikiina',
              'mongoli',
              'muu kieli',
              'muu kieli',
              'nepali',
              'norja',
              'paštu, myös: afgaani',
              'persia, farsi',
              'pohjoissaame',
              'portugali',
              'puola',
              'ranska',
              'romani',
              'romania',
              'ruanda',
              'ruotsi toisena kielenä',
              'ruotsi viittomakielisille',
              'saame, lappi',
              'saksa',
              'serbia',
              'serbokroatia',
              'sinhala',
              'slovakki',
              'sloveeni',
              'somali',
              'suahili',
              'suomi toisena kielenä',
              'suomi viittomakielisille',
              'swazi',
              'tagalog',
              'tamili',
              'tanska',
              'tataari',
              'telugu',
              'thai',
              'tigrinja',
              'turkki',
              'tšekki',
              'uiguuri',
              'ukraina',
              'unkari',
              'urdu',
              'venäjä',
              'vietnam',
              'viittomakieli',
              'viro, eesti'
            ])
          })
          after(editor.cancelChanges)
        })
      })

      describe('Uskonto', function () {
        describe('Virkailijalle jolla ei ole luottamuksellisten tietojen katseluoikeutta', function () {
          before(
            Authentication().logout,
            Authentication().login('jyvas-eiluottoa'),
            page.openPage,
            page.oppijaHaku.searchAndSelect('220109-784L'),
            opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
              'perusopetus'
            )
          )

          it('oppimäärää ei näytetä', function () {
            expect(
              opinnot.oppiaineet.oppiaine('pakollinen.KT').expandable()
            ).to.equal(false)
          })

          after(
            resetFixtures,
            Authentication().logout,
            Authentication().login(),
            page.openPage,
            page.oppijaHaku.searchAndSelect('220109-784L'),
            opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
              'perusopetus'
            )
          )
        })
      })

      describe('Oppiaineen laajuuden muutos', function () {
        var valinnainenLiikunta = opinnot.oppiaineet.oppiaine('valinnainen.LI')
        before(
          editor.edit,
          valinnainenLiikunta.property('laajuus').setValue('1.5'),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )
        it('muutettu laajuus näytetään', function () {
          expect(valinnainenLiikunta.property('laajuus').getValue()).to.equal(
            '1,5'
          )
        })
      })
      describe('Oppiaineen arvosanan muutos', function () {
        var äidinkieli = opinnot.oppiaineet.oppiaine(0)
        var arvosana = äidinkieli.propertyBySelector('.arvosana')
        before(opinnot.valitseSuoritus(undefined, '7. vuosiluokka'))

        describe('Kun annetaan numeerinen arvosana', function () {
          before(editor.edit, arvosana.selectValue('5'), editor.saveChanges)

          it('muutettu arvosana näytetään', function () {
            expect(arvosana.getValue()).to.equal('5')
          })
        })

        describe('Kun annetaan arvosana S', function () {
          before(editor.edit, arvosana.selectValue('S'), opinnot.expandAll)

          describe('Sanallinen arviointi', function () {
            var sanallinenArviointi = äidinkieli.propertyBySelector(
              '.sanallinen-arviointi .kuvaus'
            )
            before(
              editor.edit,
              sanallinenArviointi.setValue('Hienoa työtä'),
              editor.saveChanges,
              opinnot.expandAll
            )
            it('Voidaan syöttää ja näytetään', function () {
              expect(sanallinenArviointi.isVisible()).to.equal(true)
              expect(sanallinenArviointi.getValue()).to.equal('Hienoa työtä')
            })

            describe('Kun vaihdetaan numeeriseen arvosanaan', function () {
              before(editor.edit, arvosana.selectValue(8), opinnot.expandAll)

              it('Sanallinen arviointi piilotetaan', function () {
                expect(sanallinenArviointi.isVisible()).to.equal(false)
              })
            })
          })
        })

        describe('Kun poistetaan ensimmäinen oppiaine ja annetaan toiselle arvosana (bug fix)', function () {
          before(
            opinnot.collapseAll,
            editor.edit,
            äidinkieli.propertyBySelector('>tr:first-child').removeValue,
            arvosana.selectValue('9'),
            editor.saveChanges
          )
          it('Toimii', function () {
            expect(arvosana.getValue()).to.equal('9')
          })
        })
      })
      describe('Yksilöllistäminen', function () {
        before(
          resetFixtures,
          page.openPage,
          page.oppijaHaku.searchAndSelect('220109-784L'),
          opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus'),
          editor.edit,
          opinnot.expandAll,
          editor.property('yksilöllistettyOppimäärä').setValue(true),
          editor.saveChanges
        )
        it('toimii', function () {
          expect(opinnot.oppiaineet.oppiaine(0).text()).to.equal(
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9 *'
          )
        })
        after(
          editor.edit,
          opinnot.expandAll,
          editor.property('yksilöllistettyOppimäärä').setValue(false),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )
      })
      describe('Painotus', function () {
        before(
          editor.edit,
          opinnot.expandAll,
          editor.property('painotettuOpetus').setValue(true),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )
        it('toimii', function () {
          expect(opinnot.oppiaineet.oppiaine(0).text()).to.equal(
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9 **'
          )
        })
        after(
          editor.edit,
          opinnot.expandAll,
          editor.property('painotettuOpetus').setValue(false),
          editor.saveChanges
        )
      })
      describe('Käyttäytymisen arvioinnin lisäys', function () {
        var arvosana = editor.subEditor('.kayttaytyminen').property('arvosana')
        before(
          opinnot.valitseSuoritus(undefined, '7. vuosiluokka'),
          editor.edit,
          editor.propertyBySelector('.kayttaytyminen').addValue,
          editor.saveChanges
        )
        describe('Muuttamatta arviointia', function () {
          it('Näyttää oletusarvon S', function () {
            expect(arvosana.getValue()).to.equal('S')
          })
        })
        describe('Kun muutetaan arvosanaa', function () {
          before(editor.edit, arvosana.selectValue('10'), editor.saveChanges)
          it('Näyttää muutetun arvon', function () {
            expect(arvosana.getValue()).to.equal('10')
          })
        })
        describe('Kun lisätään sanallinen kuvaus', function () {
          var kuvaus = editor.subEditor('.kayttaytyminen').property('kuvaus')
          before(
            editor.edit,
            kuvaus.setValue('Hyvää käytöstä'),
            editor.saveChanges
          )
          it('Näyttää muutetun arvon', function () {
            expect(kuvaus.getValue()).to.equal('Hyvää käytöstä')
          })
        })
      })
      describe('Liitetiedot', function () {
        // Liitetiedot testataan isolla testisetillä, joilla varmistetaan ArrayEditorin toiminta
        var liitetiedot = editor.property('liitetiedot')
        describe('Liitetietojen lisäys', function () {
          before(
            opinnot.valitseSuoritus(undefined, '7. vuosiluokka'),
            editor.edit,
            liitetiedot.addItem,
            liitetiedot.property('kuvaus').setValue('TestiTesti')
          )
          it('Editoinnin aikana ei näytetä ylimääräistä poistonappia', function () {
            expect(liitetiedot.isRemoveValueVisible()).to.equal(false)
          })
          describe('Lisäyksen jälkeen', function () {
            before(editor.saveChanges)
            it('Näyttää uudet liitetiedot', function () {
              expect(liitetiedot.getText()).to.equal(
                'Liitetiedot Tunniste Käyttäytyminen\nKuvaus TestiTesti'
              )
            })
            describe('Toisen liitetiedon lisäys', function () {
              before(
                editor.edit,
                liitetiedot.addItem,
                liitetiedot.itemEditor(1).property('kuvaus').setValue('Testi2'),
                editor.saveChanges
              )
              it('Näyttää uudet liitetiedot', function () {
                expect(liitetiedot.getText()).to.equal(
                  'Liitetiedot Tunniste Käyttäytyminen\nKuvaus TestiTesti\nTunniste Käyttäytyminen\nKuvaus Testi2'
                )
              })
              describe('Ensimmäisen liitetiedon poisto', function () {
                before(
                  editor.edit,
                  liitetiedot.removeItem(0),
                  editor.saveChanges
                )
                it('Näyttää vain toisen lisätiedon', function () {
                  expect(liitetiedot.getText()).to.equal(
                    'Liitetiedot Tunniste Käyttäytyminen\nKuvaus Testi2'
                  )
                })
                describe('Lisäys ja poisto samalla kertaa', function () {
                  before(
                    editor.edit,
                    liitetiedot.addItem,
                    liitetiedot
                      .itemEditor(1)
                      .property('kuvaus')
                      .setValue('Testi3'),
                    liitetiedot.removeItem(0)
                  )
                  it('Välitulos: poisto toimii editoitaessa', function () {
                    expect(
                      liitetiedot.itemEditor(0).property('kuvaus').getValue()
                    ).to.equal('Testi3')
                  })
                  describe('Editoinnin jälkeen', function () {
                    before(editor.saveChanges)
                    it('Näyttää oikeellisesti vain toisen lisätiedon', function () {
                      expect(liitetiedot.getText()).to.equal(
                        'Liitetiedot Tunniste Käyttäytyminen\nKuvaus Testi3'
                      )
                    })

                    describe('Viimeisen alkion poisto', function () {
                      before(editor.edit, liitetiedot.removeItem(0))
                      it('Välitulos: poisto toimii editoitaessa', function () {
                        expect(liitetiedot.getItems().length).to.equal(0)
                      })
                      describe('poiston jälkeen', function () {
                        before(editor.saveChanges)
                        it('Näytetään tyhjät liitetiedot', function () {
                          expect(liitetiedot.isVisible()).to.equal(false)
                        })
                      })
                    })
                  })
                })
              })
            })
          })
        })
        describe('Useamman liitetiedon poisto kerralla', function () {
          before(
            opinnot.valitseSuoritus(undefined, '7. vuosiluokka'),
            editor.edit,
            liitetiedot.addItem,
            liitetiedot.itemEditor(0).property('kuvaus').setValue('T1'),
            liitetiedot.addItem,
            liitetiedot.itemEditor(1).property('kuvaus').setValue('T2'),
            editor.saveChanges,
            editor.edit,
            liitetiedot.removeItem(0),
            liitetiedot.removeItem(0),
            editor.saveChanges
          )

          it('Kaikki liitetiedot on poistettu', function () {
            expect(liitetiedot.isVisible()).to.equal(false)
          })
        })
      })

      describe('Valinnainen oppiaine', function () {
        before(opinnot.valitseSuoritus(undefined, '7. vuosiluokka'))
        var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine('.valinnaiset')
        var uusiPakollinenOppiaine =
          opinnot.oppiaineet.uusiOppiaine('.pakolliset')
        describe('Valtakunnallisen oppiaineen lisääminen', function () {
          var historia = editor.subEditor('.valinnainen.HI:eq(0)')
          var historia2 = editor.subEditor('.valinnainen.HI:eq(1)')
          before(
            editor.edit,
            uusiOppiaine.selectValue('Historia'),
            historia.propertyBySelector('.arvosana').selectValue('9'),
            historia
              .propertyBySelector('.property.laajuus .value')
              .setValue('1'),
            uusiOppiaine.selectValue('Historia'),
            historia2.propertyBySelector('.arvosana').selectValue('8')
          )

          describe('Ennen tallennusta', function () {
            it('Kuvaus näytetään', function () {
              expect(historia.property('kuvaus').isVisible()).to.equal(true)
            })
            describe('Tallennus', function () {
              before(editor.saveChanges, wait.until(page.isSavedLabelShown))
              it('Toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.contain('Historia 9')
                expect(extractAsText(S('.oppiaineet'))).to.contain('Historia 8')
              })
            })
          })

          describe('Poistaminen', function () {
            before(
              editor.edit,
              historia.propertyBySelector('>tr:first-child').removeValue,
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )
            it('toimii', function () {
              expect(extractAsText(S('.oppiaineet'))).to.not.contain(
                'Historia 9'
              )
            })
          })
        })

        describe('Uuden paikallisen oppiaineen lisääminen', function () {
          var uusiPaikallinen = editor.subEditor('.valinnainen.paikallinen')
          before(
            editor.edit,
            uusiOppiaine.selectValue('Lisää'),
            uusiPaikallinen.propertyBySelector('.arvosana').selectValue('7'),
            uusiPaikallinen.propertyBySelector('.koodi').setValue('TNS'),
            uusiPaikallinen.propertyBySelector('.nimi').setValue('Tanssi')
          )

          describe('Ennen tallennusta', function () {
            it('Uusi oppiaine näytetään avattuna', function () {
              expect(uusiPaikallinen.property('kuvaus').isVisible()).to.equal(
                true
              )
            })
          })

          describe('Tallennuksen jälkeen', function () {
            before(editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('Toimii', function () {
              expect(extractAsText(S('.oppiaineet'))).to.contain('Tanssi 7')
            })

            describe('Lisäyksen jälkeen', function () {
              var tanssi = editor.subEditor('.valinnainen.TNS')
              before(editor.edit)

              it('Uusi oppiaine on valittavissa myös pakollisissa oppiaineissa', function () {
                expect(
                  uusiPakollinenOppiaine.getOptions().includes('Tanssi')
                ).to.equal(true)
              })
              it('Valinnaisen oppiaineen voi lisätä useaan kertaan', function () {
                expect(uusiOppiaine.getOptions().includes('Tanssi')).to.equal(
                  true
                )
              })

              describe('Poistettaessa suoritus', function () {
                before(tanssi.propertyBySelector('>tr:first-child').removeValue)
                it('Uusi oppiaine löytyy pudotusvalikosta', function () {
                  expect(uusiOppiaine.getOptions()).to.include('Tanssi')
                })

                describe('Muutettaessa lisätyn oppiaineen kuvausta, tallennettaessa ja poistettaessa oppiaine', function () {
                  before(
                    uusiOppiaine.selectValue('Tanssi'),
                    tanssi.propertyBySelector('.arvosana').selectValue('7'),
                    tanssi
                      .propertyBySelector('.nimi')
                      .setValue('Tanssi ja liike'),
                    editor.saveChanges,
                    editor.edit
                  )
                  before(
                    tanssi.propertyBySelector('>tr:first-child').removeValue
                  )

                  describe('Käyttöliittymän tila', function () {
                    it('Muutettu oppiaine löytyy pudotusvalikosta', function () {
                      expect(uusiOppiaine.getOptions()[0]).to.equal(
                        'Tanssi ja liike'
                      )
                    })
                  })

                  describe('Poistettaessa oppiaine pudotusvalikosta', function () {
                    before(uusiOppiaine.removeFromDropdown('Tanssi ja liike'))

                    it('Oppiainetta ei löydy enää pudotusvalikosta', function () {
                      expect(uusiOppiaine.getOptions()).to.not.include(
                        'Tanssi ja liike'
                      )
                    })
                  })

                  after(editor.cancelChanges)
                })
              })
            })
          })
        })
      })

      describe('Pakollinen oppiaine', function () {
        var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine('.pakolliset')
        var uusiValinnainenOppiaine =
          opinnot.oppiaineet.uusiOppiaine('.valinnaiset')
        var filosofia = editor.subEditor('.pakollinen.FI')
        before(
          opinnot.valitseSuoritus(undefined, 'Päättötodistus'),
          editor.edit,
          uusiOppiaine.selectValue('Filosofia'),
          filosofia.propertyBySelector('.arvosana').selectValue('8')
        )

        describe('Lisääminen', function () {
          describe('Ennen tallennnusta', function () {
            it('Uusi oppiaine näytetään avattuna', function () {
              expect(
                filosofia.property('yksilöllistettyOppimäärä').isVisible()
              ).to.equal(true)
            })
            it('Kuvaus on piilotettu', function () {
              expect(filosofia.property('kuvaus').isVisible()).to.equal(false)
            })
          })
          describe('Tallennuksen jälkeen', function () {
            before(editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('Uusi oppiaine näytetään', function () {
              expect(extractAsText(S('.oppiaineet'))).to.contain('Filosofia 8')
            })
          })
        })

        describe('Poistaminen', function () {
          before(
            editor.edit,
            filosofia.propertyBySelector('>tr:first-child').removeValue,
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )
          it('toimii', function () {
            expect(extractAsText(S('.oppiaineet'))).to.not.contain(
              'Filosofia 8'
            )
          })
        })

        describe('Uuden paikallisen oppiaineen lisääminen', function () {
          var uusiPaikallinen = editor.subEditor('.pakollinen.paikallinen')
          before(
            editor.edit,
            uusiOppiaine.selectValue('Lisää'),
            uusiPaikallinen.propertyBySelector('.arvosana').selectValue('7'),
            uusiPaikallinen.propertyBySelector('.koodi').setValue('TNS'),
            uusiPaikallinen.propertyBySelector('.nimi').setValue('Tanssi')
          )

          describe('Ennen tallennusta', function () {
            it('Uusi oppiaine näytetään avattuna', function () {
              expect(uusiPaikallinen.property('kuvaus').isVisible()).to.equal(
                true
              )
            })
            it('Kuvaus on näkyvissä', function () {
              expect(uusiPaikallinen.property('kuvaus').isVisible()).to.equal(
                true
              )
            })
          })

          describe('Tallennuksen jälkeen', function () {
            before(editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('Toimii', function () {
              expect(extractAsText(S('.oppiaineet'))).to.contain('Tanssi 7')
            })

            describe('Lisäyksen jälkeen', function () {
              var tanssi = editor.subEditor('.pakollinen.TNS')
              before(editor.edit)

              it('Uusi oppiaine on valittavissa myös valinnaisissa oppiaineissa', function () {
                expect(
                  uusiValinnainenOppiaine.getOptions().includes('Tanssi')
                ).to.equal(true)
              })
              it('Oppiainetta ei voi lisätä useaan kertaan', function () {
                expect(uusiOppiaine.getOptions().includes('Tanssi')).to.equal(
                  false
                )
              })

              describe('Poistettaessa suoritus', function () {
                before(tanssi.propertyBySelector('>tr:first-child').removeValue)
                it('Uusi oppiaine löytyy pudotusvalikosta', function () {
                  expect(uusiOppiaine.getOptions()).to.include('Tanssi')
                })

                describe('Muutettaessa lisätyn oppiaineen kuvausta, tallennettaessa ja poistettaessa oppiaine', function () {
                  before(
                    uusiOppiaine.selectValue('Tanssi'),
                    tanssi.propertyBySelector('.arvosana').selectValue('7'),
                    tanssi
                      .propertyBySelector('.nimi')
                      .setValue('Tanssi ja liike'),
                    editor.saveChanges,
                    editor.edit
                  )
                  before(
                    tanssi.propertyBySelector('>tr:first-child').removeValue
                  )

                  describe('Käyttöliittymän tila', function () {
                    it('Muutettu oppiaine löytyy pudotusvalikosta', function () {
                      expect(uusiOppiaine.getOptions()[0]).to.equal(
                        'Tanssi ja liike'
                      )
                    })
                  })

                  describe('Poistettaessa oppiaine pudotusvalikosta', function () {
                    before(uusiOppiaine.removeFromDropdown('Tanssi ja liike'))

                    it('Oppiainetta ei löydy enää pudotusvalikosta', function () {
                      expect(uusiOppiaine.getOptions()).to.not.include(
                        'Tanssi ja liike'
                      )
                    })
                  })

                  after(editor.cancelChanges)
                })
              })
            })
          })
        })
      })

      // kts. TOR-1208
      // Oppiaineella Uskonto/Elämänkatsomustieto sama nimi, joten näytetään myös koodiarvo oppiainetta
      // valittaessa, jotta homma pelittää oikein
      describe('Uskonto/Elämänkatsomustieto -vaihtoehdossa näkyy mukana koodiarvo', function () {
        var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine('.pakolliset')
        before(
          opinnot.valitseSuoritus(undefined, 'Päättötodistus'),
          editor.edit,
          editor
            .subEditor('.pakollinen.KT')
            .propertyBySelector('>tr:first-child').removeValue
        )

        it('Uskonto/Elämänkatsomustieto-vaihtoehto puuttuu', function () {
          expect(uusiOppiaine.getOptions()).to.not.include(
            'Uskonto/Elämänkatsomustieto'
          )
        })

        it('Uskonto/Elämänkatsomustieto ja koodiarvo -vaihtoehto löytyy', function () {
          expect(uusiOppiaine.getOptions()).to.include(
            'Uskonto/Elämänkatsomustieto ET'
          )
          expect(uusiOppiaine.getOptions()).to.include(
            'Uskonto/Elämänkatsomustieto KT'
          )
        })

        after(editor.cancelChanges)
      })
    })

    describe('Päätason suorituksen poistaminen', function () {
      describe('Nuorten perusopetus', function () {
        before(editor.edit)

        describe('Mitätöintilinkki', function () {
          it('Näytetään', function () {
            expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(true)
          })

          describe('Painettaessa', function () {
            before(opinnot.deletePäätasonSuoritus)
            it('Pyydetään vahvistus', function () {
              expect(opinnot.confirmDeletePäätasonSuoritusIsShown()).to.equal(
                true
              )
            })

            describe('Painettaessa uudestaan', function () {
              before(
                wait.prepareForNavigation,
                opinnot.confirmDeletePäätasonSuoritus,
                wait.forNavigation,
                wait.until(page.isPäätasonSuoritusDeletedMessageShown)
              )

              it('Päätason suoritus poistetaan', function () { })

              describe('Poistettua päätason suoritusta', function () {
                before(
                  wait.until(page.isReady),
                  opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
                    'perusopetus'
                  ),
                  wait.until(function () {
                    return opinnot.suoritusTabs()[0] === '9. vuosiluokka'
                  })
                )

                it('Ei näytetä', function () {
                  expect(opinnot.suoritusTabs()).to.deep.equal([
                    '9. vuosiluokka',
                    '8. vuosiluokka',
                    '7. vuosiluokka'
                  ])
                })
              })
            })
          })
        })

        describe('Opiskeluoikeudelle, jossa on vain yksi päätason suoritus', function () {
          before(
            Authentication().logout,
            Authentication().login(),
            page.openPage,
            page.oppijaHaku.searchAndSelect('010100-325X'),
            editor.edit
          )

          it('Ei näytetä', function () {
            expect(opinnot.confirmDeletePäätasonSuoritusIsShown()).to.equal(
              false
            )
          })
        })

        describe('Opiskeluoikeudelle, joka on peräisin ulkoisesta järjestelmästä', function () {
          describe('Kun kirjautunut oppilaitoksen tallentajana', function () {
            before(
              Authentication().logout,
              Authentication().login(),
              page.openPage,
              page.oppijaHaku.searchAndSelect('010100-071R')
            )
            it('Ei näytetä poistopainiketta', function () {
              expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(false)
            })
          })

          describe('Kun kirjautunut oppilaitoksen pääkäyttäjänä', function () {
            before(
              Authentication().logout,
              Authentication().login('stadin-pää'),
              page.openPage,
              page.oppijaHaku.searchAndSelect('010100-071R')
            )
            it('Näytetään poistopainike', function () {
              expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(true)
            })
          })
        })
      })

      describe('Aikuisten perusopetus', function () {
        before(
          Authentication().logout,
          Authentication().login(),
          page.openPage,
          page.oppijaHaku.searchAndSelect('280598-2415'),
          editor.edit
        )

        describe('Mitätöintilinkki', function () {
          it('Näytetään', function () {
            expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(true)
          })

          describe('Vahvistusviestin', function () {
            before(
              opinnot.hideInvalidateMessage,
              wait.untilFalse(page.isOpiskeluoikeusInvalidatedMessageShown)
            )
            it('Voi piilottaa', function () {
              expect(page.isOpiskeluoikeusInvalidatedMessageShown()).to.equal(
                false
              )
            })
          })
        })

        after(resetFixtures)
      })

      describe('Käyttöliittymäsiirron mitätöinti oppilaitoksen pääkäyttäjänä', function () {
        before(
          Authentication().logout,
          Authentication().login('stadin-pää'),
          page.openPage,
          page.oppijaHaku.searchAndSelect('180497-112F')
        )

        describe('Mitätöintilinkki', function () {
          it('Näytetään opiskeluoikeudelle', function () {
            expect(opinnot.invalidateOpiskeluoikeusIsShown()).to.equal(true)
          })

          it('Näytetään päätason suoritukselle', function () {
            expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(true)
          })
        })
      })
    })

    describe('Navigointi pois sivulta', function () {
      describe('Kun ei ole tallentamattomia muutoksia', function () {
        before(
          Authentication().logout,
          Authentication().login(),
          page.openPage,
          page.oppijaHaku.searchAndSelect('280598-2415'),
          editor.edit,
          page.oppijaHaku.searchAndSelect('280618-402H')
        )

        it('Onnistuu normaalisti', function () { })

        after(
          page.oppijaHaku.searchAndSelect('220109-784L'),
          opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus')
        )
      })

      describe('Kun on tallentamattomia muutoksia', function () {
        before(
          editor.edit,
          editor.property('suoritustapa').setValue('Erityinen tutkinto')
        )
        describe('Kun käyttäjä valitsee Peruuta', function () {
          before(function () {
            testFrame().confirm = function (msg) {
              return false
            }
          }, opinnot.backToList)
          it('Pysytään muokkauksessa', function () {
            expect(editor.canSave()).to.equal(true)
          })
        })

        describe('Kun käyttäjä valitsee Jatka', function () {
          before(function () {
            testFrame().confirm = function (msg) {
              return true
            }
          }, opinnot.backToList)
          it('Navigointi toimii', function () {
            expect(page.oppijataulukko.isVisible()).to.equal(true)
          })

          after(
            page.oppijaHaku.selectOppija('220109-784L'),
            opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
              'perusopetus'
            )
          )
        })
      })
    })

    describe('Virhetilanteet', function () {
      describe('Kun tallennus epäonnistuu', function () {
        before(
          editor.edit,
          editor.property('todistuksellaNäkyvätLisätiedot').setValue('blah'),
          mockHttp('/koski/api/oppija', { status: 500 }),
          editor.saveChangesAndExpectError
        )
        describe('Käyttöliittymän tila', function () {
          it('Näytetään virheilmoitus', function () {
            expect(page.getErrorMessage()).to.equal(
              'Järjestelmässä tapahtui odottamaton virhe. Yritä myöhemmin uudelleen.'
            )
          })
          it('Tallenna-nappi on jälleen enabloitu', function () {
            expect(opinnot.onTallennettavissa()).to.equal(true)
          })
          it('Käyttöliittymä on edelleen muokkaustilassa', function () {
            expect(opinnot.isEditing()).to.equal(true)
          })
        })

        describe('Kun tallennetaan uudestaan', function () {
          before(editor.saveChanges)
          it('Tallennus onnistuu', function () {
            expect(
              editor.property('todistuksellaNäkyvätLisätiedot').getValue()
            ).to.equal('blah')
          })
        })
      })

      describe('Kun tallennuksen jälkeinen lataus epäonnistuu', function () {
        before(
          editor.edit,
          editor.property('todistuksellaNäkyvätLisätiedot').setValue('blerg'),
          mockHttp('/koski/api/editor', { status: 500 }),
          editor.saveChangesAndExpectError,
          wait.until(page.isTopLevelError)
        )
        describe('Käyttöliittymän tila', function () {
          it('Näytetään koko ruudun virheilmoitus', function () {
            expect(page.getErrorMessage()).to.equal(
              'Järjestelmässä tapahtui odottamaton virhe. Yritä myöhemmin uudelleen. Yritä uudestaan.'
            )
          })
        })
      })
    })
  })

  describe('Opiskeluoikeuden versiot', function () {
    var versiohistoria = opinnot.versiohistoria

    before(
      Authentication().login(),
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('220109-784L'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus'),
      versiohistoria.avaa
    )

    it('Alussa on vain yksi versio', function () {
      expect(versiohistoria.listaa()).to.deep.equal(['v1'])
    })

    describe('Muutoksen jälkeen', function () {
      var liitetiedot = editor.property('liitetiedot')
      before(
        versiohistoria.sulje,
        opinnot.valitseSuoritus(undefined, '7. vuosiluokka'),
        editor.edit,
        liitetiedot.addItem,
        liitetiedot.itemEditor(0).property('kuvaus').setValue('T2'),
        editor.saveChanges,
        versiohistoria.avaa
      )

      it('On kaksi versiota', function () {
        expect(versiohistoria.listaa()).to.deep.equal(['v1', 'v2'])
        expect(liitetiedot.getItems().length).to.equal(1)
      })

      describe('Valittaessa vanha versio', function () {
        before(versiohistoria.valitse('v1'))
        it('Näytetään vanha versio', function () {
          expect(liitetiedot.isVisible()).to.equal(false)
        })

        it('Näytetään oppiaineiden arvosanat', function () {
          expect(extractAsText(S('.oppiaineet'))).to.equal(
            'Arviointiasteikko\n' +
            'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
            'Yhteiset oppiaineet\n' +
            'Oppiaine Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\n' +
            'B1-kieli, ruotsi 8\n' +
            'A1-kieli, englanti 8\n' +
            'Äidinkielenomainen kieli A-oppimäärä, suomi 8\n' +
            'Uskonto/Elämänkatsomustieto 10\n' +
            'Historia 8\n' +
            'Yhteiskuntaoppi 10\n' +
            'Matematiikka 9\n' +
            'Kemia 7\n' +
            'Fysiikka 9\n' +
            'Biologia 9 *\n' +
            'Maantieto 9\n' +
            'Musiikki 7\n' +
            'Kuvataide 8\n' +
            'Kotitalous 8\n' +
            'Terveystieto 8\n' +
            'Käsityö 9\n' +
            'Liikunta 9 **\n' +
            'Valinnaiset aineet\n' +
            'Oppiaine Arvosana Laajuus\n' +
            'B1-kieli, ruotsi S 1 vuosiviikkotuntia\n' +
            'Kotitalous S 1 vuosiviikkotuntia\n' +
            'Liikunta S 0,5 vuosiviikkotuntia\n' +
            'B2-kieli, saksa 9 4 vuosiviikkotuntia\n' +
            'Tietokoneen hyötykäyttö 9\n' +
            '* = yksilöllistetty tai rajattu oppimäärä, ** = painotettu opetus'
          )
        })
      })
    })
  })

  describe('Useita opiskeluoikeuksia', function () {
    before(
      Authentication().login(),
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('180497-112F')
    )
    describe('Alussa', function () {
      it('Uusimpien suoritusten välilehti valittu', function () {
        expect(
          opinnot.suoritusOnValittu('Jyväskylän', '9. vuosiluokka')
        ).to.equal(true)
        expect(
          opinnot.suoritusOnValittu('Kulosaaren', '7. vuosiluokka')
        ).to.equal(true)
      })
    })
    describe('Kun valitaan, ensimmäisestä opiskeluoikeudesta', function () {
      before(opinnot.valitseSuoritus('Jyväskylän', '8. vuosiluokka'))
      it('Toimii', function () {
        expect(
          opinnot.suoritusOnValittu('Jyväskylän', '9. vuosiluokka')
        ).to.equal(false)
        expect(
          opinnot.suoritusOnValittu('Jyväskylän', '8. vuosiluokka')
        ).to.equal(true)
        expect(
          opinnot.suoritusOnValittu('Kulosaaren', '7. vuosiluokka')
        ).to.equal(true)
      })

      describe('Kun valitaan, molemmista opiskeluoikeuksista', function () {
        before(
          opinnot.valitseSuoritus('Kulosaaren', '6. vuosiluokka'),
          opinnot.valitseSuoritus('Jyväskylän', '9. vuosiluokka')
        )
        it('Toimii', function () {
          expect(
            opinnot.suoritusOnValittu('Jyväskylän', '9. vuosiluokka')
          ).to.equal(true)
          expect(
            opinnot.suoritusOnValittu('Jyväskylän', '8. vuosiluokka')
          ).to.equal(false)
          expect(
            opinnot.suoritusOnValittu('Kulosaaren', '7. vuosiluokka')
          ).to.equal(false)
          expect(
            opinnot.suoritusOnValittu('Kulosaaren', '6. vuosiluokka')
          ).to.equal(true)
        })
      })
    })
  })

  describe('Opiskeluoikeuden lisääminen', function () {
    describe('Perusopetuksen oppimäärä', function () {
      before(prepareForNewOppija('kalle', '230872-7258'))

      describe('Aluksi', function () {
        it('Lisää-nappi on disabloitu', function () {
          expect(addOppija.isEnabled()).to.equal(false)
        })
      })

      describe('Kun syötetään validit tiedot', function () {
        before(addOppija.enterValidDataPerusopetus({ suorituskieli: 'ruotsi' }))

        describe('Käyttöliittymän tila', function () {
          it('Lisää-nappi on enabloitu', function () {
            eventually(() => expect(addOppija.isEnabled()).to.equal(true))
          })

          it('Ei näytetä opintojen rahoitus -kenttää', function () {
            expect(addOppija.rahoitusIsVisible()).to.equal(false)
          })

          it('Oikeat tilat tilavaihtoehtoina', function () {
            expect(addOppija.opiskeluoikeudenTilat()).to.deep.equal([
              'Eronnut',
              'Katsotaan eronneeksi',
              'Läsnä',
              'Peruutettu',
              'Valmistunut',
              'Väliaikaisesti keskeytynyt'
            ])
          })
        })

        describe('Kun painetaan Lisää-nappia', function () {
          before(
            addOppija.submitAndExpectSuccess(
              'Tyhjä, Tero (230872-7258)',
              'Päättötodistus'
            )
          )

          var esitäytetytOppiaineet = [
            'Äidinkieli ja kirjallisuus,',
            'A1-kieli,',
            'B1-kieli,',
            'Matematiikka',
            'Biologia',
            'Maantieto',
            'Fysiikka',
            'Kemia',
            'Terveystieto',
            'Uskonto/Elämänkatsomustieto',
            'Historia',
            'Yhteiskuntaoppi',
            'Musiikki',
            'Kuvataide',
            'Käsityö',
            'Liikunta',
            'Kotitalous',
            'Opinto-ohjaus'
          ]

          it('lisätty oppija näytetään', function () { })

          describe('Käyttöliittymän tila', function () {
            it('Lisätty opiskeluoikeus näytetään', function () {
              expect(opinnot.getTutkinto()).to.equal('Perusopetus')
              expect(opinnot.getOppilaitos()).to.equal(
                'Jyväskylän normaalikoulu'
              )
              expect(opinnot.getSuorituskieli()).to.equal('ruotsi')
              expect(
                editor.propertyBySelector('.diaarinumero').getValue()
              ).to.equal('104/011/2014')
            })

            it('Esitäytetyt oppiaineet näytetään', function () {
              expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal(
                esitäytetytOppiaineet
              )
            })
          })

          describe('Painettaessa muokkaa-nappia', function () {
            before(editor.edit)
            it('Esitäytetyt oppiaineet näytetään', function () {
              expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal(
                esitäytetytOppiaineet
              )
            })

            describe('Toiminta-alueittain opiskelevalle', function () {
              before(
                opinnot.expandAll,
                editor.property('erityisenTuenPäätökset').addItem,
                editor.property('opiskeleeToimintaAlueittain').setValue(true)
              )

              it('Esitäyttää toiminta-alueet', function () {
                expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal(
                  [
                    'motoriset taidot',
                    'kieli ja kommunikaatio',
                    'sosiaaliset taidot',
                    'päivittäisten toimintojen taidot',
                    'kognitiiviset taidot'
                  ]
                )
              })

              describe('Kun muutoksia oppiaineisiin', function () {
                var biologia = editor.subEditor('.BI')
                before(
                  editor
                    .property('opiskeleeToimintaAlueittain')
                    .setValue(false),
                  biologia.propertyBySelector('.arvosana').selectValue('8'),
                  editor.property('opiskeleeToimintaAlueittain').setValue(true)
                )
                it('esitäyttöjä ei muuteta', function () {
                  expect(
                    textsOf(S('.oppiaineet .oppiaine .nimi'))
                  ).to.deep.equal([
                    'Äidinkieli ja kirjallisuus,',
                    'A1-kieli,',
                    'B1-kieli,',
                    'Matematiikka',
                    'Biologia',
                    'Maantieto',
                    'Fysiikka',
                    'Kemia',
                    'Terveystieto',
                    'Uskonto/Elämänkatsomustieto',
                    'Historia',
                    'Yhteiskuntaoppi',
                    'Musiikki',
                    'Kuvataide',
                    'Käsityö',
                    'Liikunta',
                    'Kotitalous',
                    'Opinto-ohjaus'
                  ])
                })

                describe('Esitäyttöjen poistaminen', function () {
                  var äidinkieli = editor.subEditor('.pakollinen.AI')
                  before(
                    editor.cancelChanges,
                    editor.edit,
                    äidinkieli.propertyBySelector('>tr:first-child').removeValue
                  )
                  it('toimii', function () {
                    expect(
                      textsOf(S('.oppiaineet .oppiaine .nimi'))
                    ).to.deep.equal([
                      'A1-kieli,',
                      'B1-kieli,',
                      'Matematiikka',
                      'Biologia',
                      'Maantieto',
                      'Fysiikka',
                      'Kemia',
                      'Terveystieto',
                      'Uskonto/Elämänkatsomustieto',
                      'Historia',
                      'Yhteiskuntaoppi',
                      'Musiikki',
                      'Kuvataide',
                      'Käsityö',
                      'Liikunta',
                      'Kotitalous',
                      'Opinto-ohjaus'
                    ])
                  })
                })
              })
            })

            after(editor.cancelChanges)
          })

          describe('Toisen samanlaisen opiskeluoikeuden lisääminen kun opiskeluoikeus on voimassa', function () {
            before(
              opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
              addOppija.selectOppilaitos('Jyväskylän normaalikoulu'),
              addOppija.selectOpiskeluoikeudenTyyppi('Perusopetus'),
              addOppija.submitModal,
              wait.until(page.isErrorShown)
            )
            it('Lisääminen ei onnistu', function () {
              expect(page.getErrorMessage()).to.equal(
                'Vastaava opiskeluoikeus on jo olemassa.'
              )
              expect(editor.isEditBarVisible()).to.equal(false)
            })
          })

          describe('Toisen samanlaisen opiskeluoikeuden lisääminen kun opiskeluoikeus on päättynyt', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.alkuPaiva().setValue('1.1.2117'),
              opiskeluoikeus.tila().aseta('eronnut'),
              opiskeluoikeus.tallenna,
              editor.saveChanges,
              opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
              addOppija.selectOppilaitos('Jyväskylän normaalikoulu'),
              addOppija.selectOpiskeluoikeudenTyyppi('Perusopetus'),
              addOppija.submitAndExpectSuccessModal(
                'Tyhjä, Tero (230872-7258)',
                'Päättötodistus'
              )
            )
            it('Lisääminen ei onnistu', function () {
              expect(page.getErrorMessage()).to.equal(
                'Vastaava opiskeluoikeus on jo olemassa.'
              )
              expect(editor.isEditBarVisible()).to.equal(false)
            })
          })

          describe('Toisen opiskeluoikeuden lisääminen (ammatillinen tutkinto)', function () {
            before(
              page.oppijaHaku.searchAndSelect('230872-7258'),
              opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
              addOppija.selectOppilaitos('Omnia'),
              addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
              addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
              addOppija.selectTutkinto('Autoalan perustutkinto'),
              addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
              addOppija.selectAloituspäivä('1.1.2018'),
              addOppija.selectOpintojenRahoitus(
                'Valtionosuusrahoitteinen koulutus'
              ),
              addOppija.submitAndExpectSuccessModal(
                'Tyhjä, Tero (230872-7258)',
                'Autoalan perustutkinto'
              )
            )
            it('Onnistuu ja uusi ammatillinen opiskeluoikeus tulee valituksi', function () { })

            describe('Kolmannen opiskeluoikeuden lisääminen (oppiaineen oppimäärä)', function () {
              this.timeout(30000)

              before(
                timeout.overrideWaitTime(20000),

                opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
                addOppija.selectOppilaitos('Kulosaaren ala-aste'),
                addOppija.selectOpiskeluoikeudenTyyppi('Aikuisten perusopetus'),
                addOppija.selectOppimäärä(
                  'Perusopetuksen oppiaineen oppimäärä'
                ),
                addOppija.selectOppiaine('Fysiikka'),
                addOppija.selectOpintojenRahoitus(
                  'Valtionosuusrahoitteinen koulutus'
                ),
                addOppija.submitAndExpectSuccessModal(
                  'Tyhjä, Tero (230872-7258)',
                  'Fysiikka'
                )
              )
              after(timeout.resetDefaultWaitTime())
              it('toimii', function () { })
            })
          })
        })
      })
    })

    describe('Nuorten perusopetuksen oppiaineen oppimäärä', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataPerusopetus(),
        addOppija.selectOpiskeluoikeudenTyyppi('Perusopetus')
      )

      describe('Käyttöliittymän tila', function () {
        it('Näytetään oppimäärävaihtoehdot', function () {
          expect(addOppija.oppimäärät()).to.deep.equal([
            'Nuorten perusopetuksen oppiaineen oppimäärä',
            'Perusopetuksen oppimäärä'
          ])
        })
      })

      describe('Ei-tiedossa oppiaine', function () {
        before(
          addOppija.selectOppimäärä(
            'Nuorten perusopetuksen oppiaineen oppimäärä'
          )
        )

        it('on valittavissa', function () {
          expect(addOppija.oppiaineet()).to.contain('Ei tiedossa')
        })
      })

      describe('Kun valitaan oppiaineen oppimäärä ja oppiaine', function () {
        before(
          addOppija.selectOppimäärä(
            'Nuorten perusopetuksen oppiaineen oppimäärä'
          ),
          addOppija.selectOppiaine('A1-kieli')
        )

        describe('Kun kielivalinta puuttuu', function () {
          it('Lisäys ei ole mahdollista', function () {
            expect(addOppija.isEnabled()).to.equal(false)
          })
        })

        describe('Kun valitaan kieli ja lisätään oppiaine', function () {
          before(
            addOppija.selectKieliaineenKieli('englanti'),
            wait.forMilliseconds(1000),
            addOppija.submitAndExpectSuccess(
              'Tyhjä, Tero (230872-7258)',
              'A1-kieli'
            )
          )

          it('Luodaan opiskeluoikeus, jolla on oppiaineen oppimäärän suoritus', function () {
            expect(opinnot.getSuorituskieli()).to.equal('suomi')
            expect(
              editor.propertyBySelector('.perusteenDiaarinumero').getValue()
            ).to.equal('104/011/2014')
          })

          it('Näytetään suorituksen tyyppi opiskeluoikeuden otsikossa', function () {
            expect(S('.opiskeluoikeus h3 .koulutus').text()).to.equal(
              'Perusopetuksen oppiaineen oppimäärä'
            )
          })

          describe('Toisen oppiaineen lisääminen', function () {
            var lisääSuoritus = opinnot.lisääSuoritusDialog
            before(
              editor.edit,
              lisääSuoritus.open('lisää oppiaineen suoritus'),
              wait.forAjax,
              lisääSuoritus.property('tunniste').setValue('Matematiikka'),
              lisääSuoritus.toimipiste.select(
                'Jyväskylän normaalikoulu, alakoulu'
              ),
              lisääSuoritus.lisääSuoritus
            )

            it('Näytetään uusi suoritus', function () {
              expect(opinnot.suoritusTabs()).to.deep.equal([
                'A1-kieli',
                'Matematiikka'
              ])
            })

            it('Näytetään suorituksen tyypppi opiskeluoikeuden otsikossa', function () {
              expect(S('.opiskeluoikeus h3 .koulutus').text()).to.equal(
                'Perusopetuksen oppiaineen oppimäärä'
              )
            })
          })
        })
      })
    })

    describe('Aikuisten perusopetus, uusi oppija', function () {
      this.timeout(30000)

      before(
        timeout.overrideWaitTime(20000),
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataPerusopetus(),
        addOppija.selectOpiskeluoikeudenTyyppi('Aikuisten perusopetus')
      )

      after(timeout.resetDefaultWaitTime())

      it('Näytetään opintojen rahoitus-kenttä', function () {
        return wait
          .untilVisible(
            '[data-testid="uusiOpiskeluoikeus.modal.opintojenRahoitus"'
          )()
          .then(() => {
            expect(addOppija.rahoitusIsVisible()).to.equal(true)
          })
      })
    })

    describe('Aikuisten perusopetus', function () {
      this.timeout(30000)

      before(
        timeout.overrideWaitTime(20000),
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataPerusopetus(),
        addOppija.selectOpiskeluoikeudenTyyppi('Aikuisten perusopetus'),
        addOppija.selectOppimäärä('Aikuisten perusopetuksen oppimäärä'),
        addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
        addOppija.selectMaksuttomuus(0),
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'Aikuisten perusopetuksen oppimäärä'
        )
      )

      after(timeout.resetDefaultWaitTime())

      describe('Lisäyksen jälkeen', function () {
        it('Näytetään oikein', function () {
          expect(S('.koulutusmoduuli .tunniste').text()).to.equal(
            'Aikuisten perusopetuksen oppimäärä'
          )
          expect(
            editor.propertyBySelector('.diaarinumero').getValue()
          ).to.equal('OPH-1280-2017')
          expect(opinnot.getSuorituskieli()).to.equal('suomi')
        })
        describe('Oppiaineiden suoritukset', function () {
          before(editor.edit)
          it('Esitäyttää pakolliset oppiaineet', function () {
            var expectedOppiaineet = [
              'Äidinkieli ja kirjallisuus,',
              'A1-kieli,',
              'B1-kieli,',
              'Matematiikka',
              'Biologia',
              'Maantieto',
              'Fysiikka',
              'Kemia',
              'Terveystieto',
              'Uskonto/Elämänkatsomustieto',
              'Historia',
              'Yhteiskuntaoppi',
              'Musiikki',
              'Kuvataide',
              'Käsityö',
              'Liikunta',
              'Kotitalous',
              'Opinto-ohjaus'
            ]
            return wait
              .until(function () {
                var oppiaineet = textsOf(S('.oppiaineet .oppiaine .nimi'))
                var kieli = S('.oppiaineet .oppiaine .kieli input').val()
                return (
                  oppiaineet.length === expectedOppiaineet.length &&
                  JSON.stringify(oppiaineet) ===
                    JSON.stringify(expectedOppiaineet) &&
                  kieli === 'Suomen kieli ja kirjallisuus'
                )
              })()
              .then(function () {
                var oppiaineet = textsOf(S('.oppiaineet .oppiaine .nimi'))
                expect(oppiaineet).to.deep.equal(expectedOppiaineet)
                expect(S('.oppiaineet .oppiaine .kieli input').val()).to.equal(
                  'Suomen kieli ja kirjallisuus'
                )
              })
          })
          after(editor.cancelChanges)
        })
      })

      describe('Oppiaineiden näyttäminen', function () {
        it('Arvioimattomia ei näytetä', function () {
          expect(toArray(S('.oppiaineet td.oppiaine')).length).to.equal(0)
        })

        describe('Arvioimaton oppiane jolla arvioitu kurssi', function () {
          var äidinkieli = opinnot.oppiaineet.oppiaine('AI')
          before(
            editor.edit,
            äidinkieli.avaaLisääKurssiDialog,
            äidinkieli.lisääKurssiDialog.valitseKurssi('Kieli ja kulttuuri'),
            äidinkieli.lisääKurssiDialog.lisääKurssi,
            äidinkieli.kurssi('ÄI4').arvosana.setValue('8'),
            editor.saveChanges
          )

          it('näytetään', function () {
            expect(toArray(S('.oppiaineet td.oppiaine')).length).to.equal(1)
          })
        })
      })

      describe('Alkuvaiheen opintojen lisääminen', function () {
        before(
          editor.edit,
          opinnot.lisääSuoritusDialog.clickLink(
            'lisää opintojen alkuvaiheen suoritus'
          ),
          editor.saveChanges
        )

        it('Näytetään uusi suoritus', function () {
          expect(opinnot.suoritusTabs()).to.deep.equal([
            'Aikuisten perusopetuksen oppimäärä',
            'Aikuisten perusopetuksen oppimäärän alkuvaihe'
          ])
        })
      })
    })

    describe('Aikuisten perusopetuksen alkuvaihe', function () {
      this.timeout(30000)

      before(
        timeout.overrideWaitTime(20000),
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataPerusopetus(),
        addOppija.selectOpiskeluoikeudenTyyppi('Aikuisten perusopetus'),
        addOppija.selectOppimäärä(
          'Aikuisten perusopetuksen oppimäärän alkuvaihe'
        ),
        addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
        addOppija.selectMaksuttomuus(0),
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'Aikuisten perusopetuksen oppimäärän alkuvaihe'
        )
      )

      it('Näytetään oikein', function () {
        expect(S('.koulutusmoduuli .tunniste').text()).to.equal(
          'Aikuisten perusopetuksen oppimäärän alkuvaihe'
        )
        expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal(
          'OPH-1280-2017'
        )
        expect(opinnot.getSuorituskieli()).to.equal('suomi')
      })

      describe('Tietojen muuttaminen', function () {
        before(editor.edit)
        describe('Oppiaineen lisäys', function () {
          var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine()
          describe('Valtakunnallisen oppiaineen lisääminen', function () {
            var opintoOhjaus = editor.subEditor('.valinnainen.OP')
            before(
              uusiOppiaine.selectValue('Opinto-ohjaus ja työelämän taidot'),
              opintoOhjaus.propertyBySelector('.arvosana').selectValue('9'),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )
            it('Toimii', function () {
              expect(extractAsText(S('.oppiaineet'))).to.contain(
                'Opinto-ohjaus ja työelämän taidot 9'
              )
            })

            describe('Poistaminen', function () {
              before(
                editor.edit,
                opintoOhjaus.propertyBySelector('>tr:first-child').removeValue,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )
              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.not.contain(
                  'Opinto-ohjaus ja työelämän taidot 9'
                )
              })
            })
          })

          describe('Uuden paikallisen oppiaineen lisääminen', function () {
            var uusiPaikallinen = editor.subEditor('.valinnainen.paikallinen')
            before(
              editor.edit,
              uusiOppiaine.selectValue('Lisää'),
              uusiPaikallinen.propertyBySelector('.arvosana').selectValue('7'),
              uusiPaikallinen.propertyBySelector('.koodi').setValue('TNS'),
              uusiPaikallinen.propertyBySelector('.nimi').setValue('Tanssi')
            )

            describe('Ennen tallennusta', function () {
              it('Uusi oppiaine näytetään avattuna', function () {
                expect(uusiPaikallinen.property('kuvaus').isVisible()).to.equal(
                  true
                )
              })
            })

            describe('Tallennuksen jälkeen', function () {
              before(editor.saveChanges, wait.until(page.isSavedLabelShown))
              it('Toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.contain('Tanssi 7')
              })
            })
          })
        })
      })

      after(timeout.resetDefaultWaitTime())
    })

    describe('Perusopetuksen oppiaineen oppimäärä', function () {
      this.timeout(30000)

      before(
        timeout.overrideWaitTime(20000),
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataPerusopetus(),
        addOppija.selectOpiskeluoikeudenTyyppi('Aikuisten perusopetus')
      )

      describe('Käyttöliittymän tila', function () {
        it('Näytetään oppimäärävaihtoehdot', function () {
          expect(addOppija.oppimäärät()).to.deep.equal([
            'Aikuisten perusopetuksen oppimäärän alkuvaihe',
            'Perusopetuksen oppiaineen oppimäärä',
            'Aikuisten perusopetuksen oppimäärä'
          ])
        })
      })

      describe('Ei-tiedossa oppiaine', function () {
        before(addOppija.selectOppimäärä('Perusopetuksen oppiaineen oppimäärä'))

        it('on valittavissa', function () {
          expect(addOppija.oppiaineet()).to.contain('Ei tiedossa')
        })
      })

      describe('Kun valitaan oppiaineen oppimäärä ja oppiaine', function () {
        before(
          addOppija.selectOppimäärä('Perusopetuksen oppiaineen oppimäärä'),
          addOppija.selectOppiaine('A1-kieli')
        )

        describe('Kun kielivalinta puuttuu', function () {
          it('Lisäys ei ole mahdollista', function () {
            expect(addOppija.isEnabled()).to.equal(false)
          })
        })

        describe('Kun valitaan kieli ja lisätään oppiaine', function () {
          before(
            timeout.overrideWaitTime(20000),
            addOppija.selectKieliaineenKieli('englanti'),
            wait.forMilliseconds(1000),
            addOppija.selectOpintojenRahoitus(
              'Valtionosuusrahoitteinen koulutus'
            ),
            addOppija.selectMaksuttomuus(0),
            addOppija.submitAndExpectSuccess(
              'Tyhjä, Tero (230872-7258)',
              'A1-kieli'
            )
          )

          it('Luodaan opiskeluoikeus, jolla on oppiaineen oppimäärän suoritus', function () {
            expect(opinnot.getSuorituskieli()).to.equal('suomi')
            expect(
              editor.propertyBySelector('.perusteenDiaarinumero').getValue()
            ).to.equal('OPH-1280-2017')
          })

          it('Näytetään suorituksen tyyppi opiskeluoikeuden otsikossa', function () {
            expect(S('.opiskeluoikeus h3 .koulutus').text()).to.equal(
              'Perusopetuksen oppiaineen oppimäärä'
            )
          })

          describe('Toisen oppiaineen lisääminen', function () {
            var lisääSuoritus = opinnot.lisääSuoritusDialog
            before(
              editor.edit,
              lisääSuoritus.open('lisää oppiaineen suoritus'),
              wait.forAjax,
              lisääSuoritus.property('tunniste').setValue('Matematiikka'),
              lisääSuoritus.toimipiste.select(
                'Jyväskylän normaalikoulu, alakoulu'
              ),
              lisääSuoritus.lisääSuoritus
            )

            it('Näytetään uusi suoritus', function () {
              expect(opinnot.suoritusTabs()).to.deep.equal([
                'A1-kieli',
                'Matematiikka'
              ])
            })

            it('Näytetään suorituksen tyyppi opiskeluoikeuden otsikossa', function () {
              expect(S('.opiskeluoikeus h3 .koulutus').text()).to.equal(
                'Perusopetuksen oppiaineen oppimäärä'
              )
            })
          })
        })
      })
    })

    describe('Back-nappi', function () {
      describe('Kun täytetään tiedot ja palataan hakuun', function () {
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataPerusopetus(),
          wait.prepareForNavigation,
          goBack,
          wait.forNavigation,
          wait.until(page.oppijataulukko.isVisible)
        )
        describe('Käyttöliittymän tila', function () {
          it('Syötetty henkilötunnus näytetään', function () {
            expect(page.oppijaHaku.getSearchString()).to.equal('230872-7258')
          })
          it('Uuden oppijan lisäys on mahdollista', function () {
            expect(page.oppijaHaku.canAddNewOppija()).to.equal(true)
          })
        })
        describe('Kun täytetään uudestaan', function () {
          before(
            page.oppijaHaku.addNewOppija,
            addOppija.enterValidDataPerusopetus()
          )
          it('Lisää-nappi on enabloitu', function () {
            eventually(() => expect(addOppija.isEnabled()).to.equal(true))
          })
        })
        describe('Back-nappi lisäyksen jälkeen', function () {
          before(
            addOppija.submitAndExpectSuccess(
              'Tyhjä, Tero (230872-7258)',
              'Päättötodistus'
            ),
            wait.prepareForNavigation,
            goBack,
            wait.forNavigation,
            addOppija.enterValidDataPerusopetus()
          )
          it('Uuden oppijan lisäys on mahdollista', function () {
            expect(addOppija.isEnabled()).to.equal(true)
          })

          describe('Lisättäessä uudelleen', function () {
            before(addOppija.submit, wait.until(page.isErrorShown))

            it('Lisäys epäonnistuu, koska oppijalla on jo vastaava opiskeluoikeus läsnä-tilassa', function () {
              expect(page.getErrorMessage()).to.equal(
                'Vastaava opiskeluoikeus on jo olemassa.'
              )
            })
          })
        })
      })
    })
  })
})
