describe('Piwik', function() {
  var piwik = FakePiwik()

  describe('Koski-palvelun login-sivu', function() {
    var loginPage = LoginPage()

    before(loginPage.openPage)

    it('Sivu raportoi lataamisen', function() {
      expect(piwik.getQueuedMethodCalls()).to.deep.equal([['trackPageView', '/koski/login']])
    })

    describe('Kirjautumisen jälkeen', function() {
      var koskiPage = KoskiPage()

      before(Authentication().login(), koskiPage.openPage)

      it('Sivu raportoi lataamisen', function() {
        expect(piwik.getQueuedMethodCalls()).to.deep.equal([['trackPageView', '/koski/']])
      })

      describe('Palvelimen palauttaessa 404-sivun', function() {
        before(openPage('/koski/nosuch', koskiPage.is404))

        it('Sivu luo kaksi Piwik-kutsua', function() {
          expect(piwik.getQueuedMethodCalls().length).to.equal(2)
        })

        it('Sivu raportoi lataamisen', function() {
          expect(piwik.getQueuedMethodCalls()[0]).to.deep.equal(['trackPageView', '/koski/nosuch (404)'])
        })

        it('Sivu raportoi LoadError-tapahtuman', function() {
          expect(piwik.getQueuedMethodCalls()[1]).to.deep.equal([
            'trackEvent',
            'LoadError',
            JSON.stringify({
              url: '' + testFrame().document.location,
              httpStatus: 404,
              text: 'Not found'
            })
          ])
        })
      })
    })

    describe('Palvelimen palauttaessa 404-sivun muualla kuin Koski-sovelluksessa', function() {
      before(openPage('/koski/pulssi/nosuch'))

      it('Sivu luo kaksi Piwik-kutsua', function() {
        expect(piwik.getQueuedMethodCalls().length).to.equal(2)
      })

      it('Sivu raportoi lataamisen', function() {
        expect(piwik.getQueuedMethodCalls()[0]).to.deep.equal(['trackPageView', '/koski/pulssi/nosuch (404)'])
      })

      it('Sivu raportoi LoadError-tapahtuman', function() {
        expect(piwik.getQueuedMethodCalls()[1]).to.deep.equal([
          'trackEvent',
          'LoadError',
          JSON.stringify({
            url: testFrame().document.origin + '/koski/pulssi/nosuch',
            httpStatus: 404,
            text: 'Not found'
          })
        ])
      })
    })
  })
})
