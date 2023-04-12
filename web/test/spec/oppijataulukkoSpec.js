describe('Oppijataulukko', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  before(
    Authentication().login(),
    resetFixtures,
    page.openPage,
    wait.until(page.oppijataulukko.isReady)
  )

  it('näytetään, kun käyttäjä on kirjautunut sisään', function () {
    expect(page.oppijataulukko.isVisible()).to.equal(true)
  })

  describe('Perusopetus', function () {
    it('Näytetään tiedot', function () {
      expect(
        page.oppijataulukko.findOppija('Koululainen, Kaisa', 'Perusopetus')
      ).to.deep.equal([
        'Koululainen, Kaisa',
        'Perusopetus',
        'Perusopetuksen oppimäärä',
        'Perusopetus',
        'Valmistunut',
        'Jyväskylän normaalikoulu',
        '15.8.2008',
        '4.6.2016',
        '9C'
      ])
    })
  })

  describe('Haku', function () {
    describe('nimellä', function () {
      before(page.oppijataulukko.filterBy('nimi', 'Koululainen kAisa'))
      it('toimii', function () {
        expect(
          page.oppijataulukko.data().map(function (row) {
            return row[0]
          })
        ).to.deep.equal([
          'Koululainen, Kaisa',
          'Koululainen, Kaisa',
          'Koululainen, Kaisa'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('3')
      })
    })

    describe('opiskeluoikeuden tyypillä', function () {
      before(
        page.oppijataulukko.filterBy('nimi', 'Koululainen Kaisa'),
        page.oppijataulukko.filterBy('tyyppi', 'Perusopetus')
      )
      it('toimii', function () {
        expect(
          page.oppijataulukko.data().map(function (row) {
            return row[0]
          })
        ).to.deep.equal(['Koululainen, Kaisa'])
        expect(page.opiskeluoikeudeTotal()).to.equal('1')
      })
    })

    describe('koulutuksen tyypillä', function () {
      before(
        page.oppijataulukko.filterBy('nimi'),
        page.oppijataulukko.filterBy('tyyppi', 'Aikuisten perusopetus'),
        page.oppijataulukko.filterBy(
          'koulutus',
          'Perusopetuksen oppiaineen oppimäärä'
        )
      )
      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Aikuinen, AikuisAineOpiskelijaMuuKuinVos',
          'Mervi, Monioppiaineinen',
          'Oppiaineenkorottaja, Olli'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('3')
      })
    })

    describe('tutkinnon nimellä', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('koulutus'),
        page.oppijataulukko.filterBy('tutkinto', 'telma')
      )
      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal(['Telmanen, Tuula'])
        expect(page.opiskeluoikeudeTotal()).to.equal('1')
      })
    })

    describe('tutkintonimikkeellä', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('koulutus'),
        page.oppijataulukko.filterBy('tutkinto', 'ympäristönhoitaja')
      )
      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Amis, Antti',
          'Ammattilainen, Aarne',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu Historia',
          'Korhonen, Korottaja',
          'Paallekkaisia, Pekka',
          'Rikkinäinen, Kela'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('8')
      })
    })

    describe('osaamisalalla', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('koulutus'),
        page.oppijataulukko.filterBy('tutkinto', 'ympäristöalan osaamisala')
      )
      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Amis, Antti',
          'Ammatillinen-Osittainen, Raitsu',
          'Ammattilainen, Aarne',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu Historia',
          'Korhonen, Korottaja',
          'Osittainen, Outi',
          'Paallekkaisia, Pekka',
          'Rikkinäinen, Kela'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('10')
      })
    })

    describe('tilalla', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi', 'Ammatillinen koulutus'),
        page.oppijataulukko.filterBy('tutkinto'),
        page.oppijataulukko.filterBy('tila', 'Valmistunut')
      )
      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Amikseenvalmistautuja, Anneli',
          'Ammatillinen-Osittainen, Raitsu',
          'Ammattilainen, Aarne',
          'Erikoinen, Erja',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu Historia',
          'Kokonaisuuksilla, Keijo',
          'Korhonen, Korottaja',
          'Koulutusvientiläinen, Amis',
          'Osittainen, Outi',
          'Rikkinäinen, Kela',
          'Telmanen, Tuula',
          'Valviralle, Veera'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('14')
      })
    })

    describe('luokkatiedolla', function () {
      describe('jossa väliviiva', function () {
        before(
          page.oppijataulukko.filterBy('tyyppi'),
          page.oppijataulukko.filterBy('koulutus'),
          page.oppijataulukko.filterBy('tila'),
          page.oppijataulukko.filterBy('oppilaitos'),
          page.oppijataulukko.filterBy('luokka', '6-7')
        )
        it('toimii', function () {
          expect(
            page.oppijataulukko.data().map(function (row) {
              return row[8]
            })
          ).to.deep.equal(['6-7C', '6-7C'])
        })
      })
    })

    describe('toimipisteellä', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tila'),
        page.oppijataulukko.filterBy('luokka'),
        page.oppijataulukko.filterBy('oppilaitos', 'Ressun lukio')
      )
      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'aine, opiskelija',
          'dia, opiskelija',
          'ib, opiskelija',
          'IB-final, Iina',
          'IB-Pre-IB-uusilukio, Pate',
          'IB-predicted, Petteri',
          'Kurssikertyma, Eronnut Aineopiskelija',
          'Kurssikertyma, Valmistunut Aineopiskelija'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('8')
      })
    })

    describe('alkamispäivällä 1.1.2001', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tila'),
        page.oppijataulukko.filterBy('oppilaitos'),
        page.oppijataulukko.filterBy('alkamispäivä', '1.1.2001')
      )
      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Luva, Aikuisten',
          'Luva, Nuorten'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('2')
      })
    })

    describe('alkamispäivällä 30.5.2019', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tila'),
        page.oppijataulukko.filterBy('oppilaitos'),
        page.oppijataulukko.filterBy('alkamispäivä', '30.5.2019')
      )
      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Aikuinen, AikuisAineOpiskelijaMuuKuinVos',
          'Aikuinen, AikuisopiskelijaMuuKuinVos',
          'Aikuinen, MuuRahoitus',
          'Aikuinen, Vieraskielinen',
          'Aikuinen, VieraskielinenMuuKuinVos',
          'Aikuisopiskelija, Aini',
          'aikuisten, oppimaara',
          'Amikseenvalmistautuja, Anneli',
          'Amis, Antti',
          'Ammatillinen-Osittainen, Raitsu',
          'Ammattilainen, Aarne',
          'Çelik-Eerola, Jouni',
          'Dia, Dia',
          'e, erikois',
          'e, erikois',
          'Eiperusteissa, Erkki',
          'ePerusteidenKoulutuksen-koodi, Poistettu',
          'Erikoinen, Erja',
          'ErityinenTutkinto, NuortenPerusopetus',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu Historia',
          'Erkki, Eitiedossa',
          'Esimerkki, Eero',
          'Eskari, Essi',
          'Eurooppalainen, Emilia',
          'Hetuton, Heikki',
          'Historoitsija, Hiisi',
          'IB-final, Iina',
          'IB-Pre-IB-uusilukio, Pate',
          'IB-predicted, Petteri',
          'International, Ida',
          'k, kotiopetus',
          'Kelalle, Useita',
          'Kelalle, Useita',
          'Kokonaisuuksilla, Keijo',
          'Koodari, Monthy',
          'Korhonen, Korottaja',
          'Koululainen, Kaisa',
          'Koululainen, Kaisa',
          'Koululainen, Kaisa',
          'Koulutusvientiläinen, Amis',
          'Kurssikertyma, Eronnut Aineopiskelija',
          'Kurssikertyma, Oppimaara',
          'Kurssikertyma, Valmistunut Aineopiskelija',
          'Kymppiluokkalainen, Kaisa',
          'Lisä-Eskari, Essiina',
          'Lukioaineopiskelija, Aino',
          'Lukioaineopiskelija, Aktiivinen',
          'Lukiokesken, Leila',
          'Lukiolainen, Liisa',
          'Lukiolainen, Liisa',
          'Lukioonvalmistautuja, Luke',
          'Lukioonvalmistautuja2019, Luke',
          'Luokallejäänyt, Lasse',
          'Luva, Aikuisten',
          'Luva, Nuorten',
          'Maksamaa-Toikkarinen, Matti',
          'Markkanen-Fagerström, Eéro Jorma-Petteri',
          'Mervi, Monioppiaineinen',
          'Monikoululainen, Miia',
          'Monikoululainen, Miia',
          'Muu-Ammatillinen, Marjo',
          'nuorten, oppimaara',
          'o, organisaatioHistoriallinen',
          'o, organisaatioHistoriallinen',
          'of Puppets, Master',
          'of Puppets, Master',
          'Oppiaineenkorottaja, Olli',
          'Oppija, Oili',
          'Oppija, Oili',
          'Oppija, Oili',
          'Osittainen, Outi',
          'Outinen-Toikkarinen, Taimi',
          'Perusopetuksensiirto, Pertti',
          'Pieni-Kokonaisuus, Pentti',
          'Reformi, Reijo',
          'Rikkinäinen, Kela',
          'Rikko-Toikkarinen, Risto',
          'Syntynyt, Sylvi',
          't, tavallinen',
          't, tavallinen',
          'Tehtävään-Valmistava, Tauno',
          'Tekijä, Teija',
          'Telmanen, Tuula',
          'Tiedonsiirto, Tiina',
          'Toiminta, Tommi',
          'Tunnustettu, Teuvo',
          'Tupla, Toivo',
          'Turvakielto, Tero',
          'v, virheellisestiSiirretty',
          'v, virheellisestiSiirretty',
          'v, virheellisestiSiirrettyVieraskielinen',
          'v, virheellisestiSiirrettyVieraskielinen',
          'Valviralle, Veera',
          'Valviralle-Kesken, Ville',
          'Vanhanen-Toikkarinen, Vanja',
          'Vuonna 2004 syntynyt, Peruskoulu suoritettu 2021',
          'Vuonna 2004 syntynyt, Peruskoulu suoritettu ennen 2021',
          'Vuonna 2004 syntynyt, Peruskoulusta eronnut ennen 2021'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('106')
      })
    })

    describe('päättymispäivällä', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tila'),
        page.oppijataulukko.filterBy('oppilaitos'),
        page.oppijataulukko.filterBy('alkamispäivä'),
        page.oppijataulukko.filterBy('päättymispäivä', '1.6.2016')
      )

      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Ammattilainen, Aarne',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu',
          'Erityisoppilaitoksessa, Emppu Historia',
          'Eskari, Essi',
          'Kelalle, Useita',
          'Korhonen, Korottaja',
          'Koululainen, Kaisa',
          'Lisä-Eskari, Essiina',
          'Monikoululainen, Miia',
          'Rikkinäinen, Kela'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('11')
      })
    })
  })

  describe('Sorttaus', function () {
    describe('nimellä', function () {
      before(
        page.oppijataulukko.filterBy('oppilaitos'),
        page.oppijataulukko.filterBy('tutkinto'),
        page.oppijataulukko.filterBy('tila'),
        page.oppijataulukko.filterBy('alkamispäivä'),
        page.oppijataulukko.filterBy('päättymispäivä'),
        page.oppijataulukko.filterBy('tyyppi', 'Perusopetus')
      )

      it('Oletusjärjestys nouseva nimen mukaan', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'e, erikois',
          'ErityinenTutkinto, NuortenPerusopetus',
          'Hetuton, Heikki',
          'k, kotiopetus',
          'Kelalle, Useita',
          'Koululainen, Kaisa',
          'Lukiolainen, Liisa',
          'Luokallejäänyt, Lasse',
          'Monikoululainen, Miia',
          'Monikoululainen, Miia',
          'o, organisaatioHistoriallinen',
          'of Puppets, Master',
          'Oppija, Oili',
          'Perusopetuksensiirto, Pertti',
          't, tavallinen',
          'Toiminta, Tommi',
          'Tupla, Toivo',
          'v, virheellisestiSiirretty',
          'v, virheellisestiSiirrettyVieraskielinen',
          'Vuonna 2004 syntynyt, Peruskoulu suoritettu 2021',
          'Vuonna 2004 syntynyt, Peruskoulu suoritettu ennen 2021',
          'Vuonna 2004 syntynyt, Peruskoulusta eronnut ennen 2021',
          'Vuonna 2004 syntynyt ahvenanmaalle muuttanut, Peruskoulu suoritettu 2021',
          'Vuonna 2004 syntynyt maastamuuttaja, Peruskoulu suoritettu 2021',
          'Vuonna 2005 syntynyt, Peruskoulu suoritettu 2021',
          'Vuosiluokkalainen, Ville',
          'Ysiluokkalainen, Ylermi'
        ])
      })
      it('Laskeva järjestys klikkaamalla', function () {
        return page.oppijataulukko
          .sortBy('nimi')()
          .then(function () {
            expect(page.oppijataulukko.names()).to.deep.equal([
              'Ysiluokkalainen, Ylermi',
              'Vuosiluokkalainen, Ville',
              'Vuonna 2005 syntynyt, Peruskoulu suoritettu 2021',
              'Vuonna 2004 syntynyt maastamuuttaja, Peruskoulu suoritettu 2021',
              'Vuonna 2004 syntynyt ahvenanmaalle muuttanut, Peruskoulu suoritettu 2021',
              'Vuonna 2004 syntynyt, Peruskoulusta eronnut ennen 2021',
              'Vuonna 2004 syntynyt, Peruskoulu suoritettu ennen 2021',
              'Vuonna 2004 syntynyt, Peruskoulu suoritettu 2021',
              'v, virheellisestiSiirrettyVieraskielinen',
              'v, virheellisestiSiirretty',
              'Tupla, Toivo',
              'Toiminta, Tommi',
              't, tavallinen',
              'Perusopetuksensiirto, Pertti',
              'Oppija, Oili',
              'of Puppets, Master',
              'o, organisaatioHistoriallinen',
              'Monikoululainen, Miia',
              'Monikoululainen, Miia',
              'Luokallejäänyt, Lasse',
              'Lukiolainen, Liisa',
              'Koululainen, Kaisa',
              'Kelalle, Useita',
              'k, kotiopetus',
              'Hetuton, Heikki',
              'ErityinenTutkinto, NuortenPerusopetus',
              'e, erikois'
            ])
          })
      })
    })

    describe('aloituspäivällä', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tutkinto'),
        page.oppijataulukko.filterBy('nimi', 'koululainen')
      )
      it('Nouseva järjestys', function () {
        return page.oppijataulukko
          .sortBy('alkamispäivä')()
          .then(function () {
            expect(
              page.oppijataulukko.data().map(function (row) {
                return row[6]
              })
            ).to.deep.equal(['15.8.2008', '13.8.2014', '15.8.2017'])
          })
      })
      it('Laskeva järjestys', function () {
        return page.oppijataulukko
          .sortBy('alkamispäivä')()
          .then(function () {
            expect(
              page.oppijataulukko.data().map(function (row) {
                return row[6]
              })
            ).to.deep.equal(['15.8.2017', '13.8.2014', '15.8.2008'])
          })
      })
    })

    describe('päättymispäivällä', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tutkinto'),
        page.oppijataulukko.filterBy('nimi', 'koululainen')
      )
      it('Nouseva järjestys', function () {
        return page.oppijataulukko
          .sortBy('päättymispäivä')()
          .then(function () {
            expect(
              page.oppijataulukko.data().map(function (row) {
                return row[7]
              })
            ).to.deep.equal(['3.8.2015', '4.6.2016', '1.6.2018'])
          })
      })
      it('Laskeva järjestys', function () {
        return page.oppijataulukko
          .sortBy('päättymispäivä')()
          .then(function () {
            expect(
              page.oppijataulukko.data().map(function (row) {
                return row[7]
              })
            ).to.deep.equal(['1.6.2018', '4.6.2016', '3.8.2015'])
          })
      })
    })

    describe('luokkatiedolla', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tutkinto'),
        page.oppijataulukko.filterBy('nimi'),
        page.oppijataulukko.filterBy('luokka', '9')
      )
      it('Nouseva järjestys', function () {
        return page.oppijataulukko
          .sortBy('luokka')()
          .then(function () {
            expect(
              page.oppijataulukko.data().map(function (row) {
                return row[8]
              })
            ).to.deep.equal([
              '9B',
              '9C',
              '9C',
              '9C',
              '9C',
              '9C',
              '9C',
              '9C',
              '9C',
              '9D'
            ])
          })
      })
      it('Laskeva järjestys', function () {
        return page.oppijataulukko
          .sortBy('luokka')()
          .then(function () {
            expect(
              page.oppijataulukko.data().map(function (row) {
                return row[8]
              })
            ).to.deep.equal([
              '9D',
              '9C',
              '9C',
              '9C',
              '9C',
              '9C',
              '9C',
              '9C',
              '9C',
              '9B'
            ])
          })
      })
    })
  })

  describe('Hakutekijän korostus', function () {
    before(
      page.oppijataulukko.filterBy('nimi', 'kaisa'),
      page.oppijataulukko.filterBy('tutkinto', 'perus'),
      page.oppijataulukko.filterBy('luokka', '9')
    )
    it('Toimii', function () {
      expect(page.oppijataulukko.highlights()).to.deep.equal([
        'Kaisa',
        'Perus',
        '9'
      ])
    })
  })

  describe('Siirtyminen oppijan tietoihin', function () {
    before(
      page.oppijataulukko.filterBy('nimi', 'Koululainen kAisa'),
      page.oppijataulukko.filterBy('tutkinto', ''),
      page.oppijataulukko.filterBy('luokka', ''),
      page.oppijataulukko.clickFirstOppija,
      page.waitUntilOppijaSelected('220109-784L')
    )
    describe('Klikattaessa paluulinkkiä', function () {
      before(
        editor.edit,
        editor.cancelChanges,
        wait.until(function () {
          return !opinnot.isEditing()
        }),
        opinnot.backToList
      )
      it('Säilytetään valitut hakukriteerit', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Koululainen, Kaisa',
          'Koululainen, Kaisa',
          'Koululainen, Kaisa'
        ])
      })
    })
  })

  describe('Opiskelijat linkki', function () {
    before(page.openFromMenu, wait.until(page.oppijataulukko.isReady))
    it('avaa oppijataulukon', function () {})
  })

  describe('Viranomaiselle', function () {
    before(
      Authentication().login('Eeva'),
      page.openPage,
      wait.until(page.isReady)
    )

    it('ei näytetä', function () {
      expect(page.oppijataulukko.isVisible()).to.equal(false)
    })
  })

  describe('Esiopetus', function () {
    before(
      Authentication().login('esiopetus'),
      page.openPage,
      wait.until(page.isReady)
    )

    it('ei näytetä kuin oman koulun esiopetusoppijat', function () {
      expect(
        page.oppijataulukko.data().map(function (row) {
          return row[0]
        })
      ).to.deep.equal([
        'Eskari, Essi',
        'Eskari, Essi',
        'Kelalle, Useita',
        'Lisä-Eskari, Essiina'
      ])
      expect(page.opiskeluoikeudeTotal()).to.equal('4')
    })
  })

  describe('Varhaiskasvatuksen järjestäjä', function () {
    before(
      Authentication().login('hki-tallentaja'),
      page.openPage,
      wait.until(page.isReady)
    )
    var organisaatiovalitsin = OrganisaatioHaku(page.oppijataulukko.tableElem)

    describe('ei voi hakea yksittäisistä ostopalvelutoimipisteistä joihin on tallennettu opiskeluoikeuksia', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tila'),
        organisaatiovalitsin.enter()
      )

      it('toimii', function () {
        expect(organisaatiovalitsin.oppilaitokset()).not.to.deep.include(
          'Päiväkoti Touhula'
        )
      })
    })

    describe('voi filtteröidä hakusanalla Ostopalvelu/palveluseteli', function () {
      before(organisaatiovalitsin.enter('Ostopalvelu/palveluseteli'))

      it('näyttää vain Ostopalvelu/palveluseteli -valinnan eikä aliorganisaatioita sille', function () {
        expect(organisaatiovalitsin.oppilaitokset()).to.deep.equal([
          'Ostopalvelu/palveluseteli'
        ])
      })
    })

    describe('Voi valita kaikki ostopalvelutoimipisteet', function () {
      before(organisaatiovalitsin.select('Ostopalvelu/palveluseteli'))

      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal([
          'Eskari, Essi',
          'Eskari, Essi',
          'Eskari, Essi'
        ])
        expect(
          page.oppijataulukko.oppilaitokset().slice().sort()
        ).to.deep.equal([
          'Jyväskylän normaalikoulu',
          'Päiväkoti Majakka',
          'Päiväkoti Touhula'
        ])
        expect(page.opiskeluoikeudeTotal()).to.equal('3')
      })
    })
  })

  describe('Taiteen perusopetuksen järjestäjä', function () {
    before(
      Authentication().login('hki-tallentaja'),
      page.openPage,
      wait.until(page.isReady)
    )
    var organisaatiovalitsin = OrganisaatioHaku(page.oppijataulukko.tableElem)

    describe('ei voi hakea yksittäisistä hankintakoulutuksen oppilaitoksista joihin on tallennettu opiskeluoikeuksia', function () {
      before(
        page.oppijataulukko.filterBy('tyyppi'),
        page.oppijataulukko.filterBy('tila'),
        organisaatiovalitsin.enter()
      )

      it('toimii', function () {
        expect(organisaatiovalitsin.oppilaitokset()).not.to.deep.include(
          'Varsinais-Suomen kansanopisto'
        )
      })
    })

    describe('voi filtteröidä hakusanalla Taiteen perusopetuksen hankintakoulutus', function () {
      before(
        organisaatiovalitsin.enter('Taiteen perusopetus (hankintakoulutus)')
      )

      it('näyttää vain Taiteen perusopetuksen hankintakoulutus -valinnan eikä aliorganisaatioita sille', function () {
        expect(organisaatiovalitsin.oppilaitokset()).to.deep.equal([
          'Taiteen perusopetus (hankintakoulutus)'
        ])
      })
    })

    describe('Voi valita kaikki hankintakoulutuksen toimipisteet', function () {
      before(
        organisaatiovalitsin.select('Taiteen perusopetus (hankintakoulutus)')
      )

      it('toimii', function () {
        expect(page.oppijataulukko.names()).to.deep.equal(['Taiteilija, Hank'])
        expect(
          page.oppijataulukko.oppilaitokset().slice().sort()
        ).to.deep.equal(['Varsinais-Suomen kansanopisto'])
        expect(page.opiskeluoikeudeTotal()).to.equal('1')
      })
    })
  })
})
