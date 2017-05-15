describe('Perusopetus', function() {
  var page = KoskiPage()
  var login = LoginPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()
  var tilaJaVahvistus = opinnot.tilaJaVahvistus
  var addOppija = AddOppijaPage()
  var opiskeluoikeus = OpiskeluoikeusDialog()
  var editor = opinnot.opiskeluoikeusEditor()
  var currentDate = new Date().getDate() + '.' + (1 + new Date().getMonth()) + '.' + new Date().getFullYear()

  before(Authentication().login(), resetFixtures)

  describe('Perusopetuksen lukuvuositodistukset ja päättötodistus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('220109-784L'))

    it('näyttää opiskeluoikeuden tiedot', function() {
      expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal('Alkamispäivä : 15.8.2008 — Päättymispäivä : 4.6.2016\n' +
        'Tila 4.6.2016 Valmistunut\n' +
        '15.8.2008 Läsnä')
    })

    describe('Perusopetuksen oppimäärä', function() {
      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('näyttää suorituksen tiedot', function() {
          expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal('Koulutus Peruskoulu 104/011/2014\n' +
            'Opetussuunnitelma Perusopetus\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Suoritustapa Koulutus\n' +
            'Suoritus: VALMIS Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori')
        })
        it('näyttää oppiaineiden arvosanat', function() {
          expect(extractAsText(S('.oppiaineet'))).to.equal(
            'Oppiaineiden arvosanat\n' +
            'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
            'Pakolliset oppiaineet\n' +
            'Oppiaine Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\n' +
            'B1-kieli, ruotsi 8\n' +
            'A1-kieli, englanti 8\n' +
            'Uskonto tai elämänkatsomustieto 10\n' +
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
            'Valinnaiset oppiaineet\n' +
            'Oppiaine Arvosana Laajuus\n' +
            'Valinnainen b1-kieli, ruotsi S 1 vuosiviikkotuntia\n' +
            'Valinnainen kotitalous S 1 vuosiviikkotuntia\n' +
            'Valinnainen liikunta S 0.5 vuosiviikkotuntia\n' +
            'Valinnainen b2-kieli, saksa 9 4 vuosiviikkotuntia\n' +
            'Tietokoneen hyötykäyttö 9\n' +
            'Kuvaus Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.\n' +
            '* = yksilöllistetty oppimäärä, ** = painotettu opetus'
          )
        })
      })

      describe('Päättötodistus', function() {
        before(opinnot.avaaTodistus())
        describe('Klikattaessa linkkiä', function() {
          it('näytetään', function() {
            // See more detailed content specification in PerusopetusSpec.scala
            expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
            expect(todistus.arvosanarivi('.muut-opinnot')).to.equal('Muut valinnaiset opinnot')
          })
        })
      })
    })

    describe('Lukuvuosisuoritus 8. luokka', function() {
      before(TodistusPage().close, wait.until(page.isOppijaSelected('Kaisa')), opinnot.valitseSuoritus(1, '8. vuosiluokka'))
      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('näyttää suorituksen tiedot', function() {
          expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
            'Luokka-aste 8. vuosiluokka\n' +
            'Luokka 8C\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Alkamispäivä 15.8.2014\n' +
            'Suorituskieli suomi\n' +
            'Muut suorituskielet sloveeni\n' +
            'Kielikylpykieli ruotsi\n' +
            'Suoritus: VALMIS Vahvistus : 30.5.2015 Jyväskylä Reijo Reksi , rehtori\n' +
            'Siirretään seuraavalle luokalle')
        })
        it('näyttää oppiaineiden arvosanat', function() {
          expect(extractAsText(S('.oppiaineet'))).to.equal(
            'Oppiaineiden arvosanat\n' +
            'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
            'Pakolliset oppiaineet\n' +
            'Oppiaine Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\n' +
            'B1-kieli, ruotsi 8\n' +
            'A1-kieli, englanti 8\n' +
            'Uskonto tai elämänkatsomustieto 10\n' +
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
            'Valinnaiset oppiaineet\n' +
            'Oppiaine Arvosana Laajuus\n' +
            'Valinnainen b1-kieli, ruotsi S 1 vuosiviikkotuntia\n' +
            'Valinnainen kotitalous S 1 vuosiviikkotuntia\n' +
            'Valinnainen liikunta S 0.5 vuosiviikkotuntia\n' +
            'Valinnainen b2-kieli, saksa 9 4 vuosiviikkotuntia\n' +
            'Tietokoneen hyötykäyttö 9\n' +
            'Kuvaus Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.\n' +
            'Käyttäytymisen arviointi\n' +
            'Arvosana S\n' +
            'Sanallinen arviointi Esimerkillistä käyttäytymistä koko vuoden ajan\n' +
            '* = yksilöllistetty oppimäärä, ** = painotettu opetus'
          )
        })
      })
      describe('Lukuvuositodistus', function() {
        before(opinnot.avaaTodistus())
        it('näytetään', function() {})
      })
    })

    describe('Lukuvuosisuoritus 9. luokka', function() {
      before(TodistusPage().close, wait.until(page.isOppijaSelected('Kaisa')), opinnot.valitseSuoritus(1, '9. vuosiluokka'))
      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('näyttää suorituksen tiedot', function() {
          expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
            'Luokka-aste 9. vuosiluokka\n' +
            'Luokka 9C\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Alkamispäivä 15.8.2015\n' +
            'Suorituskieli suomi\n' +
            'Suoritus: VALMIS Vahvistus : 30.5.2016 Jyväskylä Reijo Reksi , rehtori')
        })
      })
      describe('Lukuvuositodistus', function() {
        it('ei näytetä', function() {
          expect(S('a.todistus').is(':visible')).to.equal(false)
        })
      })
    })

    describe('Luokalle jäänyt 7-luokkalainen', function() {
      before(TodistusPage().close, wait.until(page.isOppijaSelected('Kaisa')), opinnot.valitseSuoritus(1, '7. vuosiluokka'))
      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('näyttää suorituksen tiedot', function() {
          expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
            'Luokka-aste 7. vuosiluokka\n' +
            'Luokka 7C\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Alkamispäivä 15.8.2013\n' +
            'Suorituskieli suomi\n' +
            'Suoritus: VALMIS Vahvistus : 30.5.2014 Jyväskylä Reijo Reksi , rehtori\n' +
            'Ei siirretä seuraavalle luokalle')
        })
      })
    })

    describe('Päättötodistus toiminta-alueittain', function() {
      before(Authentication().login(), page.openPage, page.oppijaHaku.searchAndSelect('031112-020J'))
      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)

        it('näyttää opiskeluoikeuden tiedot', function() {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal('Alkamispäivä : 15.8.2008 — Päättymispäivä : 4.6.2016\n' +
            'Tila 4.6.2016 Valmistunut\n' +
            '15.8.2008 Läsnä\n' +
            'Lisätiedot\n' +
            'Perusopetuksen aloittamista lykätty kyllä\n' +
            'Aloittanut ennen oppivelvollisuutta ei\n' +
            'Pidennetty oppivelvollisuus 15.8.2008 — 4.6.2016\n' +
            'Tukimuodot Osa-aikainen erityisopetus\n' +
            'Erityisen tuen päätös 15.8.2008 — 4.6.2016\n' +
            'Opiskelee toiminta-alueittain kyllä\n' +
            'Opiskelee erityisryhmässä kyllä\n' +
            'Tehostetun tuen päätös 15.8.2008 — 4.6.2016\n' +
            'Joustava perusopetus 15.8.2008 — 4.6.2016\n' +
            'Kotiopetus 15.8.2008 — 4.6.2016\n' +
            'Ulkomailla 15.8.2008 — 4.6.2016\n' +
            'Vuosiluokkiin sitoutumaton opetus kyllä')
        })

        it('näyttää suorituksen tiedot', function() {
          expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
            'Koulutus Peruskoulu 104/011/2014\n' +
            'Opetussuunnitelma Aikuisten perusopetus\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Suoritustapa Erityinen tutkinto\n' +
            'Suoritus: VALMIS Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori')
        })
        it('näyttää oppiaineiden arvosanat', function() {
          expect(extractAsText(S('.oppiaineet'))).to.equal('Oppiaineiden arvosanat\n' +
            'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
            'Oppiaine Arvosana\n' +
            'motoriset taidot S\n' +
            'Sanallinen arviointi Motoriset taidot kehittyneet hyvin perusopetuksen aikana\n' +
            'kieli ja kommunikaatio S\n' +
            'sosiaaliset taidot S\n' +
            'päivittäisten toimintojen taidot S\n' +
            'kognitiiviset taidot S')
        })
      })

      describe('Tietojen muuttaminen', function() {
        describe('Oppiaineet', function() {
          var uusiOppiaine = editor.propertyBySelector('.uusi-oppiaine')
          var sosiaalisetTaidot = editor.subEditor('.3')

          describe('Poistaminen', function () {
            before(editor.edit, sosiaalisetTaidot.propertyBySelector('tr').removeValue, editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('toimii', function () {
              expect(extractAsText(S('.oppiaineet'))).to.not.contain('Filosofia 8')
            })

            describe('Lisääminen', function() {
              before(editor.edit, uusiOppiaine.selectValue('sosiaaliset taidot'), sosiaalisetTaidot.propertyBySelector('.arvosana').selectValue('8'), editor.saveChanges, wait.until(page.isSavedLabelShown))
              it('toimii', function () {
                expect(extractAsText(S('.oppiaineet'))).to.contain('sosiaaliset taidot 8')
              })
            })
          })
        })

        describe('Vuosiluokan suorituksen lisäys', function() {
          var lisääSuoritus = opinnot.lisääSuoritusDialog()
          before(editor.edit, editor.property('tila').removeItem(0),
            opinnot.lisääSuoritus, lisääSuoritus.property('luokka').setValue('1a'), lisääSuoritus.toimipiste.select('Jyväskylän normaalikoulu, alakoulu'),
            lisääSuoritus.lisääSuoritus, editor.saveChanges
          )
          it('Esitäyttää toiminta-alueet', function() {
            expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal(['motoriset taidot',
              'kieli ja kommunikaatio',
              'sosiaaliset taidot',
              'päivittäisten toimintojen taidot',
              'kognitiiviset taidot'])
          })

        })
      })

      describe('Tulostettava todistus', function() {
        before(opinnot.valitseSuoritus(1, 'Peruskoulu'), opinnot.avaaTodistus(0))
        it('näytetään', function() {
          // See more detailed content specification in PerusopetusSpec.scala
          expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
        })
      })
    })

    describe('Virhetilanteet', function() {
      describe('Todistuksen avaaminen, kun käyttäjä ei ole kirjautunut', function() {
        before(Authentication().logout, reloadTestFrame, wait.until(login.isVisible))
        it('Näytetään login-sivu', function() {
          expect(login.isVisible()).to.equal(true)
        })
      })

      describe('Todistuksen avaaminen, kun todistusta ei löydy', function() {
        before(Authentication().login(), page.openPage, openPage('/koski/1010101010', page.is404))
        it('Näytetään 404-sivu', function() {} )
      })
    })
  })

  describe('Hetuton oppija', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('1.2.246.562.24.99999999123', 'Hetuton'))

    it('näyttää opiskeluoikeuden tiedot', function() {
      expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
        'Alkamispäivä : 15.8.2008 — Päättymispäivä :\n' +
        'Tila 15.8.2008 Läsnä'
      )
    })
  })

  describe('Tietojen muuttaminen', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('220109-784L'))

    describe('Opiskeluoikeuden tiedot', function() {
      it('Alkutila', function() {
        expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(true)
        expect(opinnot.opiskeluoikeusEditor().subEditor('.property.tila').propertyBySelector('label.tila:contains("Valmistunut")').isVisible()).to.equal(false)
      })

      describe('Opiskeluoikeuden tila', function() {
        before(editor.edit, editor.property('tila').removeItem(0), editor.saveChanges, wait.until(page.isSavedLabelShown))

        describe('Eronnut', function() {
          it('Alkutila', function() {
            expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
          })

          describe('Kun lisätään', function() {
            before(editor.edit, opinnot.avaaLisaysDialogi, opiskeluoikeus.tila().click('input[value="eronnut"]'), opiskeluoikeus.tallenna, editor.saveChanges)
            it('Opiskeluoikeuden päättymispäivä asetetaan', function() {
              expect(editor.property('päättymispäivä').getValue()).to.equal(currentDate)
            })

            it('Opiskeluoikeuden tilaa ei voi lisätä kun opiskeluoikeus on päättynyt', function() {
              expect(isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))).to.equal(false)
            })
          })

          describe('Kun poistetaan', function() {
            before(editor.edit, editor.property('tila').removeItem(0), editor.saveChanges)

            it('Opiskeluoikeuden päättymispäivä poistetaan', function() {
              expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
            })

            describe('editoitaessa', function() {
              before(editor.edit)
              it('Opiskeluoikeuden tilan voi lisätä', function() {
                expect(isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))).to.equal(true)
              })
              after(editor.cancelChanges)
            })

          })
        })

        describe('Valmistunut', function() {
          it('Alkutila', function() {
            expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
          })

          describe('Kun lisätään', function() {
            before(editor.edit, opinnot.avaaLisaysDialogi, opiskeluoikeus.tila().click('input[value="valmistunut"]'), opiskeluoikeus.tallenna, editor.saveChanges)

            it('Opiskeluoikeuden päättymispäivä asetetaan', function() {
              expect(editor.property('päättymispäivä').getValue()).to.equal(currentDate)
            })


            describe('editoitaessa', function() {
              before(editor.edit)
              it('Opiskeluoikeuden tilaa ei voi lisätä kun opiskeluoikeus on päättynyt', function() {
                expect(isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))).to.equal(false)
              })
              after(editor.cancelChanges)
            })

          })

          describe('Kun poistetaan', function() {
            before(editor.edit, editor.property('tila').removeItem(0), editor.saveChanges)

            it('Opiskeluoikeuden päättymispäivä poistetaan', function() {
              expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
            })

            describe('editoitaessa', function() {
              before(editor.edit)
              it('Opiskeluoikeuden tilan voi lisätä', function() {
                expect(isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))).to.equal(true)
              })
              after(editor.cancelChanges)
            })
          })

          describe('Kun suoritus on kesken', function() {
            before(page.openPage, page.oppijaHaku.searchAndSelect('160932-311V'), opinnot.opiskeluoikeusEditor().edit, opinnot.avaaLisaysDialogi)

            it('Valmistunut-tilaa ei voi lisätä', function() {
              expect(OpiskeluoikeusDialog().radioEnabled('valmistunut')).to.equal(false)
            })
          })
        })

        describe('Katsotaan eronneeksi', function() {
          before(page.oppijaHaku.searchAndSelect('220109-784L'), editor.edit)
          it('Alkutila', function() {
            expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
          })

          describe('Kun lisätään', function() {
            before(opinnot.avaaLisaysDialogi, opiskeluoikeus.tila().click('input[value="katsotaaneronneeksi"]'), opiskeluoikeus.tallenna, editor.saveChanges, wait.until(page.isSavedLabelShown))

            it('Opiskeluoikeuden päättymispäivä asetetaan', function() {
              expect(editor.property('päättymispäivä').getValue()).to.equal(currentDate)
            })

            it('Opiskeluoikeuden tilaa ei voi lisätä kun opiskeluoikeus on päättynyt', function() {
              expect(isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))).to.equal(false)
            })
          })

          describe('Kun poistetaan', function() {
            before(editor.edit, editor.property('tila').removeItem(0), editor.saveChanges)

            it('Opiskeluoikeuden päättymispäivä poistetaan', function() {
              expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
            })
          })
        })

        describe('Läsnä', function() {
          it('Alkutila', function() {
            expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
          })
          describe('Kun lisätään', function() {
            before(editor.edit, opinnot.avaaLisaysDialogi, opiskeluoikeus.tila().click('input[value="lasna"]'), opiskeluoikeus.tallenna, editor.saveChanges)

            it('Opiskeluoikeuden päättymispäivää ei aseteta', function() {
              expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
            })

            describe('editoitaessa', function() {
              before(editor.edit)
              it('Opiskeluoikeuden tilan voi lisätä', function() {
                expect(isElementVisible(S('.opiskeluoikeuden-tiedot .add-item a'))).to.equal(true)
              })
              after(editor.cancelChanges)
            })
          })
        })

        describe('Väliaikaisesti keskeytynyt', function() {
          it('Alkutila', function() {
            expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
          })
          describe('Kun lisätään', function() {
            before(editor.edit, opinnot.avaaLisaysDialogi, opiskeluoikeus.tila().click('input[value="valiaikaisestikeskeytynyt"]'), opiskeluoikeus.tallenna, editor.saveChanges)

            it('Opiskeluoikeuden päättymispäivää ei aseteta', function() {
              expect(opinnot.opiskeluoikeusEditor().property('päättymispäivä').isVisible()).to.equal(false)
            })
          })
        })

        describe('Päivämäärän validointi', function() {
          before(editor.edit, opinnot.avaaLisaysDialogi)
          describe('Virheellinen päivämäärä', function() {
            before(opiskeluoikeus.tila().click('input[value="eronnut"]'), opiskeluoikeus.alkuPaiva().setValue('11.1.200'))
            it('Tallennus on estetty', function() {
              expect(opiskeluoikeus.isEnabled()).to.equal(false)
            })
          })

          describe('Virheellinen päivämäärä ja tilan muutos', function() {
            before(opiskeluoikeus.tila().click('input[value="eronnut"]'), opiskeluoikeus.alkuPaiva().setValue('11.1.200'), opiskeluoikeus.tila().click('input[value="valmistunut"]'))
            it('Tallennus on estetty', function() {
              expect(opiskeluoikeus.isEnabled()).to.equal(false)
            })
          })

          describe('Uusi alkupäivä on aikaisempi kuin viimeisen tilan alkupäivämäärä', function() {
            before(opiskeluoikeus.tila().click('input[value="eronnut"]'), opiskeluoikeus.alkuPaiva().setValue('14.8.2008'))
            it('Tallennus on estetty', function() {
              expect(opiskeluoikeus.isEnabled()).to.equal(false)
            })
          })

          describe('Virheellinen päivämäärä korjattu oikeelliseksi', function() {
            before(opiskeluoikeus.tila().click('input[value="eronnut"]'), opiskeluoikeus.alkuPaiva().setValue('11.1.200'), opiskeluoikeus.alkuPaiva().setValue(currentDate))
            it('Tallennus on sallittu', function() {
              expect(opiskeluoikeus.isEnabled()).to.equal(true)
            })
          })
        })

        describe('Aktiivinen tila', function() {
          var tila = opinnot.opiskeluoikeusEditor().property('tila')
          it('Viimeinen tila kun päivämäärä ei ole tulevaisuudessa', function() {
            expect(findSingle('.opiskeluoikeusjakso', tila.getItems()[0].elem()).hasClass('active')).to.equal(true)
          })

          describe('Kun lisätään useita tähän päivään', function() {
            before(editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().click('input[value="valiaikaisestikeskeytynyt"]'),
              opiskeluoikeus.tallenna,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().click('input[value="lasna"]'),
              opiskeluoikeus.tallenna,
              editor.saveChanges)

            it('Viimeinen tila on aktiivinen', function() {
              var jaksoElems = tila.elem().find('.opiskeluoikeusjakso')
              toArray(jaksoElems).slice(1).forEach(function(e) {
                expect(S(e).hasClass('active')).to.equal(false)
              })
              expect(S(jaksoElems[0]).hasClass('active')).to.equal(true)
            })
          })

          describe('Kun lisätään tulevaisuuteen "väliaikaisesti keskeytynyt"', function() {
            before(editor.edit,
              opinnot.avaaLisaysDialogi,
              opiskeluoikeus.tila().click('input[value="valiaikaisestikeskeytynyt"]'),
              opiskeluoikeus.alkuPaiva().setValue('9.5.2117'),
              opiskeluoikeus.tallenna,
              editor.saveChanges)

            describe('Käyttöliittymän tila', function() {
              it('Nykyinen tila "läsnä" on aktiivinen', function() {
                expect(findSingle('.opiskeluoikeusjakso', tila.getItems()[1].elem()).hasClass('active')).to.equal(true)
              })
            })

            describe('Oppijataulukossa', function() {
              before(
                opinnot.backToList,
                page.oppijataulukko.filterBy('nimi', 'Koululainen Kaisa'), page.oppijataulukko.filterBy('tyyppi', 'Perusopetus')
              )

              it('Näytetään nykyinen tila "läsnä"', function() {
                expect(page.oppijataulukko.isVisible()).to.equal(true)
                expect(page.oppijataulukko.findOppija('Koululainen, Kaisa', 'Perusopetus')).to.deep.equal([ 'Koululainen, Kaisa',
                  'Perusopetus',
                  'Perusopetuksen oppimäärä',
                  'Peruskoulu',
                  'Läsnä',
                  'Jyväskylän normaalikoulu',
                  '15.8.2008',
                  '9C' ])

              })
            })
          })
        })

      })

      describe('Opiskeluoikeuden lisätiedot', function() {
        before(page.oppijaHaku.selectOppija('220109-784L'))
        before(editor.edit, opinnot.expandAll, editor.property('perusopetuksenAloittamistaLykätty').setValue(true), editor.saveChanges, wait.until(page.isSavedLabelShown))
        describe('Lisätietojen lisäys', function() {
          it('Toimii', function() {
            expect(editor.property('perusopetuksenAloittamistaLykätty').getValue()).to.equal('kyllä')
          })
        })

        describe('Kun lisätiedot piilotetaan ja näytetään uudestaan', function() {
          before(opinnot.collapseAll, opinnot.expandAll)
          it('Toimii', function() {
            expect(editor.property('perusopetuksenAloittamistaLykätty').getValue()).to.equal('kyllä')
          })
        })

        describe('Kun lisätiedot piilotetaan, siirrytään muokkaukseen, avataan lisätiedot, poistutaan muokkauksesta', function() {
          before(opinnot.collapseAll, editor.edit, opinnot.expandAll, editor.cancelChanges)
          it('Toimii', function() {
            expect(editor.property('perusopetuksenAloittamistaLykätty').getValue()).to.equal('kyllä')
          })
          after(editor.edit, opinnot.expandAll, editor.property('perusopetuksenAloittamistaLykätty').setValue(false), editor.saveChanges)
        })

        describe('Erityisen tuen päätös', function() {
          describe('lisätään, kun erityisen tuen tietoja asetetaan', function() {
            before(editor.edit, opinnot.expandAll, editor.property('opiskeleeToimintaAlueittain').setValue(true), editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('Toimii', function() {
              expect(editor.property('opiskeleeToimintaAlueittain').getValue()).to.equal('kyllä')
            })
          })
          describe('poistetaan, kun erityisen tuen tiedot tyhjennetään', function() {
            before(editor.edit, opinnot.expandAll, editor.property('opiskeleeToimintaAlueittain').setValue(false), editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('Toimii', function() {
              expect(isElementVisible(S('.property.erityisenTuenPäätös'))).to.equal(false)
            })
          })
        })

        describe('Päivämäärän syöttö', function() {
          var pidennettyOppivelvollisuus = editor.property('pidennettyOppivelvollisuus').toPäivämääräväli()
          before(editor.edit, opinnot.expandAll)
          describe('Virheellinen päivämäärä', function() {
            before(pidennettyOppivelvollisuus.setAlku('34.9.2000'))
            it('Estää tallennuksen', function() {
              expect(opinnot.onTallennettavissa()).to.equal(false)
            })
          })
          describe('Oikeellinen päivämäärä', function() {
            before(pidennettyOppivelvollisuus.setAlku(currentDate), editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('Tallennus onnistuu', function() {
              expect(pidennettyOppivelvollisuus.getAlku()).to.equal(currentDate)
            })
          })
        })

        describe('Virheellinen päivämääräväli', function() {
          var pidennettyOppivelvollisuus = editor.property('pidennettyOppivelvollisuus').toPäivämääräväli()
          before(editor.edit, opinnot.expandAll, pidennettyOppivelvollisuus.setAlku(currentDate), pidennettyOppivelvollisuus.setLoppu('1.2.2008'))
          it('Estää tallennuksen', function() {
            expect(pidennettyOppivelvollisuus.isValid()).to.equal(false)
            expect(opinnot.onTallennettavissa()).to.equal(false)
          })
          after(pidennettyOppivelvollisuus.setLoppu(currentDate), editor.saveChanges, wait.until(page.isSavedLabelShown))
        })

        describe('Tukimuodot', function() {
          describe('Lisättäessä ensimmäinen', function() {
            before(editor.edit, editor.propertyBySelector('.tukimuodot .add-item').setValue('Erityiset apuvälineet'), editor.saveChanges)
            it('Toimii', function() {
              expect(editor.property('tukimuodot').getValue()).to.equal('Erityiset apuvälineet')
            })

            describe('Lisättäessä toinen', function() {
              before(editor.edit, editor.propertyBySelector('.tukimuodot .add-item').setValue('Tukiopetus'), editor.saveChanges)
              it('Toimii', function() {
                expect(editor.property('tukimuodot').getValue()).to.equal('Erityiset apuvälineetTukiopetus')
              })

              describe('Poistettaessa ensimmäinen', function() {
                before(editor.edit, editor.property('tukimuodot').removeItem(0), editor.saveChanges)
                it('Toimii', function() {
                  expect(editor.property('tukimuodot').getValue()).to.equal('Tukiopetus')
                })

                describe('Poistettaessa viimeinen', function() {
                  before(editor.edit, editor.property('tukimuodot').removeItem(0), editor.saveChanges)
                  it('Toimii', function() {
                    expect(editor.property('tukimuodot').isVisible()).to.equal(false)
                  })
                })
              })
            })
          })
        })
      })
    })

    describe('Suoritusten tiedot', function() {
      describe('Luokan muuttaminen', function() {
        before(opinnot.valitseSuoritus(1, '9. vuosiluokka'), editor.edit)
        describe('Tyhjäksi', function() {
          before(editor.property('luokka').setValue(''))
          it('Näyttää validaatiovirheen', function() {
            expect(editor.property('luokka').isValid()).to.equal(false)
          })
          it('Tallennus on estetty', function() {
            expect(editor.canSave()).to.equal(false)
          })
          it('Näytetään virheviesti myös tallennuspalkissa', function() {
            expect(editor.getEditBarMessage()).to.equal('Korjaa virheelliset tiedot.')
          })
        })
        describe('Ei tyhjäksi', function() {
          before(editor.property('luokka').setValue('9C'))
          it('Poistaa validaatiovirheen', function() {
            expect(editor.property('luokka').isValid()).to.equal(true)
          })
          it('Tallennus on sallittu', function() {
            expect(editor.canSave()).to.equal(true)
          })
          it('Poistetaan virheviesti myös tallennuspalkista', function() {
            expect(editor.getEditBarMessage()).to.equal('Tallentamattomia muutoksia')
          })

          describe('Tallennus', function() {
            before(editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('Onnistuu', function() {})
          })
        })
      })
      describe('Tutkinnon perusteen diaarinumero', function() {
        var diaarinumero = editor.propertyBySelector('.diaarinumero')
        before(editor.edit, diaarinumero.setValue('1/011/2004 Perusopetuksen opetussuunnitelman perusteet 2004'), editor.saveChanges)
        it('toimii', function () {
          expect(diaarinumero.getValue()).to.equal('1/011/2004')
        })
      })
      describe('Suorituskielen lisäys', function() {
        before(opinnot.valitseSuoritus(1, 'Peruskoulu'), editor.edit, editor.property('suorituskieli').selectValue('suomi'), editor.saveChanges, wait.until(page.isSavedLabelShown))
        it('muutettu suorituskieli näytetään', function() {
          expect(editor.property('suorituskieli').getValue()).to.equal('suomi')
        })
      })
      describe('Todistuksella näkyvät lisätiedot', function() {
        describe('lisäys', function() {
          var lisätiedot = editor.property('todistuksellaNäkyvätLisätiedot')
          before(opinnot.valitseSuoritus(1, 'Peruskoulu'), editor.edit, lisätiedot.setValue('Testitesti'), editor.saveChanges, wait.until(page.isSavedLabelShown))
          it('Uudet lisätiedot näytetään', function() {
            expect(lisätiedot.getValue()).to.equal('Testitesti')
          })
        })
        describe('poisto', function() {
          var lisätiedot = editor.property('todistuksellaNäkyvätLisätiedot')
          before(opinnot.valitseSuoritus(1, 'Peruskoulu'),
            editor.edit,
            lisätiedot.setValue('Testitesti'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown),
            editor.edit,
            lisätiedot.setValue(''),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown))
          it('Lisätiedot piilotetaan', function() {
            expect(lisätiedot.isVisible()).to.equal(false)
          })
        })
        describe('lisäys ja poisto kerralla', function() {
          var lisätiedot = editor.property('todistuksellaNäkyvätLisätiedot')
          before(opinnot.valitseSuoritus(1, 'Peruskoulu'), editor.edit, lisätiedot.setValue('Testitesti'), wait.forAjax, lisätiedot.setValue(''), editor.saveChanges, wait.until(page.isSavedLabelShown))
          it('Lisätiedot piilotetaan', function() {
            expect(lisätiedot.isVisible()).to.equal(false)
          })
        })
      })
      describe('Vieraan kielen valinta', function() {

        describe('kielivalinnan muuttaminen', function() {
          var b1kieli = editor.subEditor('.oppiaineet tbody:eq(1) tr:eq(0)')
          var kieli = b1kieli.propertyBySelector('.oppiaine')
          before(editor.edit, editor.property('laajuus').setValue('2'), kieli.selectValue('saksa'), editor.saveChanges)
          it('muutettu kielivalinta näytetään', function() {
            expect(kieli.getValue()).to.equal('saksa')
          })
        })
        describe('kielien järjestys listassa', function() {
          before(editor.edit)
          it('on oikein', function() {
            expect(textsOf(S('.oppiaine.B1.pakollinen .oppiaine .options li'))).to.deep.equal(
              ['suomi',
              'ruotsi',
              'englanti',
              'Ei suoritusta',
              'espanja',
              'italia',
              'japani',
              'kiina',
              'kreikka',
              'latina',
              'latvia, lätti',
              'liettua',
              'muu kieli',
              'portugali',
              'ranska',
              'saame, lappi',
              'saksa',
              'venäjä',
              'viittomakieli',
              'viro, eesti'])
          })
          after(editor.cancelChanges)
        })
      })
      describe('Oppiaineen laajuuden muutos', function() {
        before(editor.edit, editor.property('laajuus').setValue('2'), editor.saveChanges, wait.until(page.isSavedLabelShown))
        it('muutettu laajuus näytetään', function() {
          expect(editor.property('laajuus').getValue()).to.equal('2')
        })
      })
      describe('Oppiaineen arvosanan muutos', function() {
        var äidinkieli = editor.subEditor('.oppiaineet tbody:eq(0)')
        var arvosana = äidinkieli.propertyBySelector('.arvosana')
        before(opinnot.valitseSuoritus(1, '7. vuosiluokka'))

        describe('Kun annetaan numeerinen arvosana', function() {
          before(editor.edit, arvosana.selectValue('5'), editor.saveChanges)

          it('muutettu arvosana näytetään', function() {
            expect(arvosana.getValue()).to.equal('5')
          })
        })

        describe('Kun annetaan arvosana S', function() {
          before(editor.edit, arvosana.selectValue('S'), opinnot.expandAll)

          describe('Sanallinen arviointi', function() {
            var sanallinenArviointi = äidinkieli.propertyBySelector('.kuvaus')
            before(editor.edit, sanallinenArviointi.setValue('Hienoa työtä'), editor.saveChanges, opinnot.expandAll)
            it('Voidaan syöttää ja näytetään', function() {
              expect(sanallinenArviointi.isVisible()).to.equal(true)
              expect(sanallinenArviointi.getValue()).to.equal('Hienoa työtä')
            })

            describe('Kun vaihdetaan numeeriseen arvosanaan', function() {
              before(editor.edit, arvosana.selectValue(8), opinnot.expandAll)

              it('Sanallinen arviointi piilotetaan', function() {
                expect(sanallinenArviointi.isVisible()).to.equal(false)
              })
            })
          })
        })

        describe('Kun poistetaan ensimmäinen oppiaine ja annetaan toiselle arvosana (bug fix)', function() {
          before(opinnot.collapseAll, editor.edit, äidinkieli.propertyBySelector('tr').removeValue, arvosana.selectValue('9'), editor.saveChanges)
          it('Toimii', function() {
            expect(arvosana.getValue()).to.equal('9')
          })
        })
      })
      describe('Yksilöllistäminen', function() {
        before(resetFixtures, page.openPage, page.oppijaHaku.searchAndSelect('220109-784L'),  editor.edit, opinnot.expandAll, editor.property('yksilöllistettyOppimäärä').setValue(true), editor.saveChanges)
        it('toimii', function() {
          expect(extractAsText(S('.oppiaineet tbody:eq(0) tr:eq(0)'))).to.equal('Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9 *')
        })
        after(editor.edit, opinnot.expandAll, editor.property('yksilöllistettyOppimäärä').setValue(false), editor.saveChanges, wait.until(page.isSavedLabelShown))
      })
      describe('Painotus', function() {
        before(editor.edit, opinnot.expandAll, editor.property('painotettuOpetus').setValue(true), editor.saveChanges, wait.until(page.isSavedLabelShown))
        it('toimii', function() {
          expect(extractAsText(S('.oppiaineet tbody:eq(0) tr:eq(0)'))).to.equal('Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9 **')
        })
        after(editor.edit, opinnot.expandAll, editor.property('painotettuOpetus').setValue(false), editor.saveChanges)
      })
      describe('Käyttäytymisen arvioinnin lisäys', function() {
        var arvosana = editor.subEditor('.kayttaytyminen').property('arvosana')
        before(opinnot.valitseSuoritus(1, '7. vuosiluokka'), editor.edit, editor.propertyBySelector('.kayttaytyminen').addValue, editor.saveChanges)
        describe('Muuttamatta arviointia', function() {
          it('Näyttää oletusarvon S', function() {
            expect(arvosana.getValue()).to.equal('S')
          })
        })
        describe('Kun muutetaan arvosanaa', function() {
          before(editor.edit, arvosana.selectValue('10'), editor.saveChanges)
          it('Näyttää muutetun arvon', function() {
            expect(arvosana.getValue()).to.equal('10')
          })
        })
        describe('Kun lisätään sanallinen kuvaus', function() {
          var kuvaus = editor.subEditor('.kayttaytyminen').property('kuvaus')
          before(editor.edit, kuvaus.setValue('Hyvää käytöstä'), editor.saveChanges)
          it('Näyttää muutetun arvon', function() {
            expect(kuvaus.getValue()).to.equal('Hyvää käytöstä')
          })
        })
      })
      describe('Liitetiedot', function() {
        // Liitetiedot testataan isolla testisetillä, joilla varmistetaan ArrayEditorin toiminta
        var liitetiedot = editor.property('liitetiedot')
        describe('Liitetietojen lisäys', function() {
          before(opinnot.valitseSuoritus(1, '7. vuosiluokka'), editor.edit, liitetiedot.addItem, liitetiedot.property('kuvaus').setValue('TestiTesti'))
          it('Editoinnin aikana ei näytetä ylimääräistä poistonappia', function() {
            expect(liitetiedot.isRemoveValueVisible()).to.equal(false)
          })
          describe('Lisäyksen jälkeen', function() {
            before(editor.saveChanges)
            it('Näyttää uudet liitetiedot', function() {
              expect(liitetiedot.getText()).to.equal('Liitetiedot Tunniste Käyttäytyminen\nKuvaus TestiTesti')
            })
            describe('Toisen liitetiedon lisäys', function() {
              before(editor.edit, liitetiedot.addItem, liitetiedot.itemEditor(1).property('kuvaus').setValue('Testi2'), editor.saveChanges)
              it('Näyttää uudet liitetiedot', function() {
                expect(liitetiedot.getText()).to.equal('Liitetiedot Tunniste Käyttäytyminen\nKuvaus TestiTesti\nTunniste Käyttäytyminen\nKuvaus Testi2')
              })
              describe('Ensimmäisen liitetiedon poisto', function() {
                before(editor.edit, liitetiedot.removeItem(0), editor.saveChanges)
                it('Näyttää vain toisen lisätiedon', function() {
                  expect(liitetiedot.getText()).to.equal('Liitetiedot Tunniste Käyttäytyminen\nKuvaus Testi2')
                })
                describe('Lisäys ja poisto samalla kertaa', function() {
                  before(editor.edit, liitetiedot.addItem, liitetiedot.itemEditor(1).property('kuvaus').setValue('Testi3'), liitetiedot.removeItem(0))
                  it('Välitulos: poisto toimii editoitaessa', function() {
                    expect(liitetiedot.itemEditor(0).property('kuvaus').getValue()).to.equal('Testi3')
                  })
                  describe('Editoinnin jälkeen', function() {
                    before(editor.saveChanges)
                    it('Näyttää oikeellisesti vain toisen lisätiedon', function() {
                      expect(liitetiedot.getText()).to.equal('Liitetiedot Tunniste Käyttäytyminen\nKuvaus Testi3')
                    })

                    describe('Viimeisen alkion poisto', function() {
                      before(editor.edit, liitetiedot.removeItem(0))
                      it('Välitulos: poisto toimii editoitaessa', function() {
                        expect(liitetiedot.getItems().length).to.equal(0)
                      })
                      describe('poiston jälkeen', function() {
                        before(editor.saveChanges)
                        it('Näytetään tyhjät liitetiedot', function() {
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
        describe('Useamman liitetiedon poisto kerralla', function() {
          before(opinnot.valitseSuoritus(1, '7. vuosiluokka'),
            editor.edit,
            liitetiedot.addItem, liitetiedot.itemEditor(0).property('kuvaus').setValue('T1'),
            liitetiedot.addItem, liitetiedot.itemEditor(1).property('kuvaus').setValue('T2'),
            editor.saveChanges,
            editor.edit,
            liitetiedot.removeItem(0),
            liitetiedot.removeItem(0),
            editor.saveChanges
          )

          it('Kaikki liitetiedot on poistettu', function() {
            expect(liitetiedot.isVisible()).to.equal(false)
          })
        })
      })

      describe('Valinnainen oppiaine', function() {
        before(opinnot.valitseSuoritus(1, '7. vuosiluokka'))
        var uusiOppiaine = editor.propertyBySelector('.valinnaiset .uusi-oppiaine')
        describe('Valtakunnallisen oppiaineen lisääminen', function() {
          var historia = editor.subEditor('.valinnainen.HI')
          before(editor.edit, uusiOppiaine.selectValue('Historia'), historia.propertyBySelector('.arvosana').selectValue('9'), editor.saveChanges, wait.until(page.isSavedLabelShown))
          it('Toimii', function () {
            expect(extractAsText(S('.oppiaineet'))).to.contain('Valinnainen historia 9')
          })

          describe('Poistaminen', function () {
            before(editor.edit, historia.propertyBySelector('tr').removeValue, editor.saveChanges, wait.until(page.isSavedLabelShown))
            it('toimii', function () {
              expect(extractAsText(S('.oppiaineet'))).to.not.contain('Valinnainen historia 9')
            })
          })
        })

        describe('Uuden paikallisen oppiaineen lisääminen', function() {
          var uusiPaikallinen = editor.subEditor('.valinnainen.paikallinen')
          before(editor.edit, uusiOppiaine.selectValue('Lisää'),
            uusiPaikallinen.propertyBySelector('.arvosana').selectValue('7'),
            uusiPaikallinen.propertyBySelector('.koodi').setValue('TNS'),
            uusiPaikallinen.propertyBySelector('.nimi').setValue('Tanssi'),
            editor.saveChanges, wait.until(page.isSavedLabelShown))

          it('Toimii', function () {
            expect(extractAsText(S('.oppiaineet'))).to.contain('Tanssi 7')
          })

          describe('Lisäyksen jälkeen', function() {
            var tanssi = editor.subEditor('.valinnainen.TNS')
            before(editor.edit)
            it('Uusi oppiaine ei löydy listalta (koska on jo suorituksissa)', function() {
              expect(uusiOppiaine.getOptions().includes('Tanssi')).to.equal(false)
            })

            describe('Poistettaessa suoritus', function() {
              before(tanssi.propertyBySelector('tr').removeValue)
              it('Uusi oppiaine löytyy listalta', function() {
                expect(uusiOppiaine.getOptions()).to.include('Tanssi')
              })

              describe('Muutettaessa lisätyn oppiaineen kuvausta, tallennettaessa ja poistettaessa oppiaine', function() {
                before(uusiOppiaine.selectValue('Tanssi'), tanssi.propertyBySelector('.arvosana').selectValue('7'), tanssi.propertyBySelector('.nimi').setValue('Tanssi ja liike'), editor.saveChanges, editor.edit)
                before(tanssi.propertyBySelector('tr').removeValue)

                it('Muutettu oppiaine löytyy listalta', function() {
                  expect(uusiOppiaine.getOptions()[0]).to.equal('Tanssi ja liike')
                })

                after(editor.cancelChanges)
              })
            })
          })
        })
      })

      describe('Pakollinen oppiaine', function() {
        var uusiOppiaine = editor.propertyBySelector('.pakolliset .uusi-oppiaine')
        var filosofia = editor.subEditor('.pakollinen.FI')
        before(opinnot.valitseSuoritus(1, 'Peruskoulu'), editor.edit, uusiOppiaine.selectValue('Filosofia'), filosofia.propertyBySelector('.arvosana').selectValue('8'), editor.saveChanges, wait.until(page.isSavedLabelShown))
        it('Lisääminen', function () {
          expect(extractAsText(S('.oppiaineet'))).to.contain('Filosofia 8')
        })

        describe('Poistaminen', function () {
          before(editor.edit, filosofia.propertyBySelector('tr').removeValue, editor.saveChanges, wait.until(page.isSavedLabelShown))
          it('toimii', function () {
            expect(extractAsText(S('.oppiaineet'))).to.not.contain('Filosofia 8')
          })
        })
      })
    })

    describe('Navigointi pois sivulta', function() {
      describe('Kun ei ole tallentamattomia muutoksia', function() {
        before(editor.edit, page.oppijaHaku.searchAndSelect('280618-402H'))

        it('Onnistuu normaalisti', function() {})

        after(page.oppijaHaku.searchAndSelect('220109-784L'))
      })

      describe('Kun on tallentamattomia muutoksia', function() {
        before(
          editor.edit,
          editor.property('suoritustapa').setValue('Erityinen tutkinto')
        )
        describe('Kun käyttäjä valitsee Peruuta', function() {
          before(
            function() { testFrame().confirm = function(msg) { return false } },
            opinnot.backToList
          )
          it('Pysytään muokkauksessa', function() {
            expect(editor.canSave()).to.equal(true)
          })
        })

        describe('Kun käyttäjä valitsee Jatka', function() {
          before(
            function() { testFrame().confirm = function(msg) { return true } },
            opinnot.backToList
          )
          it('Navigointi toimii', function() {
            expect(page.oppijataulukko.isVisible()).to.equal(true)
          })

          after(page.oppijaHaku.selectOppija('220109-784L'))
        })
      })
    })
    describe('Virhetilanteet', function() {
      describe('Kun tallennus epäonnistuu', function() {
        before(
          editor.edit,
          editor.property('todistuksellaNäkyvätLisätiedot').setValue('blah'),
          mockHttp('/koski/api/oppija', { status: 500}),
          editor.saveChangesAndExpectError
        )
        describe('Käyttöliittymän tila', function(){
          it('Näytetään virheilmoitus', function() {
            expect(page.getErrorMessage()).to.equal('Järjestelmässä tapahtui odottamaton virhe. Yritä myöhemmin uudelleen.')
          })
          it('Tallenna-nappi on jälleen enabloitu', function() {
            expect(opinnot.onTallennettavissa()).to.equal(true)
          })
          it('Käyttöliittymä on edelleen muokkaustilassa', function() {
            expect(opinnot.isEditing()).to.equal(true)
          })
        })

        describe('Kun tallennetaan uudestaan', function() {
          before(editor.saveChanges)
          it('Tallennus onnistuu', function() {
            expect(editor.property('todistuksellaNäkyvätLisätiedot').getValue()).to.equal('blah')
          })
        })
      })

      describe('Kun tallennuksen jälkeinen lataus epäonnistuu', function( ){
        before(
          editor.edit,
          editor.property('todistuksellaNäkyvätLisätiedot').setValue('blerg'),
          mockHttp('/koski/api/editor', { status: 500}),
          editor.saveChangesAndExpectError,
          wait.until(page.isTopLevelError)
        )
        describe('Käyttöliittymän tila', function(){
          it('Näytetään koko ruudun virheilmoitus', function() {
            expect(page.getErrorMessage()).to.equal('Järjestelmässä tapahtui odottamaton virhe. Yritä myöhemmin uudelleen. Yritä uudestaan.')
          })
        })
      })

      describe('Kun tiedot ovat peräisin ulkoisesta järjestelmästä', function() {
        before(page.openPage, page.oppijaHaku.searchAndSelect('010675-9981'))
        it('Muutokset estetty', function() {
          expect(opinnot.anythingEditable()).to.equal(false)
        })
      })

      describe('Kun käyttäjällä ei ole kirjoitusoikeuksia', function() {
        before(Authentication().logout, Authentication().login('omnia-katselija'), page.openPage, page.oppijaHaku.searchAndSelect('080154-770R'))
        it('Muutokset estetty', function() {
          var suoritus = opinnot.opiskeluoikeusEditor()
          expect(suoritus.isEditable()).to.equal(false)
        })
        it('Uuden opiskeluoikeuden lisääminen estetty', function() {
          expect(opinnot.opiskeluoikeudet.lisääOpiskeluoikeusEnabled()).to.equal(false)
        })
      })
    })
  })

  describe('Opiskeluoikeuden versiot', function() {
    var versiohistoria = opinnot.versiohistoria

    before(Authentication().login(), resetFixtures, page.openPage, page.oppijaHaku.searchAndSelect('220109-784L'), versiohistoria.avaa)

    it('Alussa on vain yksi versio', function() {
      expect(versiohistoria.listaa()).to.deep.equal(['v1'])
    })

    describe('Muutoksen jälkeen', function() {
      var liitetiedot = editor.property('liitetiedot')
      before(
        versiohistoria.sulje,
        opinnot.valitseSuoritus(1, '7. vuosiluokka'),
        editor.edit,
        liitetiedot.addItem, liitetiedot.itemEditor(0).property('kuvaus').setValue('T2'),
        editor.saveChanges,
        versiohistoria.avaa
      )

      it('On kaksi versiota', function() {
        expect(versiohistoria.listaa()).to.deep.equal(['v1', 'v2'])
        expect(liitetiedot.getItems().length).to.equal(1)
      })

      describe('Valittaessa vanha versio', function() {
        before(versiohistoria.valitse('v1'))
        it('Näytetään vanha versio', function() {
          expect(liitetiedot.isVisible()).to.equal(false)
        })

        it('Näytetään oppiaineiden arvosanat', function() {
          expect(extractAsText(S('.oppiaineet'))).to.equal(
            'Oppiaineiden arvosanat\n' +
            'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
            'Pakolliset oppiaineet\n' +
            'Oppiaine Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\n' +
            'B1-kieli, ruotsi 8\n' +
            'A1-kieli, englanti 8\n' +
            'Uskonto tai elämänkatsomustieto 10\n' +
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
            'Valinnaiset oppiaineet\n' +
            'Oppiaine Arvosana Laajuus\n' +
            'Valinnainen b1-kieli, ruotsi S 1 vuosiviikkotuntia\n' +
            'Valinnainen kotitalous S 1 vuosiviikkotuntia\n' +
            'Valinnainen liikunta S 0.5 vuosiviikkotuntia\n' +
            'Valinnainen b2-kieli, saksa 9 4 vuosiviikkotuntia\n' +
            'Tietokoneen hyötykäyttö 9\n' +
            '* = yksilöllistetty oppimäärä, ** = painotettu opetus'
          )
        })
      })
    })
  })

  describe('Useita opiskeluoikeuksia', function() {
    before(Authentication().login(), resetFixtures, page.openPage, page.oppijaHaku.searchAndSelect('180497-112F'))
    describe('Alussa', function() {
      it('Uusimpien suoritusten välilehti valittu', function() {
        expect(opinnot.suoritusOnValittu(1, 'Peruskoulu')).to.equal(true)
        expect(opinnot.suoritusOnValittu(2, 'Peruskoulu')).to.equal(true)
      })
    })
    describe('Kun valitaan, ensimmäisestä opiskeluoikeudesta', function() {
      before(opinnot.valitseSuoritus(1, '8. vuosiluokka'))
      it('Toimii', function() {
        expect(opinnot.suoritusOnValittu(1, 'Peruskoulu')).to.equal(false)
        expect(opinnot.suoritusOnValittu(1, '8. vuosiluokka')).to.equal(true)
        expect(opinnot.suoritusOnValittu(2, 'Peruskoulu')).to.equal(true)
      })

      describe('Kun valitaan, molemmista opiskeluoikeuksista', function() {
        before(opinnot.valitseSuoritus(2, '6. vuosiluokka'), opinnot.valitseSuoritus(1, '9. vuosiluokka'))
        it('Toimii', function() {
          expect(opinnot.suoritusOnValittu(1, '9. vuosiluokka')).to.equal(true)
          expect(opinnot.suoritusOnValittu(1, '8. vuosiluokka')).to.equal(false)
          expect(opinnot.suoritusOnValittu(2, '7. vuosiluokka')).to.equal(false)
          expect(opinnot.suoritusOnValittu(2, '6. vuosiluokka')).to.equal(true)
        })
      })
    })
  })

  describe('Opiskeluoikeuden lisääminen', function() {
    describe('Perusopetuksen oppimäärä', function() {
      before(prepareForNewOppija('kalle', '230872-7258'))

      describe('Aluksi', function() {
        it('Lisää-nappi on disabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(false)
        })
      })

      describe('Kun syötetään validit tiedot', function() {
        before(addOppija.enterValidDataPerusopetus())

        describe('Käyttöliittymän tila', function() {
          it('Lisää-nappi on enabloitu', function() {
            expect(addOppija.isEnabled()).to.equal(true)
          })
        })

        describe('Kun painetaan Lisää-nappia', function() {
          before(addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Peruskoulu'))

          it('lisätty oppija näytetään', function() {})

          describe('Käyttöliittymän tila', function() {
            it('Lisätty opiskeluoikeus näytetään', function() {
              expect(opinnot.getTutkinto()).to.equal('Peruskoulu')
              expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
              expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('104/011/2014')
            })
          })


          describe('Toisen opiskeluoikeuden lisääminen (ammatillinen tutkinto)', function() {
            before(
              opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
              addOppija.selectOppilaitos('Omnian ammattiopisto'),
              addOppija.selectTutkinto('Autoalan perustutkinto'),
              addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Autoalan perustutkinto')
            )
            it('Onnistuu ja uusi ammatillinen opiskeluoikeus tulee valituksi', function( ){
            })

            describe('Kolmannen opiskeluoikeuden lisääminen (oppiaineen oppimäärä)', function() {
              before(
                opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
                addOppija.selectOppilaitos('Kulosaaren ala-aste'),
                addOppija.selectOppimäärä('Perusopetuksen oppiaineen oppimäärä'),
                addOppija.selectOppiaine('Fysiikka'),
                addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Fysiikka')
              )
              it('toimii', function( ){

              })
            })
          })
        })
      })

      describe('Kun oppilaitos mahdollisesti tarjoaa myös ammatillista koulutusta', function() {
        before(
          resetFixtures,
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataPerusopetus({oppilaitos:'Helsingin medialukio'}))

        it('Opiskeluoikeuden tyypin valinnassa näytetään myös ammatillinen koulutus', function() {
          expect(addOppija.opiskeluoikeudenTyypit()).to.deep.equal(['Ammatillinen koulutus', 'Perusopetus'])
        })

        describe('Opiskeluoikeuden lisäys', function() {
          before(
            addOppija.selectOpiskeluoikeudenTyyppi('Perusopetus'),
            addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Peruskoulu')
          )

          it('lisätty oppija näytetään', function() {})
        })
      })
    })

    describe('Aikuisten perusopetus', function() {
      before(prepareForNewOppija('kalle', '230872-7258'))
      before(addOppija.enterValidDataPerusopetus(), addOppija.selectOpetussuunnitelma('Aikuisten perusopetus'))
      before(addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Peruskoulu'))
      it('Näytetään oikein', function() {
        expect(editor.property('oppimäärä').getValue()).to.equal('Aikuisten perusopetus')
        expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('19/011/2015')
      })
    })
    describe('Perusopetuksen oppiaineen oppimäärä', function() {
      before(prepareForNewOppija('kalle', '230872-7258'), addOppija.enterValidDataPerusopetus())

      describe('Käyttöliittymän tila', function() {
        it('Näytetään oppimäärävaihtoehdot (Perusopetuksen oppiaineen oppimäärä, Perusopetuksen oppimäärä)', function() {
          expect(addOppija.oppimäärät()).to.deep.equal(['Perusopetuksen oppiaineen oppimäärä', 'Perusopetuksen oppimäärä'])
        })
      })

      describe('Kun valitaan oppiaineen oppimäärä ja oppiaine', function() {
        before(
          addOppija.selectOppimäärä('Perusopetuksen oppiaineen oppimäärä'),
          addOppija.selectOppiaine('Fysiikka'),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Fysiikka'))

        it('Luodaan opiskeluoikeus, jolla on oppiaineen oppimäärän suoritus', function() {
          expect(editor.propertyBySelector('.perusteenDiaarinumero').getValue()).to.equal('19/011/2015')
        })

        it('Näytetään oppiaineen nimi opiskeluoikeuden otsikossa', function() {
          expect(S('.opiskeluoikeus h3 .koulutus').text()).to.equal('Fysiikka')
        })

        describe('Toisen oppiaineen lisääminen', function() {
          var lisääSuoritus = opinnot.lisääSuoritusDialog()
          before(editor.edit, opinnot.lisääSuoritus, wait.forAjax,
            lisääSuoritus.property('tunniste').setValue('Matematiikka'),
            lisääSuoritus.toimipiste.select('Jyväskylän normaalikoulu, alakoulu'),
            lisääSuoritus.lisääSuoritus
          )

          it('Näytetään uusi suoritus', function() {
            expect(opinnot.suoritusTabs(1)).to.deep.equal(['Fysiikka', 'Matematiikka'])
          })

          it('Näytetään oppiaineiden määrä opiskeluoikeuden otsikossa', function() {
            expect(S('.opiskeluoikeus h3 .koulutus').text()).to.equal('2 oppiainetta')
          })
        })
      })
    })

    describe('Back-nappi', function() {
      describe('Kun täytetään tiedot ja palataan hakuun', function() {
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataPerusopetus(),
          addOppija.goBack,
          wait.until(page.oppijataulukko.isVisible)
        )
        describe('Käyttöliittymän tila', function() {
          it('Syötetty henkilötunnus näytetään', function() {
            expect(page.oppijaHaku.getSearchString()).to.equal('230872-7258')
          })
          it('Uuden oppijan lisäys on mahdollista', function() {
            expect(page.oppijaHaku.canAddNewOppija()).to.equal(true)
          })
        })
        describe('Kun täytetään uudestaan', function() {
          before(
            page.oppijaHaku.addNewOppija,
            addOppija.enterValidDataPerusopetus()
          )
          it('Lisää-nappi on enabloitu', function() {
            expect(addOppija.isEnabled()).to.equal(true)
          })
        })
      })
    })

  })

  describe('Vuosiluokan suorituksen lisääminen', function() {
    var lisääSuoritus = opinnot.lisääSuoritusDialog()

    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataPerusopetus(),
      addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Peruskoulu'),
      editor.edit
    )
    describe('Kun opiskeluoikeus on tilassa VALMIS', function() {
      before(opinnot.avaaLisaysDialogi, opiskeluoikeus.tila().click('input[value="valmistunut"]'), opiskeluoikeus.tallenna)

      it('Päätason suoritusta ei voi lisätä', function() {
        expect(opinnot.lisääSuoritusVisible()).to.equal(false)
      })

      after(editor.property('tila').removeItem(0))
    })
    describe('Kun opiskeluoikeus on tilassa LÄSNÄ', function() {
      describe('Ennen lisäystä', function() {
        it('Päätason suorituksen voi lisätä', function() {
          expect(opinnot.lisääSuoritusVisible()).to.equal(true)
        })
        it('Näytetään muut päätason suoritukset', function() {
          expect(opinnot.suoritusTabs(1)).to.deep.equal(['Peruskoulu'])
        })
      })
      describe('Lisättäessä ensimmäinen', function() {
        before(opinnot.lisääSuoritus)
        describe('Aluksi', function() {
          it('Lisää-nappi on disabloitu', function() {
            expect(lisääSuoritus.isEnabled()).to.equal(false)
          })
          it('Valitsee automaattisesti pienimmän puuttuvan luokka-asteen', function( ){
            expect(lisääSuoritus.property('tunniste').getValue()).to.equal('1. vuosiluokka')
          })
        })
        describe('Kun syötetään luokkatieto ja valitaan toimipiste', function() {
          before(lisääSuoritus.property('luokka').setValue('1a'), lisääSuoritus.toimipiste.select('Jyväskylän normaalikoulu, alakoulu'))
          it('Lisää-nappi on enabloitu', function() {
            expect(lisääSuoritus.isEnabled()).to.equal(true)
          })

          describe('Kun painetaan Lisää-nappia', function() {
            before(lisääSuoritus.lisääSuoritus)
            describe('Käyttöliittymän tila', function() {
              it('Näytetään uusi suoritus', function() {
                expect(opinnot.suoritusTabs(1)).to.deep.equal(['Peruskoulu', '1. vuosiluokka'])
              })
              it('Uusi suoritus on valittuna', function() {
                expect(opinnot.getTutkinto()).to.equal('1. vuosiluokka')
              })
              it('Toimipiste on oikein', function() {
                expect(editor.property('toimipiste').getValue()).to.equal('Jyväskylän normaalikoulu, alakoulu')
              })
              it('Esitäyttää pakolliset oppiaineet', function() {
                expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal(['Äidinkieli ja kirjallisuus,', 'Matematiikka', 'Kuvataide', 'Musiikki', 'Liikunta'])
                expect(S('.oppiaineet .oppiaine .kieli input').val()).to.equal('Suomen kieli ja kirjallisuus')
              })
              describe('Tutkinnon peruste', function() {
                before(editor.saveChanges)
                it('Esitäyttää perusteen diaarinumeron', function() {
                  expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('104/011/2014')
                })
              })
            })
            describe('Annettaessa oppiaineelle arvosana', function() {
              var äidinkieli = editor.subEditor('.oppiaineet tbody:eq(0)')
              var arvosana = äidinkieli.propertyBySelector('.arvosana')
              before(editor.edit, arvosana.selectValue('5'), editor.saveChanges)
              it('muutettu arvosana näytetään', function() {
                expect(arvosana.getValue()).to.equal('5')
              })
              it('suoritus siirtyy VALMIS-tilaan', function() {
                expect(äidinkieli.elem().hasClass('valmis')).to.equal(true)
              })

              describe('Siirrettäessä suoritus KESKEN-tilaan', function() {
                before(editor.edit, opinnot.expandAll, äidinkieli.property('tila').setValue('Suoritus kesken'), editor.saveChanges, wait.until(page.isSavedLabelShown))
                it('Arvosana poistetaan', function() {
                  expect(arvosana.getValue()).to.equal('')
                })

                describe('Siirrettäessä VALMIS-tilaan ilman arvosanaa', function() {
                  before(editor.edit, opinnot.expandAll, äidinkieli.property('tila').setValue('Suoritus valmis'))
                  describe('Käyttöliittymän tila', function( ){
                    it('Tallennus on estetty', function() {
                      expect(editor.canSave()).to.equal(false)
                    })
                  })
                  describe('Lisättäessä arvosana', function() {
                    before(arvosana.selectValue('5'))
                    it('Tallennus on sallittu', function() {
                      expect(editor.canSave()).to.equal(true)
                    })
                  })
                })
              })
            })

            describe('Merkitseminen valmiiksi', function() {
              function merkitseOppiaineetValmiiksi() {
                var count = 5
                for (var i = 0; i < count; i++) {
                  var oppiaine = editor.subEditor('.oppiaineet tbody.oppiaine:eq('+i+')')
                  var arvosana = oppiaine.propertyBySelector('.arvosana')
                  before(
                    arvosana.selectValue('5')
                  )
                }
              }
              var dialog = tilaJaVahvistus.merkitseValmiiksiDialog
              describe('Aluksi', function() {
                it('Tila on "kesken"', function() {
                  expect(tilaJaVahvistus.text()).to.equal('Suoritus: KESKEN')
                })
              })
              describe('Kun on keskeneräisiä oppiaineita', function() {
                it('Merkitse valmiiksi -nappi on disabloitu', function() {
                  expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(false)
                })
              })
              describe('Kun kaikki oppiaineet on merkitty valmiiksi', function() {
                merkitseOppiaineetValmiiksi()
                before(editor.edit)
                describe('Aluksi', function() {
                  it('Merkitse valmiiksi -nappi näytetään', function() {
                    expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(true)
                  })
                })
                describe('Kun merkitään valmiksi', function() {
                  before(
                    tilaJaVahvistus.merkitseValmiiksi,
                    dialog.editor.property('päivä').setValue('11.4.2017'),
                    dialog.myöntäjät.itemEditor(0).setValue('Lisää henkilö'),
                    dialog.myöntäjät.itemEditor(0).propertyBySelector('.nimi').setValue('Reijo Reksi'),
                    dialog.myöntäjät.itemEditor(0).propertyBySelector('.titteli').setValue('rehtori')
                  )

                  describe('Merkitse valmiiksi -dialogi', function (){
                    it('Esitäyttää paikkakunnan valitun organisaation mukaan', function() {
                      expect(dialog.editor.property('paikkakunta').getValue()).to.equal('Jyväskylä')
                    })
                  })

                  describe('Kun painetaan Merkitse valmiiksi -nappia', function() {
                    before(
                      dialog.editor.property('paikkakunta').setValue('Jyväskylä mlk'),
                      dialog.merkitseValmiiksi
                    )


                    describe('Käyttöliittymän tila', function() {
                      it('Tila on "valmis" ja vahvistus näytetään', function() {
                        expect(tilaJaVahvistus.text()).to.equal('Suoritus: VALMIS Vahvistus : 11.4.2017 Jyväskylä mlk Reijo Reksi , rehtori\nSiirretään seuraavalle luokalle')
                      })

                      it('Merkitse valmiiksi -nappia ei näytetä', function() {
                        expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(false)
                      })
                    })

                    describe('Kun muutetaan takaisin keskeneräiseksi', function() {
                      before(tilaJaVahvistus.merkitseKeskeneräiseksi)
                      it('Tila on "kesken" ja vahvistus on poistettu', function() {
                        expect(tilaJaVahvistus.text()).to.equal('Suoritus: KESKEN')
                      })

                      describe('Kun merkitään keskeytyneeksi', function() {
                        var äidinkieli = editor.subEditor('.oppiaineet tbody:eq(0)')

                        before(opinnot.expandAll, äidinkieli.property('tila').setValue('Suoritus kesken'), tilaJaVahvistus.merkitseKeskeytyneeksi)


                        it('Tila on "keskeytynyt"', function() {
                          expect(tilaJaVahvistus.text()).to.equal('Suoritus: KESKEYTYNYT')
                        })

                        it('Keskeneräiset oppiainesuoritukset on merkitty keskeytyneiksi', function() {
                          expect(äidinkieli.property('tila').getValue()).to.equal('Suoritus keskeytynyt')
                        })
                      })
                    })

                    describe('Lisättäessä toinen', function() {
                      before(editor.edit, opinnot.lisääSuoritus)
                      describe('Aluksi', function() {
                        it('Lisää-nappi on disabloitu', function() {
                          expect(lisääSuoritus.isEnabled()).to.equal(false)
                        })
                        it('Valitsee automaattisesti pienimmän puuttuvan luokka-asteen', function( ){
                          expect(lisääSuoritus.property('tunniste').getValue()).to.equal('2. vuosiluokka')
                        })
                        it('Käytetään oletusarvona edellisen luokan toimipistettä', function() {
                          expect(editor.property('toimipiste').getValue()).to.equal('Jyväskylän normaalikoulu, alakoulu')
                        })
                      })
                      describe('Lisäyksen jälkeen', function() {
                        before(lisääSuoritus.property('luokka').setValue('2a'), lisääSuoritus.lisääSuoritus)

                        it('Uusin suoritus näytetään täbeissä viimeisenä', function() {
                          expect(opinnot.suoritusTabs(1)).to.deep.equal(['Peruskoulu', '1. vuosiluokka', '2. vuosiluokka'])
                        })

                        it('Uusi suoritus on valittuna', function() {
                          expect(opinnot.getTutkinto()).to.equal('2. vuosiluokka')
                          expect(editor.property('luokka').getValue()).to.equal('2a')
                        })

                        describe('Kun merkitään valmiiksi, jää luokalle', function() {
                          var tilaJaVahvistus = opinnot.tilaJaVahvistus
                          var dialog = tilaJaVahvistus.merkitseValmiiksiDialog
                          var dialogEditor = dialog.editor
                          var myöntäjät = dialogEditor.property('myöntäjäHenkilöt')
                          merkitseOppiaineetValmiiksi()
                          before(
                            tilaJaVahvistus.merkitseValmiiksi,
                            dialogEditor.propertyBySelector('.jaa-tai-siirretaan').setValue(false),
                            myöntäjät.itemEditor(0).setValue('Reijo Reksi'),
                            dialogEditor.property('päivä').setValue('11.4.2017'),
                            dialogEditor.property('paikkakunta').setValue('Jyväskylä mlk'),
                            dialog.merkitseValmiiksi
                          )

                          it('Edellisen suorituksen vahvistaja löytyy listalta', function() {
                            // implicitly tested
                          })

                          it('Tila on "valmis" ja vahvistus näytetään', function() {
                            expect(tilaJaVahvistus.text()).to.equal('Suoritus: VALMIS Vahvistus : 11.4.2017 Jyväskylä mlk Reijo Reksi , rehtori\nEi siirretä seuraavalle luokalle')
                          })

                          describe('Seuraavan luokka-asteen lisäyksessä', function() {
                            before(opinnot.lisääSuoritus)
                            it('On mahdollista lisätä sama luokka-aste uudelleen', function() {
                              expect(lisääSuoritus.property('tunniste').getValue()).to.equal('2. vuosiluokka')
                            })

                            describe('Lisättäessä toinen 2. luokan suoritus', function() {
                              before(lisääSuoritus.property('luokka').setValue('2x'), lisääSuoritus.lisääSuoritus)
                              it('Uusi suoritus tulee valituksi', function() {
                                expect(editor.property('luokka').getValue()).to.equal('2x')
                              })

                              describe('Tallennettaessa', function() {
                                before(editor.saveChanges)
                                it('Uusi suoritus on edelleen valittu', function() {
                                  expect(editor.property('luokka').getValue()).to.equal('2x')
                                })

                                it('Uusi suoritus on täbeissä ennen vanhempaa 2.luokan suoritusta', function() {
                                  expect(opinnot.suoritusTabIndex(1)).to.equal(1)
                                })

                                describe('Kun kaikki luokka-asteet on lisätty', function() {
                                  before(editor.edit)
                                  for (var i = 3; i <= 9; i++) {
                                    before(opinnot.lisääSuoritus, lisääSuoritus.property('luokka').setValue(i + 'a'), lisääSuoritus.lisääSuoritus)
                                  }

                                  it('Suorituksia ei voi enää lisätä', function() {
                                    expect(opinnot.lisääSuoritusVisible()).to.equal(false)
                                  })

                                  describe('Uudempi 2.luokan suoritus', function() {
                                    before(editor.saveChanges, opinnot.valitseSuoritus(1, '2. vuosiluokka'))
                                    it('On edelleen täbeissä ennen vanhempaa 2.luokan suoritusta', function() {
                                      expect(editor.property('luokka').getValue()).to.equal('2x')
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

  describe('Perusopetuksen oppiaineen oppimäärän suoritus', function() {
    before(Authentication().login(), page.openPage, page.oppijaHaku.searchAndSelect('110738-839L'))
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('näyttää opiskeluoikeuden tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Alkamispäivä : 15.8.2008 — Päättymispäivä : 4.6.2016\n' +
          'Tila 4.6.2016 Valmistunut\n' +
          '15.8.2008 Läsnä')
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Oppiaine Äidinkieli ja kirjallisuus\n' +
          'Kieli Suomen kieli ja kirjallisuus\n' +
          'Yksilöllistetty oppimäärä ei\n' +
          'Painotettu opetus ei\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Arvosana 9\n' +
          'Suoritustapa Erityinen tutkinto\n' +
          'Suoritus: VALMIS Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori')
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        expect(todistus.headings()).to.equal('Jyväskylän yliopisto Todistus perusopetuksen oppiaineen oppimäärän suorittamisesta Jyväskylän normaalikoulu Oppiaineenkorottaja, Olli 110738-839L')
        expect(todistus.arvosanarivi('.oppiaine.AI')).to.equal('Äidinkieli ja kirjallisuus Kiitettävä 9')
        expect(todistus.arvosanarivi('.muut-opinnot')).to.equal('')
      })
    })

    describe('Tietojen muuttaminen', function() {
      var arvosana = editor.property('arviointi')

      before(page.openPage, page.oppijaHaku.searchAndSelect('110738-839L'))
      before(editor.edit, editor.property('tila').removeItem(0)) // opiskeluoikeus: läsnä
      before(arvosana.removeItem(0)) // poistetaan arviointi

      describe('Kun suoritus on valmis, mutta arvosana puuttuu', function() {
        it('Tallennus on estetty', function() {
          expect(opinnot.onTallennettavissa()).to.equal(false)
        })
      })
      describe('Kun merkitään keskeneräiseksi', function() {
        before(tilaJaVahvistus.merkitseKeskeneräiseksi, editor.saveChanges, editor.edit)
        it('Tallennus onnistuu', function() {
        })

        it('Valmiiksi merkintä on estetty', function() {
          expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(false)
        })

        describe('Kun lisätään arvosana', function() {
          before(arvosana.addItem,
            tilaJaVahvistus.merkitseValmiiksi,
            tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).setValue('Lisää henkilö'),
            tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).propertyBySelector('.nimi').setValue('Reijo Reksi'),
            tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).propertyBySelector('.titteli').setValue('rehtori'),
            tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,  editor.saveChanges)
          it('Valmiiksi merkintä on mahdollista', function() {

          })
        })
      })
    })
  })

  describe('Perusopetuksen lisäopetus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('131025-6573'))
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)

      it('näyttää opiskeluoikeuden tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Alkamispäivä : 15.8.2008 — Päättymispäivä : 4.6.2016\n' +
          'Tila 4.6.2016 Valmistunut\n' +
          '15.8.2008 Läsnä\n' +
          'Lisätiedot\n' +
          'Perusopetuksen aloittamista lykätty ei\n' +
          'Aloittanut ennen oppivelvollisuutta ei\n' +
          'Pidennetty oppivelvollisuus 15.8.2008 — 4.6.2016\nVuosiluokkiin sitoutumaton opetus ei')
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Perusopetuksen lisäopetus\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Suoritus: VALMIS Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori')
      })
      it('näyttää oppiaineiden arvosanat', function() {
        expect(extractAsText(S('.oppiaineet'))).to.equal('Oppiaineiden arvosanat\n' +
          'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
          'Pakolliset oppiaineet\n' +
          'Oppiaine Arvosana\n' +
          'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 7 †\n' +
          'A1-kieli, englanti 10 †\n' +
          'B1-kieli, ruotsi 6 †\n' +
          'Matematiikka 6 †\n' +
          'Biologia 10 †\n' +
          'Maantieto 9 †\n' +
          'Fysiikka 8 †\n' +
          'Kemia 9 †\n' +
          'Terveystieto 8 †\n' +
          'Historia 7\n' +
          'Yhteiskuntaoppi 8 †\n' +
          'Kuvataide 8\n' +
          'Liikunta 7 * †\n' +
          'Valinnaiset oppiaineet\n' +
          'Oppiaine Arvosana\n' +
          'Monialainen oppimiskokonaisuus S\n' +
          'Kuvaus Tehtiin ryhmätyönä webbisivusto, jossa kerrotaan tupakoinnin haitoista\n' +
          '† = perusopetuksen päättötodistuksen arvosanan korotus, * = yksilöllistetty oppimäärä')
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        expect(todistus.headings()).to.equal('Jyväskylän yliopisto Todistus lisäopetuksen suorittamisesta Jyväskylän normaalikoulu Kymppiluokkalainen, Kaisa 131025-6573')
        // See more detailed content specification in PerusopetusSpec.scala
      })
    })
    describe('Tietojen muuttaminen', function() {
      before(page.openPage, page.oppijaHaku.searchAndSelect('131025-6573'))
      describe('Oppiaineen arvosanan muutos', function() {
        var äidinkieli = editor.subEditor('.oppiaineet tbody:eq(0)')
        var arvosana = äidinkieli.propertyBySelector('.arvosana')
        describe('Kun annetaan numeerinen arvosana', function() {
          before(editor.edit, arvosana.selectValue('5'), editor.saveChanges)

          it('muutettu arvosana näytetään', function() {
            expect(arvosana.getValue()).to.equal('5')
          })
        })
      })


      describe('Kurssin kuvauksen ja sanallisen arvion muuttaminen', function() {
        var kurssi = editor.subEditor('.oppiaineet tbody.xxx')
        var sanallinenArviointi = kurssi.propertyBySelector('.kuvaus:nth-child(1)')
        var kurssinKuvaus = kurssi.propertyBySelector('.kuvaus:nth-child(2)') // Yes, they both have the same class "kuvaus", which is exactly why testing this is important

        before(editor.edit, opinnot.expandAll, sanallinenArviointi.setValue('Uusi arviointi'), kurssinKuvaus.setValue('Uusi kuvaus'), editor.saveChanges, opinnot.expandAll)
        it('Toimii', function() {
          expect(sanallinenArviointi.getValue()).to.equal('Uusi arviointi')
          expect(kurssinKuvaus.getValue()).to.equal('Uusi kuvaus')
        })
      })


      describe('Pakollinen oppiaine', function() {
        var uusiOppiaine = editor.propertyBySelector('.pakolliset .uusi-oppiaine')
        var filosofia = editor.subEditor('.pakollinen.FI')
        before(editor.edit, uusiOppiaine.selectValue('Filosofia'), filosofia.propertyBySelector('.arvosana').selectValue('8'), editor.saveChanges, wait.until(page.isSavedLabelShown))
        it('Lisääminen', function () {
          expect(extractAsText(S('.oppiaineet'))).to.contain('Filosofia 8')
        })

        describe('Poistaminen', function () {
          before(editor.edit, filosofia.propertyBySelector('tr').removeValue, editor.saveChanges, wait.until(page.isSavedLabelShown))
          it('toimii', function () {
            expect(extractAsText(S('.oppiaineet'))).to.not.contain('Filosofia 8')
          })
        })
      })
    })
    describe('Opiskeluoikeuden lisääminen', function() {
      before(prepareForNewOppija('kalle', '230872-7258'))
      before(addOppija.enterValidDataPerusopetus(), addOppija.selectOpiskeluoikeudenTyyppi('Perusopetuksen lisäopetus'))
      before(addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Perusopetuksen lisäopetus'))
      it('Lisätty opiskeluoikeus näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal('Perusopetuksen lisäopetus')
        expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
        expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('105/011/2014')
      })
    })
  })

  describe('Perusopetukseen valmistava opetus', function() {
    before(Authentication().login(), page.openPage, page.oppijaHaku.searchAndSelect('220109-784L'), opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetukseenvalmistavaopetus'))
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('näyttää opiskeluoikeuden tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Alkamispäivä : 15.8.2007 — Päättymispäivä : 1.6.2008\n' +
          'Tila 1.6.2008 Valmistunut\n' +
          '15.8.2007 Läsnä')
      })
      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Perusopetukseen valmistava opetus\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Suoritus: VALMIS Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori')
      })
      it('näyttää oppiaineiden arvosanat', function() {
        expect(extractAsText(S('.oppiaineet'))).to.equal(
          'Oppiaineiden arvosanat\n' +
          'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
          'Oppiaine Arvosana Laajuus\n' +
          'Äidinkieli S 10 vuosiviikkotuntia\n' +
          'Sanallinen arviointi Keskustelee sujuvasti suomeksi\n' +
          'Opetuksen sisältö Suullinen ilmaisu ja kuullun ymmärtäminen')
      })
    })
    describe('Tietojen muuttaminen', function() {
      describe('Oppiaineen arvosanan muutos', function() {
        var äidinkieli = editor.subEditor('.oppiaineet tbody.ai tr:eq(0)')
        var arvosana = äidinkieli.propertyBySelector('.arvosana')
        before(editor.edit, arvosana.selectValue('H'), editor.saveChanges)
        it('muutettu arvosana näytetään', function() {
          expect(arvosana.getValue()).to.equal('H')
        })
        after(editor.edit, arvosana.selectValue('S'), editor.saveChanges, wait.until(page.isSavedLabelShown))
      })
      describe('Oppiaine', function() {
        var uusiOppiaine = editor.propertyBySelector('.uusi-oppiaine')
        describe('Uuden oppiaineen lisääminen', function() {
          var uusiPaikallinen = editor.subEditor('.valinnainen.paikallinen:nth-child(3)')
          before(editor.edit, uusiOppiaine.selectValue('Lisää'),
            uusiPaikallinen.propertyBySelector('.koodi').setValue('TNS'),
            uusiPaikallinen.propertyBySelector('.nimi').setValue('Tanssi'),
            uusiPaikallinen.propertyBySelector('.arvosana').selectValue('S'),
            uusiPaikallinen.propertyBySelector('.property.laajuus .value').setValue('1'),
            uusiPaikallinen.propertyBySelector('.property.laajuus .yksikko').setValue('vuosiviikkotuntia'),
            editor.saveChanges, wait.until(page.isSavedLabelShown))

          it('Toimii', function () {
            expect(extractAsText(S('.oppiaineet'))).to.contain('Tanssi S')
          })

          describe('Lisäyksen jälkeen', function() {
            var tanssi = editor.subEditor('.valinnainen.TNS')
            before(editor.edit)
            it('Uusi oppiaine ei löydy listalta (koska on jo suorituksissa)', function() {
              expect(uusiOppiaine.getOptions().includes('Tanssi')).to.equal(false)
            })

            describe('Poistettaessa suoritus', function() {
              before(tanssi.propertyBySelector('tr').removeValue)
              it('Uusi oppiaine löytyy listalta', function() {
                expect(uusiOppiaine.getOptions()).to.include('Tanssi')
              })

              describe('Muutettaessa lisätyn oppiaineen kuvausta, tallennettaessa ja poistettaessa oppiaine', function() {
                before(uusiOppiaine.selectValue('Tanssi'), tanssi.propertyBySelector('.arvosana').selectValue('H'), tanssi.propertyBySelector('.nimi').setValue('Tanssi ja liike'), editor.saveChanges, editor.edit)
                before(tanssi.propertyBySelector('tr').removeValue)

                it('Muutettu oppiaine löytyy listalta', function() {
                  expect(uusiOppiaine.getOptions()[0]).to.equal('Tanssi ja liike')
                })

                after(editor.cancelChanges)
              })
            })
          })
        })
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
        // See more detailed content specification in PerusopetusSpec.scala
      })
    })
    describe('Opiskeluoikeuden lisääminen', function() {
      before(prepareForNewOppija('kalle', '230872-7258'))
      before(addOppija.enterValidDataPerusopetus(), addOppija.selectOpiskeluoikeudenTyyppi('Perusopetukseen valmistava opetus'))
      before(addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Perusopetukseen valmistava opetus'))
      it('Lisätty opiskeluoikeus näytetään', function() {
        expect(opinnot.getTutkinto()).to.equal('Perusopetukseen valmistava opetus')
        expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
        expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('57/011/2015')
      })
    })
  })
})