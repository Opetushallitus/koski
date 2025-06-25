describe('Perusopetus 4', function () {
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

  describe('Perusopetuksen lisäopetus', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('131025-6573'))
    describe('Kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)

      it('näyttää opiskeluoikeuden tiedot', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 15.8.2008 — 4.6.2016\n' +
          'Tila 4.6.2016 Valmistunut\n' +
          '15.8.2008 Läsnä\n' +
          'Lisätiedot\n' +
          'Pidennetty oppivelvollisuus 15.8.2008 — 4.6.2016\n' +
          'Erityisen tuen jaksot 15.8.2008 — 4.6.2016\n' +
          'Opiskelee toiminta-alueittain ei\n' +
          'Opiskelee erityisryhmässä ei\n' +
          'Oppilas on muiden kuin vaikeimmin kehitysvammaisten opetuksessa 15.8.2008 — 4.6.2016'
        )
      })

      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Perusopetuksen lisäopetus 020075 105/011/2014\n' +
          'Luokka 10A\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Suorituskieli suomi\n' +
          'Suoritus valmis Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori'
        )
      })
      it('näyttää oppiaineiden arvosanat', function () {
        expect(extractAsText(S('.oppiaineet'))).to.equal(
          'Arviointiasteikko\n' +
          'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
          'Yhteiset oppiaineet\n' +
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
          'Valinnaiset aineet\n' +
          'Oppiaine Arvosana\n' +
          'Monialainen oppimiskokonaisuus S\n' +
          'Kuvaus Tehtiin ryhmätyönä webbisivusto, jossa kerrotaan tupakoinnin haitoista\n' +
          '* = yksilöllistetty tai rajattu oppimäärä, † = perusopetuksen päättötodistuksen arvosanan korotus'
        )
      })
    })
    describe('Tietojen muuttaminen', function () {
      before(page.openPage, page.oppijaHaku.searchAndSelect('131025-6573'))
      describe('Oppiaineen arvosanan muutos', function () {
        var äidinkieli = opinnot.oppiaineet.oppiaine(0)
        var arvosana = äidinkieli.propertyBySelector('.arvosana')
        describe('Kun annetaan numeerinen arvosana', function () {
          before(editor.edit, arvosana.selectValue('5'), editor.saveChanges)

          it('muutettu arvosana näytetään', function () {
            expect(arvosana.getValue()).to.equal('5')
          })
        })
      })

      describe('Kurssin kuvauksen ja sanallisen arvion muuttaminen', function () {
        var kurssi = opinnot.oppiaineet.oppiaine('xxx')
        var sanallinenArviointi = kurssi.propertyBySelector('.kuvaus:eq(0)')
        var kurssinKuvaus = kurssi.propertyBySelector('.kuvaus:eq(1)') // Yes, they both have the same class "kuvaus", which is exactly why testing this is important

        before(
          editor.edit,
          opinnot.expandAll,
          sanallinenArviointi.setValue('Uusi arviointi'),
          kurssinKuvaus.setValue('Uusi kuvaus'),
          editor.saveChanges,
          opinnot.expandAll
        )
        it('Toimii', function () {
          expect(sanallinenArviointi.getValue()).to.equal('Uusi arviointi')
          expect(kurssinKuvaus.getValue()).to.equal('Uusi kuvaus')
        })
      })

      describe('Pakollinen oppiaine', function () {
        var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine('.pakolliset')
        var filosofia = editor.subEditor('.pakollinen.FI')
        before(
          editor.edit,
          uusiOppiaine.selectValue('Filosofia'),
          filosofia.propertyBySelector('.arvosana').selectValue('8'),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )

        describe('Lisääminen', function () {
          it('Uusi oppiaine näytetään', function () {
            expect(extractAsText(S('.oppiaineet'))).to.contain('Filosofia 8')
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
      })

      describe('Opiskelu toiminta-alueittain', function () {
        describe('Toiminta-alueen lisääminen', function () {
          var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine()
          var kognitiivisetTaidot = editor.subEditor('.valinnainen.5')
          before(
            editor.edit,
            opinnot.expandAll,
            editor.property('erityisenTuenPäätökset').addItem,
            editor.property('opiskeleeToimintaAlueittain').setValue(true),
            uusiOppiaine.selectValue('kognitiiviset taidot'),
            kognitiivisetTaidot
              .propertyBySelector('.arvosana')
              .selectValue('8'),
            editor.saveChanges
          )

          it('toimii', function () {
            expect(
              kognitiivisetTaidot.propertyBySelector('.arvosana').getValue()
            ).to.equal('8')
          })
        })
      })
    })
    describe('Opiskeluoikeuden lisääminen', function () {
      before(prepareForNewOppija('kalle', '230872-7258'))
      before(
        addOppija.enterValidDataPerusopetus({
          opiskeluoikeudenTyyppi: 'Perusopetuksen lisäopetus',
          maksuttomuus: 0
        })
      )
      before(
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'Perusopetuksen lisäopetus'
        )
      )
      it('Lisätty opiskeluoikeus näytetään', function () {
        expect(opinnot.getTutkinto()).to.equal('Perusopetuksen lisäopetus')
        expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
        expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal(
          '105/011/2014'
        )
        expect(opinnot.getSuorituskieli()).to.equal('suomi')
      })
    })
  })

  describe('Perusopetukseen valmistava opetus', function () {
    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('220109-784L'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
        'perusopetukseenvalmistavaopetus'
      )
    )
    describe('Kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)
      it('näyttää opiskeluoikeuden tiedot', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 15.8.2017 — 1.6.2018\n' +
          'Tila 1.6.2018 Valmistunut\n' +
          '6.1.2018 Läsnä\n' +
          '20.12.2017 Loma\n' +
          '15.8.2017 Läsnä'
        )
      })
      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Perusopetukseen valmistava opetus 999905 57/011/2015\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Suorituskieli suomi\n' +
          'Kokonaislaajuus 11 tuntia\n' +
          'Suoritus valmis Vahvistus : 1.6.2018 Jyväskylä Reijo Reksi , rehtori'
        )
      })
      it('näyttää oppiaineiden arvosanat', function () {
        expect(extractAsText(S('.oppiaineet'))).to.equal(
          'Arviointiasteikko\n' +
          'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
          'Perusopetukseen valmistavan opetuksen opinnot Oppiaine Arvosana Laajuus\n' +
          'Äidinkieli S 10 vuosiviikkotuntia\n' +
          'Sanallinen arviointi Keskustelee sujuvasti suomeksi\n' +
          'Opetuksen sisältö Suullinen ilmaisu ja kuullun ymmärtäminen Perusopetuksen oppimäärään sisältyvät opinnot Oppiaine Arvosana Laajuus\n' +
          'Fysiikka 9 1 vuosiviikkotuntia\n' +
          'Suorituskieli suomi\n' +
          'Luokka-aste 7. vuosiluokka\n' +
          'Suoritustapa Erityinen tutkinto'
        )
      })
    })
    describe('Tietojen muuttaminen', function () {
      describe('Oppiaineen arvosanan muutos', function () {
        var äidinkieli = opinnot.oppiaineet.oppiaine(0)
        var arvosana = äidinkieli.propertyBySelector('.arvosana')
        before(editor.edit, arvosana.selectValue('H'), editor.saveChanges)
        it('muutettu arvosana näytetään', function () {
          expect(arvosana.getValue()).to.equal('H')
        })
        after(
          editor.edit,
          arvosana.selectValue('S'),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )
      })
      describe('Oppiaine', function () {
        var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine(
          '.uusi-perusopetukseen-valmistava-oppiaine'
        )
        describe('Uuden oppiaineen lisääminen', function () {
          var uusiPaikallinen = editor.subEditor(
            '.oppiaine-rivi:nth-of-type(2)'
          )
          before(
            editor.edit,
            uusiOppiaine.selectValue('Lisää'),
            uusiPaikallinen.propertyBySelector('.koodi').setValue('TNS'),
            uusiPaikallinen.propertyBySelector('.nimi').setValue('Tanssi'),
            uusiPaikallinen.propertyBySelector('.arvosana').selectValue('S'),
            uusiPaikallinen
              .propertyBySelector('.property.laajuus .value')
              .setValue('1'),
            uusiPaikallinen
              .propertyBySelector('.property.yksikko')
              .setValue('vuosiviikkotuntia'),
            editor.saveChanges,
            wait.until(page.isSavedLabelShown)
          )

          it('Toimii', function () {
            expect(extractAsText(S('.oppiaineet'))).to.contain('Tanssi S')
          })

          describe('Lisäyksen jälkeen', function () {
            var tanssi = editor.subEditor('.valinnainen.TNS')
            before(editor.edit)
            it('Valinnaisia aineita voi lisätä useaan kertaan', function () {
              expect(uusiOppiaine.getOptions().includes('Tanssi')).to.equal(
                true
              )
            })

            describe('Poistettaessa suoritus', function () {
              before(tanssi.propertyBySelector('>tr:first-child').removeValue)
              it('Uusi oppiaine löytyy listalta', function () {
                expect(uusiOppiaine.getOptions()).to.include('Tanssi')
              })

              describe('Muutettaessa lisätyn oppiaineen kuvausta, tallennettaessa ja poistettaessa oppiaine', function () {
                before(
                  uusiOppiaine.selectValue('Tanssi'),
                  tanssi.propertyBySelector('.arvosana').selectValue('H'),
                  tanssi
                    .propertyBySelector('.nimi')
                    .setValue('Tanssi ja liike'),
                  editor.saveChanges,
                  editor.edit
                )
                before(tanssi.propertyBySelector('>tr:first-child').removeValue)

                it('Muutettu oppiaine löytyy listalta', function () {
                  expect(uusiOppiaine.getOptions()[0]).to.equal(
                    'Tanssi ja liike'
                  )
                })

                after(editor.cancelChanges)
              })
            })
          })
        })

        describe('Uuden nuorten perusopetuksen oppiaineen lisääminen', function () {
          var uusiNuortenOppiaine = OpinnotPage()
            .opiskeluoikeusEditor()
            .propertyBySelector('.oppiaine-taulukko > .uusi-oppiaine')
          var historia = editor.subEditor('.HI')
          before(
            editor.edit,
            uusiNuortenOppiaine.selectValue('Historia'),
            historia.propertyBySelector('.arvosana').selectValue('8'),
            historia
              .propertyBySelector('.luokkaAste')
              .selectValue('7. vuosiluokka'),
            historia
              .propertyBySelector('.property.laajuus .value')
              .setValue('1')
          )

          it('Uusi oppiaine näytetään', function () {
            expect(historia.property('luokkaAste').isVisible()).to.equal(true)
          })

          describe('Nuorten perusopetuksen oppiaineen tallentaminen', function () {
            before(editor.saveChanges)

            it('toimii', function () { })
          })
        })
      })
    })
    describe('Opiskeluoikeuden lisääminen', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataPerusopetus({ suorituskieli: 'suomi' }),
        addOppija.selectOpiskeluoikeudenTyyppi(
          'Perusopetukseen valmistava opetus'
        )
      )
      describe('Tietojen näyttäminen', function () {
        it('Näytetään tilavaihtoehdoissa kaikki tilat paitsi mitätöity', function () {
          expect(addOppija.opiskeluoikeudenTilat()).to.deep.equal([
            'Eronnut',
            'Katsotaan eronneeksi',
            'Loma',
            'Läsnä',
            'Peruutettu',
            'Valmistunut',
            'Väliaikaisesti keskeytynyt'
          ])
        })

        describe('Kun lisätään oppija', function () {
          before(
            addOppija.submitAndExpectSuccess(
              'Tyhjä, Tero (230872-7258)',
              'Perusopetukseen valmistava opetus'
            )
          )
          it('Lisätty opiskeluoikeus näytetään', function () {
            expect(opinnot.getTutkinto()).to.equal(
              'Perusopetukseen valmistava opetus'
            )
            expect(opinnot.getOppilaitos()).to.equal('Jyväskylän normaalikoulu')
            expect(
              editor.propertyBySelector('.diaarinumero').getValue()
            ).to.equal('57/011/2015')
            expect(opinnot.getSuorituskieli()).to.equal('suomi')
          })
        })
      })
    })
  })

  describe('Pakollisen oppiaineen laajuus nuorten päättötodistuksella ja vuosiluokan suorituksella', function () {
    before(
      resetFixtures,
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('220109-784L'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus'),
      editor.edit,
      editor.property('tila').removeItem(0),
      opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
      editor.saveChanges
    )

    var matikka = opinnot.oppiaineet.oppiaine('pakollinen.MA')
    var ennenLeikkuriPäivää = '31.7.2020'
    var leikkuriPäivä = '1.8.2020'

    describe('Asetetaan pakolliselle oppiaineelle laajuus', function () {
      before(
        editor.edit,
        matikka.property('laajuus').setValue('4'),
        editor.saveChanges
      )
      it('Laajuutta ei näytetä, koska päätason suorituksella ei ole vahvistusta', function () {
        expect(matikka.property('laajuus').isVisible()).to.equal(false)
      })

      describe('Asetetaan päätason suoritukselle vahvistus ennen leikkuripäivää', function () {
        before(
          editor.edit,
          tilaJaVahvistus.merkitseValmiiksi,
          opinnot.tilaJaVahvistus.lisääVahvistus(ennenLeikkuriPäivää),
          editor.saveChanges
        )
        it('Laajuutta ei näytetä, koska vahvistus on ennen leikkuripäivää', function () {
          expect(matikka.property('laajuus').isVisible()).to.equal(false)
        })

        describe('Asetetaan päätason suoritukselle vahvistus leikkuripäivälle', function () {
          before(
            editor.edit,
            tilaJaVahvistus.merkitseKeskeneräiseksi,
            tilaJaVahvistus.merkitseValmiiksi,
            opinnot.tilaJaVahvistus.lisääVahvistus(leikkuriPäivä),
            editor.saveChanges
          )
          it('Laajuus näytetään', function () {
            expect(matikka.property('laajuus').isVisible()).to.equal(true)
          })
        })
      })
    })
  })
})
