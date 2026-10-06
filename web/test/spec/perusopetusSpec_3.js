describe('Perusopetus 3', function () {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var tilaJaVahvistus = opinnot.tilaJaVahvistus
  var editor = opinnot.opiskeluoikeusEditor()

  before(Authentication().login(), resetFixtures)

  describe('Perusopetuksen oppiaineen oppimäärän suoritus', function () {
    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('110738-839L')
    )
    describe('Kaikki tiedot näkyvissä', function () {
      it('näyttää opiskeluoikeuden tiedot', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal([
          'Jyväskylän normaalikoulu, Perusopetuksen oppiaineen oppimäärä (2008—2018, valmistunut)'
        ])
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 15.8.2008 — 4.6.2018\n' +
          'Tila 4.6.2018 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
          '15.8.2008 Läsnä (valtionosuusrahoitteinen koulutus)'
        )
      })

      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Oppiaine Äidinkieli ja kirjallisuus\n' +
          'Kieli Suomen kieli ja kirjallisuus\n' +
          'Peruste 19/011/2015\n' +
          'Oppilaitos / toimipiste Jyväskylän normaalikoulu\n' +
          'Arvosana 9\n' +
          'Suoritustapa Erityinen tutkinto\n' +
          'Suorituskieli suomi\n' +
          'Suoritus valmis Vahvistus : 4.6.2016 Jyväskylä Reijo Reksi , rehtori'
        )
      })

      describe('muokkaustilassa', function () {
        before(editor.edit)

        it('näyttää pakollinen-kentän editorin', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties .pakollinen')
            )
          ).to.contain(
            'Yhteinen oppiaine'
          )
        })
      })
    })

    describe('Monta oppiainetta', function () {
      before(page.openPage, page.oppijaHaku.searchAndSelect('131298-5248'))
      it('näyttää opiskeluoikeuden otsikon oikein', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal([
          'Jyväskylän normaalikoulu, Perusopetuksen oppiaineen oppimäärä (2008—2018, valmistunut)'
        ])
      })
    })

    describe('Tietojen muuttaminen', function () {
      var arvosana = editor.property('arviointi')

      before(page.openPage, page.oppijaHaku.searchAndSelect('110738-839L'))
      before(editor.edit, editor.property('tila').removeItem(0)) // opiskeluoikeus: läsnä

      describe('Kun arviointi poistetaan', function () {
        before(arvosana.setValue('Ei valintaa'), editor.saveChanges)

        it('Suoritus siirtyy tilaan KESKEN', function () {
          expect(tilaJaVahvistus.text()).to.equal('Suoritus kesken')
        })

        describe('Kun muokataan suoritusta', function () {
          before(editor.edit)

          it('Valmiiksi merkintä on estetty', function () {
            expect(tilaJaVahvistus.merkitseValmiiksiEnabled()).to.equal(false)
          })

          describe('Kun lisätään arvosana', function () {
            before(
              arvosana.setValue('8'),
              tilaJaVahvistus.merkitseValmiiksi,
              tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                .itemEditor(0)
                .setValue('Lisää henkilö'),
              tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                .itemEditor(0)
                .propertyBySelector('.nimi')
                .setValue('Reijo Reksi'),
              tilaJaVahvistus.merkitseValmiiksiDialog.myöntäjät
                .itemEditor(0)
                .propertyBySelector('.titteli')
                .setValue('rehtori'),
              tilaJaVahvistus.merkitseValmiiksiDialog.merkitseValmiiksi,
              editor.saveChanges
            )

            it('Valmiiksi merkintä on mahdollista', function () { })
          })
        })
      })
      describe('Kurssit', function () {
        describe('Kurssin lisääminen päättövaiheen kursseista', function () {
          var äidinkieli = Oppiaine(
            findSingle('.perusopetuksenoppiaineenoppimaaransuoritus')
          )

          before(
            editor.edit,
            tilaJaVahvistus.merkitseKeskeneräiseksi,
            äidinkieli.avaaLisääKurssiDialog
          )
          it('Näytetään vain oikean oppiaineen kurssit', function () {
            expect(äidinkieli.lisääKurssiDialog.kurssit().length).to.equal(11)
          })

          describe('Kun lisätään kurssi', function () {
            before(
              äidinkieli.lisääKurssiDialog.valitseKurssi(
                'Uutisia ja mielipiteitä'
              ),
              äidinkieli.lisääKurssiDialog.lisääKurssi
            )

            describe('Kun annetaan arvosana ja tallennetaan', function () {
              before(
                äidinkieli.kurssi('S21').arvosana.setValue('8'),
                äidinkieli.kurssi('S21').toggleDetails,
                äidinkieli.kurssi('S21').arviointipäivä.setValue('1.1.2024'),
                äidinkieli.kurssi('S21').toggleDetails,
                editor.saveChanges
              )

              it('toimii', function () { })
            })
          })
        })

        describe('Kurssin lisääminen alkuvaiheen kursseista', function () {
          var äidinkieli = Oppiaine(
            findSingle('.perusopetuksenoppiaineenoppimaaransuoritus')
          )

          before(editor.edit, äidinkieli.avaaAlkuvaiheenLisääKurssiDialog)
          it('Näytetään kaikki alkuvaiheen äidinkielen kurssit', function () {
            expect(äidinkieli.lisääKurssiDialog.kurssit().length).to.equal(52)
          })

          describe('Kun lisätään kurssi', function () {
            before(
              äidinkieli.lisääKurssiDialog.valitseKurssi(
                'Kehittyvä kielitaito: Asuminen'
              ),
              äidinkieli.lisääKurssiDialog.lisääKurssi
            )

            describe('Kun annetaan arvosana ja tallennetaan', function () {
              before(
                äidinkieli.kurssi('AS211').arvosana.setValue('5'),
                äidinkieli.kurssi('AS211').toggleDetails,
                äidinkieli.kurssi('AS211').arviointipäivä.setValue('1.1.2024'),
                äidinkieli.kurssi('AS211').toggleDetails,
                editor.saveChanges
              )

              it('toimii', function () { })
            })
          })
        })
      })
    })
  })

  describe('Perusopetuksen useamman oppiaineen aineopiskelija', function () {
    describe('Opiskeluoikeuden tilaa', function () {
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('131298-5248'),
        editor.edit,
        editor.property('tila').removeItem(0),
        opinnot.valitseSuoritus(undefined, 'Äidinkieli ja kirjallisuus'),
        opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
        opinnot.valitseSuoritus(undefined, 'Yhteiskuntaoppi'),
        opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
        opinnot.avaaLisaysDialogi
      )

      it('ei voida merkitä valmiiksi', function () {
        expect(OpiskeluoikeusDialog().radioEnabled('valmistunut')).to.equal(
          false
        )
      })

      describe('Kun yksikin suoritus merkitään valmiiksi', function () {
        before(
          opinnot.tilaJaVahvistus.merkitseValmiiksi,
          opinnot.tilaJaVahvistus.lisääVahvistus('01.01.2000'),
          opinnot.avaaLisaysDialogi,
          OpiskeluoikeusDialog().tila().aseta('valmistunut'),
          OpiskeluoikeusDialog().opintojenRahoitus().aseta('1'),
          OpiskeluoikeusDialog().tallenna,
          editor.saveChanges
        )

        it('myös opiskeluoikeuden tila voidaan merkitä valmiiksi', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.contain(
            'Valmistunut'
          )
        })
      })

      after(editor.cancelChanges)
    })

    describe('Jos opiskelijalla on "ei tiedossa"-oppiaineita', function () {
      var lisääSuoritus = opinnot.lisääSuoritusDialog
      before(
        page.openPage,
        page.oppijaHaku.searchAndSelect('131298-5248'),
        editor.edit,
        editor.property('tila').removeItem(0),
        lisääSuoritus.open('lisää oppiaineen suoritus'),
        wait.forAjax,
        lisääSuoritus.property('tunniste').setValue('Ei tiedossa'),
        lisääSuoritus.toimipiste.select('Jyväskylän normaalikoulu, alakoulu'),
        lisääSuoritus.lisääSuoritus,
        opinnot.avaaLisaysDialogi
      )

      it('Opiskeluoikeuden tilaa ei voi merkitä valmiiksi', function () {
        expect(OpiskeluoikeusDialog().radioEnabled('valmistunut')).to.equal(
          false
        )
      })

      after(editor.cancelChanges)
    })
  })
})
