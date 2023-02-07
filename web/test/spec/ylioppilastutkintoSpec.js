describe('Ylioppilastutkinto', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()

  before(Authentication().login('pää'), resetFixtures, page.openPage)

  describe('Kun tutkinto on valmis', function () {
    before(page.oppijaHaku.searchAndSelect('210244-374K'))
    describe('Tutkinnon tiedot', function () {
      it('näytetään', function () {
        expect(OpinnotPage().getTutkinto()).to.equal('Ylioppilastutkinto')
        expect(OpinnotPage().getOppilaitos()).to.equal('Helsingin medialukio')
      })

      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(S('.opiskeluoikeuden-tiedot, .suoritus > .properties'))
        ).to.equal(
          'Koulutus Ylioppilastutkinto 301000\n' +
            'Oppilaitos / toimipiste Helsingin medialukio\n' +
            'Pakolliset kokeet suoritettu kyllä\n' +
            'Koulusivistyskieli suomi'
        )
      })

      it('tila ja vahvistus', function () {
        expect(OpinnotPage().tilaJaVahvistus.tila()).to.equal('Suoritus valmis')
      })
    })

    describe('Osasuoritukset', function () {
      before(opinnot.expandAll)

      it('kaikki osasuoritukset näkyvissä', function () {
        expect(
          extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))
        ).to.equal(
          'Tutkintokerta Koe Pisteet Arvosana\n' +
            '2012 kevät Äidinkielen koe, suomi 46 Lubenter approbatur\n' +
            '2012 kevät Ruotsi, keskipitkä oppimäärä 166 Cum laude approbatur\n' +
            '2012 kevät Englanti, pitkä oppimäärä 210 Cum laude approbatur\n' +
            '2012 kevät Maantiede 26 Magna cum laude approbatur\n' +
            '2012 kevät Matematiikan koe, lyhyt oppimäärä 59 Laudatur'
        )
      })
    })
  })

  describe('Kun tutkinto on kesken', function () {
    before(page.oppijaHaku.searchAndSelect('200695-889X'))

    describe('Tutkinnon tiedot', function () {
      it('näytetään', function () {
        expect(
          OpinnotPage().opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal(['Ylioppilastutkinto'])
        expect(OpinnotPage().getTutkinto()).to.equal('Ylioppilastutkinto')
        expect(OpinnotPage().getOppilaitos()).to.equal('')
      })

      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(S('.opiskeluoikeuden-tiedot, .suoritus > .properties'))
        ).to.equal(
          'Koulutus Ylioppilastutkinto 301000\n' +
            'Oppilaitos / toimipiste Ylioppilastutkintolautakunta\n'
        )
      })

      it('tila ja vahvistus', function () {
        expect(OpinnotPage().tilaJaVahvistus.tila()).to.equal('Suoritus kesken')
      })
    })

    describe('Osasuoritukset', function () {
      before(opinnot.expandAll)

      it('kaikki osasuoritukset näkyvissä', function () {
        expect(
          extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))
        ).to.equal(
          'Tutkintokerta Koe Pisteet Arvosana\n' +
            '2012 kevät Fysiikka 18 Lubenter approbatur\n' +
            '2012 kevät Kemia 18 Lubenter approbatur'
        )
      })
    })
  })

  describe('Kypsyyskoe jne', function () {
    before(page.oppijaHaku.searchAndSelect('120674-064R'), opinnot.expandAll)

    it('kaikki osasuoritukset näkyvissä', function () {
      expect(
        extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))
      ).to.equal(
        'Tutkintokerta Koe Pisteet Arvosana\n' +
          '1998 kevät Suomi, lyhyt oppimäärä 83 Improbatur\n' +
          '1997 syksy Suomi, lyhyt oppimäärä 65 Improbatur\n' +
          '1997 kevät Suomi, lyhyt oppimäärä 67 Improbatur\n' +
          '1997 kevät Matematiikan koe, lyhyt oppimäärä 11 Approbatur\n' +
          '1997 kevät Reaali 15 Approbatur\n' +
          '1996 syksy Englanninkielinen kypsyyskoe Cum laude approbatur\n' +
          '1996 syksy Matematiikan koe, lyhyt oppimäärä 6 Improbatur\n' +
          '1996 syksy Reaali 10 Improbatur\n' +
          '1996 kevät Reaali 8 Improbatur'
      )
    })
  })

  describe('Vanha reaalikoe', function () {
    before(page.oppijaHaku.searchAndSelect('120872-781Y'), opinnot.expandAll)

    it('kaikki osasuoritukset näkyvissä', function () {
      expect(
        extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))
      ).to.equal(
        'Tutkintokerta Koe Pisteet Arvosana\n' +
          '1996 kevät Äidinkielen koe, suomi 85 Magna cum laude approbatur\n' +
          '1996 kevät Ruotsi, keskipitkä oppimäärä 160 Approbatur\n' +
          '1996 kevät Englanti, pitkä oppimäärä 220 Cum laude approbatur\n' +
          '1996 kevät Kreikka, lyhyt oppimäärä 36 Improbatur\n' +
          '1996 kevät Matematiikan koe, lyhyt oppimäärä 36 Magna cum laude approbatur\n' +
          '1996 kevät Reaali 22 Lubenter approbatur'
      )
    })
  })

  describe('Kieli puuttuu oppilastiedoista', function () {
    before(page.oppijaHaku.searchAndSelect('120674-064R'), opinnot.expandAll)

    it('kaikki osasuoritukset näkyvissä', function () {
      expect(
        extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))
      ).to.equal(
        'Tutkintokerta Koe Pisteet Arvosana\n' +
          '1998 kevät Suomi, lyhyt oppimäärä 83 Improbatur\n' +
          '1997 syksy Suomi, lyhyt oppimäärä 65 Improbatur\n' +
          '1997 kevät Suomi, lyhyt oppimäärä 67 Improbatur\n' +
          '1997 kevät Matematiikan koe, lyhyt oppimäärä 11 Approbatur\n' +
          '1997 kevät Reaali 15 Approbatur\n' +
          '1996 syksy Englanninkielinen kypsyyskoe Cum laude approbatur\n' +
          '1996 syksy Matematiikan koe, lyhyt oppimäärä 6 Improbatur\n' +
          '1996 syksy Reaali 10 Improbatur\n' +
          '1996 kevät Reaali 8 Improbatur'
      )
    })
  })

  describe('Oppilaitoksen nimi', function () {
    before(page.oppijaHaku.searchAndSelect('120872-781Y'))
    it('Näytetään oppilaitoksen nimi', function () {
      expect(OpinnotPage().getOppilaitos()).to.equal('Ressun lukio')
    })

    describe('Kun nimi on muuttunut', function () {
      before(page.oppijaHaku.searchAndSelect('080640-881R'))
      it('Näytetään oppilaitoksen nimi valmistumishetkellä', function () {
        expect(OpinnotPage().getOppilaitos()).to.equal('Ressun lukio -vanha')
      })
    })
  })

  describe('Tutkinto ennen vuotta 1990', function () {
    before(page.oppijaHaku.searchAndSelect('080845-471D'))
    it('Tila on piilotettu', function () {
      expect(OpinnotPage().tilaJaVahvistus.isVisible()).to.equal(false)
    })
  })
})
