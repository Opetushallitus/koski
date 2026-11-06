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
          return eventually(() =>
            expect(addOppija.rahoitusIsVisible()).to.equal(true)
          )()
        })

        it('Vaihtoehtoina on kaikki opintojenRahoitus-vaihtoehdot', async function () {
          expect(await addOppija.opintojenRahoitukset()).to.deep.equal([
            'Valtionosuusrahoitteinen koulutus',
            'Työvoimakoulutus (OKM rahoitus)',
            'Ammatillisen osaamisen pilotit 2019',
            'Ammatillisen osaamisen pilotit 2019 (työvoimakoulutus)',
            'Työvoimakoulutus (valtiosopimukseen perustuva rahoitus)',
            'Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus',
            'Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus (RRF)',
            'Lukuvuosimaksu',
            'Tilauskoulutus',
            'Työvoimakoulutus ely-keskukset ja työ- ja elinkeinotoimistot (kansallinen rahoitus)',
            'Työvoimakoulutus (ESR-rahoitteinen)',
            'Työnantajan kokonaan rahoittama',
            'Muuta kautta rahoitettu',
            'Nuorten aikuisten osaamisohjelma',
            'Aikuisten osaamisperustan vahvistaminen',
            'Maahanmuuttajien ammatillinen koulutus (valtionavustus)'
          ])
        })

        it('Näytetään tilavaihtoehdoissa loma-tila, mutta ei eronnut-tilaa', async function () {
          expect(await addOppija.opiskeluoikeudenTilat()).to.deep.equal([
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
            // tutkinto-Select hakee vaihtoehtonsa syötteen perusteella, eikä se
            // - toisin kuin koodistopohjaiset valikot - disabloi itseään haun
            // ajaksi. Valikko voi siis olla auki mutta vielä tyhjä, joten
            // sisältöä on odotettava erikseen.
            return eventually(async () =>
              expect(await addOppija.tutkinnot()).to.deep.equal([
                '351301 Autoalan perustutkinto (39/011/2014)',
                '351301 Autoalan perustutkinto (OPH-2762-2017)',
                '351301 Autoalan perustutkinto, Koulutusvientikokeilu (OPH-4792-2017)',
                '351407 Sähkö- ja automaatiotekniikan perustutkinto (77/011/2014)',
                '354315 Auto- ja kuljetusalan työnjohdon ammattitutkinto (30/011/2015)',
                '354401 Automaatioasentajan ammattitutkinto (3/011/2013)',
                '357304 Automekaanikon erikoisammattitutkinto (OPH-1886-2017)',
                '357401 Automaatioyliasentajan erikoisammattitutkinto (9/011/2008)',
                '364307 Puutavaran autokuljetuksen ammattitutkinto (27/011/2008)',
                '457305 Autoalan työnjohdon erikoisammattitutkinto (40/011/2001)'
              ])
            )()
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
          return eventually(() =>
            expect(addOppija.isEnabled()).to.equal(true)
          )()
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
            return eventually(() =>
              expect(addOppija.isEnabled()).to.equal(true)
            )()
          })
        })
      })
      describe('Tutkinto', function () {
        before(addOppija.enterValidDataAmmatillinen())
        describe('Aluksi', function () {
          it('Lisää-nappi enabloitu', function () {
            return eventually(() =>
              expect(addOppija.isEnabled()).to.equal(true)
            )()
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
              return eventually(() =>
                expect(addOppija.isEnabled()).to.equal(true)
              )()
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
            return eventually(() => {
              expect(addOppija.tutkintoIsEnabled()).to.equal(true)
              expect(addOppija.isEnabled()).to.equal(true)
            })()
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
              return eventually(() =>
                expect(addOppija.isEnabled()).to.equal(true)
              )()
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

    describe('Näyttötutkintoon valmistavan koulutuksen lisääminen', function () {
      describe('Olemassa olevaan opiskeluoikeuteen, jossa VALMA-suoritus', function () {
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

  })

})
