describe('EB-tutkinto', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var addOppija = AddOppijaPage()

  before(Authentication().login(), resetFixtures)

  describe('EB-tutkinto', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('050707A130V'),
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('ebtutkinto')
    )
    describe('Opiskeluoikeuden tiedot', function () {
      it('näytetään', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 14.6.2023 — 15.6.2023\nTila 15.6.2023 Valmistunut\n14.6.2023 Läsnä'
        )

        expect(
          opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()
        ).to.deep.equal(['European Baccalaureate 2023—2023, Valmistunut'])
      })
    })

    describe('Opiskeluoikeuden lisääminen', function () {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('100906A5544'),
        opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
          'europeanschoolofhelsinki'
        ),
        opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
        addOppija.selectOppilaitos('Helsingin eurooppalainen koulu'),
        addOppija.selectOpiskeluoikeudenTyyppi('EB-tutkinto'),
        addOppija.selectAloituspäivä(
          moment().subtract(1, 'years').format('D.M.YYYY')
        )
      )

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
          'Valmistunut'
        ])
      })

      describe('Kun lisää-nappia -painetaan', function () {
        before(
          addOppija.submitAndExpectSuccessModal(
            'Eurooppalainen, Eeva (100906A5544)',
            'European Baccalaureate'
          )
        )

        it('Lisätty opiskeluoikeus näytetään', function () {
          expect(opinnot.getTutkinto()).to.equal('European Baccalaureate')
          expect(
            extractAsText(S("[data-testid='koulutusmoduuli-value']"))
          ).to.deep.equal('European Baccalaureate 2023')
          expect(opinnot.getOppilaitos()).to.equal(
            'Helsingin eurooppalainen koulu'
          )
        })
      })
    })
  })
})
