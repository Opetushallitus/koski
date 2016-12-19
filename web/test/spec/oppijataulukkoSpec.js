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

  describe('Perusopetus', function() {

    it('Näytetään tiedot', function() {
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

  describe('Filtteröinti', function() {
    describe('nimellä', function() {
      before(page.oppijataulukko.filterBy("nimi", "Koululainen Kaisa"))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Koululainen, Kaisa', 'Koululainen, Kaisa' ])
      })
    })
    describe('opiskeluoikeuden tyypillä', function() {
      before(page.oppijataulukko.filterBy("nimi", "Koululainen Kaisa"), page.oppijataulukko.filterBy("tyyppi", "Perusopetus"))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Koululainen, Kaisa' ])
      })
    })

    describe('koulutuksen tyypillä', function() {
      before(page.oppijataulukko.filterBy("nimi"), page.oppijataulukko.filterBy("tyyppi", "Perusopetus"), page.oppijataulukko.filterBy("koulutus", "Perusopetuksen oppiaineen oppimäärä"))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Oppiaineenkorottaja, Olli' ])
      })
    })

    describe('tutkinnon nimellä', function() {
      before(page.oppijataulukko.filterBy("tyyppi"), page.oppijataulukko.filterBy("koulutus"), page.oppijataulukko.filterBy("tutkinto", "telma"))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Telmanen, Tuula' ])
      })
    })

    describe('tutkintonimikkeellä', function() {
      before(page.oppijataulukko.filterBy("tyyppi"), page.oppijataulukko.filterBy("koulutus"), page.oppijataulukko.filterBy("tutkinto", "ympäristönhoitaja"))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Ammattilainen, Aarne' ])
      })
    })

    describe('osaamisalalla', function() {
      before(page.oppijataulukko.filterBy("tyyppi"), page.oppijataulukko.filterBy("koulutus"), page.oppijataulukko.filterBy("tutkinto", "ympäristöalan osaamisala"))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Ammattilainen, Aarne' ])
      })
    })

  })
})