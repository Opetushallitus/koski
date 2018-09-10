describe('VALMA koulutus', function() {
  before(Authentication().login())

  var addOppija = AddOppijaPage()
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  describe('Opiskeluoikeuden lisääminen vanhalla perusteella', function() {
    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataAmmatillinen(),
      addOppija.selectOppimäärä('Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)'),
      addOppija.selectPeruste('5/011/2015'),
      addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Ammatilliseen koulutukseen valmentava koulutus (VALMA)')
    )

    it('Lisätty opiskeluoikeus näytetään', function () {
      expect(opinnot.getTutkinto()).to.equal('Ammatilliseen koulutukseen valmentava koulutus (VALMA)')
      expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
      expect(editor.propertyBySelector('.koulutusmoduuli .diaarinumero').getValue()).to.equal('5/011/2015')
    })
  })

  describe('Opiskeluoikeuden lisääminen', function() {
    before(
      resetFixtures,
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataAmmatillinen(),
      addOppija.selectOppimäärä('Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)'),
      addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Ammatilliseen koulutukseen valmentava koulutus (VALMA)')
    )

    it('Lisätty opiskeluoikeus näytetään', function () {
      expect(opinnot.getTutkinto()).to.equal('Ammatilliseen koulutukseen valmentava koulutus (VALMA)')
      expect(opinnot.getOppilaitos()).to.equal('Stadin ammattiopisto')
      expect(editor.propertyBySelector('.koulutusmoduuli .diaarinumero').getValue()).to.equal('OPH-2658-2017')
    })

    describe('Ammatillisen tutkinnon suorituksen lisääminen', function() {
      var lisääSuoritus = opinnot.lisääSuoritusDialog

      describe('Lisäyspainike', function() {
        before(editor.edit)
        it('Ei ole näkyvissä', function() {
          expect(lisääSuoritus.isLinkVisible('lisää ammatillisen tutkinnon suoritus')).to.equal(false)
        })
        after(editor.cancelChanges)
      })
    })

    var suoritustapa = editor.property('suoritustapa')
    describe('Tutkinnon osan lisääminen', function () {
      before(
        editor.edit,
        opinnot.tutkinnonOsat().lisääTutkinnonOsaToisestaTutkinnosta('Autoalan perustutkinto', 'Auton korjaaminen'),
        opinnot.tutkinnonOsat().lisääPaikallinenTutkinnonOsa('Hassut temput'),
        editor.saveChanges,
        wait.forAjax
      )

      it('näyttää oikeat tiedot', function () {
        expect(opinnot.tutkinnonOsat().tutkinnonOsa(0).nimi()).to.equal('Auton korjaaminen')
        expect(opinnot.tutkinnonOsat().tutkinnonOsa(1).nimi()).to.equal('Hassut temput')
      })
    })
  })

  describe('Ammatilliseen peruskoulutukseen valmentava koulutus VALMA', function() {
    describe('Oppilaitos-katselija -käyttöoikeuksilla', function() {
      before(Authentication().logout, Authentication().login('katselija'), page.openPage, page.oppijaHaku.searchAndSelect('130404-054C'))
      describe('kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('näyttää opiskeluoikeuden tiedot', function() {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
            'Opiskeluoikeuden voimassaoloaika : 14.9.2009 — 4.6.2016\n' +
            'Tila 4.6.2016 Valmistunut\n' +
            '14.9.2009 Läsnä'
          )
        })

        it('näyttää suorituksen tiedot', function() {
          expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
            'Koulutus Ammatilliseen koulutukseen valmentava koulutus (VALMA) 5/011/2015\n' +
            'Laajuus 65 osp\n' +
            'Oppilaitos / toimipiste Stadin ammattiopisto\n' +
            'Suorituskieli suomi\n' +
            'Suoritus valmis Vahvistus : 4.6.2016 Reijo Reksi , rehtori'
          )
        })

        it('näyttää tutkinnon osat', function() {
          expect(extractAsText(S('.suoritus > .osasuoritukset'))).to.equalIgnoreNewlines(
            'Sulje kaikki\n' +
            'Koulutuksen osa Laajuus (osp) Arvosana\n' +
            'Ammatilliseen koulutukseen orientoituminen ja työelämän perusvalmiuksien hankkiminen 10 Hyväksytty\n' +
            'Kuvaus Ammatilliseen koulutukseen orientoituminen ja työelämän perusvalmiuksien hankkiminen\n' +
            'Pakollinen kyllä\n' +
            'Opiskeluvalmiuksien vahvistaminen 10 Hyväksytty\n' +
            'Kuvaus Opiskeluvalmiuksien vahvistaminen\n' +
            'Pakollinen ei\n' +
            'Työssäoppimiseen ja oppisopimuskoulutukseen valmentautuminen 15 Hyväksytty\n' +
            'Kuvaus Työssäoppimiseen ja oppisopimuskoulutukseen valmentautuminen\n' +
            'Pakollinen ei\n' +
            'Arjen taitojen ja hyvinvoinnin vahvistaminen 10 Hyväksytty\n' +
            'Kuvaus Arjen taitojen ja hyvinvoinnin vahvistaminen\n' +
            'Pakollinen ei\n' +
            'Tietokoneen käyttäjän AB-kortti 5 Hyväksytty\n' +
            'Kuvaus Tietokoneen käyttäjän AB-kortti\n' +
            'Pakollinen ei\n' +
            'Auton lisävarustetyöt 15 Hyväksytty\n' +
            'Kuvaus Tuunaus\n' +
            'Pakollinen ei\n' +
            'Tunnustettu\n' +
            'Tutkinnon osa Asennuksen ja automaation perustyöt\n' +
            'Tutkinto Kone- ja metallialan perustutkinto 39/011/2014\n' +
            'Oppilaitos / toimipiste Stadin ammattiopisto, Lehtikuusentien toimipaikka\n' +
            'Vahvistus 3.10.2015 Helsinki Reijo Reksi , rehtori\n' +
            'Selite Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta\n' +
            'Rahoituksen piirissä ei\n' +
            'Äidinkieli, Suomen kieli ja kirjallisuus 5 3\n' +
            'Pakollinen kyllä\n' +
            'Äidinkieli, Suomen kieli ja kirjallisuus 3 3\n' +
            'Pakollinen ei\n' +
            'Toinen kotimainen kieli, ruotsi, ruotsi 1 3\n' +
            'Pakollinen kyllä\n' +
            'Vieraat kielet, englanti 2 3\n' +
            'Pakollinen kyllä\n' +
            'Yhteensä 76 osp'
          )
        })
      })

      describe('Tulostettava todistus', function() {
        before(OpinnotPage().avaaTodistus())
        it('näytetään', function() {
          // See more detailed content specification in ValmaSpec.scala
          expect(TodistusPage().vahvistus()).to.equal('4.6.2016 Reijo Reksi rehtori')
        })
      })
    })
  })
})
