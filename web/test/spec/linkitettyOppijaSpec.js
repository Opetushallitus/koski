describe('Linkitetyt oppijat', function() {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  before(Authentication().login(), resetFixtures, page.openPage)
  describe('Kun katsotaan master-henkilön tietoja', function() {
    before(page.oppijaHaku.searchAndSelect('Master'))
    describe('Tietojen katsominen', function() {
      it('Näytetään myös slave-henkilön opiskeluoikeudet', function() {
        expect(opinnot.opiskeluoikeudet.opiskeluoikeustyypit()).to.have.members([
          'Lukiokoulutus', 'Perusopetus'
        ])
      })
    })
    describe('Slaveen liitettyjen tietojen muuttaminen', function() {
      before(
        opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi('lukiokoulutus'),
        editor.edit,
        editor.property('oppimäärä').setValue('Lukio suoritetaan aikuisten opetussuunnitelman mukaan'),
        editor.saveChanges
      )

      it('Tallennus onnistuu', function() {

      })
    })

    describe('Versiohistorian katsominen', function() {
      var versiohistoria = opinnot.versiohistoria

      before(versiohistoria.avaa, versiohistoria.valitse('v1'))

      it('Toimii', function() {})
    })
  })

  describe('Slave-henkilöön liitetyt opiskeluoikeudet oppijataulukossa', function() {
    before(
      page.openPage,
      page.oppijataulukko.filterBy('nimi', 'Master')
    )

    it('Näytetään master-henkilön tiedot', function() {
      expect(page.oppijataulukko.names()).to.deep.equal([ 'of Puppets, Master', 'of Puppets, Master' ])
    })
  })

  describe('Oppijahaussa', function() {
    before(
      refreshIndices, // ensure that elasticsearch index is refreshed to reflect the changes made above
      page.oppijaHaku.search('Puppets')
    )

    it('Näytetään vain master-henkilön tiedot', function() {
      expect(page.oppijaHaku.getSearchResults()).to.deep.equal(['of Puppets, Master (101097-6107)'])
    })
  })

})
