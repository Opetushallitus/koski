describe('Perusopetus 1', function () {
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

  describe('Perusopetuksen lukuvuositodistukset ja päättötodistus', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('220109-784L'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus')
    )

    it('näyttää opiskeluoikeuden tiedot', function () {
      expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
        'Opiskeluoikeuden voimassaoloaika : 15.8.2008 — 4.6.2016\n' +
        'Tila 4.6.2016 Valmistunut\n' +
        '15.8.2008 Läsnä'
      )

      expect(
        opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()
      ).to.deep.equal(['Perusopetuksen oppimäärä 2008—2016, Valmistunut'])
    })

    describe('Perusopetuksen oppimäärä', function () {
      describe('Kaikki tiedot näkyvissä', function () {
        before(opinnot.expandAll)
        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Koulutus Perusopetus 201101 104/011/2014\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Suoritustapa Koulutus\n' +
            'Suorituskieli suomi\n' +
            'Koulusivistyskieli suomi\n' +
            'Suoritus valmis Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori'
          )
        })
        it('näyttää oppiaineiden arvosanat', function () {
          expect(extractAsText(S('.oppiaineet'))).to.equal(
            'Arviointiasteikko\n' +
            'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
            'Yhteiset oppiaineet\n' +
            'Oppiaine Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\n' +
            'Suoritustapa Erityinen tutkinto\n' +
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
            'Kuvaus Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.\n' +
            '* = yksilöllistetty tai rajattu oppimäärä, ** = painotettu opetus'
          )
        })

        describe('Kun suoritus on kesken', function () {
          before(
            editor.edit,
            editor.property('tila').removeItem(0),
            opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
            editor.saveChanges
          )

          it('arvosanoja ei näytetä', function () {
            expect(isElementVisible(S('.oppiaineet .arvosana'))).to.equal(false)
          })

          describe('Kun vahvistus on tulevaisuudessa', function () {
            var future = moment().add(1, 'years').format('D.M.YYYY')
            before(
              editor.edit,
              tilaJaVahvistus.merkitseValmiiksi,
              opinnot.tilaJaVahvistus.lisääVahvistus(future),
              editor.saveChanges
            )

            it('arvosanat näytetään', function () {
              expect(isElementVisible(S('.oppiaineet .arvosana'))).to.equal(
                true
              )
            })
          })
          after(
            resetFixtures,
            page.openPage,
            page.oppijaHaku.searchAndSelect('220109-784L'),
            opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
              'perusopetus'
            )
          )
        })
      })
    })

    describe('Lukuvuosisuoritus 8. luokka', function () {
      before(
        wait.until(page.isOppijaSelected('Kaisa')),
        opinnot.valitseSuoritus(undefined, '8. vuosiluokka')
      )
      describe('Kaikki tiedot näkyvissä', function () {
        before(opinnot.expandAll)
        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Luokka-aste 8. vuosiluokka 8 104/011/2014\n' +
            'Luokka 8C\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Alkamispäivä 15.8.2014\n' +
            'Suoritustapa Koulutus\n' +
            'Suorituskieli suomi\n' +
            'Muut suorituskielet sloveeni\n' +
            'Kielikylpykieli ruotsi\n' +
            'Suoritus valmis Vahvistus : 30.5.2015 Jyväskylä Reijo Reksi , rehtori\n' +
            'Siirretään seuraavalle luokalle'
          )
        })
        it('näyttää oppiaineiden arvosanat', function () {
          expect(extractAsText(S('.oppiaineet'))).to.equal(
            'Arviointiasteikko\n' +
            'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
            'Yhteiset oppiaineet\n' +
            'Oppiaine Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 9\n' +
            'Suoritustapa Erityinen tutkinto\n' +
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
            'Kuvaus Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.\n' +
            'Käyttäytymisen arviointi\n' +
            'Arvosana S\n' +
            'Sanallinen arviointi Esimerkillistä käyttäytymistä koko vuoden ajan\n' +
            '* = yksilöllistetty tai rajattu oppimäärä, ** = painotettu opetus'
          )
        })
      })
    })

    describe('Lukuvuosisuoritus 9. luokka', function () {
      before(
        wait.until(page.isOppijaSelected('Kaisa')),
        opinnot.valitseSuoritus(undefined, '9. vuosiluokka')
      )
      describe('Kaikki tiedot näkyvissä', function () {
        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Luokka-aste 9. vuosiluokka 9 104/011/2014\n' +
            'Luokka 9C\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Alkamispäivä 15.8.2015\n' +
            'Suorituskieli suomi\n' +
            'Suoritus valmis Vahvistus : 30.5.2016 Jyväskylä Reijo Reksi , rehtori'
          )
        })
      })
      describe('Lukuvuositodistus', function () {
        it('ei näytetä', function () {
          expect(S('a.todistus').is(':visible')).to.equal(false)
        })
      })
    })

    describe('Luokalle jäänyt 7-luokkalainen', function () {
      before(
        wait.until(page.isOppijaSelected('Kaisa')),
        opinnot.valitseSuoritus(undefined, '7. vuosiluokka')
      )
      describe('Kaikki tiedot näkyvissä', function () {
        before(opinnot.expandAll)
        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Luokka-aste 7. vuosiluokka 7 104/011/2014\n' +
            'Luokka 7C\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Alkamispäivä 16.8.2013\n' +
            'Suorituskieli suomi\n' +
            'Suoritus valmis Vahvistus : 30.5.2014 Jyväskylä Reijo Reksi , rehtori\n' +
            'Ei siirretä seuraavalle luokalle'
          )
        })
      })
    })

    describe('Päättötodistus toiminta-alueittain', function () {
      before(
        resetFixtures,
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect('031112-020J')
      )
      describe('Kaikki tiedot näkyvissä', function () {
        before(opinnot.expandAll)

        it('näyttää opiskeluoikeuden tiedot', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
            'Opiskeluoikeuden voimassaoloaika : 15.8.2017 — 18.10.2024\n' +
            'Tila 18.10.2024 Valmistunut\n' +
            '15.8.2017 Läsnä\n' +
            'Lisätiedot\n' +
            'Pidennetty oppivelvollisuus 15.8.2019 — 18.10.2024\n' +
            'Erityisen tuen jaksot 15.8.2017 — 18.10.2024\n' +
            'Opiskelee toiminta-alueittain kyllä\n' +
            'Opiskelee erityisryhmässä kyllä\n' +
            'Erityisen tuen jaksot 15.8.2017 — 18.10.2024\n' +
            'Opiskelee toiminta-alueittain kyllä\n' +
            'Opiskelee erityisryhmässä kyllä\n' +
            'Joustava perusopetus 15.8.2017 — 18.10.2024\n' +
            'Kotiopetusjaksot 15.8.2017 — 18.10.2024\n' +
            '14.7.2026 — 18.10.2026\n' +
            'Ulkomaanjaksot 15.8.2017 — 18.10.2024\n' +
            '16.9.2027 — 2.10.2028\n' +
            'Vuosiluokkiin sitomaton opetus kyllä\n' +
            'Oppilas on muiden kuin vaikeimmin kehitysvammaisten opetuksessa 15.8.2019 — 1.9.2019\n' +
            'Vaikeimmin kehitysvammainen 2.9.2019 — 18.10.2024\n' +
            'Majoitusetu 15.8.2017 — 18.10.2024\n' +
            'Kuljetusetu 15.8.2017 — 18.10.2024\n' +
            'Sisäoppilaitosmainen majoitus 1.9.2021 — 1.9.2022\n' +
            'Koulukoti 1.9.2022 — 1.9.2023'
          )
        })

        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Koulutus Perusopetus 201101 104/011/2014\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Suoritustapa Erityinen tutkinto\n' +
            'Suorituskieli suomi\n' +
            'Suoritus valmis Vahvistus : 18.10.2024 Jyväskylä Reijo Reksi , rehtori'
          )
        })
        it('näyttää oppiaineiden arvosanat', function () {
          expect(extractAsText(S('.oppiaineet'))).to.equal(
            'Toiminta-alueiden arvosanat\n' +
            'Arvostelu 4-10, S (suoritettu) tai H (hylätty)\n' +
            'Toiminta-alue Arvosana Laajuus\n' +
            'motoriset taidot S 5 vuosiviikkotuntia\n' +
            'Sanallinen arviointi Motoriset taidot kehittyneet hyvin perusopetuksen aikana\n' +
            'kieli ja kommunikaatio S 5 vuosiviikkotuntia\nsosiaaliset taidot S 5 vuosiviikkotuntia\n' +
            'päivittäisten toimintojen taidot S 5 vuosiviikkotuntia\n' +
            'kognitiiviset taidot S 5 vuosiviikkotuntia'
          )
        })
      })

      describe('Tietojen muuttaminen', function () {
        describe('Oppiaineet', function () {
          var uusiOppiaine = opinnot.oppiaineet.uusiOppiaine()
          var sosiaalisetTaidot = editor.subEditor('.3')

          describe('Poistaminen', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              editor.property('erityisenTuenPäätös').removeValue,
              sosiaalisetTaidot.propertyBySelector('>tr:first-child')
                .removeValue,
              editor.saveChanges,
              wait.until(page.isSavedLabelShown)
            )

            it('toimii', function () {
              expect(extractAsText(S('.oppiaineet'))).to.not.contain(
                'Filosofia 8'
              )
            })

            describe('Lisääminen ilman laajuutta', function () {
              before(
                editor.edit,
                uusiOppiaine.selectValue('sosiaaliset taidot'),
                sosiaalisetTaidot
                  .propertyBySelector('.arvosana')
                  .selectValue('8'),
                editor.saveChangesAndExpectError,
                wait.until(page.isErrorShown)
              )

              it('näyttää virheilmoituksen laajuudesta', function () {
                expect(extractAsText(S('.error-text'))).to.equal(
                  'Oppiaineen sosiaaliset taidot laajuus puuttuu'
                )
              })

              describe('laajuudella', function () {
                before(
                  sosiaalisetTaidot
                    .propertyBySelector('.property.laajuus')
                    .setValue(5),
                  editor.saveChanges,
                  wait.until(page.isSavedLabelShown)
                )

                it('toimii', function () {
                  expect(extractAsText(S('.oppiaineet'))).to.contain(
                    'sosiaaliset taidot 8'
                  )
                })
              })
            })
          })
        })

        describe('Vuosiluokan suorituksen lisäys', function () {
          var lisääSuoritus = opinnot.lisääSuoritusDialog
          before(
            editor.edit,
            editor.property('tila').removeItem(0),
            lisääSuoritus.open('lisää vuosiluokan suoritus'),
            lisääSuoritus.property('luokka').setValue('1a'),
            lisääSuoritus.toimipiste.select(
              'Jyväskylän normaalikoulu, alakoulu'
            ),
            lisääSuoritus.property('alkamispäivä').setValue(currentDateStr),
            lisääSuoritus.lisääSuoritus
          )
          it('Esitäyttää toiminta-alueet', function () {
            expect(textsOf(S('.oppiaineet .oppiaine .nimi'))).to.deep.equal([
              'motoriset taidot',
              'kieli ja kommunikaatio',
              'sosiaaliset taidot',
              'päivittäisten toimintojen taidot',
              'kognitiiviset taidot'
            ])
          })
        })
      })

      /*
      describe('Deprekoidut kentät', function() {
        before(
          resetFixtures,
          page.openPage,
          page.oppijaHaku.searchAndSelect('031112-020J'),
          opinnot.expandAll, editor.edit, editor.property('opiskeleeToimintaAlueittain').setValue(false)
        )

        it('muokkaus estetty', function() {
          expect(extractAsText(findSingle('.lisätiedot .erityisenTuenPäätös .propertyError'))).to.equal('Käytä korvaavaa kenttää Erityisen tuen päätökset')
          expect(editor.canSave()).to.equal(false)
        })

        describe('Jos ei sisällä dataa', function() {
          before(
            page.openPage,
            page.oppijaHaku.searchAndSelect('220109-784L'),
            editor.edit,
            wait.untilVisible(findSingle('.lisätiedot .expandable')),
            opinnot.expandAll
          )

          it('piilotetaan', function() {
            var lisätiedot = textsOf(S('.lisätiedot .property .label'))
            expect(lisätiedot.includes('Erityisen tuen päätös')).to.equal(false)
            expect(lisätiedot.includes('Erityisen tuen päätökset')).to.equal(true)
            expect(lisätiedot.includes('Tehostetun tuen päätös')).to.equal(false)
            expect(lisätiedot.includes('Tehostetun tuen päätökset')).to.equal(true)
          })
        })
      })
      */

      describe('Ilman luottamuksellinen-roolia', function () {
        before(
          Authentication().logout,
          Authentication().login('epäluotettava-tallentaja')
        )
        before(page.openPage, page.oppijaHaku.searchAndSelect('031112-020J'))
        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Koulutus Perusopetus 201101 104/011/2014\n' +
            'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
            'Suoritustapa Erityinen tutkinto\n' +
            'Suorituskieli suomi\n' +
            'Suoritus valmis Vahvistus : 18.10.2024 Jyväskylä Reijo Reksi , rehtori'
          )
        })
      })
    })

    describe('Virhetilanteet', function () {
      describe('Todistuksen avaaminen, kun käyttäjä ei ole kirjautunut', function () {
        before(
          Authentication().logout,
          reloadTestFrame,
          wait.until(login.isVisible)
        )
        it('Näytetään login-sivu', function () {
          expect(login.isVisible()).to.equal(true)
        })
      })

      describe('Todistuksen avaaminen, kun todistusta ei löydy', function () {
        before(
          Authentication().login(),
          page.openPage,
          openPage('/koski/1010101010', page.is404)
        )
        it('Näytetään 404-sivu', function () { })
      })
    })
  })

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

              äidinkieli.avaaLisääKurssiDialog,
              äidinkieli.lisääKurssiDialog.valitseKurssi('IS21'),
              äidinkieli.lisääKurssiDialog.lisääKurssi,
              äidinkieli.kurssi('IS21').arvosana.setValue('8'),

              äidinkieli.avaaLisääKurssiDialog,
              äidinkieli.lisääKurssiDialog.valitseKurssi('IMO1'),
              äidinkieli.lisääKurssiDialog.lisääKurssi,
              äidinkieli.kurssi('IMO1').arvosana.setValue('8'),
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

  describe('Hetuton oppija', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('1.2.246.562.24.99999999123', 'Hetuton')
    )

    it('näyttää syntymäajan hetun sijaan', function () {
      expect(page.getSelectedOppija()).to.equal('Hetuton, Heikki (24.2.1977)')
    })

    it('näyttää opiskeluoikeuden tiedot', function () {
      expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
        'Opiskeluoikeuden voimassaoloaika : 15.8.2008 —\n' +
        'Tila 15.8.2008 Läsnä'
      )
    })
  })
})
