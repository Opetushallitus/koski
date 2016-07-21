describe('Ammatillinen koulutus', function() {
  before(Authentication().login())
  
  var addOppija = AddOppijaPage()
  var page = KoskiPage()
  var login = LoginPage()
  var opinnot = OpinnotPage()
  var eero = 'Esimerkki, Eero 010101-123N'


  function prepareForNewOppija(username, searchString) {
    return function() {
      return Authentication().login(username)()
        .then(resetFixtures)
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
        before( openPage('/koski/uusioppija', function() {return addOppija.isVisible()}),
          addOppija.enterValidData(),
          Authentication().logout,
          addOppija.submit)

        it('Siirrytään login-sivulle', wait.until(login.isVisible))
      })

      describe('Kun hetu on virheellinen', function() {
        before(
          Authentication().login(),
          openPage('/koski/uusioppija'),
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
                expect(addOppija.oppilaitokset()).to.deep.equal(['Lahden ammattikorkeakoulu', 'Omnian ammattiopisto', 'Stadin ammattiopisto'])
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
          Authentication().login(),
          openPage('/koski/uusioppija', function() {return addOppija.isVisible()}),
          addOppija.enterValidData({sukunimi: "error"}),
          addOppija.submit)

        it('Näytetään virheilmoitus', wait.until(page.isErrorShown))
      })
    })
  })

  describe('Tutkinnon tietojen muuttaminen', function() {
    before(Authentication().login(), resetFixtures, page.openPage, addNewOppija('kalle', 'Tunkkila', { hetu: '091095-9833'}))
    it('Aluksi ei näytetä \"Kaikki tiedot tallennettu\" -tekstiä', function() {
      expect(page.isSavedLabelShown()).to.equal(false)
    })

    describe('Kun valitaan suoritustapa', function() {
      //before(opinnot.selectSuoritustapa("ops"), opinnot.selectOsaamisala("1527"))
      var suoritus = opinnot.suoritus('Autoalan perustutkinto')
      var suoritustapa = suoritus.property('suoritustapa')
      before(suoritus.expand, suoritus.edit, suoritustapa.addValue, suoritustapa.waitUntilLoaded, suoritustapa.setValue('ops'))

      describe('Muutosten näyttäminen', function() {
        before(wait.until(page.isSavedLabelShown))
        it('Näytetään "Kaikki tiedot tallennettu" -teksti', function() {
          expect(page.isSavedLabelShown()).to.equal(true)
        })
      })

      describe('Kun sivu ladataan uudelleen', function() {
        before(
          page.oppijaHaku.search('ero', 4),
          page.oppijaHaku.selectOppija('Tunkkila'),
          suoritus.expand
        )

        it('Muuttuneet tiedot on tallennettu', function() {
          expect(suoritustapa.getValue()).to.equal('ops')
        })
      })
    })
  })


  describe('Ammatillisen perustutkinnon päättötodistus', function() {
    before(resetFixtures, page.openPage, page.oppijaHaku.search('120496-949B', page.isOppijaSelected('Aarne')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Luonto- ja ympäristöalan perustutkinto")
        expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.työtehtävät .value').text()).to.equal('Toimi harjoittelijana Sortti-asemalla')
      })
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus(0))
      it('näytetään', function() {
        expect(TodistusPage().headings()).to.equal('HELSINGIN KAUPUNKIStadin ammattiopistoPäättötodistusLuonto- ja ympäristöalan perustutkintoYmpäristöalan osaamisala, Ympäristönhoitaja Ammattilainen, Aarne (120496-949B)')
        expect(TodistusPage().arvosanarivi('.tutkinnon-osa.100431')).to.equal('Kestävällä tavalla toimiminen 40 Kiitettävä 3')
        expect(TodistusPage().arvosanarivi('.opintojen-laajuus')).to.equal('Opiskelijan suorittamien tutkinnon osien laajuus osaamispisteinä 180')
        expect(TodistusPage().vahvistus()).to.equal('Helsinki 31.5.2016 Reijo Reksi rehtori')
      })
    })
  })

  describe('Näyttötutkinnot', function() {
    before(Authentication().login(), resetFixtures, page.openPage, page.oppijaHaku.search('200696-906R', page.isOppijaSelected('Erja')))
    describe('Näyttötutkintoon valmistava koulutus', function() {
      describe('Oppijan suorituksissa', function() {
        it('näytetään', function() {
          expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
          expect(OpinnotPage().getTutkinto(0)).to.equal("Näyttötutkintoon valmistava koulutus")
        })
      })

      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('toimii', function() {
          expect(S('.nayttotutkintoonvalmistavankoulutuksensuoritus .osasuoritukset .koulutusmoduuli .value').text()).to.equal('Johtaminen ja henkilöstön kehittäminen')
        })
      })

      describe('Tulostettava todistus', function() {
        before(OpinnotPage().avaaTodistus(0))
        it('näytetään', function() {
          expect(TodistusPage().vahvistus()).to.equal('Helsinki 31.5.2015 Reijo Reksi rehtori')
        })
      })
    })

    describe('Erikoisammattitutkinto', function() {
      before(TodistusPage().close, wait.until(page.isOppijaSelected('Erja')))
      describe('Oppijan suorituksissa', function() {
        it('näytetään', function() {
          expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
          expect(OpinnotPage().getTutkinto(1)).to.equal("Autoalan työnjohdon erikoisammattitutkinto")
        })
      })

      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('toimii', function() {
          expect(S('.tutkinnonosa .koulutusmoduuli .value').eq(1).text()).to.equal('Asiakaspalvelu ja korjaamopalvelujen markkinointi')
        })
      })

      describe('Tulostettava todistus', function() {
        before(OpinnotPage().avaaTodistus(1))
        it('näytetään', function() {
          expect(TodistusPage().vahvistus()).to.equal('Helsinki 31.5.2016 Reijo Reksi rehtori')
        })
      })
    })
  })

  describe('Ammatilliseen peruskoulutukseen valmentava koulutus', function() {
    before(page.openPage, page.oppijaHaku.search('160696-993Y', page.isOppijaSelected('Anneli')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
        expect(OpinnotPage().getTutkinto()).to.equal("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
      })
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in ValmaSpec.scala
        expect(TodistusPage().vahvistus()).to.equal('Helsinki 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })
})