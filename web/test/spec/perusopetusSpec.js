describe('Perusopetus', function() {
  var page = KoskiPage()
  var login = LoginPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()

  before(Authentication().login(), resetFixtures)

  describe('Perusopetuksen lukuvuositodistukset ja päättötodistus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('220109-784L'))

    describe('Perusopetuksen oppimäärä', function() {
      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('toimii', function() {
          expect(S('.perusopetuksenoppimaaransuoritus:eq(0) .osasuoritukset td.oppiaine:eq(1)').text()).to.equal('B1-kieli, ruotsi')
        })
      })

      describe('Päättötodistus', function() {
        before(opinnot.avaaTodistus())
        describe('Klikattaessa linkkiä', function() {
          it('näytetään', function() {
            // See more detailed content specification in PerusopetusSpec.scala
            expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
            expect(todistus.arvosanarivi('.muut-opinnot')).to.equal('Muut valinnaiset opinnot')
          })
        })
      })
    })

    describe('Lukuvuosisuoritus', function() {
      before(TodistusPage().close, wait.until(page.isOppijaSelected('Kaisa')), opinnot.valitseSuoritus('8. vuosiluokka'))
      describe('Kaikki tiedot näkyvissä', function() {
        before(opinnot.expandAll)
        it('toimii', function() {
          expect(S('.perusopetuksenvuosiluokansuoritus:eq(0) .osasuoritukset td.oppiaine:eq(1)').text()).to.equal('B1-kieli, ruotsi')
        })
      })
      describe('Lukuvuositodistus', function() {
        before(opinnot.avaaTodistus())
        it('näytetään', function() {})
      })
    })

    describe('Virhetilanteet', function() {
      describe('Todistuksen avaaminen, kun käyttäjä ei ole kirjautunut', function() {
        before(Authentication().logout, reloadTestFrame, wait.until(login.isVisible))
        it('Näytetään login-sivu', function() {
          expect(login.isVisible()).to.equal(true)
        })
      })

      describe('Todistuksen avaaminen, kun todistusta ei löydy', function() {
        before(Authentication().login(), page.openPage, openPage('/koski/1010101010', page.is404))
        it('Näytetään 404-sivu', function() {} )
      })
    })
  })

  describe('Tietojen muuttaminen', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('220109-784L'))

    describe('Kun poistetaan päättymispäivä', function() {
      var editor = opinnot.opiskeluoikeusEditor()
      before(editor.edit, editor.property('tila').removeItem(1), editor.property('päättymispäivä').removeValue, wait.until(page.isSavedLabelShown))

      it('Alkutila', function() {
        expect(editor.property('päättymispäivä').isVisible()).to.equal(true)
      })

      describe('Muutosten näyttäminen', function() {
        it('Näytetään "Kaikki tiedot tallennettu" -teksti', function() {
          expect(page.isSavedLabelShown()).to.equal(true)
        })
      })

      describe('Palattaessa tietojen katseluun', function() {
        before(editor.doneEditing)
        it('Näytetään muuttuneet tiedot', function() {
          expect(editor.property('päättymispäivä').isVisible()).to.equal(false)
        })
      })
    })

    describe('Muokkaus', function() {
      describe('Ulkoisen järjestelmän data', function() {
        before(page.openPage, page.oppijaHaku.searchAndSelect('010675-9981'))
        it('estetty', function() {
          expect(opinnot.anythingEditable()).to.equal(false)
        })
      })

      describe('Ilman kirjoitusoikeuksia', function() {
        before(Authentication().logout, Authentication().login('omnia-katselija'), page.openPage, page.oppijaHaku.searchAndSelect('080154-770R'))
        it('estetty', function() {
          var suoritus = opinnot.suoritusEditor()
          expect(suoritus.isEditable()).to.equal(false)
        })
      })
    })
  })

  describe('Päättötodistus toiminta-alueittain', function() {
    before(Authentication().login(), page.openPage, page.oppijaHaku.searchAndSelect('031112-020J'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.perusopetuksenoppimaaransuoritus:eq(0) .osasuoritukset td.oppiaine:eq(1) .nimi').text()).to.equal('kieli ja kommunikaatio')
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in PerusopetusSpec.scala
        expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })

  describe('Perusopetuksen oppiaineen oppimäärän suoritus', function() {
    before(Authentication().login(), page.openPage, page.oppijaHaku.searchAndSelect('110738-839L'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.vahvistus .nimi').text()).to.equal('Reijo Reksi')
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        expect(todistus.headings()).to.equal('Jyväskylän yliopisto Todistus perusopetuksen oppiaineen oppimäärän suorittamisesta Jyväskylän normaalikoulu Oppiaineenkorottaja, Olli 110738-839L')
        expect(todistus.arvosanarivi('.oppiaine.AI')).to.equal('Äidinkieli ja kirjallisuus Kiitettävä 9')
        expect(todistus.arvosanarivi('.muut-opinnot')).to.equal('')
      })
    })
  })

  describe('Perusopetuksen lisäopetus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('131025-6573'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.perusopetuksenlisaopetuksensuoritus .osasuoritukset td.oppiaine:eq(0)').text()).to.equal('Äidinkieli ja kirjallisuus')
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        expect(todistus.headings()).to.equal('Jyväskylän yliopisto Todistus lisäopetuksen suorittamisesta Jyväskylän normaalikoulu Kymppiluokkalainen, Kaisa 131025-6573')
        // See more detailed content specification in PerusopetusSpec.scala
      })
    })
  })

  describe('Perusopetukseen valmistavan opetuksen todistus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('220109-784L'), opinnot.valitseOpiskeluoikeudenTyyppi('perusopetukseenvalmistavaopetus'))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.perusopetukseenvalmistavanopetuksensuoritus .osasuoritukset td.oppiaine .nimi').text()).to.equal('Äidinkieli')
      })
    })
    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
        // See more detailed content specification in PerusopetusSpec.scala
      })
    })
  })
})