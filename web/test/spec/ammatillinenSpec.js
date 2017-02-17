describe('Ammatillinen koulutus', function() {
  before(Authentication().login())
  
  var addOppija = AddOppijaPage()
  var page = KoskiPage()
  var login = LoginPage()
  var opinnot = OpinnotPage()
  var eero = 'Esimerkki, Eero (010101-123N)'


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

      describe('Kun lisätään uusi opiskeluoikeus', function() {
        before(addNewOppija('kalle', 'Tunkkila', { etunimet: 'Tero Terde', kutsumanimi: 'Terde', sukunimi: 'Tunkkila', hetu: '280608-6619', oppilaitos: 'Stadin', tutkinto: 'Autoalan'}))

        it('Onnistuu, näyttää henkilöpalvelussa olevat nimitiedot', function() {
          expect(page.getSelectedOppija()).to.equal('Tunkkila-Fagerlund, Tero Petteri Gustaf (280608-6619)')
        })
      })

      describe('Kun lisätään opiskeluoikeus, joka henkilöllä on jo olemassa', function() {
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
          before(addOppija.submitAndExpectSuccess('Oppija, Ossi Olavi (151161-075P)', 'Autoalan perustutkinto'))

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
            return prepareForNewOppija('omnia-palvelukäyttäjä', 'Tunkkila')()
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

  describe('Tietojen muuttaminen', function() {
    before(resetFixtures, page.openPage, addNewOppija('kalle', 'Tunkkila', { hetu: '280608-6619'}))
    it('Aluksi ei näytetä \"Kaikki tiedot tallennettu\" -tekstiä', function() {
      expect(page.isSavedLabelShown()).to.equal(false)
    })

    describe('Kun valitaan suoritustapa', function() {
      var suoritus = opinnot.suoritusEditor()
      var suoritustapa = suoritus.property('suoritustapa')
      before(suoritus.edit, suoritustapa.addValue, suoritustapa.waitUntilLoaded, suoritustapa.setValue('ops'), wait.until(page.isSavedLabelShown))

      describe('Muutosten näyttäminen', function() {
        it('Näytetään "Kaikki tiedot tallennettu" -teksti', function() {
          expect(suoritustapa.isVisible()).to.equal(true)
          expect(page.isSavedLabelShown()).to.equal(true)
        })
      })

      describe('Palattaessa tietojen katseluun', function() {
        before(suoritus.doneEditing)
        it('Näytetään muuttuneet tiedot', function() {
          expect(suoritustapa.getValue()).to.equal('Opetussuunnitelman mukainen')
        })
      })

      describe('Kun sivu ladataan uudelleen', function() {
        before(
          page.openPage,
          page.oppijaHaku.search('Tunkkila-Fagerlund', 1),
          page.oppijaHaku.selectOppija('Tunkkila-Fagerlund')
        )

        it('Muuttuneet tiedot on tallennettu', function() {
          expect(suoritustapa.getValue()).to.equal('Opetussuunnitelman mukainen')
        })
      })

      describe('Kun poistetaan suoritustapa', function() {
        before(suoritus.edit, suoritustapa.removeValue, wait.until(page.isSavedLabelShown), suoritus.doneEditing)
        it('Näytetään muuttuneet tiedot', function() {
          expect(suoritustapa.isVisible()).to.equal(false)
        })
      })
    })

    describe('Muokkaus', function() {
      describe('Ulkoisen järjestelmän data', function() {
        before(page.openPage, page.oppijaHaku.searchAndSelect('010675-9981'))
        it('estetty', function() {
          expect(opinnot.anythingEditable()).to.equal(false)
        })
      })

      describe('Ilman kirjoitusoikeuksia', function() {
        before(Authentication().logout, Authentication().login('omnia-katselija'), page.openPage, page.oppijaHaku.searchAndSelect('080154-770R'))
        it('estetty', function() {
          var suoritus = opinnot.suoritusEditor()
          expect(suoritus.isEditable()).to.equal(false)
        })
      })
    })
  })

  describe('Ammatillisen perustutkinnon päättötodistus', function() {
    before(Authentication().login(), resetFixtures, page.openPage, page.oppijaHaku.searchAndSelect('280618-402H'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Luonto- ja ympäristöalan perustutkinto")
        expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('näyttää opiskeluoikeuden tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Alkamispäivä : 1.9.2012 — Päättymispäivä : 31.5.2016\n' +
          'Tila 31.5.2016 Valmistunut\n' +
          '1.9.2012 Läsnä')
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Luonto- ja ympäristöalan perustutkinto 62/011/2014\n' +
          'Tutkintonimike Ympäristönhoitaja\nOsaamisala Ympäristöalan osaamisala\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Suorituskieli suomi\n' +
          'Suoritustapa Opetussuunnitelman mukainen\n' +
          'Järjestämismuoto Koulutuksen järjestäminen lähiopetuksena, etäopetuksena tai työpaikalla\n' +
          'Suoritus: VALMIS Vahvistus : 31.5.2016 Helsinki Reijo Reksi')
      })

      it('näyttää tutkinnon osat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Tutkinnon osa Pakollisuus Laajuus Arvosana\n' +
          'Kestävällä tavalla toimiminen kyllä 40 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Työssäoppimisjaksot 1.1.2014 — 15.3.2014 Jyväskylä , Suomi\n' +
          'Työtehtävät Toimi harjoittelijana Sortti-asemalla\n' +
          'Laajuus 5 osp\n' +
          'Ympäristön hoitaminen kyllä 35 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Näyttö\n' +
          'Kuvaus Muksulan päiväkodin ympäristövaikutusten arvioiminen ja ympäristön kunnostustöiden\n' +
          'tekeminen sekä mittauksien tekeminen ja näytteiden ottaminen\n' +
          'Suorituspaikka Muksulan päiväkoti, Kaarinan kunta\n' +
          'Suoritusaika 1.2.2016 — 1.2.2016\n' +
          'Arvosana kiitettävä\n' +
          'Arviointipäivä 20.10.2014\n' +
          'Arvioitsijat Jaana Arstila (näyttötutkintomestari) Pekka Saurmann (näyttötutkintomestari) Juhani Mykkänen\n' +
          'Arviointikohteet Arviointikohde Arvosana\n' +
          'Työprosessin hallinta kiitettävä\n' +
          'Työmenetelmien, -välineiden ja materiaalin hallinta hyvä\n' +
          'Työn perustana olevan tiedon hallinta hyvä\n' +
          'Elinikäisen oppimisen avaintaidot kiitettävä\n' +
          'Arvioinnista päättäneet Opettaja\n' +
          'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
          'Työssäoppimisen yhteydessä ei\n' +
          'Uusiutuvien energialähteiden hyödyntäminen kyllä 15 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Ulkoilureittien rakentaminen ja hoitaminen kyllä 15 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Kulttuuriympäristöjen kunnostaminen ja hoitaminen kyllä 15 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Näyttö\n' +
          'Kuvaus Sastamalan kunnan kulttuuriympäristöohjelmaan liittyvän Wanhan myllyn lähiympäristön\n' +
          'kasvillisuuden kartoittamisen sekä ennallistamisen suunnittelu ja toteutus\n' +
          'Suorituspaikka Sastamalan kunta\n' +
          'Suoritusaika 1.3.2016 — 1.3.2016\n' +
          'Arvosana kiitettävä\n' +
          'Arviointipäivä 20.10.2014\n' +
          'Arvioitsijat Jaana Arstila (näyttötutkintomestari) Pekka Saurmann (näyttötutkintomestari) Juhani Mykkänen\n' +
          'Arviointikohteet Arviointikohde Arvosana\n' +
          'Työprosessin hallinta kiitettävä\n' +
          'Työmenetelmien, -välineiden ja materiaalin hallinta hyvä\n' +
          'Työn perustana olevan tiedon hallinta hyvä\n' +
          'Elinikäisen oppimisen avaintaidot kiitettävä\n' +
          'Arvioinnista päättäneet Opettaja\n' +
          'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
          'Työssäoppimisen yhteydessä ei\n' +
          'Vesistöjen kunnostaminen ja hoitaminen kyllä 15 osp Hyväksytty\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Lisätiedot Tunniste Kuvaus\n' +
          'Muutos arviointiasteikossa Tutkinnon osa on koulutuksen järjestäjän päätöksellä arvioitu asteikolla hyväksytty/hylätty.\n' +
          'Näyttö\n' +
          'Kuvaus Uimarin järven tilan arviointi ja kunnostus\n' +
          'Suorituspaikka Vesipojat Oy\n' +
          'Suoritusaika 1.4.2016 — 1.4.2016\n' +
          'Arvosana kiitettävä\n' +
          'Arviointipäivä 20.10.2014\n' +
          'Arvioitsijat Jaana Arstila (näyttötutkintomestari) Pekka Saurmann (näyttötutkintomestari) Juhani Mykkänen\n' +
          'Arviointikohteet Arviointikohde Arvosana\n' +
          'Työprosessin hallinta kiitettävä\n' +
          'Työmenetelmien, -välineiden ja materiaalin hallinta hyvä\n' +
          'Työn perustana olevan tiedon hallinta hyvä\n' +
          'Elinikäisen oppimisen avaintaidot kiitettävä\n' +
          'Arvioinnista päättäneet Opettaja\n' +
          'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
          'Työssäoppimisen yhteydessä ei\n' +
          'Viestintä- ja vuorovaikutusosaaminen kyllä 11 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Matemaattis-luonnontieteellinen osaaminen kyllä 9 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Lisätiedot Tunniste Kuvaus\n' +
          'Arvioinnin mukauttaminen Tutkinnon osan ammattitaitovaatimuksia tai osaamistavoitteita ja osaamisen arviointia on mukautettu ammatillisesta peruskoulutuksesta annetun lain (630/1998, muutos 246/2015) 19 a tai 21 §:n perusteella\n' +
          'Yhteiskunnassa ja työelämässä tarvittava osaaminen kyllä 8 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Sosiaalinen ja kulttuurinen osaaminen kyllä 7 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Matkailuenglanti ei 5 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi\n' +
          'Sosiaalinen ja kulttuurinen osaaminen ei 5 osp kiitettävä\n' +
          'Toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi'
        )
      })
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus(0))
      it('näytetään', function() {
        expect(TodistusPage().headings()).to.equal('HELSINGIN KAUPUNKIStadin ammattiopistoPäättötodistusLuonto- ja ympäristöalan perustutkintoYmpäristöalan osaamisala, Ympäristönhoitaja Ammattilainen, Aarne (280618-402H)')
        expect(TodistusPage().arvosanarivi('.tutkinnon-osa.100431')).to.equal('Kestävällä tavalla toimiminen 40 Kiitettävä 3')
        expect(TodistusPage().arvosanarivi('.opintojen-laajuus')).to.equal('Opiskelijan suorittamien tutkinnon osien laajuus osaamispisteinä 180')
        expect(TodistusPage().vahvistus()).to.equal('Helsinki 31.5.2016 Reijo Reksi rehtori')
      })
    })
  })

  describe('Näyttötutkinnot', function() {
    before(Authentication().login(), resetFixtures, page.openPage, page.oppijaHaku.searchAndSelect('250989-419V'), OpinnotPage().valitseSuoritus('Näyttötutkintoon valmistava koulutus'))
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
          expect(S('.nayttotutkintoonvalmistavankoulutuksensuoritus .osasuoritukset td.tutkinnonosa .nimi').eq(0).text()).to.equal('Johtaminen ja henkilöstön kehittäminen')
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
      before(TodistusPage().close, wait.until(page.isOppijaSelected('Erja')), OpinnotPage().valitseSuoritus('Autoalan työnjohdon erikoisammattitutkinto'))
      describe('Oppijan suorituksissa', function() {
        it('näytetään', function() {
          expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
          expect(OpinnotPage().getTutkinto()).to.equal("Autoalan työnjohdon erikoisammattitutkinto")
        })
      })

      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('toimii', function() {
          expect(S('.osasuoritukset td.tutkinnonosa .nimi').eq(1).text()).to.equal('Asiakaspalvelu ja korjaamopalvelujen markkinointi')
        })
      })

      describe('Tulostettava todistus', function() {
        before(OpinnotPage().avaaTodistus())
        it('näytetään', function() {
          expect(TodistusPage().vahvistus()).to.equal('Helsinki 31.5.2016 Reijo Reksi rehtori')
        })
      })
    })
  })

  describe('Ammatilliseen peruskoulutukseen valmentava koulutus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('130404-054C'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getOppilaitos()).to.equal("Stadin ammattiopisto")
        expect(OpinnotPage().getTutkinto()).to.equal("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
      })
    })

    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.osasuoritukset td.tutkinnonosa .nimi').eq(0).text()).to.equal('Ammatilliseen koulutukseen orientoituminen ja työelämän perusvalmiuksien hankkiminen')
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