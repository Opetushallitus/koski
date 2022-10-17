describe('EPerusteet', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()
  var tilaJaVahvistus = opinnot.tilaJaVahvistus
  before(
    Authentication().login(),
    page.openPage,
    page.oppijaHaku.searchAndSelect('220109-784L')
  )

  describe('Kun peruste löytyy eperusteista', function () {
    before(
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus'),
      wait.until(function () {
        return isElementVisible(S('.diaarinumero a'))
      })
    )

    it('linkki eperusteisiin näytetään', function () {})
  })

  describe('Kun perustetta ei löydy eperusteista', function () {
    before(
      opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
        'perusopetukseenvalmistavaopetus'
      )
    )

    it('peruste näytetään ilman linkkiä', function () {
      expect(isElementVisible(S('.diaarinumero a'))).to.equal(false)
      expect(S('.diaarinumero').text()).to.equal('57/011/2015')
    })
  })

  describe('Tutkinnon nimi', function () {
    describe('haetaan e-perusteista', function () {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('200994-834A'),
        opinnot.valitseSuoritus(
          undefined,
          'Tieto- ja viestintätekniikan perustutkinto, koulutusvientikokeilu'
        )
      )
      var koulutus = opinnot.opiskeluoikeusEditor().property('koulutusmoduuli')
      it('toimii', function () {
        expect(koulutus.getText()).to.equal(
          'Koulutus Tieto- ja viestintätekniikan perustutkinto, koulutusvientikokeilu 341101 OPH-1117-2019'
        )
      })
    })
  })

  describe('Perusteen linkki virkailijan oppija-näkymässä', function () {
    describe('Kun opiskeluoikeuden suoritus on kesken, linkitetään uusimpaan perusteeseen', function () {
      before(
        resetFixtures,
        page.openPage,
        page.oppijaHaku.searchAndSelect('251176-003P'),
        wait.until(function () {
          return isElementVisible(S('.diaarinumero a'))
        })
      )
      it('linkki osoittaa oikeaan eperusteeseen', function () {
        expect(S('.diaarinumero a')[0].href).to.contain('2434074')
      })
    })

    describe('Kun opiskeluoikeudella on päättymispäivä, linkitetään päättymispäivänä voimassa olleeseen perusteeseen', function () {
      var paattymispaiva = '31.12.2018'
      before(
        resetFixtures,
        page.openPage,
        page.oppijaHaku.searchAndSelect('251176-003P'),
        editor.edit,
        editor.property('keskiarvo').setValue(3.5),
        tilaJaVahvistus.merkitseValmiiksi,
        opinnot.tilaJaVahvistus.lisääVahvistus(paattymispaiva),
        opinnot.avaaLisaysDialogi,
        Page().setInputValue(
          '.lisaa-opiskeluoikeusjakso-modal #date-input',
          paattymispaiva
        ),
        click(
          findFirst(
            '.lisaa-opiskeluoikeusjakso-modal .tila li:nth-child(5) label'
          )
        ),
        click(findFirst('.lisaa-opiskeluoikeusjakso-modal .vahvista')),
        editor.saveChanges
      )
      it('linkki osoittaa oikeaan eperusteeseen', function () {
        expect(S('.diaarinumero a')[0].href).to.contain('2434073')
      })
    })
  })
})
