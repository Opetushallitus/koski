const kaikkiOrganisaatiot = [
  'Aalto-yliopisto',
  'Aapajoen koulu',
  'Helsingin Saksalainen koulu',
  'Helsingin eurooppalainen koulu',
  'Helsingin medialukio',
  'Helsingin yliopisto',
  'International School of Helsinki',
  'Itä-Suomen yliopisto',
  'Jatkuva Koulutus Oy',
  'Jyväskylän normaalikoulu',
  'Kallaveden lukio',
  'Kulosaaren ala-aste',
  'Kuopion aikuislukio',
  'Lahden ammattikorkeakoulu',
  'Omnia',
  'PK Vironniemi',
  'Ressun lukio',
  'Sockenbacka lågstadieskola',
  'Stadin ammatti- ja aikuisopisto',
  'Varsinais-Suomen kansanopisto',
  'WinNova',
  'Ylioppilastutkintolautakunta',
  'Yrkeshögskolan Arcada'
]

const esiopetuksenRaportit = [
  'Opiskeluoikeus- ja suoritustiedot',
  'VOS-tunnusluvut: oppilasmäärät'
]

const lukionRaportit = [
  'Opiskeluoikeus- ja suoritustiedot',
  'VOS-tunnusluvut: kurssikertymät',
  'VOS-tunnusluvut: opiskelijamäärät',
  'VOS-tunnusluvut: LUVA-opiskelijamäärät'
]

