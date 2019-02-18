describe('Muu ammatillinen koulutus', function() {
  before(Authentication().login())
  var addOppija = AddOppijaPage()
  var opinnot = OpinnotPage()

  describe('Opiskeluoikeuden lisääminen', function() {
    describe('Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus', function() {
      var paikallinenKoulutusModuuli = {
        nimi: 'Varaston täyttäminen',
        koodi: 'vrs-t-2019-k',
        kuvaus: 'Opiskelija osaa tehdä tilauksen vakiotoimittajilta sekä menettelytavat palavien ja vaarallisten aineiden varastoinnissa'
      }

      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectOppimäärä('Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus'),
        addOppija.enterPaikallinenKoulutusmoduuliData(paikallinenKoulutusModuuli),
        addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', paikallinenKoulutusModuuli.nimi)
      )

      it('Lisätty opiskeluoikeus näytetään', function () {
        expect(opinnot.getKoulutusModuuli().nimi).to.equal(paikallinenKoulutusModuuli.nimi)
        expect(opinnot.getKoulutusModuuli().koodi).to.equal(paikallinenKoulutusModuuli.koodi)
        expect(opinnot.getKoulutusModuuli().kuvaus).to.equal(paikallinenKoulutusModuuli.kuvaus)
        expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
      })
    })
  })
})
