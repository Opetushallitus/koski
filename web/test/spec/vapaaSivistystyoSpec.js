describe('VST', function () {
  var opinnot = OpinnotPage()
  var tilaJaVahvistus = opinnot.tilaJaVahvistus
  var vst = VSTSuoritukset()
  var editor = opinnot.opiskeluoikeusEditor()
  var addOppija = AddOppijaPage()
  var page = KoskiPage()
  var opiskeluoikeus = OpiskeluoikeusDialog()

  describe('Opiskeluoikeuden lisääminen oppivelvollisten suorituksella', function () {
    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataVSTKOPS(),
      addOppija.submitAndExpectSuccess(
        'Tyhjä, Tero (230872-7258)',
        'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille'
      )
    )

    it('toimii', function () {
      expect(opinnot.getTutkinto()).to.equal(
        'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille'
      )
      expect(opinnot.getOppilaitos()).to.equal('Varsinais-Suomen kansanopisto')
      expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal(
        'OPH-58-2021'
      )
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
          expect(extractAsText(S('.suoritus-taulukko'))).to.include(
            'Aktiivinen kansalaisuus'
          )
        })

        describe('Opintokokonaisuus osasuorituksena', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return vst
                .selectOsasuoritus('Aktiivinen kansalaisuus')()
                .lisääPaikallinen('Katutaide')()
            },
            function () {
              return vst
                .selectOsasuoritus('Katutaide')()
                .property('laajuus')
                .setValue(20)()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )

          it('voidaan lisätä', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include(
              'Katutaide 20 op'
            )
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
          expect(extractAsText(S('.suoritus-taulukko'))).to.include(
            'Valinnaiset suuntautumisopinnot'
          )
        })

        describe('Muualla suoritettu opinto osasuorituksena', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return vst
                .selectOsasuoritus('Valinnaiset suuntautumisopinnot')()
                .lisääMuuallaSuoritettuOpinto('Lukio-opinnot')()
            },
            function () {
              return vst
                .selectOsasuoritus('Lukio-opinnot')()
                .property('laajuus')
                .setValue(30)()
            },
            function () {
              return vst
                .selectOsasuoritus('Lukio-opinnot')()
                .property('kuvaus')
                .setValue('Lukio kuvaus')()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )

          it('voidaan lisätä', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include(
              'Lukio-opinnot 30 op'
            )
          })
        })
        describe('Opintokokonaisuus osasuorituksena', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return vst
                .selectOsasuoritus('Valinnaiset suuntautumisopinnot')()
                .lisääPaikallinen('Valinnainen matematiikka')()
            },
            function () {
              return vst
                .selectOsasuoritus('Valinnainen matematiikka')()
                .property('laajuus')
                .setValue(5)()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )

          it('voidaan lisätä', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include(
              'Valinnainen matematiikka 5 op'
            )
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
      expect(extractAsText(S('.kuullunYmmärtämisenTaitotaso'))).to.equal(
        'Kuullun ymmärtämisen taitotaso Taso C2.2'
      )
      expect(extractAsText(S('.puhumisenTaitotaso'))).to.equal(
        'Puhumisen taitotaso Taso C2.2'
      )
      expect(extractAsText(S('.luetunYmmärtämisenTaitotaso'))).to.equal(
        'Luetun ymmärtämisen taitotaso Taso C2.2'
      )
      expect(extractAsText(S('.kirjoittamisenTaitotaso'))).to.equal(
        'Kirjoittamisen taitotaso Taso C2.2'
      )
    })
  })

  describe('Opiskeluoikeuden lisääminen lukutaitokoulutuksella', function () {
    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataVSTLukutaito(),
      addOppija.submitAndExpectSuccess(
        'Tyhjä, Tero (230872-7258)',
        'Lukutaitokoulutus oppivelvollisille'
      )
    )

    it('toimii', function () {
      expect(opinnot.getTutkinto()).to.equal(
        'Lukutaitokoulutus oppivelvollisille'
      )
      expect(opinnot.getOppilaitos()).to.equal('Varsinais-Suomen kansanopisto')
      expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal(
        'OPH-2984-2017'
      )
      expect(extractAsText(S('.tunniste-koodiarvo'))).to.equal('999911')
    })

    describe('Osasuorituksen voi lisätä', function () {
      before(
        editor.edit,
        vst.lisääLukutaitokoulutuksenKokonaisuus('Numeeriset taidot'),
        function () {
          return vst
            .selectOsasuoritus('Numeeriset taidot')()
            .property('laajuus')
            .setValue(5)()
        },
        function () {
          return vst
            .selectOsasuoritus('Numeeriset taidot')()
            .propertyBySelector('.arvosana')
            .selectValue('Hyväksytty')()
        },
        editor.saveChanges
      )

      it('toimii', function () {
        expect(extractAsText(S('.vst-osasuoritus'))).to.include(
          'Numeeriset taidot 5 op Hyväksytty A1.1'
        )
      })

      describe('Suorituksen merkkaaminen valmiiksi', function () {
        before(
          editor.edit,
          tilaJaVahvistus.merkitseValmiiksi,
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
            .itemEditor(0)
            .setValue('Lisää henkilö'),
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
            .itemEditor(0)
            .propertyBySelector('.nimi')
            .setValue('Reijo Reksi'),
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
            .itemEditor(0)
            .propertyBySelector('.titteli')
            .setValue('rehtori'),
          tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
          editor.saveChanges
        )

        it('toimii', function () {
          expect(extractAsText(S('.tila-vahvistus'))).to.include(
            'Suoritus valmis'
          )
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
        addOppija.selectOppimäärä(
          'Vapaan sivistystyön vapaatavoitteinen koulutus'
        ),
        addOppija.selectOpintokokonaisuus(
          '1138 Kuvallisen ilmaisun perusteet ja välineet'
        )
      )

      it('Näytetään tilavaihtoehtoina Hyväksytysti suoritettu ja Keskeytynyt', function () {
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
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'Vapaan sivistystyön koulutus'
        )
      )

      it('Näytetään opintokokonaisuuden nimi oikein', function () {
        expect(
          extractAsText(
            S("[data-testid='hyperlink-for-opintokokonaisuudet-enum-editor']")
          )
        ).to.deep.equal('1138 Kuvallisen ilmaisun perusteet ja välineet')
      })
      it('Näytetään opintokokonaisuuden linkki ePerusteisiin oikein', function () {
        expect(
          hrefOf(
            S("[data-testid='hyperlink-for-opintokokonaisuudet-enum-editor']")
          )
        ).to.deep.equal(
          'https://eperusteet.opintopolku.fi/#/fi/opintokokonaisuus/1138'
        )
      })
    })

    describe('Opiskeluoikeuden lisääminen', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataVSTVapaatavoitteinen(),
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'Vapaan sivistystyön koulutus'
        )
      )

      it('toimii', function () {
        expect(opinnot.getTutkinto()).to.equal('Vapaan sivistystyön koulutus')
        expect(opinnot.getOppilaitos()).to.equal(
          'Varsinais-Suomen kansanopisto'
        )
      })

      describe('Osasuorituksen voi lisätä', function () {
        before(
          editor.edit,
          vst.lisääPaikallinen(
            'Paikallinen vapaan sivistystyön koulutuksen osasuoritus'
          ),
          function () {
            return vst
              .selectOsasuoritus(
                'Paikallinen vapaan sivistystyön koulutuksen osasuoritus'
              )()
              .property('laajuus')
              .setValue(5)()
          },
          function () {
            return vst
              .selectOsasuoritus(
                'Paikallinen vapaan sivistystyön koulutuksen osasuoritus'
              )()
              .propertyBySelector('tr td.arvosana')
              .selectValue('Hyväksytty')()
          },
          tilaJaVahvistus.merkitseValmiiksi,
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
            .itemEditor(0)
            .setValue('Lisää henkilö'),
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
            .itemEditor(0)
            .propertyBySelector('.nimi')
            .setValue('Reijo Reksi'),
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
            .itemEditor(0)
            .propertyBySelector('.titteli')
            .setValue('rehtori'),
          tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
          editor.saveChanges
        )

        it('toimii', function () {
          expect(extractAsText(S('.vst-osasuoritus'))).to.include(
            'Paikallinen vapaan sivistystyön koulutuksen osasuoritus 5 op'
          )
        })

        describe('Osasuoritukselle voi lisätä osasuorituksen', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return vst
                .selectOsasuoritus(
                  'Paikallinen vapaan sivistystyön koulutuksen osasuoritus'
                )()
                .lisääPaikallinen('Osasuorituksen osasuoritus')()
            },
            function () {
              return vst
                .selectOsasuoritus('Osasuorituksen osasuoritus')()
                .property('laajuus')
                .setValue(5)()
            },
            function () {
              return vst
                .selectOsasuoritus('Osasuorituksen osasuoritus')()
                .propertyBySelector('tr td.arvosana')
                .selectValue('Hyväksytty')()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )

          it('toimii', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include(
              'Osasuorituksen osasuoritus 5 op'
            )
          })
        })

        describe('Osasuoritus on tallennettu ja tallennetun osasuorituksen voi lisätä', function () {
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

          after(
            editor.cancelChanges
          )
        })
      })

      describe('Opiskeluoikeuden tilan muuttaminen toimii', function () {
        before(
          editor.edit,
          opinnot.poistaViimeisinTila,
          tilaJaVahvistus.merkitseKeskeneräiseksi,
          opinnot.avaaLisaysDialogi,
          opiskeluoikeus.tila().aseta('keskeytynyt'),
          opiskeluoikeus.tallenna
        )

        it('toimii', function () {
          expect(opinnot.lisääSuoritusDialog.isLinkVisible('Lisää')).to.equal(
            false
          )
        })

        after(
          editor.saveChangesAndWaitForSuccess
        )
      })
    })
  })

  describe('Jatkuvaan oppimiseen suunnattu VST-koulutus (JOTPA)', function () {
    describe('Opiskeluoikeuden tilat', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.selectOppilaitos('Varsinais-Suomen'),
        addOppija.selectOpiskeluoikeudenTyyppi('Vapaan'),
        addOppija.selectOppimäärä('Jatkuvaan oppimiseen suunnattu'),
        addOppija.selectOpintokokonaisuus(
          '1138 Kuvallisen ilmaisun perusteet ja välineet'
        )
      )

      it('Näytetään tilavaihtoehtoina Läsnä, Hyväksytysti suoritettu ja Keskeytynyt', function () {
        expect(addOppija.opiskeluoikeudenTilat()).to.deep.equal([
          'Hyväksytysti suoritettu',
          'Keskeytynyt',
          'Läsnä'
        ])
      })
    })

    describe('Opintokokonaisuus', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataVSTJOTPA(),
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'Vapaan sivistystyön koulutus'
        )
      )

      it('Näytetään opintokokonaisuuden nimi oikein', function () {
        expect(
          extractAsText(
            S("[data-testid='hyperlink-for-opintokokonaisuudet-enum-editor']")
          )
        ).to.deep.equal('1138 Kuvallisen ilmaisun perusteet ja välineet')
      })
      it('Näytetään opintokokonaisuuden linkki ePerusteisiin oikein', function () {
        expect(
          hrefOf(
            S("[data-testid='hyperlink-for-opintokokonaisuudet-enum-editor']")
          )
        ).to.deep.equal(
          'https://eperusteet.opintopolku.fi/#/fi/opintokokonaisuus/1138'
        )
      })
    })

    describe('Opiskeluoikeuden lisääminen', function () {
      before(
        prepareForNewOppija('kalle', '230872-7258'),
        addOppija.enterValidDataVSTJOTPA(),
        addOppija.submitAndExpectSuccess(
          'Tyhjä, Tero (230872-7258)',
          'Vapaan sivistystyön koulutus'
        )
      )

      it('toimii', function () {
        expect(opinnot.getTutkinto()).to.equal('Vapaan sivistystyön koulutus')
        expect(opinnot.getOppilaitos()).to.equal(
          'Varsinais-Suomen kansanopisto'
        )
      })

      describe('Osasuorituksen voi lisätä', function () {
        before(
          editor.edit,
          vst.lisääSuorituksenLaajuus(5),
          vst.lisääPaikallinen(
            'Paikallinen vapaan sivistystyön koulutuksen osasuoritus'
          ),
          function () {
            return vst
              .selectOsasuoritus(
                'Paikallinen vapaan sivistystyön koulutuksen osasuoritus'
              )()
              .property('laajuus')
              .setValue(5)()
          },
          function () {
            return vst
              .selectOsasuoritus(
                'Paikallinen vapaan sivistystyön koulutuksen osasuoritus'
              )()
              .propertyBySelector('tr td.arvosana')
              .selectValue('Hyväksytty')()
          },
          tilaJaVahvistus.merkitseValmiiksi,
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
            .itemEditor(0)
            .setValue('Lisää henkilö'),
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
            .itemEditor(0)
            .propertyBySelector('.nimi')
            .setValue('Reijo Reksi'),
          tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
            .itemEditor(0)
            .propertyBySelector('.titteli')
            .setValue('rehtori'),
          tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
          editor.saveChanges
        )

        it('toimii', function () {
          expect(extractAsText(S('.vst-osasuoritus'))).to.include(
            'Paikallinen vapaan sivistystyön koulutuksen osasuoritus 5 op'
          )
        })

        describe('Osasuoritukselle voi lisätä osasuorituksen', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            function () {
              return vst
                .selectOsasuoritus(
                  'Paikallinen vapaan sivistystyön koulutuksen osasuoritus'
                )()
                .lisääPaikallinen('Osasuorituksen osasuoritus')()
            },
            function () {
              return vst
                .selectOsasuoritus('Osasuorituksen osasuoritus')()
                .property('laajuus')
                .setValue(5)()
            },
            function () {
              return vst
                .selectOsasuoritus('Osasuorituksen osasuoritus')()
                .propertyBySelector('tr td.arvosana')
                .selectValue('Hyväksytty')()
            },
            editor.saveChanges,
            opinnot.avaaKaikki
          )

          it('toimii', function () {
            expect(extractAsText(S('.suoritus-taulukko'))).to.include(
              'Osasuorituksen osasuoritus 5 op'
            )
          })
        })

        describe('Osasuoritus on tallennettu ja tallenetun osasuorituksen voi lisätä', function () {
          before(
            editor.edit,
            opinnot.avaaKaikki,
            vst.lisääTallennettuPaikallinenJotpa(),
            function () {
              return vst
                .selectOsasuoritus('Osasuorituksen osasuoritus')()
                .property('laajuus')
                .setValue(5)()
            },
            function () {
              return vst
                .selectOsasuoritus('Osasuorituksen osasuoritus')()
                .propertyBySelector('tr td.arvosana')
                .selectValue('Hyväksytty')()
            },
            vst.lisääSuorituksenLaajuus(10),
            editor.saveChanges
          )

          it('toimii', function () {
            var osasuoritukset = S('.vst-osasuoritus')
            expect(osasuoritukset.length).to.equal(2)
          })
        })

        describe('Opiskeluoikeuden tilan lisääminen toimii', function () {
          before(
            editor.edit,
            opinnot.avaaLisaysDialogi,
            opiskeluoikeus.tila().aseta('hyvaksytystisuoritettu'),
            opiskeluoikeus.tallenna
          )

          it('toimii', function () {
            expect(opinnot.lisääSuoritusDialog.isLinkVisible('Lisää')).to.equal(
              false
            )
          })

          after(
            editor.saveChangesAndWaitForSuccess
          )
        })
      })
    })
  })

  describe('VST-KOTO 2022', () => {
    describe('Opiskeluoikeuden luonti', function () {
      before(
        prepareForNewOppija('kalle', '130505A284V'),
        addOppija.enterHenkilötiedot(),
        addOppija.selectOppilaitos('Varsinais-Suomen'),
        addOppija.selectOpiskeluoikeudenTyyppi('Vapaan'),
        addOppija.selectOppimäärä(
          'Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutus'
        ),
        addOppija.selectPeruste('OPH-649-2022'),
        addOppija.selectMaksuttomuus(0)
      )

      describe('Opiskeluoikeutta, joka on luotu ennen 1.8.2022', function () {
        before(addOppija.selectAloituspäivä('30.7.2022'), addOppija.submit)
        it('ei voi luoda', function () {
          expectError(
            'Vapaan sivistystyön kotiutumiskoulutuksen suorituksella ei voi olla ennen 1.8.2022 aloitettuja kotoutumiskoulutuksen opetussuunnitelman 2022 mukaisia suorituksia'
          )
        })
      })

      describe('Opiskeluoikeuden, joka on luotu 1.8.2022 tai myöhemmin', function () {
        before(addOppija.selectAloituspäivä('1.8.2022'), addOppija.submit)
        it('voi luoda', function () {
          const otsikko = S('.oppija-heading')
          expect(extractAsText(otsikko)).to.include('Tyhjä, Tero (130505A284V)')
        })
      })

      describe('Kieli- ja viestintäopintojen lisäys', function () {
        const lisääKieliopinto = (nimi, laajuus, arvosana) => [
          vst.lisääKieliJaViestintäosaamisenOsasuoritus(nimi),
          vst.enterLaajuus(nimi, laajuus),
          vst.selectArvosana(nimi, arvosana)
        ]

        before(
          editor.edit,
          vst.lisääOsaamiskokonaisuus('Kieli- ja viestintäosaaminen'),
          ...lisääKieliopinto('Kirjoittaminen', 5, 'A1.1'),
          ...lisääKieliopinto('Puhuminen', 5, 'A1.1'),
          ...lisääKieliopinto('Kuullun ymmärtäminen', 10, 'A1.1'),
          ...lisääKieliopinto('Luetun ymmärtäminen', 10, 'A1.1'),
          vst.selectArvosana('Kieli- ja viestintäosaaminen', 'Hyväksytty'),
          editor.saveChanges
        )

        it('tallentaminen onnistuu', function () {})
      })

      describe('Yhteiskunta- ja työelämän opintojen lisäys', function () {
        const lisääTyöelämäopinto = (nimi, laajuus) => [
          vst.lisääYhteiskuntaJaTyöelämänOsaamisenOsasuoritus(nimi),
          vst.enterLaajuus(nimi, laajuus)
        ]

        before(
          editor.edit,
          vst.lisääOsaamiskokonaisuus('Yhteiskunta- ja työelämäosaaminen'),
          ...lisääTyöelämäopinto('Ammatti- ja koulutuspalvelut', 5),
          ...lisääTyöelämäopinto('Työelämätietous', 5),
          ...lisääTyöelämäopinto('Työssäoppiminen', 5),
          vst.selectArvosana('Yhteiskunta- ja työelämäosaaminen', 'Hyväksytty'),
          editor.saveChangesAndExpectError
        )

        it('ei onnistu liian vähillä opintopisteillä', function () {
          expectError(
            "Oppiaineen 'Yhteiskunta- ja työelämäosaaminen' suoritettu laajuus liian suppea (15.0 op, pitäisi olla vähintään 20.0 op)"
          )
        })

        describe('Lisäämällä opintoihin yhteiskunnan peruspalvelut, 5 op', function () {
          before(
            ...lisääTyöelämäopinto('Yhteiskunnan peruspalvelut', 5),
            editor.saveChangesAndExpectError
          )

          it('ei vieläkään onnistu, koska työssäoppimisessa liian vähän opintopisteitä', function () {
            expectError(
              "Oppiaineen 'Työssäoppiminen' suoritettu laajuus liian suppea (5.0 op, pitäisi olla vähintään 8.0 op)"
            )
          })

          describe('Mutta lisäämällä 3 op työssäoppimista', function () {
            before(
              ...lisääTyöelämäopinto('Työssäoppiminen', 3),
              editor.saveChanges
            )

            it('tallentaminen onnistuu', function () {})

            describe('Opiskeluoikeuden tilan lisääminen toimii', function () {
              before(
                editor.edit,
                opinnot.avaaLisaysDialogi,
                opiskeluoikeus.tila().aseta('katsotaaneronneeksi'),
                opiskeluoikeus.tallenna
              )

              it('toimii', function () {
                expect(
                  opinnot.lisääSuoritusDialog.isLinkVisible('Lisää')
                ).to.equal(false)
              })
            })
          })
        })
      })
    })
  })
})

function expectError(error) {
  const virhe = S('#error')
  expect(extractAsText(virhe)).to.include(error)
}
