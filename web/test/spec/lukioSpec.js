describe('Lukiokoulutus', function( ){
  var page = KoskiPage()
  var todistus = TodistusPage()
  before(resetFixtures, Authentication().login())

  describe('Lukion päättötodistus', function() {
    before(page.openPage, page.oppijaHaku.search('110496-9369', page.isOppijaSelected('Liisa')))
    describe('Oppijan suorituksissa', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Ylioppilastutkinto")
        expect(OpinnotPage().getOppilaitos()).to.equal("Jyväskylän normaalikoulu")
      })
    })
    describe('Tulostettava todistus', function() {
      before(OpinnotPage().avaaTodistus)
      it('näytetään', function() {
        expect(todistus.headings()).to.equal('Lukion päättötodistusJyväskylän yliopistoJyväskylän normaalikoulu Lukiolainen, Liisa 110496-9369')
        expect(todistus.arvosanarivi('.oppiaine.KT')).to.equal('Uskonto tai elämänkatsomustieto, Evankelisluterilainen uskonto 3 Hyvä 8')
        expect(todistus.arvosanarivi('.oppiaine.MA')).to.equal('Matematiikka, pitkä oppimäärä 15 Kiitettävä 9')
        expect(todistus.arvosanarivi('.kurssimaara')).to.equal('Opiskelijan suorittama kokonaiskurssimäärä 87,5')
        expect(todistus.vahvistus()).to.equal('Jyväskylä 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })

  describe('Opintosuoritusote', function() {
    before(page.openPage, page.oppijaHaku.search('110496-9369', page.isOppijaSelected('Liisa')))
    before(OpinnotPage().avaaOpintosuoritusote(1))

    describe('Kun klikataan linkkiä', function() {
      it('näytetään', function() {
      })
    })
  })
})