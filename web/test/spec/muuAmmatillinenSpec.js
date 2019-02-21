describe('Muu ammatillinen koulutus', function() {
  before(Authentication().login())
  var addOppija = AddOppijaPage()
  var opinnot = OpinnotPage()
  var page = KoskiPage()
  var editor = opinnot.opiskeluoikeusEditor()
  var lisääSuoritus = opinnot.lisääSuoritusDialog

  describe('Muun ammatillisen koulutuksen suoritus', function() {
    before( resetFixtures, page.openPage, page.oppijaHaku.searchAndSelect('130320-899Y'))

    describe('Kaikki tiedot näkyvissä', function () {
      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutusmoduuli Tunniste KISI\n' +
          'Nimi Kiinteistösihteerin koulutus ja tutkinto (KISI)\n' +
          'Kuvaus Koulutus antaa opiskelijalle valmiudet hoitaa isännöinti- ja kiinteistöpalvelualan yritysten sihteeri- ja asiakaspalvelutehtäviä.\n' +
          'Oppilaitos / toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
          'Suorituskieli suomi\n' +
          'Suoritus kesken'
        )
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Avaa kaikki\n' +
          'Osasuoritus Arvosana\n' +
          'Asunto-osakeyhtiölain ja huoneenvuokralainsäädännön perusteet Hyväksytty\n' +
          'Asunto- ja kiinteistöosakeyhtiön talous ja verotus Hyväksytty\n' +
          'Tiedottaminen ja asiakaspalvelu Hyväksytty\n' +
          'KISI-tentti Hyväksytty\n' +
          'Yhteensä 0'
        )
      })
    })

    describe('Opiskeluoikeuden lisääminen', function() {
      describe('Paikallinen koulutusmoduuli', function() {
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataMuuAmmatillinen(),
          addOppija.enterPaikallinenKoulutusmoduuliData(),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Varaston täyttäminen')
        )

        it('Lisätty opiskeluoikeus näytetään', function() {
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

        it('Lisätty opiskeluoikeus näytetään', function() {
          expect(opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()[0]).to.match(/^Muun ammatillisen koulutuksen suoritus*/)
          expect(opinnot.getTutkinto()).to.equal('Lennonjohtaja')
          expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
        })
      })

      describe('Muiden ammatillisen päätason suoritusten lisäyspainikkeet', function() {
        before(editor.edit)
        it('ei ole näkyvissä', function() {
          expect(lisääSuoritus.isLinkVisible('lisää ammatillisen tutkinnon suoritus')).to.equal(false)
          expect(lisääSuoritus.isLinkVisible('lisää näyttötutkintoon valmistavan koulutuksen suoritus')).to.equal(false)
        })
        after(editor.cancelChanges)
      })
    })

    describe('Tietojen muuttaminen', function() {
      before(page.openPage, page.oppijaHaku.searchAndSelect('130320-899Y'))
      describe('Päätason suorituksen laajuus', function() {
        before(
          editor.edit,
          editor.propertyBySelector('.koulutusmoduuli .laajuus .arvo').setValue(24),
          editor.propertyBySelector('.koulutusmoduuli .laajuudenyksikko').setValue('opintopistettä'),
          editor.saveChangesAndWaitForSuccess
        )

        it('määrän ja yksikön määrittely onnistuu', function() {
          expect(editor.propertyBySelector('.koulutusmoduuli .laajuus .arvo').getValue()).to.equal('24')
          expect(editor.propertyBySelector('.koulutusmoduuli .laajuudenyksikko').getText()).to.equal('op')
        })
      })

      describe('Täydentää tutkintoa', function() {
        before(
          editor.edit,
          editor.property('täydentääTutkintoa').addValue,
          opinnot.tutkinto('.täydentääTutkintoa').select('autoalan perustutkinto'),
          editor.saveChangesAndWaitForSuccess
        )

        it('tutkinnon voi valita', function() {
          expect(editor.property('täydentääTutkintoa').getText()).to.equal('Täydentää tutkintoa Autoalan perustutkinto 39/011/2014')
        })
      })
    })
  })

  describe('Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus', function() {
    describe('Opiskeluoikeuden lisääminen', function() {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataAmmatillinen(),
        addOppija.selectOppimäärä('Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus'),
        addOppija.enterPaikallinenKoulutusmoduuliData(),
        addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Varaston täyttäminen')
      )

      it('Lisätty opiskeluoikeus näytetään', function() {
        expect(opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()[0]).to.match(/^Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus.*/)
        expect(opinnot.getKoulutusModuuli().nimi).to.equal('Varaston täyttäminen')
        expect(opinnot.getKoulutusModuuli().koodi).to.equal('vrs-t-2019-k')
        expect(opinnot.getKoulutusModuuli().kuvaus).to.equal('Opiskelija osaa tehdä tilauksen vakiotoimittajilta sekä menettelytavat palavien ja vaarallisten aineiden varastoinnissa')
        expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
      })

      describe('Muiden ammatillisen päätason suoritusten lisäyspainikkeet', function() {
        before(editor.edit)
        it('ei ole näkyvissä', function() {
          expect(lisääSuoritus.isLinkVisible('lisää ammatillisen tutkinnon suoritus')).to.equal(false)
          expect(lisääSuoritus.isLinkVisible('lisää näyttötutkintoon valmistavan koulutuksen suoritus')).to.equal(false)
        })
        after(editor.cancelChanges)
      })
    })
  })
})
