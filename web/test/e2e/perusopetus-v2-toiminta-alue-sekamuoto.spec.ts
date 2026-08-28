import { Oppija } from '../../app/types/fi/oph/koski/schema/Oppija'
import { Raw } from '../../app/util/schema'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * TOR-2596: sekamuotoinen osasuorituslista, eli vuosiluokka jolla on sekä
 * tavallisia oppiaineita että toiminta-alueita.
 *
 * Perusteiden mukaan tila ei ole tuettu (toiminta-alueittain opiskellaan
 * oppiainejaon sijaan), mutta sitä syntyy tiedonsiirrosta ja kesken lukuvuoden
 * tehdyistä siirtymistä, joten käyttöliittymän on esitettävä se
 * ymmärrettävästi eikä piilotettava rivejä.
 */

const jynOid = '1.2.246.562.10.14613773812'
const jynNimi = 'Jyväskylän normaalikoulu'
const jynOppilaitos = {
  oid: jynOid,
  oppilaitosnumero: {
    koodiarvo: '00204',
    nimi: { fi: jynNimi, sv: jynNimi, en: jynNimi },
    koodistoUri: 'oppilaitosnumero'
  },
  nimi: { fi: jynNimi, sv: jynNimi, en: jynNimi },
  kotipaikka: {
    koodiarvo: '179',
    nimi: { fi: 'Jyväskylä', sv: 'Jyväskylä' },
    koodistoUri: 'kunta'
  }
}

const arviointi = (koodiarvo: string) => [
  {
    arvosana: {
      koodiarvo,
      koodistoUri: 'arviointiasteikkoyleissivistava'
    },
    päivä: '2025-06-01'
  }
]

const toimintaAlue = (koodiarvo: string) => ({
  tyyppi: {
    koodiarvo: 'perusopetuksentoimintaalue',
    koodistoUri: 'suorituksentyyppi'
  },
  koulutusmoduuli: {
    tunniste: { koodiarvo, koodistoUri: 'perusopetuksentoimintaalue' }
  },
  arviointi: arviointi('S')
})

const oppiaine = (koodiarvo: string, arvosana: string) => ({
  tyyppi: {
    koodiarvo: 'perusopetuksenoppiaine',
    koodistoUri: 'suorituksentyyppi'
  },
  koulutusmoduuli: {
    tunniste: { koodiarvo, koodistoUri: 'koskioppiaineetyleissivistava' },
    pakollinen: true
  },
  painotettuOpetus: false,
  arviointi: arviointi(arvosana)
})

/**
 * Tyhjä, Tero (hetu 230872-7258): opiskeluoikeudella on lisätietolippu
 * toiminta-alueittain opiskelusta ja 7. vuosiluokka, jolla on kolme
 * toiminta-aluetta ja kaksi oppiainetta.
 */
const sekamuotoinenTero = (): Raw<Oppija> => ({
  henkilö: {
    hetu: '230872-7258',
    etunimet: 'Tero',
    kutsumanimi: 'Tero',
    sukunimi: 'Tyhjä'
  },
  opiskeluoikeudet: [
    {
      tyyppi: {
        koodiarvo: 'perusopetus',
        koodistoUri: 'opiskeluoikeudentyyppi'
      },
      oppilaitos: jynOppilaitos,
      lisätiedot: {
        erityisenTuenPäätökset: [
          {
            alku: '2017-01-01',
            loppu: '2026-08-31',
            opiskeleeToimintaAlueittain: true
          }
        ]
      },
      tila: {
        opiskeluoikeusjaksot: [
          {
            alku: '2017-01-01',
            tila: {
              koodiarvo: 'lasna',
              koodistoUri: 'koskiopiskeluoikeudentila'
            }
          }
        ]
      },
      suoritukset: [
        {
          tyyppi: {
            koodiarvo: 'perusopetuksenoppimaara',
            koodistoUri: 'suorituksentyyppi'
          },
          koulutusmoduuli: {
            tunniste: { koodiarvo: '201101', koodistoUri: 'koulutus' },
            perusteenDiaarinumero: '104/011/2014'
          },
          toimipiste: jynOppilaitos,
          suorituskieli: { koodiarvo: 'FI', koodistoUri: 'kieli' },
          suoritustapa: {
            koodiarvo: 'koulutus',
            koodistoUri: 'perusopetuksensuoritustapa'
          }
        },
        {
          tyyppi: {
            koodiarvo: 'perusopetuksenvuosiluokka',
            koodistoUri: 'suorituksentyyppi'
          },
          koulutusmoduuli: {
            tunniste: {
              koodiarvo: '7',
              koodistoUri: 'perusopetuksenluokkaaste'
            },
            perusteenDiaarinumero: '104/011/2014'
          },
          luokka: '7A',
          alkamispäivä: '2024-08-15',
          toimipiste: jynOppilaitos,
          suorituskieli: { koodiarvo: 'FI', koodistoUri: 'kieli' },
          osasuoritukset: [
            toimintaAlue('1'),
            toimintaAlue('2'),
            toimintaAlue('3'),
            oppiaine('MA', '8'),
            oppiaine('LI', '9')
          ]
        }
      ]
    }
  ]
})

