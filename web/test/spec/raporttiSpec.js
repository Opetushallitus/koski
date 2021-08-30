describe('Raportti', function() {
  var page = RaporttiPage()
  var login = LoginPage()

  describe('Tietoturva', function() {
    before(Authentication().logout, page.openPage())

    it('näytetään login, kun käyttäjä ei ole kirjautunut sisään', function() {
      expect(login.isVisible()).to.equal(true)
    })

    describe('Ei käyttöoikeuksia', function() {
      before(Authentication().login('kalle'), page.openPage())

      it('tulee forbidden', function() {
        expect(KoskiPage().is403()).to.equal(true)
      })
    })

    describe('Pääkäyttäjälle', function() {
      before(Authentication().login('pää'), page.openPage())

      it('näytetään', function() {
        expect(page.isVisible()).to.equal(true)
      })
    })
  })

  describe('Raportin tiedot', function() {
    before(Authentication().login('pää'), page.openPage(page.isVisible))

    it('oppijoiden määrä', function() {
      expect(page.metric('oppijoiden-määrä').value() >= 0).to.equal(true)
    })

    it('opiskeluoikeuksien määrä', function() {
      expect(page.metric('opiskeluoikeuksien-määrä').value() >= 0).to.equal(true)
    })

    it('ammatillisen opiskeluoikeuksien määrä', function () {
      expect(page.metric('opiskeluoikeuksien-määrä-ammatillinen-koulutus').value() >= 0).to.equal(true)
    })

    it('perusopetuksen opiskeluoikeuksien määrä', function () {
      expect(page.metric('opiskeluoikeuksien-määrä-perusopetus').value() >= 0).to.equal(true)
    })

    it('lukion opiskeluoikeuksien määrä', function () {
      expect(page.metric('opiskeluoikeuksien-määrä-lukiokoulutus').value() >= 0).to.equal(true)
    })

    it('ib:n opiskeluoikeuksien määrä', function () {
      expect(page.metric('opiskeluoikeuksien-määrä-ib-tutkinto').value() >= 0).to.equal(true)
    })

    it('esiopetuksen opiskeluoikeuksien määrä', function () {
      expect(page.metric('opiskeluoikeuksien-määrä-esiopetus').value() >= 0).to.equal(true)
    })

    it('luvan opiskeluoikeuksien määrä', function () {
      expect(page.metric('opiskeluoikeuksien-määrä-lukioon-valmistava-koulutus-luva').value() >= 0).to.equal(true)
    })

    it('perusopetukseen valmistavan opiskeluoikeuksien määrä', function () {
      expect(page.metric('opiskeluoikeuksien-määrä-perusopetukseen-valmistava-opetus').value() >= 0).to.equal(true)
    })

    it('perusopetuksen lisäopetuksen opiskeluoikeuksien määrä', function () {
      expect(page.metric('opiskeluoikeuksien-määrä-perusopetuksen-lisäopetus').value() >= 0).to.equal(true)
    })

    it('pääkäyttäjien määrä', function () {
      expect(page.metric('käyttöoikeuksien-määrä-koski-oppilaitos-pääkäyttäjä_1494486198456').value() >= 0).to.equal(true)
    })

    it('vastuukäyttäjien määrä', function () {
      expect(page.metric('käyttöoikeuksien-määrä-Vastuukayttajat').value() >= 0).to.equal(true)
    })

    it('tiedonsiirtovirheiden määrä', function () {
      expect(page.metric('tiedonsiirtovirheiden-määrä').value() >= 0).to.equal(true)
    })

    it('käyttökatkojen määrä', function () {
      expect(page.metric('käyttökatkojen-määrä').value() >= 0).to.equal(true)
    })

    it('hälytysten määrä', function () {
      expect(page.metric('hälytysten-määrä').value() >= 0).to.equal(true)
    })

    it('virheiden määrä', function () {
      expect(page.metric('virheiden-määrä').value() >= 0).to.equal(true)
    })
  })
})
