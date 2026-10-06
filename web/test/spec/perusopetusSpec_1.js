describe('Perusopetus 1', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var tilaJaVahvistus = opinnot.tilaJaVahvistus
  var editor = opinnot.opiskeluoikeusEditor()

  before(Authentication().login(), resetFixtures)

  describe('Aikuisten perusopetus', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('280598-2415'))

    it('näyttää opiskeluoikeuden tiedot', function () {
      expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
        'Opiskeluoikeuden voimassaoloaika : 15.8.2008 — 4.6.2018\n' +
        'Tila 4.6.2018 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
        '15.8.2008 Läsnä (valtionosuusrahoitteinen koulutus)'
      )
    })

    describe('Muuttunut hetu', function () {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('280598-326W', '280598-2415')
      )

      it('hakee opiskeluoikeuden tiedot', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 15.8.2008 — 4.6.2018\n' +
          'Tila 4.6.2018 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
          '15.8.2008 Läsnä (valtionosuusrahoitteinen koulutus)'
        )
      })
    })

    describe('Päättövaiheen opinnot', function () {
      before(opinnot.expandAll)
      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Aikuisten perusopetuksen oppimäärä 201101 OPH-1280-2017\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Suoritustapa Erityinen tutkinto\n' +
          'Suorituskieli suomi\n' +
          'Täydentävät oman äidinkielen opinnot Arvosana 8\n' +
          'Kieli saame, lappi\n' +
          'Laajuus 1 kurssia\n' +
          'Suoritus valmis Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori'
        )
      })
      it('näyttää oppiaineiden arvosanat', function () {
        expect(extractAsText(S('.oppiaineet'))).to.equal(
          'Arviointiasteikko\n' +
          'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
          'Yhteiset oppiaineet\n' +
          'Oppiaine Arvosana\n' +
          'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\nÄI1\n9 ÄI2\n9 ÄI3\n9 ÄI4\n4 ÄI10\n9\n' +
          'B1-kieli, ruotsi 8\n' +
          'A1-kieli, englanti 8\n' +
          'Uskonto/Elämänkatsomustieto 10\n' +
          'Historia 8\n' +
          'Yhteiskuntaoppi 10\n' +
          'Matematiikka 9\n' +
          'Kemia 7\n' +
          'Fysiikka 9\n' +
          'Biologia 9\n' +
          'Maantieto 9\n' +
          'Musiikki 7\n' +
          'Kuvataide 8\n' +
          'Kotitalous 8\n' +
          'Terveystieto 8\n' +
          'Käsityö 9\n' +
          'Liikunta 9\n' +
          'Valinnaiset aineet\n' +
          'Oppiaine Arvosana Laajuus\n' +
          'B1-kieli, ruotsi S 1 vuosiviikkotuntia\n' +
          'Kotitalous S 1 vuosiviikkotuntia\n' +
          'Liikunta S 0,5 vuosiviikkotuntia\n' +
          'B2-kieli, saksa 9 4 vuosiviikkotuntia\n' +
          'Tietokoneen hyötykäyttö 9\n' +
          'Kuvaus Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.'
        )
      })

      describe('Tietojen muuttaminen', function () {
        describe('Kurssin lisääminen', function () {
          var äidinkieli = opinnot.oppiaineet.oppiaine(0)
          var a1 = opinnot.oppiaineet.oppiaine('A1')
          describe('Valtakunnallinen kurssi', function () {
            before(editor.edit, äidinkieli.avaaLisääKurssiDialog)
            describe('Ennen kurssin lisäämistä', function () {
              it('Voidaan lisätä vain ne kurssit, joita ei vielä ole lisätty', function () {
                expect(äidinkieli.lisääKurssiDialog.kurssit()).to.include(
                  'ÄI5 Puhe- ja vuorovaikutustaidot'
                )
                expect(äidinkieli.lisääKurssiDialog.kurssit()).not.to.include(
                  'ÄI1 Suomen kielen ja kirjallisuuden perusteet'
                )
              })
            })
            describe('Kun lisätään kurssi', function () {
              before(
                äidinkieli.lisääKurssiDialog.valitseKurssi(
                  'Puhe- ja vuorovaikutustaidot'
                ),
                äidinkieli.lisääKurssiDialog.lisääKurssi
              )

              describe('Ennen arvosanan syöttöä', function () {
                it('Tallentaminen ei ole mahdollista ja virheilmoitus näytetään (koska päätason suoritus on valmis)', function () {
                  expect(editor.canSave()).to.equal(false)
                  expect(äidinkieli.errorText()).to.equal(
                    'Arvosana vaaditaan, koska päätason suoritus on merkitty valmiiksi.'
                  )
                })
              })

              describe('Kun annetaan arvosana ja tallennetaan', function () {
                before(
                  äidinkieli.kurssi('ÄI5').arvosana.setValue('8'),
                  äidinkieli.kurssi('ÄI5').toggleDetails,
                  äidinkieli.kurssi('ÄI5').arviointipäivä.setValue('1.1.2024'),
                  äidinkieli.kurssi('ÄI5').toggleDetails,
                  editor.saveChanges
                )

                it('Kurssin tiedot näytetään oikein', function () {
                  expect(äidinkieli.text()).to.equal(
                    'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\nÄI1\n9 ÄI2\n9 ÄI3\n9 ÄI4\n4 ÄI10\n9 ÄI5\n8'
                  )
                })

                describe('Kurssin poistaminen', function () {
                  before(
                    editor.edit,
                    äidinkieli.kurssi('ÄI5').poistaKurssi,
                    editor.saveChanges
                  )

                  it('Toimii', function () {
                    expect(äidinkieli.text()).to.equal(
                      'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\nÄI1\n9 ÄI2\n9 ÄI3\n9 ÄI4\n4 ÄI10\n9'
                    )
                  })
                })
              })

              describe('Kun päätason suoritus on KESKEN-tilassa', function () {
                before(
                  editor.edit,
                  äidinkieli.lisääKurssi('Puhe- ja vuorovaikutustaidot'),
                  editor.property('tila').removeItem(0),
                  opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi
                )
                describe('Kun oppiaineen suoritus on VALMIS-tilassa', function () {
                  it('Keskeneräista kurssisuoritusta ei voi tallentaa', function () {
                    expect(editor.canSave()).to.equal(false)
                  })
                })
                describe('Kun oppiaineen suoritus on KESKEN-tilassa', function () {
                  before(äidinkieli.arvosana.setValue('Ei valintaa'))
                  it('Keskeneräisen kurssisuorituksen voi tallentaa', function () {
                    expect(editor.canSave()).to.equal(true)
                  })
                })
              })
            })
          })

          describe('Äidinkielen kurssivalikoima', function () {
            function testaaÄidinkielenKurssivaihtoehdot(
              oppimäärä,
              odotetutKurssit
            ) {
              describe(
                'Kun oppimääräksi on valittuna "' + oppimäärä + '"',
                function () {
                  before(
                    editor.edit,
                    äidinkieli.kurssi('ÄI1').poistaKurssi,
                    äidinkieli.kurssi('ÄI2').poistaKurssi,
                    äidinkieli.kurssi('ÄI3').poistaKurssi,
                    äidinkieli.kurssi('ÄI10').poistaKurssi,
                    äidinkieli
                      .propertyBySelector('.kieli')
                      .selectValue(oppimäärä),
                    äidinkieli.avaaLisääKurssiDialog
                  )

                  it('Näytetään oikea kurssivalikoima', function () {
                    expect(
                      äidinkieli.lisääKurssiDialog.kurssit()
                    ).to.deep.equal(odotetutKurssit)
                  })

                  after(
                    äidinkieli.lisääKurssiDialog.sulje,
                    editor.cancelChanges
                  )
                }
              )
            }

            testaaÄidinkielenKurssivaihtoehdot('Suomen kieli ja kirjallisuus', [
              'ÄI1 Suomen kielen ja kirjallisuuden perusteet',
              'ÄI10 Nykykulttuurin ilmiöitä ja kirjallisuutta',
              'ÄI2 Monimuotoiset tekstit',
              'ÄI3 Tekstien tuottaminen ja tulkitseminen',
              'ÄI5 Puhe- ja vuorovaikutustaidot',
              'ÄI6 Median maailma',
              'ÄI7 Kauno- ja tietokirjallisuuden lukeminen',
              'ÄI8 Tekstien tulkinta',
              'ÄI9 Tekstien tuottaminen',
              'Lisää paikallinen kurssi...'
            ])
            testaaÄidinkielenKurssivaihtoehdot(
              'Suomi toisena kielenä ja kirjallisuus',
              [
                'S21 Opiskelutaitojen vahvistaminen',
                'S210 Ajankohtaiset ilmiöt Suomessa ja maailmalla',
                'S22 Luonnontieteen tekstit tutummiksi',
                'S23 Yhteiskunnallisten aineiden tekstit tutummiksi',
                'S24 Median tekstejä ja kuvia',
                'S25 Tiedonhankintataitojen syventäminen',
                'S26 Uutistekstit',
                'S27 Mielipiteen ilmaiseminen ja perusteleminen',
                'S28 Kaunokirjalliset tekstit tutuiksi',
                'S29 Kulttuurinen moninaisuus - moninainen kulttuuri',
                'Lisää paikallinen kurssi...'
              ]
            )
            testaaÄidinkielenKurssivaihtoehdot(
              'Ruotsin kieli ja kirjallisuus',
              [
                'MO1 Ruotsin kielen ja kirjallisuuden perusteet',
                'MO10 Nykykulttuurin ilmiöitä ja kirjallisuutta',
                'MO2 Monimuotoiset tekstit',
                'MO3 Tekstien tuottaminen ja tulkitseminen',
                'MO4 Kieli ja kulttuuri',
                'MO5 Puhe- ja vuorovaikutustaidot',
                'MO6 Median maailma',
                'MO7 Kauno- ja tietokirjallisuuden lukeminen',
                'MO8 Tekstien tulkinta',
                'MO9 Tekstien tuottaminen',
                'Lisää paikallinen kurssi...'
              ]
            )
          })

          describe('Kieliaineiden kurssit', function () {
            before(editor.edit, a1.avaaLisääKurssiDialog)
            it('Näytetään vain oikean oppiaineen ja kielen kurssit', function () {
              expect(a1.lisääKurssiDialog.kurssit()).to.deep.equal([
                'ENA1 Kehittyvä kielitaito: Työelämässä toimiminen ja muita muodollisia tilanteita',
                'ENA2 Kehittyvä kielitaito: Palvelu- ja viranomaistilanteet ja osallistuva kansalainen',
                'ENA3 Kehittyvä kielitaito: Kertomuksia minusta ja ympäristöstäni',
                'ENA4 Kehittyvä kielitaito: Ajankohtaiset ilmiöt',
                'ENA5 Kulttuurikohtaamisia',
                'ENA6 Globaalienglanti',
                'ENA7 Liikkuvuus ja kansainvälisyys',
                'ENA8 Avaimet elinikäiseen kieltenopiskeluun',
                'Lisää paikallinen kurssi...'
              ])
            })

            after(a1.lisääKurssiDialog.sulje, editor.cancelChanges)
          })

          describe('Paikallinen kurssi', function () {
            before(
              editor.edit,
              äidinkieli.avaaLisääKurssiDialog,
              äidinkieli.lisääKurssiDialog.valitseKurssi(
                'Lisää paikallinen kurssi...'
              ),
              äidinkieli.lisääKurssiDialog
                .property('koodiarvo')
                .setValue('ÄIX1'),
              äidinkieli.lisääKurssiDialog
                .property('nimi')
                .setValue('Äidinkielen paikallinen erikoiskurssi'),
              äidinkieli.lisääKurssiDialog.lisääKurssi,
              äidinkieli.kurssi('ÄIX1').arvosana.setValue('10'),
              äidinkieli.kurssi('ÄIX1').toggleDetails,
              äidinkieli.kurssi('ÄIX1').arviointipäivä.setValue('1.1.2024'),
              äidinkieli.kurssi('ÄIX1').toggleDetails,
              editor.saveChanges
            )
            it('Toimii', function () { })
          })
        })
      })

      describe('Oman äidinkielen opinnot', function () {
        before(editor.edit)

        it('Näyttää arvosananvalinnat oikeassa järjestyksessä', function () {
          expect(
            Page(S('.omanÄidinkielenOpinnot')).getInputOptions(
              '.arvosana .dropdown'
            )
          ).to.deep.equal(['4', '5', '6', '7', '8', '9', '10', 'H', 'O', 'S'])
        })

        after(editor.cancelChanges)
      })
    })

    describe('Alkuvaiheen opinnot', function () {
      before(
        opinnot.valitseSuoritus(
          undefined,
          'Aikuisten perusopetuksen oppimäärän alkuvaihe'
        ),
      )
      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Aikuisten perusopetuksen oppimäärän alkuvaihe aikuistenperusopetuksenoppimaaranalkuvaihe OPH-1280-2017\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Suoritustapa Erityinen tutkinto\n' +
          'Suorituskieli suomi\n' +
          'Täydentävät oman äidinkielen opinnot Arvosana 8\n' +
          'Kieli saame, lappi\n' +
          'Laajuus 1 kurssia\n' +
          'Suoritus valmis Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori'
        )
      })
      it('näyttää oppiaineiden arvosanat', function () {
        expect(extractAsText(S('.oppiaineet'))).to.equal(
          'Arviointiasteikko\nArvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
          'Oppiaine Arvosana\n' +
          'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\nLÄI1\n9 LÄI2\n9 LÄI3\n9 LÄI4\n9 LÄI5\n9 LÄI6\n9 LÄI7\nO LÄI8\n9 LÄI9\n9 AÄI1\n9 AÄI2\n9 AÄI3\n9 AÄI4\n9 AÄI5\n9 AÄI6\n9\n' +
          'Vieras kieli, englanti 7\nAENA1\n9 AENA2\n9 AENA3\n9 AENA4\n9\n' +
          'Matematiikka 10\nLMA1\n9 LMA2\n9 LMA3\n9\n' +
          'Yhteiskuntatietous ja kulttuurintuntemus 8\nLYK1\n9 LYK2\n9 LYKX\n9 LYKY\n9\n' +
          'Ympäristö- ja luonnontieto 8\nLYL1\n9\n' +
          'Terveystieto 10\nATE1\n9\n' +
          'Opinto-ohjaus ja työelämän taidot S'
        )
      })

      describe('Tietojen muuttaminen', function () {
        describe('Oppiaineen arvosana', function () {
          var matematiikka = editor.subEditor('.MA > tr:first-child')
          before(
            editor.edit,
            matematiikka
              .propertyBySelector('.arvosana')
              .selectValue('Ei valintaa'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('Ei ole pakollinen', function () { })
        })

        describe('Kurssin lisääminen', function () {
          var äidinkieli = opinnot.oppiaineet.oppiaine(0)
          describe('Valtakunnallinen kurssi', function () {
            before(
              editor.edit,
              äidinkieli.avaaLisääKurssiDialog,
              äidinkieli.lisääKurssiDialog.valitseKurssi('AÄI7'),
              äidinkieli.lisääKurssiDialog.lisääKurssi,
              äidinkieli.kurssi('AÄI7').arvosana.setValue('8'),
              äidinkieli.kurssi('AÄI7').toggleDetails,
              äidinkieli.kurssi('AÄI7').arviointipäivä.setValue('1.1.2024'),
              äidinkieli.kurssi('AÄI7').toggleDetails,

              äidinkieli.avaaLisääKurssiDialog,
              äidinkieli.lisääKurssiDialog.valitseKurssi('IS21'),
              äidinkieli.lisääKurssiDialog.lisääKurssi,
              äidinkieli.kurssi('IS21').arvosana.setValue('8'),
              äidinkieli.kurssi('IS21').toggleDetails,
              äidinkieli.kurssi('IS21').arviointipäivä.setValue('1.1.2024'),
              äidinkieli.kurssi('IS21').toggleDetails,

              äidinkieli.avaaLisääKurssiDialog,
              äidinkieli.lisääKurssiDialog.valitseKurssi('IMO1'),
              äidinkieli.lisääKurssiDialog.lisääKurssi,
              äidinkieli.kurssi('IMO1').arvosana.setValue('8'),
              äidinkieli.kurssi('IMO1').toggleDetails,
              äidinkieli.kurssi('IMO1').arviointipäivä.setValue('1.1.2024'),
              äidinkieli.kurssi('IMO1').toggleDetails,
              editor.saveChanges
            )

            it('Kurssin tiedot näytetään oikein', function () {
              expect(äidinkieli.text()).to.equal(
                'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\nLÄI1\n9 LÄI2\n9 LÄI3\n9 LÄI4\n9 LÄI5\n9 LÄI6\n9 LÄI7\nO LÄI8\n9 LÄI9\n9 AÄI1\n9 AÄI2\n9 AÄI3\n9 AÄI4\n9 AÄI5\n9 AÄI6\n9 AÄI7\n8 IS21\n8 IMO1\n8'
              )
            })
          })

          describe('Paikallinen kurssi', function () {
            before(
              editor.edit,
              äidinkieli.avaaLisääKurssiDialog,
              äidinkieli.lisääKurssiDialog.valitseKurssi(
                'Lisää paikallinen kurssi...'
              ),
              äidinkieli.lisääKurssiDialog
                .property('koodiarvo')
                .setValue('ÄIX1'),
              äidinkieli.lisääKurssiDialog
                .property('nimi')
                .setValue('Äidinkielen paikallinen erikoiskurssi'),
              äidinkieli.lisääKurssiDialog.lisääKurssi,
              äidinkieli.kurssi('ÄIX1').arvosana.setValue('10'),
              äidinkieli.kurssi('ÄIX1').toggleDetails,
              äidinkieli.kurssi('ÄIX1').arviointipäivä.setValue('1.1.2024'),
              äidinkieli.kurssi('ÄIX1').toggleDetails,
              editor.saveChanges
            )
            it('Toimii', function () { })
          })
        })
      })
    })
  })

  describe('Aikuisten perusopetuksen opiskeluoikeuden tilan asettaminen Valmistunut tilaan', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('280598-2415'),
      editor.edit
    )

    describe('Kun vain oppimäärällä on vahvistus', function () {
      before(
        editor.property('tila').removeItem(0),
        opinnot.valitseSuoritus(
          undefined,
          'Aikuisten perusopetuksen oppimäärän alkuvaihe'
        ),
        opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
        opinnot.avaaLisaysDialogi
      )

      it('Valmistunut tila voidaan asettaa', function () {
        expect(OpiskeluoikeusDialog().radioEnabled('valmistunut')).to.equal(
          true
        )
      })

      after(opinnot.suljeLisaysDialogi, editor.cancelChanges)
    })

    describe('Alkuvaiheen olleessa ainut suoritus', function () {
      before(
        editor.edit,
        opinnot.deletePäätasonSuoritus,
        wait.prepareForNavigation,
        opinnot.confirmDeletePäätasonSuoritus,
        wait.forNavigation,
        wait.until(page.isPäätasonSuoritusDeletedMessageShown),
        wait.until(page.isReady),
        opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
          'aikuistenperusopetus'
        )
      )

      describe('Ja sillä on vahvistus', function () {
        before(editor.property('tila').removeItem(0), opinnot.avaaLisaysDialogi)

        it('Valmistunu tila voidaan asettaa', function () {
          expect(OpiskeluoikeusDialog().radioEnabled('valmistunut')).to.equal(
            true
          )
        })

        after(opinnot.suljeLisaysDialogi)
      })

      describe('Ja sillä ei ole vahvistusta', function () {
        before(
          opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
          opinnot.avaaLisaysDialogi
        )

        it('Valmistunut tila on estetty', function () {
          expect(OpiskeluoikeusDialog().radioEnabled('valmistunut')).to.equal(
            false
          )
        })
      })
    })

    after(opinnot.suljeLisaysDialogi, editor.cancelChanges)
  })
})
