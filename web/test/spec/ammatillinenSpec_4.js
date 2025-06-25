describe('Ammatillinen koulutus 4', function () {
  before(Authentication().login())

  var addOppija = AddOppijaPage()
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  describe('Luottamuksellinen data', function () {
    before(
      page.openPage,
      page.oppijaHaku.searchAndSelect('010101-123N'),
      opinnot.expandAll
    )
    describe('Kun käyttäjällä on luottamuksellinen-rooli', function () {
      it('näkyy', function () {
        expect(extractAsText(S('.lisätiedot'))).to.equal(
          'Lisätiedot\n' +
          'Erityinen tuki 30.5.2019 —\n' +
          'Vaikeasti vammaisille järjestetty opetus 30.5.2019 —\n' +
          'Vankilaopetuksessa 30.5.2019 —'
        )
      })
    })

    describe('Kun käyttäjällä ei ole luottamuksellinen-roolia', function () {
      before(
        Authentication().logout,
        Authentication().login('stadin-vastuu'),
        page.openPage,
        page.oppijaHaku.searchAndSelect('010101-123N')
      )
      it('piilotettu', function () {
        expect(isElementVisible(S('.lisätiedot'))).to.equal(false)
      })
    })
  })

  describe('Osittaisen ammatillisen tutkinnon validaatio', function () {
    var yhteinenTutkinnonOsa = opinnot.tutkinnonOsat().tutkinnonOsa(5)
    var osanOsa0 = yhteinenTutkinnonOsa.osanOsat().tutkinnonOsa(0)
    var osanOsa1 = yhteinenTutkinnonOsa.osanOsat().tutkinnonOsa(1)
    var osanOsa2 = yhteinenTutkinnonOsa.osanOsat().tutkinnonOsa(2)

    describe('Valmiiksi merkitseminen', function () {
      before(
        resetFixtures,
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect('140493-2798'),
        editor.edit,
        editor.property('tila').removeItem(0),
        opinnot.valitseSuoritus(
          undefined,
          'Luonto- ja ympäristöalan perustutkinto, osittainen'
        ),
        opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi
      )

      describe('Keskeneräisellä yhteisen tutkinnon osalla', function () {
        describe('Kaikki osa-alueet valmiita', function () {
          before(
            yhteinenTutkinnonOsa
              .propertyBySelector('.arvosana')
              .setValue('Ei valintaa')
          )

          it('Voidaan asettaa valmiiksi', function () {
            expect(opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(
              true
            )
          })
        })

        describe('On yksi keskeräinen osa-alue', function () {
          before(
            yhteinenTutkinnonOsa.toggleExpand,
            osanOsa0.propertyBySelector('.arvosana').setValue('Ei valintaa')
          )

          it('Ei voida asettaa valmiiksi', function () {
            expect(opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(
              false
            )
          })
        })

        describe('Ei yhtään osan osa-aluetta', function () {
          before(
            osanOsa2.poistaTutkinnonOsa,
            osanOsa1.poistaTutkinnonOsa,
            osanOsa0.poistaTutkinnonOsa
          )

          it('Ei voida asettaa valmiiksi', function () {
            expect(opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(
              false
            )
          })
        })

        after(editor.cancelChanges)
      })

      // flaky
      describe.skip('Ammatillisen tutkinnon osan suoritus puuttuu, mutta opiskeluoikeuteen on sisällytetty toinen opiskeluoikeus', function () {
        var firstEditor = opinnot.opiskeluoikeusEditor(0)
        var secondEditor = opinnot.opiskeluoikeusEditor(1)
        var secondOpinnot = OpinnotPage(1)

        var clickLisääOpiskeluoikeus = function () {
          return S('li.add-opiskeluoikeus > a > span').click()
        }
        var clickValitseOppilaitosDropdown = function () {
          return S(
            'label.oppilaitos .organisaatio .organisaatio-selection'
          ).click()
        }
        var clickMuokkaaOpiskeluoikeus = function () {
          return S('div.opiskeluoikeuden-tiedot button.toggle-edit')[0].click()
        }
        var täydennäSisältyvänOpiskeluoikeudenOid = function () {
          return firstEditor
            .property('oid')
            .setValue(
              S(
                'ul.opiskeluoikeuksientiedot span.id:eq( 1 ) > span.value'
              ).text()
            )()
        }
        var tallenna = function () {
          return S('#edit-bar button.koski-button').click()
        }
        var waitAjax = function () {
          return wait.forAjax()
        }

        var clickMuokkaaSisällytettyOpiskeluoikeus = function () {
          return S('.opiskeluoikeuden-tiedot:eq(1) .koski-button').click()
        }

        before(
          resetFixtures,
          clickLisääOpiskeluoikeus,
          addOppija.selectOppilaitos('Omnia'),
          addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
          addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
          addOppija.selectTutkinto('Autoalan perustutkinto'),
          addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
          addOppija.selectAloituspäivä('1.1.2018'),
          addOppija.selectOpintojenRahoitus(
            'Valtionosuusrahoitteinen koulutus'
          ),
          addOppija.selectMaksuttomuus(0),
          addOppija.submitModal,

          clickMuokkaaOpiskeluoikeus,
          wait.forMilliseconds(50),
          firstEditor.property('sisältyyOpiskeluoikeuteen').addValue,
          firstEditor
            .property('oppilaitos')
            .organisaatioValitsin()
            .select('Stadin ammatti- ja aikuisopisto'),
          täydennäSisältyvänOpiskeluoikeudenOid,
          tallenna,
          waitAjax,
          clickMuokkaaSisällytettyOpiskeluoikeus,
          wait.forMilliseconds(50),
          secondEditor.property('tila').removeItem(0),
          secondOpinnot.valitseSuoritus(
            1,
            'Luonto- ja ympäristöalan perustutkinto, osittainen'
          ),
          TilaJaVahvistusIndeksillä(1).merkitseKeskeneräiseksi,
          secondEditor.property('ostettu').setValue(true),
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(5).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(4).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(3).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(2).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(1).poistaTutkinnonOsa,
          secondOpinnot.tutkinnonOsat().tutkinnonOsa(0).poistaTutkinnonOsa,

          TilaJaVahvistusIndeksillä(1).merkitseValmiiksi,
          TilaJaVahvistusIndeksillä(1).lisääVahvistus('1.1.2000')
        )

        it('Voidaan vahvistaa muualta ostettu opiskeluoikeus', function () {
          expect(isElementVisible(S('button.merkitse-kesken'))).to.equal(true)
        })

        after(resetFixtures)
      })
    })

    describe('Tallentaminen', function () {
      before(
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect('140493-2798')
      )

      describe('Kun päätason suoritus on valmis', function () {
        describe('Ja ammatillisen tutkinnon osaa ei ole suoritettu', function () {
          before(
            opinnot.tutkinnonOsat().tutkinnonOsa(3).poistaTutkinnonOsa,
            opinnot.tutkinnonOsat().tutkinnonOsa(2).poistaTutkinnonOsa,
            opinnot.tutkinnonOsat().tutkinnonOsa(1).poistaTutkinnonOsa,
            opinnot.tutkinnonOsat().tutkinnonOsa(0).poistaTutkinnonOsa
          )

          it('Tallentaminen on estetty', function () {
            expect(opinnot.onTallennettavissa()).to.equal(false)
          })

          after(editor.cancelChanges)
        })

        describe('Ja yhteinen tutkinnon osa on kesken', function () {
          before(
            editor.edit,
            yhteinenTutkinnonOsa
              .propertyBySelector('.arvosana')
              .setValue('Ei valintaa')
          )

          describe('Jos kaikki osan osa-alueet on valmiita', function () {
            it('Tallenntaminen on sallittu', function () {
              expect(opinnot.onTallennettavissa()).to.equal(true)
            })
          })

          describe('Jos yksi osan osa-alue on kesken', function () {
            before(
              editor.edit,
              yhteinenTutkinnonOsa.toggleExpand,
              osanOsa0.propertyBySelector('.arvosana').setValue('Ei valintaa')
            )

            it('Tallentaminen on estetty', function () {
              expect(opinnot.onTallennettavissa()).to.equal(false)
            })
          })

          describe('Jos ei ole osan osa-alueita', function () {
            before(
              editor.edit,
              osanOsa2.poistaTutkinnonOsa,
              osanOsa1.poistaTutkinnonOsa,
              osanOsa0.poistaTutkinnonOsa
            )

            it('Tallentaminen on estettyy', function () {
              expect(opinnot.onTallennettavissa()).to.equal(false)
            })
          })

          after(editor.cancelChanges)
        })
      })
    })
  })
})
