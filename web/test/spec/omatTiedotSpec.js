describe('Omat tiedot', function () {
  var page = KoskiPage()
  var omattiedot = OmatTiedotPage()
  var opinnot = OpinnotPage()
  var authentication = Authentication()
  var login = LoginPage()
  before(authentication.login(), resetFixtures)

  describe('Virkailijana', function () {
    before(
      authentication.login('Oili'),
      openPage('/koski/omattiedot'),
      wait.until(login.isVisible)
    )
    it('siirrytään login-sivulle', function () {})
  })

  describe('Kansalaisena', function () {
    var etusivu = LandingPage()
    var korhopankki = KorhoPankki()
    before(authentication.logout, etusivu.openPage)

    describe('Kun kirjaudutaan sisään', function () {
      before(
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login(
          '251029-7230',
          'Kansalainen',
          'VÃ¤inÃ¶ TÃµnis',
          'VÃ¤inÃ¶'
        ),
        wait.until(omattiedot.isVisible)
      )
      describe('Sivun sisältö', function () {
        it('Näytetään opiskeluoikeudet', function () {
          expect(omattiedot.nimi()).to.equal('Väinö Tõnis Kansalainen')
          expect(omattiedot.oppija()).to.equal('Opintoni')
          expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal([
            'Itä-Suomen yliopisto'
          ])
        })

        it('Näytetään opintoni-ingressi', function () {
          expect(omattiedot.ingressi()).to.equal(
            'Tällä sivulla näkyvät kaikki sähköisesti tallennetut opintosuoritukset yksittäisistä kursseista kokonaisiin tutkintoihin.'
          )
        })

        it('Näytetään nimi ja syntymäaika', function () {
          expect(omattiedot.headerNimi()).to.equal(
            'Väinö Tõnis Kansalainen\n' + 's. 25.10.1929'
          )
        })

        it('Näytetään virheraportointi-painike', function () {
          expect(!!omattiedot.virheraportointiButton().length).to.equal(true)
        })

        it('Sivun latauksessa ei tapahdu virheitä', function () {
          expect(page.getErrorMessage()).to.equal('')
        })

        describe('Ruotsinkielinen sisältö', function () {
          before(
            click(findSingle('#logout')),
            wait.until(etusivu.isVisible),
            etusivu.login(),
            wait.until(korhopankki.isReady),
            korhopankki.login(
              '251029-7230',
              'Kansalainen',
              'VÃ¤inÃ¶ TÃµnis',
              'VÃ¤inÃ¶',
              'sv'
            ),
            wait.until(omattiedot.isVisible)
          )

          it('Näytetään ruotsinkielinen ingressi', function () {
            expect(omattiedot.ingressi()).to.equal(
              'På denna sida syns alla studieprestationer som sparats elektroniskt, från enskilda kurser till hela examina.'
            )
          })
        })

        describe('Englanninkielinen sisältö', function () {
          before(
            click(findSingle('#logout')),
            wait.until(etusivu.isVisible),
            etusivu.login(),
            wait.until(korhopankki.isReady),
            korhopankki.login(
              '251029-7230',
              'Kansalainen',
              'VÃ¤inÃ¶ TÃµnis',
              'VÃ¤inÃ¶',
              'en'
            ),
            wait.until(omattiedot.isVisible)
          )

          it('Näytetään englanninkielinen ingressi', function () {
            expect(omattiedot.ingressi()).to.equal(
              'This page shows all electronically stored study info, from individual courses to whole examinations.'
            )
          })
        })
      })

      describe('Kun kirjaudutaan ulos', function () {
        before(click(findSingle('#logout')), wait.until(etusivu.isVisible))
        it('Näytetään länderi', function () {})
      })

      describe('Kun henkilöllä on syntymäaika-tieto', function () {
        before(authentication.logout, etusivu.openPage)
        before(
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('220627-833V'),
          wait.until(omattiedot.isVisible)
        )

        it('Näytetään nimi ja syntymäaika', function () {
          expect(omattiedot.headerNimi()).to.equal(
            'Sylvi Syntynyt\n' + 's. 1.1.1970'
          )
        })
      })

      describe('Ylioppilastutkinnon koesuoritukset', function () {
        before(
          authentication.logout,
          etusivu.openPage,
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('080698-967F'),
          wait.until(omattiedot.isVisible),
          opinnot.valitseOmatTiedotOpiskeluoikeus('Ylioppilastutkinto')
        )

        it('näytetään jos ne löytyy', function () {
          expect(
            extractAsText(
              S('.ylioppilastutkinnonsuoritus .osasuoritukset .suoritus-group')
            )
          ).to.equal(
            'Tutkintokerta Koe Pisteet Arvosana\n' +
              '2012 kevät Äidinkielen koe, suomi 46 Lubenter approbatur Näytä koesuoritus\n' +
              '2012 kevät Ruotsi, keskipitkä oppimäärä 166 Cum laude approbatur Näytä koesuoritus\n' +
              '2012 kevät Englanti, pitkä oppimäärä 210 Cum laude approbatur Näytä koesuoritus\n' +
              '2012 kevät Maantiede 26 Magna cum laude approbatur Näytä koesuoritus\n' +
              '2012 kevät Matematiikan koe, lyhyt oppimäärä 59 Laudatur Näytä koesuoritus'
          )
          expect(findFirst('.koesuoritus a')().attr('href')).to.equal(
            '/koski/koesuoritus/2345K_XX_12345.pdf'
          )
        })
      })

      describe('Virheistä raportointi', function () {
        before(authentication.logout, etusivu.openPage)
        before(
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('180497-112F'),
          wait.until(omattiedot.isVisible)
        )

        it('Aluksi ei näytetä lomaketta', function () {
          expect(omattiedot.virheraportointiForm.isVisible()).to.equal(false)
        })

        describe('Kun painetaan painiketta', function () {
          before(click(omattiedot.virheraportointiButton))

          var form = omattiedot.virheraportointiForm

          it('näytetään lista tiedoista, joita palvelussa ei pystytä näyttämään', function () {
            expect(form.contentsAsText()).to.equal(
              'Huomioithan, että Oma Opintopolku-palvelussa ei pystytä näyttämään seuraavia tietoja:\n' +
                'Korkeakoulututkintoja ennen vuotta 1995. Tässä voi olla korkeakoulukohtaisia poikkeuksia.\n' +
                'Ennen vuotta 1990 suoritettuja suomalaisia ylioppilastutkintoja.\n' +
                'Ennen vuotta 2018 suoritettuja peruskoulun, ammattikoulun, suomalaisen lukion tai Suomessa suoritetun kansainvälisen lukion tutkintoja, suorituksia ja opiskeluoikeuksia.\n' +
                'Asiani koskee tietoa, joka näkyy, tai kuuluisi yllämainitun perusteella näkyä Oma Opintopolku-palvelussa.'
            )
          })

          describe('Kun hyväksytään huomio palvelusta löytyvistä tiedoista', function () {
            before(form.acceptDisclaimer)

            it('näytetään oppilaitosvaihtoehdot', function () {
              expect(form.oppilaitosNames()).to.deep.equal([
                'Kulosaaren ala-aste',
                'Jyväskylän normaalikoulu',
                'Muu'
              ])
            })

            it('oppilaitoksilla on oikeat OIDit', function () {
              expect(form.oppilaitosOids()).to.deep.equal([
                '1.2.246.562.10.64353470871',
                '1.2.246.562.10.14613773812',
                'other'
              ])
            })

            it('ei vielä näytetä yhteystietoja', function () {
              expect(form.oppilaitosOptionsText()).to.equal(
                'Voit tiedustella asiaa oppilaitokseltasi.\n' +
                  'Kulosaaren ala-aste Jyväskylän normaalikoulu Muu'
              )
            })

            describe('Kun valitaan oppilaitos, jolle löytyy sähköpostiosoite', function () {
              before(form.selectOppilaitos('1.2.246.562.10.14613773812'))

              it('näytetään sähköpostiosoite ja oppilaitoksen nimi', function () {
                expect(form.yhteystiedot()).to.equal(
                  'joku.osoite@example.com\n' + 'Jyväskylän normaalikoulu'
                )
              })

              it('näytetään sähköposti-painike', function () {
                expect(!!form.sähköpostiButton().length).to.equal(true)
              })

              it('näytetään yhteystiedot kopioitavana tekstinä', function () {
                expect(form.yhteystiedotTekstinä()).to.equal(
                  'Muista mainita sähköpostissa seuraavat tiedot:\n' +
                    'Nimi: Miia Monikoululainen\n' +
                    'Oppijanumero: 1.2.246.562.24.00000000012' +
                    ' ' +
                    'Kopioi'
                )
              })

              it('mailto-linkissä on oikea viestipohja', function () {
                expect(form.sähköpostiButtonMailtoContents()).to.equal(
                  'mailto:joku.osoite@example.com?' +
                    'subject=Tiedustelu%20opintopolun%20tiedoista&' +
                    'body=' +
                    encodeURIComponent(
                      '***Kirjoita viestisi tähän***\n\n' +
                        '———————————————————————————————\n\n' +
                        'Allaoleva teksti on luotu automaattisesti Opintopolun tiedoista. Koulu tarvitsee näitä tietoja pystyäkseen käsittelemään kysymystäsi.\n\n' +
                        'Nimi: Miia Monikoululainen\n' +
                        'Oppijanumero: 1.2.246.562.24.00000000012'
                    )
                )
              })
            })

            describe('Kun valitaan oppilaitos, jolle ei löydy sähköpostiosoitetta', function () {
              before(form.selectOppilaitos('1.2.246.562.10.64353470871'))

              it('näytetään ainoastaan virheviesti', function () {
                expect(form.oppilaitosOptionsText()).to.equal(
                  'Voit tiedustella asiaa oppilaitokseltasi.\n' +
                    'Kulosaaren ala-aste Jyväskylän normaalikoulu Muu\n' +
                    'Oppilaitokselle ei löytynyt yhteystietoja.'
                )
              })
            })

            describe("Kun valitaan 'muu'", function () {
              before(form.selectOppilaitos('other'))

              it('näytetään oppilaitos-picker', function () {
                expect(isElementVisible(form.oppilaitosPicker)).to.equal(true)
              })

              describe('Kun valitaan pickerillä oppilaitos', function () {
                before(form.selectMuuOppilaitos('Ressun lukio'))

                it('näytetään sähköpostiosoite ja oppilaitoksen nimi', function () {
                  expect(form.yhteystiedot()).to.equal(
                    'joku.osoite@example.com\n' + 'Ressun lukio'
                  )
                })

                it('näytetään sähköposti-painike', function () {
                  expect(!!form.sähköpostiButton().length).to.equal(true)
                })

                it('näytetään yhteystiedot kopioitavana tekstinä', function () {
                  expect(form.yhteystiedotTekstinä()).to.equal(
                    'Muista mainita sähköpostissa seuraavat tiedot:\n' +
                      'Nimi: Miia Monikoululainen\n' +
                      'Oppijanumero: 1.2.246.562.24.00000000012' +
                      ' ' +
                      'Kopioi'
                  )
                })
              })
            })
          })
        })

        describe('Ylioppilastutkinnoille', function () {
          var form = omattiedot.virheraportointiForm

          describe('kun ei lukiosuorituksia', function () {
            before(authentication.logout, etusivu.openPage)
            before(
              etusivu.login(),
              wait.until(korhopankki.isReady),
              korhopankki.login('210244-374K'),
              wait.until(omattiedot.isVisible)
            )
            before(
              click(omattiedot.virheraportointiButton),
              form.acceptDisclaimer
            )

            it('näytetään oppilaitoksissa ylioppilastutkintolautakunta, ei lukiota, lisäksi suorituksen tyyppi (ylioppilastutkinto)', function () {
              expect(form.oppilaitosNames()).to.deep.equal([
                'Ylioppilastutkintolautakunta (ylioppilastutkinto)',
                'Muu'
              ])
            })

            it('ylioppilastutkintolautakunnalla on oikea OID', function () {
              expect(form.oppilaitosOids()).to.deep.equal([
                '1.2.246.562.10.43628088406',
                'other'
              ])
            })
          })

          describe('kun myös lukiosuorituksia', function () {
            before(authentication.logout, etusivu.openPage)
            before(
              etusivu.login(),
              wait.until(korhopankki.isReady),
              korhopankki.login('080698-967F'),
              wait.until(omattiedot.isVisible)
            )
            before(
              click(omattiedot.virheraportointiButton),
              form.acceptDisclaimer
            )

            it('näytetään oppilaitoksissa ylioppilastutkintolautakunta ja lukio', function () {
              expect(form.oppilaitosNames()).to.deep.equal([
                'Ylioppilastutkintolautakunta (ylioppilastutkinto)',
                'Jyväskylän normaalikoulu',
                'Muu'
              ])
            })

            it('oppilaitoksilla on oikeat OIDit', function () {
              expect(form.oppilaitosOids()).to.deep.equal([
                '1.2.246.562.10.43628088406',
                '1.2.246.562.10.14613773812',
                'other'
              ])
            })
          })
        })
      })

      describe('Perusopetus', function () {
        before(
          authentication.logout,
          etusivu.openPage,
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('131298-5248'),
          wait.until(omattiedot.isVisible)
        )
        describe('Monta oppiaineen oppimäärää samassa opiskeluoikeudessa', function () {
          it('Näytetään suorituksen tyyppi', function () {
            expect(
              opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikotJaOpiskeluoikeusOid()
            ).to.match(
              /Perusopetuksen oppiaineen oppimäärä \(2008—2018, valmistunut\)Opiskeluoikeuden oid:.*/
            )
          })
        })
      })

      describe('Suoritusjako', function () {
        before(authentication.logout, etusivu.openPage)
        before(
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('180497-112F'),
          wait.until(omattiedot.isVisible)
        )

        var form = omattiedot.suoritusjakoForm
        window.secrets = {}

        describe('Jakaminen', function () {
          it('Aluksi ei näytetä lomaketta', function () {
            expect(form.isVisible()).to.equal(false)
          })

          describe('Kun painetaan painiketta', function () {
            before(click(omattiedot.suoritusjakoButton))

            it('näytetään ingressi', function () {
              expect(form.ingressi()).to.equal(
                'Luomalla jakolinkin voit näyttää suoritustietosi haluamillesi henkilöille (esimerkiksi työtä tai opiskelupaikkaa hakiessasi). ' +
                  'Luotuasi linkin voit tarkistaa tarkan sisällön Esikatsele-painikkeella.'
              )
            })

            it('näytetään suoritusvaihtoehtojen otsikko', function () {
              expect(form.suoritusvaihtoehdotOtsikkoText()).to.equal(
                'Valitse jaettavat suoritustiedot'
              )
            })

            it('näytetään suoritusvaihtoehdot', function () {
              expect(form.suoritusvaihtoehdotText()).to.equal(
                'Suoritetut tutkinnot\n' +
                  'Kulosaaren ala-aste\n' +
                  '7. vuosiluokka\n' +
                  '6. vuosiluokka\n' +
                  'Jyväskylän normaalikoulu\n' +
                  '9. vuosiluokka\n' +
                  '8. vuosiluokka'
              )
            })

            it('jakopainike on disabloitu', function () {
              expect(form.canCreateSuoritusjako()).to.equal(false)
            })
          })

          describe('Kun valitaan suoritus', function () {
            before(
              form.selectSuoritus(
                null,
                '1.2.246.562.10.14613773812',
                'perusopetuksenvuosiluokka',
                '8'
              )
            )

            it('jakopainike on enabloitu', function () {
              expect(form.canCreateSuoritusjako()).to.equal(true)
            })

            describe('Kun painetaan suoritusjaon luomispainiketta', function () {
              before(
                form.createSuoritusjako(),
                wait.until(form.suoritusjako(1).isVisible)
              )

              it('suoritusjako näytetään', function () {
                var jako = form.suoritusjako(1)
                var secret = jako.url().split('/') // otetaan salaisuus talteen jaon hakemista varten
                window.secrets.perusopetus = secret[secret.length - 1]

                expect(jako.isVisible()).to.equal(true)
              })

              it('suoritusjaon tiedot näytetään', function () {
                var jako = form.suoritusjako(1)

                var date = new Date()
                var targetMonth = date.getMonth() + 6
                date.setMonth(targetMonth)
                if (date.getMonth() != targetMonth % 12) {
                  // match java.time.LocalDate.plusMonths behavior, in case e.g. today is May 31st, and
                  // November 31st doesn't exist
                  date.setDate(0)
                }

                expect(jako.url()).to.match(/^.+\/opinnot\/[0-9a-f]{32}$/)
                expect(jako.voimassaoloaika()).to.equal(
                  '' +
                    date.getDate() +
                    '.' +
                    (date.getMonth() + 1) +
                    '.' +
                    date.getFullYear()
                )
                expect(jako.esikatseluLinkHref()).to.equal(jako.url())
              })
            })
          })

          describe('Voimassaoloajan muuttaminen', function () {
            const formattedDate = function (dayDiff) {
              const date = new Date()
              date.setDate(date.getDate() + dayDiff)
              return (
                '' +
                date.getDate() +
                '.' +
                (date.getMonth() + 1) +
                '.' +
                date.getFullYear()
              )
            }

            const validDate = formattedDate(1)
            const tooBigDate = formattedDate(1337)
            const invalidDate = formattedDate(-10)

            describe('kun voimassaoloaika on validi', function () {
              before(
                form.suoritusjako(1).setVoimassaoloaika(validDate),
                wait.until(form.suoritusjako(1).feedbackText.isVisible)
              )
              it('päivittäminen onnistuu', function () {
                expect(form.suoritusjako(1).feedbackText.value()).to.equal(
                  'Muutokset tallennettu'
                )
              })
            })

            describe('kun voimassaoloaika on liian suuri', function () {
              before(
                form.suoritusjako(1).setVoimassaoloaika(tooBigDate),
                wait.until(form.suoritusjako(1).feedbackText.isVisible)
              )
              it('näytetään virheilmoitus', function () {
                expect(form.suoritusjako(1).feedbackText.value()).to.equal(
                  'Pisin voimassaoloaika on vuosi'
                )
              })
            })

            describe('kun voimassaoloaika on menneisyydessä', function () {
              before(
                form.suoritusjako(1).setVoimassaoloaika(invalidDate),
                wait.until(form.suoritusjako(1).feedbackText.isVisible)
              )
              it('näytetään virheilmoitus', function () {
                expect(form.suoritusjako(1).feedbackText.value()).to.equal(
                  'Virheellinen päivämäärä'
                )
              })
            })
          })
        })

        describe('Katselu', function () {
          var suoritusjako = SuoritusjakoPage()

          before(
            authentication.logout,
            suoritusjako.openPage('perusopetus'),
            wait.until(suoritusjako.isVisible)
          )

          it('linkki toimii', function () {
            expect(suoritusjako.isVisible()).to.equal(true)
          })

          describe('Sivun sisältö', function () {
            it('Näytetään otsikko, nimi ja syntymäaika', function () {
              expect(suoritusjako.headerText()).to.equal(
                'Opinnot' +
                  'Miia Monikoululainen' +
                  's. 18.4.1997' +
                  'Tiedot koneluettavassa muodossa'
              )
            })

            it('Näytetään jaetut opiskeluoikeudet oppilaitoksittain', function () {
              expect(suoritusjako.oppilaitosTitleText()).to.deep.equal([
                'Jyväskylän normaalikoulu'
              ])
              expect(suoritusjako.opiskeluoikeusTitleText()).to.match(
                /Perusopetus \(2008—, läsnä\)Opiskeluoikeuden oid:.*/
              )
            })

            it('Ei näytetä virheraportointi-painiketta', function () {
              expect(!!omattiedot.virheraportointiButton().length).to.equal(
                false
              )
            })

            it('Ei näytetä suoritusjako-painiketta', function () {
              expect(!!omattiedot.suoritusjakoButton().length).to.equal(false)
            })

            describe('Kun avataan oppilaitos', function () {
              before(suoritusjako.avaaOpiskeluoikeus('(2008—, läsnä)'))

              it('näytetään oikeat opiskeluoikeudet', function () {
                expect(
                  opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienMäärä()
                ).to.equal(1)
                expect(
                  opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikot()
                ).to.deep.equal(['Perusopetus (2008—, läsnä)'])
              })
            })
          })

          describe('Kielen vaihto ruotsiin', function () {
            before(
              click(suoritusjako.changeLanguageButtonSwedish),
              wait.forMilliseconds(150), // page reloads
              wait.until(function () {
                return isElementVisible(suoritusjako.header())
              })
            )

            it('toimii', function () {
              expect(suoritusjako.headerText()).to.equal(
                'Studier' +
                  'Miia Monikoululainen' +
                  'f. 18.4.1997' +
                  'Tiedot koneluettavassa muodossa'
              )
            })

            after(click(suoritusjako.changeLanguageButtonFinnish))
          })

          describe('Kielen vaihto englantiin', function () {
            before(
              click(suoritusjako.changeLanguageButtonEnglish),
              wait.forMilliseconds(150), // page reloads
              wait.until(function () {
                return isElementVisible(suoritusjako.header())
              })
            )

            it('toimii', function () {
              expect(suoritusjako.headerText()).to.equal(
                'Studies' +
                  'Miia Monikoululainen' +
                  'b. 18.4.1997' +
                  'Tiedot koneluettavassa muodossa'
              )
            })

            after(click(suoritusjako.changeLanguageButtonFinnish))
          })
        })

        describe('Korkeakoulusuoritukset', function () {
          before(
            authentication.logout,
            etusivu.openPage,
            etusivu.login(),
            wait.until(korhopankki.isReady),
            korhopankki.login('100869-192W', 'Dippainssi', 'Dilbert'),
            wait.until(omattiedot.isVisible),
            click(omattiedot.suoritusjakoButton)
          )

          describe('Tutkintosuorituksen jakaminen', function () {
            before(
              form.selectSuoritus(
                '1114082125',
                '1.2.246.562.10.56753942459',
                'korkeakoulututkinto',
                '751101'
              ),
              form.createSuoritusjako(),
              wait.until(form.suoritusjako(1).isVisible)
            )

            it('onnistuu', function () {
              var jako = form.suoritusjako(1)
              var secret = jako.url().split('/') // otetaan salaisuus talteen jaon hakemista varten
              window.secrets.korkeakoulututkinto = secret[secret.length - 1]

              expect(jako.isVisible()).to.equal(true)
            })
          })

          describe('Irralliset opintojaksot', function () {
            describe('voidaan jakaa', function () {
              before(
                form.openAdditionalSuoritusjakoForm(),
                form.selectSuoritus(
                  null,
                  '1.2.246.562.10.56753942459',
                  'korkeakoulunopintojakso',
                  null
                ),
                form.createSuoritusjako(),
                wait.until(form.suoritusjako(2).isVisible)
              )

              it('yhtenä kokonaisuutena', function () {
                var jako = form.suoritusjako(2)
                var secret = jako.url().split('/') // otetaan salaisuus talteen jaon hakemista varten
                window.secrets.korkeakoulunopintojaksot =
                  secret[secret.length - 1]

                expect(jako.isVisible()).to.equal(true)
              })
            })

            describe('ei voida', function () {
              before(form.openAdditionalSuoritusjakoForm())

              it('valita jaettaviksi yksittäin', function () {
                expect(form.suoritusvaihtoehdotText()).to.equal(
                  'Suoritetut tutkinnot\n' +
                    'Aalto-yliopisto\n' +
                    'Dipl.ins., konetekniikka ( 2013 — 2016 , päättynyt )\n' +
                    '8 opintojaksoa'
                )
              })
            })
          })

          describe('Muiden korkeakoulusuoritusten jakaminen', function () {
            before(
              authentication.logout,
              etusivu.openPage,
              etusivu.login(),
              wait.until(korhopankki.isReady),
              korhopankki.login(
                '060458-331R',
                'Korkeakoululainen',
                'Kompleksi'
              ),
              wait.until(omattiedot.isVisible),
              click(omattiedot.suoritusjakoButton)
            )

            describe('Opiskelijaliikkuvuus', function () {
              before(
                form.selectSuoritus(
                  '10065_1700969',
                  '1.2.246.562.10.56753942459',
                  'muukorkeakoulunsuoritus',
                  '8'
                ),
                form.createSuoritusjako(),
                wait.until(form.suoritusjako(1).isVisible)
              )

              it('jakaminen onnistuu', function () {
                var jako = form.suoritusjako(1)

                expect(jako.isVisible()).to.equal(true)
              })
            })

            describe('Erikoistumisopinnot', function () {
              before(
                form.openAdditionalSuoritusjakoForm(),
                form.selectSuoritus(
                  '1927',
                  '1.2.246.562.10.56753942459',
                  'muukorkeakoulunsuoritus',
                  '12'
                ),
                form.createSuoritusjako(),
                wait.until(form.suoritusjako(2).isVisible)
              )

              it('jakaminen onnistuu', function () {
                var jako = form.suoritusjako(2)

                expect(jako.isVisible()).to.equal(true)
              })
            })

            describe('Täydennyskoulutus', function () {
              before(
                form.openAdditionalSuoritusjakoForm(),
                form.selectSuoritus(
                  '46737839',
                  '1.2.246.562.10.91392558028',
                  'muukorkeakoulunsuoritus',
                  '10'
                ),
                form.createSuoritusjako(),
                wait.until(form.suoritusjako(3).isVisible)
              )

              it('jakaminen onnistuu', function () {
                var jako = form.suoritusjako(3)

                expect(jako.isVisible()).to.equal(true)
              })
            })

            describe('Opintojaksot', function () {
              before(
                form.openAdditionalSuoritusjakoForm(),
                form.selectSuoritus(
                  '1927',
                  '1.2.246.562.10.56753942459',
                  'korkeakoulunopintojakso',
                  ''
                ),
                form.createAndStoreSuoritusjako('opintojaksot')
              )

              it('jakaminen onnistuu', function () {
                expect(form.suoritusjako(4).isVisible()).to.equal(true)
              })

              describe('Katselu', function () {
                var suoritusjako = SuoritusjakoPage()
                before(
                  suoritusjako.openPage('opintojaksot'),
                  wait.until(suoritusjako.isVisible)
                )

                it('onnistuu', function () {
                  expect(suoritusjako.isVisible()).to.equal(true)
                  expect(suoritusjako.opiskeluoikeusTitleText()).to.deep.equal([
                    '12 opintojaksoa (2011—2013, päättynyt)'
                  ])
                })

                describe('Kun avataan oppilaitos', function () {
                  before(
                    suoritusjako.avaaOpiskeluoikeus(
                      '12 opintojaksoa (2011—2013, päättynyt)'
                    )
                  )

                  it('näytetään oikeat opiskeluoikeudet', function () {
                    expect(
                      extractAsText(S('.opiskeluoikeus-content'))
                    ).to.equal(
                      'Opiskeluoikeuden voimassaoloaika : 24.8.2011 — 20.6.2013\n' +
                        'Tila 21.6.2013 päättynyt\n' +
                        '24.8.2011 aktiivinen\n' +
                        'Lisätiedot\n' +
                        'Opintojakso Laajuus Arvosana\n' +
                        '+\nLaajaverkot 3 op 5\n' +
                        '+\nIP-verkkojen hallinta 4 op 1\n' +
                        '+\nTietoverkkojen tietoturva 5 op 5\n' +
                        '+\nTekninen tietoturva 3 op 4\n' +
                        '+\nWeb-ohjelmointi 5 op 3\n' +
                        '+\nLinuxin asennus ja ylläpito 5 op hyväksytty\n' +
                        '+\nTietoturvallisuuden perusteet 3 op 2\n' +
                        '+\nPhotoshopin perusteet 3 op hyväksytty\n' +
                        '+\nVirtuaalilähiverkot (CCNA3) 3 op 5\n' +
                        '+\nReititinverkot (CCNA2) 3 op 4\n' +
                        '+\nLähiverkot (CCNA1) 3 op 4\n' +
                        '+\nMikrotietokoneen hallinta 3 op hyväksytty\n' +
                        'Yhteensä 43 op'
                    )
                  })
                })
              })
            })
          })

          describe('Katselu', function () {
            before(authentication.logout)

            var suoritusjako = SuoritusjakoPage()

            describe('Tutkintosuorituksen jako', function () {
              before(
                suoritusjako.openPage('korkeakoulututkinto'),
                wait.until(suoritusjako.isVisible)
              )

              it('linkki toimii', function () {
                expect(suoritusjako.isVisible()).to.equal(true)
              })

              describe('Sivun sisältö', function () {
                it('Näytetään oikea otsikko, nimi ja syntymäaika', function () {
                  expect(suoritusjako.headerText()).to.equal(
                    'Opinnot' +
                      'Dilbert Dippainssi' +
                      's. 10.8.1969' +
                      'Tiedot koneluettavassa muodossa'
                  )
                })

                it('Näytetään jaetut opiskeluoikeudet oppilaitoksittain', function () {
                  expect(suoritusjako.oppilaitosTitleText()).to.deep.equal([
                    'Aalto-yliopisto'
                  ])
                  expect(suoritusjako.opiskeluoikeusTitleText()).to.deep.equal([
                    'Dipl.ins., konetekniikka (2013—2016, päättynyt)'
                  ])
                })

                describe('Kun avataan oppilaitos', function () {
                  before(
                    suoritusjako.avaaOpiskeluoikeus(
                      'Dipl.ins., konetekniikka (2013—2016, päättynyt)'
                    )
                  )

                  it('näytetään oikeat opiskeluoikeudet', function () {
                    expect(
                      opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienMäärä()
                    ).to.equal(1)
                    expect(
                      opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikot()
                    ).to.deep.equal([
                      'Dipl.ins., konetekniikka (2013—2016, päättynyt)'
                    ])
                  })
                })
              })
            })

            describe('Opintojaksojen jako', function () {
              before(
                suoritusjako.openPage('korkeakoulunopintojaksot'),
                wait.until(suoritusjako.isVisible)
              )

              it('linkki toimii', function () {
                expect(suoritusjako.isVisible()).to.equal(true)
              })

              describe('Sivun sisältö', function () {
                it('Näytetään oikea otsikko, nimi ja syntymäaika', function () {
                  expect(suoritusjako.headerText()).to.equal(
                    'Opinnot' +
                      'Dilbert Dippainssi' +
                      's. 10.8.1969' +
                      'Tiedot koneluettavassa muodossa'
                  )
                })

                it('Näytetään jaetut opiskeluoikeudet oppilaitoksittain', function () {
                  expect(suoritusjako.oppilaitosTitleText()).to.deep.equal([
                    'Aalto-yliopisto'
                  ])
                  expect(suoritusjako.opiskeluoikeusTitleText()).to.deep.equal([
                    '8 opintojaksoa'
                  ])
                })

                describe('Kun avataan oppilaitos', function () {
                  before(suoritusjako.avaaOpiskeluoikeus('8 opintojaksoa'))

                  it('näytetään oikeat opiskeluoikeudet', function () {
                    expect(
                      opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienMäärä()
                    ).to.equal(1)
                    expect(
                      opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikot()
                    ).to.deep.equal(['8 opintojaksoa'])
                  })
                })
              })
            })
          })
        })

        describe('Peruskoulun vuosiluokan tuplaus', function () {
          before(
            authentication.logout,
            etusivu.openPage,
            etusivu.login(),
            wait.until(korhopankki.isReady),
            korhopankki.login('170186-6520', 'Luokallejäänyt', 'Lasse'),
            wait.until(omattiedot.isVisible)
          )

          describe('Suoritusvaihtoehdoissa', function () {
            before(click(omattiedot.suoritusjakoButton))

            it('näytetään tuplattu luokka vain kerran', function () {
              expect(form.suoritusvaihtoehdotText()).to.equal(
                'Suoritetut tutkinnot\n' +
                  'Jyväskylän normaalikoulu\n' +
                  'Päättötodistus\n' +
                  '9. vuosiluokka\n' +
                  '8. vuosiluokka\n' +
                  '7. vuosiluokka'
              )
            })
          })

          describe('Jakaminen', function () {
            before(
              form.selectSuoritus(
                null,
                '1.2.246.562.10.14613773812',
                'perusopetuksenvuosiluokka',
                '7'
              ),
              form.createSuoritusjako(),
              wait.until(form.suoritusjako(1).isVisible)
            )

            it('onnistuu', function () {
              var jako = form.suoritusjako(1)
              var secret = jako.url().split('/') // otetaan salaisuus talteen jaon hakemista varten
              window.secrets.tuplattu = secret[secret.length - 1]

              expect(jako.isVisible()).to.equal(true)
            })
          })

          describe('Katselu', function () {
            var suoritusjako = SuoritusjakoPage()

            before(
              authentication.logout,
              suoritusjako.openPage('tuplattu'),
              wait.until(suoritusjako.isVisible)
            )

            it('linkki toimii', function () {
              expect(suoritusjako.isVisible()).to.equal(true)
            })

            describe('Sivun sisältö', function () {
              it('Näytetään oikea otsikko, nimi ja syntymäaika', function () {
                expect(suoritusjako.headerText()).to.equal(
                  'Opinnot' +
                    'Lasse Luokallejäänyt' +
                    's. 17.1.1986' +
                    'Tiedot koneluettavassa muodossa'
                )
              })

              it('Näytetään jaetut opiskeluoikeudet oppilaitoksittain', function () {
                expect(suoritusjako.oppilaitosTitleText()).to.deep.equal([
                  'Jyväskylän normaalikoulu'
                ])
                expect(suoritusjako.opiskeluoikeusTitleText()).to.match(
                  /Perusopetus \(2008—2016, valmistunut\)Opiskeluoikeuden oid:.*/
                )
              })

              describe('Kun avataan oppilaitos', function () {
                before(
                  suoritusjako.avaaOpiskeluoikeus('(2008—2016, valmistunut)')
                )

                it('näytetään oikeat opiskeluoikeudet', function () {
                  expect(
                    opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienMäärä()
                  ).to.equal(1)
                  expect(
                    opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikot()
                  ).to.deep.equal(['Perusopetus (2008—2016, valmistunut)'])
                })

                it('näytetään oikea suoritus (ei luokallejäänti-suoritusta)', function () {
                  expect(
                    opinnot.suoritusTabs('(2008—2016, valmistunut)', true)
                  ).to.deep.equal(['7. vuosiluokka'])
                  expect(
                    opinnot
                      .opiskeluoikeusEditor(undefined, true)
                      .property('luokka')
                      .getValue()
                  ).to.equal('7A')
                })
              })
            })
          })
        })

        describe('Suoritetut tutkinnot', function () {
          before(
            authentication.logout,
            etusivu.openPage,
            etusivu.login(),
            wait.until(korhopankki.isReady),
            korhopankki.login('280618-402H', 'Ammattilainen', 'Aarne'),
            wait.until(omattiedot.isVisible),
            click(omattiedot.suoritusjakoButton)
          )

          describe('suoritettujen tutkintojen jakaminen', function () {
            before(
              form.selectSuoritetutTutkinnot(),
              form.createSuoritusjako(),
              wait.until(form.suoritusjako(1).isVisible)
            )

            it('onnistuu', function () {
              var jako = form.suoritusjako(1)
              var secret = jako.url().split('/') // otetaan salaisuus talteen jaon hakemista varten
              window.secrets.suoritetutTutkinnot = secret[secret.length - 1]
              expect(jako.isVisible()).to.equal(true)
            })
          })

          describe('Katselu', function () {
            var suoritusjako = SuoritusjakoPage()

            before(
              authentication.logout,
              suoritusjako.openPage(
                'suoritetutTutkinnot',
                'suoritetut-tutkinnot'
              ),
              wait.until(suoritusjako.isVisible)
            )

            it('onnistuu', function () {
              expect(suoritusjako.isVisible()).to.equal(true)
            })
          })
        })
      })

      describe('Ylioppilastutkinto suoritusjako', function () {
        var suoritusjako = SuoritusjakoPage()
        var form = omattiedot.suoritusjakoForm
        before(
          authentication.logout,
          etusivu.openPage,
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('080698-967F'),
          wait.until(omattiedot.isVisible),
          click(omattiedot.suoritusjakoButton),
          form.selectSuoritus(
            '',
            '1.2.246.562.10.14613773812',
            'ylioppilastutkinto',
            '301000'
          ),
          form.createAndStoreSuoritusjako('ylioppilastutkinto'),
          suoritusjako.openPage('ylioppilastutkinto'),
          wait.until(suoritusjako.isVisible),
          suoritusjako.avaaOpiskeluoikeus('Ylioppilastutkinto')
        )

        it('näytetään opiskeluoikeuden tiedot ilman koesuorituslinkkejä', function () {
          expect(extractAsText(S('.opiskeluoikeus-content'))).to.equal(
            'Ylioppilastutkinto\n' +
              'Koulutus Ylioppilastutkinto\n' +
              'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
              'Pakolliset kokeet suoritettu kyllä\n' +
              'Koulusivistyskieli suomi\n' +
              'Suoritus valmis Vahvistus : 2.6.2012 Helsinki\n' +
              'Tutkintokerta Koe Pisteet Arvosana\n' +
              '2012 kevät Äidinkielen koe, suomi 46 Lubenter approbatur\n' +
              '2012 kevät Ruotsi, keskipitkä oppimäärä 166 Cum laude approbatur\n' +
              '2012 kevät Englanti, pitkä oppimäärä 210 Cum laude approbatur\n' +
              '2012 kevät Maantiede 26 Magna cum laude approbatur\n' +
              '2012 kevät Matematiikan koe, lyhyt oppimäärä 59 Laudatur\n' +
              'Ylioppilastodistus\n' +
              'Todistuksen kieli\n' +
              ':\n' +
              'Lataa todistus'
          )
        })
      })

      describe('Kun tiedot löytyvät vain YTR:stä', function () {
        before(authentication.logout, etusivu.openPage)

        before(
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('010342-8411'),
          wait.until(omattiedot.isVisible)
        )

        describe('Sivun sisältö', function () {
          it('Näytetään opiskeluoikeudet', function () {
            expect(omattiedot.nimi()).to.equal('Mia Orvokki Numminen')
            expect(omattiedot.oppija()).to.equal('Opintoni')
            expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal([
              'Ylioppilastutkintolautakunta'
            ])
          })
        })
      })

      describe('Ostettu tietoa', function () {
        before(
          authentication.logout,
          etusivu.openPage,
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('080154-770R'),
          wait.until(omattiedot.isVisible),
          SuoritusjakoPage().avaaOpiskeluoikeus(
            'Autoalan perustutkinto (2019—, läsnä)'
          )
        )

        it('ei näytetä', function () {
          expect(extractAsText(S('.opiskeluoikeus-content'))).not.to.contain(
            'Ostettu'
          )
        })
      })

      describe('Opiskeluoikeuden organisaatio historiaa', function () {
        before(
          authentication.logout,
          etusivu.openPage,
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('200994-834A'),
          wait.until(omattiedot.isVisible),
          SuoritusjakoPage().avaaOpiskeluoikeus(
            'Tieto- ja viestintätekniikan perustutkinto, koulutusvientikokeilu (2016—, läsnä)'
          )
        )

        it('ei näytetä', function () {
          expect(extractAsText(S('.opiskeluoikeus-content'))).not.to.contain(
            'Opiskeluoikeuden organisaatiohistoria'
          )
        })
      })

      describe('Kun Virta-tietoja ei saada haettua', function () {
        before(authentication.logout, etusivu.openPage)

        before(
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('250390-680P'),
          wait.until(omattiedot.isVisible)
        )
        it('Näytetään varoitusteksti', function () {
          expect(omattiedot.nimi()).to.equal('Eivastaa Virtanen')
          expect(omattiedot.varoitukset()).to.equal(
            'Korkeakoulujen opintoja ei juuri nyt saada haettua. Yritä myöhemmin uudestaan.'
          )
        })
      })

      describe('Virhetilanne', function () {
        before(authentication.logout, etusivu.openPage)

        before(
          etusivu.login(),
          wait.until(korhopankki.isReady),
          korhopankki.login('010342-8413'),
          wait.until(VirhePage().isVisible)
        )

        describe('Sivun sisältö', function () {
          it('Näytetään virhesivu', function () {
            expect(VirhePage().teksti().trim()).to.equalIgnoreNewlines(
              'Koski-järjestelmässä tapahtui virhe, ole hyvä ja yritä myöhemmin uudelleen\n          Palaa etusivulle'
            )
          })
        })
      })
    })
  })
})
