describe('Käyttöoikeudet', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var markkanen = 'Markkanen-Fagerström, Eéro Jorma-Petteri (080154-770R)'

  describe('Oppijahaku', function () {
    before(
      Authentication().login('omnia-palvelukäyttäjä'),
      page.openPage,
      page.oppijaHaku.search('eero', [markkanen])
    )
    it('Näytetään vain ne oppijat, joiden opinto-oikeuksiin liittyviin organisaatioihin on käyttöoikeudet', function () {})
  })

  describe('Oppijan lisääminen', function () {
    before(
      Authentication().login('omnia-katselija'),
      page.openPage,
      page.oppijaHaku.search(
        '230872-7258',
        page.oppijaHaku.isNoResultsLabelShown
      )
    )
    it('Ei ole mahdollista ilman kirjoitusoikeuksia', function () {
      expect(page.oppijaHaku.canAddNewOppija()).to.equal(false)
    })
  })

  describe('Navigointi oppijan sivulle', function () {
    before(
      Authentication().login('omnia-palvelukäyttäjä'),
      openPage('/koski/oppija/1.2.246.562.24.00000000002', page.is404)
    )

    it('Estetään jos oppijalla ei opinto-oikeutta, joihin käyttäjällä on katseluoikeudet', function () {})
  })

  describe('Kun tiedot ovat peräisin ulkoisesta järjestelmästä', function () {
    before(
      Authentication().logout,
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('270303-281N')
    )
    it('Muutokset estetty', function () {
      expect(opinnot.anythingEditable()).to.equal(false)
    })
  })

  describe('Kun käyttäjällä ei ole kirjoitusoikeuksia', function () {
    before(
      Authentication().logout,
      Authentication().login('omnia-katselija'),
      page.openPage,
      page.oppijaHaku.searchAndSelect('080154-770R')
    )
    it('Muutokset estetty', function () {
      var suoritus = opinnot.opiskeluoikeusEditor()
      expect(suoritus.isEditable()).to.equal(false)
    })
    it('Uuden opiskeluoikeuden lisääminen estetty', function () {
      expect(opinnot.opiskeluoikeudet.lisääOpiskeluoikeusEnabled()).to.equal(
        false
      )
    })
  })

  describe('Kun käyttäjällä on kirjoitusoikeudet, muttei luottamuksellinen-roolia', function () {
    before(
      Authentication().logout,
      Authentication().login('epäluotettava-tallentaja'),
      page.openPage,
      page.oppijaHaku.searchAndSelect('080154-770R')
    )
    it('Muutokset estetty', function () {
      var suoritus = opinnot.opiskeluoikeusEditor()
      expect(suoritus.isEditable()).to.equal(false)
    })
    it('Uuden opiskeluoikeuden lisääminen estetty', function () {
      expect(opinnot.opiskeluoikeudet.lisääOpiskeluoikeusEnabled()).to.equal(
        false
      )
    })
  })

  describe('Viranomaiskäyttäjän näkymä', function () {
    before(Authentication().login('viranomais'), page.openPage)
    var hetut = [
      '190751-739W',
      '101097-6107',
      '101097-6107',
      '300996-870E',
      '071096-317K',
      '040701-432D',
      '190751-739W',
      '140176-449X',
      '230297-6448',
      '250989-419V',
      '021080-725C',
      '031112-020J',
      '210244-374K',
      '130404-054C',
      '211007-442N',
      '131025-6573',
      '280598-2415',
      '110738-839L',
      '170691-3962',
      '090197-411W',
      '250686-102E',
      '150113-4146',
      '100869-192W',
      '211097-402L',
      '280618-402H',
      '210163-2367',
      '190363-279X',
      '020655-2479',
      '180497-112F',
      '160932-311V',
      '220109-784L',
      '251019-039B',
      '080154-770R',
      '081165-793C',
      '010101-123N'
    ]

    hetut.forEach(function (hetu) {
      describe(hetu, function () {
        before(page.oppijaHaku.searchAndSelect(hetu))
        it('toimii', function () {})
      })
    })
  })

  describe('Linkit henkilöpalveluun, Virta-opintotietoihin ja suoritusrekisteriin oppijan sivulla', function () {
    describe('Käyttäjällä on kirjoitusoikeudet henkilöpalveluun', function () {
      before(
        Authentication().logout,
        Authentication().login('pää'),
        page.openPage,
        page.oppijaHaku.searchAndSelect('080154-770R')
      )
      it('Linkit näytetään', function () {
        expect(extractAsText(S('.oppija-content h2 a'))).to.equal(
          'JSON Oppijanumerorekisteri Virta XML Suoritusrekisteri'
        )
      })
    })
    describe('Käyttäjällä on globaalit lukuoikeudet', function () {
      before(
        Authentication().logout,
        Authentication().login('viranomais'),
        page.openPage,
        page.oppijaHaku.searchAndSelect('080154-770R')
      )
      it('Virta-linkki näytetään', function () {
        expect(extractAsText(S('.oppija-content h2 a'))).to.equal(
          'JSON Virta XML'
        )
      })
    })
    describe('Käyttäjällä ei ole oikeuksia henkilöpalveluun eikä globaaleja lukuoikeuksia', function () {
      before(
        Authentication().logout,
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect('080154-770R')
      )
      it('Linkkejä ei näytetä', function () {
        expect(extractAsText(S('.oppija-content h2 a'))).to.equal('JSON')
      })
    })
  })
})
