describe('Ylioppilastutkinto', function( ){
  var page = KoskiPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()

  before(Authentication().login('pää'), resetFixtures)

  describe('Kun tutkinto on valmis', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('010696-971K'))
    describe('Tutkinnon tiedot', function() {
      it('näytetään', function() {
        expect(OpinnotPage().getTutkinto()).to.equal("Ylioppilastutkinto")
        expect(OpinnotPage().getOppilaitos()).to.equal("Helsingin medialukio")
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot, .suoritus > .properties'))).to.equal(
          'Koulutus Ylioppilastutkinto\n' +
          'Oppilaitos / toimipiste Helsingin medialukio\n' +
          'Pakolliset kokeet suoritettu kyllä')
      })

      it('tila ja vahvistus', function() {
        expect(OpinnotPage().tilaJaVahvistus.tila()).to.equal('Suoritus valmis')
      })
    })


    describe('Osasuoritukset', function() {
      before(opinnot.expandAll)

      it('kaikki osasuoritukset näkyvissä', function() {
        expect(extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))).to.equal('Tutkintokerta Koe Arvosana\n' +
          '2012 kevät Äidinkielen koe, suomi Lubenter approbatur\n' +
          '2012 kevät Ruotsi, keskipitkä oppimäärä Cum laude approbatur\n' +
          '2012 kevät Englanti, pitkä oppimäärä Cum laude approbatur\n' +
          '2012 kevät Maantiede Magna cum laude approbatur\n2012 kevät Matematiikan koe, lyhyt oppimäärä Laudatur'
        )
      })
    })

    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus())
      it('näytetään', function() {
        // See more detailed content specification in YlioppilastutkintoSpec.scala
      })
    })
  })

  describe('Kun tutkinto on kesken', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('200695-889X'))

    describe('Tutkinnon tiedot', function() {
      it('näytetään', function() {
        expect(OpinnotPage().opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.deep.equal(['Ylioppilastutkinto'])
        expect(OpinnotPage().getTutkinto()).to.equal("Ylioppilastutkinto")
        expect(OpinnotPage().getOppilaitos()).to.equal("")
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot, .suoritus > .properties'))).to.equal(
          'Koulutus Ylioppilastutkinto')
      })

      it('tila ja vahvistus', function() {
        expect(OpinnotPage().tilaJaVahvistus.tila()).to.equal('Suoritus kesken')
      })
    })

    describe('Osasuoritukset', function() {
      before(opinnot.expandAll)

      it('kaikki osasuoritukset näkyvissä', function() {
        expect(extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))).to.equal('Tutkintokerta Koe Arvosana\n' +
          '2012 kevät Fysiikka Lubenter approbatur\n' +
          '2012 kevät Kemia Lubenter approbatur'
        )
      })
    })
  })

  describe('Kypsyyskoe jne', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('120674-064R'))
    before(opinnot.expandAll)

    it('kaikki osasuoritukset näkyvissä', function() {
        expect(extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))).to.equal(
          'Tutkintokerta Koe Arvosana\n' +
          '1996 kevät Reaali, elämänkatsomustiedon kysymykset Improbatur\n' +
          '1996 syksy Englanninkielinen kypsyyskoe Cum laude approbatur\n' +
          '1996 syksy Matematiikan koe, lyhyt oppimäärä Improbatur\n' +
          '1996 syksy Reaali, elämänkatsomustiedon kysymykset Improbatur\n' +
          '1997 kevät Suomi, lyhyt oppimäärä Improbatur\n' +
          '1997 kevät Matematiikan koe, lyhyt oppimäärä Approbatur\n' +
          '1997 kevät Reaali, elämänkatsomustiedon kysymykset Approbatur\n' +
          '1997 syksy Suomi, lyhyt oppimäärä Improbatur\n' +
          '1998 kevät Suomi, lyhyt oppimäärä Improbatur'
        )
    })
  })

  describe('Vanha reaalikoe', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('120872-781Y'))
    before(opinnot.expandAll)

    it('kaikki osasuoritukset näkyvissä', function() {
      expect(extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))).to.equal(
        'Tutkintokerta Koe Arvosana\n1996 kevät Äidinkielen koe, suomi Magna cum laude approbatur\n' +
        '1996 kevät Ruotsi, keskipitkä oppimäärä Approbatur\n' +
        '1996 kevät Englanti, pitkä oppimäärä Cum laude approbatur\n' +
        '1996 kevät Kreikka, lyhyt oppimäärä Improbatur\n' +
        '1996 kevät Matematiikan koe, lyhyt oppimäärä Magna cum laude approbatur\n' +
        '1996 kevät Reaali, ev lut uskonnon kysymykset Lubenter approbatur'
      )
    })
  })

  describe('Kieli puuttuu oppilastiedoista', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('120674-064R'))
    before(opinnot.expandAll)

    it('kaikki osasuoritukset näkyvissä', function() {
      expect(extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))).to.equal(
        'Tutkintokerta Koe Arvosana\n' +
        '1996 kevät Reaali, elämänkatsomustiedon kysymykset Improbatur\n' +
        '1996 syksy Englanninkielinen kypsyyskoe Cum laude approbatur\n' +
        '1996 syksy Matematiikan koe, lyhyt oppimäärä Improbatur\n' +
        '1996 syksy Reaali, elämänkatsomustiedon kysymykset Improbatur\n' +
        '1997 kevät Suomi, lyhyt oppimäärä Improbatur\n' +
        '1997 kevät Matematiikan koe, lyhyt oppimäärä Approbatur\n' +
        '1997 kevät Reaali, elämänkatsomustiedon kysymykset Approbatur\n' +
        '1997 syksy Suomi, lyhyt oppimäärä Improbatur\n' +
        '1998 kevät Suomi, lyhyt oppimäärä Improbatur'
      )
    })
  })
})