/**
 * Sama oppija ilman lisätietolippua ja ilman oppiaineita: pelkkä
 * toiminta-aluelista. Tuotannossa tällaisia päätason suorituksia on 1444
 * (805 opiskeluoikeutta). Ilman lippua taulukko jää normaalitilaan, jolloin
 * ryhmittely piilottaisi kaikki rivit muokkaustilassa.
 */
const puhdasToimintaAlueIlmanLippua = (): Raw<Oppija> => {
  const oppija = sekamuotoinenTero()
  const oo = oppija.opiskeluoikeudet[0] as Record<string, unknown>
  delete oo.lisätiedot
  const vuosiluokka = (oo.suoritukset as Record<string, unknown>[])[1]
  vuosiluokka.osasuoritukset = [
    toimintaAlue('1'),
    toimintaAlue('2'),
    toimintaAlue('3')
  ]
  return oppija
}

/**
 * Heidin raportoima tapaus: oppilas opiskeli toiminta-alueittain aiemmalla
 * vuosiluokalla, mutta on siirtynyt oppiaineittain. Erityisen tuen päätös on
 * päättynyt, mutta lisätietolippu on päivämäärätön, joten se ohjaisi yhä sekä
 * esitäytön että lisäyspudotuksen toiminta-alueisiin.
 */
const siirtynytOppiaineittain = (): Raw<Oppija> => {
  const oppija = sekamuotoinenTero()
  const oo = oppija.opiskeluoikeudet[0] as Record<string, unknown>
  oo.lisätiedot = {
    erityisenTuenPäätökset: [
      {
        alku: '2017-01-01',
        loppu: '2020-06-01',
        opiskeleeToimintaAlueittain: true
      }
    ]
  }
  const vuosiluokka = (oo.suoritukset as Record<string, unknown>[])[1]
  vuosiluokka.osasuoritukset = [toimintaAlue('1'), toimintaAlue('2')]
  return oppija
}

