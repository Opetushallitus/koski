describe('Ammatillinen koulutus', function () {
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
        it('Onnistuu, näyttää henkilöpalvelussa olevat nimitiedot', function () {})
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
          addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen')
        )

        it('Näytetään opintojen rahoitus -kenttä', function () {
          expect(addOppija.rahoitusIsVisible()).to.equal(true)
        })

        it('Vaihtoehtoina on kaikki opintojenRahoitus-vaihtoehdot', function () {
          expect(addOppija.opintojenRahoitukset()).to.deep.equal([
            'Valtionosuusrahoitteinen koulutus',
            'Työvoimakoulutus ely-keskukset ja työ- ja elinkeinotoimistot (kansallinen rahoitus)',
            'Työvoimakoulutus (ESR-rahoitteinen)',
            'Työnantajan kokonaan rahoittama',
            'Muuta kautta rahoitettu',
            'Nuorten aikuisten osaamisohjelma',
            'Aikuisten osaamisperustan vahvistaminen',
            'Maahanmuuttajien ammatillinen koulutus (valtionavustus)',
            'Työvoimakoulutus (OKM rahoitus)',
            'Ammatillisen osaamisen pilotit 2019',
            'Ammatillisen osaamisen pilotit 2019 (työvoimakoulutus)',
            'Työvoimakoulutus (valtiosopimukseen perustuva rahoitus)',
            'Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus',
            'Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus (RRF)'
          ])
        })

        it('Näytetään tilavaihtoehdoissa loma-tila, mutta ei eronnut-tilaa', function () {
          expect(addOppija.opiskeluoikeudenTilat()).to.deep.equal([
            'Katsotaan eronneeksi',
            'Loma',
            'Läsnä',
            'Peruutettu',
            'Valmistunut',
            'Väliaikaisesti keskeytynyt'
          ])
        })

        describe('Koulutusvienti', () => {
          before((done) => {
            addOppija
              .enterTutkinto('auto')()
              .then(wait.until(addOppija.tutkinnotIsVisible))
              .then(done)
          })
          it('Näytetään myös koulutusviennin kautta tuodut tutkinnot', function () {
            expect(addOppija.tutkinnot()).to.equal(
              'Autoalan perustutkinto 39/011/2014 Autoalan työnjohdon erikoisammattitutkinto 40/011/2001 Auto- ja kuljetusalan työnjohdon ammattitutkinto 30/011/2015 Automaatioasentajan ammattitutkinto 3/011/2013 Automaatioyliasentajan erikoisammattitutkinto 9/011/2008 Puutavaran autokuljetuksen ammattitutkinto 27/011/2008 Sähkö- ja automaatiotekniikan perustutkinto 77/011/2014 Autoalan perustutkinto OPH-2762-2017 Automekaanikon erikoisammattitutkinto OPH-1886-2017 Autoalan perustutkinto, Koulutusvientikokeilu OPH-4792-2017'
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
          it('lisätty oppija näytetään', function () {})

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

        it('lisätty oppija näytetään', function () {})

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

        it('lisätty oppija näytetään', function () {})

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
              addOppija.selectTutkinto('auto'),
              addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
              addOppija.selectAloituspäivä('1.1.2018')
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
                'Lahden ammattikorkeakoulu  (lakkautettu)',
                'Stadin ammatti- ja aikuisopisto'
              ])
            })
          })
        })
        describe('Kun oppilaitosta ei olla valittu', function () {
          before(addOppija.enterData({ oppilaitos: undefined }))
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
          it('tutkinto pitää valita uudestaan', function () {
            expect(addOppija.isEnabled()).to.equal(false)
          })
          describe('Tutkinnon valinnan jälkeen', function () {
            before(
              addOppija.selectTutkinto('auto'),
              addOppija.selectSuoritustapa('Ammatillinen perustutkinto')
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
            suorituskieli: 'ruotsi',
            tutkinto: 'Autoalan työnjoh',
            suoritustapa: ''
          }),
          addOppija.selectOppimäärä('Näyttötutkintoon valmistava koulutus'),
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
            suoritustapa: '',
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
          addOppija.enterValidDataAmmatillinen(),
          addOppija.selectOppimäärä(
            'Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)'
          ),
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
              'Työssäoppimisen yhteydessä ei\n' +
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
              'Työssäoppimisen yhteydessä ei\n' +
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
          function () {
            return wait
              .until(Page().getInput('.tutkinto input').isVisible)()
              .then(
                Page().setInputValue(
                  '.tutkinto input',
                  'Autoalan perustutkinto'
                )
              )
              .then(click('.results li:last()'))
          },
          addOppija.selectAloituspäivä('1.1.2018'),
          addOppija.selectOpintojenRahoitus(
            'Valtionosuusrahoitteinen koulutus'
          ),
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
        addOppija.selectOppimäärä('Ammatillisen tutkinnon osa/osia'),
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
          wait.forAjax
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
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectOppimäärä(
          'Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)'
        ),
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
      before(prepareForNewOppija('kalle', '230872-7258'))
      before(
        addOppija.enterValidDataAmmatillinen({
          opintojenRahoitus: 'Aikuisten osaamisperustan vahvistaminen'
        })
      )
      before(
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

  describe('Opiskeluoikeuden mitätöiminen', function () {
    before(resetFixtures, page.openPage)
    describe('Mitätöintilinkki', function () {
      before(page.oppijaHaku.searchAndSelect('010101-123N'), editor.edit)
      it('Näytetään', function () {
        expect(opinnot.invalidateOpiskeluoikeusIsShown()).to.equal(true)
      })

      describe('Painettaessa', function () {
        before(opinnot.invalidateOpiskeluoikeus)
        it('Pyydetään vahvistus', function () {
          expect(opinnot.confirmInvalidateOpiskeluoikeusIsShown()).to.equal(
            true
          )
        })

        describe('Painettaessa uudestaan', function () {
          before(
            opinnot.confirmInvalidateOpiskeluoikeus,
            wait.until(page.oppijataulukko.isReady)
          )
          it('Opiskeluoikeus mitätöidään', function () {
            expect(page.isOpiskeluoikeusInvalidatedMessageShown()).to.equal(
              true
            )
          })

          describe('Mitätöityä opiskeluoikeutta', function () {
            before(
              syncPerustiedot,
              page.oppijataulukko.filterBy('nimi', 'Esimerkki')
            )
            it('Ei näytetä', function () {
              expect(page.oppijataulukko.names()).to.deep.equal([])
            })
          })

          describe('Vahvistusviestin', function () {
            before(
              opinnot.hideInvalidateMessage,
              wait.untilFalse(page.isOpiskeluoikeusInvalidatedMessageShown)
            )
            it('Voi piilottaa', function () {
              expect(page.isOpiskeluoikeusInvalidatedMessageShown()).to.equal(
                false
              )
            })
          })
        })
      })
    })

    describe('Opiskeluoikeudelle jossa ei ole valmiita suorituksia, ja joka on peräisin ulkoisesta järjestelmästä', function () {
      describe('Kun kirjautunut oppilaitoksen tallentajana', function () {
        before(
          Authentication().logout,
          Authentication().login(),
          page.openPage,
          page.oppijaHaku.searchAndSelect('270303-281N')
        )
        it('Ei näytetä mitätöintilinkkiä', function () {
          expect(opinnot.invalidateOpiskeluoikeusIsShown()).to.equal(false)
        })
      })
      describe('Kun kirjautunut oppilaitoksen pääkäyttäjänä', function () {
        before(
          Authentication().logout,
          Authentication().login('stadin-pää'),
          page.openPage,
          page.oppijaHaku.searchAndSelect('270303-281N')
        )
        it('Näytetään mitätöintilinkki', function () {
          expect(opinnot.invalidateOpiskeluoikeusIsShown()).to.equal(true)
        })
      })
    })
  })

  describe('Tietojen muuttaminen', function () {
    before(addOppija.addNewOppija('kalle', '280608-6619'))

    it('Aluksi ei näytetä "Kaikki tiedot tallennettu" -tekstiä', function () {
      expect(page.isSavedLabelShown()).to.equal(false)
    })

    describe('Järjestämismuodot', function () {
      var järjestämismuodot = editor.property('järjestämismuodot')
      before(
        editor.edit,
        järjestämismuodot.addItem,
        järjestämismuodot
          .propertyBySelector('.järjestämismuoto')
          .setValue('Koulutuksen järjestäminen oppisopimuskoulutuksena'),
        järjestämismuodot.property('nimi').setValue('Virheellinen'),
        järjestämismuodot.property('yTunnus').setValue('123')
      )

      it('Aluksi näyttää y-tunnuksen esimerkin', function () {
        expect(
          järjestämismuodot.propertyBySelector('.yTunnus input').elem()[0]
            .placeholder,
          'Esimerkki: 1234567-8'
        )
      })

      describe('Epävalidi y-tunnus', function () {
        before(
          järjestämismuodot.property('nimi').setValue('Virheellinen'),
          järjestämismuodot.property('yTunnus').setValue('123')
        )

        it('Ei anna tallentaa virheellistä y-tunnusta', function () {
          expect(opinnot.onTallennettavissa()).to.equal(false)
        })
      })

      describe('Validi y-tunnus', function () {
        before(
          editor.cancelChanges,
          editor.edit,
          järjestämismuodot.addItem,
          järjestämismuodot.propertyBySelector('.alku').setValue('22.8.2017'),
          järjestämismuodot
            .propertyBySelector('.järjestämismuoto')
            .setValue('Koulutuksen järjestäminen oppisopimuskoulutuksena'),
          järjestämismuodot.property('nimi').setValue('Autohuolto oy'),
          järjestämismuodot.property('yTunnus').setValue('1629284-5'),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )

        it('Toimii', function () {
          expect(page.isSavedLabelShown()).to.equal(true)
          expect(extractAsText(S('.järjestämismuodot'))).to.equal(
            'Järjestämismuodot 22.8.2017 — , Koulutuksen järjestäminen oppisopimuskoulutuksena\n' +
              'Yritys Autohuolto oy Y-tunnus 1629284-5'
          )
        })
      })
    })

    describe('Opiskeluoikeuden ostettu tieto', function () {
      describe('Aluksi', function () {
        it('ei näytetä', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).not.to.contain(
            'Ostettu'
          )
        })

        describe('Muuttaminen', function () {
          before(
            editor.edit,
            editor.property('ostettu').setValue(true),
            editor.saveChangesAndWaitForSuccess
          )

          it('toimii', function () {
            expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.contain(
              'Ostettu kyllä'
            )
          })
        })
      })
    })

    describe('Opiskeluoikeuden lisätiedot', function () {
      before(
        editor.edit,
        opinnot.expandAll,
        editor.property('hojks').addValue,
        editor
          .property('hojks')
          .property('opetusryhmä')
          .setValue('Erityisopetusryhmä'),
        editor.property('oikeusMaksuttomaanAsuntolapaikkaan').setValue(true),
        editor.property('ulkomaanjaksot').addItem,
        editor
          .property('ulkomaanjaksot')
          .propertyBySelector('.alku')
          .setValue('22.6.2017'),
        editor.property('ulkomaanjaksot').property('maa').setValue('Algeria'),
        editor
          .property('ulkomaanjaksot')
          .property('kuvaus')
          .setValue('Testing'),
        editor.property('majoitus').addItem,
        editor
          .property('majoitus')
          .propertyBySelector('.alku')
          .setValue('22.6.2017'),
        editor
          .property('majoitus')
          .propertyBySelector('.loppu')
          .setValue('1.1.2099'),
        editor.property('osaAikaisuusjaksot').addItem,
        editor
          .property('osaAikaisuusjaksot')
          .propertyBySelector('.alku')
          .setValue('22.6.2017'),
        editor
          .property('osaAikaisuusjaksot')
          .property('osaAikaisuus')
          .setValue('80'),
        editor.property('opiskeluvalmiuksiaTukevatOpinnot').addItem,
        editor
          .property('opiskeluvalmiuksiaTukevatOpinnot')
          .propertyBySelector('.alku')
          .setValue('22.6.2017'),
        editor
          .property('opiskeluvalmiuksiaTukevatOpinnot')
          .propertyBySelector('.loppu')
          .setValue('28.6.2017'),
        editor
          .property('opiskeluvalmiuksiaTukevatOpinnot')
          .property('kuvaus')
          .setValue('Testing'),
        editor.saveChanges,
        wait.until(page.isSavedLabelShown)
      )

      it('Toimii', function () {
        expect(extractAsText(S('.lisätiedot'))).to.equal(
          'Lisätiedot\n' +
            'Majoitus 22.6.2017 — 1.1.2099\n' +
            'Ulkomaanjaksot 22.6.2017 — Maa Algeria Kuvaus Testing\n' +
            'Hojks Opetusryhmä Erityisopetusryhmä\n' +
            'Osa-aikaisuusjaksot 22.6.2017 — Osa-aikaisuus 80 %\n' +
            'Opiskeluvalmiuksia tukevat opinnot 22.6.2017 — 28.6.2017 Kuvaus Testing'
        )
      })
    })

    describe('Osaamisala', function () {
      describe('Osaamisalalista haetaan', function () {
        before(editor.edit)

        it('eperusteista', function () {
          expect(textsOf(toArray(S('.osaamisala .options li')))).to.deep.equal([
            'Ei valintaa',
            'Autokorinkorjauksen osaamisala (1525)',
            'Automaalauksen osaamisala (1526)',
            'Automyynnin osaamisala (1527)',
            'Autotekniikan osaamisala (1528)',
            'Moottorikäyttöisten pienkoneiden korjauksen osaamisala (1622)',
            'Varaosamyynnin osaamisala (1529)'
          ])
        })

        describe('Kun perustetta ei löydy eperusteista', function () {
          before(
            page.openPage,
            page.oppijaHaku.searchAndSelect('201137-361Y'),
            editor.edit
          )

          it('haetaan kaikki osaamisalat', function () {
            var osaamisalat = textsOf(toArray(S('.osaamisala .options li')))

            expect(osaamisalat.slice(0, 5)).to.deep.equal([
              'Ei valintaa',
              'Agroautomaation hyödyntämisen osaamisala (3169)',
              'Agrologistiikan osaamisala (2413)',
              'Aikuisliikunnan osaamisala (2065)',
              'Aikuisten perusopetus (0009)'
            ])

            expect(osaamisalat.slice(-5)).to.deep.equal([
              'Yritystoiminnan suunnittelun ja käynnistämisen osaamisala (2284)',
              'Äänitekniikan osaamisala (2240)',
              'Ääniteknikko (2128)',
              'Äänitetuottaja (2127)',
              'Äänityön osaamisala (2007)'
            ])
          })
        })
      })

      describe('Tallennus ilman päivämääriä', function () {
        before(
          editor.edit,
          editor
            .property('osaamisala')
            .itemEditor(0)
            .property('osaamisala')
            .setValue('Automyynnin osaamisala'),
          editor.saveChanges
        )
        it('toimii', function () {
          expect(editor.property('osaamisala').getText()).to.equal(
            'Osaamisala Automyynnin osaamisala'
          )
        })
      })

      describe('Päivämäärien lisäys', function () {
        before(
          editor.edit,
          editor
            .property('osaamisala')
            .itemEditor(0)
            .property('alku')
            .setValue('1.1.2017'),
          editor.saveChanges
        )

        it('toimii', function () {
          expect(editor.property('osaamisala').getText()).to.equal(
            'Osaamisala Automyynnin osaamisala 1.1.2017 —'
          )
        })
      })

      after(page.openPage, page.oppijaHaku.searchAndSelect('280608-6619'))
    })

    describe('Tutkinnon osat', function () {
      var suoritustapa = editor.property('suoritustapa')
      describe('Kun suoritustapa on opetussuunnitelman mukainen', function () {
        describe('Tutkinnon osan lisääminen', function () {
          before(editor.edit)
          describe('Aluksi', function () {
            it('Taulukko on tyhjä', function () {
              expect(opinnot.tutkinnonOsat('1').tyhjä()).to.equal(true)
            })
            it('Näytetään laajuussarake ja -yksikkö muokattaessa', function () {
              expect(opinnot.tutkinnonOsat().laajuudenOtsikko()).to.equal(
                'Laajuus (osp)'
              )
            })
          })
          describe('Pakollisen tutkinnon osan lisääminen', function () {
            describe('Ennen lisäystä', function () {
              it('Näyttää e-perusteiden mukaisen vaihtoehtolistan', function () {
                expect(
                  opinnot.tutkinnonOsat('1').tutkinnonosavaihtoehdot().length
                ).to.equal(47)
              })

              it('Näytetään pakollisten tutkinnon osien otsikkorivi', function () {
                expect(
                  opinnot.tutkinnonOsat('1').isGroupHeaderVisible()
                ).to.equal(true)
              })
            })

            describe('Lisäyksen jälkeen', function () {
              before(
                opinnot
                  .tutkinnonOsat('1')
                  .lisääTutkinnonOsa('Huolto- ja korjaustyöt')
              )
              it('lisätty osa näytetään', function () {
                expect(
                  opinnot.tutkinnonOsat('1').tutkinnonOsa(0).nimi()
                ).to.equal('Huolto- ja korjaustyöt')
              })
              describe('Arvosanan lisääminen', function () {
                before(
                  opinnot
                    .tutkinnonOsat('1')
                    .tutkinnonOsa(0)
                    .propertyBySelector('.arvosana')
                    .setValue('3', 1)
                )

                describe('Lisättäessä', function () {
                  it('Merkitsee tutkinnon osan tilaan VALMIS', function () {
                    expect(
                      opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()
                    ).to.equal(true)
                  })
                })

                describe('Tallentamisen jälkeen', function () {
                  before(editor.saveChanges, wait.forAjax)

                  describe('Käyttöliittymän tila', function () {
                    it('näyttää edelleen oikeat tiedot', function () {
                      expect(
                        opinnot.tutkinnonOsat().tutkinnonOsa(0).nimi()
                      ).to.equal('Huolto- ja korjaustyöt')
                    })
                  })

                  describe('Arvosanan poistaminen', function () {
                    before(
                      editor.edit,
                      opinnot
                        .tutkinnonOsat('1')
                        .tutkinnonOsa(0)
                        .propertyBySelector('.arvosana')
                        .setValue('Ei valintaa'),
                      editor.saveChanges
                    )
                    it('Tallennus onnistuu ja suoritus siirtyy tilaan KESKEN', function () {
                      expect(
                        opinnot.tutkinnonOsat().tutkinnonOsa(0).tila()
                      ).to.equal('Suoritus kesken')
                    })
                  })

                  describe('Laajuus', function () {
                    describe('Kun siirrytään muokkaamaan tietoja', function () {
                      before(editor.edit)
                      it('Laajuussarake ja laajuuden yksikkö näytetään', function () {
                        expect(
                          opinnot.tutkinnonOsat().laajuudenOtsikko()
                        ).to.equal('Laajuus (osp)')
                      })

                      describe('Kun syötetään laajuus ja tallennetaan', function () {
                        before(
                          opinnot
                            .tutkinnonOsat(1)
                            .tutkinnonOsa(0)
                            .property('laajuus')
                            .setValue('30'),
                          opinnot
                            .tutkinnonOsat('1')
                            .tutkinnonOsa(0)
                            .propertyBySelector('.arvosana')
                            .setValue('3', 1),
                          editor.saveChanges
                        )
                        it('Näytetään laajuus', function () {
                          expect(
                            opinnot.tutkinnonOsat().laajuudenOtsikko()
                          ).to.equal('Laajuus (osp)')
                          expect(
                            opinnot.tutkinnonOsat().laajuudetYhteensä()
                          ).to.equal('30')
                        })

                        describe('Kun poistetaan arvosana ja tallennetaan', function () {
                          before(
                            editor.edit,
                            opinnot
                              .tutkinnonOsat(1)
                              .tutkinnonOsa(0)
                              .propertyBySelector('.arvosana')
                              .setValue('Ei valintaa'),
                            editor.saveChanges
                          )

                          it('Laajuutta ei lasketa arvioimattomista', function () {
                            expect(
                              opinnot.tutkinnonOsat().laajuudetYhteensä()
                            ).to.equal('0')
                          })
                        })

                        describe('Kun poistetaan laajuus ja tallennetaan', function () {
                          before(
                            editor.edit,
                            opinnot
                              .tutkinnonOsat(1)
                              .tutkinnonOsa(0)
                              .property('laajuus')
                              .setValue(''),
                            editor.saveChanges
                          )

                          it('Laajuussarake piilotetaan', function () {
                            expect(
                              opinnot.tutkinnonOsat().laajuudenOtsikko()
                            ).to.equal('')
                          })
                        })
                      })
                    })
                  })

                  describe('Tutkinnon osan poistaminen', function () {
                    before(
                      editor.edit,
                      opinnot.tutkinnonOsat('1').tutkinnonOsa(0)
                        .poistaTutkinnonOsa,
                      editor.saveChanges
                    )
                    it('toimii', function () {
                      expect(opinnot.tutkinnonOsat().tyhjä()).to.equal(true)
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
                  '101054 Matemaattis-luonnontieteellinen osaaminen',
                  '101056 Sosiaalinen ja kulttuurinen osaaminen',
                  '101053 Viestintä- ja vuorovaikutusosaaminen',
                  '101055 Yhteiskunnassa ja työelämässä tarvittava osaaminen'
                ])
              })
            })

            describe('Lisäyksen jälkeen', function () {
              before(
                opinnot
                  .tutkinnonOsat('2')
                  .lisääTutkinnonOsa(
                    'Matemaattis-luonnontieteellinen osaaminen'
                  )
              )
              it('lisätty osa näytetään', function () {
                expect(
                  opinnot.tutkinnonOsat('2').tutkinnonOsa(0).nimi()
                ).to.equal('Matemaattis-luonnontieteellinen osaaminen')
              })

              describe('Tutkinnon osan osa-alueen lisääminen', function () {
                describe('Ennen lisäystä', function () {
                  it('Näyttää e-perusteiden mukaisen vaihtoehtolistan', function () {
                    expect(
                      opinnot
                        .tutkinnonOsat('2')
                        .tutkinnonOsa(0)
                        .osanOsat()
                        .tutkinnonosavaihtoehdot()
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
                })

                describe('Lisäyksen jälkeen', function () {
                  var tutkinnonOsienOsat = opinnot.tutkinnonOsat('999999')
                  before(
                    tutkinnonOsienOsat.lisääTutkinnonOsa('MA Matematiikka'),
                    opinnot
                      .tutkinnonOsat('999999')
                      .tutkinnonOsa(0)
                      .property('laajuus')
                      .setValue('3'),
                    editor.saveChanges,
                    opinnot.avaaKaikki
                  )
                  it('lisätty osa näytetään', function () {
                    expect(
                      opinnot.tutkinnonOsat('999999').tutkinnonOsa(0).nimi()
                    ).to.equal('Matematiikka')
                  })

                  describe('Paikallinen tutkinnon osan osa-alue', function () {
                    before(
                      editor.edit,
                      opinnot.avaaKaikki,
                      tutkinnonOsienOsat.lisääPaikallinenTutkinnonOsa(
                        'Hassut temput'
                      )
                    )

                    describe('Lisäyksen jälkeen', function () {
                      it('lisätty osa näytetään', function () {
                        expect(
                          tutkinnonOsienOsat.tutkinnonOsa(1).nimi()
                        ).to.equal('Hassut temput')
                      })
                    })

                    describe('Tallennuksen jälkeen', function () {
                      before(editor.saveChanges, opinnot.avaaKaikki)
                      it('lisätty osa näytetään', function () {
                        expect(
                          tutkinnonOsienOsat.tutkinnonOsa(1).nimi()
                        ).to.equal('Hassut temput')
                      })
                    })
                  })
                })
              })
            })
          })
          describe('Vapaavalintaisen tutkinnon osan lisääminen', function () {
            describe('Valtakunnallinen tutkinnon osa', function () {
              before(
                editor.edit,
                opinnot
                  .tutkinnonOsat('3')
                  .lisääTutkinnonOsa('Huippuosaajana toimiminen')
              )

              describe('Lisäyksen jälkeen', function () {
                it('lisätty osa näytetään', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Huippuosaajana toimiminen')
                })
              })

              describe('Tallennuksen jälkeen', function () {
                before(editor.saveChanges)
                it('lisätty osa näytetään', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Huippuosaajana toimiminen')
                })
              })
            })

            describe('Paikallinen tutkinnon osa', function () {
              before(
                editor.edit,
                opinnot.tutkinnonOsat('3').tutkinnonOsa(0).poistaTutkinnonOsa,
                opinnot
                  .tutkinnonOsat('3')
                  .lisääPaikallinenTutkinnonOsa('Hassut temput')
              )

              describe('Lisäyksen jälkeen', function () {
                it('lisätty osa näytetään', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Hassut temput')
                })
              })

              describe('Tallennuksen jälkeen', function () {
                before(editor.saveChanges)
                it('lisätty osa näytetään', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Hassut temput')
                })
              })
            })

            describe('Tutkinnon osa toisesta tutkinnosta', function () {
              describe('Kun valitaan sama tutkinto, kuin mitä ollaan suorittamassa', function () {
                before(
                  editor.edit,
                  opinnot.tutkinnonOsat('3').tutkinnonOsa(0).poistaTutkinnonOsa,
                  opinnot
                    .tutkinnonOsat('3')
                    .lisääTutkinnonOsaToisestaTutkinnosta(
                      'Autoalan perustutkinto',
                      'Auton korjaaminen'
                    ),
                  editor.saveChanges
                )
                it('Lisäys onnistuu (siksi, että dataan ei tule tutkinto-kenttää)', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Auton korjaaminen')
                })
              })
              describe('Kun valitaan toinen tutkinto', function () {
                before(
                  page.oppijaHaku.searchAndSelect('211097-402L'),
                  editor.edit,
                  opinnot.tutkinnonOsat('3').tutkinnonOsa(0).poistaTutkinnonOsa,
                  opinnot
                    .tutkinnonOsat('3')
                    .lisääTutkinnonOsaToisestaTutkinnosta(
                      'Autoalan perustutkinto',
                      'Auton korjaaminen'
                    )
                )

                describe('Lisäyksen jälkeen', function () {
                  it('lisätty osa näytetään', function () {
                    expect(
                      opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                    ).to.equal('Auton korjaaminen')
                  })
                })

                describe('Tallennuksen jälkeen', function () {
                  before(editor.saveChanges)
                  it('lisätty osa näytetään', function () {
                    expect(
                      opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                    ).to.equal('Auton korjaaminen')
                  })
                })
              })
            })
          })
        })
      })

      describe('Osaamisen tunnustamisen muokkaus', function () {
        var tunnustaminen = opinnot
          .tutkinnonOsat('1')
          .tutkinnonOsa(0)
          .property('tunnustettu')

        before(
          page.oppijaHaku.searchAndSelect('280608-6619'),
          editor.edit,
          opinnot
            .tutkinnonOsat('1')
            .lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
          opinnot.expandAll
        )

        describe('Alussa', function () {
          it('Ei osaamisen tunnustamistietoa, lisäysmahdollisuus', function () {
            expect(tunnustaminen.getValue()).to.equal(
              'Lisää osaamisen tunnustaminen'
            )
          })
        })

        describe('Lisääminen', function () {
          before(
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0)
              .lisääOsaamisenTunnustaminen,
            tunnustaminen
              .propertyBySelector('.selite')
              .setValue('Tunnustamisen esimerkkiselite'),
            editor.saveChanges,
            opinnot.expandAll
          )

          describe('Tallennuksen jälkeen', function () {
            it('Osaamisen tunnustamisen selite näytetään', function () {
              expect(tunnustaminen.getText()).to.equal(
                'Tunnustettu\nSelite Tunnustamisen esimerkkiselite\nRahoituksen piirissä ei'
              )
            })
          })

          describe('Muokkaus', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              tunnustaminen
                .propertyBySelector('.selite')
                .setValue('Tunnustamisen muokattu esimerkkiselite'),
              tunnustaminen.property('rahoituksenPiirissä').setValue(true),
              editor.saveChanges,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(tunnustaminen.getText()).to.equal(
                'Tunnustettu\nSelite Tunnustamisen muokattu esimerkkiselite\nRahoituksen piirissä kyllä'
              )
            })
          })

          describe('Poistaminen', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0)
                .poistaOsaamisenTunnustaminen,
              editor.saveChanges,
              editor.edit,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(tunnustaminen.getValue()).to.equal(
                'Lisää osaamisen tunnustaminen'
              )
            })
          })
        })
      })

      describe('Tutkinnon osan lisätietojen muokkaus', function () {
        function lisätiedot() {
          return opinnot.tutkinnonOsat('1').tutkinnonOsa(0).lisätiedot()
        }

        before(
          page.oppijaHaku.searchAndSelect('280608-6619'),
          editor.edit,
          opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaTutkinnonOsa,
          opinnot
            .tutkinnonOsat('1')
            .lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
          opinnot.expandAll
        )

        describe('Alussa', function () {
          it('ei lisätietoja', function () {
            expect(lisätiedot().getValue()).to.equal('lisää uusi')
          })
        })

        describe('Lisääminen', function () {
          before(
            lisätiedot().addItem,
            lisätiedot()
              .propertyBySelector(
                '.ammatillisentutkinnonosanlisatieto .dropdown-wrapper'
              )
              .setValue('Muu lisätieto'),
            lisätiedot()
              .propertyBySelector('.kuvaus')
              .setValue('Muita tietoja'),
            editor.saveChanges,
            opinnot.expandAll
          )

          describe('Tallennuksen jälkeen', function () {
            it('toimii', function () {
              expect(lisätiedot().getText()).to.equal(
                'Lisätiedot\nMuu lisätieto\nMuita tietoja'
              )
            })
          })

          describe('Muokkaus', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              lisätiedot()
                .propertyBySelector(
                  '.ammatillisentutkinnonosanlisatieto .dropdown-wrapper'
                )
                .setValue('Osaamisen arvioinnin mukauttaminen'),
              lisätiedot()
                .propertyBySelector('.kuvaus')
                .setValue('Arviointia on mukautettu'),
              editor.saveChanges,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(lisätiedot().getText()).to.equal(
                'Lisätiedot\nOsaamisen arvioinnin mukauttaminen\nArviointia on mukautettu'
              )
            })
          })

          describe('Poistaminen', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaLisätieto,
              editor.saveChanges,
              editor.edit,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(lisätiedot().getValue()).to.equal('lisää uusi')
            })
          })
        })
      })

      describe('Näytön muokkaus', function () {
        before(
          editor.edit,
          opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaTutkinnonOsa,
          opinnot.tutkinnonOsat('1').lisääTutkinnonOsa('Huolto- ja korjaustyöt')
        )

        describe('Alussa', function () {
          it('ei näyttöä', function () {
            expect(
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0).näyttö().getValue()
            ).to.equal('Lisää ammattiosaamisen näyttö')
          })
        })

        describe('Lisääminen', function () {
          before(
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0).avaaNäyttöModal,
            opinnot
              .tutkinnonOsat('1')
              .tutkinnonOsa(0)
              .asetaNäytönTiedot({
                kuvaus: 'Näytön esimerkkikuvaus',
                suorituspaikka: [
                  'työpaikka',
                  'Esimerkkityöpaikka, Esimerkkisijainti'
                ],
                työssäoppimisenYhteydessä: false,
                arvosana: '3',
                arvioinnistaPäättäneet: ['Opettaja'],
                arviointikeskusteluunOsallistuneet: ['Opettaja', 'Opiskelija'],
                arviointipäivä: '1.2.2017'
              }),
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0).painaOkNäyttöModal
          )
          it('toimii', function () {
            expect(
              opinnot
                .tutkinnonOsat('1')
                .tutkinnonOsa(0)
                .näyttö()
                .property('arvosana')
                .getValue()
            ).to.equal('3')
            expect(
              opinnot
                .tutkinnonOsat('1')
                .tutkinnonOsa(0)
                .näyttö()
                .property('kuvaus')
                .getValue()
            ).to.equal('Näytön esimerkkikuvaus')
          })
        })

        describe('Muokkaus', function () {
          before(
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0).avaaNäyttöModal,
            opinnot
              .tutkinnonOsat('1')
              .tutkinnonOsa(0)
              .asetaNäytönTiedot({
                kuvaus: 'Näytön muokattu esimerkkikuvaus',
                suorituspaikka: [
                  'työpaikka',
                  'Esimerkkityöpaikka, Esimerkkisijainti'
                ],
                työssäoppimisenYhteydessä: true,
                arvosana: '2',
                arvioinnistaPäättäneet: ['Opettaja'],
                arviointikeskusteluunOsallistuneet: ['Opettaja', 'Opiskelija'],
                arviointipäivä: '1.2.2017'
              }),
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0).painaOkNäyttöModal
          )
          describe('Näyttää oikeat tiedot', function () {
            it('toimii', function () {
              expect(
                opinnot
                  .tutkinnonOsat('1')
                  .tutkinnonOsa(0)
                  .näyttö()
                  .property('arvosana')
                  .getValue()
              ).to.equal('2')
              expect(
                opinnot
                  .tutkinnonOsat('1')
                  .tutkinnonOsa(0)
                  .näyttö()
                  .property('kuvaus')
                  .getValue()
              ).to.equal('Näytön muokattu esimerkkikuvaus')
            })
          })
          describe('Oikeat tiedot säilyvät modalissa', function () {
            before(opinnot.tutkinnonOsat('1').tutkinnonOsa(0).avaaNäyttöModal)
            it('toimii', function () {
              var näyttö = opinnot
                .tutkinnonOsat('1')
                .tutkinnonOsa(0)
                .lueNäyttöModal()
              expect(näyttö.kuvaus).to.equal('Näytön muokattu esimerkkikuvaus')
              expect(näyttö.suorituspaikka).to.deep.equal([
                'työpaikka',
                'Esimerkkityöpaikka, Esimerkkisijainti'
              ])
              expect(näyttö.työssäoppimisenYhteydessä).to.equal(true)
              expect(näyttö.arvosana).to.equal('2')
              expect(näyttö.arvioinnistaPäättäneet).to.deep.equal(['Opettaja'])
              expect(näyttö.arviointikeskusteluunOsallistuneet).to.deep.equal([
                'Opettaja',
                'Opiskelija'
              ])
              expect(näyttö.arviointipäivä).to.equal('1.2.2017')
            })
            after(opinnot.tutkinnonOsat('1').tutkinnonOsa(0).painaOkNäyttöModal)
          })
        })

        describe('Tallentamisen jälkeen', function () {
          before(editor.saveChanges, editor.edit, opinnot.expandAll)
          it('näyttää edelleen oikeat tiedot', function () {
            expect(
              opinnot
                .tutkinnonOsat('1')
                .tutkinnonOsa(0)
                .näyttö()
                .property('kuvaus')
                .getValue()
            ).to.equal('Näytön muokattu esimerkkikuvaus')
          })
        })

        describe('Poistaminen', function () {
          before(opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaNäyttö)
          it('toimii', function () {
            expect(
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0).näyttö().getValue()
            ).to.equal('Lisää ammattiosaamisen näyttö')
          })
        })

        describe('Tallentamisen jälkeen', function () {
          before(editor.saveChanges, editor.edit, opinnot.expandAll)
          it('näyttää edelleen oikeat tiedot', function () {
            expect(
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0).näyttö().getValue()
            ).to.equal('Lisää ammattiosaamisen näyttö')
          })
        })
      })

      describe('Sanallisen arvioinnin muokkaus', function () {
        describe('VALMA-suorituksen osille', function () {
          var sanallinenArviointi = opinnot
            .tutkinnonOsat()
            .tutkinnonOsa(0)
            .sanallinenArviointi()

          before(
            prepareForNewOppija('kalle', '230872-7258'),
            addOppija.enterValidDataAmmatillinen(),
            addOppija.selectOppimäärä(
              'Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)'
            ),
            addOppija.submitAndExpectSuccess(
              'Tyhjä, Tero (230872-7258)',
              'Ammatilliseen koulutukseen valmentava koulutus (VALMA)'
            ),
            editor.edit,
            opinnot
              .tutkinnonOsat()
              .lisääPaikallinenTutkinnonOsa('Hassut temput')
          )

          describe('Alussa', function () {
            it('syöttökenttä ei näytetä', function () {
              expect(sanallinenArviointi.isVisible()).to.equal(false)
            })
          })

          describe('Kun arvosana lisätty', function () {
            before(
              opinnot
                .tutkinnonOsat()
                .tutkinnonOsa(0)
                .propertyBySelector('.arvosana')
                .setValue('3', 1)
            )

            it('syöttökenttä näytetään', function () {
              expect(sanallinenArviointi.isVisible()).to.equal(true)
            })
          })

          describe('Muokkaus', function () {
            before(sanallinenArviointi.setValue('Hyvin meni'))

            it('näyttää oikeat tiedot', function () {
              expect(sanallinenArviointi.getValue()).to.equal('Hyvin meni')
            })
          })

          describe('Tallentamisen jälkeen', function () {
            before(editor.saveChanges, editor.edit, opinnot.expandAll)

            it('näyttää edelleen oikeat tiedot', function () {
              expect(sanallinenArviointi.getValue()).to.equal('Hyvin meni')
            })
          })

          describe('Arvosanan poistamisen ja uudelleenlisäämisen jälkeen', function () {
            before(
              opinnot
                .tutkinnonOsat()
                .tutkinnonOsa(0)
                .propertyBySelector('.arvosana')
                .setValue('Ei valintaa'),
              opinnot
                .tutkinnonOsat()
                .tutkinnonOsa(0)
                .propertyBySelector('.arvosana')
                .setValue('3', 1)
            )

            it('syöttökenttä on tyhjä', function () {
              expect(sanallinenArviointi.getValue()).to.equal('')
            })
          })
        })
      })

      describe('Kun suoritustapana on näyttö', function () {
        before(
          addOppija.addNewOppija('kalle', '280608-6619', {
            suoritustapa: 'Näyttö'
          }),
          editor.edit
        )

        it('Tutkinnon osia ei ryhmitellä', function () {
          expect(opinnot.tutkinnonOsat('1').isGroupHeaderVisible()).to.equal(
            false
          )
        })

        it('Keskiarvo-kenttä ei ole näkyvissä', function () {
          expect(editor.property('keskiarvo').isVisible()).to.equal(false)
        })

        describe('Tutkinnon osan lisääminen', function () {
          before(
            opinnot.tutkinnonOsat().lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
            editor.saveChanges
          )
          it('toimii', function () {})

          describe('Tutkinnon osan poistaminen', function () {
            before(
              editor.edit,
              opinnot.tutkinnonOsat().tutkinnonOsa(0).poistaTutkinnonOsa,
              editor.saveChanges
            )
            it('toimii', function () {
              expect(opinnot.tutkinnonOsat().tyhjä()).to.equal(true)
            })
          })
        })
      })

      describe('Uuden arvioinnin lisääminen', function () {
        function arviointi() {
          return opinnot.tutkinnonOsat('1').tutkinnonOsa(0).arviointi()
        }

        function lisääArviointi() {
          const addItemElems = opinnot
            .tutkinnonOsat('1')
            .tutkinnonOsa(0)
            .arviointi()
            .elem()
            .find('.add-item a')
          click(addItemElems[addItemElems.length - 1])()
        }

        function arviointiNth(nth) {
          return opinnot.tutkinnonOsat('1').tutkinnonOsa(0).arviointiNth(nth)
        }

        function poistaArvioinnit() {
          const items = opinnot
            .tutkinnonOsat('1')
            .tutkinnonOsa(0)
            .arviointi()
            .elem()
            .find('.remove-item')
          click(items)()
        }

        before(
          prepareForNewOppija('kalle', '060918-7919'),
          addOppija.enterHenkilötiedot({
            etunimet: 'Tero',
            kutsumanimi: 'Tero',
            sukunimi: 'Tyhjä'
          }),
          addOppija.selectOppilaitos('Stadin'),
          addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
          function () {
            return wait
              .until(Page().getInput('.tutkinto input').isVisible)()
              .then(
                Page().setInputValue(
                  '.tutkinto input',
                  'Autoalan perustutkinto'
                )
              )
              .then(click('.results li:last()'))
          },
          addOppija.selectAloituspäivä('1.1.2018'),
          addOppija.selectOpintojenRahoitus(
            'Valtionosuusrahoitteinen koulutus'
          ),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (060918-7919)'),
          editor.edit,
          opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaTutkinnonOsa,
          opinnot
            .tutkinnonOsat('1')
            .lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
          opinnot.expandAll
        )

        describe('Alussa', function () {
          it('ei lisätietoja', function () {
            expect(arviointi().getValue()).to.equal('lisää uusi')
          })
        })

        describe('Lisääminen', function () {
          before(lisääArviointi, lisääArviointi, opinnot.expandAll)

          it('toimii', function () {
            expect(arviointi().getText()).to.include(
              'Arviointi Arvosana Arviointiasteikko'
            )
          })

          describe('Muokkaus', function () {
            before(
              arviointiNth(0).propertyBySelector('.arvosana').selectValue(3),
              arviointiNth(1).propertyBySelector('.arvosana').selectValue(4)
            )

            it('toimii', function () {
              expect(arviointi().getText()).to.include(
                'Arviointi Arvosana Arviointiasteikko'
              )
            })
          })

          describe('Tallennetaan', function () {
            before(editor.saveChanges, opinnot.expandAll)

            it('näkyy oikein', function () {
              const arviointipäivä = moment().format('D.M.YYYY')
              expect(arviointi().getText()).to.equal(
                'Arviointi Arvosana 3\nArviointipäivä ' +
                  arviointipäivä +
                  '\nArvosana 4\nArviointipäivä ' +
                  arviointipäivä
              )
            })
          })

          describe('Poistaminen', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              poistaArvioinnit,
              editor.saveChanges,
              editor.edit,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(arviointi().getValue()).to.equal('lisää uusi')
            })
          })
        })
      })
    })

    describe('Päätason suorituksen poistaminen', function () {
      before(
        Authentication().logout,
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect('250989-419V'),
        editor.edit
      )

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
              opinnot.confirmDeletePäätasonSuoritus,
              wait.until(page.isPäätasonSuoritusDeletedMessageShown)
            )
            it('Päätason suoritus poistetaan', function () {
              expect(page.isPäätasonSuoritusDeletedMessageShown()).to.equal(
                true
              )
            })

            describe('Poistettua päätason suoritusta', function () {
              before(
                wait.until(page.isReady),
                opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
                  'ammatillinenkoulutus'
                )
              )

              it('Ei näytetä', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Autoalan työnjohdon erikoisammattitutkinto'
                ])
              })
            })
          })
        })
      })
    })
  })

  describe('Ammatillinen perustutkinto', function () {
    before(
      Authentication().login(),
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('280618-402H')
    )
    describe('Suoritus valmis, kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)
      describe('Tietojen näyttäminen', function () {
        it('näyttää ammatillisenopiskeluoikeudentyypin tiedot', function () {
          expect(extractAsText(S('.ammatillinenkoulutus'))).to.equal(
            'Ammatillinen koulutus\n' +
              'Stadin ammatti- ja aikuisopisto\n' +
              'Ammatillinen tutkinto 2012 — 2016 , Valmistunut'
          )
        })
        it('näyttää opiskeluoikeuden otsikkotiedot', function () {
          expect(
            opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
          ).to.deep.equal([
            'Stadin ammatti- ja aikuisopisto, Luonto- ja ympäristöalan perustutkinto (2012—2016, valmistunut)'
          ])
        })
        it('näyttää opiskeluoikeuden tiedot', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
            'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 31.5.2016\n' +
              'Tila 31.5.2016 Valmistunut (työnantajan kokonaan rahoittama)\n' +
              '1.9.2012 Läsnä (työnantajan kokonaan rahoittama)'
          )
        })

        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equalIgnoreNewlines(
            'Koulutus Luonto- ja ympäristöalan perustutkinto 361902 62/011/2014\n' +
              'Suoritustapa Ammatillinen perustutkinto\n' +
              'Tutkintonimike Ympäristönhoitaja\nOsaamisala Ympäristöalan osaamisala\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Suorituskieli suomi\n' +
              'Järjestämismuodot 1.9.2013 — , Koulutuksen järjestäminen oppilaitosmuotoisena\n' +
              'Työssäoppimisjaksot 1.1.2014 — 15.3.2014 Jyväskylä , Suomi\n' +
              'Työssäoppimispaikka Sortti-asema\n' +
              'Työtehtävät Toimi harjoittelijana Sortti-asemalla\n' +
              'Laajuus 5 osp\n' +
              'Ryhmä YMP14SN\n' +
              'Painotettu keskiarvo 4,00\n' +
              'Suoritus valmis Vahvistus : 31.5.2016 Helsinki Reijo Reksi , rehtori'
          )
        })

        it('näyttää tutkinnon osat', function () {
          expect(
            extractAsText(S('.ammatillisentutkinnonsuoritus > .osasuoritukset'))
          ).to.equalIgnoreNewlines(
            'Sulje kaikki\n' +
              'Ammatilliset tutkinnon osat Laajuus (osp) Arvosana\n' +
              'Kestävällä tavalla toimiminen 40 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 1.1.2015\n' +
              'Ympäristön hoitaminen 35 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Näyttö\n' +
              'Kuvaus Muksulan päiväkodin ympäristövaikutusten arvioiminen ja ympäristön kunnostustöiden tekeminen sekä mittauksien tekeminen ja näytteiden ottaminen\n' +
              'Suorituspaikka Muksulan päiväkoti, Kaarinan kunta\n' +
              'Suoritusaika 1.2.2016 — 1.2.2016\n' +
              'Työssäoppimisen yhteydessä ei\n' +
              'Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Arvioijat Jaana Arstila ( näyttötutkintomestari ) Pekka Saurmann ( näyttötutkintomestari ) Juhani Mykkänen\n' +
              'Arviointikohteet Arviointikohde Arvosana\n' +
              'Työprosessin hallinta 3\n' +
              'Työmenetelmien, -välineiden ja materiaalin hallinta 2\n' +
              'Työn perustana olevan tiedon hallinta 2\n' +
              'Elinikäisen oppimisen avaintaidot 3\n' +
              'Arvioinnista päättäneet Opettaja\n' +
              'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Uusiutuvien energialähteiden hyödyntäminen 15 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Ulkoilureittien rakentaminen ja hoitaminen 15 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Kulttuuriympäristöjen kunnostaminen ja hoitaminen 15 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Näyttö\n' +
              'Kuvaus Sastamalan kunnan kulttuuriympäristöohjelmaan liittyvän Wanhan myllyn lähiympäristön kasvillisuuden kartoittamisen sekä ennallistamisen suunnittelu ja toteutus\n' +
              'Suorituspaikka Sastamalan kunta\n' +
              'Suoritusaika 1.3.2016 — 1.3.2016\n' +
              'Työssäoppimisen yhteydessä ei\n' +
              'Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Arvioijat Jaana Arstila ( näyttötutkintomestari ) Pekka Saurmann ( näyttötutkintomestari ) Juhani Mykkänen\n' +
              'Arviointikohteet Arviointikohde Arvosana\n' +
              'Työprosessin hallinta 3\n' +
              'Työmenetelmien, -välineiden ja materiaalin hallinta 2\n' +
              'Työn perustana olevan tiedon hallinta 2\n' +
              'Elinikäisen oppimisen avaintaidot 3\n' +
              'Arvioinnista päättäneet Opettaja\n' +
              'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Vesistöjen kunnostaminen ja hoitaminen 15 Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Lisätiedot\n' +
              'Muutos arviointiasteikossa\n' +
              'Tutkinnon osa on koulutuksen järjestäjän päätöksellä arvioitu asteikolla hyväksytty/hylätty.\n' +
              'Näyttö\n' +
              'Kuvaus Uimarin järven tilan arviointi ja kunnostus\n' +
              'Suorituspaikka Vesipojat Oy\n' +
              'Suoritusaika 1.4.2016 — 1.4.2016\n' +
              'Työssäoppimisen yhteydessä ei\n' +
              'Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Arvioijat Jaana Arstila ( näyttötutkintomestari ) Pekka Saurmann ( näyttötutkintomestari ) Juhani Mykkänen\n' +
              'Arviointikohteet Arviointikohde Arvosana\n' +
              'Työprosessin hallinta 3\n' +
              'Työmenetelmien, -välineiden ja materiaalin hallinta 2\n' +
              'Työn perustana olevan tiedon hallinta 2\n' +
              'Elinikäisen oppimisen avaintaidot 3\n' +
              'Arvioinnista päättäneet Opettaja\n' +
              'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Kokonaisuus Arvosana\n' +
              'Hoitotarpeen määrittäminen Hyväksytty\n' +
              'Kuvaus Hoitotarpeen määrittäminen\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.3.2013\n' +
              'Arvioijat Jaana Arstila Pekka Saurmann Juhani Mykkänen\n' +
              'Yhteensä 135 / 135 osp\n' +
              'Yhteiset tutkinnon osat Laajuus (osp) Arvosana\n' +
              'Viestintä- ja vuorovaikutusosaaminen 11 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Osa-alue Laajuus (osp) Arvosana\n' +
              'Äidinkieli, Suomen kieli ja kirjallisuus 5 3\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Äidinkieli, Suomen kieli ja kirjallisuus 3 3\n' +
              'Pakollinen ei\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Toinen kotimainen kieli, ruotsi, ruotsi 1 3\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Vieraat kielet, englanti 2 3\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Matemaattis-luonnontieteellinen osaaminen 12 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Lisätiedot\n' +
              'Osaamisen arvioinnin mukauttaminen\n' +
              'Tutkinnon osan ammattitaitovaatimuksia tai osaamistavoitteita ja osaamisen arviointia on mukautettu ammatillisesta peruskoulutuksesta annetun lain (630/1998, muutos 246/2015) 19 a tai 21 §:n perusteella\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Osa-alue Laajuus (osp) Arvosana\n' +
              'Matematiikka 3 3\n' +
              'Kuvaus Matematiikan opinnot\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Matematiikka 3 3\n' +
              'Kuvaus Matematiikan opinnot\n' +
              'Pakollinen ei\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Fysiikka ja kemia 2 3\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Fysiikka ja kemia 3 3\n' +
              'Pakollinen ei\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Tieto- ja viestintätekniikka sekä sen hyödyntäminen 1 3\n' +
              'Pakollinen kyllä\n' +
              'Alkamispäivä 1.1.2014\n' +
              'Tunnustettu\n' +
              'Tutkinnon osa Asennushitsaus\n' +
              'Selite Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta\n' +
              'Rahoituksen piirissä ei\n' +
              'Lisätiedot\n' +
              'Osaamisen arvioinnin mukauttaminen\n' +
              'Tutkinnon osan ammattitaitovaatimuksia tai osaamistavoitteita ja osaamisen arviointia on mukautettu ammatillisesta peruskoulutuksesta annetun lain (630/1998, muutos 246/2015) 19 a tai 21 §:n perusteella\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 1.1.2015\n' +
              'Yhteiskunnassa ja työelämässä tarvittava osaaminen 8 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Osa-alue Laajuus (osp) Arvosana\n' +
              'Yhteiskuntatieto 8 3\n' +
              'Kuvaus Yhteiskuntaopin opinnot\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sosiaalinen ja kulttuurinen osaaminen 7 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Osa-alue Laajuus (osp) Arvosana\n' +
              'Sosiaalitaito 7 3\n' +
              'Kuvaus Vuorotaitovaikutuksen kurssi\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yhteensä 38 / 35 osp\n' +
              'Vapaasti valittavat tutkinnon osat Laajuus (osp) Arvosana\n' +
              'Sosiaalinen ja kulttuurinen osaaminen 5 3\n' +
              'Kuvaus Sosiaalinen ja kulttuurinen osaaminen\n' +
              'Pakollinen ei\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yhteensä 5 / 10 osp\n' +
              'Tutkintoa yksilöllisesti laajentavat tutkinnon osat Laajuus (osp) Arvosana\n' +
              'Matkailuenglanti 5 3\n' +
              'Kuvaus Matkailuenglanti\n' +
              'Pakollinen ei\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yhteensä 5 osp'
          )
        })
      })
    })

    describe('Suoritus kesken, vanhan perusteen suoritus tunnustettu', function () {
      before(
        Authentication().login(),
        resetFixtures,
        page.openPage,
        page.oppijaHaku.searchAndSelect('140176-449X'),
        opinnot.expandAll
      )
      it('näyttää opiskeluoikeuden tiedot', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 1.9.2016 — 1.5.2020 (arvioitu)\n' +
            'Tila 1.9.2016 Läsnä (valtionosuusrahoitteinen koulutus)'
        )
      })

      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Autoalan perustutkinto 351301 39/011/2014\n' +
            'Suoritustapa Näyttötutkinto\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Alkamispäivä 1.9.2016\n' +
            'Suorituskieli suomi\n' +
            'Suoritus kesken'
        )
      })

      it('näyttää tutkinnon osat', function () {
        expect(extractAsText(S('.osasuoritukset'))).to.equalIgnoreNewlines(
          'Sulje kaikki\n' +
            'Tutkinnon osa Laajuus (osp) Arvosana\n' +
            'Moottorin ja voimansiirron huolto ja korjaus 15 Hyväksytty\n' +
            'Pakollinen ei\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Vahvistus 31.5.2013 Reijo Reksi , rehtori\n' +
            'Tunnustettu\n' +
            'Tutkinnon osa Tunniste 11-22-33\n' +
            'Nimi Moottorin korjaus\n' +
            'Kuvaus Opiskelijan on - tunnettava jakopyörästön merkitys moottorin toiminnalle - osattava kytkeä moottorin testauslaite ja tulkita mittaustuloksen suhdetta valmistajan antamiin ohjearvoihin - osattava käyttää moottorikorjauksessa tarvittavia perustyökaluja - osattava suorittaa jakopään hammashihnan vaihto annettujen ohjeiden mukaisesti - tunnettava venttiilikoneiston merkitys moottorin toiminnan osana osatakseen mm. ottaa se huomioon jakopään huoltoja tehdessään - noudatettava sovittuja työaikoja\n' +
            'Vahvistus 28.5.2002 Reijo Reksi\n' +
            'Näyttö\n' +
            'Kuvaus Moottorin korjaus\n' +
            'Suorituspaikka Autokorjaamo Oy, Riihimäki\n' +
            'Suoritusaika 20.4.2002 — 20.4.2002\n' +
            'Työssäoppimisen yhteydessä ei\n' +
            'Selite Tutkinnon osa on tunnustettu aiemmin suoritetusta autoalan perustutkinnon osasta (1.8.2000 nro 11/011/2000)\nRahoituksen piirissä ei\n' +
            'Arviointi Arvosana Hyväksytty\n' +
            'Arviointipäivä 20.3.2013\n' +
            'Arvioijat Jaana Arstila Pekka Saurmann Juhani Mykkänen\n' +
            'Yhteensä 15 osp'
        )
      })
    })

    describe('Opiskeluoikeuden lisätiedot', function () {
      before(
        Authentication().login(),
        resetFixtures,
        page.openPage,
        page.oppijaHaku.searchAndSelect('211097-402L'),
        opinnot.expandAll
      )

      it('näytetään', function () {
        expect(
          extractAsText(S('.opiskeluoikeuden-tiedot > .lisätiedot'))
        ).to.equal(
          'Lisätiedot\n' +
            'Majoitus 1.9.2012 — 1.9.2013\n' +
            'Sisäoppilaitosmainen majoitus 1.9.2012 — 1.9.2013\n' +
            'Vaativan erityisen tuen yhteydessä järjestettävä majoitus 1.9.2012 — 1.9.2013\n' +
            'Ulkomaanjaksot 1.9.2012 — 1.9.2013 Maa Ruotsi Kuvaus Harjoittelua ulkomailla\n' +
            'Hojks Opetusryhmä Yleinen opetusryhmä\n' +
            'Vaikeasti vammaisille järjestetty opetus 1.9.2012 — 1.9.2013\n' +
            'Vammainen ja avustaja 1.9.2012 — 1.9.2013\n' +
            'Osa-aikaisuusjaksot 1.9.2012 — Osa-aikaisuus 80 %\n' +
            '8.5.2019 — Osa-aikaisuus 60 %\n' +
            'Opiskeluvalmiuksia tukevat opinnot 1.10.2013 — 31.10.2013 Kuvaus Opiskeluvalmiuksia tukevia opintoja\n' +
            'Henkilöstökoulutus kyllä\n' +
            'Vankilaopetuksessa 2.9.2013 —'
        )
      })
    })
  })

  describe('Osittainen ammatillinen tutkinto', function () {
    before(
      Authentication().login(),
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('230297-6448')
    )
    describe('Kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)

      it('näyttää opiskeluoikeuden otsikkotiedot', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal([
          'Stadin ammatti- ja aikuisopisto, Luonto- ja ympäristöalan perustutkinto, osittainen (2012—2016, valmistunut)'
        ])
        expect(extractAsText(S('.suoritus-tabs .selected'))).to.equal(
          'Luonto- ja ympäristöalan perustutkinto, osittainen'
        )
      })

      it('näyttää opiskeluoikeuden tiedot', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 4.6.2016\n' +
            'Tila 4.6.2016 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
            '1.9.2012 Läsnä (valtionosuusrahoitteinen koulutus)'
        )
      })

      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Luonto- ja ympäristöalan perustutkinto 361902 62/011/2014\n' +
            'Suoritustapa Ammatillinen perustutkinto\n' +
            'Tutkintonimike Autokorinkorjaaja\n' +
            'Toinen tutkintonimike kyllä\n' +
            'Osaamisala Autokorinkorjauksen osaamisala\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Suorituskieli suomi\n' +
            'Järjestämismuodot 1.9.2012 — , Koulutuksen järjestäminen oppilaitosmuotoisena\n' +
            'Todistuksella näkyvät lisätiedot Suorittaa toista osaamisalaa\n' +
            'Painotettu keskiarvo 4,00\n' +
            'Suoritus valmis Vahvistus : 4.6.2016 Reijo Reksi , rehtori'
        )
      })

      it('näyttää tutkinnon osat', function () {
        expect(extractAsText(S('.osasuoritukset'))).to.equalIgnoreNewlines(
          'Sulje kaikki\n' +
            'Ammatilliset tutkinnon osat Laajuus (osp) Arvosana\n' +
            'Ympäristön hoitaminen 35 3\n' +
            'Pakollinen kyllä\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
            'Arviointi Arvosana 3\n' +
            'Arviointipäivä 20.10.2014\n' +
            'Yhteensä 35 / 135 osp'
        )
      })
    })
  })

  describe('Näyttötutkinnot', function () {
    before(
      Authentication().login(),
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('250989-419V'),
      OpinnotPage().valitseSuoritus(
        undefined,
        'Näyttötutkintoon valmistava koulutus'
      )
    )
    describe('Näyttötutkintoon valmistava koulutus', function () {
      describe('Kaikki tiedot näkyvissä', function () {
        before(opinnot.expandAll)
        it('näyttää opiskeluoikeuden tiedot', function () {
          expect(
            opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()
          ).to.deep.equal([
            'Näyttötutkintoon valmistava koulutus 2012—2016, Valmistunut'
          ])
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
            'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 31.8.2016\n' +
              'Tila 31.8.2016 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
              '1.9.2012 Läsnä (valtionosuusrahoitteinen koulutus)'
          )
        })

        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Koulutus Näyttötutkintoon valmistava koulutus\n' +
              'Tutkinto Autoalan työnjohdon erikoisammattitutkinto 457305 40/011/2001\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Alkamispäivä 1.9.2012\n' +
              'Päättymispäivä 31.5.2015\n' +
              'Suorituskieli suomi\n' +
              'Suoritus valmis Vahvistus : 31.5.2015 Helsinki Reijo Reksi , rehtori'
          )
        })

        it('näyttää tutkinnon osat', function () {
          expect(extractAsText(S('.osasuoritukset'))).to.equalIgnoreNewlines(
            'Sulje kaikki\n' +
              'Koulutuksen osa\n' +
              'Johtaminen ja henkilöstön kehittäminen\n' +
              'Kuvaus Johtamisen ja henkilöstön kehittämisen valmistava koulutus\n' +
              'Auton lisävarustetyöt\n' +
              'Kuvaus valojärjestelmät\n' +
              'Auton lisävarustetyöt\n' +
              'Kuvaus lämmitysjärjestelmät'
          )
        })
      })

      describe('Tietojen muokkaus', function () {
        describe('Tutkinnon osan lisääminen', function () {
          before(editor.edit)

          describe('Paikallinen koulutuksen osa', function () {
            before(
              editor.edit,
              opinnot
                .tutkinnonOsat()
                .lisääPaikallinenTutkinnonOsa('Hassut temput')
            )

            describe('Lisäyksen jälkeen', function () {
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(3).nimi()).to.equal(
                  'Hassut temput'
                )
              })
            })

            describe('Tallennuksen jälkeen', function () {
              before(editor.saveChanges)
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(3).nimi()).to.equal(
                  'Hassut temput'
                )
              })
            })
          })

          describe('Ammatillinen tutkinnon osa', function () {
            before(
              editor.edit,
              opinnot.tutkinnonOsat().lisääTutkinnonOsa('Projektiosaaminen')
            )

            describe('Lisäyksen jälkeen', function () {
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(4).nimi()).to.equal(
                  'Projektiosaaminen'
                )
              })
            })

            describe('Tallennuksen jälkeen', function () {
              before(editor.saveChanges)
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(4).nimi()).to.equal(
                  'Projektiosaaminen'
                )
              })
            })
          })

          describe('Tutkinnon osa toisesta tutkinnosta', function () {
            before(
              editor.edit,
              opinnot
                .tutkinnonOsat()
                .lisääTutkinnonOsaToisestaTutkinnosta(
                  'Autoalan perustutkinto',
                  'Auton korjaaminen'
                )
            )

            describe('Lisäyksen jälkeen', function () {
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(5).nimi()).to.equal(
                  'Auton korjaaminen'
                )
              })
            })

            describe('Tallennuksen jälkeen', function () {
              before(editor.saveChanges)
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(5).nimi()).to.equal(
                  'Auton korjaaminen'
                )
              })
            })
          })
        })
      })
    })

    describe('Erikoisammattitutkinto', function () {
      before(
        wait.until(page.isOppijaSelected('Erja')),
        OpinnotPage().valitseSuoritus(
          undefined,
          'Autoalan työnjohdon erikoisammattitutkinto'
        )
      )
      describe('Kaikki tiedot näkyvissä', function () {
        before(opinnot.expandAll)
        it('näyttää opiskeluoikeuden tiedot', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
            'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 31.8.2016\n' +
              'Tila 31.8.2016 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
              '1.9.2012 Läsnä (valtionosuusrahoitteinen koulutus)'
          )
        })

        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Koulutus Autoalan työnjohdon erikoisammattitutkinto 457305 40/011/2001\n' +
              'Suoritustapa Näyttötutkinto\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Suorituskieli suomi\n' +
              'Järjestämismuodot 1.8.2014 — , Koulutuksen järjestäminen oppilaitosmuotoisena\n' +
              '31.5.2015 — , Koulutuksen järjestäminen oppisopimuskoulutuksena\n' +
              'Yritys Autokorjaamo Oy Y-tunnus 1234567-8\n' +
              '31.3.2016 — , Koulutuksen järjestäminen oppilaitosmuotoisena\n' +
              'Suoritus valmis Vahvistus : 31.5.2016 Helsinki Reijo Reksi , rehtori'
          )
        })

        it('näyttää tutkinnon osat', function () {
          expect(extractAsText(S('.osasuoritukset'))).to.equalIgnoreNewlines(
            'Sulje kaikki Tutkinnon osa Arvosana\n' +
              'Johtaminen ja henkilöstön kehittäminen Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Asiakaspalvelu ja korjaamopalvelujen markkinointi Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Työnsuunnittelu ja organisointi Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Taloudellinen toiminta Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yrittäjyys Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yhteensä 0 osp'
          )
        })
      })

      describe('Tutkinnon osat', function () {
        before(editor.edit)
        it('Tutkinnon osia ei ryhmitellä', function () {
          expect(opinnot.tutkinnonOsat('1').isGroupHeaderVisible()).to.equal(
            false
          )
        })

        before(
          opinnot.tutkinnonOsat().lisääTutkinnonOsa('Tekniikan asiantuntemus')
        )

        describe('Lisäyksen jälkeen', function () {
          it('lisätty osa näytetään', function () {
            expect(opinnot.tutkinnonOsat().tutkinnonOsa(5).nimi()).to.equal(
              'Tekniikan asiantuntemus'
            )
          })
          describe('kun tallennetaan', function () {
            before(
              editor.property('tila').removeItem(0),
              opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
              opinnot
                .tutkinnonOsat()
                .tutkinnonOsa(5)
                .propertyBySelector('.arvosana')
                .setValue('3', 1),
              editor.saveChanges
            )
            it('tallennus onnistuu', function () {
              expect(page.isSavedLabelShown()).to.equal(true)
            })
          })
        })
      })
    })

    describe('Uusi erikoisammattitutkinto', function () {
      before(
        addOppija.addNewOppija('kalle', '250858-5188', {
          oppilaitos: 'Stadin',
          tutkinto: 'Autoalan työnjohdon erikoisammattitutkinto',
          suoritustapa: ''
        })
      )
      describe('Uuden tutkinnonosan lisääminen', function () {
        before(
          editor.edit,
          opinnot.tutkinnonOsat().lisääTutkinnonOsa('Tekniikan asiantuntemus'),
          opinnot
            .tutkinnonOsat()
            .tutkinnonOsa(0)
            .propertyBySelector('.arvosana')
            .setValue('3', 1),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )
        it('onnistuu', function () {
          expect(extractAsText(S('.osasuoritukset'))).to.equalIgnoreNewlines(
            'Avaa kaikki\n' +
              'Tutkinnon osa Arvosana\n' +
              'Tekniikan asiantuntemus 3\n' +
              'Yhteensä 0 osp'
          )
        })
      })
    })
  })

  describe('Luottamuksellinen data', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('010101-123N'),
      opinnot.expandAll
    )
    describe('Kun käyttäjällä on luottamuksellinen-rooli', function () {
      it('näkyy', function () {
        expect(extractAsText(S('.lisätiedot'))).to.equal(
          'Lisätiedot\n' +
            'Erityinen tuki 30.5.2019 —\n' +
            'Vaikeasti vammaisille järjestetty opetus 30.5.2019 —\n' +
            'Vankilaopetuksessa 30.5.2019 —'
        )
      })
    })

    describe('Kun käyttäjällä ei ole luottamuksellinen-roolia', function () {
      before(
        Authentication().logout,
        Authentication().login('stadin-vastuu'),
        page.openPage,
        page.oppijaHaku.searchAndSelect('010101-123N'),
        opinnot.expandAll
      )
      it('piilotettu', function () {
        expect(isElementVisible(S('.lisätiedot'))).to.equal(false)
      })
    })
  })

  describe('Osittaisen ammatillisen tutkinnon validaatio', function () {
    var yhteinenTutkinnonOsa = opinnot.tutkinnonOsat().tutkinnonOsa(5)
    var osanOsa0 = yhteinenTutkinnonOsa.osanOsat().tutkinnonOsa(0)
    var osanOsa1 = yhteinenTutkinnonOsa.osanOsat().tutkinnonOsa(1)
    var osanOsa2 = yhteinenTutkinnonOsa.osanOsat().tutkinnonOsa(2)

    describe('Valmiiksi merkitseminen', function () {
      before(
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect('140493-2798'),
        editor.edit,
        editor.property('tila').removeItem(0),
        opinnot.valitseSuoritus(
          undefined,
          'Luonto- ja ympäristöalan perustutkinto, osittainen'
        ),
        opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi
      )

      describe('Keskeneräisellä yhteisen tutkinnon osalla', function () {
        describe('Kaikki osa-alueet valmiita', function () {
          before(
            yhteinenTutkinnonOsa
              .propertyBySelector('.arvosana')
              .setValue('Ei valintaa')
          )

          it('Voidaan asettaa valmiiksi', function () {
            expect(opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(
              true
            )
          })
        })

        describe('On yksi keskeräinen osa-alue', function () {
          before(
            editor.edit,
            yhteinenTutkinnonOsa.toggleExpand,
            osanOsa0.propertyBySelector('.arvosana').setValue('Ei valintaa')
          )

          it('Ei voida asettaa valmiiksi', function () {
            expect(opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(
              false
            )
          })
        })

        describe('Ei yhtään osan osa-aluetta', function () {
          before(
            editor.edit,
            osanOsa2.poistaTutkinnonOsa,
            osanOsa1.poistaTutkinnonOsa,
            osanOsa0.poistaTutkinnonOsa
          )

          it('Ei voida asettaa valmiiksi', function () {
            expect(opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(
              false
            )
          })
        })

        after(editor.cancelChanges)
      })

      describe('Ammatillisen tutkinnon osan suoritus puuttuu, mutta opiskeluoikeuteen on sisällytetty toinen opiskeluoikeus', function () {
        var firstEditor = opinnot.opiskeluoikeusEditor(0)
        var secondEditor = opinnot.opiskeluoikeusEditor(1)
        var secondOpinnot = OpinnotPage(1)

        var clickLisääOpiskeluoikeus = function () {
          return S('li.add-opiskeluoikeus > a > span').click()
        }
        var clickValitseOppilaitosDropdown = function () {
          return S(
            'label.oppilaitos .organisaatio .organisaatio-selection'
          ).click()
        }
        var clickMuokkaaOpiskeluoikeus = function () {
          return S('div.opiskeluoikeuden-tiedot button.toggle-edit')[0].click()
        }
        var täydennäSisältyvänOpiskeluoikeudenOid = function () {
          return firstEditor
            .property('oid')
            .setValue(
              S(
                'ul.opiskeluoikeuksientiedot span.id:eq( 1 ) > span.value'
              ).text()
            )()
        }
        var tallenna = function () {
          return S('#edit-bar button.koski-button').click()
        }
        var waitAjax = function () {
          return wait.forAjax()
        }

        var clickMuokkaaSisällytettyOpiskeluoikeus = function () {
          return S('.opiskeluoikeuden-tiedot:eq(1) .koski-button').click()
        }

        before(
          clickLisääOpiskeluoikeus,
          waitAjax,
          clickValitseOppilaitosDropdown,
          addOppija.selectOppilaitos('Omnia'),
          addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen'),
          addOppija.selectTutkinto('Autoalan perustutkinto'),
          addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
          addOppija.selectAloituspäivä('1.1.2018'),
          addOppija.selectOpintojenRahoitus(
            'Valtionosuusrahoitteinen koulutus'
          ),
          addOppija.submitModal,

          clickMuokkaaOpiskeluoikeus,
          firstEditor.property('sisältyyOpiskeluoikeuteen').addValue,
          firstEditor
            .property('oppilaitos')
            .organisaatioValitsin()
            .select('Stadin ammatti- ja aikuisopisto'),

          täydennäSisältyvänOpiskeluoikeudenOid,
          tallenna,
          waitAjax,

          clickMuokkaaSisällytettyOpiskeluoikeus,
          secondEditor.property('tila').removeItem(0),
          secondOpinnot.valitseSuoritus(
            1,
            'Luonto- ja ympäristöalan perustutkinto, osittainen'
          ),
          TilaJaVahvistusIndeksillä(1).merkitseKeskeneräiseksi,
          secondEditor.property('ostettu').setValue(true),
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(5).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(4).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(3).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(2).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(1).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(0).poistaTutkinnonOsa,

          TilaJaVahvistusIndeksillä(1).merkitseValmiiksi,
          TilaJaVahvistusIndeksillä(1).lisääVahvistus('1.1.2000')
        )

        it('Voidaan vahvistaa muualta ostettu opiskeluoikeus', function () {
          expect(isElementVisible(S('button.merkitse-kesken'))).to.equal(true)
        })

        after(resetFixtures)
      })
    })

    describe('Tallentaminen', function () {
      before(
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect('140493-2798')
      )

      describe('Kun päätason suoritus on valmis', function () {
        describe('Ja ammatillisen tutkinnon osaa ei ole suoritettu', function () {
          before(
            opinnot.tutkinnonOsat().tutkinnonOsa(3).poistaTutkinnonOsa,
            opinnot.tutkinnonOsat().tutkinnonOsa(2).poistaTutkinnonOsa,
            opinnot.tutkinnonOsat().tutkinnonOsa(1).poistaTutkinnonOsa,
            opinnot.tutkinnonOsat().tutkinnonOsa(0).poistaTutkinnonOsa
          )

          it('Tallentaminen on estetty', function () {
            expect(opinnot.onTallennettavissa()).to.equal(false)
          })

          after(editor.cancelChanges)
        })

        describe('Ja yhteinen tutkinnon osa on kesken', function () {
          before(
            editor.edit,
            yhteinenTutkinnonOsa
              .propertyBySelector('.arvosana')
              .setValue('Ei valintaa')
          )

          describe('Jos kaikki osan osa-alueet on valmiita', function () {
            it('Tallenntaminen on sallittu', function () {
              expect(opinnot.onTallennettavissa()).to.equal(true)
            })
          })

          describe('Jos yksi osan osa-alue on kesken', function () {
            before(
              editor.edit,
              yhteinenTutkinnonOsa.toggleExpand,
              osanOsa0.propertyBySelector('.arvosana').setValue('Ei valintaa')
            )

            it('Tallentaminen on estetty', function () {
              expect(opinnot.onTallennettavissa()).to.equal(false)
            })
          })

          describe('Jos ei ole osan osa-alueita', function () {
            before(
              editor.edit,
              osanOsa2.poistaTutkinnonOsa,
              osanOsa1.poistaTutkinnonOsa,
              osanOsa0.poistaTutkinnonOsa
            )

            it('Tallentaminen on estettyy', function () {
              expect(opinnot.onTallennettavissa()).to.equal(false)
            })
          })

          after(editor.cancelChanges)
        })
      })
    })
  })
})
