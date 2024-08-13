describe('Kela', function () {
  var kela = KelaPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()
  var page = KoskiPage()

  describe('Sivun latauksessa ei tapahdu virheitä', function () {
    before(Authentication().login('Laaja'), kela.openPage)

    it('ok', function () {
      expect(page.getErrorMessage()).to.equal('')
    })
  })

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
      kela.searchAndSelect('220109-784L', 'Kaisa'),
      kela.selectOpiskeluoikeusByTyyppi('Perusopetus')
    )
    it('Näytetään valitun henkilon opinnot', function () {
      expect(kela.getOppijanNimi()).to.equal('Koululainen, Kaisa (220109-784L)')
      expect(kela.getValittuOpiskeluoikeusOtsikko()).to.include(
        'Jyväskylän normaalikoulu (2008 - 2016, Valmistunut)'
      )
      expect(extractAsText(S('table.osasuoritukset'))).to.include(
        'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus'
      )
      expect(extractAsText(S('table.osasuoritukset'))).to.include(
        'B1-kieli, ruotsi'
      )
    })

    it('Kaikkien osasuoritusten yhteislaajuus', function () {
      expect(extractAsText(S('.yhteislaajuus'))).to.include(
        'Yhteislaajuus 24.5 vuosiviikkotuntia'
      )
    })

    describe('Valitaan toinen päätason suoritus', function () {
      before(kela.selectSuoritus('9. vuosiluokka'))
      it('Näytetään valitun suorituksen tiedot', function () {
        expect(extractAsText(S('.suoritukset .properties'))).to.equal(
          'Toimipiste Jyväskylän normaalikoulu\n' +
            'Tyyppi Perusopetuksen vuosiluokka\n' +
            'Alkamispäivä 2015-08-15\n' +
            'Jää luokalle ei'
        )
      })

      describe('Valitaan toinen opiskeluoikeus', function () {
        before(
          kela.selectOpiskeluoikeusByTyyppi('Perusopetukseen valmistava opetus')
        )

        it('Näytetään valitun opiskeluoikeuden tiedot', function () {
          expect(kela.getValittuOpiskeluoikeusOtsikko()).to.include(
            'Jyväskylän normaalikoulu (2017 - 2018, Valmistunut)'
          )
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

    var osasuorituksenSisältö =
      'Osasuoritukset Laajuus (osaamispistettä) Arviointipäivä Hyväksytty\n' +
      'Matematiikka 3 20.10.2014 kyllä\n' +
      'Matematiikka 3 20.10.2014 kyllä\n' +
      'Fysiikka ja kemia 2 20.10.2014 kyllä\n' +
      'Fysiikka ja kemia 3 20.10.2014 kyllä\n'

    describe('Osasuorituksen osasuoritukset on valmiiksi auki', function () {
      it('Toimii', function () {
        expect(extractAsText(S('table.osasuoritukset'))).to.include(
          osasuorituksenSisältö
        )
      })
    })

    describe('Osasuoritus voidaan sulkea', function () {
      before(
        kela.selectOsasuoritus('Matemaattis-luonnontieteellinen osaaminen')
      )

      it('Ei näy sulkemisen jälkeen', function () {
        expect(extractAsText(S('table.osasuoritukset'))).to.not.include(
          osasuorituksenSisältö
        )
      })
    })
  })

  describe('Kelan suppeilla käyttöoikeuksilla voi käyttää Kelan käyttöliittymää', function () {
    before(
      Authentication().login('Suppea'),
      kela.openPage,
      kela.searchAndSelect('220109-784L', 'Kaisa'),
      kela.selectOpiskeluoikeusByTyyppi('Perusopetus')
    )

    it('Näytetään valitun henkilon opinnot', function () {
      expect(kela.getOppijanNimi()).to.equal('Koululainen, Kaisa (220109-784L)')
      expect(kela.getValittuOpiskeluoikeusOtsikko()).to.include(
        'Jyväskylän normaalikoulu (2008 - 2016, Valmistunut)'
      )
    })
  })

  describe('Kela käyttöoikeuksilla henkilö ohjataan Kelan käyttölittymään', function () {
    before(Authentication().login('Suppea'), kela.openVirkailijaPage())

    it('Uudelleen ohjaus toimii', function () {
      expect(kela.getCurrentUrl().endsWith('/koski/kela')).to.equal(true)
    })
  })

  describe('DIA:n ensimmäisen tason osasuorituksilta piilotetaan arviointi-sarakkeet, koska näillä ei ole arviointia', function () {
    before(
      Authentication().login('Laaja'),
      kela.openPage,
      kela.searchAndSelect('151013-2195', 'Dia')
    )

    var ensimmäisenTasonOsasuoritus =
      'Osasuoritukset Laajuus (vuosiviikkotuntia)\n' + 'Äidinkieli, saksa 3'

    var toisenTasonOsasuoritus =
      'Osasuoritukset Laajuus (vuosiviikkotuntia) Arviointipäivä Hyväksytty\n' +
      '10/I 1 4.6.2016 kyllä'

    it('Ensimmäisen tason osasuorituksella ei ole arviointi-sarakkeita', function () {
      expect(extractAsText(S('table.osasuoritukset'))).to.include(
        ensimmäisenTasonOsasuoritus
      )
    })

    it('Toisen tason osasuorituksella on arviointi-sarakkeet', function () {
      expect(extractAsText(S('table.osasuoritukset'))).to.include(
        toisenTasonOsasuoritus
      )
    })
  })

  describe('Jos käännös on vain englanniksi, suomenkielinen virkailija näkee käännöksen englanniksi', function () {
    before(
      Authentication().login('Laaja'),
      kela.openPage,
      kela.searchAndSelect('040701-432D', 'Iina'),
      kela.selectSuoritus('IB-tutkinto (International Baccalaureate)')
    )

    it('Näytetään englanninkielinen käännös', function () {
      expect(extractAsText(S('table.osasuoritukset.nested'))).to.include(
        'FIN_S1'
      )
    })
  })

  describe('Jos osasuorituksella ei ole arviointia, käyttöliittymä ei näytä suoritusta hylätyksi', function () {
    before(
      Authentication().login('Laaja'),
      kela.openPage,
      kela.searchAndSelect('211097-402L', 'Antti')
    )

    it('toimii', function () {
      expect(extractAsText(S('table.osasuoritukset'))).to.include(
        'Ulkoilureittien rakentaminen ja hoitaminen\n'
      )
    })
  })

  describe('lops2019', function () {
    before(
      Authentication().login('Laaja'),
      kela.openPage,
      kela.searchAndSelect('010705A6119', 'Urho')
    )

    describe('Piilotetaan päätason suorituksen vahvistus lops2019 oppiaineen oppimäärän suorituksilta, koska tietomallissa sitä ei ole', function () {
      it('toimii', function () {
        expect(extractAsText(S('.suoritukset')).toLowerCase()).to.not.include(
          'suoritus kesken'
        )
      })
    })

    describe('Päätason suorituksen välilehdessä näytetään suorituksen tyyppi, jos koulutumoduulin tunnisteella ei ole nimeä (lops2019 oppiaineen oppimäärä', function () {
      it('toimii', function () {
        expect(extractAsText(S('.tabs'))).to.equal('Lukion aineopinnot')
      })
    })
  })

  describe('Opiskeluoikeuden tila', function () {
    before(
      Authentication().login('Laaja'),
      kela.openPage,
      kela.searchAndSelect('280618-402H', 'Aarne')
    )

    it('Näytetään rahoitusmuoto', function () {
      expect(extractAsText(S('.opiskeluoikeus.tila'))).to.include(
        'Valmistunut ( Työnantajan kokonaan rahoittama )\n'
      )
    })
  })

  describe('Versiohistoria', function () {
    var oppijanHetu = '220109-784L'

    describe('Jos versiohistoriasta ei ole valittu uutta versiota', function () {
      before(
        Authentication().login('Laaja'),
        kela.openPage,
        kela.searchAndSelect(oppijanHetu)
      )

      it('Ei näytetä "palaa versiohistoriasta yleisnäkymään"-linkkiä', function () {
        expect(kela.palaaVersiohistoriastaLinkkiIsVisible()).to.equal(false)
      })
    })

    describe('Jos opiskeluoikeudesta on vain yksi versio, valitaan se', function () {
      before(
        Authentication().login(),
        resetFixtures,
        Authentication().login('Laaja'),
        kela.openPage,
        kela.searchAndSelect(oppijanHetu),
        kela.openVersiohistoria,
        kela.selectFromVersiohistoria('1'),
        wait.until(kela.isReady),
        kela.openVersiohistoria
      )

      it('Toimii versiohistoria oikein', function () {
        expect(kela.getValittuVersioVersiohistoriasta())
          .to.be.a('string')
          .and.satisfy((str) => str.startsWith('1 '))
      })

      it('Näytetään palaa versiohistoriasta linkki', function () {
        expect(kela.palaaVersiohistoriastaLinkkiIsVisible()).to.equal(true)
      })

      describe('Palataan versiohistoriasta', function () {
        before(kela.clickPalaaVersiohistoriasta)

        it('Linkkiä ei enää näytetä', function () {
          expect(kela.palaaVersiohistoriastaLinkkiIsVisible()).to.equal(false)
        })
        it('Nähdään muut opiskeluoikeudet', function () {
          expect(extractAsText(S('.opiskeluoikeus-tabs > ul'))).to.include(
            'Perusopetus'
          )
          expect(extractAsText(S('.opiskeluoikeus-tabs > ul'))).to.include(
            'Perusopetukseen valmistava opetus'
          )
        })
      })
    })

    describe('Luodaan versiohistoriaa oppijalle', function () {
      before(
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect(oppijanHetu),
        opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('perusopetus'),
        opinnot.valitseSuoritus(undefined, 'Päättötodistus'),
        editor.edit,
        opinnot.avaaLisätiedot,
        editor.property('ulkomaanjaksot').addItem,
        editor
          .property('ulkomaanjaksot')
          .propertyBySelector('.alku')
          .setValue('18.5.2022'),
        editor.saveChanges
      )

      describe('Versiohistorian tarkastelu kelan virkailijana', function () {
        before(
          Authentication().login('Laaja'),
          kela.openPage,
          kela.searchAndSelect(oppijanHetu),
          kela.selectOpiskeluoikeusByTyyppi('Perusopetus'),
          kela.openVersiohistoria
        )

        it('Näytetään uusin versio (2) ja tämän hetkinen versio näytetään valittuna versiohistorian listassa', function () {
          expect(extractAsText(S('.ulkomaanjaksot'))).to.equal(
            'Ulkomaanjaksot 18.5.2022 -'
          )
          expect(kela.getValittuVersioVersiohistoriasta())
            .to.be.a('string')
            .and.satisfy((str) => str.startsWith('2 '))
        })

        describe('Kun valitaan versio', function () {
          before(
            kela.selectFromVersiohistoria('1'),
            wait.until(kela.isReady),
            kela.openVersiohistoria
          )

          it('Näytetään valitun version opiskeluoikeus ja valittu versio on valittuna versiohistorian listassa', function () {
            // expect(extractAsText(S('.suoritustapa'))).to.equal('Suoritustapa Koulutus')
            expect(kela.getValittuVersioVersiohistoriasta())
              .to.be.a('string')
              .and.satisfy((str) => str.startsWith('1 '))
          })
        })
      })
    })
  })
})
