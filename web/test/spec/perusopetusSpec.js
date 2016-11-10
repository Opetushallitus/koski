describe('Perusopetus', function() {
  var page = KoskiPage()
  var login = LoginPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()

  before(Authentication().login(), resetFixtures)

  describe('Perusopetuksen lukuvuositodistukset ja päättötodistus', function() {
    before(page.openPage, page.oppijaHaku.search('220109-784L', page.isOppijaSelected('Kaisa')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(opinnot.getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
        expect(opinnot.getTutkinto(1)).to.equal("8. vuosiluokka")
        expect(opinnot.getTutkinto(2)).to.equal("9. vuosiluokka")
        expect(opinnot.getTutkinto(3)).to.equal("Peruskoulu")
      })
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.perusopetuksenoppimaaransuoritus:eq(0) .osasuoritukset .oppiaine:eq(1)').text()).to.equal('B1-kieli, ruotsi')
      })
    })

    describe('Päättötodistus', function() {
      before(opinnot.avaaTodistus(3))
      describe('Klikattaessa linkkiä', function() {
        it('näytetään', function() {
          // See more detailed content specification in PerusopetusSpec.scala
          expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
          expect(todistus.arvosanarivi('.muut-opinnot')).to.equal('Muut valinnaiset opinnot')
        })
      })
    })

    describe('Lukuvuositodistus', function() {
      before(TodistusPage().close, wait.until(page.isOppijaSelected('Kaisa')), opinnot.avaaTodistus(1))
      it('näytetään', function() {})
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

  describe('Päättötodistus toiminta-alueittain', function() {
    before(Authentication().login(), page.openPage, page.oppijaHaku.search('031112-020J', page.isOppijaSelected('Tommi')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.perusopetuksenoppimaaransuoritus:eq(0) .osasuoritukset .perusopetuksentoiminta_alueensuoritus:eq(1) .koulutusmoduuli .value').text()).to.equal('kieli ja kommunikaatio')
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

  describe('Perusopetuksen oppiaineen oppimäärän todistus', function() {
    before(Authentication().login(), page.openPage, page.oppijaHaku.search('110738-839L', page.isOppijaSelected('Olli')))
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

  describe('Perusopetuksen lisäopetuksen todistus', function() {
    before(page.openPage, page.oppijaHaku.search('131025-6573', page.isOppijaSelected('Kaisa')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.perusopetuksenlisaopetuksensuoritus .osasuoritukset .oppiaineensuoritus:eq(0) .oppiaine').text()).to.equal('Äidinkieli ja kirjallisuus')
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
    before(page.openPage, page.oppijaHaku.search('220109-784L', page.isOppijaSelected('Kaisa')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('toimii', function() {
        expect(S('.perusopetukseenvalmistavanopetuksensuoritus .osasuoritukset .oppiaine').text()).to.equal('Äidinkieli')
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