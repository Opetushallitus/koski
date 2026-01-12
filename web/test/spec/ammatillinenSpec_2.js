describe('Ammatillinen koulutus 2', function () {
  before(Authentication().login())

  var addOppija = AddOppijaPage()
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  describe('Opiskeluoikeuden mitätöiminen', function () {
    before(resetFixtures, page.openPage)
    describe('Mitätöintilinkki', function () {
      before(page.oppijaHaku.searchAndSelect('010101-123N'), editor.edit)
      it('Näytetään', function () {
        expect(opinnot.invalidateOpiskeluoikeusIsShown()).to.equal(true)
      })

      describe('Painettaessa', function () {
        before(opinnot.invalidateOpiskeluoikeus)
        it('Pyydetään vahvistus', function () {
          expect(opinnot.confirmInvalidateOpiskeluoikeusIsShown()).to.equal(
            true
          )
        })

        describe('Painettaessa uudestaan', function () {
          before(
            opinnot.confirmInvalidateOpiskeluoikeus,
            wait.until(page.oppijataulukko.isReady)
          )
          it('Opiskeluoikeus mitätöidään', function () {
            expect(page.isOpiskeluoikeusInvalidatedMessageShown()).to.equal(
              true
            )
          })

          describe('Mitätöityä opiskeluoikeutta', function () {
            before(
              syncPerustiedot,
              page.oppijataulukko.filterBy('nimi', 'Esimerkki')
            )
            it('Ei näytetä', function () {
              expect(page.oppijataulukko.names()).to.deep.equal([])
            })
          })

          describe('Vahvistusviestin', function () {
            before(
              opinnot.hideInvalidateMessage,
              wait.untilFalse(page.isOpiskeluoikeusInvalidatedMessageShown)
            )
            it('Voi piilottaa', function () {
              expect(page.isOpiskeluoikeusInvalidatedMessageShown()).to.equal(
                false
              )
            })
          })
        })
      })
    })

    describe('Opiskeluoikeudelle jossa ei ole valmiita suorituksia, ja joka on peräisin ulkoisesta järjestelmästä', function () {
      describe('Kun kirjautunut oppilaitoksen tallentajana', function () {
        before(
          Authentication().logout,
          Authentication().login(),
          page.openPage,
          page.oppijaHaku.searchAndSelect('270303-281N')
        )
        it('Ei näytetä mitätöintilinkkiä', function () {
          expect(opinnot.invalidateOpiskeluoikeusIsShown()).to.equal(false)
        })
      })
      describe('Kun kirjautunut oppilaitoksen pääkäyttäjänä', function () {
        before(
          Authentication().logout,
          Authentication().login('stadin-pää'),
          page.openPage,
          page.oppijaHaku.searchAndSelect('270303-281N'),
          wait.until(opinnot.invalidateOpiskeluoikeusIsShown)
        )
        it('Näytetään mitätöintilinkki', function () {
          expect(opinnot.invalidateOpiskeluoikeusIsShown()).to.equal(true)
        })
      })
    })
  })

  describe('Käyttöliittymän kautta luodun opiskeluoikeuden mitätöinti oppilaitoksen pääkäyttäjänä', function () {
    before(
      resetFixtures,
      Authentication().logout,
      Authentication().login('stadin-pää'),
      page.openPage
    )
    describe('Mitätöintilinkki', function () {
      before(page.oppijaHaku.searchAndSelect('010101-123N'))
      it('Näytetään', function () {
        expect(opinnot.invalidateOpiskeluoikeusIsShown()).to.equal(true)
      })

      describe('Painettaessa', function () {
        before(opinnot.invalidateOpiskeluoikeus)
        it('Pyydetään vahvistus', function () {
          expect(opinnot.confirmInvalidateOpiskeluoikeusIsShown()).to.equal(
            true
          )
        })

        describe('Painettaessa uudestaan', function () {
          before(
            opinnot.confirmInvalidateOpiskeluoikeus,
            wait.until(page.oppijataulukko.isReady)
          )
          it('Opiskeluoikeus mitätöidään', function () {
            expect(page.isOpiskeluoikeusInvalidatedMessageShown()).to.equal(
              true
            )
          })

          describe('Mitätöityä opiskeluoikeutta', function () {
            before(
              syncPerustiedot,
              page.oppijataulukko.filterBy('nimi', 'Esimerkki')
            )
            it('Ei näytetä', function () {
              expect(page.oppijataulukko.names()).to.deep.equal([])
            })
          })

          describe('Vahvistusviestin', function () {
            before(
              opinnot.hideInvalidateMessage,
              wait.untilFalse(page.isOpiskeluoikeusInvalidatedMessageShown)
            )
            it('Voi piilottaa', function () {
              expect(page.isOpiskeluoikeusInvalidatedMessageShown()).to.equal(
                false
              )
            })
          })
        })
      })
    })
  })

  describe('Tietojen muuttaminen', function () {
    before(addOppija.addNewOppija('kalle', '280608-6619'))

    it('Aluksi ei näytetä "Kaikki tiedot tallennettu" -tekstiä', function () {
      expect(page.isSavedLabelShown()).to.equal(false)
    })

    describe('Järjestämismuodot', function () {
      var järjestämismuodot = editor.property('järjestämismuodot')
      before(
        editor.edit,
        järjestämismuodot.addItem,
        järjestämismuodot
          .propertyBySelector('.järjestämismuoto')
          .setValue('Koulutuksen järjestäminen oppisopimuskoulutuksena'),
        järjestämismuodot.property('nimi').setValue('Virheellinen'),
        järjestämismuodot.property('yTunnus').setValue('123')
      )

      it('Aluksi näyttää y-tunnuksen esimerkin', function () {
        expect(
          järjestämismuodot.propertyBySelector('.yTunnus input').elem()[0]
            .placeholder,
          'Esimerkki: 1234567-8'
        )
      })

      describe('Epävalidi y-tunnus', function () {
        before(
          järjestämismuodot.property('nimi').setValue('Virheellinen'),
          järjestämismuodot.property('yTunnus').setValue('123')
        )

        it('Ei anna tallentaa virheellistä y-tunnusta', function () {
          expect(opinnot.onTallennettavissa()).to.equal(false)
        })
      })

      describe('Validi y-tunnus', function () {
        before(
          editor.cancelChanges,
          editor.edit,
          järjestämismuodot.addItem,
          järjestämismuodot.propertyBySelector('.alku').setValue('22.8.2017'),
          järjestämismuodot
            .propertyBySelector('.järjestämismuoto')
            .setValue('Koulutuksen järjestäminen oppisopimuskoulutuksena'),
          järjestämismuodot.property('nimi').setValue('Autohuolto oy'),
          järjestämismuodot.property('yTunnus').setValue('1629284-5'),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )

        it('Toimii', function () {
          expect(page.isSavedLabelShown()).to.equal(true)
          expect(extractAsText(S('.järjestämismuodot'))).to.equal(
            'Järjestämismuodot 22.8.2017 — , Koulutuksen järjestäminen oppisopimuskoulutuksena\n' +
            'Yritys Autohuolto oy Y-tunnus 1629284-5'
          )
        })
      })
    })

    describe('Opiskeluoikeuden ostettu tieto', function () {
      describe('Aluksi', function () {
        it('ei näytetä', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).not.to.contain(
            'Ostettu'
          )
        })

        describe('Muuttaminen', function () {
          before(
            editor.edit,
            editor.property('ostettu').setValue(true),
            editor.saveChangesAndWaitForSuccess
          )

          it('toimii', function () {
            expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.contain(
              'Ostettu kyllä'
            )
          })
        })
      })
    })

    describe('Opiskeluoikeuden lisätiedot', function () {
      before(
        editor.edit,
        opinnot.expandAll,
        editor.property('hojks').addValue,
        editor
          .property('hojks')
          .property('opetusryhmä')
          .setValue('Erityisopetusryhmä'),
        editor.property('oikeusMaksuttomaanAsuntolapaikkaan').setValue(true),
        editor.property('ulkomaanjaksot').addItem,
        editor
          .property('ulkomaanjaksot')
          .propertyBySelector('.alku')
          .setValue('22.6.2017'),
        editor.property('ulkomaanjaksot').property('maa').setValue('Algeria'),
        editor
          .property('ulkomaanjaksot')
          .property('kuvaus')
          .setValue('Testing'),
        editor.property('majoitus').addItem,
        editor
          .property('majoitus')
          .propertyBySelector('.alku')
          .setValue('22.6.2017'),
        editor
          .property('majoitus')
          .propertyBySelector('.loppu')
          .setValue('1.1.2099'),
        editor.property('osaAikaisuusjaksot').addItem,
        editor
          .property('osaAikaisuusjaksot')
          .propertyBySelector('.alku')
          .setValue('22.6.2017'),
        editor
          .property('osaAikaisuusjaksot')
          .property('osaAikaisuus')
          .setValue('80'),
        editor.property('opiskeluvalmiuksiaTukevatOpinnot').addItem,
        editor
          .property('opiskeluvalmiuksiaTukevatOpinnot')
          .propertyBySelector('.alku')
          .setValue('22.6.2017'),
        editor
          .property('opiskeluvalmiuksiaTukevatOpinnot')
          .propertyBySelector('.loppu')
          .setValue('28.6.2017'),
        editor
          .property('opiskeluvalmiuksiaTukevatOpinnot')
          .property('kuvaus')
          .setValue('Testing'),
        editor.saveChanges,
        wait.until(page.isSavedLabelShown)
      )

      it('Toimii', function () {
        expect(extractAsText(S('.lisätiedot'))).to.equal(
          'Lisätiedot\n' +
          'Majoitus 22.6.2017 — 1.1.2099\n' +
          'Ulkomaanjaksot 22.6.2017 — Maa Algeria Kuvaus Testing\n' +
          'Hojks Opetusryhmä Erityisopetusryhmä\n' +
          'Osa-aikaisuusjaksot 22.6.2017 — Osa-aikaisuus 80 %\n' +
          'Opiskeluvalmiuksia tukevat opinnot 22.6.2017 — 28.6.2017 Kuvaus Testing'
        )
      })
    })

    describe('Osaamisala', function () {
      describe('Osaamisalalista haetaan', function () {
        before(editor.edit)

        it('eperusteista', function () {
          expect(textsOf(toArray(S('.osaamisala .options li')))).to.deep.equal([
            'Ei valintaa',
            'Autokorinkorjauksen osaamisala (1525)',
            'Automaalauksen osaamisala (1526)',
            'Automyynnin osaamisala (1527)',
            'Autotekniikan osaamisala (1528)',
            'Moottorikäyttöisten pienkoneiden korjauksen osaamisala (1622)',
            'Varaosamyynnin osaamisala (1529)'
          ])
        })

        describe('Kun perustetta ei löydy eperusteista', function () {
          before(
            page.openPage,
            page.oppijaHaku.searchAndSelect('201137-361Y'),
            editor.edit
          )

          it('haetaan kaikki osaamisalat', function () {
            var osaamisalat = textsOf(toArray(S('.osaamisala .options li')))

            expect(osaamisalat.slice(0, 5)).to.deep.equal([
              'Ei valintaa',
              'Agroautomaation hyödyntämisen osaamisala (3169)',
              'Agrologistiikan osaamisala (2413)',
              'Aikuisliikunnan osaamisala (2065)',
              'Aikuisten perusopetus (0009)'
            ])

            expect(osaamisalat.slice(-5)).to.deep.equal([
              'Yritystoiminnan suunnittelun ja käynnistämisen osaamisala (2284)',
              'Äänitekniikan osaamisala (2240)',
              'Ääniteknikko (2128)',
              'Äänitetuottaja (2127)',
              'Äänityön osaamisala (2007)'
            ])
          })
        })
      })

      describe('Tallennus ilman päivämääriä', function () {
        before(
          editor.edit,
          editor
            .property('osaamisala')
            .itemEditor(0)
            .property('osaamisala')
            .setValue('Automyynnin osaamisala'),
          editor.saveChanges
        )
        it('toimii', function () {
          expect(editor.property('osaamisala').getText()).to.equal(
            'Osaamisala Automyynnin osaamisala'
          )
        })
      })

      describe('Päivämäärien lisäys', function () {
        before(
          editor.edit,
          editor
            .property('osaamisala')
            .itemEditor(0)
            .property('alku')
            .setValue('1.1.2017'),
          editor.saveChanges
        )

        it('toimii', function () {
          expect(editor.property('osaamisala').getText()).to.equal(
            'Osaamisala Automyynnin osaamisala 1.1.2017 —'
          )
        })
      })

      after(page.openPage, page.oppijaHaku.searchAndSelect('280608-6619'))
    })

    describe('Tutkinnon osat', function () {
      var suoritustapa = editor.property('suoritustapa')
      describe('Kun suoritustapa on opetussuunnitelman mukainen', function () {
        describe('Tutkinnon osan lisääminen', function () {
          before(editor.edit)
          describe('Aluksi', function () {
            it('Taulukko on tyhjä', function () {
              expect(opinnot.tutkinnonOsat('1').tyhjä()).to.equal(true)
            })
            it('Näytetään laajuussarake ja -yksikkö muokattaessa', function () {
              expect(opinnot.tutkinnonOsat().laajuudenOtsikko()).to.equal(
                'Laajuus (osp)'
              )
            })
          })
          describe('Pakollisen tutkinnon osan lisääminen', function () {
            describe('Ennen lisäystä', function () {
              it('Näyttää e-perusteiden mukaisen vaihtoehtolistan', function () {
                expect(
                  opinnot.tutkinnonOsat('1').tutkinnonosavaihtoehdot().length
                ).to.equal(47)
              })

              it('Näytetään pakollisten tutkinnon osien otsikkorivi', function () {
                expect(
                  opinnot.tutkinnonOsat('1').isGroupHeaderVisible()
                ).to.equal(true)
              })
            })

            describe('Lisäyksen jälkeen', function () {
              before(
                opinnot
                  .tutkinnonOsat('1')
                  .lisääTutkinnonOsa('Huolto- ja korjaustyöt')
              )
              it('lisätty osa näytetään', function () {
                expect(
                  opinnot.tutkinnonOsat('1').tutkinnonOsa(0).nimi()
                ).to.equal('Huolto- ja korjaustyöt')
              })
              describe('Arvosanan lisääminen', function () {
                before(
                  opinnot
                    .tutkinnonOsat('1')
                    .tutkinnonOsa(0)
                    .propertyBySelector('.arvosana')
                    .setValue('3', 1)
                )

                describe('Lisättäessä', function () {
                  it('Merkitsee tutkinnon osan tilaan VALMIS', function () {
                    expect(
                      opinnot.tilaJaVahvistus.merkitseValmiiksiEnabled()
                    ).to.equal(true)
                  })
                })

                describe('Tallentamisen jälkeen', function () {
                  before(editor.saveChanges, wait.forAjax)

                  describe('Käyttöliittymän tila', function () {
                    it('näyttää edelleen oikeat tiedot', function () {
                      expect(
                        opinnot.tutkinnonOsat().tutkinnonOsa(0).nimi()
                      ).to.equal('Huolto- ja korjaustyöt')
                    })
                  })

                  describe('Arvosanan poistaminen', function () {
                    before(
                      editor.edit,
                      opinnot
                        .tutkinnonOsat('1')
                        .tutkinnonOsa(0)
                        .propertyBySelector('.arvosana')
                        .setValue('Ei valintaa'),
                      editor.saveChanges
                    )
                    it('Tallennus onnistuu ja suoritus siirtyy tilaan KESKEN', function () {
                      expect(
                        opinnot.tutkinnonOsat().tutkinnonOsa(0).tila()
                      ).to.equal('Suoritus kesken')
                    })
                  })

                  describe('Laajuus', function () {
                    describe('Kun siirrytään muokkaamaan tietoja', function () {
                      before(editor.edit)
                      it('Laajuussarake ja laajuuden yksikkö näytetään', function () {
                        expect(
                          opinnot.tutkinnonOsat().laajuudenOtsikko()
                        ).to.equal('Laajuus (osp)')
                      })

                      describe('Kun syötetään laajuus ja tallennetaan', function () {
                        before(
                          opinnot
                            .tutkinnonOsat(1)
                            .tutkinnonOsa(0)
                            .property('laajuus')
                            .setValue('30'),
                          opinnot
                            .tutkinnonOsat('1')
                            .tutkinnonOsa(0)
                            .propertyBySelector('.arvosana')
                            .setValue('3', 1),
                          editor.saveChanges
                        )
                        it('Näytetään laajuus', function () {
                          expect(
                            opinnot.tutkinnonOsat().laajuudenOtsikko()
                          ).to.equal('Laajuus (osp)')
                          expect(
                            opinnot.tutkinnonOsat().laajuudetYhteensä()
                          ).to.equal('30')
                        })

                        describe('Kun poistetaan arvosana ja tallennetaan', function () {
                          before(
                            editor.edit,
                            opinnot
                              .tutkinnonOsat(1)
                              .tutkinnonOsa(0)
                              .propertyBySelector('.arvosana')
                              .setValue('Ei valintaa'),
                            editor.saveChanges
                          )

                          it('Laajuutta ei lasketa arvioimattomista', function () {
                            expect(
                              opinnot.tutkinnonOsat().laajuudetYhteensä()
                            ).to.equal('0')
                          })
                        })

                        describe('Kun poistetaan laajuus ja tallennetaan', function () {
                          before(
                            editor.edit,
                            opinnot
                              .tutkinnonOsat(1)
                              .tutkinnonOsa(0)
                              .property('laajuus')
                              .setValue(''),
                            editor.saveChanges
                          )

                          it('Laajuussarake piilotetaan', function () {
                            expect(
                              opinnot.tutkinnonOsat().laajuudenOtsikko()
                            ).to.equal('')
                          })
                        })
                      })
                    })
                  })

                  describe('Tutkinnon osan poistaminen', function () {
                    before(
                      editor.edit,
                      opinnot.tutkinnonOsat('1').tutkinnonOsa(0)
                        .poistaTutkinnonOsa,
                      editor.saveChanges
                    )
                    it('toimii', function () {
                      expect(opinnot.tutkinnonOsat().tyhjä()).to.equal(true)
                    })
                  })
                })
              })
            })
          })

          describe('Yhteisen tutkinnon osan lisääminen', function () {
            before(editor.edit)

            describe('Ennen lisäystä', function () {
              it('Näyttää e-perusteiden mukaisen vaihtoehtolistan', function () {
                expect(
                  opinnot.tutkinnonOsat('2').tutkinnonosavaihtoehdot()
                ).to.deep.equal([
                  '101054 Matemaattis-luonnontieteellinen osaaminen',
                  '101056 Sosiaalinen ja kulttuurinen osaaminen',
                  '101053 Viestintä- ja vuorovaikutusosaaminen',
                  '101055 Yhteiskunnassa ja työelämässä tarvittava osaaminen'
                ])
              })
            })

            describe('Lisäyksen jälkeen', function () {
              before(
                opinnot
                  .tutkinnonOsat('2')
                  .lisääTutkinnonOsa(
                    'Matemaattis-luonnontieteellinen osaaminen'
                  )
              )
              it('lisätty osa näytetään', function () {
                expect(
                  opinnot.tutkinnonOsat('2').tutkinnonOsa(0).nimi()
                ).to.equal('Matemaattis-luonnontieteellinen osaaminen')
              })

              describe('Tutkinnon osan osa-alueen lisääminen', function () {
                describe('Ennen lisäystä', function () {
                  it('Näyttää e-perusteiden mukaisen vaihtoehtolistan', function () {
                    expect(
                      opinnot
                        .tutkinnonOsat('2')
                        .tutkinnonOsa(0)
                        .osanOsat()
                        .tutkinnonosavaihtoehdot()
                    ).to.deep.equal([
                      'ETK Etiikka',
                      'FK Fysiikka ja kemia',
                      'MLFK Fysikaaliset ja kemialliset ilmiöt ja niiden soveltaminen',
                      'YTKK Kestävän kehityksen edistäminen',
                      'VVAI17 Kommunikation och interaktion på modersmålet, svenska som andraspråk',
                      'KU Kulttuurien tuntemus',
                      'MAVA Matemaattis-luonnontieteellinen osaaminen',
                      'MA Matematiikka',
                      'MLMA Matematiikka ja matematiikan soveltaminen',
                      'YTOU Opiskelu- ja urasuunnitteluvalmiudet',
                      'PS Psykologia',
                      'TAK Taide ja kulttuuri',
                      'VVTL Taide ja luova ilmaisu',
                      'TVT Tieto- ja viestintätekniikka sekä sen hyödyntäminen',
                      'VVTD Toiminta digitaalisessa ympäristössä',
                      'TK1 Toinen kotimainen kieli, ruotsi',
                      'TK2 Toinen kotimainen kieli, suomi',
                      'YTTT Työelämässä toimiminen',
                      'TET Työelämätaidot',
                      'YTTH Työkyvyn ja hyvinvoinnin ylläpitäminen',
                      'TYT Työkyvyn ylläpitäminen, liikunta ja terveystieto',
                      'VK Vieraat kielet',
                      'VVTK Viestintä ja vuorovaikutus toisella kotimaisella kielellä',
                      'VVVK Viestintä ja vuorovaikutus vieraalla kielellä',
                      'VVAI22 Viestintä ja vuorovaikutus äidinkielellä',
                      'VVAI16 Viestintä ja vuorovaikutus äidinkielellä, opiskelijan äidinkieli',
                      'VVAI4 Viestintä ja vuorovaikutus äidinkielellä, romani',
                      'VVAI8 Viestintä ja vuorovaikutus äidinkielellä, ruotsi toisena kielenä',
                      'VVAI3 Viestintä ja vuorovaikutus äidinkielellä, saame',
                      'VVAI Viestintä ja vuorovaikutus äidinkielellä, suomi',
                      'VVAI7 Viestintä ja vuorovaikutus äidinkielellä, suomi toisena kielenä',
                      'VVAI11 Viestintä ja vuorovaikutus äidinkielellä, suomi viittomakielisille',
                      'VVAI15 Viestintä ja vuorovaikutus äidinkielellä, viittomakieli',
                      '003 Viestintä- ja vuorovaikutusosaaminen',
                      '001 Yhteiskunnassa ja kansalaisena toimiminen',
                      'YTYK Yhteiskunnassa ja kansalaisena toimiminen',
                      'YKT Yhteiskuntataidot',
                      'YM Ympäristöosaaminen',
                      'YTYY Yrittäjyys ja yrittäjämäinen toiminta',
                      'YYT Yrittäjyys ja yritystoiminta',
                      'AI Äidinkieli'
                    ])
                  })
                })

                describe('Lisäyksen jälkeen', function () {
                  var tutkinnonOsienOsat = opinnot.tutkinnonOsat('999999')
                  before(
                    tutkinnonOsienOsat.lisääTutkinnonOsa('MA Matematiikka'),
                    opinnot
                      .tutkinnonOsat('999999')
                      .tutkinnonOsa(0)
                      .property('laajuus')
                      .setValue('3'),
                    editor.saveChanges,
                    opinnot.avaaKaikki
                  )
                  it('lisätty osa näytetään', function () {
                    expect(
                      opinnot.tutkinnonOsat('999999').tutkinnonOsa(0).nimi()
                    ).to.equal('Matematiikka')
                  })

                  describe('Paikallinen tutkinnon osan osa-alue', function () {
                    before(
                      editor.edit,
                      opinnot.avaaKaikki,
                      tutkinnonOsienOsat.lisääPaikallinenTutkinnonOsa(
                        'Hassut temput'
                      )
                    )

                    describe('Lisäyksen jälkeen', function () {
                      it('lisätty osa näytetään', function () {
                        expect(
                          tutkinnonOsienOsat.tutkinnonOsa(1).nimi()
                        ).to.equal('Hassut temput')
                      })
                    })

                    describe('Tallennuksen jälkeen', function () {
                      before(editor.saveChanges, opinnot.avaaKaikki)
                      it('lisätty osa näytetään', function () {
                        expect(
                          tutkinnonOsienOsat.tutkinnonOsa(1).nimi()
                        ).to.equal('Hassut temput')
                      })
                    })
                  })
                })
              })
            })
          })
          describe('Vapaavalintaisen tutkinnon osan lisääminen', function () {
            describe('Valtakunnallinen tutkinnon osa', function () {
              before(
                editor.edit,
                opinnot
                  .tutkinnonOsat('3')
                  .lisääTutkinnonOsa('Huippuosaajana toimiminen')
              )

              describe('Lisäyksen jälkeen', function () {
                it('lisätty osa näytetään', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Huippuosaajana toimiminen')
                })
              })

              describe('Tallennuksen jälkeen', function () {
                before(editor.saveChanges)
                it('lisätty osa näytetään', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Huippuosaajana toimiminen')
                })
              })
            })

            describe('Paikallinen tutkinnon osa', function () {
              before(
                editor.edit,
                opinnot.tutkinnonOsat('3').tutkinnonOsa(0).poistaTutkinnonOsa,
                opinnot
                  .tutkinnonOsat('3')
                  .lisääPaikallinenTutkinnonOsa('Hassut temput')
              )

              describe('Lisäyksen jälkeen', function () {
                it('lisätty osa näytetään', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Hassut temput')
                })
              })

              describe('Tallennuksen jälkeen', function () {
                before(editor.saveChanges)
                it('lisätty osa näytetään', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Hassut temput')
                })
              })
            })

            describe('Tutkinnon osa toisesta tutkinnosta', function () {
              describe('Kun valitaan sama tutkinto, kuin mitä ollaan suorittamassa', function () {
                before(
                  editor.edit,
                  opinnot.tutkinnonOsat('3').tutkinnonOsa(0).poistaTutkinnonOsa,
                  opinnot
                    .tutkinnonOsat('3')
                    .lisääTutkinnonOsaToisestaTutkinnosta(
                      'Autoalan perustutkinto',
                      'Auton korjaaminen'
                    ),
                  editor.saveChanges
                )
                it('Lisäys onnistuu (siksi, että dataan ei tule tutkinto-kenttää)', function () {
                  expect(
                    opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                  ).to.equal('Auton korjaaminen')
                })
              })
              describe('Kun valitaan toinen tutkinto', function () {
                before(
                  page.oppijaHaku.searchAndSelect('211097-402L'),
                  editor.edit,
                  opinnot.tutkinnonOsat('3').tutkinnonOsa(0).poistaTutkinnonOsa,
                  opinnot
                    .tutkinnonOsat('3')
                    .lisääTutkinnonOsaToisestaTutkinnosta(
                      'Autoalan perustutkinto',
                      'Auton korjaaminen'
                    )
                )

                describe('Lisäyksen jälkeen', function () {
                  it('lisätty osa näytetään', function () {
                    expect(
                      opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                    ).to.equal('Auton korjaaminen')
                  })
                })

                describe('Tallennuksen jälkeen', function () {
                  before(editor.saveChanges)
                  it('lisätty osa näytetään', function () {
                    expect(
                      opinnot.tutkinnonOsat('3').tutkinnonOsa(0).nimi()
                    ).to.equal('Auton korjaaminen')
                  })
                })
              })
            })
          })
        })
      })

      describe('Osaamisen tunnustamisen muokkaus', function () {
        var tunnustaminen = opinnot
          .tutkinnonOsat('1')
          .tutkinnonOsa(0)
          .property('tunnustettu')

        before(
          page.oppijaHaku.searchAndSelect('280608-6619'),
          editor.edit,
          opinnot
            .tutkinnonOsat('1')
            .lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
          opinnot.expandAll
        )

        describe('Alussa', function () {
          it('Ei osaamisen tunnustamistietoa, lisäysmahdollisuus', function () {
            expect(tunnustaminen.getValue()).to.equal(
              'Lisää osaamisen tunnustaminen'
            )
          })
        })

        describe('Lisääminen', function () {
          before(
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0)
              .lisääOsaamisenTunnustaminen,
            tunnustaminen
              .propertyBySelector('.selite')
              .setValue('Tunnustamisen esimerkkiselite'),
            editor.saveChanges,
            opinnot.expandAll
          )

          describe('Tallennuksen jälkeen', function () {
            it('Osaamisen tunnustamisen selite näytetään', function () {
              expect(tunnustaminen.getText()).to.equal(
                'Tunnustettu\nSelite Tunnustamisen esimerkkiselite\nRahoituksen piirissä ei'
              )
            })
          })

          describe('Muokkaus', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              tunnustaminen
                .propertyBySelector('.selite')
                .setValue('Tunnustamisen muokattu esimerkkiselite'),
              tunnustaminen.property('rahoituksenPiirissä').setValue(true),
              editor.saveChanges,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(tunnustaminen.getText()).to.equal(
                'Tunnustettu\nSelite Tunnustamisen muokattu esimerkkiselite\nRahoituksen piirissä kyllä'
              )
            })
          })

          describe('Poistaminen', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0)
                .poistaOsaamisenTunnustaminen,
              editor.saveChanges,
              editor.edit,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(tunnustaminen.getValue()).to.equal(
                'Lisää osaamisen tunnustaminen'
              )
            })
          })
        })
      })

      describe('Tutkinnon osan lisätietojen muokkaus', function () {
        function lisätiedot() {
          return opinnot.tutkinnonOsat('1').tutkinnonOsa(0).lisätiedot()
        }

        before(
          page.oppijaHaku.searchAndSelect('280608-6619'),
          editor.edit,
          opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaTutkinnonOsa,
          opinnot
            .tutkinnonOsat('1')
            .lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
          opinnot.expandAll
        )

        describe('Alussa', function () {
          it('ei lisätietoja', function () {
            expect(lisätiedot().getValue()).to.equal('lisää uusi')
          })
        })

        describe('Lisääminen', function () {
          before(
            lisätiedot().addItem,
            lisätiedot()
              .propertyBySelector(
                '.ammatillisentutkinnonosanlisatieto .dropdown-wrapper'
              )
              .setValue('Muu lisätieto'),
            lisätiedot()
              .propertyBySelector('.kuvaus')
              .setValue('Muita tietoja'),
            editor.saveChanges,
            opinnot.expandAll
          )

          describe('Tallennuksen jälkeen', function () {
            it('toimii', function () {
              expect(lisätiedot().getText()).to.equal(
                'Lisätiedot\nMuu lisätieto\nMuita tietoja'
              )
            })
          })

          describe('Muokkaus', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              lisätiedot()
                .propertyBySelector(
                  '.ammatillisentutkinnonosanlisatieto .dropdown-wrapper'
                )
                .setValue('Osaamisen arvioinnin mukauttaminen'),
              lisätiedot()
                .propertyBySelector('.kuvaus')
                .setValue('Arviointia on mukautettu'),
              editor.saveChanges,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(lisätiedot().getText()).to.equal(
                'Lisätiedot\nOsaamisen arvioinnin mukauttaminen\nArviointia on mukautettu'
              )
            })
          })

          describe('Poistaminen', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaLisätieto,
              editor.saveChanges,
              editor.edit,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(lisätiedot().getValue()).to.equal('lisää uusi')
            })
          })
        })
      })

      describe('Näytön muokkaus', function () {
        before(
          editor.edit,
          opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaTutkinnonOsa,
          opinnot.tutkinnonOsat('1').lisääTutkinnonOsa('Huolto- ja korjaustyöt')
        )

        describe('Alussa', function () {
          it('ei näyttöä', function () {
            expect(
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0).näyttö().getValue()
            ).to.equal('Lisää ammattiosaamisen näyttö')
          })
        })

        describe('Lisääminen', function () {
          before(
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0).avaaNäyttöModal,
            opinnot
              .tutkinnonOsat('1')
              .tutkinnonOsa(0)
              .asetaNäytönTiedot({
                kuvaus: 'Näytön esimerkkikuvaus',
                suorituspaikka: [
                  'työpaikka',
                  'Esimerkkityöpaikka, Esimerkkisijainti'
                ],
                työssäoppimisenYhteydessä: false,
                arvosana: '3',
                arvioinnistaPäättäneet: ['Opettaja'],
                arviointikeskusteluunOsallistuneet: ['Opettaja', 'Opiskelija'],
                arviointipäivä: '1.2.2017'
              }),
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0).painaOkNäyttöModal
          )
          it('toimii', function () {
            expect(
              opinnot
                .tutkinnonOsat('1')
                .tutkinnonOsa(0)
                .näyttö()
                .property('arvosana')
                .getValue()
            ).to.equal('3')
            expect(
              opinnot
                .tutkinnonOsat('1')
                .tutkinnonOsa(0)
                .näyttö()
                .property('kuvaus')
                .getValue()
            ).to.equal('Näytön esimerkkikuvaus')
          })
        })

        describe('Muokkaus', function () {
          before(
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0).avaaNäyttöModal,
            opinnot
              .tutkinnonOsat('1')
              .tutkinnonOsa(0)
              .asetaNäytönTiedot({
                kuvaus: 'Näytön muokattu esimerkkikuvaus',
                suorituspaikka: [
                  'työpaikka',
                  'Esimerkkityöpaikka, Esimerkkisijainti'
                ],
                työssäoppimisenYhteydessä: true,
                arvosana: '2',
                arvioinnistaPäättäneet: ['Opettaja'],
                arviointikeskusteluunOsallistuneet: ['Opettaja', 'Opiskelija'],
                arviointipäivä: '1.2.2017'
              }),
            opinnot.tutkinnonOsat('1').tutkinnonOsa(0).painaOkNäyttöModal
          )
          describe('Näyttää oikeat tiedot', function () {
            it('toimii', function () {
              expect(
                opinnot
                  .tutkinnonOsat('1')
                  .tutkinnonOsa(0)
                  .näyttö()
                  .property('arvosana')
                  .getValue()
              ).to.equal('2')
              expect(
                opinnot
                  .tutkinnonOsat('1')
                  .tutkinnonOsa(0)
                  .näyttö()
                  .property('kuvaus')
                  .getValue()
              ).to.equal('Näytön muokattu esimerkkikuvaus')
            })
          })
          describe('Oikeat tiedot säilyvät modalissa', function () {
            before(opinnot.tutkinnonOsat('1').tutkinnonOsa(0).avaaNäyttöModal)
            it('toimii', function () {
              var näyttö = opinnot
                .tutkinnonOsat('1')
                .tutkinnonOsa(0)
                .lueNäyttöModal()
              expect(näyttö.kuvaus).to.equal('Näytön muokattu esimerkkikuvaus')
              expect(näyttö.suorituspaikka).to.deep.equal([
                'työpaikka',
                'Esimerkkityöpaikka, Esimerkkisijainti'
              ])
              expect(näyttö.työssäoppimisenYhteydessä).to.equal(true)
              expect(näyttö.arvosana).to.equal('2')
              expect(näyttö.arvioinnistaPäättäneet).to.deep.equal(['Opettaja'])
              expect(näyttö.arviointikeskusteluunOsallistuneet).to.deep.equal([
                'Opettaja',
                'Opiskelija'
              ])
              expect(näyttö.arviointipäivä).to.equal('1.2.2017')
            })
            after(opinnot.tutkinnonOsat('1').tutkinnonOsa(0).painaOkNäyttöModal)
          })
        })

        describe('Tallentamisen jälkeen', function () {
          before(editor.saveChanges, editor.edit, opinnot.expandAll)
          it('näyttää edelleen oikeat tiedot', function () {
            expect(
              opinnot
                .tutkinnonOsat('1')
                .tutkinnonOsa(0)
                .näyttö()
                .property('kuvaus')
                .getValue()
            ).to.equal('Näytön muokattu esimerkkikuvaus')
          })
        })

        describe('Poistaminen', function () {
          before(opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaNäyttö)
          it('toimii', function () {
            expect(
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0).näyttö().getValue()
            ).to.equal('Lisää ammattiosaamisen näyttö')
          })
        })

        describe('Tallentamisen jälkeen', function () {
          before(editor.saveChanges, editor.edit, opinnot.expandAll)
          it('näyttää edelleen oikeat tiedot', function () {
            expect(
              opinnot.tutkinnonOsat('1').tutkinnonOsa(0).näyttö().getValue()
            ).to.equal('Lisää ammattiosaamisen näyttö')
          })
        })
      })

      describe('Sanallisen arvioinnin muokkaus', function () {
        describe('VALMA-suorituksen osille', function () {
          var sanallinenArviointi = opinnot
            .tutkinnonOsat()
            .tutkinnonOsa(0)
            .sanallinenArviointi()

          before(
            prepareForNewOppija('kalle', '230872-7258'),
            addOppija.enterValidDataAmmatillinen(),
            addOppija.selectSuoritustyyppi(
              'Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)'
            ),
            addOppija.submitAndExpectSuccess(
              'Tyhjä, Tero (230872-7258)',
              'Ammatilliseen koulutukseen valmentava koulutus (VALMA)'
            ),
            editor.edit,
            opinnot
              .tutkinnonOsat()
              .lisääPaikallinenTutkinnonOsa('Hassut temput')
          )

          describe('Alussa', function () {
            it('syöttökenttä ei näytetä', function () {
              expect(sanallinenArviointi.isVisible()).to.equal(false)
            })
          })

          describe('Kun arvosana lisätty', function () {
            before(
              opinnot
                .tutkinnonOsat()
                .tutkinnonOsa(0)
                .propertyBySelector('.arvosana')
                .setValue('3', 1)
            )

            it('syöttökenttä näytetään', function () {
              expect(sanallinenArviointi.isVisible()).to.equal(true)
            })
          })

          describe('Muokkaus', function () {
            before(sanallinenArviointi.setValue('Hyvin meni'))

            it('näyttää oikeat tiedot', function () {
              expect(sanallinenArviointi.getValue()).to.equal('Hyvin meni')
            })
          })

          describe('Tallentamisen jälkeen', function () {
            before(editor.saveChanges, editor.edit, opinnot.expandAll)

            it('näyttää edelleen oikeat tiedot', function () {
              expect(sanallinenArviointi.getValue()).to.equal('Hyvin meni')
            })
          })

          describe('Arvosanan poistamisen ja uudelleenlisäämisen jälkeen', function () {
            before(
              opinnot
                .tutkinnonOsat()
                .tutkinnonOsa(0)
                .propertyBySelector('.arvosana')
                .setValue('Ei valintaa'),
              opinnot
                .tutkinnonOsat()
                .tutkinnonOsa(0)
                .propertyBySelector('.arvosana')
                .setValue('3', 1)
            )

            it('syöttökenttä on tyhjä', function () {
              expect(sanallinenArviointi.getValue()).to.equal('')
            })
          })
        })
      })

      describe('Kun suoritustapana on näyttö', function () {
        before(
          addOppija.addNewOppija('kalle', '280608-6619', {
            suoritustapa: 'Näyttötutkinto'
          }),
          editor.edit
        )

        it('Tutkinnon osia ei ryhmitellä', function () {
          expect(opinnot.tutkinnonOsat('1').isGroupHeaderVisible()).to.equal(
            false
          )
        })

        it('Keskiarvo-kenttä ei ole näkyvissä', function () {
          expect(editor.property('keskiarvo').isVisible()).to.equal(false)
        })

        describe('Tutkinnon osan lisääminen', function () {
          before(
            opinnot.tutkinnonOsat().lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
            editor.saveChanges
          )
          it('toimii', function () { })

          describe('Tutkinnon osan poistaminen', function () {
            before(
              editor.edit,
              opinnot.tutkinnonOsat().tutkinnonOsa(0).poistaTutkinnonOsa,
              editor.saveChanges
            )
            it('toimii', function () {
              expect(opinnot.tutkinnonOsat().tyhjä()).to.equal(true)
            })
          })
        })
      })

      describe('Uuden arvioinnin lisääminen', function () {
        function arviointi() {
          return opinnot.tutkinnonOsat('1').tutkinnonOsa(0).arviointi()
        }

        function lisääArviointi() {
          const addItemElems = opinnot
            .tutkinnonOsat('1')
            .tutkinnonOsa(0)
            .arviointi()
            .elem()
            .find('.add-item a')
          click(addItemElems[addItemElems.length - 1])()
        }

        function arviointiNth(nth) {
          return opinnot.tutkinnonOsat('1').tutkinnonOsa(0).arviointiNth(nth)
        }

        function poistaArvioinnit() {
          const items = opinnot
            .tutkinnonOsat('1')
            .tutkinnonOsa(0)
            .arviointi()
            .elem()
            .find('.remove-item')
          click(items)()
        }

        before(
          prepareForNewOppija('kalle', '060918-7919'),
          addOppija.enterHenkilötiedot({
            etunimet: 'Tero',
            kutsumanimi: 'Tero',
            sukunimi: 'Tyhjä'
          }),
          addOppija.selectOppilaitos('Stadin'),
          addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
          addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
          addOppija.selectTutkinto('Autoalan perustutkinto', 2),
          addOppija.selectAloituspäivä('1.1.2018'),
          addOppija.selectOpintojenRahoitus(
            'Valtionosuusrahoitteinen koulutus'
          ),
          addOppija.selectMaksuttomuus(0),
          addOppija.submitAndExpectSuccess('Tyhjä, Tero (060918-7919)'),
          editor.edit,
          opinnot.tutkinnonOsat('1').tutkinnonOsa(0).poistaTutkinnonOsa,
          opinnot
            .tutkinnonOsat('1')
            .lisääTutkinnonOsa('Huolto- ja korjaustyöt'),
          opinnot.expandAll
        )

        describe('Alussa', function () {
          it('ei lisätietoja', function () {
            expect(arviointi().getValue()).to.equal('lisää uusi')
          })
        })

        describe('Lisääminen', function () {
          before(lisääArviointi, lisääArviointi)

          it('toimii', function () {
            expect(arviointi().getText()).to.include(
              'Arviointi Arvosana Arviointiasteikko'
            )
          })

          describe('Muokkaus', function () {
            before(
              arviointiNth(0).propertyBySelector('.arvosana').selectValue(3),
              arviointiNth(1).propertyBySelector('.arvosana').selectValue(4)
            )

            it('toimii', function () {
              expect(arviointi().getText()).to.include(
                'Arviointi Arvosana Arviointiasteikko'
              )
            })
          })

          describe('Tallennetaan', function () {
            before(editor.saveChanges, opinnot.expandAll)

            it('näkyy oikein', function () {
              const arviointipäivä = moment().format('D.M.YYYY')
              expect(arviointi().getText()).to.equal(
                'Arviointi Arvosana 3\nArviointipäivä ' +
                arviointipäivä +
                '\nArvosana 4\nArviointipäivä ' +
                arviointipäivä
              )
            })
          })

          describe('Poistaminen', function () {
            before(
              editor.edit,
              opinnot.expandAll,
              poistaArvioinnit,
              editor.saveChanges,
              editor.edit,
              opinnot.expandAll
            )
            it('toimii', function () {
              expect(arviointi().getValue()).to.equal('lisää uusi')
            })
          })
        })
      })
    })

    describe('Päätason suorituksen poistaminen', function () {
      before(
        Authentication().logout,
        Authentication().login(),
        page.openPage,
        page.oppijaHaku.searchAndSelect('250989-419V'),
        editor.edit
      )

      describe('Mitätöintilinkki', function () {
        it('Näytetään', function () {
          expect(opinnot.deletePäätasonSuoritusIsShown()).to.equal(true)
        })

        describe('Painettaessa', function () {
          before(opinnot.deletePäätasonSuoritus)
          it('Pyydetään vahvistus', function () {
            expect(opinnot.confirmDeletePäätasonSuoritusIsShown()).to.equal(
              true
            )
          })

          describe('Painettaessa uudestaan', function () {
            before(
              opinnot.confirmDeletePäätasonSuoritus,
              wait.until(page.isPäätasonSuoritusDeletedMessageShown)
            )
            it('Päätason suoritus poistetaan', function () {
              expect(page.isPäätasonSuoritusDeletedMessageShown()).to.equal(
                true
              )
            })

            describe('Poistettua päätason suoritusta', function () {
              before(
                wait.until(page.isReady),
                opinnot.opiskeluoikeudet.valitseOpiskeluoikeudenTyyppi(
                  'ammatillinenkoulutus'
                )
              )

              it('Ei näytetä', function () {
                expect(opinnot.suoritusTabs()).to.deep.equal([
                  'Autoalan työnjohdon erikoisammattitutkinto'
                ])
              })
            })
          })
        })
      })
    })
  })
})
