describe('VST', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()
  var opiskeluoikeus = OpiskeluoikeusDialog()
  var addOppija = AddOppijaPage()

  describe('Opiskeluoikeuden lisääminen', function () {
    before(
      prepareForNewOppija('kalle', '230872-7258'),
      addOppija.enterValidDataVSTKOPS(),
      addOppija.submitAndExpectSuccess('Tyhjä, Tero (230872-7258)', 'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille')
    )

    it('toimii', function () {
      expect(opinnot.getTutkinto()).to.equal('Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille')
      expect(opinnot.getOppilaitos()).to.equal('Varsinais-Suomen kansanopisto')
      expect(editor.propertyBySelector('.diaarinumero').getValue()).to.equal('OPH-58-2021')
      expect(extractAsText(S('.tunniste-koodiarvo'))).to.equal('999909')
    })
  })
})
