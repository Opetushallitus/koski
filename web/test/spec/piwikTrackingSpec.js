describe('Piwik-seuranta', function() {
  var piwik = FakePiwik()

  describe('Koski-palvelun login-sivu', function() {
    var loginPage = LoginPage()

    before(loginPage.openPage)

    it('Sivu luo yhden Piwik-kutsun', function() {
      expect(piwik.getQueuedMethodCalls().length).to.equal(1)
    })

    it('Sivu raportoi lataamisen', function() {
      expectPiwikTrackLoadPage(piwik.getQueuedMethodCalls()[0], '/koski/login')
    })

    describe('Kirjautumisen jälkeen', function() {
      var koskiPage = KoskiPage()

      before(
        Authentication().login(),
        resetFixtures,
        koskiPage.openPage,
        wait.until(koskiPage.isReady))

      it('Sivu luo yhden Piwik-kutsun', function() {
        expect(piwik.getQueuedMethodCalls().length).to.equal(1)
      })

      it('Sivu raportoi lataamisen', function() {
        expectPiwikTrackLoadPage(piwik.getQueuedMethodCalls()[0], '/koski/')
      })

      describe('Siirtyäessä oppijan tietoihin', function() {
        var oppijaPathRegexp = /^\/koski\/oppija\/\d.\d.\d{3}.\d{3}.\d{2}.\d+(\?.*)?$/

        before(
          piwik.reset,
          koskiPage.oppijataulukko.clickFirstOppija,
          koskiPage.waitUntilAnyOppijaSelected())

        it('Sivu raportoi lataamisen', function() {
          expectPiwikTrackAjaxPage(piwik.getQueuedMethodCalls(), oppijaPathRegexp)
        })

        describe('Klikatessa paluulinkkiä', function() {
          var opinnotPage = OpinnotPage()

          before(piwik.reset, opinnotPage.backToList)

          it('Sivu raportoi lataamisen', function() {
            expectPiwikTrackAjaxPage(piwik.getQueuedMethodCalls(), '/koski/')
          })
        })

        describe('Klikatessa selaimen back-nappia', function() {
          before(
            piwik.reset,
            goBack,
            koskiPage.waitUntilAnyOppijaSelected())

          it('Sivu raportoi lataamisen', function() {
            expectPiwikTrackAjaxPage(piwik.getQueuedMethodCalls(), oppijaPathRegexp)
          })
        })

        describe('Klikatessa selaimen forward-nappia', function() {
          before(
            piwik.reset,
            goForward,
            wait.until(koskiPage.isReady))

          it('Sivu raportoi lataamisen', function() {
            expectPiwikTrackAjaxPage(piwik.getQueuedMethodCalls(), '/koski/')
          })
        })
      })

      describe('Palvelimen palauttaessa 404-sivun', function() {
        before(openPage('/koski/nosuch', koskiPage.is404))

        it('Sivu luo kaksi Piwik-kutsua', function() {
          expect(piwik.getQueuedMethodCalls()).to.have.lengthOf(2)
        })

        it('Sivu raportoi lataamisen', function() {
          expect(piwik.getQueuedMethodCalls()[0]).to.deep.equal(['trackPageView', '/koski/nosuch (404)'])
        })

        it('Sivu raportoi LoadError-tapahtuman', function() {
          expectPiwikTrackError(piwik.getQueuedMethodCalls()[1], 'LoadError', {
            location: /\/koski\/nosuch$/,
            httpStatus: 404,
            text: 'Not found'
          })
        })
      })

      describe('Palvelimen vastatessa Ajax-kyselyyn virheellä', function() {
        before(
          koskiPage.openPage,
          piwik.reset,
          koskiPage.oppijaHaku.search('#error#', koskiPage.isErrorShown))

        it('Sivu luo yhden Piwik-kutsun', function() {
          expect(piwik.getQueuedMethodCalls()).to.have.lengthOf(1)
        })

        it('Sivu raportoi RuntimeError-tapahtuman', function() {
          expectPiwikTrackError(piwik.getQueuedMethodCalls()[0], 'RuntimeError', {
            location: /\/koski\/$/,
            url: /\/koski\/api\/henkilo\/search\?query=%23error%23$/,
            httpStatus: 500,
            message: 'http error 500'
          })
        })
      })
    })

    describe('Palvelimen palauttaessa 404-sivun muualla kuin Koski-sovelluksessa', function() {
      before(openPage('/koski/pulssi/nosuch'))

      it('Sivu luo kaksi Piwik-kutsua', function() {
        expect(piwik.getQueuedMethodCalls().length).to.equal(2)
      })

      it('Sivu raportoi lataamisen', function() {
        expectPiwikTrackLoadPage(piwik.getQueuedMethodCalls()[0], '/koski/pulssi/nosuch (404)')
      })

      it('Sivu raportoi LoadError-tapahtuman', function() {
        expectPiwikTrackError(piwik.getQueuedMethodCalls()[1], 'LoadError', {
          location: /\/koski\/pulssi\/nosuch$/,
          httpStatus: 404,
          text: 'Not found'
        })
      })
    })
  })

  function expectPiwikTrackLoadPage(piwikQueuedMethodCall, title) {
    expect(piwikQueuedMethodCall).to.deep.equal(['trackPageView', title])
  }

  function expectPiwikTrackAjaxPage(piwikQueuedMethodCalls, urlPath) {
    expect(piwikQueuedMethodCalls).to.have.lengthOf(2)

    var customUrlCall = piwikQueuedMethodCalls[0]

    expect(customUrlCall).to.have.lengthOf(2)
    expect(customUrlCall[0]).to.equal('setCustomUrl')
    expect(customUrlCall[1]).to[urlPath instanceof RegExp ? 'match' : 'equal'](urlPath)

    var trackPageViewCall = piwikQueuedMethodCalls[1]

    expect(trackPageViewCall).to.have.lengthOf(2)
    expect(trackPageViewCall[0]).to.equal('trackPageView')
    expect(trackPageViewCall[1]).to[urlPath instanceof RegExp ? 'match' : 'equal'](urlPath)
  }

  function expectPiwikTrackError(piwikQueuedMethodCall, errorName, errorData) {
    expect(piwikQueuedMethodCall).to.have.lengthOf(3)
    expect(piwikQueuedMethodCall[0]).to.equal('trackEvent')
    expect(piwikQueuedMethodCall[1]).to.equal(errorName)

    var msgKey = errorData.text ? 'text' : 'message'
    var json = JSON.parse(piwikQueuedMethodCall[2])

    expect(json).to.have.keys(Object.keys(errorData))
    expect(json.location).to.match(errorData.location)
    if (errorData.url) {
      expect(json.url).to.match(errorData.url)
    }
    expect(json.httpStatus).to.equal(errorData.httpStatus)
    expect(json[msgKey]).to.equal(errorData[msgKey])
  }
})
