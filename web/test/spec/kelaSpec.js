describe('Kela', function () {
  var kela = KelaPage()

  describe('Jos oppijahakuun syötetään epävalidi hetu', function () {
    before(
      Authentication().login('Laaja'),
      kela.openPage,
      kela.setSearchInputValue('XXXXXXX')
    )

    it('Ilmoitetaan virheestä', function () {
      expect(S('#kela-search-query').hasClass('invalid')).to.equal(true)
    })
  })

  describe('Useita opiskeluoikeuksia ja päätason suorituksia', function () {
    before(
      Authentication().login('Laaja'),
      kela.openPage,
      kela.searchAndSelect('220109-784L', 'Kaisa')
    )
    it('Näytetään valitun henkilon opinnot', function () {
      expect(kela.getOppijanNimi()).to.equal('Koululainen, Kaisa (220109-784L)')
      expect(kela.getValittuOpiskeluoikeusOtsikko()).to.include('Jyväskylän normaalikoulu (2008 - 2016, Valmistunut)')
    })

    describe('Valitaan toinen päätason suoritus', function () {
      before(
        kela.selectSuoritus('9. vuosiluokka')
      )
      it('Näytetään valitun suorituksen tiedot', function () {
        expect(extractAsText(S('.suoritukset .properties'))).to.equal(
          'Toimipiste Jyväskylän normaalikoulu\n' +
          'Tyyppi Perusopetuksen vuosiluokka\n' +
          'Alkamispäivä 2015-08-15\n' +
          'Jää luokalle ei'
        )
      })

      describe('Valitaan toinen opiskeluoikeus', function () {
        before(kela.selectOpiskeluoikeusByTyyppi('Perusopetukseen valmistava opetus'))

        it('Näytetään valitun opiskeluoikeuden tiedot', function () {
          expect(kela.getValittuOpiskeluoikeusOtsikko()).to.include('Jyväskylän normaalikoulu (2007 - 2008, Valmistunut)')
        })
      })
    })
  })

  describe('Sisäkkäisiä osasuorituksia', function () {
    before(
      Authentication().login('Laaja'),
      kela.openPage,
      kela.searchAndSelect('280618-402H', 'Aarne')
    )

    var osasuorituksenSisältö = 'Osasuoritukset Laajuus (osaamispistettä) Arviointipäivä Hyväksytty\n' +
      'Matematiikka 3 20.10.2014 kyllä\n' +
      'Fysiikka ja kemia 3 20.10.2014 kyllä'

    describe('Ennen avaamista', function () {
      it('Ei näy osasuorituksen sisältämiä tietoja', function () {
        expect(extractAsText(S('table.osasuoritukset'))).to.not.include(osasuorituksenSisältö)
      })
    })

    describe('Avataan osasuoritus', function () {
      before(kela.selectOsasuoritus('Matemaattis-luonnontieteellinen osaaminen'))

      it('Osasuoritus avautuu', function () {
        expect(extractAsText(S('table.osasuoritukset'))).to.include(osasuorituksenSisältö)
      })
    })
  })
})
