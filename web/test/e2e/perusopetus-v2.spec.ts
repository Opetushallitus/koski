import { expect, test } from './base'
import { virkailija } from './setup/auth'

const kaisaOid = '1.2.246.562.24.00000000007'
const tommiOid = '1.2.246.562.24.00000000051'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`
const tommiUrl = `${tommiOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })
  test('Renderöi perusopetuksen oppimäärän tiedot', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)

    // Opiskeluoikeuden otsikko + edit-painike
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
      'Jyväskylän normaalikoulu, perusopetus'
    )
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toContainText(
      'Muokkaa'
    )

    // Opiskeluoikeuden voimassaoloaika
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
    ).toContainText('15.8.2008 – 4.6.2016')

    // Suorituksen tiedot
    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toContainText(
      'Perusopetus'
    )
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suoritustapa.value')
    ).toContainText('Koulutus')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.organisaatio.value')
    ).toContainText('Jyväskylän normaalikoulu')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')
    ).toContainText('suomi')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.koulusivistyskieli')
    ).toContainText('suomi')

    // Ensimmäisen oppiaineen arvosana
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')
    ).toContainText('9')

    // Suorituksen vahvistus
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.status')
    ).toContainText('Suoritus valmis')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.details')
    ).toContainText('Vahvistus: 4.6.2016 Jyväskylä')
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.value.henkilö.0'
      )
    ).toContainText('Reijo Reksi (rehtori)')

    // Opiskeluoikeuden tila (kaksi jaksoa: ensin valmistunut, sitten läsnä)
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.date')
    ).toContainText('4.6.2016')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toContainText('Valmistunut')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.date')
    ).toContainText('15.8.2008')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.tila')
    ).toContainText('Läsnä')
  })

  test('Näyttää oppiaineiden arvosanat', async ({ page, oppijaPage }) => {
    await oppijaPage.goto(kaisaUrl)

    // Arvosteluasteikko-kuvaus
    await expect(
      page.getByTestId('perusopetuksen-arvosteluasteikko')
    ).toContainText('Arvostelu 4-10, S (suoritettu) tai H (hylätty)')

    // Pakollinen oppiaine numeerisella arvosanalla (uskonto)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.5.nimi')
    ).toContainText('Uskonto/Elämänkatsomustieto')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.5.arvosana.value')
    ).toContainText('10')

    // Pakollinen oppiaine yksilöllistetyllä oppimäärällä (biologia)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.11.nimi')
    ).toContainText('Biologia')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.11.arvosana.value')
    ).toContainText('9')

    // Pakollinen oppiaine painotetulla opetuksella (liikunta)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.19.nimi')
    ).toContainText('Liikunta')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.19.arvosana.value')
    ).toContainText('9')

    // Valinnainen oppiaine numeerisella arvosanalla (B2-kieli, saksa)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.21.nimi')
    ).toContainText('B2-kieli')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.21.arvosana.value')
    ).toContainText('9')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.21.laajuus.value')
    ).toContainText('4 vuosiviikkotuntia')

    // Valinnainen oppiaine hyväksytty-arvosanalla (B1-kieli, ruotsi)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.nimi')
    ).toContainText('B1-kieli')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.arvosana.value')
    ).toContainText('S')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.laajuus.value')
    ).toContainText('1 vuosiviikkotuntia')

    // Paikallinen oppiaine (Tietokoneen hyötykäyttö)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.22.nimi')
    ).toContainText('Tietokoneen hyötykäyttö')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.22.arvosana.value')
    ).toContainText('9')

    // Footnote-merkinnät: yksilöllistetty (*) ja painotettu (**)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.11.footnote')
    ).toHaveText('*')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.19.footnote')
    ).toHaveText('**')
  })

  test('Renderöi 8. luokan lukuvuosisuorituksen', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)

    // Vaihda 8. vuosiluokan välilehdelle (tab-järjestys: oppimäärä, 9, 8, 7)
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

    // Suorituksen tiedot
    await expect(page.getByTestId('oo.0.suoritukset.2.koulutus')).toContainText(
      '8. vuosiluokka 8'
    )
    await expect(
      page.getByTestId('oo.0.suoritukset.2.luokka.value')
    ).toContainText('8C')
    await expect(
      page.getByTestId('oo.0.suoritukset.2.alkamispäivä.value')
    ).toContainText('15.8.2014')
    await expect(
      page.getByTestId('oo.0.suoritukset.2.organisaatio.value')
    ).toContainText('Jyväskylän normaalikoulu')
    await expect(
      page.getByTestId('oo.0.suoritukset.2.suoritustapa.value')
    ).toContainText('Koulutus')
    await expect(
      page.getByTestId('oo.0.suoritukset.2.suorituskieli.value')
    ).toContainText('suomi')
    await expect(
      page.getByTestId('oo.0.suoritukset.2.muutSuorituskielet')
    ).toContainText('sloveeni')
    await expect(
      page.getByTestId('oo.0.suoritukset.2.kielikylpykieli.value')
    ).toContainText('ruotsi')

    // Suorituksen vahvistus
    await expect(
      page.getByTestId('oo.0.suoritukset.2.suorituksenVahvistus.value.status')
    ).toContainText('Suoritus valmis')
    await expect(
      page.getByTestId('oo.0.suoritukset.2.suorituksenVahvistus.value.details')
    ).toContainText('Vahvistus: 30.5.2015 Jyväskylä')
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.2.suorituksenVahvistus.value.luokalleSiirtyminen'
      )
    ).toContainText('Siirretään seuraavalle luokalle')

    // Käyttäytymisen arviointi: arvosana + sanallinen kuvaus
    await expect(
      page.getByTestId('oo.0.suoritukset.2.kayttaytyminen.arvosana')
    ).toContainText('S')
    await expect(
      page.getByTestId('oo.0.suoritukset.2.kayttaytyminen.kuvaus.value')
    ).toContainText('Esimerkillistä käyttäytymistä koko vuoden ajan')
  })

  test('Renderöi 9. luokan lukuvuosisuorituksen', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)

    // 9. vuosiluokka on toinen tabi (oppimäärä, 9, 8, 7)
    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()

    await expect(page.getByTestId('oo.0.suoritukset.1.koulutus')).toContainText(
      '9. vuosiluokka 9'
    )
    await expect(
      page.getByTestId('oo.0.suoritukset.1.luokka.value')
    ).toContainText('9C')
    await expect(
      page.getByTestId('oo.0.suoritukset.1.alkamispäivä.value')
    ).toContainText('15.8.2015')
    await expect(
      page.getByTestId('oo.0.suoritukset.1.suorituksenVahvistus.value.details')
    ).toContainText('Vahvistus: 30.5.2016 Jyväskylä')
  })

  test('Renderöi luokalle jääneen 7. luokan lukuvuosisuorituksen', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)

    // 7. vuosiluokka on viimeinen tabi (oppimäärä, 9, 8, 7)
    await page.getByTestId('oo.0.suoritusTabs.3.tab').click()

    await expect(page.getByTestId('oo.0.suoritukset.3.koulutus')).toContainText(
      '7. vuosiluokka 7'
    )
    await expect(
      page.getByTestId('oo.0.suoritukset.3.luokka.value')
    ).toContainText('7C')
    await expect(
      page.getByTestId('oo.0.suoritukset.3.alkamispäivä.value')
    ).toContainText('16.8.2013')
    await expect(
      page.getByTestId('oo.0.suoritukset.3.jääLuokalle.value')
    ).toContainText('Kyllä')
    await expect(
      page.getByTestId('oo.0.suoritukset.3.suorituksenVahvistus.value.details')
    ).toContainText('Vahvistus: 30.5.2014 Jyväskylä')
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.3.suorituksenVahvistus.value.luokalleSiirtyminen'
      )
    ).toContainText('Ei siirretä seuraavalle luokalle')
  })

  test('Renderöi toiminta-alueittain opiskelleen oppijan päättötodistuksen', async ({
    page,
    oppijaPage
  }) => {
    // Tommi Toiminta, 031112-020J
    await oppijaPage.goto(tommiUrl)

    // Opiskeluoikeuden otsikko
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
      'Jyväskylän normaalikoulu, perusopetus'
    )

    // Opiskeluoikeuden voimassaoloaika
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
    ).toContainText('15.8.2017 – 18.10.2024')

    // Suorituksen tiedot
    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toContainText(
      'Perusopetus'
    )
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suoritustapa.value')
    ).toContainText('Erityinen tutkinto')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.organisaatio.value')
    ).toContainText('Jyväskylän normaalikoulu')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')
    ).toContainText('suomi')

    // Toiminta-alueiden nimet ja arvosanat (5 kpl, kaikki S) + 5 vvt laajuus
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.nimi')
    ).toContainText('motoriset taidot')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')
    ).toContainText('S')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.laajuus.value')
    ).toContainText('5 vuosiviikkotuntia')
    // Sanallinen arviointi motorisille taidoille
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.osasuoritukset.0.sanallinenArviointi.value'
      )
    ).toContainText('Motoriset taidot kehittyneet hyvin perusopetuksen aikana')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.nimi')
    ).toContainText('kieli ja kommunikaatio')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.laajuus.value')
    ).toContainText('5 vuosiviikkotuntia')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.arvosana.value')
    ).toContainText('S')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.nimi')
    ).toContainText('sosiaaliset taidot')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.arvosana.value')
    ).toContainText('S')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.3.nimi')
    ).toContainText('päivittäisten toimintojen taidot')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.3.arvosana.value')
    ).toContainText('S')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.nimi')
    ).toContainText('kognitiiviset taidot')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.arvosana.value')
    ).toContainText('S')

    // Suorituksen vahvistus
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.status')
    ).toContainText('Suoritus valmis')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.details')
    ).toContainText('Vahvistus: 18.10.2024 Jyväskylä')
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.value.henkilö.0'
      )
    ).toContainText('Reijo Reksi (rehtori)')

    // Opiskeluoikeuden tila
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.date')
    ).toContainText('18.10.2024')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toContainText('Valmistunut')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.date')
    ).toContainText('15.8.2017')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.tila')
    ).toContainText('Läsnä')

    // Lisätiedot: yksittäiset aikajaksot
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.pidennettyOppivelvollisuus.alku'
      )
    ).toContainText('15.8.2019')
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.pidennettyOppivelvollisuus.loppu'
      )
    ).toContainText('18.10.2024')
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.joustavaPerusopetus.alku'
      )
    ).toContainText('15.8.2017')
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.joustavaPerusopetus.loppu'
      )
    ).toContainText('18.10.2024')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.majoitusetu.alku')
    ).toContainText('15.8.2017')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.kuljetusetu.alku')
    ).toContainText('15.8.2017')

    // Lisätiedot: aikajakso-listat (useita jaksoja)
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.kotiopetusjaksot.0.alku')
    ).toContainText('15.8.2017')
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.kotiopetusjaksot.0.loppu'
      )
    ).toContainText('18.10.2024')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.kotiopetusjaksot.1.alku')
    ).toContainText('14.7.2026')
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.kotiopetusjaksot.1.loppu'
      )
    ).toContainText('18.10.2026')

    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.ulkomaanjaksot.0.alku')
    ).toContainText('15.8.2017')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.ulkomaanjaksot.1.alku')
    ).toContainText('16.9.2027')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.ulkomaanjaksot.1.loppu')
    ).toContainText('2.10.2028')

    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.sisäoppilaitosmainenMajoitus.0.alku'
      )
    ).toContainText('1.9.2021')
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.sisäoppilaitosmainenMajoitus.0.loppu'
      )
    ).toContainText('1.9.2022')

    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.koulukoti.0.alku')
    ).toContainText('1.9.2022')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.koulukoti.0.loppu')
    ).toContainText('1.9.2023')

    // Lisätiedot: vammainen-aikajaksot
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.vammainen.0.alku')
    ).toContainText('15.8.2019')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.vammainen.0.loppu')
    ).toContainText('1.9.2019')
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.vaikeastiVammainen.0.alku'
      )
    ).toContainText('2.9.2019')
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.vaikeastiVammainen.0.loppu'
      )
    ).toContainText('18.10.2024')

    // Lisätiedot: vuosiluokkiin sitoutumaton opetus (boolean)
    await expect(
      page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.vuosiluokkiinSitoutumatonOpetus.value'
      )
    ).toContainText('Kyllä')
  })
})