const v2Url = (oid: string) =>
  `${oid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

// Vuosiluokan suoritus on välilehti 1 (oppimäärä on 0).
const vuosiluokkaRivit = (page: import('@playwright/test').Page) =>
  page.locator(
    '[data-testid^="oo.0.suoritukset.1.osasuoritukset."][data-testid$=".nimi"]'
  )

test.describe('Perusopetuksen uusi käyttöliittymä: sekamuotoinen osasuorituslista', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Otsikot eivät väitä listan olevan pelkkiä oppiaineita tai toiminta-alueita', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(sekamuotoinenTero())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))
    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()

    const oppiaineet = page.locator('.oppiaineet')
    await expect(oppiaineet.locator('> h5')).toHaveText('Arvosanat')
    await expect(oppiaineet.locator('.OsasuoritusHeader')).toContainText(
      'Oppiaine tai toiminta-alue'
    )
  })

  test('Kaikki rivit näkyvät eikä listaa ryhmitellä pakollisiin ja valinnaisiin', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(sekamuotoinenTero())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))
    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()

    // Ryhmittely suodattaisi toiminta-alueet pois molemmista taulukoista.
    await expect(page.getByTestId('oppiaineet-pakolliset')).toHaveCount(0)
    await expect(page.getByTestId('oppiaineet-valinnaiset')).toHaveCount(0)

    await expect(vuosiluokkaRivit(page)).toHaveCount(5)
    await expect(vuosiluokkaRivit(page).nth(0)).toContainText('motoriset')
    await expect(vuosiluokkaRivit(page).nth(3)).toContainText('Matematiikka')
  })

  test('Muokkaustilassa kaikki rivit näkyvät eikä lisäyspudotusta tarjota', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(sekamuotoinenTero())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))
    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    await expect(vuosiluokkaRivit(page)).toHaveCount(5)
    await expect(page.getByTestId('oppiaineet-pakolliset')).toHaveCount(0)
    await expect(
      page.getByTestId('oo.0.suoritukset.1.uusi-oppiaine.input')
    ).toHaveCount(0)
  })

  test('Osasuorituksettomassa suorituksessa lisätietolippu ratkaisee yhä', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(sekamuotoinenTero())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    await expect(page.locator('.oppiaineet > h5')).toHaveText(
      'Toiminta-alueiden arvosanat'
    )
  })

  test('Pelkkä toiminta-aluelista ilman lisätietolippua ei tyhjene muokkaustilassa', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(puhdasToimintaAlueIlmanLippua())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))
    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()

    await expect(vuosiluokkaRivit(page)).toHaveCount(3)

    // Otsikot johdetaan sisällöstä, eivät lipusta.
    const oppiaineet = page.locator('.oppiaineet')
    await expect(oppiaineet.locator('> h5')).toHaveText(
      'Toiminta-alueiden arvosanat'
    )
    await expect(oppiaineet.locator('.OsasuoritusHeader')).toContainText(
      'Toiminta-alue'
    )

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Ilman lippua tila on normaali, joten ryhmittely kytkeytyisi
    // muokkaustilassa päälle ja suodattaisi kaikki toiminta-alueet pois.
    await expect(page.getByTestId('oppiaineet-pakolliset')).toHaveCount(0)
    await expect(page.getByTestId('oppiaineet-valinnaiset')).toHaveCount(0)
    await expect(vuosiluokkaRivit(page)).toHaveCount(3)

    // Lisäyspudotus seuraa samaa sisältöä kuin otsikot: toiminta-aluelistaan
    // lisätään toiminta-alueita, ei oppiaineita.
    await page.getByTestId('oo.0.suoritukset.1.uusi-oppiaine.input').click()
    await expect(
      page.locator('.Select__optionLabel').filter({ hasText: /motoriset/i })
    ).toHaveCount(0)
    await expect(
      page
        .locator('.Select__optionLabel')
        .filter({ hasText: /päivittäisten toimintojen taidot/i })
    ).toHaveCount(1)
  })
  test('Tyhjällä listalla kirjaustavan voi vaihtaa, jolloin oppiaineita voi lisätä', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(siirtynytOppiaineittain())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))
    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Lista ei ole tyhjä, joten kirjaustapaa ei tarjota.
    await expect(
      page.getByTestId('oo.0.suoritukset.1.kirjaustapa')
    ).toHaveCount(0)

    await page.getByTestId('oo.0.suoritukset.1.osasuoritukset.1.delete').click()
    await page.getByTestId('oo.0.suoritukset.1.osasuoritukset.0.delete').click()
    await expect(vuosiluokkaRivit(page)).toHaveCount(0)

    // Tyhjällä listalla valinta näkyy ja on oletuksena lipun mukainen.
    const kirjaustapa = page.getByTestId('oo.0.suoritukset.1.kirjaustapa.input')
    await expect(kirjaustapa).toBeChecked()

    // Vaihto oppiaineittain: otsikko ja ryhmittely seuraavat valintaa.
    await kirjaustapa.click()
    // Avain on 'Oppiaineiden arvosanat', mutta lokalisoitu teksti on
    // 'Arviointiasteikko' (koski-default-texts.json).
    await expect(page.locator('.oppiaineet > h5')).toHaveText(
      'Arviointiasteikko'
    )
    await expect(page.getByTestId('oppiaineet-pakolliset')).toHaveCount(1)
  })
  test('Uuden vuosiluokan kirjaustavan voi vaihtaa modaalissa, jolloin esitäyttö on oppiaineita', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(siirtynytOppiaineittain())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .getByRole('button', { name: /lisää vuosiluokan suoritus/i })
      .click()

    const modal = page.locator('.Modal')
    await modal.waitFor({ state: 'visible' })

    // Päätös on päättynyt, mutta lippu on päivämäärätön: oletus on yhä
    // toiminta-alueittain. Käyttäjä vaihtaa sen.
    const kirjaustapa = page.getByTestId(
      'oo.0.modal.uusiVuosiluokanSuoritus.kirjaustapa.input'
    )
    await expect(kirjaustapa).toBeChecked()
    await kirjaustapa.click()

    await page
      .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.luokka.input')
      .fill('8A')
    const pvm = page.getByTestId(
      'oo.0.modal.uusiVuosiluokanSuoritus.alkamispäivä.edit.input'
    )
    await pvm.fill('15.8.2024')
    await pvm.blur()
    await page.getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.submit').click()
    await modal.waitFor({ state: 'hidden' })

    // Esitäyttö on oppiaineita, ei viittä toiminta-aluetta.
    const rivit = page.locator(
      '[data-testid^="oo.0.suoritukset.2.osasuoritukset."][data-testid$=".nimi"]'
    )
    await expect(rivit.filter({ hasText: /motoriset taidot/i })).toHaveCount(0)
    await expect(rivit.filter({ hasText: /Matematiikka/i })).toHaveCount(1)
  })
})
