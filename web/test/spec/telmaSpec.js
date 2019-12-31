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
          'Opiskeluoikeuden voimassaoloaika : 14.9.2009 — 4.6.2016\n' +
          'Tila 4.6.2016 Valmistunut\n' +
          '14.9.2009 Läsnä'
        )
      })

      it('näyttää suorituksen tiedot', function() {
        expect(extractAsText(S('.suoritus > .properties, .suoritus > .tila-vahvistus'))).to.equal(
          'Koulutus Työhön ja itsenäiseen elämään valmentava koulutus (TELMA) 999903 6/011/2015\n' +
          'Oppilaitos / toimipiste Stadin ammattiopisto\n' +
          'Suorituskieli suomi\n' +
          'Suoritus valmis Vahvistus : 4.6.2016 Helsinki Reijo Reksi , rehtori'
        )
      })

      it('näyttää koulutuksen osat', function() {
        expect(extractAsText(S('.osasuoritukset'))).to.equalIgnoreNewlines(
          'Sulje kaikki\n' +
          'Koulutuksen osa Laajuus (osp) Arvosana\n' +
          'Toimintakyvyn vahvistaminen 18 Hyväksytty\n' +
          'Kuvaus Toimintakyvyn vahvistaminen\n' +
          'Pakollinen kyllä\n' +
          'Arviointipäivä 20.3.2013\n' +
          'Opiskeluvalmiuksien vahvistaminen 15 Hyväksytty\n' +
          'Kuvaus Opiskeluvalmiuksien vahvistaminen\n' +
          'Pakollinen kyllä\n' +
          'Arviointipäivä 20.3.2013\n' +
          'Työelämään valmentautuminen 20 Hyväksytty\n' +
          'Kuvaus Työelämään valmentautuminen\n' +
          'Pakollinen kyllä\n' +
          'Arviointipäivä 20.3.2013\n' +
          'Tieto- ja viestintätekniikka sekä sen hyödyntäminen 2 Hyväksytty\n' +
          'Kuvaus Tieto- ja viestintätekniikka sekä sen hyödyntäminen\n' +
          'Pakollinen ei\n' +
          'Tunnustettu\n' +
          'Selite Yhteisten tutkinnon osien osa-alue on suoritettu x- perustutkinnon perusteiden (2015) osaamistavoitteiden mukaisesti\n' +
          'Rahoituksen piirissä ei\n' +
          'Arviointipäivä 20.3.2013\n' +
          'Uimaliikunta ja vesiturvallisuus 5 2\n' +
          'Kuvaus Kurssilla harjoitellaan vedessä liikkumista ja perehdytään vesiturvallisuuden perusteisiin. Kurssilla käytäviä asioita: - uinnin hengitystekniikka - perehdytystä uinnin eri tekniikoihin - allasturvallisuuden perustiedot\n' +
          'Pakollinen ei\n' +
          'Tunnustettu\n' +
          'Selite Koulutuksen osa on tunnustettu Vesikallion urheiluopiston osaamistavoitteiden mukaisesti\n' +
          'Rahoituksen piirissä ei\n' +
          'Arviointipäivä 20.3.2013\n' +
          'Auton lisävarustetyöt 15 Hyväksytty\n' +
          'Kuvaus Tuunaus\n' +
          'Pakollinen ei\n' +
          'Arviointipäivä 20.3.2013\n' +
          'Yhteensä 75 osp'
        )
      })
    })

    describe('Tulostettava todistus', function() {
      before(opinnot.avaaTodistus())
      it('näytetään', function() {
        // See more detailed content specification in TelmaSpec.scala
        expect(todistus.vahvistus()).to.equal('Helsinki 4.6.2016 Reijo Reksi rehtori')
      })
    })
  })
})
