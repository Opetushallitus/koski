describe('TOR', function() {
  var page = TorPage()
  var login = LoginPage()
  var authentication = Authentication()
  var opinnot = OpinnotPage()
  var addOppija = AddOppijaPage()

  var eero = 'Esimerkki, Eero 010101-123N'
  var markkanen = 'Markkanen, Eero '
  var eerola = 'Eerola, Jouni '
  var teija = 'Tekijä, Teija 150995-914X'

  describe('Login-sivu', function() {
    before(login.openPage)
    it('näytetään, kun käyttäjä ei ole kirjautunut sisään', function() {
      expect(login.isVisible()).to.equal(true)
    })
    describe('Väärällä käyttäjätunnuksella', function() {
      before(login.login('fail', 'fail'))
      before(wait.until(login.isLoginErrorVisible))
      it('näytetään virheilmoitus', function() {})
    })
    describe('Väärällä salasanalla', function() {
      before(login.openPage)
      before(login.login('kalle', 'fail'))
      before(wait.until(login.isLoginErrorVisible))
      it('näytetään virheilmoitus', function() {})
    })
    describe('Onnistuneen loginin jälkeen', function() {
      before(login.openPage)
      before(login.login('kalle', 'kalle'))
      before(wait.until(page.isVisible))
      it('siirrytään TOR-etusivulle', function() {
        expect(page.isVisible()).to.equal(true)
      })
      it('näytetään kirjautuneen käyttäjän nimi', function() {
        expect(page.getUserName()).to.equal('kalle käyttäjä')
      })
    })
  })

  describe('Oppijahaku', function() {
    before(authentication.login(), resetFixtures, page.openPage)
    it('näytetään, kun käyttäjä on kirjautunut sisään', function() {
      expect(page.isVisible()).to.equal(true)
      expect(page.oppijaHaku.isNoResultsLabelShown()).to.equal(false)
    })
    describe('Hakutulos-lista', function() {
      it('on aluksi tyhjä', function() {
        expect(page.oppijaHaku.getSearchResults().length).to.equal(0)
      })
    })
    describe('Kun haku tuottaa yhden tuloksen', function() {
      before(page.oppijaHaku.search('esimerkki', 1))
      it('ensimmäinen tulos näytetään', function() {
        expect(page.oppijaHaku.getSearchResults()).to.deep.equal([eero])
        expect(page.oppijaHaku.isNoResultsLabelShown()).to.equal(false)
      })

      it('ensimmäinen tulos valitaan automaattisesti', wait.until(function() { return page.getSelectedOppija() == eero }))

      describe('Kun haku tuottaa uudestaan yhden tuloksen', function() {
        before(page.oppijaHaku.search('teija', 1))
        it('tulosta ei valita automaattisesti', function() {
          expect(page.getSelectedOppija()).to.equal(eero)
        })
      })
    })
    describe('Haun tyhjentäminen', function() {
      before(page.oppijaHaku.search('esimerkki', 1))
      before(page.oppijaHaku.search('', 0))

      it('säilyttää oppijavalinnan', function() {
        expect(page.getSelectedOppija()).to.equal(eero)
      })

      it('tyhjentää hakutulos-listauksen', function() {
        expect(page.oppijaHaku.getSearchResults().length).to.equal(0)
        expect(page.oppijaHaku.isNoResultsLabelShown()).to.equal(false)
      })
    })
    describe('Kun haku tuottaa useamman tuloksen', function() {
      before(page.oppijaHaku.search('eero', 3))

      it('Hakutulokset näytetään', function() {
        expect(page.oppijaHaku.getSearchResults()).to.deep.equal([eerola, eero, markkanen])
      })

      describe('Kun klikataan oppijaa listalla', function() {
        before(page.oppijaHaku.selectOppija('Markkanen'))

        it('Oppija valitaan', function() {
          expect(page.getSelectedOppija()).to.equal(markkanen)
        })
      })
    })

    describe('Kun haku ei tuota tuloksia', function() {
      before(page.oppijaHaku.search('asdf', page.oppijaHaku.isNoResultsLabelShown))

      it('Näytetään kuvaava teksti', function() {
        expect(page.oppijaHaku.isNoResultsLabelShown()).to.equal(true)
      })
    })

    describe('Kun haetaan olemassa olevaa henkilöä, jolla ei ole opinto-oikeuksia', function() {
      before(page.oppijaHaku.search('Presidentti', page.oppijaHaku.isNoResultsLabelShown))

      it('Tuloksia ei näytetä', function() {

      })
    })

    describe('Hakutavat', function() {
      it ('Hetulla, case-insensitive', function() {
        return page.oppijaHaku.search('010101-123n', [eero])()
      })
      it ('Nimen osalla, case-insensitive', function() {
        return page.oppijaHaku.search('JoU', [eerola])()
      })
      it ('Oidilla', function() {
        return page.oppijaHaku.search('1.2.246.562.24.00000000003', [markkanen])()
      })
    })
  })

  function prepareForNewOppija(username, searchString) {
    return function() {
      return resetFixtures()
        .then(authentication.login(username))
        .then(page.openPage)
        .then(page.oppijaHaku.search(searchString, page.oppijaHaku.isNoResultsLabelShown))
        .then(page.oppijaHaku.addNewOppija)
    }
  }

  function addNewOppija(username, searchString, oppijaData) {
    return function() {
      return prepareForNewOppija(username, searchString)()
        .then(addOppija.enterValidData(oppijaData))
        .then(addOppija.submitAndExpectSuccess(oppijaData.hetu, oppijaData.tutkinto))
        .then(OpinnotPage().waitUntilRakenneVisible())
    }
  }

  describe('Opinto-oikeuden lisääminen', function() {
    describe('Olemassa olevalle henkilölle', function() {

      describe('Kun lisätään uusi opinto-oikeus', function() {
        before(addNewOppija('kalle', 'Tunkkila', { etunimet: 'Tero Terde', kutsumanimi: 'Terde', sukunimi: 'Tunkkila', hetu: '091095-9833', oppilaitos: 'Stadin', tutkinto: 'Autoalan'}))

        it('Onnistuu, näyttää henkilöpalvelussa olevat nimitiedot', function() {
          expect(page.getSelectedOppija()).to.equal('Tunkkila-Fagerlund, Tero Petteri Gustaf 091095-9833')
        })
      })

      describe('Kun lisätään opinto-oikeus, joka henkilöllä on jo olemassa', function() {
        before(addNewOppija('kalle', 'kalle', { etunimet: 'Eero Adolf', kutsumanimi: 'Eero', sukunimi: 'Esimerkki', hetu: '010101-123N', oppilaitos: 'Stadin', tutkinto: 'Autoalan'}))

        it('Näytetään olemassa oleva tutkinto', function() {
          expect(page.getSelectedOppija()).to.equal(eero)
          expect(opinnot.getTutkinto()).to.equal('Autoalan perustutkinto')
          expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
        })
      })
    })

    describe('Uudelle henkilölle', function() {
      before(prepareForNewOppija('kalle', 'kalle'))

      describe('Aluksi', function() {
        it('Lisää-nappi on disabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(false)
        })
        it('Tutkinto-kenttä on disabloitu', function() {
          expect(addOppija.tutkintoIsEnabled()).to.equal(false)
        })
      })

      describe('Kun syötetään validit tiedot', function() {
        before(addOppija.enterValidData())

        describe('Käyttöliittymän tila', function() {
          it('Lisää-nappi on enabloitu', function() {
            expect(addOppija.isEnabled()).to.equal(true)
          })
        })

        describe('Kun painetaan Lisää-nappia', function() {
          before(addOppija.submitAndExpectSuccess('Oppija, Ossi Olavi 300994-9694', 'Autoalan perustutkinto'))

          it('lisätty oppija näytetään', function() {})

          it('Lisätty opiskeluoikeus näytetään', function() {
            expect(opinnot.getTutkinto()).to.equal('Autoalan perustutkinto')
            expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
          })
        })
      })

      describe('Kun sessio on vanhentunut', function() {
        before( openPage('/tor/uusioppija', function() {return addOppija.isVisible()}),
          addOppija.enterValidData(),
          authentication.logout,
          addOppija.submit)

        it('Siirrytään login-sivulle', wait.until(login.isVisible))
      })

      describe('Kun hetu on virheellinen', function() {
        before(
          authentication.login(),
          openPage('/tor/uusioppija'),
          wait.until(function() {return addOppija.isVisible()}),
          addOppija.enterValidData({hetu: '123456-1234'})
        )
        it('Lisää-nappi on disabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(false)
        })
      })
      describe('Kun hetu sisältää väärän tarkistusmerkin', function() {
        before(
          addOppija.enterValidData({hetu: '011095-953Z'})
        )
        it('Lisää-nappi on disabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(false)
        })
      })
      describe('Kun hetu sisältää väärän päivämäärän, mutta on muuten validi', function() {
        before(
          addOppija.enterValidData({hetu: '300275-5557'})
        )
        it('Lisää-nappi on disabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(false)
        })
      })
      describe('Kun kutsumanimi ei löydy etunimistä', function() {
        before(
          addOppija.enterValidData({kutsumanimi: 'eiloydy'})
        )
        it('Lisää-nappi on disabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(false)
        })
        it('Näytetään virheilmoitus', function() {
          expect(addOppija.isErrorShown('kutsumanimi')()).to.equal(true)
        })
      })
      describe('Kun kutsumanimi löytyy väliviivallisesta nimestä', function() {
        before(
          addOppija.enterValidData({etunimet: 'Juha-Pekka', kutsumanimi: 'Pekka'})
        )
        it('Lisää-nappi on enabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(true)
        })
      })
      describe('Kun oppilaitos on valittu', function() {
        before(addOppija.enterValidData())
        it('voidaan valita tutkinto', function(){
          expect(addOppija.tutkintoIsEnabled()).to.equal(true)
          expect(addOppija.isEnabled()).to.equal(true)
        })
        describe('Kun oppilaitos-valinta muutetaan', function() {
          before(addOppija.selectOppilaitos('Omnia'))
          it('tutkinto pitää valita uudestaan', function() {
            expect(addOppija.isEnabled()).to.equal(false)
          })
          describe('Tutkinnon valinnan jälkeen', function() {
            before(addOppija.selectTutkinto('auto'))
            it('Lisää nappi on enabloitu', function() {
              expect(addOppija.isEnabled()).to.equal(true)
            })
          })
        })
      })
      describe('Oppilaitosvalinta', function() {
        describe('Näytetään vain käyttäjän organisaatiopuuhun kuuluvat oppilaitokset', function() {
          it('1', function() {
            return prepareForNewOppija('hiiri', 'Tunkkila')()
              .then(addOppija.enterOppilaitos('ammatti'))
              .then(wait.forMilliseconds(500))
              .then(function() {
                expect(addOppija.oppilaitokset()).to.deep.equal(['Omnian ammattiopisto'])
              })
          })
          it('2', function() {
            return prepareForNewOppija('kalle', 'Tunkkila')()
              .then(addOppija.enterOppilaitos('ammatti'))
              .then(wait.forMilliseconds(500))
              .then(function() {
                expect(addOppija.oppilaitokset()).to.deep.equal(['Omnian ammattiopisto', 'Stadin ammattiopisto'])
              })
          })
        })
        describe('Kun oppilaitos on virheellinen', function() {
          before(addOppija.enterValidData(), addOppija.enterOppilaitos('virheellinen'))
          it('Lisää-nappi on disabloitu', function() {
            expect(addOppija.isEnabled()).to.equal(false)
          })
          it('Tutkinnon valinta on estetty', function() {
            expect(addOppija.tutkintoIsEnabled()).to.equal(false)
          })
        })
      })
      describe('Kun tutkinto on virheellinen', function() {
        before(addOppija.enterValidData(), addOppija.enterTutkinto('virheellinen'))
        it('Lisää-nappi on disabloitu', function() {
          expect(addOppija.isEnabled()).to.equal(false)
        })
      })
    })

    describe('Virhetilanteet', function() {
      describe('Kun tallennus epäonnistuu', function() {
        before(
          authentication.login(),
          openPage('/tor/uusioppija', function() {return addOppija.isVisible()}),
          addOppija.enterValidData({sukunimi: "error"}),
          addOppija.submit)

        it('Näytetään virheilmoitus', wait.until(page.isErrorShown))
      })
    })
  })


  describe('Tutkinnon rakenne', function() {
    describe("Ammatillinen perustutkinto", function() {
      before(addNewOppija('kalle', 'Tunkkila', { hetu: '091095-9833'}))

      it('Osaamisala- ja suoritustapavalinnat näytetään', function() {
        expect(opinnot.isSuoritustapaSelectable()).to.equal(true)
        expect(opinnot.isOsaamisalaSelectable()).to.equal(true)
      })
      describe('Kun valitaan osaamisala ja suoritustapa', function() {
        before(opinnot.selectSuoritustapa("ops"), opinnot.selectOsaamisala("1527"))

        it('Näytetään tutkinnon rakenne', function() {
          expect(opinnot.getTutkinnonOsat()[0]).to.equal('Myynti ja tuotetuntemus')
        })
      })
    })

    describe('Erikoisammattitutkinto', function() {
      before(addNewOppija('kalle', 'Tunkkila', { etunimet: 'Tero Terde', kutsumanimi: 'Terde', sukunimi: 'Tunkkila', hetu: '091095-9833', oppilaitos: 'Stadin', tutkinto: 'erikois'}))

      it('Ei näytetä osaamisala- ja suoritustapavalintoja (koska mitään valittavaa ei ole)', function() {
        expect(opinnot.isSuoritustapaSelectable()).to.equal(false)
        expect(opinnot.isOsaamisalaSelectable()).to.equal(false)
      })

      it('Näytetään tutkinnon rakenne', function() {
        expect(opinnot.getTutkinnonOsat()[0]).to.equal('Johtaminen ja henkilöstön kehittäminen')
      })
    })
  })

  describe('Tutkinnon tietojen muuttaminen', function() {
    before(authentication.login(), resetFixtures, page.openPage, addNewOppija('kalle', 'Tunkkila', { hetu: '091095-9833'}))
    it('Aluksi ei näytetä \"Kaikki tiedot tallennettu\" -tekstiä', function() {
      expect(page.isSavedLabelShown()).to.equal(false)
    })

    describe('Kun valitaan osaamisala ja suoritustapa', function() {
      before(opinnot.selectSuoritustapa("ops"), opinnot.selectOsaamisala("1527"))

      describe('Muutosten näyttäminen', function() {
        before(wait.until(page.isSavedLabelShown))
        it('Näytetään "Kaikki tiedot tallennettu" -teksti', function() {
          expect(page.isSavedLabelShown()).to.equal(true)
        })
      })

      describe('Kun sivu ladataan uudelleen', function() {
        before( page.oppijaHaku.search('ero', 4),
                page.oppijaHaku.selectOppija('Tunkkila'), opinnot.waitUntilRakenneVisible())

        it('Muuttuneet tiedot on tallennettu', function() {
          expect(opinnot.getTutkinnonOsat()[0]).to.equal('Myynti ja tuotetuntemus')
        })
      })
    })

    describe('Kun annetaan arviointi tutkinnonosalle', function() {
      describe('Arvion antaminen käyttöliittymän kautta', function() {
        describe('OPS-muotoinen, asteikko T1-K3', function() {
          before(opinnot.selectSuoritustapa("ops"), opinnot.selectOsaamisala("1527"))
          var tutkinnonOsa = opinnot.getTutkinnonOsa("Markkinointi ja asiakaspalvelu")
          before(tutkinnonOsa.addArviointi("H2"))
          it('Uusi arviointi näytetään', function() {
            expect(tutkinnonOsa.getArvosana()).to.equal("H2")
          })

          describe('Kun sivu ladataan uudelleen', function() {
            before( page.oppijaHaku.search('ero', 4),
              page.oppijaHaku.selectOppija('Tunkkila'), opinnot.waitUntilRakenneVisible())

            it('Muuttuneet tiedot on tallennettu', function() {
              expect(tutkinnonOsa.getArvosana()).to.equal("H2")
            })
          })
        })
        describe('Näyttömuotoinen, asteikko HYVÄKSYTTY/HYLÄTTY', function() {
          before(opinnot.selectSuoritustapa("naytto"), opinnot.selectOsaamisala("1527"))
          var tutkinnonOsa = opinnot.getTutkinnonOsa("Myynti ja tuotetuntemus")
          before(tutkinnonOsa.addArviointi("Hylätty"))
          it('Uusi arviointi näytetään', function() {
            expect(tutkinnonOsa.getArvosana()).to.equal("Hylätty")
          })

          describe('Kun sivu ladataan uudelleen', function() {
            before( page.oppijaHaku.search('ero', 4),
              page.oppijaHaku.selectOppija('Tunkkila'), opinnot.waitUntilRakenneVisible())

            it('Muuttuneet tiedot on tallennettu', function() {
              expect(tutkinnonOsa.getArvosana()).to.equal("Hylätty")
            })
          })
        })
      })
    })

    describe('Virhetilanteet', function() {
      verifyErrorMessage('Kun tallennus epäonnistuu', 500, 'Järjestelmässä tapahtui odottamaton virhe. Yritä myöhemmin uudelleen.')
      verifyErrorMessage('Kun toinen käyttäjä on tehnyt muutoksen', 409, 'Muutoksia ei voida tallentaa, koska toinen käyttäjä on muuttanut tietoja sivun latauksen jälkeen. Lataa sivu uudelleen.')

      function verifyErrorMessage(desc, statusCode, message) {
        describe(desc, function() {
          before(
            mockHttp("/tor/api/oppija", { status: statusCode }),
            opinnot.selectOsaamisala("1622"),
            wait.until(page.isErrorShown)
          )

          it('Näytetään virheilmoitus', function() {
            expect(page.getErrorMessage()).to.equal(message)
          })
        })
      }
    })
  })

  describe('Korkeakoulujen opiskeluoikeudet', function() {
    var opintosuoritusote = OpintosuoritusotePage()
    before(
      resetFixtures,
      authentication.login()
    )
    describe('Valmis diplomi-insinööri', function() {
      before(
        page.openPage,
        page.oppijaHaku.search('290492-9455', page.isOppijaSelected('Dilbert'))
      )
      describe('Oppilaitos ja tutkinto', function() {
        it('näytetään', function() {
          expect(OpinnotPage().getTutkinto()).to.equal('Dipl.ins., konetekniikka')
          expect(OpinnotPage().getOppilaitos()).to.equal('Aalto-yliopisto')
          expect(OpinnotPage().getOpintoOikeus()).to.equal('(Opiskeluoikeus päättynyt, Suoritus valmis)')
        })
      })
      describe('Opintosuoritusote', function() {
        before(OpinnotPage().avaaOpintosuoritusote(1))

        it('näytetään', function() {
        })
      })
    })
    describe('Maisteri, jolla ensisijainen opiskeluoikeus', function() {
      before(
        page.openPage,
        page.oppijaHaku.search('090888-929X', page.isOppijaSelected('Harri'))
      )
      describe('Oppilaitos ja tutkinto', function() {
        it('näytetään', function() {
          expect(OpinnotPage().getTutkinto(1)).to.equal('Dipl.ins., kemian tekniikka')
          expect(OpinnotPage().getOppilaitos(1)).to.equal('Helsingin yliopisto')
          expect(OpinnotPage().getOpintoOikeus(1)).to.equal('(Opiskeluoikeus passivoitu, Suoritus kesken)')
        })
      })
      describe('Opiskeluoikeus', function() {
        before(OpinnotPage().avaaOpintosuoritusote(2))
        it('näytetään', function() {
          expect(S('section.opiskeluoikeus h3').text()).to.equal('Ensisijainen opinto-oikeus')
        })
      })
    })
    describe('Keskeneräinen tutkinto', function() {
      before(
        page.openPage,
        page.oppijaHaku.search('010675-9981', page.isOppijaSelected('Kikka')),
        OpinnotPage().avaaOpintosuoritusote(1)
      )
      it('näytetään', function() {
        expect(S('section.opiskeluoikeus h3').text()).to.equal('Ensisijainen opinto-oikeus')
      })
    })
    describe('AMK, keskeyttänyt', function() {
      before(
        page.openPage,
        page.oppijaHaku.search('100193-948U', page.isOppijaSelected('Valtteri'))
      )
      describe('Oppilaitos ja tutkinto', function() {
        it('näytetään', function() {
          expect(OpinnotPage().getTutkinto()).to.equal('Ensihoitaja (AMK)')
          expect(OpinnotPage().getOppilaitos()).to.equal('Yrkeshögskolan Arcada')
          expect(OpinnotPage().getOpintoOikeus()).to.equal('(Opiskeluoikeus luopunut, Suoritus kesken)')
        })
      })
    })
  })

  describe('Todistukset', function( ){
    describe('Perusopetuksen päättötodistus', function() {
      var todistus = TodistusPage()
      before(resetFixtures, authentication.login())
      before(openPage('/tor/oppija/1.2.246.562.24.00000000008', page.isOppijaSelected('Kaisa')))
      describe('Oppijan suorituksissa', function() {
        it('näytetään', function() {
          expect(OpinnotPage().getTutkinto()).to.equal("Peruskoulu")
          expect(OpinnotPage().getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
        })
      })
      describe('Tulostettava todistus', function() {
        before(
          function() { triggerEvent(S('a.todistus'), 'click') },
          wait.until(todistus.isVisible)
        )
        it('näytetään', function() {
          expect(todistus.headings()).to.equal('Jyväskylän yliopistoPerusopetuksen päättötodistusJyväskylän normaalikoulu Koululainen, Kaisa 110496-926Y')
          expect(todistus.arvosanarivi('.oppiaine.KT')).to.equal('Uskonto tai elämänkatsomustieto, Evankelisluterilainen uskonto Erinomainen 10')
          expect(todistus.arvosanarivi('.oppiaine.KO.valinnainen')).to.equal('Valinnainen kotitalous 1.0 Hyväksytty')
          expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
        })
      })
    })

    describe('Lukion päättötodistus', function() {
      var todistus = TodistusPage()
      before(resetFixtures, authentication.login())
      before(openPage('/tor/oppija/1.2.246.562.24.00000000009', page.isOppijaSelected('Liisa')))
      describe('Oppijan suorituksissa', function() {
        it('näytetään', function() {
          expect(OpinnotPage().getTutkinto()).to.equal("Ylioppilastutkinto")
          expect(OpinnotPage().getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
        })
      })
      describe('Tulostettava todistus', function() {
        before(
          function() { triggerEvent(S('a.todistus'), 'click') },
          wait.until(todistus.isVisible)
        )
        it('näytetään', function() {
          expect(todistus.headings()).to.equal('Lukion päättötodistusJyväskylän yliopistoJyväskylän normaalikoulu Lukiolainen, Liisa 110496-9369')
          expect(todistus.arvosanarivi('.oppiaine.KT')).to.equal('Uskonto tai elämänkatsomustieto, Evankelisluterilainen uskonto 3 Hyvä 8')
          expect(todistus.arvosanarivi('.oppiaine.MA')).to.equal('Matematiikka, pitkä oppimäärä 15 Kiitettävä 9')
          expect(todistus.arvosanarivi('.kurssimaara')).to.equal('Opiskelijan suorittama kokonaiskurssimäärä 87,5')
          expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
        })
      })
    })

    describe('Ammatillisen perustutkinnon päättötodistus', function() {
      var todistus = TodistusPage()
      before(resetFixtures, authentication.login())
      before(openPage('/tor/oppija/1.2.246.562.24.000000000010', page.isOppijaSelected('Aarne')))
      describe('Oppijan suorituksissa', function() {
        it('näytetään', function() {
          expect(OpinnotPage().getTutkinto()).to.equal("Luonto- ja ympäristöalan perustutkinto")
          expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
        })
      })
      describe('Tulostettava todistus', function() {
        before(
          function() { triggerEvent(S('a.todistus'), 'click') },
          wait.until(todistus.isVisible)
        )
        it('näytetään', function() {
          expect(todistus.headings()).to.equal('HELSINGIN KAUPUNKIStadin ammattiopistoPäättötodistusLuonto- ja ympäristöalan perustutkintoYmpäristöalan osaamisala, Ympäristönhoitaja Ammattilainen, Aarne (120496-949B)')
          expect(todistus.arvosanarivi('.tutkinnon-osa.100431')).to.equal('Kestävällä tavalla toimiminen 40 Kiitettävä 3')
          expect(todistus.arvosanarivi('.opintojen-laajuus')).to.equal('Opiskelijan suorittamien tutkinnon osien laajuus osaamispisteinä 180')
          expect(todistus.vahvistus()).to.equal('Helsinki 31.5.2016 Keijo Perttilä rehtori')
        })
      })
    })
  })

  describe('Navigointi suoraan oppijan sivulle', function() {
    before(
      resetFixtures,
      authentication.login(),
      openPage('/tor/oppija/1.2.246.562.24.00000000001', page.isOppijaSelected('Eero')),
      opinnot.waitUntilRakenneVisible()
    )

    it('Oppijan tiedot näytetään', function() {
      expect(page.getSelectedOppija()).to.equal(eero)
    })

    it('Oppijan tutkinto ja oppilaitos näytetään', function() {
      expect(opinnot.getTutkinto()).to.equal('Autoalan perustutkinto')
      expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
    })

    it('Hakutulos näytetään', function() {
      expect(page.oppijaHaku.getSearchResults()).to.deep.equal([eero])
    })
  })

  describe('Virhetilanteet', function() {
    before(
      authentication.login(),
      resetFixtures
    )

    describe('Odottamattoman virheen sattuessa', function() {
      before(
        page.openPage,
        page.oppijaHaku.search('error', page.isErrorShown))

      it('näytetään virheilmoitus', function() {})
    })

    describe('Kun palvelimeen ei saada yhteyttä', function() {
      before(
        page.openPage,
        mockHttp('/tor/api/oppija/search?query=blah', {}),
        page.oppijaHaku.search('blah', page.isErrorShown))

      it('näytetään virheilmoitus', function() {})
    })


    describe('Kun sivua ei löydy', function() {
      before(authentication.login(), openPage('/tor/asdf', page.is404))

      it('näytetään 404-sivu', function() {})
    })
  })

  describe('Käyttöoikeudet', function() {
    describe('Oppijahaku', function() {
      before(authentication.login('hiiri'), page.openPage, page.oppijaHaku.search('eero', [markkanen]))

      it('Näytetään vain ne oppijat, joiden opinto-oikeuksiin liittyviin organisaatioihin on käyttöoikeudet', function() {

      })
    })

    describe('Navigointi oppijan sivulle', function() {
      before(authentication.login('hiiri'), openPage('/tor/oppija/1.2.246.562.24.00000000002', page.is404))

      it('Estetään jos oppijalla ei opinto-oikeutta, joihin käyttäjällä on katseluoikeudet', function() {

      })
    })
  })

  describe('Tietoturva', function() {
    before(login.openPage)

    describe('Kun klikataan logout-linkkiä', function() {
      before(authentication.login(), page.openPage, page.logout)

      it('Siirrytään login-sivulle', function() {
        expect(login.isVisible()).to.equal(true)
      })

      describe('Kun ladataan sivu uudelleen', function() {
        before(openPage('/tor', login.isVisible))

        it('Sessio on päättynyt ja login-sivu näytetään', function() {
          expect(login.isVisible()).to.equal(true)
        })
      })

      describe('Kun kirjaudutaan uudelleen sisään', function() {
        before(authentication.login(), page.openPage, page.oppijaHaku.search('jouni', [eerola]), page.logout, login.login('kalle', 'kalle'), wait.until(page.isReady))
        it ('Käyttöliittymä on palautunut alkutilaan', function() {
          expect(page.oppijaHaku.getSearchResults()).to.deep.equal([])
          expect(page.getSelectedOppija()).to.equal('')
        })
      })
    })

    describe('Session vanhennuttua', function() {
      before(authentication.login(), page.openPage, authentication.logout, page.oppijaHaku.search('eero', login.isVisible))

      it('Siirrytään login-sivulle', function() {
        expect(login.isVisible()).to.equal(true)
      })
    })
  })

  function resetFixtures() {
    return Q($.ajax({ url: '/tor/fixtures/reset', method: 'post'}))
  }

  function mockHttp(url, result) {
    return function() { testFrame().http.mock(url, result) }
  }
})