describe('Raporttien luominen', function () {
  var page = RaportitPage()
  var login = LoginPage()

  before(
    Authentication().login('pää'),
    page.lataaRaportointikanta,
    page.odotaRaportointikantaOnLatautunut
  )

  describe('Tietoturva', function () {
    before(Authentication().logout, page.openPage())

    it('näytetään login, kun käyttäjä ei ole kirjautunut sisään', function () {
      expect(login.isVisible()).to.equal(true)
    })
  })

  describe('Raporttikategoriat', function () {
    describe('Testipääkäyttäjä: kaikki välilehdet', function () {
      before(
        Authentication().login('pää'),
        page.openPage(),
        page.odotaRaporttikategoriat()
      )

      it('Näyttää oikeat välilehdet', function () {
        expect(page.raporttikategoriat()).to.deep.equal([
          'Esiopetus',
          'Perusopetus',
          'Perusopetukseen valmistava opetus',
          'Tutkintokoulutukseen valmentava koulutus',
          'Aikuisten perusopetus',
          'Ammatillinen koulutus',
          'Lukiokoulutus',
          'Lukiokoulutus (Lops2021)',
          'IB-koulutus',
          'Yleiset'
        ])
        expect(page.valittuRaporttikategoria()).to.equal('Esiopetus')
      })

      it('Näyttää oikeat organisaatiot oikeassa järjestyksessä', function () {
        expect(page.valittavatOrganisaatiot()).to.deep.equal([
          'Helsingin kaupunki',
          'Kulosaaren ala-aste',
          'Jyväskylän yliopisto',
          'Jyväskylän normaalikoulu',
          'Pyhtään kunta',
          'Päiväkoti Majakka',
          'Päiväkoti Touhula'
        ])
      })
    })

    describe('Testikäyttäjä kalle: kaikki välilehdet', function () {
      before(
        Authentication().login('kalle'),
        page.openPage(),
        page.odotaRaporttikategoriat()
      )

      it('Näyttää oikeat välilehdet', function () {
        expect(page.raporttikategoriat()).to.deep.equal([
          'Esiopetus',
          'Perusopetus',
          'Perusopetukseen valmistava opetus',
          'Tutkintokoulutukseen valmentava koulutus',
          'Aikuisten perusopetus',
          'Ammatillinen koulutus',
          'Lukiokoulutus',
          'Lukiokoulutus (Lops2021)',
          'IB-koulutus',
          'Yleiset'
        ])
        expect(page.valittuRaporttikategoria()).to.equal('Esiopetus')
      })
    })

    describe('Testikäyttäjä stadin-esiopetus: rajatut välilehdet', function () {
      before(
        Authentication().login('stadin-esiopetus'),
        page.openPage(),
        page.odotaRaporttikategoriat()
      )

      it('Näyttää oikeat välilehdet', function () {
        expect(page.raporttikategoriat()).to.deep.equal([
          'Esiopetus',
          'Yleiset'
        ])
      })
    })

    describe('Vaihtaa välilehdeltä toiselle', function () {
      before(
        Authentication().login('kalle'),
        page.openPage(),
        page.odotaRaporttikategoriat(),
        page.valitseRaporttikategoria(1) // Perusopetus
      )

      it('Toimii', function () {
        expect(page.valittuRaporttikategoria()).to.equal('Perusopetus')
        expect(page.otsikko()).to.equal('Perusopetuksen raportit')
      })
    })
  })

  describe('Raporttivalitsin', function () {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat()
    )

    it('Näyttää oikeat raportit latauksen jälkeen', function () {
      expect(page.raportit()).to.deep.equal(esiopetuksenRaportit)
      expect(page.valittuRaportti()).to.equal(esiopetuksenRaportit[0])
    })

    describe('Näyttää oikeat raportit kategorian vaihdon jälkeen', function () {
      before(page.valitseRaporttikategoria(6)) // Lukio

      it('Toimii', function () {
        expect(page.raportit()).to.deep.equal(lukionRaportit)
        expect(page.valittuRaportti()).to.equal(lukionRaportit[0])
      })
    })

    describe('Raportin valinta', function () {
      before(page.valitseRaportti(1))

      it('Toimii', function () {
        expect(page.valittuRaportti()).to.equal(lukionRaportit[1])
      })
    })

    describe('Ensimmäisen raportin valinta kategorian vaihtuessa', function () {
      before(page.valitseRaporttikategoria(0))

      it('Toimii', function () {
        expect(page.valittuRaportti()).to.equal(esiopetuksenRaportit[0])
      })
    })
  })

  describe('Organisaatiovalitsin', function () {
    const esiopetuksenOrganisaatiot = [
      'Jyväskylän normaalikoulu',
      'Kulosaaren ala-aste'
    ]

    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat()
    )

    it('Valittavat organisaatiot oikein', function () {
      expect(page.valittavatOrganisaatiot()).to.deep.equal(
        esiopetuksenOrganisaatiot
      )
    })

    describe('Toisen organisaation valitseminen', function () {
      before(page.valitseOrganisaatio(1))

      it('Toimii', function () {
        expect(page.valittuOrganisaatio()).to.equal(
          esiopetuksenOrganisaatiot[1]
        )
      })
    })

    describe('Raportin vaihtaminen säilyttää organisaatiovalinnan', function () {
      before(page.valitseRaportti(1))

      it('Toimii', function () {
        expect(page.valittuOrganisaatio()).to.equal(
          esiopetuksenOrganisaatiot[1]
        )
      })
    })

    describe('Raporttikategorian vaihtaminen säilyttää organisaatiovalinnan', function () {
      before(page.valitseRaporttikategoria(1))

      it('Toimii', function () {
        expect(page.valittuOrganisaatio()).to.equal(
          esiopetuksenOrganisaatiot[1]
        )
      })
    })

    describe('Raporttikategorian vaihtaminen nollaa organisaatiovalinnan, jos edellistä valintaa ei ole listassa', function () {
      before(page.valitseRaporttikategoria(4))

      it('Toimii', function () {
        expect(page.valittuOrganisaatio()).to.equal('Jyväskylän normaalikoulu')
      })
    })
  })

  describe('Organisaatiovalitsin koulutustoimijan käyttäjällä', function () {
    const esiopetuksenOrganisaatiot = [
      'Helsingin kaupunki',
      'Kulosaaren ala-aste',
      'Ostopalvelu/palveluseteli'
    ]
    const perusopetuksenOrganisaatiot = [
      'Helsingin kaupunki',
      'Kulosaaren ala-aste',
      'Stadin ammatti- ja aikuisopisto'
    ]

    before(
      Authentication().login('hki-tallentaja'),
      page.openPage(),
      page.odotaRaporttikategoriat()
    )

    it('Valittavat organisaatiot oikein esiopetukselle', function () {
      expect(page.valittavatOrganisaatiot()).to.deep.equal(
        esiopetuksenOrganisaatiot
      )
    })

    describe('Valittavat organisaatiot oikein perusopetukselle', function () {
      before(
        page.valitseRaporttikategoria(1) // Perusopetus
      )
      it('Ostopalvelu ei valittavana', function () {
        expect(page.valittavatOrganisaatiot()).to.deep.equal(
          perusopetuksenOrganisaatiot
        )
      })
    })
  })

  describe('Organisaatiovalitsimen hakutoiminto', function () {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(9), // Muut
      page.haeOrganisaatioita('helsin')
    )

    it('Rajaa organisaatiot hakusanan "helsin" perusteella', function () {
      expect(page.valittavatOrganisaatiot()).to.deep.equal([
        'Helsingin Saksalainen koulu',
        'Helsingin eurooppalainen koulu',
        'Helsingin medialukio',
        'Helsingin yliopisto',
        'International School of Helsinki'
      ])
    })
  })

  describe('Organisaatiovalitsimen hakutoiminto juuriorganisaatioille', function () {
    before(
      Authentication().login('pää'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(9), // Muut
      page.haeOrganisaatioita('itä-suomen yliopisto')
    )

    it('Rajaa näkyviin kaikki Itä-Suomen yliopiston toimipaikat', function () {
      expect(page.valittavatOrganisaatiot()).to.deep.equal([
        'Itä-Suomen yliopisto',
        /**/ 'Itä-Suomen yliopisto',
        /**/ 'Joensuun normaalikoulu',
        /**/ 'Rantakylän normaalikoulu',
        /**/ 'Savonlinnan normaalikoulu',
        /**/ 'Tulliportin normaalikoulu'
      ])
    })
  })

  describe('Organisaatiovalitsimen hakutoiminto toimipisteille', function () {
    before(
      Authentication().login('pää'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(9), // Muut
      page.haeOrganisaatioita('aikuisopisto')
    )

    it('Rajaa näkyviin kaikki Itä-Suomen yliopiston toimipaikat', function () {
      expect(page.valittavatOrganisaatiot()).to.deep.equal([
        'Espoon seudun koulutuskuntayhtymä Omnia',
        /**/ 'Omnian aikuisopisto',
        'Helsingin kaupunki',
        /**/ 'Stadin aikuisopisto',
        /**/ 'Stadin ammatti- ja aikuisopisto'
      ])
    })
  })

  describe('Päivävalitsin', function () {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat()
    )

    it('Oletuksena tämä päivä', function () {
      const today = new Date()
      const todayStr = `${today.getDate()}.${
        today.getMonth() + 1
      }.${today.getFullYear()}`
      expect(page.valitutPäivät()).to.deep.equal([todayStr])
    })

    it('Latausnappi oletuksena aktiivisena', function () {
      expect(page.latausnappiAktiivinen()).to.equal(true)
    })

    describe('Latausnappi harmaana, jos virheellinen päivämäärä', function () {
      before(page.syötäAika(0, '12.12'))

      it('Toimii', function () {
        expect(page.latausnappiAktiivinen()).to.equal(false)
      })
    })
  })

  describe('Aikajaksovalitsin', function () {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(4) // Aikuisten perusopetus
    )

    it('Oletuksena tyhjät kentät', function () {
      expect(page.valitutPäivät()).to.deep.equal(['', ''])
    })

    it('Latausnappi harmaana', function () {
      expect(page.latausnappiAktiivinen()).to.equal(false)
    })
  })

  describe('Aikajaksollinen raportti', function () {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(4) // Aikuisten perusopetus
    )

    it('Latausnappi on harmaana', function () {
      expect(page.latausnappiAktiivinen()).to.equal(false)
    })

    describe('Syötetään aikajakson alku', function () {
      before(page.syötäAika(0, '1.1.2020'))

      it('Latausnappi pysyy harmaana', function () {
        expect(page.latausnappiAktiivinen()).to.equal(false)
      })
    })

    describe('Syötetään aikajakson loppu', function () {
      before(page.syötäAika(1, '31.1.2020'))

      it('Latausnappi aktivoituu', function () {
        expect(page.latausnappiAktiivinen()).to.equal(true)
      })
    })
  })

  describe('Päällekkäisten opiskeluoikeuksien raportti', function () {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(9) // Muut
    )

    it('Näyttää organisaatiovalitsimen', function () {
      expect(page.organisaatioValitsinNäkyvillä()).to.equal(true)
    })

    it('Näyttää organisaatiovalitsimessa kaikki organisaatiot', function () {
      expect(page.valittavatOrganisaatiot()).to.deep.equal(kaikkiOrganisaatiot)
    })

    it('Raportin päivitysaika näkyy oikein', function () {
      expect(page.raportinPäivitysaika()).to.match(
        /^\d{1,2}\.\d{1,2}\.\d{4} \d{1,2}:\d{2}$/
      )
    })
  })
})
