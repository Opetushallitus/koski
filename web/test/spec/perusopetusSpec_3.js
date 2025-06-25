describe('Perusopetus 3', function () {
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

  describe('Vuosiluokan suorituksen lisääminen', function () {
    var lisääSuoritus = opinnot.lisääSuoritusDialog

    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataPerusopetus(),
      addOppija.submitAndExpectSuccess(
        'Tyhjä, Tero (230872-7258)',
        'Päättötodistus'
      ),
      editor.edit,
      editor.property('tila').removeItem(0),
      opinnot.avaaLisaysDialogi,
      opiskeluoikeus.alkuPaiva().setValue(date2017Str),
      opiskeluoikeus.tila().aseta('lasna'),
      opiskeluoikeus.tallenna
    )
    describe('Kun opiskeluoikeus on tilassa VALMIS', function () {
      before(
        opinnot.avaaLisaysDialogi,
        opiskeluoikeus.tila().aseta('valmistunut'),
        opiskeluoikeus.tallenna
      )

      it('Päätason suoritusta ei voi lisätä', function () {
        expect(
          lisääSuoritus.isLinkVisible('lisää vuosiluokan suoritus')
        ).to.equal(false)
      })

      after(editor.property('tila').removeItem(0))
    })
    describe('Kun opiskeluoikeus on tilassa LÄSNÄ', function () {
      describe('Ennen lisäystä', function () {
        it('Päätason suorituksen voi lisätä', function () {
          expect(
            lisääSuoritus.isLinkVisible('lisää vuosiluokan suoritus')
          ).to.equal(true)
        })
        it('Näytetään muut päätason suoritukset', function () {
          expect(opinnot.suoritusTabs()).to.deep.equal(['Päättötodistus'])
        })
      })
      describe('Lisättäessä ensimmäinen', function () {
        before(lisääSuoritus.open('lisää vuosiluokan suoritus'))
        describe('Aluksi', function () {
          it('Lisää-nappi on disabloitu', function () {
            expect(lisääSuoritus.isEnabled()).to.equal(false)
          })
          it('Valitsee automaattisesti pienimmän puuttuvan luokka-asteen', function () {
            expect(lisääSuoritus.property('tunniste').getValue()).to.equal(
              '1. vuosiluokka'
            )
          })
        })
        describe('Kun syötetään luokkatieto ja valitaan toimipiste', function () {
          before(
            lisääSuoritus.property('luokka').setValue('1a'),
            lisääSuoritus.toimipiste.select(
              'Jyväskylän normaalikoulu, alakoulu'
            )
          )
          it('Lisää-nappi on disabloitu', function () {
            expect(lisääSuoritus.isEnabled()).to.equal(false)
          })

          describe('Kun syötetään vielä alkamispäivä', function () {
            before(lisääSuoritus.property('alkamispäivä').setValue(date2017Str))
            it('Lisää nappi on enabloitu', function () {
              expect(lisääSuoritus.isEnabled()).to.equal(true)
            })

            describe('Kun painetaan Lisää-nappia', function () {
              var äidinkieli = opinnot.oppiaineet.oppiaine(0)
              var arvosana = äidinkieli.propertyBySelector('.arvosana')
              before(lisääSuoritus.lisääSuoritus)
              describe('Käyttöliittymän tila', function () {
                it('Näytetään uusi suoritus', function () {
                  expect(opinnot.suoritusTabs()).to.deep.equal([
                    'Päättötodistus',
                    '1. vuosiluokka'
                  ])
                })
                it('Uusi suoritus on valittuna', function () {
                  expect(opinnot.getTutkinto()).to.equal('1. vuosiluokka')
                })
                it('Toimipiste on oikein', function () {
                  expect(editor.property('toimipiste').getValue()).to.equal(
                    'Jyväskylän normaalikoulu, alakoulu'
                  )
                })
                describe('Tutkinnon peruste', function () {
                  before(editor.saveChanges)
                  it('Esitäyttää perusteen diaarinumeron', function () {
                    expect(
                      editor.propertyBySelector('.diaarinumero').getValue()
                    ).to.equal('104/011/2014')
                  })
                })
              })
              describe('Annettaessa oppiaineelle arvosana', function () {
                before(
                  editor.edit,
                  arvosana.selectValue('5'),
                  editor.saveChanges
                )
                it('muutettu arvosana näytetään', function () {
                  expect(arvosana.getValue()).to.equal('5')
                })
                it('suoritus siirtyy VALMIS-tilaan', function () {
                  expect(äidinkieli.elem().hasClass('valmis')).to.equal(true)
                })

                describe('Poistettaessa arvosana', function () {
                  before(
                    editor.edit,
                    opinnot.expandAll,
                    arvosana.selectValue('Ei valintaa'),
                    editor.saveChanges,
                    wait.until(page.isSavedLabelShown)
                  )

                  it('Arvosana poistetaan', function () {
                    // Arvosanataulukko näytetään, vaikka kaikki oppiaineet ovat KESKEN-tilassa
                    expect(opinnot.oppiaineet.isVisible()).to.equal(true)
                  })
                })
              })

              describe('Merkitseminen valmiiksi', function () {
                before(editor.edit)
                var dialog = tilaJaVahvistus.merkitseValmiiksiDialog
                describe('Aluksi', function () {
                  it('Tila on "kesken"', function () {
                    expect(tilaJaVahvistus.text()).to.equal('Suoritus kesken')
                  })
                })
                describe('Kun on keskeneräisiä oppiaineita', function () {
                  it('Merkitse valmiiksi -nappi on disabloitu', function () {
                    expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(
                      false
                    )
                  })
                })
                describe('Kun kaikki oppiaineet on merkitty valmiiksi', function () {
                  before(
                    opinnot.oppiaineet.merkitseOppiaineetValmiiksi(),
                    editor.edit
                  )
                  describe('Aluksi', function () {
                    it('Merkitse valmiiksi -nappi näytetään', function () {
                      expect(
                        tilaJaVahvistus.merkitseValmiiksiEnabled()
                      ).to.equal(true)
                    })
                  })
                  describe('Kun merkitään valmiksi', function () {
                    before(
                      tilaJaVahvistus.merkitseValmiiksi,
                      dialog.editor.property('päivä').setValue(currentDateStr),
                      dialog.myöntäjät.itemEditor(0).setValue('Lisää henkilö'),
                      dialog.myöntäjät
                        .itemEditor(0)
                        .propertyBySelector('.nimi')
                        .setValue('Reijo Reksi'),
                      dialog.myöntäjät
                        .itemEditor(0)
                        .propertyBySelector('.titteli')
                        .setValue('rehtori')
                    )

                    describe('Merkitse valmiiksi -dialogi', function () {
                      it('Esitäyttää paikkakunnan valitun organisaation mukaan', function () {
                        expect(
                          dialog.editor.property('paikkakunta').getValue()
                        ).to.equal('Jyväskylä')
                      })
                    })

                    describe('Kun painetaan Merkitse valmiiksi -nappia', function () {
                      before(
                        dialog.editor
                          .property('paikkakunta')
                          .setValue('Jyväskylä mlk'),
                        dialog.merkitseValmiiksi
                      )

                      describe('Käyttöliittymän tila', function () {
                        it('Tila on "valmis" ja vahvistus näytetään', function () {
                          expect(tilaJaVahvistus.text()).to.equal(
                            'Suoritus valmis Vahvistus : ' +
                            currentDateStr +
                            ' Jyväskylä mlk Reijo Reksi , rehtori\nSiirretään seuraavalle luokalle'
                          )
                        })

                        it('Merkitse valmiiksi -nappia ei näytetä', function () {
                          expect(
                            tilaJaVahvistus.merkitseValmiiksiEnabled()
                          ).to.equal(false)
                        })
                      })

                      describe('Kun muutetaan takaisin keskeneräiseksi', function () {
                        before(tilaJaVahvistus.merkitseKeskeneräiseksi)
                        it('Tila on "kesken" ja vahvistus on poistettu', function () {
                          expect(tilaJaVahvistus.text()).to.equal(
                            'Suoritus kesken'
                          )
                        })
                      })

                      describe('Kun muutetaan tila takaisin valmiiksi', function () {
                        before(
                          tilaJaVahvistus.merkitseValmiiksi,
                          dialog.editor
                            .property('päivä')
                            .setValue(currentDateStr),
                          dialog.myöntäjät
                            .itemEditor(0)
                            .setValue('Lisää henkilö'),
                          dialog.myöntäjät
                            .itemEditor(0)
                            .propertyBySelector('.nimi')
                            .setValue('Reijo Reksi'),
                          dialog.myöntäjät
                            .itemEditor(0)
                            .propertyBySelector('.titteli')
                            .setValue('rehtori'),
                          dialog.editor
                            .property('paikkakunta')
                            .setValue('Jyväskylä mlk'),
                          dialog.merkitseValmiiksi
                        )
                        it('Tila on "valmis" ja vahvistus näytetään', function () {
                          expect(tilaJaVahvistus.text()).to.equal(
                            'Suoritus valmis Vahvistus : ' +
                            currentDateStr +
                            ' Jyväskylä mlk Reijo Reksi , rehtori\nSiirretään seuraavalle luokalle'
                          )
                        })
                      })

                      describe('Lisättäessä toinen', function () {
                        before(
                          editor.edit,
                          lisääSuoritus.open('lisää vuosiluokan suoritus')
                        )
                        describe('Aluksi', function () {
                          it('Lisää-nappi on disabloitu', function () {
                            expect(lisääSuoritus.isEnabled()).to.equal(false)
                          })
                          it('Valitsee automaattisesti pienimmän puuttuvan luokka-asteen', function () {
                            expect(
                              lisääSuoritus.property('tunniste').getValue()
                            ).to.equal('2. vuosiluokka')
                          })
                          it('Käytetään oletusarvona edellisen luokan toimipistettä', function () {
                            expect(
                              editor.property('toimipiste').getValue()
                            ).to.equal('Jyväskylän normaalikoulu, alakoulu')
                          })
                        })
                        describe('Lisäyksen jälkeen', function () {
                          before(
                            lisääSuoritus.property('luokka').setValue('2a'),
                            lisääSuoritus
                              .property('alkamispäivä')
                              .setValue(date2018Str),
                            lisääSuoritus.lisääSuoritus
                          )

                          it('Uusin suoritus näytetään täbeissä viimeisenä', function () {
                            expect(opinnot.suoritusTabs()).to.deep.equal([
                              'Päättötodistus',
                              '1. vuosiluokka',
                              '2. vuosiluokka'
                            ])
                          })

                          it('Uusi suoritus on valittuna', function () {
                            expect(opinnot.getTutkinto()).to.equal(
                              '2. vuosiluokka'
                            )
                            expect(
                              editor.property('luokka').getValue()
                            ).to.equal('2a')
                          })

                          describe('Kun merkitään valmiiksi, jää luokalle', function () {
                            var opintojenTilaJaVahvistus =
                              opinnot.tilaJaVahvistus
                            var merkitseValmiiksiDialog =
                              opintojenTilaJaVahvistus.merkitseValmiiksiDialog
                            var dialogEditor = merkitseValmiiksiDialog.editor
                            var myöntäjät =
                              dialogEditor.property('myöntäjäHenkilöt')
                            before(
                              opinnot.oppiaineet.merkitseOppiaineetValmiiksi(),
                              opintojenTilaJaVahvistus.merkitseValmiiksi,
                              dialogEditor
                                .propertyBySelector('.jaa-tai-siirretaan')
                                .setValue(false),
                              dialogEditor
                                .property('päivä')
                                .setValue(date2018Str),
                              dialogEditor
                                .property('paikkakunta')
                                .setValue('Jyväskylä mlk')
                            )

                            describe('Myöntäjät-lista', function () {
                              it('Edellisen suorituksen vahvistaja löytyy listalta', function () {
                                expect(myöntäjät.getOptions()).to.deep.equal([
                                  'Reijo Reksi, rehtori',
                                  'Lisää henkilö'
                                ])
                              })
                            })

                            describe('Kun jatketaan valmiiksi merkintää käyttäen edellistä myöntäjä-henkilöä', function () {
                              before(
                                myöntäjät.itemEditor(0).setValue('Reijo Reksi'),
                                merkitseValmiiksiDialog.merkitseValmiiksi
                              )

                              it('Tila on "valmis" ja vahvistus näytetään', function () {
                                expect(
                                  opintojenTilaJaVahvistus.text()
                                ).to.equal(
                                  'Suoritus valmis Vahvistus : ' +
                                  date2018Str +
                                  ' Jyväskylä mlk Reijo Reksi , rehtori\nEi siirretä seuraavalle luokalle'
                                )
                              })

                              describe('Seuraavan luokka-asteen lisäyksessä', function () {
                                before(
                                  lisääSuoritus.open(
                                    'lisää vuosiluokan suoritus'
                                  )
                                )
                                it('On mahdollista lisätä sama luokka-aste uudelleen', function () {
                                  expect(
                                    lisääSuoritus
                                      .property('tunniste')
                                      .getValue()
                                  ).to.equal('2. vuosiluokka')
                                })

                                describe('Lisättäessä toinen 2. luokan suoritus', function () {
                                  before(
                                    lisääSuoritus
                                      .property('luokka')
                                      .setValue('2x'),
                                    lisääSuoritus
                                      .property('alkamispäivä')
                                      .setValue(date2019Str),
                                    lisääSuoritus.lisääSuoritus
                                  )

                                  it('Uusi suoritus tulee valituksi', function () {
                                    expect(
                                      editor.property('luokka').getValue()
                                    ).to.equal('2x')
                                  })

                                  describe('Tallennettaessa', function () {
                                    before(
                                      opinnot.oppiaineet.merkitseOppiaineetValmiiksi(),
                                      opintojenTilaJaVahvistus.merkitseValmiiksi,
                                      opintojenTilaJaVahvistus.lisääVahvistus(
                                        date2019Str
                                      ),
                                      editor.saveChanges
                                    )

                                    it('Uusi suoritus on edelleen valittu', function () {
                                      expect(
                                        editor.property('luokka').getValue()
                                      ).to.equal('2x')
                                    })

                                    it('Uusi suoritus on täbeissä ennen vanhempaa 2.luokan suoritusta', function () {
                                      expect(
                                        opinnot.suoritusTabIndex()
                                      ).to.equal(1)
                                    })

                                    describe('Kun kaikki luokka-asteet on lisätty', function () {
                                      before(editor.edit)
                                      for (var i = 3; i <= 9; i++) {
                                        const pvm = `2.1.${2015 + i}`
                                        before(
                                          lisääSuoritus.open(
                                            'lisää vuosiluokan suoritus'
                                          ),
                                          lisääSuoritus
                                            .property('luokka')
                                            .setValue(i + 'a'),
                                          lisääSuoritus
                                            .property('alkamispäivä')
                                            .setValue(pvm),
                                          lisääSuoritus.lisääSuoritus,
                                          opinnot.oppiaineet.merkitseOppiaineetValmiiksi()
                                        )
                                        if (i < 9) {
                                          before(
                                            opintojenTilaJaVahvistus.merkitseValmiiksi,
                                            opintojenTilaJaVahvistus.lisääVahvistus(
                                              pvm
                                            ),
                                            opinnot.avaaLisaysDialogi,
                                            opiskeluoikeus
                                              .tila()
                                              .aseta('valmistunut')
                                          )
                                        }
                                      }

                                      it('Suorituksia ei voi enää lisätä', function () {
                                        expect(
                                          lisääSuoritus.isLinkVisible(
                                            'lisää vuosiluokan suoritus'
                                          )
                                        ).to.equal(false)
                                      })

                                      it('9. luokalle ei esitäytetä oppiaineita', function () {
                                        expect(
                                          textsOf(
                                            S('.oppiaineet .oppiaine .nimi')
                                          )
                                        ).to.deep.equal([])
                                      })

                                      describe('Uudempi 2.luokan suoritus', function () {
                                        before(
                                          editor.saveChanges,
                                          opinnot.valitseSuoritus(
                                            undefined,
                                            '2. vuosiluokka'
                                          )
                                        )
                                        it('On edelleen täbeissä ennen vanhempaa 2.luokan suoritusta', function () {
                                          expect(
                                            editor.property('luokka').getValue()
                                          ).to.equal('2x')
                                        })
                                      })

                                      describe('Kun poistetaan myöntäjä listalta', function () {
                                        before(
                                          opinnot.valitseSuoritus(
                                            undefined,
                                            '2. vuosiluokka'
                                          ),
                                          editor.edit,
                                          tilaJaVahvistus.merkitseKeskeneräiseksi,
                                          opinnot.oppiaineet.merkitseOppiaineetValmiiksi(),
                                          opintojenTilaJaVahvistus.merkitseValmiiksi,
                                          myöntäjät.removeFromDropdown(
                                            'Reijo Reksi'
                                          ),
                                          merkitseValmiiksiDialog.peruuta,
                                          opintojenTilaJaVahvistus.merkitseValmiiksi
                                        )
                                        it('Uudelleen avattaessa myöntäjää ei enää ole listalla', function () {
                                          expect(
                                            myöntäjät.getOptions()
                                          ).to.deep.equal(['Lisää henkilö'])
                                        })
                                      })
                                    })
                                  })
                                })
                              })
                            })
                          })
                        })
                      })
                    })
                  })
                })
              })
            })
          })
        })
      })

      describe('Oppiaineiden esitäyttö', function () {
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataPerusopetus(),
          addOppija.submitAndExpectSuccess(
            'Tyhjä, Tero (230872-7258)',
            'Päättötodistus'
          ),
          editor.edit
        )

        function lisääVuosiluokka(luokkaAste) {
          before(
            lisääSuoritus.open('lisää vuosiluokan suoritus'),
            lisääSuoritus
              .property('tunniste')
              .setValue(luokkaAste + '. vuosiluokka'),
            lisääSuoritus.property('luokka').setValue(luokkaAste + 'a'),
            lisääSuoritus.toimipiste.select(
              'Jyväskylän normaalikoulu, alakoulu'
            ),
            lisääSuoritus.property('alkamispäivä').setValue(currentDateStr),
            lisääSuoritus.lisääSuoritus
          )
        }

        describe('Luokat 1-2', function () {
          lisääVuosiluokka('1')
          it('Esitäyttää pakolliset oppiaineet', function () {
            expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal([
              'Äidinkieli ja kirjallisuus,',
              'Matematiikka',
              'Ympäristöoppi',
              'Uskonto/Elämänkatsomustieto',
              'Musiikki',
              'Kuvataide',
              'Käsityö',
              'Liikunta',
              'Opinto-ohjaus'
            ])
            expect(S('.oppiaineet .oppiaine .kieli input').val()).to.equal(
              'Suomen kieli ja kirjallisuus'
            )
          })
        })

        describe('Luokat 3-6', function () {
          lisääVuosiluokka('3')
          it('Esitäyttää pakolliset oppiaineet', function () {
            expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal([
              'Äidinkieli ja kirjallisuus,',
              'A1-kieli,',
              'Matematiikka',
              'Ympäristöoppi',
              'Uskonto/Elämänkatsomustieto',
              'Historia',
              'Yhteiskuntaoppi',
              'Musiikki',
              'Kuvataide',
              'Käsityö',
              'Liikunta',
              'Opinto-ohjaus'
            ])
            expect(S('.oppiaineet .oppiaine .kieli input').val()).to.equal(
              'Suomen kieli ja kirjallisuus'
            )
          })
        })

        describe('Luokat 7-8', function () {
          lisääVuosiluokka('7')
          it('Esitäyttää pakolliset oppiaineet', function () {
            expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal([
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
            expect(S('.oppiaineet .oppiaine .kieli input').val()).to.equal(
              'Suomen kieli ja kirjallisuus'
            )
          })
        })

        describe('Luokka 9', function () {
          lisääVuosiluokka('9')
          it('Ei esitäytetä eikä näytetä oppiaineita', function () {
            expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal([])
            expect(opinnot.oppiaineet.isVisible()).to.equal(false)
          })
        })
      })
    })
  })

  describe('9. vuosiluokan oppilas', function () {
    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('160932-311V')
    )

    describe('Aluksi', function () {
      it('Näytetään 9. luokan suoritus, koska oppijalla ei ole päättötodistusta', function () {
        expect(opinnot.getTutkinto()).to.equal('9. vuosiluokka')
      })

      it('Oppiaineita ei näytetä', function () {
        expect(opinnot.oppiaineet.isVisible()).to.equal(false)
      })
    })

    describe('Muokattaessa', function () {
      before(editor.edit)

      it('Merkitse valmiiksi -nappia ei näytetä', function () {
        expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(false)
      })

      describe('Kun oppilas jää luokalle', function () {
        before(editor.property('jääLuokalle').setValue(true))

        describe('Käyttöliittymän tila', function () {
          it('Oppiaineet näytetään', function () {
            expect(opinnot.oppiaineet.isVisible()).to.equal(true)
          })

          it('Oppiaineet esitäytetään', function () {
            expect(
              textsOf(S('.oppiaineet .oppiaine .nimi')).length
            ).to.be.greaterThan(0)
          })
        })

        describe('Kun merkitään valmiiksi', function () {
          before(
            opinnot.oppiaineet.merkitseOppiaineetValmiiksi(),
            tilaJaVahvistus.merkitseValmiiksi
          )

          it('Siirretään seuraavalle luokalle -riviä ei näytetä', function () {
            expect(
              tilaJaVahvistus.merkitseValmiiksiDialog.editor
                .propertyBySelector('.jaa-tai-siirretaan')
                .isVisible()
            ).to.equal(false)
          })

          describe('Kun on merkitty valmiiksi', function () {
            before(
              opinnot.tilaJaVahvistus.lisääVahvistus('31.7.2020'),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )
            it('Tallennus onnistuu', function () { })

            describe('Kun poistetaan luokalleen jäänti ja merkitään jälleen keskeneräiseksi', function () {
              before(
                editor.edit,
                editor.property('jääLuokalle').setValue(false),
                tilaJaVahvistus.merkitseKeskeneräiseksi,
                editor.saveChanges
              )
              it('Tallennus onnistuu', function () { })
            })
          })
        })
      })

      describe('Kun oppilas ei jää luokalle', function () {
        before(editor.edit, editor.property('jääLuokalle').setValue(false))

        it('Merkitse valmiiksi -nappia ei näytetä', function () {
          expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(false)
        })

        describe('Perusopetuksen oppimäärän vahvistaminen', function () {
          before(
            opinnot.valitseSuoritus(undefined, 'Päättötodistus'),
            wait.forAjax,
            opinnot.oppiaineet
              .uusiOppiaine('.pakolliset')
              .selectValue('Matematiikka'),
            opinnot.oppiaineet
              .oppiaine(0)
              .property('yksilöllistettyOppimäärä')
              .setValue(true),
            opinnot.oppiaineet
              .oppiaine(0)
              .propertyBySelector('.arvosana')
              .selectValue('S'),
            opinnot.oppiaineet
              .oppiaine(0)
              .propertyBySelector('.sanallinen-arviointi .kuvaus')
              .setValue('Hienoa työtä'),
            editor.saveChanges
          )

          it('Ensin piilottaa oppiaineiden arvosanat (ja sanallisen arvioinnin)', function () {
            expect(extractAsText(S('.oppiaineet'))).to.equal(
              'Arviointiasteikko\nArvostelu 4-10, S (suoritettu) tai H (hylätty)\nYhteiset oppiaineet\nOppiaine\nMatematiikka *\n* = yksilöllistetty tai rajattu oppimäärä'
            )
          })

          describe('Kun merkitään valmiiksi', function () {
            before(
              editor.edit,
              tilaJaVahvistus.merkitseValmiiksi,
              tilaJaVahvistus.lisääVahvistus('31.7.2020'),
              editor.saveChanges
            )

            it('näyttää oppiaineiden arvosanat', function () {
              expect(extractAsText(S('.oppiaineet'))).to.equal(
                'Arviointiasteikko\nArvostelu 4-10, S (suoritettu) tai H (hylätty)\nYhteiset oppiaineet\nOppiaine Arvosana\nMatematiikka S *\nSanallinen arviointi Hienoa työtä\n* = yksilöllistetty tai rajattu oppimäärä'
              )
            })

            describe('Käyttöliittymän tila', function () {
              before(opinnot.valitseSuoritus(undefined, '9. vuosiluokka'))
              it('Merkitsee myös 9. vuosiluokan suorituksen valmiiksi', function () {
                expect(tilaJaVahvistus.tila()).to.equal('Suoritus valmis')
              })
            })

            describe('Kun palautetaan päättötodistus KESKEN-tilaan', function () {
              before(
                page.openPage,
                page.oppijaHaku.searchAndSelect('160932-311V'),
                editor.edit,
                tilaJaVahvistus.merkitseKeskeneräiseksi,
                editor.saveChanges
              )
              it('Pysytään päättötodistus -täbillä', function () {
                expect(opinnot.getTutkinto()).to.equal('Perusopetus')
              })
            })
          })
        })
      })
    })
  })

  describe('Perusopetuksen oppiaineen oppimäärän suoritus', function () {
    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('110738-839L')
    )
    describe('Kaikki tiedot näkyvissä', function () {
      it('näyttää opiskeluoikeuden tiedot', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal([
          'Jyväskylän normaalikoulu, Perusopetuksen oppiaineen oppimäärä (2008—2018, valmistunut)'
        ])
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 15.8.2008 — 4.6.2018\n' +
          'Tila 4.6.2018 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
          '15.8.2008 Läsnä (valtionosuusrahoitteinen koulutus)'
        )
      })

      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Oppiaine Äidinkieli ja kirjallisuus\n' +
          'Kieli Suomen kieli ja kirjallisuus\n' +
          'Peruste 19/011/2015\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Arvosana 9\n' +
          'Suoritustapa Erityinen tutkinto\n' +
          'Suorituskieli suomi\n' +
          'Suoritus valmis Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori'
        )
      })
    })

    describe('Monta oppiainetta', function () {
      before(page.openPage, page.oppijaHaku.searchAndSelect('131298-5248'))
      it('näyttää opiskeluoikeuden otsikon oikein', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal([
          'Jyväskylän normaalikoulu, Perusopetuksen oppiaineen oppimäärä (2008—2018, valmistunut)'
        ])
      })
    })

    describe('Tietojen muuttaminen', function () {
      var arvosana = editor.property('arviointi')

      before(page.openPage, page.oppijaHaku.searchAndSelect('110738-839L'))
      before(editor.edit, editor.property('tila').removeItem(0)) // opiskeluoikeus: läsnä

      describe('Kun arviointi poistetaan', function () {
        before(arvosana.setValue('Ei valintaa'), editor.saveChanges)

        it('Suoritus siirtyy tilaan KESKEN', function () {
          expect(tilaJaVahvistus.text()).to.equal('Suoritus kesken')
        })

        describe('Kun muokataan suoritusta', function () {
          before(editor.edit)

          it('Valmiiksi merkintä on estetty', function () {
            expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(false)
          })

          describe('Kun lisätään arvosana', function () {
            before(
              arvosana.setValue('8'),
              tilaJaVahvistus.merkitseValmiiksi,
              tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                .itemEditor(0)
                .setValue('Lisää henkilö'),
              tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                .itemEditor(0)
                .propertyBySelector('.nimi')
                .setValue('Reijo Reksi'),
              tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                .itemEditor(0)
                .propertyBySelector('.titteli')
                .setValue('rehtori'),
              tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
              editor.saveChanges
            )

            it('Valmiiksi merkintä on mahdollista', function () { })
          })
        })
      })
      describe('Kurssit', function () {
        describe('Kurssin lisääminen päättövaiheen kursseista', function () {
          var äidinkieli = Oppiaine(
            findSingle('.perusopetuksenoppiaineenoppimaaransuoritus')
          )

          before(
            editor.edit,
            tilaJaVahvistus.merkitseKeskeneräiseksi,
            äidinkieli.avaaLisääKurssiDialog
          )
          it('Näytetään vain oikean oppiaineen kurssit', function () {
            expect(äidinkieli.lisääKurssiDialog.kurssit().length).to.equal(11)
          })

          describe('Kun lisätään kurssi', function () {
            before(
              äidinkieli.lisääKurssiDialog.valitseKurssi(
                'Uutisia ja mielipiteitä'
              ),
              äidinkieli.lisääKurssiDialog.lisääKurssi
            )

            describe('Kun annetaan arvosana ja tallennetaan', function () {
              before(
                äidinkieli.kurssi('S21').arvosana.setValue('8'),
                editor.saveChanges
              )

              it('toimii', function () { })
            })
          })
        })

        describe('Kurssin lisääminen alkuvaiheen kursseista', function () {
          var äidinkieli = Oppiaine(
            findSingle('.perusopetuksenoppiaineenoppimaaransuoritus')
          )

          before(editor.edit, äidinkieli.avaaAlkuvaiheenLisääKurssiDialog)
          it('Näytetään kaikki alkuvaiheen äidinkielen kurssit', function () {
            expect(äidinkieli.lisääKurssiDialog.kurssit().length).to.equal(52)
          })

          describe('Kun lisätään kurssi', function () {
            before(
              äidinkieli.lisääKurssiDialog.valitseKurssi(
                'Kehittyvä kielitaito: Asuminen'
              ),
              äidinkieli.lisääKurssiDialog.lisääKurssi
            )

            describe('Kun annetaan arvosana ja tallennetaan', function () {
              before(
                äidinkieli.kurssi('AS211').arvosana.setValue('5'),
                editor.saveChanges
              )

              it('toimii', function () { })
            })
          })
        })
      })
    })
  })

  describe('Perusopetuksen useamman oppiaineen aineopiskelija', function () {
    describe('Opiskeluoikeuden tilaa', function () {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('131298-5248'),
        editor.edit,
        editor.property('tila').removeItem(0),
        opinnot.valitseSuoritus(undefined, 'Äidinkieli ja kirjallisuus'),
        opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
        opinnot.valitseSuoritus(undefined, 'Yhteiskuntaoppi'),
        opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
        opinnot.avaaLisaysDialogi
      )

      it('ei voida merkitä valmiiksi', function () {
        expect(OpiskeluoikeusDialog().radioEnabled('valmistunut')).to.equal(
          false
        )
      })

      describe('Kun yksikin suoritus merkitään valmiiksi', function () {
        before(
          opinnot.tilaJaVahvistus.merkitseValmiiksi,
          opinnot.tilaJaVahvistus.lisääVahvistus('01.01.2000'),
          opinnot.avaaLisaysDialogi,
          OpiskeluoikeusDialog().tila().aseta('valmistunut'),
          OpiskeluoikeusDialog().opintojenRahoitus().aseta('1'),
          OpiskeluoikeusDialog().tallenna,
          editor.saveChanges
        )

        it('myös opiskeluoikeuden tila voidaan merkitä valmiiksi', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.contain(
            'Valmistunut'
          )
        })
      })

      after(editor.cancelChanges)
    })

    describe('Jos opiskelijalla on "ei tiedossa"-oppiaineita', function () {
      var lisääSuoritus = opinnot.lisääSuoritusDialog
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('131298-5248'),
        editor.edit,
        editor.property('tila').removeItem(0),
        lisääSuoritus.open('lisää oppiaineen suoritus'),
        wait.forAjax,
        lisääSuoritus.property('tunniste').setValue('Ei tiedossa'),
        lisääSuoritus.toimipiste.select('Jyväskylän normaalikoulu, alakoulu'),
        lisääSuoritus.lisääSuoritus,
        opinnot.avaaLisaysDialogi
      )

      it('Opiskeluoikeuden tilaa ei voi merkitä valmiiksi', function () {
        expect(OpiskeluoikeusDialog().radioEnabled('valmistunut')).to.equal(
          false
        )
      })

      after(editor.cancelChanges)
    })
  })
})
