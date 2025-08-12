describe('Useampi voimassa oleva opinto oikeus samassa oppilaitoksessa', function () {
  var addOppija = AddOppijaPage()
  var opinnot = OpinnotPage()
  var page = KoskiPage()

  before(
    Authentication().login(),
    resetFixtures,
    page.openPage,
    page.oppijaHaku.searchAndSelect('130320-899Y'),
    prepareForNewOppija('kalle', '230872-7258'),
    addOppija.enterValidDataMuuAmmatillinen(),
    addOppija.enterPaikallinenKoulutusmoduuliData(),
    addOppija.submitAndExpectSuccess(
      'Tyhjä, Tero (230872-7258)',
      'Varaston täyttäminen'
    )
  )

  describe('perusopetuksessa', function () {
    before(
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Aalto-yliopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Perusopetus'),
      addOppija.submitModal,
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Aalto-yliopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Perusopetus'),
      addOppija.submitModal
    )
    it('ei ole sallittu', function () {
      expect(page.getErrorMessage()).to.equal(
        'Vastaava opiskeluoikeus on jo olemassa.'
      )
    })
  })
  describe('ammatillisessa koulutuksessa', function () {
    before(
      page.oppijaHaku.searchAndSelect('230872-7258'),
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
      addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
      addOppija.selectTutkinto('Autoalan perustutkinto'),
      addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
      addOppija.selectAloituspäivä('1.1.2018'),
      addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
      addOppija.submitModal,
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
      addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
      addOppija.selectTutkinto('Autoalan perustutkinto'),
      addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
      addOppija.selectAloituspäivä('1.1.2018'),
      addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
      addOppija.submitModal
    )
    it('ei ole sallittu', function () {
      expect(page.getErrorMessage()).to.equal(
        'Vastaava opiskeluoikeus on jo olemassa.'
      )
    })
    describe('kun suoritustyyppi eroaa', function () {
      before(
        page.oppijaHaku.searchAndSelect('230872-7258'),
        opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
        addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
        addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
        addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
        addOppija.selectTutkinto('Autoalan työnjohdon erikoisammattitutkinto'),
        addOppija.selectSuoritustapa('Näyttötutkinto'),
        addOppija.selectAloituspäivä('1.1.2018'),
        addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
        addOppija.submitModal
      )
      it('on sallittu', function () {
        expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienMäärä()).to.equal(3)
      })
    })
  })
  describe('muussa ammatillisessa koulutuksessa', function () {
    before(
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
      addOppija.selectSuoritustyyppi('Muun ammatillisen koulutuksen suoritus'),
      addOppija.selectKoulutusmoduuli(
        'Ammatilliseen tehtävään valmistava koulutus'
      ),
      addOppija.enterAmmatilliseenTehtäväänvalmistava(
        'Ansio- ja liikennelentäjä'
      ),
      addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
      addOppija.submitModal,
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
      addOppija.selectSuoritustyyppi('Muun ammatillisen koulutuksen suoritus'),
      addOppija.selectKoulutusmoduuli(
        'Ammatilliseen tehtävään valmistava koulutus'
      ),
      addOppija.enterAmmatilliseenTehtäväänvalmistava(
        'Ansio- ja liikennelentäjä'
      ),
      addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
      addOppija.submitModal
    )
    it('on sallittu', function () {
      expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienMäärä()).to.equal(5)
    })
  })
})
