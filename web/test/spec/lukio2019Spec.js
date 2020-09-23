describe('Lukiokoulutus2019', function( ){
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()
  var addOppija = AddOppijaPage()
  var opiskeluoikeus = OpiskeluoikeusDialog()
  before(Authentication().login(), resetFixtures)

  describe('Lukion päättötodistus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('250605A518Y'), opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('lukiokoulutus'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal("Lukion oppimäärä")
        expect(opinnot.getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('näyttää opiskeluoikeuden tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal('Opiskeluoikeuden voimassaoloaika : 1.8.2019 — 15.5.2020\n' +
          'Tila 15.5.2020 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
          '1.8.2019 Läsnä (valtionosuusrahoitteinen koulutus)'
        )
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Lukion oppimäärä 309902 OPH-2263-2019\n' +
          'Opetussuunnitelma Lukio suoritetaan nuorten opetussuunnitelman mukaan\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Opetuskieli suomi\n' +
          'Lukion oppimäärää täydentävät oman äidinkielen opinnot Arvosana 8\n' +
          'Kieli saame, lappi\n' +
          'Laajuus 3 op\n' +
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
          'Todistuksella näkyvät lisätiedot Osallistunut kansalliseen etäopetuskokeiluun\n' +
          'Ryhmä AH\n' +
          'Koulusivistyskieli suomi\n' +
          'Suoritus valmis Vahvistus : 15.5.2020 Jyväskylä Reijo Reksi , rehtori'
        )
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Arvioitu (opintopistettä) Hyväksytysti arvioitu (opintopistettä) Arvosana\n' +
          'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus\nOÄI1\n8 OÄI2\n8 OÄI3\n8 6 6 9\n' +
          'Matematiikka, pitkä oppimäärä\nMAB2\n8 MAB3\n8 MAB4\n9 6 6 9\n' +
          'Opinto-ohjaus\nOP1\nH OP2\nS 4 2 H\n' +
          'Uskonto/Elämänkatsomustieto\nUE1\n4 1,5 0 4\n' +
          'Äidinkielenomainen kieli, ruotsi\nRUA4\n7 1 1 9\n' +
          'Fysiikka\nFY1\n10 FY2\n10 FY3\n10 FY123 *\n10 FY124 *\nS 8 8 10\n' +
          'Kemia 0 0 4\nTanssi ja liike *\nLI5\n7 ITT234 *\n10 3 3 8\n' +
          'Lukiodiplomit\nMELD5\n7 KÄLD3\n9 4 4\n' +
          'Muut suoritukset\nKE3\n10 HAI765 *\nS 3 3\n' +
          'Teemaopinnot\nKAN200 *\nS 1 1\n' +
          'Arvioitujen osasuoritusten laajuus yhteensä: 37,5 Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä: 34,0\n' +
          '* = paikallinen opintojakso tai oppiaine')
      })
    })

    describe('Moduulin tiedot', function() {
      var moduuli = opinnot.oppiaineet.oppiaine('FY').kurssi('FY124')
      before(page.openPage, page.oppijaHaku.searchAndSelect('250605A518Y'), opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('lukiokoulutus'))
      describe('Kun klikataan', function() {
        before(moduuli.toggleDetails)
        it('näyttää moduulin tiedot', function() {
          expect(moduuli.detailsText()).to.equal(
            'Tunniste FY124\n' +
            'Nimi Keittiöfysiikka 2\n' +
            'Laajuus 1 op\n' +
            'Kuvaus Haastava kokeellinen keittiöfysiikka, liekitys ja lämpöreaktiot\n' +
            'Pakollinen ei')
        })
      })
      describe('Kun klikataan uudestaan', function() {
        before(moduuli.toggleDetails)
        it('piilottaa moduulin tiedot', function() {
          expect(moduuli.detailsText()).to.equal('')
        })
      })
      describe('Kaikkien moduulien tiedot', function() {
        it('voidaan avata yksitellen virheettömästi', function() {
          Kurssi.findAll().forEach(function(moduuli) {
            expect(moduuli.detailsText()).to.equal('')
            moduuli.toggleDetails()
            expect(moduuli.detailsText().length > 10).to.equal(true)
          })
        })
      })
    })

    describe('Tietojen muuttaminen', function() {
      describe('Suoritusten tiedot', function() {
        describe('Oppiaine', function() {
          before(editor.edit)

          var ai = opinnot.oppiaineet.oppiaine('oppiaine.AI')
          var kieli = ai.propertyBySelector('.title .properties')
          var arvosana = ai.propertyBySelector('td.arvosana')
          var suoritettuErityisenäTutkintona = ai.propertyBySelector('.properties .suoritettuErityisenäTutkintona')
          var suorituskieli = ai.propertyBySelector('.properties .suorituskieli')

          describe('Alkutila', function () {
            it('on oikein', function() {
              expect(editor.canSave()).to.equal(false)
              expect(kieli.getValue()).to.equal('Suomen kieli ja kirjallisuus')
              expect(arvosana.getValue()).to.equal('9')
              expect(suoritettuErityisenäTutkintona.getText()).to.equal('Suoritettu erityisenä tutkintona')
              expect(suorituskieli.getValue()).to.equal('Ei valintaa')
            })
          })

          describe('Kieliaineen kielen muuttaminen', function() {
            before(kieli.selectValue('Ruotsin kieli ja kirjallisuus'))

            it('onnistuu', function() {
              expect(kieli.getValue()).to.equal('Ruotsin kieli ja kirjallisuus')
            })

            it('tallennus on mahdollista', function() {
              expect(editor.canSave()).to.equal(true)
            })
          })

          describe('Suorituskielen muuttaminen', function() {
            before(suorituskieli.selectValue('arabia'))

            it('onnistuu', function() {
              expect(suorituskieli.getValue()).to.equal('arabia')
            })

            it('tallennus on mahdollista', function() {
              expect(editor.canSave()).to.equal(true)
            })
          })

          describe('Arvosanan muuttaminen', function () {
            before(arvosana.selectValue(8), editor.saveChanges, wait.until(page.isSavedLabelShown))

            it('onnistuu', function () {
              expect(findSingle('.oppiaine.AI .arvosana .annettuArvosana')().text()).to.equal('8')
            })
          })

          describe('Valtakunnallinen oppiaine', function() {
            var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine()
            var psykologia = editor.subEditor('.oppiaine.PS:eq(0)')

            describe('Lisääminen', function () {
              before(
                editor.edit,
                uusiOppiaine.selectValue('Psykologia')
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.contain('Psykologia')
              })

              it('arvosana vaaditaan kun päätason suoritus on merkitty valmiiksi', function () {
                expect(editor.canSave()).to.equal(false)
                expect(extractAsText(S('.oppiaineet'))).to.contain('Arvosana vaaditaan, koska päätason suoritus on merkitty valmiiksi.')
              })

              describe('Arvosanan kanssa', function () {
                before(
                  psykologia.propertyBySelector('.arvosana').selectValue('9'),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('tallennus toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.contain('Psykologia')
                })
              })
            })

            describe('Poistaminen', function () {
              before(
                editor.edit,
                psykologia.propertyBySelector('.remove-row').removeValue,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.not.contain('Psykologia')
              })
            })
          })

          describe('Paikallinen oppiaine', function() {
            before(editor.edit)

            var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine()
            var paikallinen = editor.subEditor('.oppiaine.oppiaine-rivi:last')

            it('alkutila', function() {
              expect(editor.canSave()).to.equal(false)
              expect(editor.getEditBarMessage()).to.equal('Ei tallentamattomia muutoksia')
              expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(11)
            })

            describe('Lisääminen', function () {
              before(
                editor.edit,
                uusiOppiaine.selectValue('Lisää')
              )

              it('lisää oppiaineen', function () {
                expect(S('.oppiaineet .oppiaine-rivi').length).to.equal(12)
              })

              it('estää tallennuksen kunnes pakolliset tiedot on täytetty', function () {
                expect(editor.canSave()).to.equal(false)
                expect(editor.getEditBarMessage()).to.equal('Korjaa virheelliset tiedot.')
              })

              describe('Tiedot täytettynä', function () {
                before(
                  paikallinen.propertyBySelector('.koodi').setValue('PAI'),
                  paikallinen.propertyBySelector('.nimi').setValue('Paikallinen oppiaine'),
                  paikallinen.propertyBySelector('.kuvaus').setValue('Pakollinen kuvaus'),
                  paikallinen.propertyBySelector('.arvosana').selectValue(9),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('tallennus toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.contain('Paikallinen oppiaine')
                })
              })
            })

            describe('Poistaminen', function () {
              before(
                editor.edit,
                paikallinen.propertyBySelector('.remove-row').removeValue,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.not.contain('Paikallinen oppiaine')
              })
            })

            describe('Lisätty paikallinen oppiaine', function() {
              before(editor.edit)

              it('tallettuu organisaation preferenceihin', function() {
                expect(uusiOppiaine.getOptions()).to.contain('Paikallinen oppiaine')
              })

              after(editor.cancelChanges)
            })

            describe('Organisaation preferenceistä löytyvä aine', function() {
              describe('Lisääminen', function () {
                before(
                  editor.edit,
                  uusiOppiaine.selectValue('Paikallinen oppiaine'),
                  paikallinen.propertyBySelector('.arvosana').selectValue(9),
                  editor.saveChanges
                )

                it('toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.contain('Paikallinen oppiaine')
                })
              })
            })
          })

          describe('Oppiaineen moduuli', function() {
            before(
              editor.edit,
              editor.property('tila').removeItem(0),
              opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi
            )

            describe('Arvosanan muuttaminen', function() {
              var moduuli = opinnot.oppiaineet.oppiaine('MA').kurssi('MAB4')

              before(moduuli.arvosana.selectValue('6'), editor.saveChanges, wait.until(page.isSavedLabelShown))

              it('Toimii', function() {
                expect(moduuli.arvosana.getText()).to.equal('6')
              })
            })

            describe('Osaamisen tunnustaminen', function() {
              var moduuli = opinnot.oppiaineet.oppiaine('MA').kurssi('MAB4')

              before(
                editor.edit,
                moduuli.toggleDetails
              )

              describe('Alussa', function() {
                it('ei osaamisen tunnustamistietoa, näytetään lisäysmahdollisuus', function() {
                  expect(moduuli.tunnustettu.getValue()).to.equal('Lisää osaamisen tunnustaminen')
                })
              })

              describe('Lisääminen', function () {
                before(
                  moduuli.lisääTunnustettu,
                  moduuli.tunnustettu.propertyBySelector('.selite').setValue('Tunnustamisen esimerkkiselite'),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown),
                  moduuli.showDetails
                )

                it('toimii', function() {
                  expect(moduuli.tunnustettu.getText()).to.equal(
                    'Tunnustettu\nSelite Tunnustamisen esimerkkiselite\nRahoituksen piirissä ei'
                  )
                })
              })

              describe('Rahoituksen piirissä -tiedon lisääminen', function () {
                before(
                  editor.edit,
                  moduuli.toggleDetails,
                  moduuli.tunnustettu.property('rahoituksenPiirissä').setValue(true),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown),
                  moduuli.showDetails
                )

                it('toimii', function() {
                  expect(moduuli.tunnustettu.getText()).to.equal(
                    'Tunnustettu\nSelite Tunnustamisen esimerkkiselite\nRahoituksen piirissä kyllä'
                  )
                })
              })

              describe('Poistaminen', function () {
                before(
                  editor.edit,
                  moduuli.toggleDetails,
                  moduuli.poistaTunnustettu,
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown),
                  editor.edit,
                  moduuli.toggleDetails
                )

                it('toimii', function() {
                  expect(moduuli.tunnustettu.getValue()).to.equal('Lisää osaamisen tunnustaminen')
                })

                after(editor.cancelChanges)
              })
            })

            describe('Lisääminen', function () {
              before(
                editor.edit,
                opinnot.oppiaineet.oppiaine('AOM').lisääLaajuudellinenOpintojakso('RUA2', 2),
                opinnot.oppiaineet.oppiaine('AOM').kurssi('RUA2').arvosana.selectValue('9'),
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet .AOM'))).to.contain('RUA2')
              })
            })

            describe('Poistaminen', function () {
              var oai2 = ai.kurssi('OÄI2')

              before(
                editor.edit,
                oai2.poistaKurssi,
                editor.saveChanges,
                wait.until(page.isSavedLabelShown)
              )

              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet .AI'))).to.not.contain('OÄI2')
              })
            })
          })
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
  })

  describe('Lukion oppiaineiden oppimäärien suoritus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('010705A6119'))

    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal("Lukion oppiaineet")
        expect(opinnot.getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
      })
    })

    describe('Kaikki tiedot näkyvissä', function () {
      it('näyttää opiskeluoikeuden tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 1.8.2019 — 15.5.2020\n' +
          'Tila 15.5.2020 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
          '1.8.2019 Läsnä (valtionosuusrahoitteinen koulutus)')
      })

      it('näyttää suorituksen tyypin opiskeluoikeuden otsikossa', function () {
        expect(S('.opiskeluoikeus h3 .koulutus').text()).to.equal('Lukion oppiaineet')
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Lukion oppiaineet OPH-2263-2019\n' +
          'Opetussuunnitelma Lukio suoritetaan nuorten opetussuunnitelman mukaan\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Opetuskieli suomi')
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Oppiaine Arvioitu (opintopistettä) Hyväksytysti arvioitu (opintopistettä) Arvosana\n' +
          'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus\nOÄI1\n8 OÄI2\n8 OÄI3\n8 6 6 9\n' +
          'Matematiikka, pitkä oppimäärä\nMAB2\n8 MAB3\n8 MAB4\n9 6 6 9\n' +
          'Opinto-ohjaus\nOP1\nH OP2\nS 4 2 H\n' +
          'Uskonto/Elämänkatsomustieto\nUE1\n4 1,5 0 4\n' +
          'Äidinkielenomainen kieli, ruotsi\nRUA4\n7 1 1 9\n' +
          'Fysiikka\nFY1\n10 FY2\n10 FY3\n10 FY123 *\n10 FY124 *\nS 8 8 10\n' +
          'Kemia 0 0 4\n' +
          'Tanssi ja liike *\nLI5\n7 ITT234 *\n10 3 3 8\n' +
          'Arvioitujen osasuoritusten laajuus yhteensä: 29,5 Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä: 26,0\n' +
          '* = paikallinen opintojakso tai oppiaine')
      })

      describe('valmistumistieto poistettaessa', function() {
        const currentDate = new Date()
        function currentDatePlusYears(years) {return currentDate.getDate() + '.' + (1 + currentDate.getMonth()) + '.' + (currentDate.getFullYear() + years)}
        const currentDateStr = currentDatePlusYears(0)

        before(
          editor.edit,
          editor.property('tila').removeItem(0)
        )

        describe('valmistumistiedon lisääminen', function() {
          before(
            opinnot.avaaLisaysDialogi,
            opiskeluoikeus.tila().aseta('valmistunut'),
            opiskeluoikeus.opintojenRahoitus().aseta('1'),
            opiskeluoikeus.tallenna,
            editor.saveChanges
          )
          it('onnistuu vaikka suoritusta ei ole merkitty vahvistetuksi', function() {
            expect(opinnot.opiskeluoikeusEditor().päättymispäivä()).to.equal(currentDateStr)
          })
        })
      })
    })
  })

  describe('Opiskeluoikeuden lisääminen', function() {
    describe('Lukion oppimäärä', function() {
      describe('Nuorten 2019 oppimäärä', function() {
        before(
          prepareForNewOppija('kalle', '140981-334Y'),
          addOppija.enterValidDataLukio({ oppilaitos: 'Ressun', oppimäärä: 'Lukion oppimäärä 2019 opetussuunnitelman mukaan', peruste: 'OPH-2263-2019', opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus' }),
          addOppija.selectOppimäärä('Lukion oppimäärä 2019 opetussuunnitelman mukaan'),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (140981-334Y)', 'Lukion oppimäärä')
        )

        describe('Lisäyksen jälkeen', function () {
          describe('Opiskeluoikeuden tiedot', function() {
            it('näytetään oikein', function () {
              expect(S('.koulutusmoduuli .tunniste').text()).to.equal('Lukion oppimäärä')
              expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('OPH-2263-2019')
              expect(editor.propertyBySelector('.oppimäärä').getValue()).to.equal('Lukio suoritetaan nuorten opetussuunnitelman mukaan')
              expect(editor.propertyBySelector('.toimipiste').getValue()).to.equal('Ressun lukio')
              expect(opinnot.getSuorituskieli()).to.equal('suomi')
            })
          })

          describe('Oppiaineita', function () {
            before(editor.edit)
            it('ei esitäytetä', function () {
              expect(S('.oppiaineet').text()).to.equal('')
            })
            after(editor.cancelChanges)
          })
        })
      })

      describe('Aikuisten 2019 oppimäärä', function() {
        before(
          prepareForNewOppija('kalle', '170491-517B'),
          addOppija.enterValidDataLukio({ oppilaitos: 'Ressun', oppimäärä: 'Lukion oppimäärä 2019 opetussuunnitelman mukaan', peruste: 'OPH-2263-2019', opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus' }),
          addOppija.selectOppimäärä('Lukion oppimäärä 2019 opetussuunnitelman mukaan'),
          addOppija.selectPeruste('OPH-2267-2019'),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (170491-517B)', 'Lukion oppimäärä')
        )

        describe('Lisäyksen jälkeen', function () {
          describe('Opiskeluoikeuden tiedot', function() {
            it('näytetään oikein', function () {
              expect(S('.koulutusmoduuli .tunniste').text()).to.equal('Lukion oppimäärä')
              expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('OPH-2267-2019')
              expect(editor.propertyBySelector('.oppimäärä').getValue()).to.equal('Lukio suoritetaan aikuisten opetussuunnitelman mukaan')
              expect(editor.propertyBySelector('.toimipiste').getValue()).to.equal('Ressun lukio')
              expect(opinnot.getSuorituskieli()).to.equal('suomi')
            })
          })
        })
      })
    })

    describe('Lukion oppiaineen oppimäärä', function() {
      describe('Nuorten 2019 oppimäärä', function() {
        before(
          prepareForNewOppija('kalle', '260613-0652'),
          addOppija.enterValidDataLukio({ oppilaitos: 'Ressun', oppimäärä: 'Lukion oppiaineet', peruste: 'OPH-2263-2019', opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus' }),
          addOppija.selectOppimäärä('Lukion oppiaineet'),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (260613-0652)', 'Lukion oppiaineet')
        )

        describe('Lisäyksen jälkeen', function () {
          describe('Opiskeluoikeuden tiedot', function() {
            it('näytetään oikein', function () {
              expect(S('.koulutusmoduuli .tunniste').text()).to.equal('Lukion oppiaineet')
              expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('OPH-2263-2019')
              expect(editor.propertyBySelector('.toimipiste').getValue()).to.equal('Ressun lukio')
              expect(opinnot.getSuorituskieli()).to.equal('suomi')
            })
          })
        })
      })

      describe('Aikuisten 2019 oppimäärä', function() {
        before(
          prepareForNewOppija('kalle', '051009-409T'),
          addOppija.enterValidDataLukio({ oppilaitos: 'Ressun', oppimäärä: 'Lukion oppiaineet', peruste: 'OPH-2267-2019', opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus' }),
          addOppija.selectOppimäärä('Lukion oppiaineet'),
          addOppija.selectPeruste('OPH-2267-2019'),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (051009-409T)', 'Lukion oppiaineet')
        )

        describe('Lisäyksen jälkeen', function () {
          describe('Opiskeluoikeuden tiedot', function() {
            it('näytetään oikein', function () {
              expect(S('.koulutusmoduuli .tunniste').text()).to.equal('Lukion oppiaineet')
              expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('OPH-2267-2019')
              expect(editor.propertyBySelector('.toimipiste').getValue()).to.equal('Ressun lukio')
              expect(opinnot.getSuorituskieli()).to.equal('suomi')
            })
          })
        })
      })
    })
  })
})
