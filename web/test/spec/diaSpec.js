describe('DIA', function( ) {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()
  var opiskeluoikeus = OpiskeluoikeusDialog()
  var addOppija = AddOppijaPage()
  before(Authentication().login(), resetFixtures)

  describe('Opiskeluoikeuden lisääminen', function() {
    describe('DIA-tutkinto', function() {
      before(
        prepareForNewOppija('kalle', '020782-5339'),
        addOppija.enterValidDataDIA({etunimet: 'Doris', kutsumanimi: 'Doris', sukunimi: 'Dia'}),
        addOppija.submitAndExpectSuccess('Dia, Doris (020782-5339)', 'Deutsche Internationale Abitur')
      )

      describe('Lisäyksen jälkeen', function () {
        describe('Opiskeluoikeuden tiedot', function() {
          it('näytetään oikein', function () {
            expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
              'Koulutus Deutsche Internationale Abitur; Reifeprüfung\n' +
              'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
              'Suorituskieli suomi\n' +
              'Suoritus kesken'
            )
          })
        })

        describe('Oppiaineita', function () {
          it('ei esitäytetä', function () {
            expect(extractAsText(S('.oppiaineet'))).to.equal('Oppiaine Laajuus (vuosiviikkotuntia)')
          })
        })

        describe('Muokattaessa', function() {
          before(editor.edit)

          it('näytetään tyhjät(kin) osa-alueet sekä Lisäaineet-ryhmä', function () {
            expect(extractAsText(S('.oppiaineet .aineryhmä'))).to.equal('' +
              'Kielet, kirjallisuus, taide\n\n' +
              'Matematiikka ja luonnontieteet\n\n' +
              'Yhteiskuntatieteet\n\n' +
              'Lisäaineet'
            )
          })

          it('näytetään jokaiselle osa-alueelle ja Lisäaineille uuden oppiaineen lisäys-dropdown', function () {
            expect(S('.oppiaineet .aineryhmä + .uusi-oppiaine .dropdown').length).to.equal(4)
          })

          after(editor.cancelChanges)
        })

        describe('Valmistavan DIA-vaiheen suorituksen lisääminen', function() {
          var lisääSuoritus = opinnot.lisääSuoritusDialog
          var lisäysTeksti = 'lisää valmistavan DIA-vaiheen suoritus'

          describe('Kun opiskeluoikeus on tilassa VALMIS', function() {
            before(editor.edit, opinnot.avaaLisaysDialogi, opiskeluoikeus.tila().aseta('valmistunut'), opiskeluoikeus.tallenna)

            it('Valmistavaa DIA-vaihetta ei voi lisätä', function() {
              expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(false)
            })

            after(editor.property('tila').removeItem(0))
          })

          describe('Kun opiskeluoikeus on tilassa LÄSNÄ', function() {
            describe('Ennen lisäystä', function() {
              it('Näytetään DIA-tutkinto', function() {
                expect(opinnot.suoritusTabs()).to.deep.equal(['Deutsche Internationale Abitur'])
              })

                it('Valmistavan DIA-vaiheen voi lisätä', function() {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(true)
              })
            })

            describe('Lisäyksen jälkeen', function() {
              before(lisääSuoritus.clickLink(lisäysTeksti))

              it('Näytetään myös valmistavan DIA-vaiheen suoritus', function() {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Deutsche Internationale Abitur',
                  'Valmistava DIA-vaihe'
                ])
              })

              it('Valmistavaa vaihetta ei voi enää lisätä', function() {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(false)
              })

              describe('Suorituksen tiedot', function() {
                before(editor.saveChanges, wait.until(page.isSavedLabelShown))

                it('näytetään oikein', function () {
                  expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
                    'Koulutus Valmistava DIA-vaihe\n' +
                    'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
                    'Suorituskieli suomi\n' +
                    'Suoritus kesken'
                  )
                })
              })
            })
          })
        })
      })
    })

    describe('Valmistava DIA-vaihe', function() {
      before(
        prepareForNewOppija('kalle', '020782-5339'),
        addOppija.enterValidDataDIAValmistavaVaihe({etunimet: 'Doris', kutsumanimi: 'Doris', sukunimi: 'Dia'}),
        addOppija.submitAndExpectSuccess('Dia, Doris (020782-5339)', 'Valmistava DIA-vaihe')
      )

      describe('Lisäyksen jälkeen', function () {
        describe('Opiskeluoikeuden tiedot', function() {
          it('näytetään oikein', function () {
            expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
              'Koulutus Valmistava DIA-vaihe\n' +
              'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
              'Suorituskieli suomi\n' +
              'Suoritus kesken'
            )
          })
        })

        describe('Oppiaineita', function () {
          it('ei esitäytetä', function () {
            expect(extractAsText(S('.oppiaineet'))).to.equal('Oppiaine Laajuus (vuosiviikkotuntia)')
          })
        })

        describe('Muokattaessa', function() {
          before(editor.edit)

          it('näytetään tyhjät(kin) osa-alueet mutta ei Lisäaineet-ryhmää', function () {
            expect(extractAsText(S('.oppiaineet .aineryhmä'))).to.equal('' +
              'Kielet, kirjallisuus, taide\n\n' +
              'Matematiikka ja luonnontieteet\n\n' +
              'Yhteiskuntatieteet'
            )
          })

          it('näytetään jokaiselle osa-alueelle uuden oppiaineen lisäys-dropdown', function () {
            expect(S('.oppiaineet .aineryhmä + .uusi-oppiaine .dropdown').length).to.equal(3)
          })

          after(editor.cancelChanges)
        })

        describe('DIA-tutkinnon suorituksen lisääminen', function() {
          var lisääSuoritus = opinnot.lisääSuoritusDialog
          var lisäysTeksti = 'lisää DIA-tutkinnon suoritus'

          describe('Kun opiskeluoikeus on tilassa VALMIS', function() {
            before(editor.edit, opinnot.avaaLisaysDialogi, opiskeluoikeus.tila().aseta('valmistunut'), opiskeluoikeus.tallenna)

            it('DIA-tutkinnon suoritusta ei voi lisätä', function() {
              expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(false)
            })

            after(editor.property('tila').removeItem(0))
          })

          describe('kun opiskeluoikeus on tilassa läsnä', function() {
            describe('Ennen lisäystä', function() {
              it('Näytetään valmistavan DIA-vaiheen suoritus', function() {
                expect(opinnot.suoritusTabs()).to.deep.equal(['Valmistava DIA-vaihe'])
              })

              it('DIA-tutkinnon suorituksen voi lisätä', function() {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(true)
              })
            })

            describe('Lisäyksen jälkeen', function() {
              before(lisääSuoritus.clickLink(lisäysTeksti))

              it('Näytetään myös DIA-tutkinnon suoritus', function() {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Valmistava DIA-vaihe',
                  'Deutsche Internationale Abitur'
                ])
              })

              it('DIA-tutkinnon suoritusta ei voi enää lisätä', function() {
                expect(lisääSuoritus.isLinkVisible(lisäysTeksti)).to.equal(false)
              })

              describe('Suorituksen tiedot', function() {
                before(editor.saveChanges, wait.until(page.isSavedLabelShown))

                it('näytetään oikein', function () {
                  expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
                    'Koulutus Deutsche Internationale Abitur; Reifeprüfung\n' +
                    'Oppilaitos / toimipiste Helsingin Saksalainen koulu\n' +
                    'Suorituskieli suomi\n' +
                    'Suoritus kesken'
                  )
                })
              })
            })
          })
        })
      })
    })
  })
})
