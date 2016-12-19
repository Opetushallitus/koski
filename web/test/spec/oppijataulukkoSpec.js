describe('Oppijataulukko', function() {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var eero = 'Esimerkki, Eero 010101-123N'
  var markkanen = 'Markkanen-Fagerström, Eéro Jorma-Petteri 080154-770R'
  var eerola = 'Eerola, Jouni 081165-793C'
  var teija = 'Tekijä, Teija 251019-039B'

  before(Authentication().login(), resetFixtures, page.openPage, wait.until(page.oppijataulukko.isReady))

  it('näytetään, kun käyttäjä on kirjautunut sisään', function() {
    expect(page.oppijataulukko.isVisible()).to.equal(true)
  })

  it('Perusopetus', function() {
    expect(page.oppijataulukko.findOppija('Koululainen, Kaisa', 'Perusopetus')).to.deep.equal([ 'Koululainen, Kaisa',
      'Perusopetus',
      'Perusopetuksen oppimäärä',
      'Peruskoulu',
      'Valmistunut',
      'Jyväskylän normaalikoulu',
      '15.8.2008',
      '9C' ])
  })
})