describe('Perusopetus', function() {
  var page = KoskiPage()
  var login = LoginPage()
  var todistus = TodistusPage()

  describe('Perusopetuksen päättötodistus', function() {
    before(page.openPage, page.oppijaHaku.search('110496-926Y', page.isOppijaSelected('Kaisa')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Peruskoulu")
        expect(OpinnotPage().getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
      })
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus)
      describe('Klikattaessa linkkiä', function() {
        it('näytetään', function() {
          expect(todistus.headings()).to.equal('Jyväskylän yliopisto Perusopetuksen päättötodistus Jyväskylän normaalikoulu Koululainen, Kaisa 110496-926Y')
          expect(todistus.arvosanarivi('.oppiaine.KT')).to.equal('Uskonto tai elämänkatsomustieto, Evankelisluterilainen uskonto Erinomainen 10')
          expect(todistus.arvosanarivi('.oppiaine.KO.valinnainen')).to.equal('Valinnainen kotitalous 1.0 Hyväksytty')
          expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
          expect(todistus.arvosanarivi('.muut-opinnot')).to.equal('Muut valinnaiset opinnot')
        })
      })
      describe('Todistuksen avaaminen, kun käyttäjä ei ole kirjautunut', function() {
        before(Authentication().logout,  reloadTestFrame, wait.until(login.isVisible))
        it('Näytetään login-sivu', function() {
          expect(login.isVisible()).to.equal(true)
        })
      })

      describe('Todistuksen avaaminen, kun todistusta ei löydy', function() {
        before(Authentication().login(), page.openPage, openPage('/koski/todistus/opiskeluoikeus/1010101010', page.is404))
        it('Näytetään 404-sivu', function() {

        })
      })
    })
  })
  describe('Perusopetuksen oppiaineen oppimäärän todistus', function() {
    before(Authentication().login(), page.openPage, page.oppijaHaku.search('190596-953T', page.isOppijaSelected('Olli')), OpinnotPage().avaaTodistus)
    it('näytetään', function() {
      expect(todistus.headings()).to.equal('Jyväskylän yliopisto Todistus perusopetuksen oppiaineen oppimäärän suorittamisesta Jyväskylän normaalikoulu Oppiaineenkorottaja, Olli 190596-953T')
      expect(todistus.arvosanarivi('.oppiaine.AI')).to.equal('Äidinkieli ja kirjallisuus Kiitettävä 9')
      expect(todistus.arvosanarivi('.muut-opinnot')).to.equal('')
    })
  })

  describe('Perusopetuksen lisäopetuksen todistus', function() {
    before(page.openPage, page.oppijaHaku.search('200596-9755', page.isOppijaSelected('Kaisa')), OpinnotPage().avaaTodistus)
    it('näytetään', function() {
      expect(todistus.headings()).to.equal('Jyväskylän yliopisto Todistus lisäopetuksen suorittamisesta Jyväskylän normaalikoulu Kymppiluokkalainen, Kaisa 200596-9755')
    })
  })
})