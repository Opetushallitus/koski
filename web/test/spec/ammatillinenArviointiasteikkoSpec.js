describe('Ammatillisten koulutusten arviointiasteikko', function () {
  var verifyArviointiasteikko = function (expectedText) {
    return expect(
      extractAsText(
        S('.ammatillinenpaatasonsuoritus > .ammatillinenarviointiasteikko')
      )
    ).to.equal(expectedText)
  }

  describe('Virkailijan käyttöliittymässä', function () {
    before(Authentication().login())
    var page = KoskiPage()

    describe('Arviointi 1-5, Hylätty tai Hyväksytty', function () {
      before(
        insertExample('ammatillinen - reformin mukainen perustutkinto.json'),
        page.openPage,
        page.oppijaHaku.searchAndSelect('020882-577H')
      )
      it('Näytetään käyttöliittymässä', function () {
        verifyArviointiasteikko(
          'Tutkinnon osien arviointiasteikko :\n' +
            '1-5, Hylätty tai Hyväksytty'
        )
      })
    })

    describe('Arviointi 1-3, Hyväksytty tai Hylätty', function () {
      before(page.openPage, page.oppijaHaku.searchAndSelect('140493-2798'))

      it('Näytetään käyttöliittymässä', function () {
        verifyArviointiasteikko(
          'Tutkinnon osien arviointiasteikko :\n' +
            '1-3, Hylätty tai Hyväksytty'
        )
      })
    })

    describe('Hyväksytty tai hylätty', function () {
      before(page.openPage, page.oppijaHaku.searchAndSelect('130320-899Y'))

      it('Näytetään käyttöliittymässä', function () {
        verifyArviointiasteikko(
          'Tutkinnon osien arviointiasteikko :\n' + 'Hylätty tai Hyväksytty'
        )
      })
    })
  })

  describe('Arviointiasteikko näkyy myös kansalaisen näkymässä', function () {
    var omattiedot = OmatTiedotPage()
    var etusivu = LandingPage()
    var korhopankki = KorhoPankki()
    var authentication = Authentication()
    var opinnot = OpinnotPage()

    before(
      authentication.logout,
      etusivu.openMobilePage,
      etusivu.login(),
      wait.until(korhopankki.isReady),
      korhopankki.login('140493-2798'),
      wait.until(omattiedot.isVisible),
      opinnot.valitseOmatTiedotOpiskeluoikeus('Luonto- ja ympäristöalan')
    )

    it('ok', function () {
      verifyArviointiasteikko(
        'Tutkinnon osien arviointiasteikko :\n' + '1-3, Hylätty tai Hyväksytty'
      )
    })
  })
})
