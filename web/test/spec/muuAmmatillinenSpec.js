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
          'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
          'Suorituskieli suomi\n' +
          'Suoritus kesken'
        )
      })

      it('näyttää oppiaineiden ja kurssien arvosanat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Avaa kaikki\n' +
          'Osasuoritus Laajuus (op) Arvosana\n' +
          'Asunto-osakeyhtiölain ja huoneenvuokralainsäädännön perusteet 5 op Suoritettu\n' +
          'Asunto- ja kiinteistöosakeyhtiön talous ja verotus 15 op Suoritettu\n' +
          'Tiedottaminen ja asiakaspalvelu 20 op Suoritettu\n' +
          'KISI-tentti Suoritettu\n' +
          'ATK-Ajokortti\n' +
          'Fysiikka ja kemia Hyväksytty\n' +
          'Äidinkieli, Suomen kieli ja kirjallisuus Hyväksytty\n' +
          'Yhteensä 40 op'
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
          expect(opinnot.getOppilaitos()).to.equal('Stadin ammatti- ja aikuisopisto')
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
          expect(opinnot.getOppilaitos()).to.equal('Stadin ammatti- ja aikuisopisto')
        })

        it('Sallii kahden voimassaolevan opiskeluoikeuden lisäämisen samassa oppilaitoksessa', function(done) {
          opinnot.opiskeluoikeudet.lisääOpiskeluoikeus()
            .then(addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'))
            .then(addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'))
            .then(addOppija.selectOppimäärä('Muun ammatillisen koulutuksen suoritus'))
            .then(addOppija.selectKoulutusmoduuli('Ammatilliseen tehtävään valmistava koulutus'))
            .then(addOppija.enterAmmatilliseenTehtäväänvalmistava('Ansio- ja liikennelentäjä'))
            .then(addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'))
            .then(addOppija.submitModal)
            .then(_ => expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienMäärä()).to.equal(2))
            .then(_ => done())
        })
      })

      describe('Muiden ammatillisen päätason suoritusten lisäyspainikkeet', function() {
        before(opinnot.opiskeluoikeusEditor(0).edit)
        it('ei ole näkyvissä', function() {
          expect(lisääSuoritus.isLinkVisible('lisää ammatillisen tutkinnon suoritus')).to.equal(false)
          expect(lisääSuoritus.isLinkVisible('lisää näyttötutkintoon valmistavan koulutuksen suoritus')).to.equal(false)
        })
        after(editor.cancelChanges)
      })
    })

    describe('Tietojen muuttaminen', function() {
      before(page.openPage, page.oppijaHaku.searchAndSelect('130320-899Y'))

      describe('Täydentää tutkintoa', function() {
        before(
          editor.edit,
          editor.property('täydentääTutkintoa').addValue,
          opinnot.tutkinto('.täydentääTutkintoa').select('autoalan työnjohdon'),
          editor.saveChangesAndWaitForSuccess
        )

        it('tutkinnon voi valita', function() {
          expect(editor.property('täydentääTutkintoa').getText()).to.equal('Täydentää tutkintoa Autoalan työnjohdon erikoisammattitutkinto 357305 40/011/2001')
        })
      })

      describe('Osasuorituksen osasuorituksen lisääminen', function() {
        var osasuoritus = opinnot.tutkinnonOsat().tutkinnonOsa(0)
        var osanOsa1 = osasuoritus.osanOsat().tutkinnonOsa(0)
        var osanOsa2 = osasuoritus.osanOsat().tutkinnonOsa(1)
        before(
          editor.edit,
          opinnot.avaaKaikki,
          osasuoritus.propertyBySelector('.laajuus .arvo').setValue(2),
          osasuoritus.propertyBySelector('.laajuudenyksikko').setValue('opintopistettä'),
          osasuoritus.osanOsat().lisääPaikallinenTutkinnonOsa('Asunto-osakeyhtiolain perusteet'),
          osanOsa1.propertyBySelector('.laajuus .arvo').setValue(2),
          osanOsa1.propertyBySelector('.laajuudenyksikko').setValue('opintopistettä'),
          osanOsa1.propertyBySelector('.arvosana').setValue('3', 1),
          osanOsa1.toggleExpand,
          osasuoritus.osanOsat().lisääPaikallinenTutkinnonOsa('Huoneenvuokralainsäädännön perusteet'),
          osanOsa2.propertyBySelector('.laajuus .arvo').setValue(1),
          osanOsa2.propertyBySelector('.laajuudenyksikko').setValue('opintopistettä'),
          osanOsa2.propertyBySelector('.arvosana').setValue('3', 1),
          editor.saveChangesAndExpectError
        )

        it('epäonnistuu kun laajuudet ei täsmää', function() {})

        describe('Kun laajuudet täsmää', function() {
          before(
            osanOsa1.toggleExpand,
            osanOsa2.toggleExpand,
            osanOsa1.propertyBySelector('.laajuus .arvo').setValue(1),
            editor.saveChangesAndWaitForSuccess,
            opinnot.avaaKaikki
          )
          it('lisääminen onnistuu', function() {
            expect(osasuoritus.osanOsat().osienTekstit()).to.equal(
              'Asunto-osakeyhtiolain perusteet 1 3\n\n' +
              'Huoneenvuokralainsäädännön perusteet 1 3'
            )
          })

          describe('Osasuorituksen osasuoritukseen lisääminen', function() {
            before(
              editor.edit,
              opinnot.avaaKaikki,
              osanOsa1.toggleExpand
            )

            it('on estetty', function() {
              expect(extractAsText(osanOsa1.elem())).to.not.contain('Lisää osasuoritus')
            })
          })
        })
      })

      describe('Yhteisen tutkinnon osan osa-alueen lisääminen', function () {
        describe('Koodistosta löytyvä osan osa-alue', function () {
          before(
            opinnot.suljeKaikki,
            editor.edit,
            opinnot.tutkinnonOsat().lisääTutkinnonOsa('ETK Etiikka'),
            editor.saveChangesAndWaitForSuccess
          )

          it('onnistuu', function () {
            expect(opinnot.tutkinnonOsat().osienTekstit()).to.include(
              'Etiikka'
            )
          })
        })
        describe('Paikallinen osan osa-alue', function () {
          before(
            editor.edit,
            opinnot.tutkinnonOsat().lisääPaikallinenTutkinnonOsa('Kotiviinin valmistaminen', ':contains(Lisää paikallinen yhteisen tutkinnon osan osa-alue)'),
            editor.saveChangesAndWaitForSuccess
          )

          it('onnistuu', function () {
            expect(opinnot.tutkinnonOsat().osienTekstit()).to.include(
              'Kotiviinin valmistaminen'
            )
          })
        })
      })

      describe('Tutkinnon osaa pienemmän kokonaisuuden suorituksen lisääminen', function() {
        before(
          editor.edit,
          opinnot.tutkinnonOsat().lisääTutkinnonOsaaPienempiKokonaisuus('Autoalan perustutkinto', 'Auton korjaaminen', 'Auton tuunaus'),
          editor.saveChangesAndWaitForSuccess
        )

        it('onnistuu', function() {
          expect(opinnot.tutkinnonOsat().osienTekstit()).to.include(
            'Auton tuunaus'
          )
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
        expect(opinnot.getOppilaitos()).to.equal('Stadin ammatti- ja aikuisopisto')
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
      before(resetFixtures, page.openPage, page.oppijaHaku.searchAndSelect('040754-054W'))

      describe('Tutkinnon osaa pienemmän kokonaisuuden suorituksen muuttaminen', function() {
        before(
          editor.edit,
          opinnot.avaaKaikki,
          opinnot.tutkinnonOsat().tutkinnonOsa(0).liittyyTutkinnonOsaan().valitseTutkinto('Autoalan perustutkinto'),
          opinnot.tutkinnonOsat().tutkinnonOsa(0).liittyyTutkinnonOsaan().valitseTutkinnonOsa('Varaosatyö ja varaston hallinta'),
          editor.saveChangesAndWaitForSuccess,
          opinnot.avaaKaikki
        )

        it('onnistuu', function() {
          expect(opinnot.tutkinnonOsat().osienTekstit()).to.equal(
            'Asunto- ja kiinteistöosakeyhtiön talous ja verotus Hyväksytty\n' +
            'Kuvaus Kurssilla opitaan hallitsemaan asunto- ja kiinteistöosakeyhtiön taloutta ja verotusta.\n' +
            'Liittyy tutkinnon osaan Varaosatyö ja varaston hallinta\n' +
            'Arviointi Arvosana Hyväksytty\n' +
            'Arviointipäivä 20.3.2013\n' +
            'Arvioijat Jaana Arstila Pekka Saurmann Juhani Mykkänen\n' +
            '\n' +
            'Tietokoneiden huolto 4 osp\n' +
            'Kuvaus Kurssilla opitaan korjaamaan tietokoneita.\n' +
            'Liittyy tutkinnon osaan Asunto-osakeyhtiön hallinnon hoitaminen\n' +
            '\n' +
            'Fysiikka ja kemia Hyväksytty\n' +
            'Pakollinen kyllä\n' +
            'Arviointi Arvosana Hyväksytty\n' +
            'Arviointipäivä 20.3.2013\n' +
            'Arvioijat Jaana Arstila Pekka Saurmann Juhani Mykkänen\n' +
            '\n' +
            'Äidinkieli, Suomen kieli ja kirjallisuus Hyväksytty\n' +
            'Pakollinen ei\n' +
            'Arviointi Arvosana Hyväksytty\n' +
            'Arviointipäivä 20.3.2013\n' +
            'Arvioijat Jaana Arstila Pekka Saurmann Juhani Mykkänen\n' +
            '\n' +
            'Etiikka\n' +
            'Pakollinen kyllä\n' +
            'Tunnustettu\n' +
            'Tutkinnon osa Asennushitsaus\n' +
            'Selite Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta\n' +
            'Rahoituksen piirissä ei\n' +
            '\n' +
            'Psykologia 5 osp Hyväksytty\n' +
            'Pakollinen kyllä\n' +
            'Tunnustettu\n' +
            'Tutkinnon osa Asennushitsaus\n' +
            'Selite Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta\n' +
            'Rahoituksen piirissä kyllä\n' +
            'Arviointi Arvosana Hyväksytty\n' +
            'Arviointipäivä 20.3.2013\n' +
            'Arvioijat Jaana Arstila Pekka Saurmann Juhani Mykkänen'
          )
        })
      })

      describe('Tutkinnon osaa pienemmän kokonaisuuden suorituksen lisääminen', function() {
        before(
          editor.edit,
          opinnot.tutkinnonOsat().lisääTutkinnonOsaaPienempiKokonaisuus('Autoalan perustutkinto', 'Auton korjaaminen', 'Auton tuunaus'),
          editor.saveChangesAndWaitForSuccess
        )

        it('onnistuu', function() {
          expect(opinnot.tutkinnonOsat().osienTekstit()).to.equal(
            'Asunto- ja kiinteistöosakeyhtiön talous ja verotus Hyväksytty\n' +
            '\nTietokoneiden huolto 4 osp\n' +
            '\nFysiikka ja kemia Hyväksytty\n' +
            '\nÄidinkieli, Suomen kieli ja kirjallisuus Hyväksytty\n' +
            '\nEtiikka\n' +
            '\nPsykologia 5 osp Hyväksytty\n' +
            '\nAuton tuunaus'
          )
        })
      })

      describe('Yhteisen tutkinnon osan osa-alueen lisääminen', function () {
        describe('Koodistosta löytyvä osan osa-alue', function () {
          before(
            editor.edit,
            opinnot.tutkinnonOsat().lisääTutkinnonOsa('ETK Etiikka'),
            editor.saveChangesAndWaitForSuccess
          )

          it('onnistuu', function () {
            expect(opinnot.tutkinnonOsat().osienTekstit()).to.include(
              'Etiikka'
            )
          })
        })
        describe('Paikallinen osan osa-alue', function () {
          before(
            editor.edit,
            opinnot.tutkinnonOsat().lisääPaikallinenTutkinnonOsa('Kotiviinin valmistaminen', ':contains(Lisää paikallinen yhteisen tutkinnon osan osa-alue)'),
            editor.saveChangesAndWaitForSuccess
          )

          it('onnistuu', function () {
            expect(opinnot.tutkinnonOsat().osienTekstit()).to.include(
              'Kotiviinin valmistaminen'
            )
          })
        })
      })
    })
  })
})
