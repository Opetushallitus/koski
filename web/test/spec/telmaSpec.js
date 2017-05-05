describe('Telma', function() {
  var page = KoskiPage()
  var todistus = TodistusPage()
  var opinnot = OpinnotPage()
  before(Authentication().login(), resetFixtures)

  describe('Työhön ja itsenäiseen elämään valmentava koulutus', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('021080-725C'))
    describe('Kaikki tiedot näkyvissä', function() {
      before(opinnot.expandAll)
      it('näyttää opiskeluoikeuden tiedot', function() {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Alkamispäivä : 14.9.2009 — Päättymispäivä : 4.6.2016\n' +
          'Tila 4.6.2016 Valmistunut\n' +
          '14.9.2009 Läsnä'
        )
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)\n' +
          'Oppilaitos / toimipiste Stadin ammattiopisto\n' +
          'Suoritus: VALMIS Vahvistus : 4.6.2016 Helsinki Reijo Reksi , rehtori'
        )
      })

      it('näyttää koulutuksen osat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equal(
          'Koulutuksen osa\nSulje kaikki Pakollisuus Laajuus (osp) Arvosana\n' +
          'Toimintakyvyn vahvistaminen kyllä 18 Hyväksytty\n' +
          'Sanallinen arviointi Opiskelija selviytyy arkielämään liittyvistä toimista, osaa hyödyntää apuvälineitä, palveluita ja tukea sekä on valinnut itselleen sopivan tavan viettää vapaa-aikaa.\n' +
          'Opiskeluvalmiuksien vahvistaminen kyllä 15 Hyväksytty\n' +
          'Sanallinen arviointi Opiskelija osaa opiskella työskennellä itsenäisesti, mutta ryhmässä toimimisessa tarvitsee joskus apua. Hän viestii vuorovaikutustilanteissa hyvin, osaa käyttää tietotekniikkaa ja matematiikan perustaitoja arkielämässä.\n' +
          'Työelämään valmentautuminen kyllä 20 Hyväksytty\n' +
          'Sanallinen arviointi Opiskelijalla on käsitys itsestä työntekijänä, mutta työyhteisön säännöt vaativat vielä harjaantumista.\n' +
          'Tieto- ja viestintätekniikka sekä sen hyödyntäminen ei 2 Hyväksytty\n' +
          'Tunnustettu\n' +
          'Yhteisten tutkinnon osien osa-alue on suoritettu x- perustutkinnon perusteiden (2015) osaamistavoitteiden mukaisesti\n' +
          'Uimaliikunta ja vesiturvallisuus ei 5 2\n' +
          'Tunnustettu\n' +
          'Koulutuksen osa on tunnustettu Vesikallion urheiluopiston osaamistavoitteiden mukaisesti\n' +
          'Auton lisävarustetyöt ei 15 Hyväksytty'
        )
      })
    })

    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus(0))
      it('näytetään', function() {
        // See more detailed content specification in TelmaSpec.scala
        expect(todistus.vahvistus()).to.equal('Helsinki 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })
})