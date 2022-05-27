describe('Omat tiedot - TUVA', function () {
  var omattiedot = OmatTiedotPage()
  var opinnot = OpinnotPage()
  var authentication = Authentication()
  var etusivu = LandingPage()
  var korhopankki = KorhoPankki()
  before(authentication.login(), resetFixtures)

  describe('TUVAn suorittanut', function () {
    before(authentication.logout, etusivu.openPage)
    before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('270504A317P'), wait.until(omattiedot.isVisible))

    it('Opiskeluoikeus näytetään listassa', function () {
      expect(opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikot()).to.contain('Tutkintokoulutukseen valmentava koulutus (2021—2021, valmistunut)')
    })

    it('Näytetään opiskeluoikeudet', function () {
      expect(omattiedot.nimi()).to.equal('Turo Vaara')
      expect(omattiedot.oppija()).to.equal('Opintoni')
      expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal([
        'Stadin ammatti- ja aikuisopisto'])
    })

    describe('Kun opiskeluoikeus avataan', function () {
      before(
        opinnot.valitseOmatTiedotOpiskeluoikeus('Tutkintokoulutukseen valmentava koulutus (2021—2021, valmistunut)')
      )

      it('näytetään suorituksen yleiset tiedot', function () {
        expect(extractAsText(findSingle('.tab.selected'))).to.equal('Tutkintokoulutukseen valmentava koulutus')
        expect(opinnot.opiskeluoikeudet.järjestämislupa()).to.equal('Ammatillisen koulutuksen järjestämislupa (TUVA)')

        opinnot.avaaLisätiedot()
        expect(extractAsText(findSingle('.maksuttomuus .value'))).to.equal('1.8.2021 — Maksuton')

        expect(extractAsText(findSingle('.koulutusmoduuli .tunniste'))).to.equal('Tutkintokoulutukseen valmentava koulutus')
        expect(extractAsText(findSingle('.diaarinumero .value'))).to.equal('OPH-1488-2021')
        expect(extractAsText(findSingle('.tila-vahvistus.valmis'))).to.equal('Suoritus valmis Vahvistus : 31.12.2021 Helsinki Reijo Reksi , rehtori')
      })
      it('listataan osasuoritukset ja niiden tiedot', function () {
        opinnot.avaaKaikki()
        expect(opinnot.tutkinnonOsat().laajuudetYhteensä()).to.equal('12')
        expect(extractAsText(S('.tuva-osasuoritus-1'))).to.equal(
          // Opiskelu- ja urasuunnittelutaidot
          'Opiskelu- ja urasuunnittelutaidot 2 vk Hyväksytty\n' +
          'Suorituskieli suomi\n' +
          'Arviointipäivä 1.9.2021\n\n' +
          // Perustaitojen vahvistaminen
          'Perustaitojen vahvistaminen 1 vk Hyväksytty\n' +
          'Suorituskieli suomi\n' +
          'Arviointipäivä 1.9.2021\n\n' +
          // Ammatillisen koulutuksen opinnot ja niihin valmentautuminen
          'Ammatillisen koulutuksen opinnot ja niihin valmentautuminen 1 vk Hyväksytty\n' +
          'Suorituskieli suomi\n' +
          'Arviointipäivä 1.10.2021\n\n' +
          // Työelämätaidot ja työpaikalla tapahtuva oppiminen
          'Työelämätaidot ja työpaikalla tapahtuva oppiminen 1 vk Hyväksytty\n' +
          'Suorituskieli suomi\n' +
          'Arviointipäivä 1.10.2021\n\n' +
          // Arjen ja yhteiskunnallisen osallisuuden taidot
          'Arjen ja yhteiskunnallisen osallisuuden taidot 1 vk Hyväksytty\n' +
          'Suorituskieli suomi\n' +
          'Arviointipäivä 1.11.2021\n\n' +
          // Lukiokoulutuksen opinnot ja niihin valmentautuminen
          'Lukiokoulutuksen opinnot ja niihin valmentautuminen 1 vk Hyväksytty\n' +
          'Suorituskieli suomi\n' +
          'Tunnustettu\n' +
          'Nimi Englannin kieli ja maailmani\n' +
          'Laajuus 1 kurssia\n' +
          'Kurssin tyyppi Pakollinen\n' +
          'Arvosana 8\n' +
          'Arviointipäivä 4.6.2016\n' +
          'Selite Tunnustettu lukion kurssi\n' +
          'Rahoituksen piirissä ei\n' +
          'Arviointipäivä 1.11.2021\n\n' +
          // Valinnaiset koulutuksen osat
          'Valinnaiset koulutuksen osat 5 vk Hyväksytty\n' +
          'Suorituskieli suomi\n' +
          'Arviointipäivä 1.12.2021\n' +
          'Valinnaisen koulutuksen osan paikallinen osasuoritus Laajuus Arvosana\n' +
          'Valinnainen kurssi 1 2 vk Hyväksytty\n' +
          'Valinnainen kurssi 2 3 vk Hyväksytty'
        )
      })
    })
  })
})
