describe('International school', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var tilaJaVahvistus = opinnot.tilaJaVahvistus
  var addOppija = AddOppijaPage()
  var opiskeluoikeus = OpiskeluoikeusDialog()
  var editor = opinnot.opiskeluoikeusEditor()

  before(Authentication().login(), resetFixtures)

  describe('International school', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('170186-854H'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
        'internationalschool'
      )
    )
    describe('Opiskeluoikeuden tiedot', function () {
      it('näytetään', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 15.8.2004 — 30.6.2018\n' +
            'Tila 30.6.2018 Valmistunut\n15.8.2004 Läsnä\nLisätiedot'
        )

        expect(
          opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()
        ).to.deep.equal([
          'International school diploma vuosiluokka 2004—2018, Valmistunut'
        ])
      })

      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Grade 12 (IB Diploma)\n' +
            'Luokka 12C\n' +
            'Alkamispäivä 15.8.2017\n' +
            'Oppilaitos / toimipiste International School of Helsinki\n' +
            'Suoritus valmis Vahvistus : 30.6.2018 Helsinki Reijo Reksi , rehtori'
        )
      })

      it('näyttää oppiaineiden arvosanat', function () {
        expect(extractAsText(S('.oppiaineet'))).to.equal(
          'Oppiaine Arvosana\n' +
            'Language A: literature, suomi 6\n' +
            'B-language, suomi 6\n' +
            'Psychology 6\n' +
            'Chemistry 4\n' +
            'Biology 5\n' +
            'Mathematics: Applications and Interpretation 4\n' +
            'Fitness and Well-Being Fail\n' +
            'Theory of knowledge C'
        )
      })
    })
  })

  describe('Tietojen muuttaminen', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('170186-854H'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
        'internationalschool'
      )
    )

    describe('Suoritusten tiedot', function () {
      describe('Oppiaineen kielen valinta, Diploma-suoritus', function () {
        describe('kielivalinnan muuttaminen', function () {
          var kieli = opinnot.oppiaineet
            .oppiaine('B')
            .propertyBySelector('.oppiaine')
          before(editor.edit, kieli.selectValue('englanti'), editor.saveChanges)
          it('muutettu kielivalinta näytetään', function () {
            expect(kieli.getText()).to.equal('B-language, englanti')
          })
        })
        describe('kielien järjestys listassa', function () {
          before(editor.edit)
          it('on oikein', function () {
            expect(textsOf(S('.B .oppiaine .options li'))).to.deep.equal([
              'suomi',
              'englanti',
              'espanja',
              'ranska'
            ])
          })
          after(editor.cancelChanges)
        })
      })

      describe('Oppiaineen kielen valinta, MYP-suoritus', function () {
        before(opinnot.valitseSuoritus(undefined, 'Grade 9'), editor.edit)
        describe('kielivalinnan muuttaminen', function () {
          var kieli = opinnot.oppiaineet
            .oppiaine('LL')
            .propertyBySelector('.oppiaine')
          before(kieli.selectValue('suomi'), editor.saveChanges)
          it('muutettu kielivalinta näytetään', function () {
            expect(kieli.getText()).to.equal('Language and Literature, suomi')
          })
        })
      })

      describe('Oppiaineen arvosanan muutos', function () {
        var languageAndLiterature = opinnot.oppiaineet.oppiaine(0)
        var arvosana = languageAndLiterature.propertyBySelector('td.arvosana')
        before(opinnot.valitseSuoritus(undefined, 'Grade 9'))

        describe('Kun annetaan numeerinen arvosana', function () {
          before(editor.edit, arvosana.selectValue('5'), editor.saveChanges)

          it('muutettu arvosana näytetään', function () {
            expect(arvosana.getValue()).to.equal('5')
          })
        })
      })

      describe('Oppiaineen', function () {
        var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine('.uusi-oppiaine')
        var economics = editor.subEditor('.ECO')
        before(
          opinnot.valitseSuoritus(undefined, 'Grade 12'),
          editor.edit,
          uusiOppiaine.selectValue('Economics'),
          economics.propertyBySelector('.arvosana').selectValue('7'),
          editor.saveChangesAndWaitForSuccess
        )

        describe('Lisääminen', function () {
          it('Uusi oppiaine näytetään', function () {
            expect(extractAsText(S('.oppiaineet'))).to.contain('Economics')
          })
        })

        describe('Poistaminen', function () {
          before(
            editor.edit,
            economics.propertyBySelector('.remove-row').removeValue,
            editor.saveChangesAndWaitForSuccess
          )
          it('toimii', function () {
            expect(extractAsText(S('.oppiaineet'))).to.not.contain('Economics')
          })
        })
      })

      describe('Theory of knowledge', function () {
        var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine('.uusi-oppiaine')
        var tok = editor.subEditor('.TOK')
        before(opinnot.valitseSuoritus(undefined, 'Grade 12'))

        describe('Poistaminen', function () {
          before(
            editor.edit,
            tok.propertyBySelector('.remove-row').removeValue,
            editor.saveChangesAndWaitForSuccess
          )

          it('toimii', function () {
            expect(extractAsText(S('.oppiaineet'))).to.not.contain(
              'Theory of knowledge'
            )
          })
        })

        describe('Lisääminen', function () {
          before(
            editor.edit,
            uusiOppiaine.selectValue('Theory of knowledge'),
            tok.propertyBySelector('.arvosana').selectValue('A'),
            editor.saveChangesAndWaitForSuccess
          )

          it('Uusi oppiaine näytetään', function () {
            expect(extractAsText(S('.oppiaineet'))).to.contain(
              'Theory of knowledge'
            )
          })
        })
      })
    })

    describe('Päätason suorituksen poistaminen', function () {
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

            it('Päätason suoritus poistetaan', function () {})

            describe('Poistettua päätason suoritusta', function () {
              before(
                wait.until(page.isReady),
                opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
                  'internationalschool'
                )
              )

              it('Ei näytetä', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Grade 11',
                  'Grade 10',
                  'Grade 9',
                  'Grade 8',
                  'Grade 7',
                  'Grade 6',
                  'Grade 5',
                  'Grade 4',
                  'Grade 3',
                  'Grade 2',
                  'Grade 1',
                  'Grade explorer'
                ])
              })
            })
          })
        })
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
      before(addOppija.enterValidDataInternationalSchool())

      describe('Käyttöliittymän tila', function () {
        it('Lisää-nappi on enabloitu', function () {
          expect(addOppija.isEnabled()).to.equal(true)
        })

        it('Vaihtoehtoina on lukion opintojenRahoitus-vaihtoehdot', function () {
          expect(addOppija.opintojenRahoitukset()).to.deep.equal([
            'Valtionosuusrahoitteinen koulutus',
            'Muuta kautta rahoitettu'
          ])
        })

        it('Ei näytetä opintojen rahoitus -kenttää', function () {
          expect(addOppija.rahoitusIsVisible()).to.equal(true)
        })

        it('Näytetään oikeat tilavaihtoehdot', function () {
          expect(addOppija.opiskeluoikeudenTilat()).to.deep.equal([
            'Eronnut',
            'Läsnä',
            'Valmistunut',
            'Väliaikaisesti keskeytynyt'
          ])
        })
      })

      describe('Kun painetaan Lisää-nappia', function () {
        before(
          addOppija.submitAndExpectSuccess(
            'Tyhjä, Tero (230872-7258)',
            'Grade explorer'
          )
        )

        it('lisätty oppija näytetään', function () {})

        describe('Käyttöliittymän tila', function () {
          it('Lisätty opiskeluoikeus näytetään', function () {
            expect(opinnot.getTutkinto()).to.equal('Grade explorer (PYP)')
            expect(opinnot.getOppilaitos()).to.equal(
              'International School of Helsinki'
            )
          })
        })
      })
    })
  })

  describe('Opiskeluoikeuden lisääminen diplomaluokalle', function () {
    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataInternationalSchool({ grade: 'Grade 12' }),
      addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Grade 12')
    )

    it('toimii', function () {})
  })

  describe('Vuosiluokan suorituksen lisääminen', function () {
    var lisääSuoritus = opinnot.lisääSuoritusDialog

    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataInternationalSchool(),
      addOppija.submitAndExpectSuccess(
        'Tyhjä, Tero (230872-7258)',
        'Grade explorer'
      ),
      editor.edit
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
          expect(opinnot.suoritusTabs()).to.deep.equal(['Grade explorer'])
        })
      })
      describe('Grade 1 (PYP)', function () {
        before(lisääSuoritus.open('lisää vuosiluokan suoritus'))
        describe('Aluksi', function () {
          it('Valitsee automaattisesti pienimmän puuttuvan luokka-asteen', function () {
            expect(
              lisääSuoritus.property('international-school-grade').getValue()
            ).to.equal('Grade 1')
          })
        })
        describe('Kun painetaan Lisää-nappia', function () {
          before(lisääSuoritus.lisääSuoritus)
          describe('Käyttöliittymän tila', function () {
            it('Näytetään uusi suoritus', function () {
              expect(opinnot.suoritusTabs()).to.deep.equal([
                'Grade explorer',
                'Grade 1'
              ])
            })
            it('Uusi suoritus on valittuna', function () {
              expect(opinnot.getTutkinto()).to.equal('Grade 1 (PYP)')
            })
            it('Toimipiste on oikein', function () {
              expect(editor.property('toimipiste').getValue()).to.equal(
                'International School of Helsinki'
              )
            })
          })

          describe('Vuosiluokan merkitseminen valmiiksi', function () {
            var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine('.uusi-oppiaine')
            var sciences = editor.subEditor('.SCI')
            before(
              editor.edit,
              uusiOppiaine.selectValue('Science'),
              editor.saveChangesAndWaitForSuccess
            )
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
                editor.edit,
                opinnot.oppiaineet.merkitseOppiaineetValmiiksi(
                  'Achieved Outcomes'
                )
              )
              describe('Aluksi', function () {
                it('Merkitse valmiiksi -nappi näytetään', function () {
                  expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(
                    true
                  )
                })
              })
              describe('Kun merkitään valmiksi', function () {
                before(
                  tilaJaVahvistus.merkitseValmiiksi,
                  dialog.editor.property('päivä').setValue('11.4.2017'),
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
                    ).to.equal('Helsinki')
                  })
                })

                describe('Kun painetaan Merkitse valmiiksi -nappia', function () {
                  before(
                    dialog.editor.property('paikkakunta').setValue('Helsinki'),
                    dialog.merkitseValmiiksi
                  )

                  describe('Käyttöliittymän tila', function () {
                    it('Tila on "valmis" ja vahvistus näytetään', function () {
                      expect(tilaJaVahvistus.text()).to.equal(
                        'Suoritus valmis Vahvistus : 11.4.2017 Helsinki Reijo Reksi , rehtori'
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
                      expect(tilaJaVahvistus.text()).to.equal('Suoritus kesken')
                    })
                  })
                })
              })
            })
          })
        })
      })

      describe('Grade 9 (MYP)', function () {
        before(
          lisääSuoritus.open('lisää vuosiluokan suoritus'),
          lisääSuoritus.selectTutkinto(
            'Grade 9',
            '.international-school-grade'
          ),
          lisääSuoritus.lisääSuoritus
        )

        it('Oppiainevalinnat', function () {
          expect(
            Page(S('.oppiaineet')).getInputOptions('.uusi-oppiaine .dropdown')
          ).to.deep.equal([
            'Language Acquisition',
            'Language and Literature',
            'Advisory',
            'Design',
            'Drama',
            'English as an additional language',
            'Extended Mathematics',
            'Independent language studies',
            'Individuals and Societies',
            'Integrated Studies',
            'Math Foundations',
            'Mathematics',
            'Media',
            'Music',
            'Personal project',
            'Physical and Health Education',
            'Sciences',
            'Standard Mathematics',
            'Visual Art'
          ])
        })
      })

      describe('Grade 12 (Diploma)', function () {
        before(
          lisääSuoritus.open('lisää vuosiluokan suoritus'),
          lisääSuoritus.selectTutkinto(
            'Grade 12',
            '.international-school-grade'
          ),
          lisääSuoritus.lisääSuoritus
        )

        it('Oppiainevalinnat', function () {
          expect(
            Page(S('.oppiaineet')).getInputOptions('.uusi-oppiaine .dropdown')
          ).to.deep.equal([
            'Creativity, activity, service',
            'Extended essay',
            'Theory of knowledge',
            'Fitness and Well-Being',
            'Film',
            'HS Consumer Math 11/12',
            'Information Technology in a Global Society',
            'Integrated Studies',
            'Mathematics: Analysis and Approaches',
            'Mathematics: Applications and Interpretation',
            'B-language',
            'Language A: language and literature',
            'Language A: literature',
            'ab initio',
            'Biology',
            'Chemistry',
            'Economics',
            'Environmental systems and societies',
            'History',
            'Mathematical studies',
            'Mathematics',
            'Physics',
            'Psychology',
            'Visual arts'
          ])
        })
      })
    })
  })
})
