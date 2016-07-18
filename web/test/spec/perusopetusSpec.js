describe('Perusopetus', function() {
  var page = KoskiPage()
  var login = LoginPage()
  var todistus = TodistusPage()

  before(Authentication().login(), resetFixtures)

  describe('Perusopetuksen lukuvuositodistukset ja päättötodistus', function() {
    before(page.openPage, page.oppijaHaku.search('110496-926Y', page.isOppijaSelected('Kaisa')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
        expect(OpinnotPage().getTutkinto(1)).to.equal("8. vuosiluokka")
        expect(OpinnotPage().getTutkinto(2)).to.equal("9. vuosiluokka")
        expect(OpinnotPage().getTutkinto(3)).to.equal("Peruskoulu")
      })
    })
    describe('Päättötodistus', function() {
      before(OpinnotPage().avaaTodistus(3))
      describe('Klikattaessa linkkiä', function() {
        it('näytetään', function() {
          // See more detailed content specification in PerusopetusSpec.scala
          expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
          expect(todistus.arvosanarivi('.muut-opinnot')).to.equal('Muut valinnaiset opinnot')
        })
      })
    })

    describe('Lukuvuositodistus', function() {
      before(TodistusPage().close, wait.until(page.isOppijaSelected('Kaisa')), OpinnotPage().avaaTodistus(1))
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
    before(Authentication().login(), page.openPage, page.oppijaHaku.search('130696-913E', page.isOppijaSelected('Tommi')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in PerusopetusSpec.scala
        expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })

  describe('Perusopetuksen oppiaineen oppimäärän todistus', function() {
    before(Authentication().login(), page.openPage, page.oppijaHaku.search('190596-953T', page.isOppijaSelected('Olli')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus(0))
      it('näytetään', function() {
        expect(todistus.headings()).to.equal('Jyväskylän yliopisto Todistus perusopetuksen oppiaineen oppimäärän suorittamisesta Jyväskylän normaalikoulu Oppiaineenkorottaja, Olli 190596-953T')
        expect(todistus.arvosanarivi('.oppiaine.AI')).to.equal('Äidinkieli ja kirjallisuus Kiitettävä 9')
        expect(todistus.arvosanarivi('.muut-opinnot')).to.equal('')
      })
    })
  })

  describe('Perusopetuksen lisäopetuksen todistus', function() {
    before(page.openPage, page.oppijaHaku.search('200596-9755', page.isOppijaSelected('Kaisa')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus(0))
      it('näytetään', function() {
        expect(todistus.headings()).to.equal('Jyväskylän yliopisto Todistus lisäopetuksen suorittamisesta Jyväskylän normaalikoulu Kymppiluokkalainen, Kaisa 200596-9755')
        // See more detailed content specification in PerusopetusSpec.scala
      })
    })
  })

  describe('Perusopetukseen valmistavan opetuksen todistus', function() {
    before(page.openPage, page.oppijaHaku.search('110496-926Y', page.isOppijaSelected('Kaisa')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {})
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus(0))
      it('näytetään', function() {
        expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
        // See more detailed content specification in PerusopetusSpec.scala
      })
    })
  })
})