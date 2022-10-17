describe('Tiedonsiirrot', function () {
  var tiedonsiirrot = TiedonsiirrotPage()
  var authentication = Authentication()

  before(
    authentication.login('stadin-palvelu'),
    resetFixtures,
    insertOppija('<oppija></oppija>'),
    insertOppija('{"henkilö": {}}'),
    insertExample('tiedonsiirto - epäonnistunut.json'),
    insertExample('tiedonsiirto - onnistunut.json'),
    insertExample('tiedonsiirto - epäonnistunut 2.json'),
    insertExample('tiedonsiirto - vain syntymäaika.json'),
    insertExample('tiedonsiirto - epäonnistunut 3.json'),
    syncTiedonsiirrot,
    tiedonsiirrot.openPage
  )

  describe('Tiedonsiirtoloki', function () {
    function sortByName(a, b) {
      return a[1].localeCompare(b[1])
    }

    it('Näytetään', function () {
      expect(tiedonsiirrot.tiedot().sort(sortByName)).to.deep.equal(
        [
          [
            'epävalidiHetu',
            'Tiedonsiirto, Tiina',
            'Stadin ammatti- ja aikuisopisto',
            'Luonto- ja ympäristöalan perustutkintoAutokorinkorjauksen osaamisalaAutokorinkorjaaja',
            'virhe',
            'tiedot'
          ],
          [
            '280618-402H',
            'Ammattilainen, Aarne',
            'Aalto-yliopisto',
            '',
            'virhe',
            'tiedot'
          ],
          [
            '24.2.1977',
            'Hetuton, Heikki',
            'Stadin ammatti- ja aikuisopisto',
            'Autoalan perustutkinto',
            '',
            ''
          ],
          [
            '270303-281N',
            'Tiedonsiirto, Tiina',
            'Stadin ammatti- ja aikuisopisto',
            'Autoalan perustutkinto',
            '',
            ''
          ],
          ['', '', '', '', 'virhe', 'tiedot'],
          ['', '', '', '', 'virhe', 'tiedot']
        ].sort(sortByName)
      )
    })
  })

  describe('Yhteenveto', function () {
    before(tiedonsiirrot.openYhteenveto)

    it('Näytetään', function () {
      expect(
        tiedonsiirrot.tiedot().map(function (row) {
          return row[0]
        })
      ).to.deep.equal([
        'Aalto-yliopisto',
        'Helsingin kaupunki',
        'Stadin ammatti- ja aikuisopisto'
      ])
    })
  })

  describe('Virhelistaus', function () {
    before(tiedonsiirrot.openVirhesivu)

    it('Näytetään', function () {
      expect(tiedonsiirrot.tiedot()).to.deep.equal([
        [
          'epävalidiHetu',
          'Tiedonsiirto, Tiina',
          'Stadin ammatti- ja aikuisopisto',
          'Luonto- ja ympäristöalan perustutkintoAutokorinkorjauksen osaamisalaAutokorinkorjaaja',
          'Virheellinen muoto hetulla: epävalidiHetuvirhe',
          'tiedot'
        ],
        [
          '280618-402H',
          'Ammattilainen, Aarne',
          'Aalto-yliopisto',
          '',
          'Ei oikeuksia organisatioon 1.2.246.562.10.56753942459virhe',
          'tiedot'
        ],
        [
          '',
          '',
          '',
          '',
          'Viesti ei ole skeeman mukainen (notAnyOf henkilö)virhe',
          'tiedot'
        ],
        ['', '', '', '', 'Epäkelpo JSON-dokumenttivirhe', 'tiedot']
      ])
    })

    describe('Poistettaessa', function () {
      before(
        Authentication().login('pää'),
        tiedonsiirrot.openPage,
        tiedonsiirrot.openVirhesivu
      )

      describe('Aluksi', function () {
        it('Poista valitut nappi on disabloitu', function () {
          expect(tiedonsiirrot.poistaNappiEnabloitu()).to.equal(false)
        })
      })
      describe('Kun valitaan rivi', function () {
        before(tiedonsiirrot.setValintaViimeiseen(true))

        it('Poista valitut nappi enabloituu', function () {
          expect(tiedonsiirrot.poistaNappiEnabloitu()).to.equal(true)
        })

        describe('Kun valitaan toinen rivi', function () {
          before(
            tiedonsiirrot.setValinta(
              'tiedonsiirto-1.2.246.562.10.346830761110_280618-402H',
              true
            )
          )

          it('Poista valitut nappi on edelleen enabloitu', function () {
            expect(tiedonsiirrot.poistaNappiEnabloitu()).to.equal(true)
          })

          describe('Kun poistetaan toinen rivi', function () {
            before(
              tiedonsiirrot.setValinta(
                'tiedonsiirto-1.2.246.562.10.346830761110_280618-402H',
                false
              )
            )

            it('Poista valitut nappi on edelleen enabloitu', function () {
              expect(tiedonsiirrot.poistaNappiEnabloitu()).to.equal(true)
            })

            describe('Kun poistetaan viimeinen rivi', function () {
              before(tiedonsiirrot.setValintaViimeiseen(false))

              it('Poista valitut nappi on disabloitu', function () {
                expect(tiedonsiirrot.poistaNappiEnabloitu()).to.equal(false)
              })
            })
          })
        })
      })
      describe('Kun poistetaan valittu rivi', function () {
        before(
          tiedonsiirrot.setValintaViimeiseen(true),
          // tiedonsiirrot.poista does page reload, so wait.forAjax is not enough
          wait.prepareForNavigation,
          tiedonsiirrot.poista,
          wait.forNavigation,
          wait.until(tiedonsiirrot.isVisible)
        )
        it('Se poistuu listauksesta', function () {
          expect(tiedonsiirrot.tiedot()).to.deep.equal([
            [
              'epävalidiHetu',
              'Tiedonsiirto, Tiina',
              'Stadin ammatti- ja aikuisopisto',
              'Luonto- ja ympäristöalan perustutkintoAutokorinkorjauksen osaamisalaAutokorinkorjaaja',
              'Virheellinen muoto hetulla: epävalidiHetuvirhe',
              'tiedot'
            ],
            [
              '280618-402H',
              'Ammattilainen, Aarne',
              'Aalto-yliopisto',
              '',
              'Ei oikeuksia organisatioon 1.2.246.562.10.56753942459virhe',
              'tiedot'
            ],
            [
              '',
              '',
              '',
              '',
              'Viesti ei ole skeeman mukainen (notAnyOf henkilö)virhe',
              'tiedot'
            ]
          ])
        })
        it('Poista valitut nappi on disabloitu', function () {
          expect(tiedonsiirrot.poistaNappiEnabloitu()).to.equal(false)
        })
        describe('Kun valitaan rivi', function () {
          before(
            tiedonsiirrot.setValinta(
              'tiedonsiirto-1.2.246.562.10.346830761110_280618-402H',
              false
            )
          )
          it('Poista valitut nappi enabloituu', function () {
            expect(tiedonsiirrot.poistaNappiEnabloitu()).to.equal(false)
          })
        })
      })
    })

    describe('Poistettaessa useampi kerralla', function () {
      before(
        resetFixtures,
        authentication.login('stadin-palvelu'),
        tiedonsiirrot.openPage,
        tiedonsiirrot.openVirhesivu,
        insertExample('tiedonsiirto - epäonnistunut.json'),
        insertExample('tiedonsiirto - epäonnistunut 2.json'),
        syncTiedonsiirrot,
        authentication.login('pää'),
        tiedonsiirrot.openPage,
        tiedonsiirrot.openVirhesivu,
        tiedonsiirrot.setValinta(
          'tiedonsiirto-1.2.246.562.10.346830761110_280618-402H',
          true
        ),
        tiedonsiirrot.setValinta(
          'tiedonsiirto-1.2.246.562.10.346830761110_270303-281N',
          true
        ),
        // tiedonsiirrot.poista does page reload, so wait.forAjax is not enough
        wait.prepareForNavigation,
        tiedonsiirrot.poista,
        wait.forNavigation,
        wait.until(tiedonsiirrot.isVisible)
      )
      it('Kaikki valitut rivit poistuvat', function () {
        expect(tiedonsiirrot.tiedot()).to.deep.equal([])
      })
    })

    describe('Ilman tiedonsiirron mitätöintioikeutta', function () {
      before(
        authentication.login('stadin-palvelu'),
        tiedonsiirrot.openPage,
        tiedonsiirrot.openVirhesivu
      )
      it('Poista valitut nappi on piilotettu', function () {
        expect(tiedonsiirrot.poistaNappiNäkyvissä()).to.equal(false)
      })
      it('Tiedonsiirto-rivien valinta on piilotettu', function () {
        expect(tiedonsiirrot.rivinValintaNäkyvissä()).to.equal(false)
      })
    })
  })

  function insertOppija(dataString) {
    return function () {
      return sendAjax(
        '/koski/api/oppija',
        'application/json',
        dataString,
        'PUT'
      ).catch(function () {})
    }
  }
})
