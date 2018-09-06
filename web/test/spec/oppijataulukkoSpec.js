describe('Oppijataulukko', function() {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var eero = 'Esimerkki, Eero 010101-123N'
  var markkanen = 'Markkanen-Fagerström, Eéro Jorma-Petteri 080154-770R'
  var eerola = 'Eerola, Jouni 081165-793C'
  var teija = 'Tekijä, Teija 251019-039B'
  var editor = opinnot.opiskeluoikeusEditor()

  before(Authentication().login(), resetFixtures, page.openPage, wait.until(page.oppijataulukko.isReady))

  it('näytetään, kun käyttäjä on kirjautunut sisään', function() {
    expect(page.oppijataulukko.isVisible()).to.equal(true)
  })

  describe('Perusopetus', function() {
    it('Näytetään tiedot', function() {
      expect(page.oppijataulukko.findOppija('Koululainen, Kaisa', 'Perusopetus')).to.deep.equal([ 'Koululainen, Kaisa',
        'Perusopetus',
        'Perusopetuksen oppimäärä',
        'Perusopetus',
        'Valmistunut',
        'Jyväskylän normaalikoulu',
        '15.8.2008',
        '4.6.2016',
        '9C' ])
    })
  })

  describe('Haku', function() {
    describe('nimellä', function() {
      before(page.oppijataulukko.filterBy('nimi', 'Koululainen kAisa'))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Koululainen, Kaisa', 'Koululainen, Kaisa' ])
        expect(page.opiskeluoikeudeTotal()).to.equal('2')
      })
    })

    describe('opiskeluoikeuden tyypillä', function() {
      before(page.oppijataulukko.filterBy('nimi', 'Koululainen Kaisa'), page.oppijataulukko.filterBy('tyyppi', 'Perusopetus'))
      it('toimii', function() {
        expect(page.oppijataulukko.data().map(function(row) { return row[0]})).to.deep.equal([ 'Koululainen, Kaisa' ])
        expect(page.opiskeluoikeudeTotal()).to.equal('1')
      })
    })

    describe('koulutuksen tyypillä', function() {
      before(page.oppijataulukko.filterBy('nimi'), page.oppijataulukko.filterBy('tyyppi', 'Aikuisten perusopetus'), page.oppijataulukko.filterBy('koulutus', 'Perusopetuksen oppiaineen oppimäärä'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Oppiaineenkorottaja, Olli' ])
        expect(page.opiskeluoikeudeTotal()).to.equal('1')
      })
    })

    describe('tutkinnon nimellä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('koulutus'), page.oppijataulukko.filterBy('tutkinto', 'telma'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Telmanen, Tuula' ])
        expect(page.opiskeluoikeudeTotal()).to.equal('1')
      })
    })

    describe('tutkintonimikkeellä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('koulutus'), page.oppijataulukko.filterBy('tutkinto', 'ympäristönhoitaja'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Amis, Antti', 'Ammattilainen, Aarne' ])
        expect(page.opiskeluoikeudeTotal()).to.equal('2')
      })
    })

    describe('osaamisalalla', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('koulutus'), page.oppijataulukko.filterBy('tutkinto', 'ympäristöalan osaamisala'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Amis, Antti', 'Ammattilainen, Aarne', 'Osittainen, Outi' ])
        expect(page.opiskeluoikeudeTotal()).to.equal('3')
      })
    })

    describe('tilalla', function() {
      before(page.oppijataulukko.filterBy('tyyppi', 'Ammatillinen koulutus'), page.oppijataulukko.filterBy('tutkinto'), page.oppijataulukko.filterBy('tila', 'Valmistunut'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Amikseenvalmistautuja, Anneli', 'Ammattilainen, Aarne', 'Erikoinen, Erja', 'Osittainen, Outi', 'Telmanen, Tuula' ])
        expect(page.opiskeluoikeudeTotal()).to.equal('5')
      })
    })

    describe('toimipisteellä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('tila'), page.oppijataulukko.filterBy('oppilaitos', 'Ressun lukio'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal(['IB-final, Iina', 'IB-predicted, Petteri'])
        expect(page.opiskeluoikeudeTotal()).to.equal('2')
      })
    })

    describe('alkamispäivällä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('tila'),  page.oppijataulukko.filterBy('oppilaitos'), page.oppijataulukko.filterBy('alkamispäivä', '1.1.2001'))
      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Eerola, Jouni',
          'Eiperusteissa, Erkki',
          'Esimerkki, Eero',
          'Liiketalous, Lilli',
          'Markkanen-Fagerström, Eéro Jorma-Petteri',
          'Syntynyt, Sylvi',
          'Tekijä, Teija',
          'Tiedonsiirto, Tiina'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('8')
      })
    })

    describe('päättymispäivällä', function() {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tila'),
        page.oppijataulukko.filterBy('oppilaitos'),
        page.oppijataulukko.filterBy('alkamispäivä'),
        page.oppijataulukko.filterBy('päättymispäivä', '1.6.2016')
      )

      it('toimii', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Ammattilainen, Aarne',
          'Erikoinen, Erja',
          'Eskari, Essi',
          'Koululainen, Kaisa',
          'Lukioaineopiskelija, Aino'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('5')
      })
    })
  })

  describe('Sorttaus', function() {
    describe('nimellä', function() {
      before(
        page.oppijataulukko.filterBy('oppilaitos'),
        page.oppijataulukko.filterBy('tutkinto'),
        page.oppijataulukko.filterBy('tila'),
        page.oppijataulukko.filterBy('alkamispäivä'),
        page.oppijataulukko.filterBy('päättymispäivä'),
        page.oppijataulukko.filterBy('tyyppi', 'Perusopetus')
      )

      it('Oletusjärjestys nouseva nimen mukaan', function() {
        expect(page.oppijataulukko.names()).to.deep.equal([ 'Hetuton, Heikki', 'Koululainen, Kaisa', 'Lukiolainen, Liisa', 'Luokallejäänyt, Lasse', 'Monikoululainen, Miia', 'Monikoululainen, Miia', 'Oppija, Oili', 'Perusopetuksensiirto, Pertti', 'Toiminta, Tommi', 'Vuosiluokkalainen, Ville', 'Ysiluokkalainen, Ylermi', 'of Puppets, Master' ])
      })
      it('Laskeva järjestys klikkaamalla', function() {
        return page.oppijataulukko.sortBy('nimi')().then(function() {
          expect(page.oppijataulukko.names()).to.deep.equal([ 'of Puppets, Master', 'Ysiluokkalainen, Ylermi', 'Vuosiluokkalainen, Ville', 'Toiminta, Tommi', 'Perusopetuksensiirto, Pertti', 'Oppija, Oili', 'Monikoululainen, Miia', 'Monikoululainen, Miia', 'Luokallejäänyt, Lasse', 'Lukiolainen, Liisa', 'Koululainen, Kaisa', 'Hetuton, Heikki' ])
        })
      })
    })

    describe('aloituspäivällä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('tutkinto'), page.oppijataulukko.filterBy('nimi', 'koululainen'))
      it('Nouseva järjestys', function() {
        return page.oppijataulukko.sortBy('alkamispäivä')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[6]})).to.deep.equal(['15.8.2007', '15.8.2008'])
        })
      })
      it('Laskeva järjestys', function() {
        return page.oppijataulukko.sortBy('alkamispäivä')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[6]})).to.deep.equal(['15.8.2008', '15.8.2007'])
        })
      })
    })

    describe('päättymispäivällä', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('tutkinto'), page.oppijataulukko.filterBy('nimi', 'koululainen'))
      it('Nouseva järjestys', function() {
        return page.oppijataulukko.sortBy('päättymispäivä')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[7]})).to.deep.equal(['1.6.2008', '4.6.2016'])
        })
      })
      it('Laskeva järjestys', function() {
        return page.oppijataulukko.sortBy('päättymispäivä')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[7]})).to.deep.equal(['4.6.2016', '1.6.2008'])
        })
      })
    })

    describe('luokkatiedolla', function() {
      before(page.oppijataulukko.filterBy('tyyppi'), page.oppijataulukko.filterBy('tutkinto'), page.oppijataulukko.filterBy('nimi'), page.oppijataulukko.filterBy('luokka', '9'))
      it('Nouseva järjestys', function() {
        return page.oppijataulukko.sortBy('luokka')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[8]})).to.deep.equal([ '9B', '9C', '9C', '9C', '9C', '9C', '9C', '9C', '9D' ])
        })
      })
      it('Laskeva järjestys', function() {
        return page.oppijataulukko.sortBy('luokka')().then(function() {
          expect(page.oppijataulukko.data().map(function(row) { return row[8]})).to.deep.equal([ '9D', '9C', '9C', '9C', '9C', '9C', '9C', '9C', '9B' ])
        })
      })
    })
  })

  describe('Hakutekijän korostus', function() {
    before(page.oppijataulukko.filterBy('nimi', 'kaisa'), page.oppijataulukko.filterBy('tutkinto', 'perus'), page.oppijataulukko.filterBy('luokka', '9'))
    it('Toimii', function() {
      expect(page.oppijataulukko.highlights()).to.deep.equal(["Kaisa", "Perus", "9"])
    })
  })

  describe('Siirtyminen oppijan tietoihin', function() {
    before(
      page.oppijataulukko.filterBy('nimi', 'Koululainen kAisa'), page.oppijataulukko.filterBy('tutkinto', ''), page.oppijataulukko.filterBy('luokka', ''),
      page.oppijataulukko.clickFirstOppija,
      page.waitUntilOppijaSelected('220109-784L')
    )
    describe('Klikattaessa oppijan nimeä', function() {
      it('Siirrytään oppijan tietoihin', function() {

      })
    })
    describe('Klikattaessa paluulinkkiä', function() {
      before(
        editor.edit,
        editor.cancelChanges,
        wait.until(function() { return !opinnot.isEditing() }),
        opinnot.backToList
      )
      it('Säilytetään valitut hakukriteerit', function() {
        expect(page.oppijataulukko.names()).to.deep.equal(['Koululainen, Kaisa', 'Koululainen, Kaisa'])
      })
    })
  })

  describe('Opiskelijat linkki', function() {
    before(page.openFromMenu, wait.until(page.oppijataulukko.isReady))
    it('avaa oppijataulukon', function() {})
  })

  describe('Viranomaiselle', function() {
    before(Authentication().login('Eeva'), page.openPage, wait.until(page.isReady))

    it('ei näytetä', function() {
      expect(page.oppijataulukko.isVisible()).to.equal(false)
    })
  })
})
