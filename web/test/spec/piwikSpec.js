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

      before(Authentication().login(), resetFixtures, koskiPage.openPage)

      it('Sivu raportoi lataamisen', function() {
        expect(piwik.getQueuedMethodCalls()).to.deep.equal([['trackPageView', '/koski/']])
      })

      describe('Siirtyäessä oppijan tietoihin', function() {
        var oppijaPathRegexp = /^\/koski\/oppija\/\d.\d.\d{3}.\d{3}.\d{2}.\d+$/

        before(
          piwik.reset,
          koskiPage.oppijataulukko.clickFirstOppija,
          koskiPage.waitUntilAnyOppijaSelected())

        it('Sivu luo kaksi Piwik-kutsua', function() {
          expect(piwik.getQueuedMethodCalls().length).to.equal(2)
        })

        it('Sivu asettaa custom urlin raportoitavaksi', function() {
          expect(piwik.getQueuedMethodCalls()[0][0]).to.equal('setCustomUrl')
          expect(piwik.getQueuedMethodCalls()[0][1]).to.match(oppijaPathRegexp)
        })

        it('Sivu raportoi lataamisen', function() {
          expect(piwik.getQueuedMethodCalls()[1][0]).to.equal('trackPageView')
          expect(piwik.getQueuedMethodCalls()[1][1]).to.match(oppijaPathRegexp)
        })

        describe('Klikatessa paluulinkkiä', function() {
          var opinnotPage = OpinnotPage()

          before(piwik.reset, opinnotPage.backToList)

          it('Sivu luo kaksi Piwik-kutsua', function() {
            expect(piwik.getQueuedMethodCalls().length).to.equal(2)
          })

          it('Sivu asettaa custom urlin raportoitavaksi', function() {
            expect(piwik.getQueuedMethodCalls()[0]).to.deep.equal(['setCustomUrl', '/koski/'])
          })

          it('Sivu raportoi lataamisen', function() {
            expect(piwik.getQueuedMethodCalls()[1]).to.deep.equal(['trackPageView', '/koski/'])
          })
        })
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
              url: testFrame().document.origin + '/koski/nosuch',
              httpStatus: 404,
              text: 'Not found'
            })
          ])
        })
      })

      describe('Palvelimen vastatessa Ajax-kyselyyn virheellä', function() {
        before(
          koskiPage.openPage,
          piwik.reset,
          koskiPage.oppijaHaku.search('#error#', koskiPage.isErrorShown))

        it('Sivu raportoi RuntimeError-tapahtuman', function() {
          expect(piwik.getQueuedMethodCalls()).to.deep.equal([[
            'trackEvent',
            'RuntimeError',
            JSON.stringify({
              message: 'http error 400',
              httpStatus: 400,
              url: '' + testFrame().document.location
            })
          ]])
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
