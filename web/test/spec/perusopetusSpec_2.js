describe('Perusopetus 2', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var addOppija = AddOppijaPage()
  var editor = opinnot.opiskeluoikeusEditor()

  before(Authentication().login(), resetFixtures)

  describe('Tietojen muuttaminen', function () {
    describe('Päätason suorituksen poistaminen', function () {
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
        })

        after(resetFixtures)
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
            return eventually(() =>
              expect(addOppija.isEnabled()).to.equal(true)
            )()
          })

          it('Ei näytetä opintojen rahoitus -kenttää', function () {
            expect(addOppija.rahoitusIsVisible()).to.equal(false)
          })

          it('Oikeat tilat tilavaihtoehtoina', async function () {
            expect(await addOppija.opiskeluoikeudenTilat()).to.deep.equal([
              'Eronnut',
              'Katsotaan eronneeksi',
              'Läsnä',
              'Peruutettu',
              'Valmistunut',
              'Väliaikaisesti keskeytynyt'
            ])
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
        it('Näytetään oppimäärävaihtoehdot', async function () {
          expect(await addOppija.oppimäärät()).to.deep.equal([
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

        it('on valittavissa', async function () {
          expect(await addOppija.oppiaineet()).to.contain('Ei tiedossa')
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

            it('Näytetään suorituksen tyyppi opiskeluoikeuden otsikossa', function () {
              expect(S('.opiskeluoikeus h3 .koulutus').text()).to.equal(
                'Perusopetuksen oppiaineen oppimäärä'
              )
            })

            describe('muokkaustilassa', function () {
              before(editor.edit)

              it('näyttää pakollinen-kentän editorin', function () {
                expect(
                  extractAsText(
                    S('.suoritus > .properties .pakollinen')
                  )
                ).to.contain(
                  'Yhteinen oppiaine'
                )
              })
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
              }, 20000)()
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
            äidinkieli.kurssi('ÄI4').toggleDetails,
            äidinkieli.kurssi('ÄI4').arviointipäivä.setValue('1.1.2024'),
            äidinkieli.kurssi('ÄI4').toggleDetails,
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
        it('Näytetään oppimäärävaihtoehdot', async function () {
          expect(await addOppija.oppimäärät()).to.deep.equal([
            'Aikuisten perusopetuksen oppimäärän alkuvaihe',
            'Perusopetuksen oppiaineen oppimäärä',
            'Aikuisten perusopetuksen oppimäärä'
          ])
        })
      })

      describe('Ei-tiedossa oppiaine', function () {
        before(addOppija.selectOppimäärä('Perusopetuksen oppiaineen oppimäärä'))

        it('on valittavissa', async function () {
          expect(await addOppija.oppiaineet()).to.contain('Ei tiedossa')
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
            return eventually(() =>
              expect(addOppija.isEnabled()).to.equal(true)
            )()
          })
        })
      })
    })
  })
})
