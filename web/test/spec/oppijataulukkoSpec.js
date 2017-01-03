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
      before(page.oppijataulukko.filterBy('nimi', 'Koululainen kAisa'))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Koululainen, Kaisa', 'Koululainen, Kaisa' ])
      })
    })
    describe('opiskeluoikeuden tyypillä', function() {
      before(page.oppijataulukko.filterBy('nimi', 'Koululainen Kaisa'), page.oppijataulukko.filterBy('tyyppi', 'Perusopetus'))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Koululainen, Kaisa' ])
      })
    })

    describe('koulutuksen tyypillä', function() {
      before(page.oppijataulukko.filterBy('nimi'), page.oppijataulukko.filterBy('tyyppi', 'Perusopetus'), page.oppijataulukko.filterBy('koulutus', 'Perusopetuksen oppiaineen oppimäärä'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Oppiaineenkorottaja, Olli' ])
      })
    })

    describe('tutkinnon nimellä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('koulutus'), page.oppijataulukko.filterBy('tutkinto', 'telma'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Telmanen, Tuula' ])
      })
    })

    describe('tutkintonimikkeellä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('koulutus'), page.oppijataulukko.filterBy('tutkinto', 'ympäristönhoitaja'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Ammattilainen, Aarne' ])
      })
    })

    describe('osaamisalalla', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('koulutus'), page.oppijataulukko.filterBy('tutkinto', 'ympäristöalan osaamisala'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Ammattilainen, Aarne' ])
      })
    })
    describe('tilalla', function() {
      before(page.oppijataulukko.filterBy('tyyppi', 'Ammatillinen koulutus'), page.oppijataulukko.filterBy('tutkinto'), page.oppijataulukko.filterBy('tila', 'Valmistunut'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal(['Amikseenvalmistautuja, Anneli', 'Ammattilainen, Aarne', 'Erikoinen, Erja', 'Telmanen, Tuula'])
      })
    })
    describe('toimipisteellä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('tila'), page.oppijataulukko.filterBy('oppilaitos', 'Ressun lukio'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal(['IB-final, Iina', 'IB-predicted, Petteri'])
      })
    })
    describe('alkamispäivällä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('tila'),  page.oppijataulukko.filterBy('oppilaitos'), page.oppijataulukko.filterBy('alkamispäivä', '1.1.2001'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal(['Eerola, Jouni', 'Esimerkki, Eero', 'Markkanen-Fagerström, Eéro Jorma-Petteri', 'Tekijä, Teija'])
      })
    })
  })

  describe('Sorttaus', function() {
    describe('nimellä', function() {
      before(page.oppijataulukko.filterBy('oppilaitos'), page.oppijataulukko.filterBy('tutkinto'), page.oppijataulukko.filterBy('tila'), page.oppijataulukko.filterBy('alkamispäivä'), page.oppijataulukko.filterBy('tyyppi', 'Perusopetus'))
      it('Oletusjärjestys nouseva nimen mukaan', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Koululainen, Kaisa', 'Lukiolainen, Liisa', 'Oppiaineenkorottaja, Olli', 'Oppija, Oili', 'Toiminta, Tommi' ])
      })
      it('Laskeva järjestys klikkaamalla', function() {
        return page.oppijataulukko.sortBy('nimi')().then(function() {
          expect(page.oppijataulukko.names()).to.deep.equal([ 'Toiminta, Tommi', 'Oppija, Oili', 'Oppiaineenkorottaja, Olli', 'Lukiolainen, Liisa', 'Koululainen, Kaisa' ])
        })
      })
    })

    describe('aloituspäivällä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('tutkinto'), page.oppijataulukko.filterBy('nimi', 'koululainen'))
      it('Nouseva järjestys', function() {
        return page.oppijataulukko.sortBy('aloitus')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[6]})).to.deep.equal(['15.8.2007', '15.8.2008'])
        })
      })
      it('Laskeva järjestys', function() {
        return page.oppijataulukko.sortBy('aloitus')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[6]})).to.deep.equal(['15.8.2008', '15.8.2007'])
        })
      })
    })

    describe('luokkatiedolla', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('tutkinto'), page.oppijataulukko.filterBy('nimi'), page.oppijataulukko.filterBy('luokka', '9'))
      it('Nouseva järjestys', function() {
        return page.oppijataulukko.sortBy('luokka')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[7]})).to.deep.equal(['9B', '9C', '9D'])
        })
      })
      it('Laskeva järjestys', function() {
        return page.oppijataulukko.sortBy('luokka')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[7]})).to.deep.equal(['9D', '9C', '9B'])
        })
      })
    })

  })
})