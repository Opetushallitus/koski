describe('Huollettavien tiedot', function () {
  var omattiedot = OmatTiedotPage()
  var opinnot = OpinnotPage()
  var authentication = Authentication()
  var etusivu = LandingPage()
  var korhopankki = KorhoPankki()
  var huollettavantiedot = omattiedot.huollettavientiedotForm

  // Ks. huoltaja -> huollettava mäppäys metodista fi.oph.koski.omattiedot.MockValtuudetClient.findOppija

  describe('Aluksi', function () {
    before(authentication.logout, etusivu.openPage, etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('280598-2415'), wait.until(omattiedot.isVisible))

    it('näytetään omat tiedot', function () {
      verifyOppija('Opintoni', 'Aini Aikuisopiskelija\ns. 28.5.1998', ['Jyväskylän normaalikoulu'], ['Aikuisten perusopetuksen oppimäärä (2008—2016, valmistunut)'])
    })

    describe('Kun painetaan Huollettavien opintotiedot-nappia', function () {
      before(click(omattiedot.huollettavantiedotButton))

      it('näytetään lomake', function () {
        expect(huollettavantiedot.contentsAsText()).to.equal(
          'Jos sinulla on huollettavia, voit tarkastella myös heidän opintotietojaan. Tarkastelua varten sinut ohjataan suomi.fi-valtuudet palveluun.\n' +
          'Tarkastele huollettavasi tietoja'
        )
      })

      describe('Kun painetaan Tarkastele huollettavasi tietoja-nappia ja palataan suomifi-valtuudet palvelusta', function () {
        before(wait.until(huollettavantiedot.isVisible), click(huollettavantiedot.tarkasteleHuollettavasiTietojaButton), wait.until(omattiedot.huollettavanTiedotNäkyvissä))

        it('näytetään huollettavan tiedot', function () {
          verifyOppija('Huollettavani opinnot', 'Ynjevi Ylioppilaslukiolainen\ns. 8.6.1998', ['Jyväskylän normaalikoulu'], ['Ylioppilastutkinto', 'Lukion oppimäärä (2012—2016, valmistunut)'])
        })

        describe('Ylioppilastutkinnon koesuoritukset', function () {
          before(
            opinnot.valitseOmatTiedotOpiskeluoikeus('Ylioppilastutkinto')
          )

          it('näytetään', function () {
            expect(extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))).to.equal(
              'Tutkintokerta Koe Pisteet Arvosana\n' +
              '2012 kevät Äidinkielen koe, suomi 46 Lubenter approbatur Näytä koesuoritus\n' +
              '2012 kevät Ruotsi, keskipitkä oppimäärä 166 Cum laude approbatur Näytä koesuoritus\n' +
              '2012 kevät Englanti, pitkä oppimäärä 210 Cum laude approbatur Näytä koesuoritus\n' +
              '2012 kevät Maantiede 26 Magna cum laude approbatur Näytä koesuoritus\n' +
              '2012 kevät Matematiikan koe, lyhyt oppimäärä 59 Laudatur Näytä koesuoritus'
            )
            expect(findFirst('.koesuoritus a')().attr('href')).to.equal('/koski/koesuoritus/2345K_XX_12345.pdf?huollettava=true')
          })
        })

        describe('Kun painetaan "Palaa omiin opintotietoihin"-linkkiä', function () {
          before(click(omattiedot.palaaOmiinTietoihin), wait.until(omattiedot.omatTiedotNäkyvissä))

          it('näytetään omat tiedot', function () {
            verifyOppija('Opintoni', 'Aini Aikuisopiskelija\ns. 28.5.1998', ['Jyväskylän normaalikoulu'], ['Aikuisten perusopetuksen oppimäärä (2008—2016, valmistunut)'])
          })
        })

        describe('Sivun uudelleenlataaminen ei aiheuta virhettä', function () {
          before(click(omattiedot.huollettavantiedotButton), wait.until(huollettavantiedot.isVisible), click(huollettavantiedot.tarkasteleHuollettavasiTietojaButton), wait.until(omattiedot.huollettavanTiedotNäkyvissä))
          before(reloadTestFrame, wait.forMilliseconds(function() { return isElementVisible(S('.oppija-content'))}))
          it('ei aiheuta virhettä', function () {
            expect(isElementVisible(S('.varoitus'))).to.equal(false)
            verifyOppija('Huollettavani opinnot', 'Ynjevi Ylioppilaslukiolainen\ns. 8.6.1998', ['Jyväskylän normaalikoulu'], ['Ylioppilastutkinto', 'Lukion oppimäärä (2012—2016, valmistunut)'])
          })
        })
      })
    })
  })

  describe('Kun huollettavalla ei ole opintoja Koskessa', function() {
    before(authentication.logout, etusivu.openPage, etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('100869-192W'), wait.until(omattiedot.isVisible))
    it('näytetään omat tiedot', function () {
      verifyOppija('Opintoni', 'Dilbert Dippainssi\ns. 10.8.1969', ['Aalto-yliopisto'], ['Dipl.ins., konetekniikka (2013—2016, päättynyt)', '8 opintojaksoa'])
    })

    describe('Kun tarkastellaan huollettavan tietoja', function() {
      before(
        click(omattiedot.huollettavantiedotButton),
        wait.until(huollettavantiedot.isVisible),
        click(huollettavantiedot.tarkasteleHuollettavasiTietojaButton),
        wait.until(omattiedot.huollettavanTiedotNäkyvissä)
      )

      it('näytetään huollettavan tiedot', function () {
        verifyOppijaEmpty('Huollettavani opinnot', 'Eino EiKoskessa\ns. 27.1.1981')
      })

      describe('Kun painetaan "Palaa omiin opintotietoihin"-linkkiä', function() {
        before(click(omattiedot.palaaOmiinTietoihin), wait.until(omattiedot.omatTiedotNäkyvissä))

        it('näytetään omat tiedot', function () {
          verifyOppija('Opintoni', 'Dilbert Dippainssi\ns. 10.8.1969', ['Aalto-yliopisto'], ['Dipl.ins., konetekniikka (2013—2016, päättynyt)', '8 opintojaksoa'])
        })
      })
    })
  })

  describe('Kun itsellä ei ole opintoja Koskessa', function() {
    before(authentication.logout, etusivu.openPage, etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('270181-5263'), wait.until(omattiedot.isVisible))
    it('näytetään omat tiedot', function () {
      verifyOppijaEmpty('Opintoni', 'Eino EiKoskessa\ns. 27.1.1981')
    })

    describe('Kun tarkastellaan huollettavan tietoja', function() {
      before(
        click(omattiedot.huollettavantiedotButton),
        wait.until(huollettavantiedot.isVisible),
        click(huollettavantiedot.tarkasteleHuollettavasiTietojaButton),
        wait.until(omattiedot.huollettavanTiedotNäkyvissä)
      )

      it('näytetään huollettavan tiedot', function () {
        verifyOppija('Huollettavani opinnot', 'Dilbert Dippainssi\ns. 10.8.1969', ['Aalto-yliopisto'], ['Dipl.ins., konetekniikka (2013—2016, päättynyt)', '8 opintojaksoa'])
      })

      describe('Kun painetaan "Palaa omiin opintotietoihin"-linkkiä', function() {
        before(click(omattiedot.palaaOmiinTietoihin), wait.until(omattiedot.omatTiedotNäkyvissä))

        it('näytetään omat tiedot', function () {
          verifyOppijaEmpty('Opintoni', 'Eino EiKoskessa\ns. 27.1.1981')
        })
      })
    })
  })

  describe('Kun huollettavan Koski-tietoja haettaessa tulee ongelmia', function() {
    before(
      authentication.logout,
      etusivu.openPage,
      etusivu.login(),
      wait.until(korhopankki.isReady),
      korhopankki.login('251019-039B'),
      wait.until(omattiedot.isVisible),
      click(omattiedot.huollettavantiedotButton),
      wait.until(huollettavantiedot.isVisible),
      click(huollettavantiedot.tarkasteleHuollettavasiTietojaButton),
      wait.until(omattiedot.huollettavanTiedotNäkyvissä)
    )

    it('näytetään huollettavan tiedot ja varoitus', function () {
      verifyOppijaEmpty('Huollettavani opinnot', 'Eivastaa Virtanen\ns. 25.3.1990')
      expect(extractAsText(S('.varoitus'))).to.equal('Korkeakoulujen opintoja ei juuri nyt saada haettua. Yritä myöhemmin uudestaan.')
    })

    describe('Kun painetaan "Palaa omiin opintotietoihin"-linkkiä', function() {
      before(click(omattiedot.palaaOmiinTietoihin), wait.until(omattiedot.omatTiedotNäkyvissä))

      it('näytetään omat tiedot', function () {
        verifyOppija('Opintoni', 'Teija Tekijä\ns. 25.10.1919', ['Stadin ammattiopisto'], ['Autoalan perustutkinto (2000—, läsnä)'])
      })
    })
  })

  describe('Kun suomifivaltuudet-sessiota luotaessa tulee ongelmia', function() {
    before(
      authentication.logout,
      etusivu.openPage,
      etusivu.login(),
      wait.until(korhopankki.isReady),
      korhopankki.login('080154-770R'),
      wait.until(omattiedot.isVisible),
      click(omattiedot.huollettavantiedotButton),
      wait.until(huollettavantiedot.isVisible),
      click(huollettavantiedot.tarkasteleHuollettavasiTietojaButton),
      wait.until(omattiedot.varoitusNäkyvissä)
    )

    it('näytetään omat tiedot ja varoitus', function () {
      verifyOppija('Opintoni', 'Eéro Jorma-Petteri Markkanen-Fagerström\ns. 8.1.1954', ['Omnian ammattiopisto'], ['Autoalan perustutkinto (2000—, läsnä)'])
      expect(omattiedot.omatTiedotNäkyvissä()).to.equal(true)
      expect(extractAsText(S('.varoitus'))).to.equal('Huollettavien opintotietoja ei juuri nyt saada haettua. Yritä myöhemmin uudelleen.')
    })
  })

  describe('Kun valitun huollettavan hetun hakeminen suomifivaltuudet-palvelusta epäonnistuu', function() {
    before(
      authentication.logout,
      etusivu.openPage,
      etusivu.login(),
      wait.until(korhopankki.isReady),
      korhopankki.login('280608-6619'),
      wait.until(omattiedot.isVisible),
      click(omattiedot.huollettavantiedotButton),
      wait.until(huollettavantiedot.isVisible),
      click(huollettavantiedot.tarkasteleHuollettavasiTietojaButton),
      wait.until(omattiedot.varoitusNäkyvissä)
    )

    it('näytetään omat tiedot ja varoitus', function () {
      verifyOppijaEmpty('Opintoni', 'Tero Petteri Gustaf Tunkkila-Fagerlund\ns. 28.6.1908')
      expect(omattiedot.omatTiedotNäkyvissä()).to.equal(true)
      expect(extractAsText(S('.varoitus'))).to.equal('Huollettavien opintotietoja ei juuri nyt saada haettua. Yritä myöhemmin uudelleen.')
    })
  })

  function verifyOppija(expectedHeader, expectedName, expectedOppilaitokset, expectedOpiskeluoikeudet) {
    expect(omattiedot.oppija()).to.equal(expectedHeader)
    expect(omattiedot.headerNimi()).to.equal(expectedName)
    expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal(expectedOppilaitokset)
    expect(opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikot()).to.deep.equal(expectedOpiskeluoikeudet)
  }

  var expectedHuollettavaEmptyText =  'Huollettavasi tiedoilla ei löydy opintosuorituksia eikä opiskeluoikeuksia.'
  var expectedOmattiedotEmptyText = 'Tiedoillasi ei löydy opintosuorituksia eikä opiskeluoikeuksia.'
  function verifyOppijaEmpty(expectedHeader, expectedName) {
    expect(omattiedot.oppija()).to.equal(expectedHeader)
    expect(omattiedot.headerNimi()).to.equal(expectedName)
    expect(isElementVisible(S('.oppija-content .ei-suorituksia'))).to.equal(true)
    var expectedHeader = expectedHeader === 'Opintoni' ? expectedOmattiedotEmptyText : expectedHuollettavaEmptyText
    expect(extractAsText(S('.oppija-content .ei-suorituksia h2'))).to.equal(expectedHeader)
    expect(isElementVisible(omattiedot.suoritusjakoButton())).to.equal(false)
    expect(isElementVisible(omattiedot.virheraportointiButton())).to.equal(false)
    expect(isElementVisible(omattiedot.huollettavantiedotButton())).to.equal(true)
  }
})
