describe('DIA', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()
  var opiskeluoikeus = OpiskeluoikeusDialog()
  var addOppija = AddOppijaPage()

  before(Authentication().login(), resetFixtures)

  describe('Opiskeluoikeuden tiedot', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('151013-2195'))
    it('näytetään oikein', function () {
      expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
        'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 4.6.2016\n' +
          'Tila 4.6.2016 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
          '1.9.2012 Läsnä (valtionosuusrahoitteinen koulutus)'
      )
    })

    describe('Opiskeluoikeuden lisätiedot', function () {
      describe('Lisätietojen lisäys', function () {
        before(
          editor.edit,
          opinnot.expandAll,
          editor.property('pidennettyPäättymispäivä').setValue(true),
          editor.property('ulkomainenVaihtoopiskelija').setValue(true),
          editor.property('ulkomaanjaksot').addItem,
          editor
            .property('ulkomaanjaksot')
            .propertyBySelector('.alku')
            .setValue('4.1.2013'),
          editor
            .property('ulkomaanjaksot')
            .propertyBySelector('.loppu')
            .setValue('16.3.2013'),
          editor
            .property('ulkomaanjaksot')
            .property('maa')
            .setValue('Ahvenanmaa'),
          editor
            .property('ulkomaanjaksot')
            .property('kuvaus')
            .setValue('Ulkomaanjakso'),
          editor.property('erityisenKoulutustehtävänJaksot').addItem,
          editor
            .property('erityisenKoulutustehtävänJaksot')
            .propertyBySelector('.alku')
            .setValue('1.9.2012'),
          editor
            .property('erityisenKoulutustehtävänJaksot')
            .propertyBySelector('.loppu')
            .setValue('4.6.2016'),
          editor
            .property('erityisenKoulutustehtävänJaksot')
            .property('tehtävä')
            .setValue('Kieliin painottuva koulutus'),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )
        it('tallentaa ja esittää kaikki lisätiedot', function () {
          expect(extractAsText(S('.lisätiedot'))).to.equal(
            'Lisätiedot\n' +
              'Pidennetty päättymispäivä kyllä\n' +
              'Ulkomainen vaihto-opiskelija. kyllä\n' +
              'Erityisen koulutustehtävän jaksot 1.9.2012 — 4.6.2016 Tehtävä Kieliin painottuva koulutus\n' +
              'Ulkomaanjaksot 4.1.2013 — 16.3.2013 Maa Ahvenanmaa Kuvaus Ulkomaanjakso'
          )
        })
      })

      describe('Lisätietojen muokkaus', function () {
        before(
          editor.edit,
          editor.property('pidennettyPäättymispäivä').setValue(false),
          editor.property('ulkomainenVaihtoopiskelija').setValue(false),
          editor
            .property('ulkomaanjaksot')
            .propertyBySelector('.alku')
            .setValue('5.1.2013'),
          editor
            .property('ulkomaanjaksot')
            .propertyBySelector('.loppu')
            .setValue('14.2.2013'),
          editor.property('ulkomaanjaksot').property('maa').setValue('Ruotsi'),
          editor
            .property('ulkomaanjaksot')
            .property('kuvaus')
            .setValue('Jotain muuta'),
          editor.property('ulkomaanjaksot').addItem,
          editor
            .property('ulkomaanjaksot')
            .propertyBySelector('.alku:last')
            .setValue('15.2.2013'),
          editor
            .property('ulkomaanjaksot')
            .propertyBySelector('.loppu:last')
            .setValue('29.4.2013'),
          editor
            .property('ulkomaanjaksot')
            .property('maa:last')
            .setValue('Norja'),
          editor
            .property('ulkomaanjaksot')
            .property('kuvaus:last')
            .setValue('Ja vielä jotain'),
          editor
            .property('erityisenKoulutustehtävänJaksot')
            .propertyBySelector('.alku')
            .setValue('1.9.2012'),
          editor
            .property('erityisenKoulutustehtävänJaksot')
            .propertyBySelector('.loppu')
            .setValue('4.6.2015'),
          editor
            .property('erityisenKoulutustehtävänJaksot')
            .property('tehtävä')
            .setValue('Ilmaisutaitoon painottuva opetus'),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )

        it('tallentaa muutokset ja esittää ajantasaiset lisätiedot', function () {
          expect(extractAsText(S('.lisätiedot'))).to.equal(
            'Lisätiedot\n' +
              'Erityisen koulutustehtävän jaksot 1.9.2012 — 4.6.2015 Tehtävä Ilmaisutaitoon painottuva opetus\n' +
              'Ulkomaanjaksot 5.1.2013 — 14.2.2013 Maa Ruotsi Kuvaus Jotain muuta\n' +
              '15.2.2013 — 29.4.2013 Maa Norja Kuvaus Ja vielä jotain'
          )
        })
      })

      describe('Lisätietojen poisto', function () {
        before(
          editor.edit,
          editor.property('ulkomaanjaksot').removeItem(0),
          editor.property('ulkomaanjaksot').removeItem(0),
          editor.property('erityisenKoulutustehtävänJaksot').removeItem(0),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )

        it('poistaa lisätiedot eikä enää esitä niitä', function () {
          expect(isElementVisible(findSingle('.lisätiedot'))).to.equal(false)
        })
      })
    })
  })

  describe('Valmistava DIA-vaihe', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('151013-2195'),
      opinnot.valitseSuoritus(undefined, 'Valmistava DIA-vaihe')
    )

    describe('Oppijan suorituksissa', function () {
      it('näytetään tutkinto ja oppilaitos', function () {
        expect(opinnot.getTutkinto()).to.equal('Valmistava DIA-vaihe')
        expect(opinnot.getOppilaitos()).to.equal('Helsingin Saksalainen koulu')
      })
    })

    describe('Kaikki tiedot näkyvissä', function () {
      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Valmistava DIA-vaihe\n' +
            'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
            'Suorituskieli englanti\n' +
            'Suoritus valmis Vahvistus : 4.6.2016 Helsinki Reijo Reksi , rehtori'
        )
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function () {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Laajuus (vuosiviikkotuntia)\n' +
            'Kielet, kirjallisuus, taide\n' +
            'Äidinkieli, saksa\n10/I\n3 10/II\n5 3\n' +
            'Äidinkieli, suomi\n10/I\n2 10/II\n3 3\n' +
            'A-kieli, englanti\n10/I\n3 3\n' +
            'B1-kieli, ruotsi\n10/I\n2 10/II\n2 3\n' +
            'B3-kieli, venäjä\n10/I\n4 10/II\n3 3\n' +
            'Kuvataide\n10/I\n4 10/II\n5 2\n' +
            'Matematiikka ja luonnontieteet\n' +
            'Matematiikka\n10/I\n3 10/II\n1 4\n' +
            'Fysiikka\n10/I\n3 10/II\n2 2\n' +
            'Kemia\n10/I\n2 10/II\n4 2\n' +
            'Tietotekniikka\n10/I\n1 10/II\n2 2\n' +
            'Yhteiskuntatieteet\n' +
            'Historia\n10/I\n3 10/II\n4 2\n' +
            'Taloustieto ja yhteiskuntaoppi\n10/I\n5 10/II\n3 2\n' +
            'Maantiede (saksa ja englanti)\n10/I\n3 10/II\n3 2\n' +
            'Filosofia\n10/I\n1 10/II\n1 2\n' +
            'Lisäaineet\n' +
            'B2-kieli, latina\n10/I\n4 10/II\n4 2'
        )
      })
    })

    describe('Tietojen muuttaminen', function () {
      describe('Päätason suorituksen poistaminen', function () {
        before(editor.edit)

        describe('Mitätöintilinkki', function () {
          it('Ei näytetä', function () {
            expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(false)
          })
        })
      })

      describe('Valmistavan vaiheen tiedot', function () {
        describe('Suorituskieli', function () {
          before(
            editor.edit,
            opinnot
              .opiskeluoikeusEditor()
              .property('suorituskieli')
              .setValue('saksa'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('voidaan vaihtaa', function () {
            expect(extractAsText(S('.suorituskieli:last'))).to.equal(
              'Suorituskieli saksa'
            )
          })
        })

        describe('Todistuksella näkyvät lisätiedot', function () {
          before(
            editor.edit,
            opinnot
              .opiskeluoikeusEditor()
              .property('todistuksellaNäkyvätLisätiedot')
              .setValue('Valmistavan vaiheen lisätieto'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('voidaan lisätä', function () {
            expect(
              extractAsText(S('.todistuksellaNäkyvätLisätiedot:first'))
            ).to.equal(
              'Todistuksella näkyvät lisätiedot Valmistavan vaiheen lisätieto'
            )
          })
        })
      })

      describe('Suoritusten tiedot', function () {
        describe('Oppiaine', function () {
          var aine = opinnot.oppiaineet.oppiaine('oppiaine.A')

          describe('Laajuus', function () {
            describe('Muuttaminen', () => {
              before(
                editor.edit,
                aine.laajuus.setValue(4),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(findSingle('.oppiaine.A .laajuus')().text()).to.equal(
                  '4'
                )
              })
            })

            describe('Poistaminen', () => {
              before(
                editor.edit,
                aine.laajuus.setValue(''),
                editor.saveChangesAndExpectError,
                wait.until(page.isErrorShown)
              )

              describe('Valmiissa päätason suorituksessa', () => {
                it('ei onnistu', function () {
                  expect(page.getErrorMessage()).to.equal(
                    'Suoritus suorituksentyyppi/diavalmistavavaihe on merkitty valmiiksi, mutta se sisältää oppiaineen, jolta puuttuu laajuus'
                  )
                })
              })

              describe('Keskeneräisessä päätason suorituksessa', () => {
                before(
                  editor.property('tila').removeItem(0),
                  opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('onnistuu', function () {
                  expect(findSingle('.oppiaine.A .laajuus')().text()).to.equal(
                    ''
                  )
                })
              })
            })
          })

          describe('Suorituskielen muuttaminen', function () {
            before(
              editor.edit,
              aine.suorituskieli.selectValue('suomi'),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )

            it('onnistuu', function () {
              expect(
                findSingle('.oppiaine.A .suorituskieli .value')().text()
              ).to.equal('suomi')
            })
          })

          describe('Oppiaineen osasuoritukset', function () {
            var osasuoritus = aine.kurssi('10/I')

            describe('Arvosana-asteikko', function () {
              before(editor.edit)

              it('on oikea', function () {
                expect(osasuoritus.arvosana.getOptions()).to.deep.equal([
                  'Ei valintaa',
                  '1',
                  '2',
                  '2-',
                  '3',
                  '4',
                  '5',
                  '6'
                ])
              })

              after(editor.cancelChanges)
            })

            describe('Arvosanan muuttaminen', function () {
              before(
                editor.edit,
                osasuoritus.arvosana.selectValue(6),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(
                  findFirst('.oppiaine.A .kurssi:first .arvosana')().text()
                ).to.equal('6')
              })
            })

            describe('Laajuuden muuttaminen', function () {
              before(
                editor.edit,
                osasuoritus.toggleDetails,
                osasuoritus.details().property('laajuus').setValue(1),
                osasuoritus.toggleDetails,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              describe('Kun oppiaineella ei ole laajuutta', function () {
                before(osasuoritus.showDetails)

                it('lukukauden laajuuden muutos tallentuu ja muutos näkyy osasuorituksen tiedoissa', function () {
                  expect(osasuoritus.laajuus.getText()).to.equal(
                    'Laajuus 1 vuosiviikkotuntia'
                  )
                })
              })

              describe('Kun oppiaineella on laajuus', () => {
                before(
                  editor.edit,
                  aine.laajuus.setValue(4),
                  editor.saveChangesAndExpectError,
                  wait.until(page.isErrorShown)
                )

                describe('ja osasuoritusten laajuuden summa on eri kuin oppiaineen laajuus', () => {
                  it('muutos ei tallennu', function () {
                    expect(page.getErrorMessage()).to.equal(
                      'Suorituksen oppiaineetdia/A osasuoritusten laajuuksien summa 1.0 ei vastaa suorituksen laajuutta 4.0'
                    )
                  })
                })

                describe('ja osasuoritusten laajuuden summa on sama kuin oppiaineen laajuus', () => {
                  before(
                    osasuoritus.toggleDetails,
                    osasuoritus
                      .details()
                      .propertyBySelector('span.laajuus')
                      .setValue(4),
                    osasuoritus.toggleDetails,
                    editor.saveChanges,
                    wait.until(page.isSavedLabelShown),
                    osasuoritus.showDetails
                  )

                  it('muutos tallentuu ja muutos näkyy osasuorituksen tiedoissa', function () {
                    expect(osasuoritus.laajuus.getText()).to.equal(
                      'Laajuus 4 vuosiviikkotuntia'
                    )
                  })
                })
              })
            })

            describe('Lisääminen', function () {
              var dialog = aine.lisääKurssiDialog

              before(
                editor.edit,
                aine.avaaLisääKurssiDialog,
                dialog.valitseKurssi('10/II'),
                dialog.lisääKurssi,
                aine.kurssi('10/II').arvosana.selectValue(2),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('lisää osasuorituksen', function () {
                expect(extractAsText(S('.oppiaineet .A'))).to.contain(
                  '10/II\n2'
                )
              })
            })

            describe('Kun kaksi lukukautta on jo lisätty', function () {
              var dialog = aine.lisääKurssiDialog

              before(editor.edit)

              it('nappi osasuorituksen lisäämiselle valmistavan vaiheen oppiaineeseen on piilotettu', function () {
                expect(
                  isElementVisible(aine.propertyBySelector('.uusi-kurssi a'))
                ).to.equal(false)
              })

              after(editor.cancelChanges)
            })

            describe('Poistaminen', function () {
              before(
                editor.edit,
                aine.kurssi('10/II').poistaKurssi,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('poistaa osasuorituksen', function () {
                expect(extractAsText(S('.oppiaineet .A'))).to.not.contain(
                  '10/II'
                )
              })
            })
          })

          describe('Lisääminen', function () {
            var uusiOppiaine = OpinnotPage()
              .opiskeluoikeusEditor()
              .propertyBySelector('.uusi-oppiaine:last')

            before(editor.edit, uusiOppiaine.selectValue('Liikunta'))

            it('lisää oppiaineen', function () {
              expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(16)
            })

            describe('Laajuuden tallentaminen uudelle oppiaineelle', function () {
              var liikunta = opinnot.oppiaineet.oppiaine('oppiaine.LI')
              before(
                liikunta.laajuus.setValue('2'),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('tallentaa oppiaineen laajuuksineen', function () {
                expect(extractAsText(S('.oppiaineet'))).to.contain('Liikunta')
              })
            })
          })

          describe('Poistaminen', function () {
            var liikunta = opinnot.oppiaineet.oppiaine('oppiaine.LI')

            before(
              editor.edit,
              liikunta.poista(),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )

            it('poistaa oppiaineen', function () {
              expect(extractAsText(S('.oppiaineet'))).to.not.contain('Liikunta')
            })
          })
        })
      })
    })
  })

  describe('Deutsche Internationale Abitur', function () {
    before(
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('151013-2195'),
      editor.edit,
      editor.property('tila').removeItem(0),
      editor.saveChanges,
      wait.until(page.isSavedLabelShown),
      opinnot.valitseSuoritus(undefined, 'Deutsche Internationale Abitur')
    )

    describe('Oppijan suorituksissa', function () {
      it('näytetään tutkinto ja oppilaitos', function () {
        expect(opinnot.getTutkinto()).to.equal(
          'Deutsche Internationale Abitur; Reifeprüfung'
        )
        expect(opinnot.getOppilaitos()).to.equal('Helsingin Saksalainen koulu')
      })
    })

    describe('Kaikki tiedot näkyvissä', function () {
      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Deutsche Internationale Abitur; Reifeprüfung\n' +
            'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
            'Kokonaispistemäärä 870\n' +
            'Lukukausisuoritusten kokonaispistemäärä 590\n' +
            'Tutkintoaineiden kokonaispistemäärä 280\n' +
            'Kokonaispistemäärästä johdettu keskiarvo 1,2\n' +
            'Suorituskieli englanti\n' +
            'Suoritus valmis Vahvistus : 4.6.2016 Helsinki Reijo Reksi , rehtori'
        )
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function () {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Laajuus (vuosiviikkotuntia)\n' +
            'Kielet, kirjallisuus, taide\n' +
            'Äidinkieli, saksa\nVastaavuustodistuksen tiedot Keskiarvo 4\nLukio-opintojen laajuus 2,5 op\nKoetuloksen nelinkertainen pistemäärä 20\n11/I\n3 11/II\n5 12/I\n4 12/II\n3 Kirjallinen koe\n5 10\n' +
            'Äidinkieli, suomi\n11/I\n2 11/II\n3 12/I\n3 12/II\n3 8\n' +
            'A-kieli, englanti\n11/I\n2 11/II\n3 12/I\n2 6\n' +
            'B1-kieli, ruotsi\n11/I\n2 11/II\n2 12/I\n4 12/II\n3 6\n' +
            'B3-kieli, venäjä\n11/I\n4 11/II\n3 12/I\n4 12/II\n3 6\n' +
            'Kuvataide\n11/I\n4 11/II\n3 12/I\n2 12/II\n2 4\n' +
            'Matematiikka ja luonnontieteet\n' +
            'Matematiikka\n11/I\n3 11/II\n1 12/I\n1 12/II\n2 8\n' +
            'Fysiikka\n11/I\n2 11/II\n2 12/I\n1 12/II\n1 6\n' +
            'Kemia\n11/I\n3 11/II\n2 12/I\n1 12/II\n2 6\n' +
            'Tietotekniikka\n11/I\n2 11/II\n1 12/I\n1 12/II\n1 4\n' +
            'Yhteiskuntatieteet\n' +
            'Historia\nSuorituskieli suomi\n11/I\n3 11/II\n4 12/I\n3 12/II\n1 6\n' +
            'Taloustieto ja yhteiskuntaoppi\n11/I\n4 11/II\n3 12/I\n2 12/II\n3 6\n' +
            'Maantiede (saksa ja englanti)\n11/I\n3 11/II\n5 12/I\n3 12/II\n2 6\n' +
            'Filosofia\n11/I\n2 11/II\n2 12/I\n2 12/II\n2 4\n' +
            'Lisäaineet\n' +
            'B2-kieli, latina\n11/I\n3 11/II\n3 12/I\n2 12/II\n2 4\n' +
            'Cambridge Certificate of Advanced English\n12/I\n3 12/II\n1 1\n' +
            'Matematiikka, syventävä\n11/I\n2 11/II\n1 12/I\n2 12/II\n4 2'
        )
      })
    })

    describe('Tietojen muuttaminen', function () {
      describe('Päätason suorituksen poistaminen', function () {
        before(editor.edit)

        describe('Mitätöintilinkki', function () {
          it('Ei näytetä', function () {
            expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(false)
          })
        })
      })

      describe('Tutkintovaiheen tiedot', function () {
        describe('Kokonaispistemäärä', function () {
          before(
            editor.edit,
            opinnot
              .opiskeluoikeusEditor()
              .property('kokonaispistemäärä')
              .setValue('850'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('voidaan muuttaa', function () {
            expect(extractAsText(S('.kokonaispistemäärä'))).to.equal(
              'Kokonaispistemäärä 850'
            )
          })
        })

        describe('Lukukausisuoritusten kokonaispistemäärä', function () {
          before(
            editor.edit,
            opinnot
              .opiskeluoikeusEditor()
              .property('lukukausisuoritustenKokonaispistemäärä')
              .setValue('500'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('voidaan muuttaa', function () {
            expect(
              extractAsText(S('.lukukausisuoritustenKokonaispistemäärä'))
            ).to.equal('Lukukausisuoritusten kokonaispistemäärä 500')
          })
        })

        describe('Tutkintoaineiden kokonaispistemäärä', function () {
          before(
            editor.edit,
            opinnot
              .opiskeluoikeusEditor()
              .property('tutkintoaineidenKokonaispistemäärä')
              .setValue('215'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('voidaan muuttaa', function () {
            expect(
              extractAsText(S('.tutkintoaineidenKokonaispistemäärä'))
            ).to.equal('Tutkintoaineiden kokonaispistemäärä 215')
          })
        })

        describe('Kokonaispistemäärästä johdettu keskiarvo', function () {
          before(
            editor.edit,
            opinnot
              .opiskeluoikeusEditor()
              .property('kokonaispistemäärästäJohdettuKeskiarvo')
              .setValue('2,0'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('voidaan muuttaa', function () {
            expect(
              extractAsText(S('.kokonaispistemäärästäJohdettuKeskiarvo'))
            ).to.equal('Kokonaispistemäärästä johdettu keskiarvo 2,0')
          })
        })

        describe('Suorituskieli', function () {
          before(
            editor.edit,
            opinnot
              .opiskeluoikeusEditor()
              .property('suorituskieli')
              .setValue('suomi'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('voidaan vaihtaa', function () {
            expect(extractAsText(S('.suorituskieli:first'))).to.equal(
              'Suorituskieli suomi'
            )
          })
        })

        describe('Todistuksella näkyvät lisätiedot', function () {
          before(
            editor.edit,
            opinnot
              .opiskeluoikeusEditor()
              .property('todistuksellaNäkyvätLisätiedot')
              .setValue('Tässä jotain lisätietoa'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('voidaan lisätä', function () {
            expect(
              extractAsText(S('.todistuksellaNäkyvätLisätiedot:first'))
            ).to.equal(
              'Todistuksella näkyvät lisätiedot Tässä jotain lisätietoa'
            )
          })
        })
      })

      describe('Suoritusten tiedot', function () {
        function lisääOppiaineelleKurssiTunnisteella(aine, kurssinTunniste) {
          var dialog = aine.lisääKurssiDialog
          return seq(
            aine.avaaLisääKurssiDialog,
            dialog.valitseKurssi(kurssinTunniste),
            dialog.lisääKurssi,
            aine.kurssi(kurssinTunniste).arvosana.selectValue(9)
          )
        }

        describe('Oppiaine', function () {
          var aine = opinnot.oppiaineet.oppiaine('oppiaine.A')

          describe('Laajuus', function () {
            describe('Muuttaminen', () => {
              before(
                editor.edit,
                aine.laajuus.setValue(4),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(findSingle('.oppiaine.A .laajuus')().text()).to.equal(
                  '4'
                )
              })
            })

            describe('Poistaminen', () => {
              before(
                editor.edit,
                aine.laajuus.setValue(''),
                editor.saveChangesAndExpectError,
                wait.until(page.isErrorShown)
              )

              describe('Valmiissa päätason suorituksessa', () => {
                it('ei onnistu', function () {
                  expect(page.getErrorMessage()).to.equal(
                    'Suoritus koulutus/301103 on merkitty valmiiksi, mutta se sisältää oppiaineen, jolta puuttuu laajuus'
                  )
                })
              })

              describe('Keskeneräisessä päätason suorituksessa', () => {
                before(
                  opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('onnistuu', function () {
                  expect(findSingle('.oppiaine.A .laajuus')().text()).to.equal(
                    ''
                  )
                })
              })
            })
          })

          describe('Vastaavuustodistuksen tiedot', function () {
            var oppiaine = opinnot.oppiaineet.oppiaine('oppiaine.AI:first')
            before(
              editor.edit,
              oppiaine
                .property('lukioOpintojenLaajuus')
                .property('yksikko')
                .setValue('opintopistettä'),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )

            it('laajuuden yksikön vaihtaminen opintopisteiksi onnistuu', function () {
              expect(
                oppiaine.property('lukioOpintojenLaajuus').getText()
              ).to.deep.equal('Lukio-opintojen laajuus 2,5 op')
            })

            describe('laajuden yksikön vaihtaminen kursseiksi', function () {
              var opintojenOppiaine =
                opinnot.oppiaineet.oppiaine('oppiaine.AI:first')
              before(
                editor.edit,
                opintojenOppiaine
                  .property('lukioOpintojenLaajuus')
                  .property('yksikko')
                  .setValue('kurssia'),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(
                  opintojenOppiaine.property('lukioOpintojenLaajuus').getText()
                ).to.deep.equal('Lukio-opintojen laajuus 2,5 kurssia')
              })
            })
          })

          describe('Suorituskielen muuttaminen', function () {
            before(
              editor.edit,
              aine.suorituskieli.selectValue('suomi'),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )

            it('onnistuu', function () {
              expect(
                findSingle('.oppiaine.A .suorituskieli .value')().text()
              ).to.equal('suomi')
            })
          })

          describe('Koetuloksen nelinkertainen pistemäärä', function () {
            describe('Muuttaminen', () => {
              before(
                editor.edit,
                aine.koetuloksenNelinkertainenPistemäärä.setValue(30),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(
                  findSingle(
                    '.oppiaine.A .koetuloksenNelinkertainenPistemäärä .value'
                  )().text()
                ).to.equal('30')
              })
            })

            describe('Virheellisen arvon syöttäminen', function () {
              before(
                editor.edit,
                aine.koetuloksenNelinkertainenPistemäärä.setValue(100)
              )

              it('ei ole sallittu', function () {
                expect(editor.canSave()).to.equal(false)
              })

              after(editor.cancelChanges)
            })

            describe('Poistaminen', () => {
              before(
                editor.edit,
                aine.koetuloksenNelinkertainenPistemäärä.setValue(''),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaine.A'))).to.not.contain(
                  'Koetuloksen nelinkertainen pistemäärä'
                )
              })
            })
          })

          describe('Oppiaineen osasuoritukset', function () {
            var osasuoritus = aine.propertyBySelector('.kurssi:first')
            var arvosana = osasuoritus.propertyBySelector('.arvosana')

            var lukukausi_11_I = aine.nthOsasuoritus(0)
            var lukukausi_11_II = aine.nthOsasuoritus(1)
            var lukukausi_12_I = aine.nthOsasuoritus(2)

            describe('Arvosana-asteikko', function () {
              before(editor.edit)

              it('on oikea', function () {
                expect(arvosana.getOptions()).to.deep.equal([
                  'Ei valintaa',
                  '0',
                  '1',
                  '2',
                  '3',
                  '4',
                  '5',
                  '6',
                  '7',
                  '8',
                  '9',
                  '10',
                  '11',
                  '12',
                  '13',
                  '14',
                  '15',
                  'Suoritettu'
                ])
              })

              after(editor.cancelChanges)
            })

            describe('Arvosanan muuttaminen', function () {
              before(
                editor.edit,
                arvosana.selectValue(6),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('onnistuu', function () {
                expect(
                  findFirst('.oppiaine.A .kurssi:first .arvosana')().text()
                ).to.equal('6')
              })
            })

            describe('Laajuuden muuttaminen', function () {
              before(
                editor.edit,
                lukukausi_11_I.toggleDetails,
                lukukausi_11_I.details().property('laajuus').setValue(2),
                lukukausi_11_I.toggleDetails,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              describe('Kun oppiaineella ei ole laajuutta', function () {
                before(lukukausi_11_I.showDetails)

                it('lukukauden laajuuden muutos tallentuu ja muutos näkyy osasuorituksen tiedoissa', function () {
                  expect(lukukausi_11_I.laajuus.getText()).to.equal(
                    'Laajuus 2 vuosiviikkotuntia'
                  )
                })
              })

              describe('Kun oppiaineella on laajuus', () => {
                before(
                  editor.edit,
                  aine.laajuus.setValue(5),
                  editor.saveChangesAndExpectError,
                  wait.until(page.isErrorShown)
                )

                describe('ja osasuoritusten laajuuden summa on eri kuin oppiaineen laajuus', () => {
                  it('muutos ei tallennu', function () {
                    expect(page.getErrorMessage()).to.equal(
                      'Suorituksen oppiaineetdia/A osasuoritusten laajuuksien summa 2.0 ei vastaa suorituksen laajuutta 5.0'
                    )
                  })
                })

                describe('ja osasuoritusten laajuuden summa on sama kuin oppiaineen laajuus', () => {
                  before(
                    lukukausi_11_II.showDetails,
                    lukukausi_11_II.details().property('laajuus').setValue(2),

                    lukukausi_12_I.showDetails,
                    lukukausi_12_I.details().property('laajuus').setValue(1),

                    editor.saveChanges,
                    wait.until(page.isSavedLabelShown)
                  )

                  describe('muutos tallentuu', () => {
                    describe('Lukukaudelle 11/I', () => {
                      before(lukukausi_11_I.showDetails)
                      it('näkyy osasuoritusten tiedoissa', function () {
                        expect(lukukausi_11_I.laajuus.getText()).to.equal(
                          'Laajuus 2 vuosiviikkotuntia'
                        )
                      })
                    })

                    describe('Lukukaudelle 11/II', () => {
                      before(lukukausi_11_II.showDetails)
                      it('näkyy osasuoritusten tiedoissa', function () {
                        expect(lukukausi_11_II.laajuus.getText()).to.equal(
                          'Laajuus 2 vuosiviikkotuntia'
                        )
                      })
                    })

                    describe('Lukukaudelle 12/I', () => {
                      before(lukukausi_12_I.showDetails)
                      it('näkyy osasuoritusten tiedoissa', function () {
                        expect(lukukausi_12_I.laajuus.getText()).to.equal(
                          'Laajuus 1 vuosiviikkotuntia'
                        )
                      })
                    })
                  })
                })
              })
            })

            describe('Lisääminen', function () {
              var dialog = aine.lisääKurssiDialog

              before(
                editor.edit,
                aine.avaaLisääKurssiDialog,
                dialog.valitseKurssi('12/II'),
                dialog.lisääKurssi,
                aine.kurssi('12/II').arvosana.selectValue(4),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('lisää osasuorituksen', function () {
                expect(extractAsText(S('.oppiaineet .A'))).to.contain(
                  '12/II\n4'
                )
              })

              describe('Lasketaan kokonaispistemäärään -tieto', () => {
                var kurssi = aine.kurssi('12/II')
                before(kurssi.showDetails)
                it('on merkitty todeksi', function () {
                  expect(
                    kurssi.lasketaanKokonaispistemäärään.getValue()
                  ).to.equal('kyllä')
                })
              })
            })

            describe('Lasketaan kokonaispistemäärään -valinnan poistaminen', function () {
              var kurssi = aine.kurssi('12/II')

              before(
                editor.edit,
                kurssi.toggleDetails,
                kurssi
                  .details()
                  .property('lasketaanKokonaispistemäärään')
                  .setValue(false),
                kurssi.toggleDetails,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown),
                aine.kurssi('12/II').showDetails
              )

              it('poistuu ja muutos näkyy osasuorituksen tiedoissa', function () {
                expect(
                  kurssi.lasketaanKokonaispistemäärään.getValue()
                ).to.equal('ei')
                expect(extractAsText(S('.oppiaineet .A'))).to.contain(
                  '12/II *\n4'
                )
              })
            })

            describe('Lasketaan kokonaispistemäärään -valinnan lisääminen', function () {
              var kurssi = aine.kurssi('12/II')

              before(
                editor.edit,
                kurssi.toggleDetails,
                kurssi
                  .details()
                  .property('lasketaanKokonaispistemäärään')
                  .setValue(true),
                kurssi.toggleDetails,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown),
                aine.kurssi('12/II').showDetails
              )

              it('tallentuu ja muutos näkyy osasuorituksen tiedoissa', function () {
                expect(
                  kurssi.lasketaanKokonaispistemäärään.getValue()
                ).to.equal('kyllä')
                expect(extractAsText(S('.oppiaineet .A'))).to.contain(
                  '12/II\n4'
                )
              })
            })

            describe('Kun neljä lukukautta on jo lisätty', function () {
              var dialog = aine.lisääKurssiDialog

              before(editor.edit, aine.avaaLisääKurssiDialog)

              it('voi lisätä enää kirjallisen tai suullisen kokeen tai näyttötutkinnon', function () {
                expect(dialog.kurssit()).to.deep.equal([
                  'Erityisosaamisen näyttötutkinto',
                  'Kirjallinen koe',
                  'Suullinen koe'
                ])
              })

              after(dialog.sulje, editor.cancelChanges)
            })

            describe('Päättökokeet', function () {
              function testaaPäättökokeenLisäysJaPoisto(nimi, arvosana2) {
                describe(nimi, function () {
                  var dialog = aine.lisääKurssiDialog

                  describe('Lisääminen', function () {
                    before(
                      editor.edit,
                      aine.avaaLisääKurssiDialog,
                      dialog.valitseKurssi(nimi),
                      dialog.lisääKurssi,
                      aine.kurssi(nimi).arvosana.selectValue(arvosana2),
                      aine.kurssi(nimi).toggleDetails,
                      aine
                        .kurssi(nimi)
                        .details()
                        .property('lasketaanKokonaispistemäärään')
                        .setValue(true),
                      aine.kurssi(nimi).toggleDetails,
                      editor.saveChanges,
                      wait.until(page.isSavedLabelShown)
                    )

                    it('lisää päättökokeen ja näyttää sen osasuorituslistauksessa', function () {
                      expect(aine.text()).to.contain(nimi + '\n' + arvosana2)
                    })
                  })

                  describe('Poistaminen', function () {
                    before(
                      editor.edit,
                      aine.kurssi(nimi).poistaKurssi,
                      editor.saveChanges,
                      wait.until(page.isSavedLabelShown)
                    )

                    it('poistaa päättökokeen eikä näytä sitä enää osasuorituslistauksessa', function () {
                      expect(aine.text()).to.not.contain(nimi)
                    })
                  })
                })
              }

              testaaPäättökokeenLisäysJaPoisto('Kirjallinen koe', 10)
              testaaPäättökokeenLisäysJaPoisto('Suullinen koe', 9)
              testaaPäättökokeenLisäysJaPoisto(
                'Erityisosaamisen näyttötutkinto',
                11
              )
            })

            describe('Poistaminen', function () {
              before(
                editor.edit,
                aine.kurssi('12/II').poistaKurssi,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('poistaa osasuorituksen', function () {
                expect(extractAsText(S('.oppiaineet .A'))).to.not.contain(
                  '12/II'
                )
              })
            })

            describe('Lisääminen ei-kronologisessa järjestyksessä', function () {
              var dialog = aine.lisääKurssiDialog

              function asetaKurssinLaajuus(kurssinTunniste, laajuus) {
                return seq(
                  aine.kurssi(kurssinTunniste).toggleDetails,
                  aine
                    .kurssi(kurssinTunniste)
                    .details()
                    .property('laajuus')
                    .setValue(laajuus),
                  aine.kurssi(kurssinTunniste).toggleDetails
                )
              }

              before(
                editor.edit,
                aine.kurssi('11/II').poistaKurssi,
                lisääOppiaineelleKurssiTunnisteella(aine, 'Kirjallinen koe'),
                lisääOppiaineelleKurssiTunnisteella(aine, '12/II'),
                asetaKurssinLaajuus('12/II', 1),
                lisääOppiaineelleKurssiTunnisteella(aine, '11/II'),
                asetaKurssinLaajuus('11/II', 1),
                editor.saveChanges
              )

              it('toimii', function () {
                expect(page.isSavedLabelShown()).to.equal(true)
              })

              it('kurssit esitetään kronologisessa järjestyksessä', function () {
                expect(extractAsText(S('.oppiaineet .A'))).to.equal(
                  'A-kieli, englanti\nSuorituskieli suomi\n11/I\n6 11/II\n9 12/I\n2 12/II\n9 Kirjallinen koe\n9 5'
                )
              })
            })
          })

          describe('Lisääminen', function () {
            var uusiOppiaine = OpinnotPage()
              .opiskeluoikeusEditor()
              .propertyBySelector('.uusi-oppiaine:last')

            before(editor.edit, uusiOppiaine.selectValue('Lakitieto'))

            it('lisää oppiaineen', function () {
              expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(18)
            })

            describe('Laajuuden tallentaminen uudelle oppiaineelle', function () {
              var lakitieto = opinnot.oppiaineet.oppiaine('oppiaine.LT')

              before(
                lakitieto.laajuus.setValue('1'),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('tallentaa oppiaineen laajuuksineen', function () {
                expect(extractAsText(S('.oppiaineet'))).to.contain('Lakitieto')
              })
            })
          })

          describe('Poistaminen', function () {
            var lakitieto = opinnot.oppiaineet.oppiaine('oppiaine.LT')

            before(
              editor.edit,
              lakitieto.poista(),
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )

            it('poistaa oppiaineen', function () {
              expect(extractAsText(S('.oppiaineet'))).to.not.contain(
                'Lakitieto'
              )
            })
          })
        })

        describe('Ryhmätön oppiaine', function () {
          var uusiRyhmätönOppiaineDropdown = OpinnotPage()
            .opiskeluoikeusEditor()
            .propertyBySelector('.uusi-oppiaine:last')

          before(
            editor.edit,
            uusiRyhmätönOppiaineDropdown.selectValue('Liikunta'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          var liikunta = opinnot.oppiaineet.oppiaine('oppiaine.LI')

          describe('Oppiaineen osasuoritukset', function () {
            describe('Lisääminen ei-kronologisessa järjestyksessä', function () {
              before(
                editor.edit,
                lisääOppiaineelleKurssiTunnisteella(liikunta, '12/I'),
                lisääOppiaineelleKurssiTunnisteella(liikunta, '11/I'),
                editor.saveChanges
              )

              it('toimii', function () {
                expect(page.isSavedLabelShown()).to.equal(true)
              })

              it('osasuoritukset esitetään kronologisessa järjestyksessä', function () {
                expect(extractAsText(S('.oppiaineet .LI'))).to.equal(
                  'Liikunta\n11/I\n9 12/I\n9'
                )
              })
            })
          })

          after(
            editor.edit,
            liikunta.poista(),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )
        })
      })
    })
  })

  describe('Opiskeluoikeuden lisääminen', function () {
    describe('Lisäysdialogi', function () {
      describe('DIA-tutkinnon valinta', function () {
        before(
          prepareForNewOppija('kalle', '020782-5339'),
          addOppija.enterValidDataDIA({
            etunimet: 'Doris',
            kutsumanimi: 'Doris',
            sukunimi: 'Dia'
          })
        )

        it('muuttaa automaattisesti suorituskieleksi saksan', function () {
          expect(extractAsText(S('.suorituskieli .select'))).to.equal('saksa')
        })
      })
    })

    describe('DIA-tutkinto', function () {
      before(
        prepareForNewOppija('kalle', '020782-5339'),
        addOppija.enterValidDataDIA({
          etunimet: 'Doris',
          kutsumanimi: 'Doris',
          sukunimi: 'Dia'
        }),
        addOppija.submitAndExpectSuccess(
          'Dia, Doris (020782-5339)',
          'Deutsche Internationale Abitur'
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
              'Koulutus Deutsche Internationale Abitur; Reifeprüfung\n' +
                'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
                'Suorituskieli saksa\n' +
                'Suoritus kesken'
            )
          })
        })

        describe('Oppiaineita', function () {
          it('ei esitäytetä', function () {
            expect(extractAsText(S('.oppiaineet'))).to.equal(
              'Oppiaine Laajuus (vuosiviikkotuntia)'
            )
          })
        })

        describe('Muokattaessa', function () {
          before(editor.edit)

          it('näytetään tyhjät(kin) osa-alueet sekä Lisäaineet-ryhmä', function () {
            expect(extractAsText(S('.oppiaineet .aineryhmä'))).to.equal(
              '' +
                'Kielet, kirjallisuus, taide\n\n' +
                'Matematiikka ja luonnontieteet\n\n' +
                'Yhteiskuntatieteet\n\n' +
                'Lisäaineet'
            )
          })

          it('näytetään jokaiselle osa-alueelle ja Lisäaineille uuden oppiaineen lisäys-dropdown', function () {
            expect(
              S('.oppiaineet .aineryhmä + .uusi-oppiaine .dropdown').length
            ).to.equal(4)
          })

          after(editor.cancelChanges)
        })

        describe('Valmistavan DIA-vaiheen suorituksen lisääminen', function () {
          var lisääSuoritus = opinnot.lisääSuoritusDialog
          var lisäysTeksti = 'lisää valmistavan DIA-vaiheen suoritus'

          describe('Kun opiskeluoikeus on tilassa VALMIS', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('valmistunut'),
              opiskeluoikeus.tallenna
            )

            it('Valmistavaa DIA-vaihetta ei voi lisätä', function () {
              expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(false)
            })

            after(editor.property('tila').removeItem(0))
          })

          describe('Kun opiskeluoikeus on tilassa LÄSNÄ', function () {
            describe('Ennen lisäystä', function () {
              it('Näytetään DIA-tutkinto', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Deutsche Internationale Abitur'
                ])
              })

              it('Valmistavan DIA-vaiheen voi lisätä', function () {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(true)
              })
            })

            describe('Lisäyksen jälkeen', function () {
              before(lisääSuoritus.clickLink(lisäysTeksti))

              it('Näytetään myös valmistavan DIA-vaiheen suoritus', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Deutsche Internationale Abitur',
                  'Valmistava DIA-vaihe'
                ])
              })

              it('Valmistavaa vaihetta ei voi enää lisätä', function () {
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
                    'Koulutus Valmistava DIA-vaihe\n' +
                      'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
                      'Suorituskieli saksa\n' +
                      'Suoritus kesken'
                  )
                })
              })
            })
          })
        })
      })
    })

    describe('Valmistava DIA-vaihe', function () {
      before(
        prepareForNewOppija('kalle', '020782-5339'),
        addOppija.enterValidDataDIAValmistavaVaihe({
          etunimet: 'Doris',
          kutsumanimi: 'Doris',
          sukunimi: 'Dia'
        }),
        addOppija.submitAndExpectSuccess(
          'Dia, Doris (020782-5339)',
          'Valmistava DIA-vaihe'
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
              'Koulutus Valmistava DIA-vaihe\n' +
                'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
                'Suorituskieli saksa\n' +
                'Suoritus kesken'
            )
          })
        })

        describe('Oppiaineita', function () {
          it('ei esitäytetä', function () {
            expect(extractAsText(S('.oppiaineet'))).to.equal(
              'Oppiaine Laajuus (vuosiviikkotuntia)'
            )
          })
        })

        describe('Muokattaessa', function () {
          before(editor.edit)

          it('näytetään tyhjät(kin) osa-alueet sekä Lisäaineet-ryhmä', function () {
            expect(extractAsText(S('.oppiaineet .aineryhmä'))).to.equal(
              '' +
                'Kielet, kirjallisuus, taide\n\n' +
                'Matematiikka ja luonnontieteet\n\n' +
                'Yhteiskuntatieteet\n\n' +
                'Lisäaineet'
            )
          })

          it('näytetään jokaiselle osa-alueelle uuden oppiaineen lisäys-dropdown', function () {
            expect(
              S('.oppiaineet .aineryhmä + .uusi-oppiaine .dropdown').length
            ).to.equal(4)
          })

          after(editor.cancelChanges)
        })

        describe('DIA-tutkinnon suorituksen lisääminen', function () {
          var lisääSuoritus = opinnot.lisääSuoritusDialog
          var lisäysTeksti = 'lisää DIA-tutkinnon suoritus'

          describe('Kun opiskeluoikeus on tilassa VALMIS', function () {
            before(
              editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().aseta('valmistunut'),
              opiskeluoikeus.tallenna
            )

            it('DIA-tutkinnon suoritusta ei voi lisätä', function () {
              expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(false)
            })

            after(editor.property('tila').removeItem(0))
          })

          describe('kun opiskeluoikeus on tilassa läsnä', function () {
            describe('Ennen lisäystä', function () {
              it('Näytetään valmistavan DIA-vaiheen suoritus', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Valmistava DIA-vaihe'
                ])
              })

              it('DIA-tutkinnon suorituksen voi lisätä', function () {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(true)
              })

              describe('Oppiaineita lisättäessä', function () {
                before(editor.edit, opinnot.lisääDIAValmistavaOppiaine)

                it('näytetään "lisää osasuoritus"-nappi', function () {
                  expect(extractAsText(S('.uusi-kurssi'))).to.equal(
                    'Lisää osasuoritus'
                  )
                })

                describe('Lisättäessä osasuoritus', function () {
                  before(opinnot.lisääDIAValmistavaOppiaineOsasuoritus)

                  it('Osasuorituksen arvosanan valinta tulee näkyviin', function () {
                    expect(
                      isElementVisible(
                        S('.kurssit .kurssi .arvosana .dropdown')
                      )
                    ).to.equal(true)
                  })

                  describe('Lisättäessä kaikki mahdolliset osasuoritukset', function () {
                    before(function () {
                      return new Promise(function (resolve) {
                        opinnot
                          .haeValmistavanOppiaineenOsasuoritustenVaihtoehtojenLukumäärä()
                          .then(function (amount) {
                            var chain = Promise.resolve()
                            for (var i = 0; i < amount; i++) {
                              chain = chain.then(function () {
                                return opinnot.lisääDIAValmistavaOppiaineOsasuoritus()
                              })
                            }
                            chain.then(function () {
                              resolve()
                            })
                          })
                      })
                    })

                    it('Piilotetaan "Lisää osasuoritus"-nappi', function () {
                      expect(isElementVisible(S('.uusi-kurssi'))).to.equal(
                        false
                      )
                    })

                    after(click('.suoritukset .remove-row a.remove-value'))
                  })
                })
              })
            })

            describe('Lisäyksen jälkeen', function () {
              before(lisääSuoritus.clickLink(lisäysTeksti))

              it('Näytetään myös DIA-tutkinnon suoritus', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Valmistava DIA-vaihe',
                  'Deutsche Internationale Abitur'
                ])
              })

              it('DIA-tutkinnon suoritusta ei voi enää lisätä', function () {
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
                    'Koulutus Deutsche Internationale Abitur; Reifeprüfung\n' +
                      'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
                      'Suorituskieli saksa\n' +
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
