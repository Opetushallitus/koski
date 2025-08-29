describe('Ammatillinen koulutus 1', function () {
  before(Authentication().login())

  var addOppija = AddOppijaPage()
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  describe('Opiskeluoikeuden lisääminen', function () {
    this.timeout(40000)
    describe('Olemassa olevalle henkilölle', function () {
      this.timeout(40000)
      before(
        timeout.overrideWaitTime(40000),
        prepareForNewOppija('kalle', '280608-6619')
      )
      before(addOppija.enterValidDataAmmatillinen())

      describe('Tietojen näyttäminen', function () {
        it('Näytetään henkilöpalvelussa olevat nimitiedot', function () {
          expect(addOppija.henkilötiedot()).to.deep.equal([
            'Tero Petteri Gustaf',
            'Tero',
            'Tunkkila-Fagerlund'
          ])
        })
      })

      describe('Kun lisätään oppija', function () {
        before(
          addOppija.submitAndExpectSuccess(
            'Tunkkila-Fagerlund, Tero Petteri Gustaf (280608-6619)',
            'Autoalan perustutkinto'
          )
        )
        it('Onnistuu, näyttää henkilöpalvelussa olevat nimitiedot', function () { })
      })
    })

    describe('Uudelle henkilölle', function () {
      before(prepareForNewOppija('kalle', '230872-7258'))

      describe('Tietojen näyttäminen', function () {
        it('Näytetään tyhjät nimitietokentät', function () {
          expect(addOppija.henkilötiedot()).to.deep.equal(['', '', ''])
        })

        it('Ei näytetä opintojen rahoitus -kenttää', function () {
          expect(addOppija.rahoitusIsVisible()).to.equal(false)
        })
      })

      describe('Kun valitaan opiskeluoikeudeksi Ammatillinen koulutus', function () {
        before(
          addOppija.selectOppilaitos('Stadin'),
          addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
          addOppija.selectSuoritustyyppi('Ammatillinen tutkinto')
        )

        it('Näytetään opintojen rahoitus -kenttä', function () {
          eventually(() => expect(addOppija.rahoitusIsVisible()).to.equal(true))
        })

        it('Vaihtoehtoina on kaikki opintojenRahoitus-vaihtoehdot', function () {
          eventually(() =>
            expect(addOppija.opintojenRahoitukset()).to.deep.equal([
              'Valtionosuusrahoitteinen koulutus',
              'Työvoimakoulutus (OKM rahoitus)',
              'Ammatillisen osaamisen pilotit 2019',
              'Ammatillisen osaamisen pilotit 2019 (työvoimakoulutus)',
              'Työvoimakoulutus (valtiosopimukseen perustuva rahoitus)',
              'Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus',
              'Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus (RRF)',
              'Työvoimakoulutus ely-keskukset ja työ- ja elinkeinotoimistot (kansallinen rahoitus)',
              'Työvoimakoulutus (ESR-rahoitteinen)',
              'Työnantajan kokonaan rahoittama',
              'Muuta kautta rahoitettu',
              'Nuorten aikuisten osaamisohjelma',
              'Aikuisten osaamisperustan vahvistaminen',
              'Maahanmuuttajien ammatillinen koulutus (valtionavustus)'
            ])
          )
        })

        it('Näytetään tilavaihtoehdoissa loma-tila, mutta ei eronnut-tilaa', function () {
          eventually(() =>
            expect(addOppija.opiskeluoikeudenTilat()).to.deep.equal([
              'Katsotaan eronneeksi',
              'Loma',
              'Läsnä',
              'Peruutettu',
              'Valmistunut',
              'Väliaikaisesti keskeytynyt'
            ])
          )
        })

        describe('Koulutusvienti', () => {
          before((done) => {
            addOppija
              .enterTutkinto('auto')()
              .then(wait.until(addOppija.tutkinnotIsVisible))
              .then(done)
          })
          it('Näytetään myös koulutusviennin kautta tuodut tutkinnot', function () {
            eventually(() =>
              expect(addOppija.tutkinnot()).to.equal(
                'Autoalan perustutkinto 39/011/2014 Autoalan työnjohdon erikoisammattitutkinto 40/011/2001 Auto- ja kuljetusalan työnjohdon ammattitutkinto 30/011/2015 Automaatioasentajan ammattitutkinto 3/011/2013 Automaatioyliasentajan erikoisammattitutkinto 9/011/2008 Puutavaran autokuljetuksen ammattitutkinto 27/011/2008 Sähkö- ja automaatiotekniikan perustutkinto 77/011/2014 Autoalan perustutkinto OPH-2762-2017 Automekaanikon erikoisammattitutkinto OPH-1886-2017 Autoalan perustutkinto, Koulutusvientikokeilu OPH-4792-2017'
              )
            )
          })
        })
      })

      describe('Kun lisätään oppija', function () {
        before(
          addOppija.enterValidDataAmmatillinen({ suorituskieli: 'ruotsi' })
        )
        before(
          addOppija.submitAndExpectSuccess(
            'Tyhjä, Tero (230872-7258)',
            'Autoalan perustutkinto'
          )
        )

        describe('Lisäyksen jälkeen', function () {
          it('lisätty oppija näytetään', function () { })

          it('Lisätty opiskeluoikeus näytetään', function () {
            expect(opinnot.getOpiskeluoikeudenTila()).to.match(
              /Läsnä \(valtionosuusrahoitteinen koulutus\)$/
            )
            expect(opinnot.getTutkinto()).to.equal('Autoalan perustutkinto')
            expect(opinnot.getOppilaitos()).to.equal(
              'Stadin ammatti- ja aikuisopisto'
            )
            expect(opinnot.getSuorituskieli()).to.equal('ruotsi')
          })
        })

        describe('Toisen ammatillisen tutkinnon lisääminen samaan opiskeluoikeuteen', function () {
          var lisääSuoritus = opinnot.lisääSuoritusDialog
          before(editor.edit)
          it('ei ole mahdollista', function () {
            expect(
              lisääSuoritus.isLinkVisible(
                'lisää ammatillisen tutkinnon suoritus'
              )
            ).to.equal(false)
          })
        })
      })
    })

    describe('Henkilöpalvelusta löytyvälle oppijalle, jolla on OID ja Hetu', function () {
      before(prepareForNewOppija('kalle', '1.2.246.562.24.99999555555'))
      describe('Tietojen näyttäminen', function () {
        it('Näytetään täydennetyt nimitietokentät', function () {
          expect(addOppija.henkilötiedot()).to.deep.equal([
            'Eino',
            'Eino',
            'EiKoskessa'
          ])
        })
        it('Hetua ei näytetä', function () {
          expect(addOppija.hetu()).equal('')
        })
      })

      describe('Kun lisätään oppija', function () {
        before(addOppija.enterValidDataAmmatillinen())
        before(
          addOppija.submitAndExpectSuccess(
            'EiKoskessa, Eino (270181-5263)',
            'Autoalan perustutkinto'
          )
        )

        it('lisätty oppija näytetään', function () { })

        it('Lisätty opiskeluoikeus näytetään', function () {
          expect(opinnot.getTutkinto()).to.equal('Autoalan perustutkinto')
          expect(opinnot.getOppilaitos()).to.equal(
            'Stadin ammatti- ja aikuisopisto'
          )
          expect(opinnot.getSuorituskieli()).to.equal('suomi')
        })
      })
    })

    describe('Henkilöpalvelusta löytyvälle oppijalle, jolla on vanha hetu', function () {
      before(prepareForNewOppija('kalle', '270181-517T'))
      describe('Tietojen näyttäminen', function () {
        it('Näytetään täydennetyt nimitietokentät', function () {
          expect(addOppija.henkilötiedot()).to.deep.equal([
            'Eino',
            'Eino',
            'EiKoskessa'
          ])
        })
        it('Hetu näkyy', function () {
          expect(addOppija.hetu()).equal('270181-517T')
        })
      })

      describe('Kun lisätään oppija', function () {
        before(addOppija.enterValidDataAmmatillinen())
        before(
          addOppija.submitAndExpectSuccess(
            'EiKoskessa, Eino (270181-5263)',
            'Autoalan perustutkinto'
          )
        )

        it('lisätyn oppijan uusi hetu näytetään', function () {
          expect(KoskiPage().getSelectedOppija()).to.equal(
            'EiKoskessa, Eino (270181-5263)'
          )
        })

        it('Lisätty opiskeluoikeus näytetään', function () {
          expect(opinnot.getTutkinto()).to.equal('Autoalan perustutkinto')
          expect(opinnot.getOppilaitos()).to.equal(
            'Stadin ammatti- ja aikuisopisto'
          )
          expect(opinnot.getSuorituskieli()).to.equal('suomi')
        })
      })
    })

    describe('Henkilöpalvelusta löytyvälle oppijalle, jolla on vain OID', function () {
      before(prepareForNewOppija('kalle', '1.2.246.562.24.99999555556'))
      describe('Tietojen näyttäminen', function () {
        it('Näytetään täydennetyt nimitietokentät', function () {
          expect(addOppija.henkilötiedot()).to.deep.equal([
            'Eino',
            'Eino',
            'EiKoskessaHetuton'
          ])
        })
      })

      describe('Kun lisätään oppija', function () {
        before(addOppija.enterValidDataAmmatillinen())
        before(
          addOppija.submitAndExpectSuccess(
            'EiKoskessaHetuton, Eino',
            'Autoalan perustutkinto'
          )
        )

        it('lisätty oppija näytetään', function () { })

        it('Lisätty opiskeluoikeus näytetään', function () {
          expect(opinnot.getTutkinto()).to.equal('Autoalan perustutkinto')
          expect(opinnot.getOppilaitos()).to.equal(
            'Stadin ammatti- ja aikuisopisto'
          )
          expect(opinnot.getSuorituskieli()).to.equal('suomi')
        })
      })
    })

    describe('Validointi', function () {
      before(prepareForNewOppija('kalle', '230872-7258'))

      describe('Aluksi', function () {
        it('Lisää-nappi on disabloitu', function () {
          expect(addOppija.isEnabled()).to.equal(false)
        })
        it('Tutkinto-kenttä on disabloitu', function () {
          expect(addOppija.tutkintoIsEnabled()).to.equal(false)
        })
      })
      describe('Kun kutsumanimi löytyy väliviivallisesta nimestä', function () {
        before(
          addOppija.enterValidDataAmmatillinen({
            etunimet: 'Juha-Pekka',
            kutsumanimi: 'Pekka'
          })
        )
        it('Lisää-nappi on enabloitu', function () {
          expect(addOppija.isEnabled()).to.equal(true)
        })
      })
      describe('Aloituspäivä', function () {
        describe('Kun syötetään epäkelpo päivämäärä', function () {
          before(
            addOppija.enterValidDataAmmatillinen({
              etunimet: 'Juha-Pekka',
              kutsumanimi: 'Pekka'
            }),
            addOppija.selectAloituspäivä('38.1.2070')
          )
          it('Lisää-nappi on disabloitu', function () {
            expect(addOppija.isEnabled()).to.equal(false)
          })
        })
        describe('Kun valitaan kelvollinen päivämäärä', function () {
          before(
            addOppija.enterValidDataAmmatillinen({
              etunimet: 'Juha-Pekka',
              kutsumanimi: 'Pekka'
            }),
            addOppija.selectAloituspäivä('1.1.2020')
          )
          it('Lisää-nappi on enabloitu', function () {
            expect(addOppija.isEnabled()).to.equal(true)
          })
        })
      })
      describe('Tutkinto', function () {
        before(addOppija.enterValidDataAmmatillinen())
        describe('Aluksi', function () {
          it('Lisää-nappi enabloitu', function () {
            expect(addOppija.isEnabled()).to.equal(true)
          })
        })
        describe('Kun tutkinto on virheellinen', function () {
          before(addOppija.enterTutkinto('virheellinen'))
          it('Lisää-nappi on disabloitu', function () {
            expect(addOppija.isEnabled()).to.equal(false)
          })
        })
      })
      describe('Oppilaitosvalinta', function () {
        describe('Näytetään vain käyttäjän organisaatiopuuhun kuuluvat oppilaitokset', function () {
          describe('Kun vain 1 vaihtoehto', function () {
            before(
              prepareForNewOppija('omnia-palvelukäyttäjä', '230872-7258'),
              addOppija.enterHenkilötiedot(),
              addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
              addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
              addOppija.selectTutkinto('auto'),
              addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
              addOppija.selectAloituspäivä('1.1.2018'),
              addOppija.selectMaksuttomuus(0)
            )
            it('Vaihtoehto on valmiiksi valittu', function () {
              expect(addOppija.oppilaitos()).to.deep.equal('Omnia')
            })
            it('Lisää-nappi on enabloitu', function () {
              expect(addOppija.isEnabled()).to.equal(true)
            })
          })
          describe('Kun useampia vaihtoehtoja', function () {
            before(
              prepareForNewOppija('kalle', '230872-7258'),
              addOppija.enterValidDataAmmatillinen(),
              addOppija.enterOppilaitos('ammatti'),
              wait.forMilliseconds(500)
            )
            it('Mahdollistetaan valinta', function () {
              expect(addOppija.oppilaitokset()).to.deep.equal([
                'Lahden ammattikorkeakoulu (lakkautettu)',
                'Stadin ammatti- ja aikuisopisto'
              ])
            })
          })
        })
        describe('Kun oppilaitosta ei olla valittu', function () {
          before(
            prepareForNewOppija('kalle', '230872-7258'),
            addOppija.enterData({ oppilaitos: undefined })
          )
          it('Lisää-nappi on disabloitu', function () {
            expect(addOppija.isEnabled()).to.equal(false)
          })
          it('Tutkinnon valinta on estetty', function () {
            expect(addOppija.tutkintoIsEnabled()).to.equal(false)
          })
        })
        describe('Kun oppilaitos on valittu', function () {
          before(addOppija.enterValidDataAmmatillinen())
          it('voidaan valita tutkinto', function () {
            expect(addOppija.tutkintoIsEnabled()).to.equal(true)
            expect(addOppija.isEnabled()).to.equal(true)
          })
        })
        describe('Kun oppilaitos-valinta muutetaan', function () {
          before(
            addOppija.selectOppilaitos('Omnia'),
            addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus')
          )
          describe('Tutkinnon valinnan jälkeen', function () {
            before(
              addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
              addOppija.selectTutkinto('auto'),
              addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
              addOppija.selectMaksuttomuus(0)
            )
            it('Lisää-nappi on enabloitu', function () {
              expect(addOppija.isEnabled()).to.equal(true)
            })
          })
        })
      })
      describe('Hetun validointi', function () {
        before(Authentication().login(), page.openPage)
        describe('Kun hetu on virheellinen', function () {
          before(
            page.oppijaHaku.search(
              '123456-1234',
              page.oppijaHaku.isNoResultsLabelShown
            )
          )
          it('Lisää-nappi on disabloitu', function () {
            expect(page.oppijaHaku.canAddNewOppija()).to.equal(false)
          })
        })
        describe('Kun hetu sisältää väärän tarkistusmerkin', function () {
          before(
            page.oppijaHaku.search(
              '011095-953Z',
              page.oppijaHaku.isNoResultsLabelShown
            )
          )
          it('Lisää-nappi on disabloitu', function () {
            expect(page.oppijaHaku.canAddNewOppija()).to.equal(false)
          })
        })
        describe('Kun hetu sisältää väärän päivämäärän, mutta on muuten validi', function () {
          before(
            page.oppijaHaku.search(
              '300275-5557',
              page.oppijaHaku.isNoResultsLabelShown
            )
          )
          it('Lisää-nappi on disabloitu', function () {
            expect(page.oppijaHaku.canAddNewOppija()).to.equal(false)
          })
        })
      })
    })

    describe('Virhetilanteet', function () {
      describe('Kun sessio on vanhentunut', function () {
        before(
          resetFixtures,
          openPage('/koski/uusioppija#hetu=230872-7258', function () {
            return addOppija.isVisible()
          }),
          addOppija.enterValidDataAmmatillinen(),
          Authentication().logout,
          addOppija.submit
        )

        it('Siirrytään etusivulle', wait.until(LandingPage().isVisible))
      })

      describe('Kun tallennus epäonnistuu', function () {
        before(
          Authentication().login(),
          openPage('/koski/uusioppija#hetu=230872-7258', function () {
            return addOppija.isVisible()
          }),
          addOppija.enterValidDataAmmatillinen({ sukunimi: 'error' }),
          addOppija.submit
        )

        it('Näytetään virheilmoitus', wait.until(page.isErrorShown))
      })
    })

    describe('Näyttötutkintoon valmistava koulutus', function () {
      describe('Uutena opiskeluoikeutena', function () {
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataAmmatillinen({
            suoritustyyppi: 'Näyttötutkintoon valmistava koulutus',
            suorituskieli: 'ruotsi',
            tutkinto: 'Autoalan työnjoh',
            suoritustapa: ''
          }),
          addOppija.submitAndExpectSuccess(
            'Tyhjä, Tero (230872-7258)',
            'Näyttötutkintoon valmistava koulutus'
          )
        )

        describe('Lisäyksen jälkeen', function () {
          it('Lisätty opiskeluoikeus näytetään', function () {
            expect(opinnot.getTutkinto()).to.equal(
              'Näyttötutkintoon valmistava koulutus'
            )
            expect(opinnot.getOppilaitos()).to.equal(
              'Stadin ammatti- ja aikuisopisto'
            )
            expect(opinnot.getSuorituskieli()).to.equal('ruotsi')
          })
        })

        describe('Ammatillisen tutkinnon lisääminen samaan opiskeluoikeuteen', function () {
          var lisääSuoritus = opinnot.lisääSuoritusDialog
          before(
            editor.edit,
            lisääSuoritus.open('lisää ammatillisen tutkinnon suoritus')
          )
          describe('Ennen lisäystä', function () {
            it('Esitäyttää tutkinnon näyttötutkintoon valmistavasta koulutuksesta', function () {
              expect(lisääSuoritus.tutkinto()).to.equal(
                'Autoalan työnjohdon erikoisammattitutkinto 40/011/2001'
              )
            })
          })
          describe('Lisäyksen jälkeen', function () {
            before(
              lisääSuoritus.selectTutkinto(
                'Autoalan työnjohdon erikoisammattitutkinto'
              ),
              lisääSuoritus.lisääSuoritus,
              editor.saveChanges
            )
            it('Tutkinnon suoritus ja suoritustapa näytetään', function () {
              expect(opinnot.getTutkinto()).to.equal(
                'Autoalan työnjohdon erikoisammattitutkinto'
              )
              expect(opinnot.getSuoritustapa()).to.equal('Näyttötutkinto')
            })
          })
        })
      })
      describe('Lisääminen olemassa olevaan opiskeluoikeuteen, jossa ammatillisen tutkinnon suoritus', function () {
        var lisääSuoritus = opinnot.lisääSuoritusDialog
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataAmmatillinen({
            suorituskieli: 'ruotsi',
            suoritustapa: 'Näyttötutkinto',
            tutkinto: 'Autoalan työnjohd'
          }),
          addOppija.submitAndExpectSuccess(
            'Tyhjä, Tero (230872-7258)',
            'Autoalan työnjohdon erikoisammattitutkinto'
          ),
          editor.edit,
          lisääSuoritus.open(
            'lisää näyttötutkintoon valmistavan koulutuksen suoritus'
          )
        )
        describe('Ennen lisäystä', function () {
          it('Lisäyspainike on näkyvissä', function () {
            expect(
              lisääSuoritus.isLinkVisible(
                'lisää näyttötutkintoon valmistavan koulutuksen suoritus'
              )
            ).to.equal(true)
          })

          it('Esitäyttää oppilaitoksen', function () {
            expect(lisääSuoritus.toimipiste.oppilaitos()).to.equal(
              'Stadin ammatti- ja aikuisopisto'
            )
          })

          it('Esitäyttää tutkinnon tutkintokoulutuksen suorituksesta', function () {
            expect(lisääSuoritus.tutkinto()).to.equal(
              'Autoalan työnjohdon erikoisammattitutkinto 40/011/2001'
            )
          })
        })
        describe('Lisäyksen jälkeen', function () {
          before(lisääSuoritus.lisääSuoritus, editor.saveChanges)
          it('Näyttötutkintoon valmistavan koulutuksen suoritus näytetään', function () {
            expect(opinnot.getTutkinto()).to.equal(
              'Näyttötutkintoon valmistava koulutus'
            )
          })
        })
      })

      describe('Lisääminen olemassa olevaan opiskeluoikeuteen, jossa VALMA-suoritus', function () {
        var lisääSuoritus = opinnot.lisääSuoritusDialog

        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataAmmatillinen({
            suoritustyyppi:
              'Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)',
            suoritustapa: '',
            tutkinto: ''
          }),
          addOppija.submitAndExpectSuccess(
            'Tyhjä, Tero (230872-7258)',
            'Ammatilliseen koulutukseen valmentava koulutus (VALMA)'
          ),
          editor.edit
        )

        describe('Lisäyspainike', function () {
          it('Ei ole näkyvissä', function () {
            expect(
              lisääSuoritus.isLinkVisible(
                'lisää näyttötutkintoon valmistavan koulutuksen suoritus'
              )
            ).to.equal(false)
          })
        })
      })
    })

    describe('Ammatillinen perustutkinto, suoritustapa reformi', function () {
      describe('Tiedot', function () {
        before(
          insertExample('ammatillinen - reformin mukainen perustutkinto.json'),
          page.openPage,
          page.oppijaHaku.searchAndSelect('020882-577H'),
          wait.untilVisible('.expand-all'),
          click(findFirst('.expand-all'))
        )

        it('näytetään', function () {
          expect(
            extractAsText(
              findSingle(
                '.ammatillisentutkinnonsuoritus:first > .osasuoritukset'
              )
            )
          ).to.equalIgnoreNewlines(
            'Sulje kaikki\n' +
            'Ammatilliset tutkinnon osat Laajuus (osp) Arvosana\n' +
            'Huolto- ja korjaustyöt 5\n' +
            'Pakollinen kyllä\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
            'Näyttö\n' +
            'Kuvaus Vuosihuoltojen suorittaminen\n' +
            'Suorituspaikka Volkswagen Center\n' +
            'Suoritusaika 2.2.2018 — 2.2.2018\n' +
            'Arvosana 5\n' +
            'Arviointipäivä 20.10.2014\n' +
            'Arvioijat Jaana Arstila ( näyttötutkintomestari ) Pekka Saurmann ( näyttötutkintomestari ) Juhani Mykkänen\n' +
            'Arviointikohteet Arviointikohde Arvosana\n' +
            'Työprosessin hallinta 5\n' +
            'Työmenetelmien, -välineiden ja materiaalin hallinta 5\n' +
            'Työn perustana olevan tiedon hallinta Hyväksytty\n' +
            'Elinikäisen oppimisen avaintaidot 5\n' +
            'Arvioinnista päättäneet Muu koulutuksen järjestäjän edustaja\n' +
            'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
            'Arviointi Arvosana 5\n' +
            'Arviointipäivä 20.10.2014\n' +
            'Maalauksen esikäsittelytyöt 5\n' +
            'Pakollinen ei\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
            'Näyttö\n' +
            'Kuvaus Pieniä pohja- ja hiomamaalauksia\n' +
            'Suorituspaikka Volkswagen Center\n' +
            'Suoritusaika 2.2.2018 — 2.2.2018\n' +
            'Arvosana 5\n' +
            'Arviointipäivä 20.10.2014\n' +
            'Arvioijat Jaana Arstila ( näyttötutkintomestari ) Pekka Saurmann ( näyttötutkintomestari ) Juhani Mykkänen\n' +
            'Arviointikohteet Arviointikohde Arvosana\n' +
            'Työprosessin hallinta 5\n' +
            'Työmenetelmien, -välineiden ja materiaalin hallinta 5\n' +
            'Työn perustana olevan tiedon hallinta Hyväksytty\n' +
            'Elinikäisen oppimisen avaintaidot 5\n' +
            'Arvioinnista päättäneet Muu koulutuksen järjestäjän edustaja\n' +
            'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
            'Arviointi Arvosana 5\n' +
            'Arviointipäivä 20.10.2014\n' +
            'Korkeakouluopinnot\n' +
            'Avaa kaikki\n' +
            'Osasuoritus Laajuus (osp) Arvosana\n' +
            'Saksa 5 5\n' +
            'Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja\n' +
            'Avaa kaikki\n' +
            'Osasuoritus Laajuus (osp) Arvosana\n' +
            'Maantieto 5\n' +
            'Englanti 3 osp 5\n' +
            'Tieto- ja viestintätekniikka sekä sen hyödyntäminen 3 osp 5\n' +
            'Hoitotarpeen määrittäminen 5\n' +
            'Yhteensä 11 / 145 osp\n' +
            'Yhteiset tutkinnon osat Laajuus (osp) Arvosana\n' +
            'Viestintä- ja vuorovaikutusosaaminen 8 osp\n' +
            'Pakollinen kyllä\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Avaa kaikki\n' +
            'Osa-alue Laajuus (osp) Arvosana\n' +
            'Viestintä ja vuorovaikutus äidinkielellä, suomi, suomi 4 5\n' +
            'Yhteensä 0 / 35 osp'
          )
        })
      })

      describe('Tietojen muokkaaminen', function () {
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterHenkilötiedot({
            etunimet: 'Tero',
            kutsumanimi: 'Tero',
            sukunimi: 'Tyhjä'
          }),
          addOppija.selectOppilaitos('Stadin'),
          addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
          addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
          addOppija.selectTutkinto(
            'Autoalan perustutkinto, Koulutusvientikokeilu'
          ),
          addOppija.selectAloituspäivä('1.1.2018'),
          addOppija.selectOpintojenRahoitus(
            'Valtionosuusrahoitteinen koulutus'
          ),
          addOppija.selectMaksuttomuus(0),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)'),
          editor.edit
        )

        it('Lisätty opiskeluoikeus näytetään', function () {
          expect(
            textsOf(toArray(S('.group-header > tr > td.suoritus')))
          ).to.deep.equal([
            'Ammatilliset tutkinnon osat',
            'Yhteiset tutkinnon osat'
          ])
        })

        describe('Tutkinnon osan lisääminen', function () {
          before(
            editor.edit,
            opinnot
              .tutkinnonOsat('1')
              .lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
            opinnot
              .tutkinnonOsat('1')
              .tutkinnonOsa(0)
              .propertyBySelector('.arvosana')
              .setValue('3', 1),
            editor.saveChanges,
            wait.forAjax
          )

          it('näyttää oikeat tiedot', function () {
            expect(opinnot.tutkinnonOsat().tutkinnonOsa(0).nimi()).to.equal(
              'Huolto- ja korjaustyöt'
            )
          })

          describe('Korkeakouluopinnot', function () {
            before(editor.edit)

            describe('Lisääminen', function () {
              before(opinnot.tutkinnonOsat(1).lisääKorkeakouluopintoja)

              it('Toimii ja arviointia ei vaadita', function () {
                expect(
                  opinnot.tutkinnonOsat(1).tutkinnonOsa(1).nimi()
                ).to.equal('Korkeakouluopinnot')
                expect(
                  opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()
                ).to.equal(true)
              })

              describe('korkeakoulukokonaisuudet', function () {
                var korkeakouluopinnot = opinnot
                  .tutkinnonOsat(1)
                  .tutkinnonOsa(1)
                  .osanOsat()

                before(
                  korkeakouluopinnot.lisääPaikallinenTutkinnonOsa(
                    'Johdatus akateemisiin opintoihin'
                  ),
                  editor.saveChangesAndWaitForSuccess,
                  opinnot.avaaKaikki
                )

                it('Toimii ja arviointi vaaditaan', function () {
                  expect(korkeakouluopinnot.tutkinnonOsa(0).nimi()).to.equal(
                    'Johdatus akateemisiin opintoihin'
                  )
                  expect(
                    opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()
                  ).to.equal(false)
                })

                after(
                  editor.edit,
                  opinnot.tutkinnonOsat(1).tutkinnonOsa(1).poistaTutkinnonOsa,
                  editor.saveChangesAndWaitForSuccess
                )
              })
            })
          })

          describe('Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja', function () {
            before(editor.edit)

            describe('Lisääminen', function () {
              var jatkoOpintovalmiuksiaTukevatOpinnot = opinnot
                .tutkinnonOsat(1)
                .tutkinnonOsa(1)
                .osanOsat()
              before(
                opinnot.tutkinnonOsat(1)
                  .lisääYhteistenTutkinnonOsienOsaAlueitaLukioOpintojaTaiMuitaJatkoOpintovalmiuksiaTukeviaOpintoja
              )

              it('Toimii ja arviointia ei vaadita', function () {
                expect(
                  opinnot.tutkinnonOsat(1).tutkinnonOsa(1).nimi()
                ).to.equal(
                  'Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja'
                )
                expect(
                  opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()
                ).to.equal(true)
              })

              it('Hakee tutkinnon osan osa-alueet e-perusteista', function () {
                expect(
                  jatkoOpintovalmiuksiaTukevatOpinnot.tutkinnonosavaihtoehdot()
                ).to.deep.equal([
                  'ETK Etiikka',
                  'FK Fysiikka ja kemia',
                  'MLFK Fysikaaliset ja kemialliset ilmiöt ja niiden soveltaminen',
                  'YTKK Kestävän kehityksen edistäminen',
                  'VVAI17 Kommunikation och interaktion på modersmålet, svenska som andraspråk',
                  'KU Kulttuurien tuntemus',
                  'MAVA Matemaattis-luonnontieteellinen osaaminen',
                  'MA Matematiikka',
                  'MLMA Matematiikka ja matematiikan soveltaminen',
                  'YTOU Opiskelu- ja urasuunnitteluvalmiudet',
                  'PS Psykologia',
                  'TAK Taide ja kulttuuri',
                  'VVTL Taide ja luova ilmaisu',
                  'TVT Tieto- ja viestintätekniikka sekä sen hyödyntäminen',
                  'VVTD Toiminta digitaalisessa ympäristössä',
                  'TK1 Toinen kotimainen kieli, ruotsi',
                  'TK2 Toinen kotimainen kieli, suomi',
                  'YTTT Työelämässä toimiminen',
                  'TET Työelämätaidot',
                  'YTTH Työkyvyn ja hyvinvoinnin ylläpitäminen',
                  'TYT Työkyvyn ylläpitäminen, liikunta ja terveystieto',
                  'VK Vieraat kielet',
                  'VVTK Viestintä ja vuorovaikutus toisella kotimaisella kielellä',
                  'VVVK Viestintä ja vuorovaikutus vieraalla kielellä',
                  'VVAI22 Viestintä ja vuorovaikutus äidinkielellä',
                  'VVAI16 Viestintä ja vuorovaikutus äidinkielellä, opiskelijan äidinkieli',
                  'VVAI4 Viestintä ja vuorovaikutus äidinkielellä, romani',
                  'VVAI8 Viestintä ja vuorovaikutus äidinkielellä, ruotsi toisena kielenä',
                  'VVAI3 Viestintä ja vuorovaikutus äidinkielellä, saame',
                  'VVAI Viestintä ja vuorovaikutus äidinkielellä, suomi',
                  'VVAI7 Viestintä ja vuorovaikutus äidinkielellä, suomi toisena kielenä',
                  'VVAI11 Viestintä ja vuorovaikutus äidinkielellä, suomi viittomakielisille',
                  'VVAI15 Viestintä ja vuorovaikutus äidinkielellä, viittomakieli',
                  '003 Viestintä- ja vuorovaikutusosaaminen',
                  '001 Yhteiskunnassa ja kansalaisena toimiminen',
                  'YTYK Yhteiskunnassa ja kansalaisena toimiminen',
                  'YKT Yhteiskuntataidot',
                  'YM Ympäristöosaaminen',
                  'YTYY Yrittäjyys ja yrittäjämäinen toiminta',
                  'YYT Yrittäjyys ja yritystoiminta',
                  'AI Äidinkieli'
                ])
              })

              describe('Tutkinnon osan osa-alueen lisääminen', function () {
                before(
                  jatkoOpintovalmiuksiaTukevatOpinnot.lisääTutkinnonOsa(
                    'Työelämätaidot'
                  ),
                  editor.saveChangesAndWaitForSuccess,
                  opinnot.avaaKaikki
                )

                it('Toimii', function () {
                  expect(
                    jatkoOpintovalmiuksiaTukevatOpinnot.tutkinnonOsa(0).nimi()
                  ).to.equal('Työelämätaidot')
                  expect(
                    opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()
                  ).to.equal(false)
                })

                describe('Lukio-opinnon lisääminen', function () {
                  before(
                    editor.edit,
                    opinnot.avaaKaikki,
                    jatkoOpintovalmiuksiaTukevatOpinnot.lisääLukioOpinto(
                      'MAA4 Vektorit'
                    ),
                    editor.saveChangesAndWaitForSuccess,
                    opinnot.avaaKaikki
                  )

                  it('Toimii', function () {
                    expect(
                      jatkoOpintovalmiuksiaTukevatOpinnot.tutkinnonOsa(1).nimi()
                    ).to.equal('MAA4 Vektorit')
                  })

                  describe('Muun opintovalmiuksia tukeva opinnon lisääminen', function () {
                    before(
                      editor.edit,
                      opinnot.avaaKaikki,
                      jatkoOpintovalmiuksiaTukevatOpinnot.lisääMuuOpintovalmiuksiaTukevaOpinto(
                        'Tutortoiminta'
                      ),
                      editor.saveChangesAndWaitForSuccess,
                      opinnot.avaaKaikki
                    )

                    it('Toimii', function () {
                      expect(
                        jatkoOpintovalmiuksiaTukevatOpinnot
                          .tutkinnonOsa(2)
                          .nimi()
                      ).to.equal('Tutortoiminta')
                    })

                    describe('Kun arvioinnit lisätty', function () {
                      before(
                        editor.edit,
                        opinnot.avaaKaikki,
                        jatkoOpintovalmiuksiaTukevatOpinnot
                          .tutkinnonOsa(0)
                          .propertyBySelector('.arvosana')
                          .setValue('3', 1),
                        jatkoOpintovalmiuksiaTukevatOpinnot
                          .tutkinnonOsa(1)
                          .propertyBySelector('.arvosana')
                          .setValue('3', 1),
                        jatkoOpintovalmiuksiaTukevatOpinnot
                          .tutkinnonOsa(2)
                          .propertyBySelector('.arvosana')
                          .setValue('3', 1)
                      )

                      it('Merkitseminen valmiiksi on mahdollita', function () {
                        expect(
                          opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()
                        ).to.equal(true)
                      })

                      after(editor.cancelChanges)
                    })
                  })
                })
              })
            })
          })
        })

        describe('Yhteisen tutkinnon osan lisääminen', function () {
          before(editor.edit)

          describe('Ennen lisäystä', function () {
            it('Näyttää e-perusteiden mukaisen vaihtoehtolistan', function () {
              expect(
                opinnot.tutkinnonOsat('2').tutkinnonosavaihtoehdot()
              ).to.deep.equal([
                '400013 Matemaattis-luonnontieteellinen osaaminen',
                '400012 Viestintä- ja vuorovaikutusosaaminen',
                '400014 Yhteiskunta- ja työelämäosaaminen'
              ])
            })
          })

          describe('Lisäyksen jälkeen', function () {
            before(
              opinnot
                .tutkinnonOsat('2')
                .lisääTutkinnonOsa('Matemaattis-luonnontieteellinen osaaminen')
            )
            it('lisätty osa näytetään', function () {
              expect(
                opinnot.tutkinnonOsat('2').tutkinnonOsa(0).nimi()
              ).to.equal('Matemaattis-luonnontieteellinen osaaminen')
            })
          })
        })

        describe('Keskiarvo', function () {
          describe('Aluksi', function () {
            before(editor.edit)
            it('keskiarvo- ja keskiarvoSisältääMukautettujaArvosanoja -kentät on näkyvissä', function () {
              expect(editor.property('keskiarvo').isVisible()).to.equal(true)
              expect(
                editor
                  .property('keskiarvoSisältääMukautettujaArvosanoja')
                  .isVisible()
              ).to.equal(true)
            })
            after(editor.cancelChanges)
          })
          describe('Ei-validin keskiarvon lisäys', function () {
            before(editor.edit, editor.property('keskiarvo').setValue(7))
            it('ei ole sallittu', function () {
              expect(editor.canSave()).to.equal(false)
            })
            after(editor.cancelChanges)
          })
          describe('Validin keskiarvon lisäys', function () {
            before(
              editor.edit,
              editor.property('keskiarvo').setValue(3.5),
              editor
                .property('keskiarvoSisältääMukautettujaArvosanoja')
                .setValue(false),
              opinnot
                .tutkinnonOsat(2)
                .lisääTutkinnonOsa('Matemaattis-luonnontieteellinen osaaminen'),
              opinnot
                .tutkinnonOsat(2)
                .tutkinnonOsa(0)
                .property('laajuus')
                .setValue('35'),
              opinnot
                .tutkinnonOsat(2)
                .tutkinnonOsa(0)
                .propertyBySelector('.arvosana')
                .setValue('3', 1),
              opinnot
                .tutkinnonOsat(999999)
                .lisääTutkinnonOsa('Ympäristöosaaminen'),
              opinnot
                .tutkinnonOsat(999999)
                .tutkinnonOsa(0)
                .property('laajuus')
                .setValue('35'),
              opinnot
                .tutkinnonOsat(999999)
                .tutkinnonOsa(0)
                .propertyBySelector('.arvosana')
                .setValue('3', 1),
              opinnot.tutkinnonOsat('1').tutkinnonOsa(1).poistaTutkinnonOsa,
              opinnot.tutkinnonOsat('1').tutkinnonOsa(1).poistaTutkinnonOsa,
              opinnot.tilaJaVahvistus.merkitseValmiiksi,
              opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                .itemEditor(0)
                .setValue('Lisää henkilö'),
              opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                .itemEditor(0)
                .propertyBySelector('.nimi')
                .setValue('Reijo Reksi'),
              opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                .itemEditor(0)
                .propertyBySelector('.titteli')
                .setValue('Rehtori'),
              opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
              editor.saveChanges
            )
            it('toimii', function () {
              expect(page.isSavedLabelShown()).to.equal(true)
            })
            it('keskiarvo näytetään kahden desimaalin tarkkuudella', function () {
              expect(editor.property('keskiarvo').getValue()).to.equal('3,50')
              expect(
                !editor
                  .property('keskiarvoSisältääMukautettujaArvosanoja')
                  .isVisible()
              )
            })
          })
        })

        describe('Organisaatiovalitsin', function () {
          before(
            editor.edit,
            editor
              .property('toimipiste')
              .organisaatioValitsin()
              .select('Aalto-yliopisto'),
            editor
              .property('toimipiste')
              .organisaatioValitsin()
              .select('Helsingin yliopisto')
          )
          it('Organisaation voi vaihtaa monta kertaa', function () {
            expect(editor.property('toimipiste').getValue()).to.equal(
              'Helsingin yliopisto'
            )
          })
        })
      })
    })

    describe('Ammatillisen tutkinnon osittainen suoritus', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterHenkilötiedot({
          etunimet: 'Tero',
          kutsumanimi: 'Tero',
          sukunimi: 'Tyhjä'
        }),
        addOppija.selectOppilaitos('Stadin'),
        addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
        addOppija.selectSuoritustyyppi('Ammatillisen tutkinnon osa/osia'),
        addOppija.selectTutkinto('Autoalan perust'),
        addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
        addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
        addOppija.selectMaksuttomuus(0),
        addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)')
      )

      it('Lisätty opiskeluoikeus näytetään', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()[0]
        ).to.match(
          /^Stadin ammatti- ja aikuisopisto, Autoalan perustutkinto, osittainen.*/
        )
        expect(opinnot.getTutkinto()).to.equal('Autoalan perustutkinto')
        expect(opinnot.getOppilaitos()).to.equal(
          'Stadin ammatti- ja aikuisopisto'
        )
      })

      describe('Tutkinnon osan lisääminen', function () {
        before(
          editor.edit,
          opinnot
            .tutkinnonOsat('1')
            .lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
          opinnot
            .tutkinnonOsat('1')
            .tutkinnonOsa(0)
            .propertyBySelector('.arvosana')
            .setValue('3', 1),
          editor.saveChanges,
          wait.forAjax,
          wait.until(isReadyToResolveOpiskeluoikeus)
        )

        it('näyttää oikeat tiedot', function () {
          expect(opinnot.tutkinnonOsat().tutkinnonOsa(0).nimi()).to.equal(
            'Huolto- ja korjaustyöt'
          )
        })
      })

      describe('Keskiarvo', function () {
        describe('Aluksi', function () {
          before(editor.edit)
          it('keskiarvo- ja keskiarvoSisältääMukautettujaArvosanoja -kentät on näkyvissä', function () {
            expect(editor.property('keskiarvo').isVisible()).to.equal(true)
            expect(
              editor
                .property('keskiarvoSisältääMukautettujaArvosanoja')
                .isVisible()
            ).to.equal(true)
          })
          after(editor.cancelChanges)
        })
        describe('Validin keskiarvon lisäys osittaiselle tutkinnolle', function () {
          before(
            editor.edit,
            editor.property('keskiarvo').setValue(3.5),
            editor
              .property('keskiarvoSisältääMukautettujaArvosanoja')
              .setValue(false),
            opinnot.tilaJaVahvistus.merkitseValmiiksi,
            opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
              .itemEditor(0)
              .setValue('Lisää henkilö'),
            opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
              .itemEditor(0)
              .propertyBySelector('.nimi')
              .setValue('Reijo Reksi'),
            opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
              .itemEditor(0)
              .propertyBySelector('.titteli')
              .setValue('Rehtori'),
            opinnot.tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
            editor.saveChanges
          )
          it('toimii', function () {
            expect(page.isSavedLabelShown()).to.equal(true)
          })
          it('keskiarvo näytetään kahden desimaalin tarkkuudella', function () {
            expect(editor.property('keskiarvo').getValue()).to.equal('3,50')
            expect(
              !editor
                .property('keskiarvoSisältääMukautettujaArvosanoja')
                .isVisible()
            )
          })
        })
      })
    })

    describe('TELMA suoritus', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataAmmatillinen({
          suoritustyyppi:
            'Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)',
          suoritustapa: '',
          tutkinto: ''
        }),
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)'
        )
      )

      it('Lisätty opiskeluoikeus näytetään', function () {
        expect(opinnot.getTutkinto()).to.equal(
          'Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)'
        )
        expect(opinnot.getOppilaitos()).to.equal(
          'Stadin ammatti- ja aikuisopisto'
        )
      })

      var suoritustapa = editor.property('suoritustapa')
      describe('Paikallisen tutkinnon osan lisääminen', function () {
        before(
          editor.edit,
          opinnot
            .tutkinnonOsat()
            .lisääPaikallinenTutkinnonOsa('Uimaliikunta ja vesiturvallisuus'),
          editor.saveChanges,
          wait.forAjax
        )

        it('näyttää oikeat tiedot', function () {
          expect(opinnot.tutkinnonOsat().tutkinnonOsa(0).nimi()).to.equal(
            'Uimaliikunta ja vesiturvallisuus'
          )
        })
      })

      describe('Lisäysmahdollisuutta tutkinnon osan lisäämiselle toisesta tutkinnosta', function () {
        before(editor.edit)

        it('ei ole näkyvissä', function () {
          expect(
            opinnot
              .tutkinnonOsat()
              .isLisääTutkinnonOsaToisestaTutkinnostaVisible()
          ).to.equal(false)
        })
      })
    })

    describe('Opintojen rahoitus', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataAmmatillinen({
          opintojenRahoitus: 'Aikuisten osaamisperustan vahvistaminen'
        }),
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'Autoalan perustutkinto'
        )
      )

      it('Lisätty opiskeluoikeus ja opintojen rahoitus näytetään', function () {
        expect(opinnot.getOpiskeluoikeudenTila()).to.match(
          /Läsnä \(aikuisten osaamisperustan vahvistaminen\)$/
        )
      })
    })
  })

  describe('Opiskeluoikeuden tila', function () {
    before(
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('280618-402H'),
      editor.edit,
      editor.property('tila').removeItem(0),
      editor.saveChanges,
      wait.until(page.isSavedLabelShown)
    )

    describe('Ammatillisen koulutuksen tilat', function () {
      before(editor.edit, opinnot.avaaLisaysDialogi)
      it('Sisältää loma-tilan, mutta ei eronnut-tilaa', function () {
        expect(OpiskeluoikeusDialog().tilat()).to.deep.equal([
          'koskiopiskeluoikeudentila_katsotaaneronneeksi',
          'koskiopiskeluoikeudentila_loma',
          'koskiopiskeluoikeudentila_lasna',
          'koskiopiskeluoikeudentila_peruutettu',
          'koskiopiskeluoikeudentila_valmistunut',
          'koskiopiskeluoikeudentila_valiaikaisestikeskeytynyt'
        ])
      })
    })
  })

  describe('Opiskeluoikeuden organisaatiohistoria', function () {
    before(page.openPage, page.oppijaHaku.searchAndSelect('200994-834A'))

    it('Näytetään', function () {
      expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
        'Opiskeluoikeuden voimassaoloaika : 1.9.2016 — 1.5.2020 (arvioitu)\n' +
        'Tila 1.9.2016 Läsnä (valtionosuusrahoitteinen koulutus)\n' +
        'Opiskeluoikeuden organisaatiohistoria Muutospäivä 5.5.2005\n' +
        'Aikaisempi oppilaitos Stadin ammatti- ja aikuisopisto\n' +
        'Aikaisempi koulutustoimija Helsingin kaupunki\n' +
        'Muutospäivä 2.2.2002\n' +
        'Aikaisempi oppilaitos Ressun lukio\n' +
        'Aikaisempi koulutustoimija Helsingin kaupunki'
      )
    })
  })


})
