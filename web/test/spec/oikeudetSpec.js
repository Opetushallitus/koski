describe('Käyttöoikeudet', function() {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var markkanen = 'Markkanen-Fagerström, Eéro Jorma-Petteri (080154-770R)'

  describe('Oppijahaku', function() {
    before(Authentication().login('omnia-palvelukäyttäjä'), page.openPage, page.oppijaHaku.search('eero', [markkanen]))
    it('Näytetään vain ne oppijat, joiden opinto-oikeuksiin liittyviin organisaatioihin on käyttöoikeudet', function() {

    })
  })

  describe('Oppijan lisääminen', function() {
    before(Authentication().login('omnia-katselija'), page.openPage, page.oppijaHaku.search('230872-7258', page.oppijaHaku.isNoResultsLabelShown))
    it('Ei ole mahdollista ilman kirjoitusoikeuksia', function() {
      expect(page.oppijaHaku.canAddNewOppija()).to.equal(false)
    })
  })

  describe('Navigointi oppijan sivulle', function() {
    before(Authentication().login('omnia-palvelukäyttäjä'), openPage('/koski/oppija/1.2.246.562.24.00000000002', page.is404))

    it('Estetään jos oppijalla ei opinto-oikeutta, joihin käyttäjällä on katseluoikeudet', function() {

    })
  })

  /*
  //TODO: meillä ei ole tällaista keissiä fikstuureissa
  describe('Kun tiedot ovat peräisin ulkoisesta järjestelmästä', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('270303-281N'))
    it('Muutokset estetty', function() {
      expect(opinnot.anythingEditable()).to.equal(false)
    })
  })
  */

  describe('Kun käyttäjällä ei ole kirjoitusoikeuksia', function() {
    before(Authentication().logout, Authentication().login('omnia-katselija'), page.openPage, page.oppijaHaku.searchAndSelect('080154-770R'))
    it('Muutokset estetty', function() {
      var suoritus = opinnot.opiskeluoikeusEditor()
      expect(suoritus.isEditable()).to.equal(false)
    })
    it('Uuden opiskeluoikeuden lisääminen estetty', function() {
      expect(opinnot.opiskeluoikeudet.lisääOpiskeluoikeusEnabled()).to.equal(false)
    })
  })

  describe('Kun käyttäjällä on kirjoitusoikeudet, muttei luottamuksellinen roolia', function() {
    before(Authentication().logout, Authentication().login('epäluotettava-tallentaja'), page.openPage, page.oppijaHaku.searchAndSelect('080154-770R'))
    it('Muutokset estetty', function() {
      var suoritus = opinnot.opiskeluoikeusEditor()
      expect(suoritus.isEditable()).to.equal(false)
    })
    it('Uuden opiskeluoikeuden lisääminen estetty', function() {
      expect(opinnot.opiskeluoikeudet.lisääOpiskeluoikeusEnabled()).to.equal(false)
    })
  })
})