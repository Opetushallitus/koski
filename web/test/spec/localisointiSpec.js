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
})