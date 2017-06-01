describe('Lokalisointi', function() {
  var page = KoskiPage()
  describe('Ruotsinkielisellä käyttäjällä', function() {
    before(Authentication().login('pärre'), resetFixtures, page.openPage)
    describe('Oppijataulukko', function() {
      it('Näyttää ruotsinkielisen tekstin, mikäli käännös löytyy', function() {
        expect(S('#logo').text()).to.equal('Studieinfo.fi')
      })
      it('Näyttää suomenkielisen tekstin, mikäli käännös puuttuu', function() {
        expect(S('.oppija-haku h3').text()).to.equal('Hae tai lisää opiskelija')
      })
    })
  })
  describe('Tekstien muokkaus', function() {
    function editLink() { return S('#topbar .edit-localizations') }
    function startEdit() { triggerEvent(editLink(), 'click') }

    describe('Tavallisella käyttäjällä', function() {
      before(Authentication().login(), resetFixtures, page.openPage)
      it('Ei näytetä', function() {
        expect(editLink().is(':visible')).to.equal(false)
      })
    })

    describe('Käyttäjällä, jolla on lokalisoinnin CRUD-oikeudet', function() {
      before(Authentication().login('pää'), resetFixtures, page.openPage)

      it('Näytetään muokkauslinkki', function() {
        expect(editLink().is(':visible')).to.equal(true)
      })

    })
  })
})