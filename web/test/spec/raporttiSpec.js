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

    it('siirtäneiden oppilaitosten määrä', function () {
      expect(page.metric('siirtäneiden-oppilaitosten-määrä').value() >= 0).to.equal(true)
    })

    it('oppilaitoksien määrä', function () {
      expect(page.metric('oppilaitoksien-määrä').value() >= 0).to.equal(true)
    })

    it('siirtoprosentti', function () {
      expect(page.metric('siirtoprosentti').value() >= 0).to.equal(true)
    })

    it('siirtäneitä oppilaitoksia, ammatillinen', function () {
      expect(page.metric('siirtäneiden-oppilaitosten-määrä-ammatillinen-koulutus').value() >= 0).to.equal(true)
    })

    it('siirtäneitä oppilaitoksia, perusopetus', function () {
      expect(page.metric('siirtäneiden-oppilaitosten-määrä-perusopetus').value() >= 0).to.equal(true)
    })

    it('siirtäneitä oppilaitoksia, lukio', function () {
      expect(page.metric('siirtäneiden-oppilaitosten-määrä-lukiokoulutus').value() >= 0).to.equal(true)
    })

    it('käyttöoikeuksien määrä', function () {
      expect(page.metric('käyttöoikeuksien-määrä').value() >= 0).to.equal(true)
    })

    it('palvelukäyttäjäoikeuksien määrä', function () {
      expect(page.metric('käyttöoikeusien-määrä-koski-oppilaitos-palvelukäyttäjä').value() >= 0).to.equal(true)
    })

    it('tallentajaoikeuksien määrä', function () {
      expect(page.metric('käyttöoikeusien-määrä-koski-oppilaitos-tallentaja').value() >= 0).to.equal(true)
    })

    it('pääkäyttäjien määrä', function () {
      expect(page.metric('käyttöoikeusien-määrä-koski-oph-pääkäyttäjä').value() >= 0).to.equal(true)
    })

    it('viranomaiskatselijaoikeuksien määrä', function () {
      expect(page.metric('käyttöoikeusien-määrä-koski-viranomainen-katselija').value() >= 0).to.equal(true)
    })

    it('oppilaitoskatselijaoikeuksien määrä', function () {
      expect(page.metric('käyttöoikeusien-määrä-koski-oppilaitos-katselija').value() >= 0).to.equal(true)
    })

    it('vastuukäyttäjien määrä', function () {
      expect(page.metric('käyttöoikeusien-määrä-Vastuukayttajat').value() >= 0).to.equal(true)
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