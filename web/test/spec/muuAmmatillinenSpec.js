describe('Muu ammatillinen koulutus', function() {
  before(Authentication().login())
  var addOppija = AddOppijaPage()
  var opinnot = OpinnotPage()

  describe('Opiskeluoikeuden lisääminen', function() {
    describe('Muun ammatillisen koulutuksen suoritus', function() {
      describe('Paikallinen koulutusmoduuli', function() {
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataMuuAmmatillinen(),
          addOppija.enterPaikallinenKoulutusmoduuliData(),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Varaston täyttäminen')
        )

        it('Lisätty opiskeluoikeus näytetään', function () {
          expect(opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()[0]).to.match(/^Muun ammatillisen koulutuksen suoritus*/)
          expect(opinnot.getKoulutusModuuli().nimi).to.equal('Varaston täyttäminen')
          expect(opinnot.getKoulutusModuuli().koodi).to.equal('vrs-t-2019-k')
          expect(opinnot.getKoulutusModuuli().kuvaus).to.equal('Opiskelija osaa tehdä tilauksen vakiotoimittajilta sekä menettelytavat palavien ja vaarallisten aineiden varastoinnissa')
          expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
        })
      })


      describe('Ammatilliseen tehtävään valmistava koulutus', function() {
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataMuuAmmatillinen({koulutusmoduuli: 'Ammatilliseen tehtävään valmistava koulutus'}),
          addOppija.enterAmmatilliseenTehtäväänvalmistava('Lennonjohtaja'),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Lennonjohtaja')
        )

        it('Lisätty opiskeluoikeus näytetään', function () {
          expect(opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()[0]).to.match(/^Muun ammatillisen koulutuksen suoritus*/)
          expect(opinnot.getTutkinto()).to.equal('Lennonjohtaja')
          expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
        })
      })
    })

    describe('Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus', function() {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectOppimäärä('Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus'),
        addOppija.enterPaikallinenKoulutusmoduuliData(),
        addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Varaston täyttäminen')
      )

      it('Lisätty opiskeluoikeus näytetään', function () {
        expect(opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()[0]).to.match(/^Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus.*/)
        expect(opinnot.getKoulutusModuuli().nimi).to.equal('Varaston täyttäminen')
        expect(opinnot.getKoulutusModuuli().koodi).to.equal('vrs-t-2019-k')
        expect(opinnot.getKoulutusModuuli().kuvaus).to.equal('Opiskelija osaa tehdä tilauksen vakiotoimittajilta sekä menettelytavat palavien ja vaarallisten aineiden varastoinnissa')
        expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
      })
    })
  })
})
