describe('TUVA', function () {
  var opinnot = OpinnotPage()
  var tuva = TUVASuoritukset()
  var editor = opinnot.opiskeluoikeusEditor()
  var addOppija = AddOppijaPage()

  describe('Opiskeluoikeuden lisääminen', function () {
    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataTUVA(),
      addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Tutkintokoulutukseen valmentava koulutus')
    )
    it('toimii', function () {
      expect(editor.propertyBySelector('.opiskeluoikeusjakso .date span').getText()).to.equal('1.8.2021')
      expect(editor.propertyBySelector('.opiskeluoikeusjakso .tila').getText()).to.equal('Läsnä (muuta kautta rahoitettu)')
      expect(editor.propertyBySelector('.järjestämislupa').getValue()).to.equal('Perusopetuksen järjestämislupa (TUVA)')
      expect(opinnot.getTutkinto()).to.equal('Tutkintokoulutukseen valmentava koulutus')
      expect(opinnot.getOppilaitos()).to.equal('Ressun lukio')
      expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('OPH-1488-2021')
      expect(editor.propertyBySelector('.tunniste').getText()).to.equal('Tutkintokoulutukseen valmentava koulutus')
      expect(editor.propertyBySelector('.tunniste-koodiarvo').getText()).to.equal('999908')
      expect(editor.propertyBySelector('.suorituskieli .value').getText()).to.equal('suomi')
    })

    describe('Järjestämisluvan mukaiset lisätietokentät näytetään', function () {
      before(editor.edit)
      it('oikein', function () {
        opinnot.avaaLisätiedot()
        expect(extractAsText(findSingle('.lisätiedot'))).to.equal(
          'Lisätiedot\n' +
          'Koulutuksen maksuttomuus lisää uusi\n' +
          'Oikeutta maksuttomuuteen pidennetty lisää uusi\n' +
          'Ulkomaanjaksot lisää uusi\n' +
          'Muu kuin vaikeimmin kehitysvammainen lisää uusi\n' +
          'Vaikeimmin kehitysvammainen lisää uusi\n' +
          'Majoitusetu lisää\n' +
          'Kuljetusetu lisää\n' +
          'Sisäoppilaitosmainen majoitus lisää uusi\n' +
          'Koulukoti lisää uusi\n' +
          'Pidennetty oppivelvollisuus lisää\n' +
          'Pidennetty päättymispäivä'
        )
      })
      after(editor.cancelChanges)
    })

    describe('Osasuoritusten lisäys', function () {
      describe('Opiskelu- ja urasuunnittelutaidot osasuoritus', function () {
        before(
          editor.edit,
          tuva.lisääOsaSuoritus('.tuva-lisaa-osasuoritus-opiskelujaura'),
          tuva.lisääOsaSuoritus('.tuva-lisaa-osasuoritus-perustaidot'),
          editor.saveChanges
        )
        it('voidaan lisätä', function () {
          expect(extractAsText(S('.suoritus-taulukko'))).to.include('Opiskelu- ja urasuunnittelutaidot')
          expect(extractAsText(S('.suoritus-taulukko'))).to.include('Perustaitojen vahvistaminen')
        })

        describe('Osasuorituksen laajuus ja arvosana', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return tuva.selectOsasuoritus('Opiskelu- ja urasuunnittelutaidot')().property('laajuus').setValue(2)()
            },
            function () {
              return tuva.selectOsasuoritus('Opiskelu- ja urasuunnittelutaidot')().lisääArvosana('Hyväksytty')()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )
          it('voidaan lisätä', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include('Opiskelu- ja urasuunnittelutaidot 2 viikkoa Hyväksytty')
          })
        })
      })

      describe('Valinnainen osasuoritus', function () {
        before(
          editor.edit,
          tuva.lisääOsaSuoritus('.tuva-lisaa-osasuoritus-vapaavalintainen'),
          editor.saveChanges
        )
        it('voidaan lisätä', function () {
          expect(extractAsText(S('.suoritus-taulukko'))).to.include('Valinnaiset koulutuksen osat')
        })

        describe('Valinnainen paikallinen osasuoritus', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            tuva.lisääValinnainenPaikallinenSuoritus('Ohjelmointi 1'),
            function () {
              return tuva.selectOsasuoritus('Ohjelmointi 1')().property('laajuus').setValue(2)()
            },
            function () {
              return tuva.selectOsasuoritus('Ohjelmointi 1')().lisääArvosana('Hyväksytty')()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )
          it('voidaan lisätä', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include('Valinnaiset koulutuksen osat 2 viikkoa')
          })
        })

        describe('Osasuorituksen laajuus ja arvosana', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return tuva.selectOsasuoritus('Valinnaiset koulutuksen osat')().lisääArvosana('Hyväksytty', '.tuva-osasuoritusrivi-1 .dropdown-wrapper')()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )
          it('voidaan lisätä', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include('Valinnaiset koulutuksen osat 2 viikkoa Hyväksytty') //  2 viikkoa
          })
        })
      })

      describe('Osasuorituksen poistaminen', function () {
        before(editor.edit)
        it('toimii', function () {
          click('.osasuoritukset tbody:nth-child(4) .remove a')()
          expect(extractAsText(S('.suoritus-taulukko'))).to.not.include('Perustaitojen vahvistaminen')
        })
        after(editor.saveChanges)
      })

      describe('Suoritusten yhteislaajuus', function () {
        it('lasketaan oikein', function () {
          expect(extractAsText(S('.yhteislaajuus'))).to.equal('Yhteensä 4 viikkoa')
        })
      })
    })
  })
})
