const kaikkiOrganisaatiot = [
  'Aalto-yliopisto',
  'Aapajoen koulu',
  'Helsingin Saksalainen koulu',
  'Helsingin medialukio',
  'Helsingin yliopisto',
  'International School of Helsinki',
  'Itä-Suomen yliopisto',
  'Jyväskylän normaalikoulu',
  'Kulosaaren ala-aste',
  'Lahden ammattikorkeakoulu',
  'Omnia',
  'PK Vironniemi',
  'Ressun lukio',
  'Stadin ammatti- ja aikuisopisto',
  'WinNova',
  'Ylioppilastutkintolautakunta',
  'Yrkeshögskolan Arcada'
]

const esiopetuksenRaportit = [
  'Opiskeluoikeudet ja suoritustiedot',
  'VOS-rahoituslaskenta: oppijamäärät'
]

const lukionRaportit = [
  'Tarkistusraportti',
  'VOS-tunnusluvut: Kurssikertymät',
  'VOS-tunnusluvut: Opiskelijamäärät',
  'VOS-tunnusluvut: Valmistavan koulutuksen opiskelijamäärät'
]

describe('Raporttien luominen', function() {
  var page = RaportitPage()
  var login = LoginPage()

  describe('Tietoturva', function() {
    before(Authentication().logout, page.openPage())

    it('näytetään login, kun käyttäjä ei ole kirjautunut sisään', function() {
      expect(login.isVisible()).to.equal(true)
    })
  })

  describe('Raporttikategoriat', function() {
    describe('Testikäyttäjä kalle: kaikki välilehdet', function() {
      before(
        Authentication().login('kalle'),
        page.openPage(),
        page.odotaRaporttikategoriat()
      )

      it('Näyttää oikeat välilehdet', function() {
        expect(page.raporttikategoriat()).to.deep.equal([
          'Esiopetus',
          'Perusopetus',
          'Aikuisten perusopetus',
          'Ammatillinen',
          'Lukio',
          'Muut'
        ])
        expect(page.valittuRaporttikategoria()).to.equal('Esiopetus')
      })
    })

    describe('Testikäyttäjä stadin-palvelu: rajatut välilehdet', function() {
      before(
        Authentication().login('stadin-palvelu'),
        page.openPage(),
        page.odotaRaporttikategoriat()
      )

      it('Näyttää oikeat välilehdet', function() {
        expect(page.raporttikategoriat()).to.deep.equal([
          'Esiopetus',
          'Perusopetus',
          'Lukio',
          'Muut'
        ])
      })
    })

    describe('Vaihtaa välilehdeltä toiselle', function() {
      before(
        Authentication().login('kalle'),
        page.openPage(),
        page.odotaRaporttikategoriat(),
        page.valitseRaporttikategoria(1) // Perusopetus
      )

      it('Toimii', function() {
        expect(page.valittuRaporttikategoria()).to.equal('Perusopetus')
        expect(page.otsikko()).to.equal('Perusopetuksen raportit')
      })
    })
  })

  describe('Raporttivalitsin', function() {
    before(Authentication().login('kalle'), page.openPage(), page.odotaRaporttikategoriat())

    it('Näyttää oikeat raportit latauksen jälkeen', function() {
      expect(page.raportit()).to.deep.equal(esiopetuksenRaportit)
      expect(page.valittuRaportti()).to.equal(esiopetuksenRaportit[0])
    })

    describe('Näyttää oikeat raportit kategorian vaihdon jälkeen', function() {
      before(page.valitseRaporttikategoria(4)) // Lukio

      it('Toimii', function() {
        expect(page.raportit()).to.deep.equal(lukionRaportit)
        expect(page.valittuRaportti()).to.equal(lukionRaportit[0])
      })
    })

    describe('Raportin valinta', function() {
      before(page.valitseRaportti(1))

      it('Toimii', function() {
        expect(page.valittuRaportti()).to.equal(lukionRaportit[1])
      })
    })

    describe('Ensimmäisen raportin valinta kategorian vaihtuessa', function() {
      before(page.valitseRaporttikategoria(0))

      it('Toimii', function() {
        expect(page.valittuRaportti()).to.equal(esiopetuksenRaportit[0])
      })
    })
  })

  describe('Organisaatiovalitsin', function() {
    const esiopetuksenOrganisaatiot = [ 'Jyväskylän normaalikoulu', 'Kulosaaren ala-aste' ]

    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat()
    )

    it('Valittavat organisaatiot oikein', function() {
      expect(page.valittavatOrganisaatiot()).to.deep.equal(esiopetuksenOrganisaatiot)
    })

    describe('Toisen organisaation valitseminen', function() {
      before(page.valitseOrganisaatio(1))

      it('Toimii', function() {
        expect(page.valittuOrganisaatio()).to.equal(esiopetuksenOrganisaatiot[1])
      })
    })

    describe('Raportin vaihtaminen säilyttää organisaatiovalinnan', function() {
      before(page.valitseRaportti(1))

      it('Toimii', function() {
        expect(page.valittuOrganisaatio()).to.equal(esiopetuksenOrganisaatiot[1])
      })
    })

    describe('Raporttikategorian vaihtaminen säilyttää organisaatiovalinnan', function() {
      before(page.valitseRaporttikategoria(1))

      it('Toimii', function() {
        expect(page.valittuOrganisaatio()).to.equal(esiopetuksenOrganisaatiot[1])
      })
    })

    describe('Raporttikategorian vaihtaminen nollaa organisaatiovalinnan, jos edellistä valintaa ei ole listassa', function() {
      before(page.valitseRaporttikategoria(2))

      it('Toimii', function() {
        expect(page.valittuOrganisaatio()).to.equal('Jyväskylän normaalikoulu')
      })
    })
  })

  describe('Organisaatiovalitsimen hakutoiminto', function() {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(5), // Muut
      page.haeOrganisaatioita('helsin')
    )

    it('Rajaa organisaatiot hakusanan "helsin" perusteella', function() {
      expect(page.valittavatOrganisaatiot()).to.deep.equal([
        'Helsingin Saksalainen koulu',
        'Helsingin medialukio',
        'Helsingin yliopisto',
        'International School of Helsinki'
      ])
    })
  })

  describe('Päivävalitsin', function() {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat()
    )

    it('Oletuksena tämä päivä', function() {
      const today = new Date()
      const todayStr = `${today.getDate()}.${today.getMonth() + 1}.${today.getFullYear()}`
      expect(page.valitutPäivät()).to.deep.equal([todayStr])
    })

    it('Latausnappi oletuksena aktiivisena', function() {
      expect(page.latausnappiAktiivinen()).to.equal(true)
    })

    describe('Latausnappi harmaana, jos virheellinen päivämäärä', function() {
      before(page.syötäAika(0, '12.12'))

      it('Toimii', function() {
        expect(page.latausnappiAktiivinen()).to.equal(false)
      })
    })
  })

  describe('Aikajaksovalitsin', function() {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(2) // Aikuisten perusopetus
    )

    it('Oletuksena tyhjät kentät', function() {
      expect(page.valitutPäivät()).to.deep.equal(['', ''])
    })

    it('Latausnappi harmaana', function() {
      expect(page.latausnappiAktiivinen()).to.equal(false)
    })
  })

  describe('Aikajaksollinen raportti', function() {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(2) // Aikuisten perusopetus
    )

    it('Latausnappi on harmaana', function() {
      expect(page.latausnappiAktiivinen()).to.equal(false)
    })

    describe('Syötetään aikajakson alku', function() {
      before(page.syötäAika(0, '1.1.2020'))

      it('Latausnappi pysyy harmaana', function() {
        expect(page.latausnappiAktiivinen()).to.equal(false)
      })
    })

    describe('Syötetään aikajakson loppu', function() {
      before(page.syötäAika(1, '31.1.2020'))

      it('Latausnappi aktivoituu', function() {
        expect(page.latausnappiAktiivinen()).to.equal(true)
      })
    })
  })

  describe('Päällekkäisten opiskeluoikeuksien raportti', function() {
    before(
      Authentication().login('kalle'),
      page.openPage(),
      page.odotaRaporttikategoriat(),
      page.valitseRaporttikategoria(5) // Muut
    )

    it('Näyttää organisaatiovalitsimen', function() {
      expect(page.organisaatioValitsinNäkyvillä()).to.equal(true)
    })

    it('Näyttää organisaatiovalitsimessa kaikki organisaatiot', function() {
      expect(page.valittavatOrganisaatiot()).to.deep.equal(kaikkiOrganisaatiot)
    })
  })
})
