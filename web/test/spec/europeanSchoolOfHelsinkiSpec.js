describe('Helsingin eurooppalainen koulu', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var addOppija = AddOppijaPage()

  before(Authentication().login(), resetFixtures)

  describe('Helsingin eurooppalainen koulu', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('050707A130V'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
        'europeanschoolofhelsinki'
      )
    )
    describe('Opiskeluoikeuden tiedot', function () {
      it('näytetään', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 1.8.2022 —\nTila 1.8.2022 Läsnä (muuta kautta rahoitettu)\nLisätiedot'
        )

        expect(
          opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()
        ).to.deep.equal([
          'European School of Helsinki nursery cycle year class 2022—, Läsnä'
        ])
      })
    })

    describe('Opiskeluoikeuden lisääminen', function () {
      before(prepareForNewOppija('kalle', '070998-798T'))

      describe('Aluksi', function () {
        it('Lisää-nappi on disabloitu', function () {
          expect(addOppija.isEnabled()).to.equal(false)
        })
      })

      describe('Kun syötetään validit tiedot', function () {
        before(addOppija.enterValidDataEsh())

        describe('Käyttöliittymän tila', function () {
          it('Lisää-nappi on enabloitu', function () {
            expect(addOppija.isEnabled()).to.equal(true)
          })

          it('Ei näytetä opintojen rahoitus -kenttää', function () {
            expect(addOppija.rahoitusIsVisible()).to.equal(false)
          })

          it('Näytetään oikeat tilavaihtoehdot', function () {
            expect(addOppija.opiskeluoikeudenTilat()).to.deep.equal([
              'Eronnut',
              'Läsnä',
              'Valmistunut',
              'Väliaikaisesti keskeytynyt'
            ])
          })
        })

        describe('Kun painetaan Lisää-nappia', function () {
          before(
            addOppija.submitAndExpectSuccess(
              'Tyhjä, Tero (070998-798T)',
              'Class N1'
            )
          )

          it('lisätty oppija näytetään', function () {})

          describe('Käyttöliittymän tila', function () {
            it('Lisätty opiskeluoikeus näytetään', function () {
              expect(opinnot.getTutkinto()).to.equal('Class N1')
              expect(
                extractAsText(
                  S(
                    "[data-test-id='span-for-europeanschoolofhelsinkicurriculum-enum-editor']"
                  )
                )
              ).to.deep.equal('Curriculum 2023')
              expect(opinnot.getOppilaitos()).to.equal(
                'Helsingin eurooppalainen koulu'
              )
              expect(opinnot.getSuorituskieli()).to.equal('englanti')
            })
          })
        })
      })
    })
  })
})
