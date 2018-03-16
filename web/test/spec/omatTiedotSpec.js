describe('Omat tiedot', function() {
  var omattiedot = OmatTiedotPage()
  var opinnot = OpinnotPage()
  var authentication = Authentication()
  before(authentication.login(), resetFixtures)

  describe("Virkailijana", function() {
    describe("Kun virkailijalla on opiskeluoikeuksia", function() {
      before(authentication.login('Oili'), omattiedot.openPage)
      it('ne näytetään', function() {
        expect(omattiedot.oppija()).to.equal("Opintoni")
        expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.have.members([
          'Stadin ammattiopisto', 'Jyväskylän normaalikoulu' ])
      })
    })

    describe("Kun virkailijalla ei ole opiskeluoikeuksia", function() {
      before(authentication.login(), omattiedot.openPage)
      it('näytetään viesti', function() {
        expect(omattiedot.virhe()).to.equal(
          "Tiedoillasi ei löydy opintosuorituksia eikä opiskeluoikeuksia." +
          "Koski-palvelussa pystytään näyttämään seuraavat tiedot:" +
          "Vuoden 2018 tammikuun jälkeen suoritetut peruskoulun, lukion ja ammattikoulun opinnot ja voimassa olevat opiskeluoikeudet." +
          "Vuoden 1990 jälkeen suoritetut ylioppilastutkinnot." +
          "Korkeakoulutusuoritukset ja opiskeluoikeudet ovat näkyvissä pääsääntöisesti vuodesta 1995 eteenpäin, mutta tässä voi olla korkeakoulukohtaisia poikkeuksia." +
          "Mikäli tiedoistasi puuttuu opintosuorituksia tai opiskeluoikeuksia, joiden kuuluisi ylläolevien tietojen perusteella näkyä Koski-palvelussa, voit ilmoittaa asiasta oppilaitoksellesi.")
      })
    })
  })

  describe("Kansalaisena", function() {
    var etusivu = LandingPage()
    var korhopankki = KorhoPankki()
    before(authentication.logout, etusivu.openPage)

    describe("Kun ei olla kirjauduttu sisään", function() {
      it("Näytetään länderi", function() {

      })
    })

    describe("Kun kirjaudutaan sisään", function() {
      before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('030658-998X', 'Kansalainen', 'VÃ¤inÃ¶ TÃµnis', 'VÃ¤inÃ¶'), wait.until(omattiedot.isVisible))
      describe("Sivun sisältö", function() {
        it("Näytetään opiskeluoikeudet", function() {
          expect(omattiedot.nimi()).to.equal("Väinö Tõnis Kansalainen")
          expect(omattiedot.oppija()).to.equal("Opintoni")
          expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal([
            'Itä-Suomen yliopisto' ])
        })

        it("Näytetään opintoni-ingressi", function() {
          expect(omattiedot.ingressi()).to.equal(
            "Tällä sivulla näkyvät kaikki sähköisesti tallennetut opintosuoritukset yksittäisistä kursseista kokonaisiin tutkintoihin."
          )
        })

        it("Näytetään 'Mitkä tiedot palvelussa näkyvät?' -painike", function() {
          expect(!!omattiedot.palvelussaNäkyvätTiedotButton().length).to.equal(true)
        })

        describe("'Mitkä tiedot palvelussa näkyvät' -teksti", function () {
          it("Aluksi ei näytetä tekstiä", function () {
            expect(omattiedot.palvelussaNäkyvätTiedotText()).to.equal("")
          })

          describe("Kun painetaan painiketta", function () {
            before(click(omattiedot.palvelussaNäkyvätTiedotButton))

            it("näytetään teksti", function () {
              expect(omattiedot.palvelussaNäkyvätTiedotText()).to.equal(
                "Koski-palvelussa pystytään näyttämään seuraavat tiedot:\n" +
                "Vuoden 2018 tammikuun jälkeen suoritetut peruskoulun, lukion ja ammattikoulun opinnot ja voimassa olevat opiskeluoikeudet.\n" +
                "Vuoden 1990 jälkeen suoritetut ylioppilastutkinnot.\n" +
                "Korkeakoulutusuoritukset ja opiskeluoikeudet ovat näkyvissä pääsääntöisesti vuodesta 1995 eteenpäin, mutta tässä voi olla korkeakoulukohtaisia poikkeuksia."
              )
            })
          })

          describe("Kun painetaan painiketta uudestaan", function () {
            before(click(omattiedot.palvelussaNäkyvätTiedotButton))

            it("teksti piilotetaan", function () {
              expect(omattiedot.palvelussaNäkyvätTiedotText()).to.equal("")
            })
          })

          describe("Kun teksti on näkyvissä", function () {
            before(click(omattiedot.palvelussaNäkyvätTiedotButton))

            it("alkutila (teksti näkyvissä)", function () {
              expect(omattiedot.palvelussaNäkyvätTiedotText()).to.not.equal("")
            })

            describe("Pop-upin painikkeella", function () {
              before(click(omattiedot.palvelussaNäkyvätTiedotCloseButton))

              it("voidaan piilottaa teksti", function () {
                expect(omattiedot.palvelussaNäkyvätTiedotText()).to.equal("")
              })
            })
          })
        })
      })

      describe("Kun kirjaudutaan ulos", function () {
        before(click(findSingle('#logout')), wait.until(etusivu.isVisible))
        it("Näytetään länderi", function() {

        })
      })

      describe("Kun tiedot löytyvät vain YTR:stä", function() {
        before(authentication.logout, etusivu.openPage)

        before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('010342-8411'), wait.until(omattiedot.isVisible))

        describe("Sivun sisältö", function() {
          it("Näytetään opiskeluoikeudet", function() {
            expect(omattiedot.nimi()).to.equal("Mia Orvokki Numminen")
            expect(omattiedot.oppija()).to.equal("Opintoni")
            expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal([
              'Ylioppilastutkintolautakunta' ])
          })
        })
      })

      describe("Virhetilanne", function() {
        before(authentication.logout, etusivu.openPage)

        before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('010342-8413'), wait.until(VirhePage().isVisible))

        describe("Sivun sisältö", function() {
          it("Näytetään virhesivu", function() {
            expect(VirhePage().teksti().trim()).to.equalIgnoreNewlines('Koski-järjestelmässä tapahtui virhe, yritä myöhemmin uudelleen\n          Palaa etusivulle')
          })
        })
      })
    })
  })
})
