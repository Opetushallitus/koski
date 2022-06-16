describe('VST', function () {
  var opinnot = OpinnotPage()
  var tilaJaVahvistus = opinnot.tilaJaVahvistus
  var vst = VSTSuoritukset()
  var editor = opinnot.opiskeluoikeusEditor()
  var addOppija = AddOppijaPage()
  var page = KoskiPage()

  describe('Opiskeluoikeuden lisääminen oppivelvollisten suorituksella', function () {
    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataVSTKOPS(),
      addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille')
    )

    it('toimii', function () {
      expect(opinnot.getTutkinto()).to.equal('Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille')
      expect(opinnot.getOppilaitos()).to.equal('Varsinais-Suomen kansanopisto')
      expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('OPH-58-2021')
      expect(extractAsText(S('.tunniste-koodiarvo'))).to.equal('999909')
    })

    describe('Osasuoritusten lisäys', function () {
      describe('Osaamiskokonaisuus', function () {
        before(
          editor.edit,
          vst.lisääOsaamiskokonaisuus('Aktiivinen kansalaisuus'),
          editor.saveChanges
        )

        it('voidaan lisätä', function () {
          expect(extractAsText(S('.suoritus-taulukko'))).to.include('Aktiivinen kansalaisuus')
        })

        describe('Opintokokonaisuus osasuorituksena', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return vst.selectOsasuoritus('Aktiivinen kansalaisuus')().lisääPaikallinen('Katutaide')()
            },
            function () {
              return vst.selectOsasuoritus('Katutaide')().property('laajuus').setValue(20)()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )

          it('voidaan lisätä', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include('Katutaide 20 op')
          })
        })
      })
      describe('Suuntautumisopinto', function () {
        before(
          editor.edit,
          vst.lisääSuuntautumisopinto('Valinnaiset suuntautumisopinnot'),
          editor.saveChanges
        )

        it('voidaan lisätä', function () {
          expect(extractAsText(S('.suoritus-taulukko'))).to.include('Valinnaiset suuntautumisopinnot')
        })

        describe('Muualla suoritettu opinto osasuorituksena', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return vst.selectOsasuoritus('Valinnaiset suuntautumisopinnot')().lisääMuuallaSuoritettuOpinto('Lukio-opinnot')()
            },
            function () {
              return vst.selectOsasuoritus('Lukio-opinnot')().property('laajuus').setValue(30)()
            },
            function () {
              return vst.selectOsasuoritus('Lukio-opinnot')().property('kuvaus').setValue('Lukio kuvaus')()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )

          it('voidaan lisätä', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include('Lukio-opinnot 30 op')
          })
        })
        describe('Opintokokonaisuus osasuorituksena', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return vst.selectOsasuoritus('Valinnaiset suuntautumisopinnot')().lisääPaikallinen('Valinnainen matematiikka')()
            },
            function () {
              return vst.selectOsasuoritus('Valinnainen matematiikka')().property('laajuus').setValue(5)()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )

          it('voidaan lisätä', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include('Valinnainen matematiikka 5 op')
          })
        })
      })

      describe('Suoritusten yhteislaajuus', function () {
        it('lasketaan oikein', function () {
          expect(extractAsText(S('.yhteislaajuus'))).to.equal('Yhteensä 55 op')
        })
      })
    })
  })

  describe('Kotoutuskoulutus', function () {
    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('260769-598H'),
      opinnot.avaaKaikki
    )

    it('kielisuorituksen arvioinnin taitotasot näkyvät', function () {
      expect(extractAsText(S('.kuullunYmmärtämisenTaitotaso'))).to.equal('Kuullun ymmärtämisen taitotaso Taso C2.2')
      expect(extractAsText(S('.puhumisenTaitotaso'))).to.equal('Puhumisen taitotaso Taso C2.2')
      expect(extractAsText(S('.luetunYmmärtämisenTaitotaso'))).to.equal('Luetun ymmärtämisen taitotaso Taso C2.2')
      expect(extractAsText(S('.kirjoittamisenTaitotaso'))).to.equal('Kirjoittamisen taitotaso Taso C2.2')
    })
  })

  describe('Opiskeluoikeuden lisääminen lukutaitokoulutuksella', function () {
    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataVSTLukutaito(),
      addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Kansanopistojen vapaan sivistystyön lukutaitokoulutus')
    )

    it('toimii', function () {
      expect(opinnot.getTutkinto()).to.equal('Kansanopistojen vapaan sivistystyön lukutaitokoulutus')
      expect(opinnot.getOppilaitos()).to.equal('Varsinais-Suomen kansanopisto')
      expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('OPH-2984-2017')
      expect(extractAsText(S('.tunniste-koodiarvo'))).to.equal('999911')
    })

    describe('Osasuorituksen voi lisätä', function () {
      before(
        editor.edit,
        vst.lisääLukutaitokoulutuksenKokonaisuus('Vapaan sivistystyön lukutaitokoulutuksen numeeristen taitojen suoritus'),
        function () {
          return vst.selectOsasuoritus('Vapaan sivistystyön lukutaitokoulutuksen numeeristen taitojen suoritus')().property('laajuus').setValue(5)()
        },
        function () {
          return vst.selectOsasuoritus('Vapaan sivistystyön lukutaitokoulutuksen numeeristen taitojen suoritus')().propertyBySelector('.arvosana').selectValue('Hyväksytty')()
        },
        editor.saveChanges
      )

      it('toimii', function () {
        expect(extractAsText(S('.vst-osasuoritus'))).to.include('Vapaan sivistystyön lukutaitokoulutuksen numeeristen taitojen suoritus 5 op Hyväksytty A1.1')
      })

      describe('Suorituksen merkkaaminen valmiiksi', function () {
        before(
          editor.edit,
          tilaJaVahvistus.merkitseValmiiksi,
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).setValue('Lisää henkilö'),
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).propertyBySelector('.nimi').setValue('Reijo Reksi'),
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).propertyBySelector('.titteli').setValue('rehtori'),
          tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
          editor.saveChanges
        )

        it('toimii', function () {
          expect(extractAsText(S('.tila-vahvistus'))).to.include('Suoritus valmis')
        })
      })
    })
  })

  describe('Vapaatavoitteinen VST-koulutus', function () {
    describe('Opiskeluoikeuden tilat', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.selectOppilaitos('Varsinais-Suomen'),
        addOppija.selectOpiskeluoikeudenTyyppi('Vapaan'),
        addOppija.selectOppimäärä('Vapaan sivistystyön vapaatavoitteinen koulutus'),
        addOppija.selectOpintokokonaisuus('1138 Kuvallisen ilmaisun perusteet ja välineet')
      )

      it('Näytetään tilavaihtoehtoina Hyväksytysti suoritettu ja Keskeytynyt', function() {
        expect(addOppija.opiskeluoikeudenTilat()).to.deep.equal([
          'Hyväksytysti suoritettu',
          'Keskeytynyt'
        ])
      })
    })

    describe('Opintokokonaisuus', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataVSTVapaatavoitteinen(),
        addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Vapaan sivistystyön koulutus')
      )

      it('Näytetään opintokokonaisuuden nimi oikein', function() {
        expect(extractAsText(S("[data-test-id='hyperlink-for-opintokokonaisuudet-enum-editor']"))).to.deep.equal('1138 Kuvallisen ilmaisun perusteet ja välineet')
      })
      it('Näytetään opintokokonaisuuden linkki ePerusteisiin oikein', function() {
        expect(hrefOf(S("[data-test-id='hyperlink-for-opintokokonaisuudet-enum-editor']"))).to.deep.equal('https://eperusteet.opintopolku.fi/#/fi/opintokokonaisuus/1138')
      })
    })

    describe('Opiskeluoikeuden lisääminen', function () {
        before(
          prepareForNewOppija('kalle', '230872-7258'),
          addOppija.enterValidDataVSTVapaatavoitteinen(),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Vapaan sivistystyön koulutus')
        )

        it('toimii', function () {
          expect(opinnot.getTutkinto()).to.equal('Vapaan sivistystyön koulutus')
          expect(opinnot.getOppilaitos()).to.equal('Varsinais-Suomen kansanopisto')
        })

        describe('Osasuorituksen voi lisätä', function () {
          before(
            editor.edit,
            vst.lisääPaikallinen('Paikallinen vapaan sivistystyön koulutuksen osasuoritus'),
            function () {
              return vst.selectOsasuoritus('Paikallinen vapaan sivistystyön koulutuksen osasuoritus')().property('laajuus').setValue(5)()
            },
            function () {
              return vst.selectOsasuoritus('Paikallinen vapaan sivistystyön koulutuksen osasuoritus')().propertyBySelector('tr td.arvosana').selectValue('Hyväksytty')()
            },
            tilaJaVahvistus.merkitseValmiiksi,
            tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).setValue('Lisää henkilö'),
            tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).propertyBySelector('.nimi').setValue('Reijo Reksi'),
            tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät.itemEditor(0).propertyBySelector('.titteli').setValue('rehtori'),
            tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
            editor.saveChanges
          )

          it('toimii', function () {
            expect(extractAsText(S('.vst-osasuoritus'))).to.include('Paikallinen vapaan sivistystyön koulutuksen osasuoritus 5 op')
          })

          describe('Osasuoritukselle voi lisätä osasuorituksen', function () {
            before(
              editor.edit,
              opinnot.avaaKaikki,
              function () {
                return vst.selectOsasuoritus('Paikallinen vapaan sivistystyön koulutuksen osasuoritus')().lisääPaikallinen('Osasuorituksen osasuoritus')()
              },
              function () {
                return vst.selectOsasuoritus('Osasuorituksen osasuoritus')().property('laajuus').setValue(5)()
              },
              function () {
                return vst.selectOsasuoritus('Osasuorituksen osasuoritus')().propertyBySelector('tr td.arvosana').selectValue('Hyväksytty')()
              },
              editor.saveChanges,
              opinnot.avaaKaikki
            )

            it('toimii', function () {
              expect(extractAsText(S('.suoritus-taulukko'))).to.include('Osasuorituksen osasuoritus 5 op')
            })
          })

          describe('Osasuoritus on tallennettu ja tallenetun osasuorituksen voi lisätä', function () {
            before(
              editor.edit,
              opinnot.avaaKaikki,
              vst.lisääTallennettuPaikallinen(),
              opinnot.avaaKaikki
            )

            it('toimii', function () {
              var osasuoritukset = S('.vst-osasuoritus')
              expect(osasuoritukset.length).to.equal(2)
            })
          })
        })
      })
    })
})
