describe('IB', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()
  var opiskeluoikeus = OpiskeluoikeusDialog()
  var addOppija = AddOppijaPage()
  before(Authentication().login(), resetFixtures)

  describe('Opiskeluoikeuden tiedot', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('040701-432D'))
    it('näytetään oikein', function () {
      expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
        'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 4.6.2016\n' +
          'Tila 4.6.2016 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
          '1.9.2012 Läsnä (valtionosuusrahoitteinen koulutus)'
      )
    })
  })

  describe('Pre-IB', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('040701-432D'),
      opinnot.valitseSuoritus(undefined, 'Pre-IB')
    )

    describe('Oppijan suorituksissa', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto()).to.equal('Pre-IB')
        expect(opinnot.getOppilaitos()).to.equal('Ressun lukio')
      })
    })

    describe('Kaikki tiedot näkyvissä', function () {
      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Pre-IB\n' +
            'Oppilaitos / toimipiste Ressun lukio\n' +
            'Suorituskieli englanti\n' +
            'Suoritus valmis Vahvistus : 4.6.2016 Helsinki Reijo Reksi , rehtori'
        )
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function () {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Laajuus (kurssia) Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus\n' +
            'ÄI1\n8 ÄI2\n8 ÄI3\n8 3 8\n' +
            'A1-kieli, englanti\nENA1\n10 ENA2\n10 ENA5\n10 3 10\n' +
            'B1-kieli, ruotsi\nRUB11\n8 RUB12\n7 2 7\n' +
            'B2-kieli, ranska\nRAN3 *\n9 1 9\n' +
            'B3-kieli, espanja\nES1 *\nS 1 6\n' +
            'Matematiikka, pitkä oppimäärä\nMAA11\n7 MAA12\n7 MAA13\n7 MAA2\n7 4 7\n' +
            'Biologia\nBI1\n8 BI10 *\nS 2 8\n' +
            'Maantieto\nGE2\n10 1 10\n' +
            'Fysiikka\nFY1\n7 1 7\n' +
            'Kemia\nKE1\n8 1 8\n' +
            'Uskonto/Elämänkatsomustieto\nUK4\n10 1 10\n' +
            'Filosofia\nFI1\nS 1 7\n' +
            'Psykologia\nPS1\n8 1 8\n' +
            'Historia\nHI3\n9 HI4\n8 HI10 *\nS 3 8\n' +
            'Yhteiskuntaoppi\nYH1\n8 1 8\n' +
            'Liikunta\nLI1\n8 1 8\n' +
            'Musiikki\nMU1\n8 1 8\n' +
            'Kuvataide\nKU1\n9 1 9\n' +
            'Terveystieto\nTE1\n7 1 7\n' +
            'Opinto-ohjaus\nOP1\nS 1 7\n' +
            'Teemaopinnot\nMTA *\nS 1 S\n' +
            'Suoritettujen kurssien määrä yhteensä: 32\n' +
            '* = paikallinen kurssi tai oppiaine'
        )
      })
    })

    describe('Tietojen muuttaminen', function () {
      describe('Suoritusten tiedot', function () {
        describe('Oppiaine', function () {
          var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine()

          describe('Lukion oppiaine', function () {
            var aine = opinnot.oppiaineet.oppiaine('oppiaine.BI')
            var arvosana = aine.propertyBySelector('td.arvosana')

            describe('Arvosana-asteikko', function () {
              before(editor.edit)

              it('on oikea', function () {
                expect(arvosana.getOptions()).to.deep.equal([
                  'Ei valintaa',
                  '4',
                  '5',
                  '6',
                  '7',
                  '8',
                  '9',
                  '10',
                  'H',
                  'O',
                  'S'
                ])
              })

              after(editor.cancelChanges)
            })

            describe('Arvosanan muuttaminen', function () {
              before(
                editor.edit,
                arvosana.selectValue(5),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(
                  findSingle('.oppiaine.BI .arvosana .annettuArvosana')().text()
                ).to.equal('5')
              })
            })

            describe('Oppiaineen kurssi', function () {
              describe('Arvosanan muuttaminen', function () {
                var kurssi = aine.kurssi('BI10')

                before(
                  editor.edit,
                  kurssi.arvosana.selectValue('5'),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('toimii', function () {
                  expect(kurssi.arvosana.getText()).to.equal('5')
                })
              })

              describe('Kurssivaihtoehdot', function () {
                before(editor.edit, aine.avaaLisääKurssiDialog)

                it('sisältää valtakunnalliset lukion kurssit', function () {
                  expect(aine.lisääKurssiDialog.kurssit()).to.include(
                    'BI2 Ekologia ja ympäristö'
                  )
                })

                after(editor.cancelChanges)
              })

              describe('Paikallisen lukion kurssin', function () {
                describe('lisäyksessä', function () {
                  var dialog = aine.lisääKurssiDialog

                  before(
                    editor.edit,
                    aine.avaaLisääKurssiDialog,
                    aine.lisääKurssiDialog.valitseKurssi('paikallinen')
                  )

                  it('tyyppi kysytään', function () {
                    expect(dialog.hasKurssinTyyppi()).to.equal(true)
                  })

                  after(dialog.sulje, editor.cancelChanges)
                })

                describe('lisääminen', function () {
                  before(
                    editor.edit,
                    aine.lisääPaikallinenKurssi('PAIK'),
                    aine.kurssi('PAIK').arvosana.selectValue('8'),
                    editor.saveChanges,
                    wait.until(page.isSavedLabelShown)
                  )

                  it('toimii', function () {
                    expect(extractAsText(S('.oppiaineet .BI'))).to.contain(
                      'PAIK'
                    )
                  })
                })

                describe('Poistaminen', function () {
                  before(
                    editor.edit,
                    aine.kurssi('PAIK').poistaKurssi,
                    editor.saveChanges,
                    wait.until(page.isSavedLabelShown)
                  )

                  it('toimii', function () {
                    expect(extractAsText(S('.oppiaineet .AI'))).to.not.contain(
                      'PAIK'
                    )
                  })
                })
              })
            })
          })

          describe('Lukion kieliaine', function () {
            before(editor.edit)

            var aine = opinnot.oppiaineet.oppiaine('oppiaine.AI')
            var kieli = aine.propertyBySelector('.title .properties:eq(0)')
            var arvosana = aine.propertyBySelector('td.arvosana')

            describe('Alkutila', function () {
              it('on oikein', function () {
                expect(editor.canSave()).to.equal(false)
                expect(kieli.getValue()).to.equal(
                  'Suomen kieli ja kirjallisuus'
                )
                expect(arvosana.getValue()).to.equal('8')
              })
            })

            describe('Kielen muuttaminen', function () {
              before(kieli.selectValue('Muu oppilaan äidinkieli'))

              it('onnistuu', function () {
                expect(kieli.getValue()).to.equal('Muu oppilaan äidinkieli')
              })

              it('tallennus on mahdollista', function () {
                expect(editor.canSave()).to.equal(true)
              })

              after(editor.cancelChanges)
            })
          })

          describe('Lukion paikallinen aine', function () {
            before(editor.edit)

            var aine = editor.subEditor('.oppiaine.oppiaine-rivi:last')

            it('alkutila', function () {
              expect(editor.canSave()).to.equal(false)
              expect(editor.getEditBarMessage()).to.equal(
                'Ei tallentamattomia muutoksia'
              )
              expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(21)
            })

            describe('Lisääminen', function () {
              before(editor.edit, uusiOppiaine.selectValue('Lisää'))

              it('lisää oppiaineen', function () {
                expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(22)
              })

              it('estää tallennuksen kunnes pakolliset tiedot on täytetty', function () {
                expect(editor.canSave()).to.equal(false)
                expect(editor.getEditBarMessage()).to.equal(
                  'Korjaa virheelliset tiedot.'
                )
              })

              describe('Tiedot täytettynä', function () {
                before(
                  aine.propertyBySelector('.koodi').setValue('PAI'),
                  aine
                    .propertyBySelector('.nimi')
                    .setValue('Paikallinen oppiaine'),
                  aine
                    .propertyBySelector('.kuvaus')
                    .setValue('Pakollinen kuvaus'),
                  aine.propertyBySelector('.arvosana').selectValue(9),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('tallennus toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.contain(
                    'Paikallinen oppiaine'
                  )
                })
              })
            })

            describe('Poistaminen', function () {
              before(
                editor.edit,
                aine.propertyBySelector('.remove-row').removeValue,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.not.contain(
                  'Paikallinen oppiaine'
                )
              })
            })

            describe('Lisätty paikallinen oppiaine', function () {
              before(editor.edit)

              it('tallettuu organisaation preferenceihin', function () {
                expect(uusiOppiaine.getOptions()).to.contain(
                  'Paikallinen oppiaine'
                )
              })
            })

            describe('Organisaation preferenceistä löytyvä aine', function () {
              describe('Lisääminen', function () {
                before(
                  editor.edit,
                  uusiOppiaine.selectValue('Paikallinen oppiaine'),
                  aine.propertyBySelector('.arvosana').selectValue(9),
                  editor.saveChanges
                )

                it('toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.contain(
                    'Paikallinen oppiaine'
                  )
                })
              })

              describe('Poistaminen', function () {
                before(
                  editor.edit,
                  aine.propertyBySelector('.remove-row').removeValue,
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.not.contain(
                    'Paikallinen oppiaine'
                  )
                })
              })
            })
          })

          describe('Muu IB-aine', function () {
            before(editor.edit)

            var aine = opinnot.oppiaineet.oppiaine('oppiaine.CHE')
            var arvosana = aine.propertyBySelector('td.arvosana')

            it('alkutila', function () {
              expect(editor.canSave()).to.equal(false)
              expect(editor.getEditBarMessage()).to.equal(
                'Ei tallentamattomia muutoksia'
              )
              expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(21)
            })

            describe('Lisääminen', function () {
              before(
                editor.edit,
                uusiOppiaine.selectValue('Chemistry'),
                aine.propertyBySelector('.arvosana').selectValue(9)
              )

              it('lisää oppiaineen', function () {
                expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(22)
              })

              it('estää tallennuksen kunnes aineryhmä on valittu', function () {
                expect(editor.canSave()).to.equal(false)
                expect(editor.getEditBarMessage()).to.equal(
                  'Korjaa virheelliset tiedot.'
                )
              })

              describe('Kun aineryhmä on valittu', function () {
                before(
                  aine
                    .propertyBySelector('.ryhmä')
                    .setValue('Experimental sciences'),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('tallennus toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.contain(
                    'Chemistry'
                  )
                })
              })

              describe('Arvosana-asteikko', function () {
                before(editor.edit)

                it('on oikea', function () {
                  expect(arvosana.getOptions()).to.deep.equal([
                    'Ei valintaa',
                    '4',
                    '5',
                    '6',
                    '7',
                    '8',
                    '9',
                    '10',
                    'H',
                    'O',
                    'S'
                  ])
                })

                after(editor.cancelChanges)
              })

              describe('Oppiaineen kurssi', function () {
                describe('Kurssivaihtoehdot', function () {
                  before(editor.edit, aine.avaaLisääKurssiDialog)

                  it('sisältää valtakunnalliset lukion kurssit', function () {
                    expect(aine.lisääKurssiDialog.kurssit()).to.include(
                      'BI2 Ekologia ja ympäristö'
                    )
                  })

                  after(editor.cancelChanges)
                })

                describe('Paikallisen IB-kurssin', function () {
                  describe('lisäyksessä', function () {
                    var dialog = aine.lisääKurssiDialog

                    before(
                      editor.edit,
                      aine.avaaLisääKurssiDialog,
                      aine.lisääKurssiDialog.valitseKurssi('paikallinen')
                    )

                    it('tyyppiä ei kysytä', function () {
                      expect(dialog.hasKurssinTyyppi()).to.equal(false)
                    })

                    after(dialog.sulje, editor.cancelChanges)
                  })

                  describe('lisääminen', function () {
                    before(
                      editor.edit,
                      aine.lisääPaikallinenKurssi('PAIK'),
                      aine.kurssi('PAIK').arvosana.selectValue('8'),
                      editor.saveChanges,
                      wait.until(page.isSavedLabelShown)
                    )

                    it('toimii', function () {
                      expect(extractAsText(S('.oppiaineet .CHE'))).to.contain(
                        'PAIK'
                      )
                    })
                  })

                  describe('Poistaminen', function () {
                    before(
                      editor.edit,
                      aine.kurssi('PAIK').poistaKurssi,
                      editor.saveChanges,
                      wait.until(page.isSavedLabelShown)
                    )

                    it('toimii', function () {
                      expect(
                        extractAsText(S('.oppiaineet .AI'))
                      ).to.not.contain('PAIK')
                    })
                  })
                })
              })

              describe('Poistaminen', function () {
                before(
                  editor.edit,
                  aine.propertyBySelector('.remove-row').removeValue,
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.not.contain(
                    'Chemistry'
                  )
                })
              })
            })

            describe('IB-kieliaine', function () {
              before(
                editor.edit,
                editor.property('tila').removeItem(0),
                opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi
              )

              var aine = opinnot.oppiaineet.oppiaine('oppiaine.B')
              var kieli = aine.propertyBySelector(
                '.title .properties:eq(0) > .dropdown-wrapper'
              )

              describe('Lisääminen', function () {
                before(
                  editor.edit,
                  uusiOppiaine.selectValue('B-language'),
                  aine.propertyBySelector('.arvosana').selectValue(9)
                )

                it('lisää oppiaineen', function () {
                  expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(22)
                })

                it('estää tallennuksen kunnes aineryhmä ja kieli on valittu', function () {
                  expect(editor.canSave()).to.equal(false)
                  expect(editor.getEditBarMessage()).to.equal(
                    'Korjaa virheelliset tiedot.'
                  )
                })

                describe('Kun kieli on valittu', function () {
                  before(kieli.setValue('ranska'))

                  it('tallennus ei vielä onnistu', function () {
                    expect(editor.canSave()).to.equal(false)
                    expect(editor.getEditBarMessage()).to.equal(
                      'Korjaa virheelliset tiedot.'
                    )
                  })
                })

                describe('Kun aineryhmä on valittu', function () {
                  before(
                    aine
                      .propertyBySelector('.ryhmä')
                      .setValue('Studies in language and literature'),
                    editor.saveChanges,
                    wait.until(page.isSavedLabelShown)
                  )

                  it('tallennus toimii', function () {
                    expect(extractAsText(S('.oppiaineet'))).to.contain(
                      'B-language'
                    )
                  })
                })
              })

              describe('Kielen muuttaminen', function () {
                before(editor.edit, kieli.selectValue('englanti'))

                it('onnistuu', function () {
                  expect(kieli.getValue()).to.equal('englanti')
                })

                it('tallennus on mahdollista', function () {
                  expect(editor.canSave()).to.equal(true)
                })
              })
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
        })
      })
    })
  })

  describe('Pre-IB 2019', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('180300A8736'),
      opinnot.valitseSuoritus(undefined, 'Pre-IB 2019')
    )

    describe('Oppijan suorituksissa', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto()).to.equal('Pre-IB 2019')
        expect(opinnot.getOppilaitos()).to.equal('Ressun lukio')
      })
    })

    describe('Kaikki tiedot näkyvissä', function () {
      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Pre-IB 2019\n' +
            'Oppilaitos / toimipiste Ressun lukio\n' +
            'Suorituskieli englanti\n' +
            'Lukion oppimäärää täydentävät oman äidinkielen opinnot Arvosana 8\n' +
            'Kieli saame, lappi\n' +
            'Laajuus 3 op\n' +
            'Osasuoritukset Kurssi Suorituskieli Arviointi\n' +
            'Kieli- ja tekstitietoisuus\n' +
            'Laajuus 1 op pohjoissaame Arvosana O\n' +
            'Arviointipäivä 30.8.2019\n' +
            'Vuorovaikutus 1\n' +
            'Laajuus 1 op pohjoissaame Arvosana O\n' +
            'Arviointipäivä 30.8.2019\n' +
            'Tekstien tulkinta ja kirjoittaminen\n' +
            'Laajuus 1 op pohjoissaame Arvosana O\n' +
            'Arviointipäivä 30.8.2019\n' +
            'Puhvi-koe Arvosana 7\n' +
            'Arviointipäivä 30.8.2019\n' +
            'Suullisen kielitaidon kokeet Kieli englanti\n' +
            'Arvosana 6\n' +
            'Taitotaso B1.1\n' +
            'Arviointipäivä 3.9.2019\n' +
            'Kieli espanja\n' +
            'Arvosana S\n' +
            'Taitotaso Yli C1.1\n' +
            'Kuvaus Puhetaito äidinkielen tasolla\n' +
            'Arviointipäivä 3.9.2019\n' +
            'Todistuksella näkyvät lisätiedot Suorittanut etäopetuskokeiluna\n' +
            'Ryhmä AH\n' +
            'Suoritus valmis Vahvistus : 4.6.2016 Helsinki Reijo Reksi , rehtori'
        )
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function () {
        expect(
          extractAsText(S('.preibsuoritus2019 > .osasuoritukset'))
        ).to.equal(
          'Oppiaine Arvioitu (opintopistettä) Hyväksytysti arvioitu (opintopistettä) Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus, valinnainen\n' +
            'ÄI1\n' +
            '8 ÄI2\n' +
            '8 4 4 9\n' +
            'Matematiikka, lyhyt oppimäärä\n' +
            'MAB2\n' +
            '10 MAB3\n' +
            '10 4 4 10\n' +
            'Uskonto/Elämänkatsomustieto\n' +
            'UK1\n' +
            '9 2 2 9\n' +
            'Liikunta\n' +
            'LI2\n' +
            '8 LITT1 *\n' +
            'S 3 3 8\n' +
            'Fysiikka 0 0 8\n' +
            'Kemia\n' +
            'KE1\n' +
            '6 2 2 7\n' +
            'A-kieli, englanti\n' +
            'ENA1\n' +
            '10 ENA2\n' +
            '9 4 4 9\n' +
            'A-kieli, espanja\n' +
            'VKA1\n' +
            '6 VKA2\n' +
            '7 4 4 6\n' +
            'Tanssi ja liike, valinnainen *\n' +
            'ITT234 *\n' +
            '6 ITT235 *\n' +
            '7 2 2 6\n' +
            'Muut suoritukset\n' +
            'ÄI1\n' +
            '7 VKAAB31\n' +
            '6 RUB11\n' +
            '6 6 6\n' +
            'Lukiodiplomit\n' +
            'KULD2\n' +
            '9 2 2\n' +
            'Teemaopinnot\n' +
            'HAI765 *\n' +
            'S 1 1\n' +
            'Arvioitujen osasuoritusten laajuus yhteensä: 34,0 Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä: 34,0\n' +
            '* = paikallinen opintojakso tai oppiaine'
        )
      })
    })
  })

  describe('IB-tutkinto', function () {
    before(
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('040701-432D')
    )
    describe('Oppijan suorituksissa', function () {
      it('näytetään', function () {
        expect(opinnot.getTutkinto()).to.equal(
          'IB-tutkinto (International Baccalaureate)'
        )
        expect(opinnot.getOppilaitos()).to.equal('Ressun lukio')
      })
    })

    describe('Kaikki tiedot näkyvissä', function () {
      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus IB-tutkinto (International Baccalaureate)\n' +
            'Oppilaitos / toimipiste Ressun lukio\n' +
            'Suorituskieli englanti\n' +
            'Theory of knowledge A\n' +
            'TOK1\nS TOK2\nS\n' +
            'Extended essay B\n' +
            'Creativity action service S\n' +
            'Lisäpisteet 3\n' +
            'Suoritus valmis Vahvistus : 4.6.2016 Helsinki Reijo Reksi , rehtori'
        )
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function () {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Laajuus (kurssia) Predicted grade Päättöarvosana\n' +
            'Studies in language and literature\n' +
            'Language A: literature, suomi\nFIN_S1\n4 FIN_S2\n4 FIN_S3\nS FIN_S4\n5 FIN_S5\n6 FIN_S6\n5 FIN_S7\n5 FIN_S8\nS FIN_S9\n5 9 4 4\n' +
            'Language A: language and literature, englanti\nENG_B_H1\n6 ENG_B_H2\n7 ENG_B_H4\nS ENG_B_H5\n6 ENG_B_H6\n6 ENG_B_H8\n5 6 6 7\n' +
            'Individuals and societies\n' +
            'History\nHIS_H3\n6 HIS_H4\n6 HIS_H5\n7 HIS_H6\n6 HIS_H7\n1 HIS_H9\nS 6 6 6\n' +
            'Psychology\nPSY_S1\n6 PSY_S2\n6 PSY_S3\n6 PSY_S4\n5 PSY_S5\nS PSY_S6\n6 PSY_S7\n5 PSY_S8\n2 PSY_S9\nS 9 7 7\n' +
            'Experimental sciences\nBiology\nBIO_H1\n5 BIO_H2\n4 BIO_H3\nS BIO_H4\n5 BIO_H5\n5 BIO_H6\n2 BIO_H7\n3 BIO_H8\n4 BIO_H9\n1 9 5 5\n' +
            'Mathematics\n' +
            'Mathematical studies\nMATST_S1\n5 MATST_S2\n7 MATST_S3\n6 MATST_S4\n6 MATST_S5\n4 MATST_S6\nS 6 5 5'
        )
      })
    })

    describe('Oppiaineet', function () {
      before(opinnot.expandAll)
      it('ryhmitellään aineryhmittäin', function () {
        var rivit = S('.oppiaineet tbody tr')
        expect(S(rivit.get(0)).hasClass('aineryhmä')).to.equal(true)
        expect(S(rivit.get(1)).hasClass('A')).to.equal(true)
        expect(S(rivit.get(2)).hasClass('A2')).to.equal(true)
        expect(S(rivit.get(3)).hasClass('uusi-oppiaine')).to.equal(true)
        expect(S(rivit.get(4)).hasClass('aineryhmä')).to.equal(true)
        expect(S(rivit.get(5)).hasClass('HIS')).to.equal(true)
        expect(S(rivit.get(6)).hasClass('PSY')).to.equal(true)
        expect(S(rivit.get(7)).hasClass('uusi-oppiaine')).to.equal(true)
        expect(S(rivit.get(8)).hasClass('aineryhmä')).to.equal(true)
        expect(S(rivit.get(9)).hasClass('BIO')).to.equal(true)
        expect(S(rivit.get(10)).hasClass('uusi-oppiaine')).to.equal(true)
        expect(S(rivit.get(11)).hasClass('aineryhmä')).to.equal(true)
        expect(S(rivit.get(12)).hasClass('MATST')).to.equal(true)
      })

      describe('Kun oppiaineen arvosana ei ole ennakkoarvosana', function () {
        it('arvosanalle ei näytetä alaviitettä', function () {
          expect(S('.oppiaineet tbody tr:eq(1) > .arvosana').text()).to.equal(
            '4'
          )
        })

        it('alaviitteiden selitteitä ei näytetä, kun yhtään alaviitettä (ennakkoarvosanaa) ei ole', function () {
          expect(S('.osasuoritukset .selitteet').text()).to.equal('')
        })

        describe('Yhteiset IB-suoritukset (TOK, CAS, EE)', function () {
          it('arvosanalle näytetään alaviite', function () {
            expect(extractAsText(S('.theoryOfKnowledge'))).to.not.contain('*')
            expect(extractAsText(S('.creativityActionService'))).to.not.contain(
              '*'
            )
            expect(extractAsText(S('.extendedEssay'))).to.not.contain('*')
          })
        })
      })

      describe('Kun oppiaineen arvosana on ennakkoarvosana', function () {
        before(
          page.openPage,
          page.oppijaHaku.searchAndSelect('071096-317K'),
          opinnot.valitseSuoritus(undefined, 'IB-tutkinto'),
          opinnot.expandAll
        )

        it('arvosana näytetään predicted-sarakkeessa', function () {
          expect(S('.oppiaineet tbody tr:eq(1) > .arvosana').text()).to.equal(
            ''
          )
          expect(
            S('.oppiaineet tbody tr:eq(1) > .predicted-arvosana').text()
          ).to.equal('4')
        })

        it('ennakkoarvosana-alaviitteelle näyteään selite', function () {
          expect(S('.osasuoritukset .selitteet').text()).to.equal(
            '* = ennustettu arvosana'
          )
        })

        describe('Yhteiset IB-suoritukset (TOK, CAS, EE)', function () {
          it('arvosanalle näytetään alaviite', function () {
            expect(extractAsText(S('.theoryOfKnowledge'))).to.contain('*')
            expect(extractAsText(S('.creativityActionService'))).to.contain('*')
            expect(extractAsText(S('.extendedEssay'))).to.contain('*')
          })
        })
      })
    })

    describe('Tietojen muuttaminen', function () {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('040701-432D'),
        opinnot.valitseSuoritus(undefined, 'IB-tutkinto')
      )
      describe('Suoritusten tiedot', function () {
        describe('Yhteinen IB-suoritus', function () {
          var tok = opinnot.ibYhteisetSuoritukset.suoritus('theoryOfKnowledge')
          var cas = opinnot.ibYhteisetSuoritukset.suoritus(
            'creativityActionService'
          )
          var ee = opinnot.ibYhteisetSuoritukset.suoritus('extendedEssay')

          describe('Alkutila', function () {
            before(editor.edit)

            it('on oikein', function () {
              expect(editor.canSave()).to.equal(false)
              expect(tok.arvosana.getValue()).to.equal('A')
              expect(cas.getValue()).to.equal('S')

              expect(ee.arvosana.getValue()).to.equal('B')
              expect(ee.taso.getValue()).to.equal('Higher Level')
              expect(ee.ryhmä.getValue()).to.equal(
                'Studies in language and literature'
              )
              expect(ee.aihe.getValue()).to.equal(
                "How is the theme of racial injustice treated in Harper Lee's To Kill a Mockingbird and Solomon Northup's 12 Years a Slave"
              )
            })

            after(editor.cancelChanges)
          })

          describe('Theory of Knowledge', function () {
            describe('Arvosanan muuttaminen', function () {
              before(
                editor.edit,
                tok.arvosana.selectValue('B'),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(tok.arvosana.getText()).to.equal('B')
              })
            })

            describe('Arvosanan asettaminen ennakkoarvosanaksi', function () {
              before(
                editor.edit,
                tok.predicted.setValue('on'),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('näyttää alaviitteen', function () {
                expect(tok.arvosana.getText()).to.equal('B *')
              })

              it('näyttää alaviitteen selitteen', function () {
                expect(S('.osasuoritukset .selitteet').text()).to.equal(
                  '* = ennustettu arvosana'
                )
              })
            })

            describe('Kurssin', function () {
              before(editor.edit)

              describe('Arvosanan muuttaminen', function () {
                var kurssi = tok.asOppiaine.kurssi('TOK1')

                before(
                  kurssi.arvosana.selectValue('5'),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('toimii', function () {
                  expect(kurssi.arvosana.getText()).to.equal('5')
                })
              })

              describe('Arvioinnin effort-tiedon muuttaminen', function () {
                var kurssi = tok.asOppiaine.kurssi('TOK1')

                before(
                  editor.edit,
                  kurssi.toggleDetails,
                  kurssi.details().property('effort').selectValue('good'),
                  kurssi.toggleDetails,
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown),
                  kurssi.toggleDetails
                )

                it('toimii', function () {
                  expect(
                    kurssi.details().property('arviointi').getText()
                  ).to.equal('Arviointi Arvosana 5\n' + 'Effort good')
                })
              })

              describe('Arvioinnin effort-tiedon poistaminen', function () {
                var kurssi = tok.asOppiaine.kurssi('TOK1')

                before(
                  editor.edit,
                  kurssi.toggleDetails,
                  kurssi
                    .details()
                    .property('effort')
                    .selectValue('Ei valintaa'),
                  kurssi.toggleDetails,
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown),
                  kurssi.toggleDetails
                )

                it('toimii', function () {
                  expect(
                    kurssi.details().property('arviointi').getText()
                  ).to.equal('Arviointi Arvosana 5')
                })
              })

              describe('Lisääminen', function () {
                before(
                  editor.edit,
                  tok.asOppiaine.lisääPaikallinenKurssi('PAIK'),
                  tok.asOppiaine.kurssi('PAIK').arvosana.selectValue('3'),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('toimii', function () {
                  expect(extractAsText(S('.theoryOfKnowledge'))).to.contain(
                    'PAIK'
                  )
                })
              })

              describe('Poistaminen', function () {
                before(
                  editor.edit,
                  tok.asOppiaine.kurssi('PAIK').poistaKurssi,
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('toimii', function () {
                  expect(extractAsText(S('.theoryOfKnowledge'))).to.not.contain(
                    'PAIK'
                  )
                })
              })
            })
          })

          describe('Creativity, action, service', function () {
            before(editor.edit)

            it('voi valita vain arvosanan S', function () {
              expect(cas.getOptions()).to.deep.equal(['Ei valintaa', 'S'])
            })

            describe('Arvosanan muuttaminen', function () {
              before(
                cas.selectValue('S'),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(cas.getValue()).to.equal('S')
              })
            })
          })

          describe('Extended Essay', function () {
            before(editor.edit)

            describe('Arvosanan muuttaminen', function () {
              before(ee.arvosana.selectValue('C'))

              it('onnistuu', function () {
                expect(ee.arvosana.getValue()).to.equal('C')
              })
            })

            describe('Tason muuttaminen muuttaminen', function () {
              before(ee.taso.selectValue('Standard Level'))

              it('onnistuu', function () {
                expect(ee.taso.getValue()).to.equal('Standard Level')
              })
            })

            describe('Ryhmän muuttaminen muuttaminen', function () {
              before(ee.ryhmä.selectValue('Individuals and societies'))

              it('onnistuu', function () {
                expect(ee.ryhmä.getValue()).to.equal(
                  'Individuals and societies'
                )
              })
            })

            describe('Aiheen muuttaminen muuttaminen', function () {
              before(ee.aihe.setValue('Testi'))

              it('onnistuu', function () {
                expect(ee.aihe.getValue()).to.equal('Testi')
              })
            })

            describe('Muutosten tallennus', function () {
              before(editor.saveChanges, wait.until(page.isSavedLabelShown))

              it('onnistuu', function () {
                expect(ee.arvosana.getText()).to.equal('C')
              })
            })
          })
        })

        describe('Oppiaine', function () {
          before(editor.edit)

          var a = opinnot.oppiaineet.oppiaine('oppiaine.A')
          var kieli = a.propertyBySelector(
            '.title > .properties > .dropdown-wrapper'
          )
          var taso = a.propertyBySelector('.property.taso')
          var predictedGrade = a.propertyBySelector('td.predicted-arvosana')
          var arvosana = a.propertyBySelector('td.arvosana')

          var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine('.A2 +')

          describe('Alkutila', function () {
            it('on oikein', function () {
              expect(editor.canSave()).to.equal(false)
              expect(kieli.getValue()).to.equal('suomi')
              expect(taso.getValue()).to.equal('Standard Level')
              expect(arvosana.getValue()).to.equal('4')
            })
          })

          describe('Kielioppiaineen kielen muuttaminen', function () {
            before(kieli.selectValue('englanti'))

            it('onnistuu', function () {
              expect(kieli.getValue()).to.equal('englanti')
            })

            it('tallennus on mahdollista', function () {
              expect(editor.canSave()).to.equal(true)
            })
          })

          describe('Tason muuttaminen', function () {
            before(taso.selectValue('Higher Level'))

            it('onnistuu', function () {
              expect(taso.getValue()).to.equal('Higher Level')
            })

            it('tallennus on mahdollista', function () {
              expect(editor.canSave()).to.equal(true)
            })
          })

          describe('Arvosanan muuttaminen', function () {
            describe('Predicted grade', function () {
              before(
                predictedGrade.selectValue(3),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(
                  findSingle(
                    '.oppiaine.A .predicted-arvosana .annettuArvosana'
                  )().text()
                ).to.equal('3')
              })
            })

            describe('Päättöarvosana', function () {
              before(
                editor.edit,
                arvosana.selectValue(5),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(
                  findSingle('.oppiaine.A .arvosana .annettuArvosana')().text()
                ).to.equal('5')
              })
            })
          })

          describe('Kielioppiaine', function () {
            before(
              editor.edit,
              editor.property('tila').removeItem(0),
              opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi
            )

            var b = opinnot.oppiaineet.oppiaine('oppiaine.B')
            var kieliB = b.propertyBySelector(
              '.title > .properties > .dropdown-wrapper'
            )

            describe('Lisääminen', function () {
              before(uusiOppiaine.selectValue('B-language'))

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.contain('B-language')
              })

              it('kieli vaaditaan', function () {
                expect(editor.canSave()).to.equal(false)
                expect(
                  S('.oppiaine.B .title .dropdown-wrapper').hasClass('error')
                ).to.equal(true)
              })

              describe('Kielen kanssa', function () {
                before(
                  kieliB.selectValue('espanja'),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('tallennus toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.contain(
                    'B-language'
                  )
                })
              })
            })

            describe('Poistaminen', function () {
              before(
                editor.edit,
                b.propertyBySelector('.remove-row').removeValue,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.not.contain(
                  'Kotitalous'
                )
              })
            })
          })

          describe('Muu oppiaine', function () {
            before(editor.edit)

            var che = opinnot.oppiaineet.oppiaine('oppiaine.CHE')

            it('alkutila', function () {
              expect(editor.canSave()).to.equal(false)
              expect(editor.getEditBarMessage()).to.equal(
                'Ei tallentamattomia muutoksia'
              )
              expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(6)
            })

            describe('Lisääminen', function () {
              before(editor.edit, uusiOppiaine.selectValue('Chemistry'))

              it('lisää oppiaineen', function () {
                expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(7)
              })

              it('tallennus toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.contain('Chemistry')
              })
            })

            describe('Poistaminen', function () {
              before(
                editor.edit,
                che.propertyBySelector('.remove-row').removeValue,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.not.contain(
                  'Chemistry'
                )
              })
            })

            describe('Jo olemassa olevan oppiaineen lisääminen', function () {
              it('alkutila', function () {
                expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(6)
              })

              describe('löytyy dropdownista', function () {
                before(editor.edit, uusiOppiaine.selectValue('Biology'))

                it('toimii', function () {
                  expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(7)
                })
              })

              after(editor.cancelChanges)
            })
          })

          describe('Oppiaineen kurssi', function () {
            before(editor.edit)

            describe('Arvosanan muuttaminen', function () {
              var kurssi = a.kurssi('FIN_S1')

              before(
                kurssi.arvosana.selectValue('5'),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(kurssi.arvosana.getText()).to.equal('5')
              })
            })

            describe('Arvioinnin effort-tiedon muuttaminen', function () {
              var kurssi = a.kurssi('FIN_S1')

              before(
                editor.edit,
                kurssi.toggleDetails,
                kurssi
                  .details()
                  .property('effort')
                  .selectValue('needs improvement'),
                kurssi.toggleDetails,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown),
                kurssi.toggleDetails
              )

              it('toimii', function () {
                expect(
                  kurssi.details().property('arviointi').getText()
                ).to.equal(
                  'Arviointi Arvosana 5\n' + 'Effort needs improvement'
                )
              })
            })

            describe('Arvioinnin effort-tiedon poistaminen', function () {
              var kurssi = a.kurssi('FIN_S1')

              before(
                editor.edit,
                kurssi.toggleDetails,
                kurssi.details().property('effort').selectValue('Ei valintaa'),
                kurssi.toggleDetails,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown),
                kurssi.toggleDetails
              )

              it('toimii', function () {
                expect(
                  kurssi.details().property('arviointi').getText()
                ).to.equal('Arviointi Arvosana 5')
              })
            })

            describe('Lisääminen', function () {
              before(
                editor.edit,
                a.lisääPaikallinenKurssi('PAIK'),
                a.kurssi('PAIK').arvosana.selectValue('3'),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet .A'))).to.contain('PAIK')
              })
            })

            describe('Poistaminen', function () {
              before(
                editor.edit,
                a.kurssi('PAIK').poistaKurssi,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet .A'))).to.not.contain(
                  'PAIK'
                )
              })
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
        })
      })
    })
  })

  describe('Vanhan mallinen IB-tutkinto', function () {
    before(
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('101000A684K')
    )
    it('näyttää oppiaineiden ja kurssien arvosanat, sisältäen pelkkiä päättöarvosanoja', function () {
      expect(extractAsText(S('.osasuoritukset'))).to.equal(
        'Oppiaine Laajuus (kurssia) Predicted grade Päättöarvosana\n' +
          'Studies in language and literature\n' +
          'Language A: literature, suomi\nFIN_S1\n4 FIN_S2\n4 FIN_S3\nS FIN_S4\n5 FIN_S5\n6 FIN_S6\n5 FIN_S7\n5 FIN_S8\nS FIN_S9\n5 9 4\n' +
          'Language A: language and literature, englanti\nENG_B_H1\n6 ENG_B_H2\n7 ENG_B_H4\nS ENG_B_H5\n6 ENG_B_H6\n6 ENG_B_H8\n5 6 7\n' +
          'Individuals and societies\n' +
          'History\nHIS_H3\n6 HIS_H4\n6 HIS_H5\n7 HIS_H6\n6 HIS_H7\n1 HIS_H9\nS 6 6\n' +
          'Psychology\nPSY_S1\n6 PSY_S2\n6 PSY_S3\n6 PSY_S4\n5 PSY_S5\nS PSY_S6\n6 PSY_S7\n5 PSY_S8\n2 PSY_S9\nS 9 7\n' +
          'Experimental sciences\nBiology\nBIO_H1\n5 BIO_H2\n4 BIO_H3\nS BIO_H4\n5 BIO_H5\n5 BIO_H6\n2 BIO_H7\n3 BIO_H8\n4 BIO_H9\n1 9 5\n' +
          'Mathematics\n' +
          'Mathematical studies\nMATST_S1\n5 MATST_S2\n7 MATST_S3\n6 MATST_S4\n6 MATST_S5\n4 MATST_S6\nS 6 5'
      )
    })
  })

  describe('Opiskeluoikeuden lisääminen', function () {
    describe('IB-tutkinto', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataIB(),
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'IB-tutkinto (International Baccalaureate)'
        )
      )

      describe('Lisäyksen jälkeen', function () {
        describe('Opiskeluoikeuden tiedot', function () {
          it('näytetään oikein', function () {
            expect(
              extractAsText(
                S('.suoritus > .properties, .suoritus > .tila-vahvistus')
              )
            ).to.equal(
              'Koulutus IB-tutkinto (International Baccalaureate)\n' +
                'Oppilaitos / toimipiste Ressun lukio\n' +
                'Suorituskieli suomi\n' +
                'Suoritus kesken'
            )
          })
        })

        describe('Oppiaineita', function () {
          it('ei esitäytetä', function () {
            expect(extractAsText(S('.oppiaineet'))).to.equal(
              'Oppiaine Laajuus (kurssia) Predicted grade Päättöarvosana'
            )
          })
        })

        describe('Muokattaessa', function () {
          before(editor.edit)
          it('näytetään editorit IB-tutkinnon ylätason oppiaineiden suorituksille', function () {
            expect(
              editor.propertyBySelector('.theoryOfKnowledge').getValue()
            ).to.equal('Ei valintaa')
            expect(
              editor.propertyBySelector('.extendedEssay').getValue()
            ).to.equal('Ei valintaa')
            expect(
              editor.propertyBySelector('.creativityActionService').getValue()
            ).to.equal('Ei valintaa')
          })

          it('näytetään tyhjät(kin) aineryhmät', function () {
            expect(extractAsText(S('.oppiaineet .aineryhmä'))).to.equal(
              '' +
                'Studies in language and literature\n\n' +
                'Language acquisition\n\n' +
                'Individuals and societies\n\n' +
                'Experimental sciences\n\n' +
                'Mathematics\n\n' +
                'The arts'
            )
          })
          after(editor.cancelChanges)
        })

        describe('Pre-IB-suorituksen lisääminen', function () {
          var lisääSuoritus = opinnot.lisääSuoritusDialog
          var lisäysTeksti = 'lisää pre-IB-suoritus'

          describe('Kun opiskeluoikeus on tilassa VALMIS', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('valmistunut'),
              opiskeluoikeus.opintojenRahoitus().aseta('1'),
              opiskeluoikeus.tallenna
            )

            it('Pre-IB-suoritusta ei voi lisätä', function () {
              expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(false)
            })

            after(editor.property('tila').removeItem(0))
          })

          describe('Kun opiskeluoikeus on tilassa LÄSNÄ', function () {
            describe('Ennen lisäystä', function () {
              it('Näytetään IB-tutkinto', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'IB-tutkinto (International Baccalaureate)'
                ])
              })

              it('Pre-IB-suorituksen voi lisätä', function () {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(true)
              })
            })

            describe('Lisäyksen jälkeen', function () {
              before(lisääSuoritus.clickLink(lisäysTeksti))

              it('Näytetään myös pre-IB-suoritus', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'IB-tutkinto (International Baccalaureate)',
                  'Pre-IB'
                ])
              })

              it('Pre-IB-suoritusta ei voi enää lisätä', function () {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(
                  false
                )
              })

              describe('Suorituksen tiedot', function () {
                before(editor.saveChanges, wait.until(page.isSavedLabelShown))

                it('näytetään oikein', function () {
                  expect(
                    extractAsText(
                      S('.suoritus > .properties, .suoritus > .tila-vahvistus')
                    )
                  ).to.equal(
                    'Koulutus Pre-IB\n' +
                      'Oppilaitos / toimipiste Ressun lukio\n' +
                      'Suorituskieli suomi\n' +
                      'Suoritus kesken'
                  )
                })
              })
            })
          })
        })
      })
    })

    describe('Pre-IB', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataPreIB(),
        addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Pre-IB')
      )

      describe('Lisäyksen jälkeen', function () {
        describe('Opiskeluoikeuden tiedot', function () {
          it('näytetään oikein', function () {
            expect(
              extractAsText(
                S('.suoritus > .properties, .suoritus > .tila-vahvistus')
              )
            ).to.equal(
              'Koulutus Pre-IB\n' +
                'Oppilaitos / toimipiste Ressun lukio\n' +
                'Suorituskieli suomi\n' +
                'Suoritus kesken'
            )
          })
        })

        describe('Oppiaineita', function () {
          it('ei esitäytetä', function () {
            expect(extractAsText(S('.osasuoritukset'))).to.equal('')
          })
        })

        describe('Muokattaessa', function () {
          before(editor.edit)
          it('näytetään uuden oppiaineen lisäys-dropdown', function () {
            expect(
              isElementVisible(S('.osasuoritukset .uusi-oppiaine .dropdown'))
            ).to.equal(true)
          })
          after(editor.cancelChanges)
        })

        describe('IB-tutkinnon suorituksen lisääminen', function () {
          var lisääSuoritus = opinnot.lisääSuoritusDialog
          var lisäysTeksti = 'lisää IB-tutkinnon suoritus'

          describe('Kun opiskeluoikeus on tilassa VALMIS', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('valmistunut'),
              opiskeluoikeus.opintojenRahoitus().aseta('1'),
              opiskeluoikeus.tallenna
            )

            it('IB-tutkinnon suoritusta ei voi lisätä', function () {
              expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(false)
            })

            after(editor.property('tila').removeItem(0))
          })

          describe('Kun opiskeluoikeus on tilassa LÄSNÄ', function () {
            describe('Ennen lisäystä', function () {
              it('Näytetään pre-IB-suoritus', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal(['Pre-IB'])
              })

              it('IB-tutkinnon suorituksen voi lisätä', function () {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(true)
              })
            })

            describe('Lisäyksen jälkeen', function () {
              before(lisääSuoritus.clickLink(lisäysTeksti))

              it('Näytetään myös IB-tutkinnon suoritus', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Pre-IB',
                  'IB-tutkinto (International Baccalaureate)'
                ])
              })

              it('IB-tutkinnon suoritusta ei voi enää lisätä', function () {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(
                  false
                )
              })

              describe('Suorituksen tiedot', function () {
                before(editor.saveChanges, wait.until(page.isSavedLabelShown))

                it('näytetään oikein', function () {
                  expect(
                    extractAsText(
                      S('.suoritus > .properties, .suoritus > .tila-vahvistus')
                    )
                  ).to.equal(
                    'Koulutus IB-tutkinto (International Baccalaureate)\n' +
                      'Oppilaitos / toimipiste Ressun lukio\n' +
                      'Suorituskieli suomi\n' +
                      'Suoritus kesken'
                  )
                })
              })
            })
          })
        })
      })
    })
  })
})
