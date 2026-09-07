import { expect, test } from './base'
import { KorkeakouluOpiskeluoikeus } from './pages/oppija/components/KorkeakouluOpiskeluoikeus'
import { kansalainen, virkailija } from './setup/auth'

test.describe('Korkeakoulutus: virkailija', () => {
  test.use({ storageState: virkailija('pää') })

  test.describe('Valmis diplomi-insinööri', () => {
    test('Opintojen tiedot ja lukuvuosimaksut', async ({
      virkailijaPage,
      oppijaHaku,
      oppijaPage
    }) => {
      await virkailijaPage.goto()
      await (await oppijaHaku.search('100869-192W')).clickOnFirst()
      await oppijaPage.selectOpiskeluoikeus('korkeakoulutus')

      await expect(
        oppijaPage.getOppilaitosNimi('Dipl.ins., konetekniikka')
      ).toHaveText('Aalto-yliopisto')
      const opiskeluoikeus = oppijaPage.getKorkeakouluOpiskeluoikeus(
        'Dipl.ins., konetekniikka'
      )
      await expect(opiskeluoikeus.tutkinnonNimi).toHaveText(
        'Dipl.ins., konetekniikka'
      )

      await expect(
        oppijaPage.opiskeluoikeudetNavValittuVälilehti.locator(
          '.opiskeluoikeus'
        )
      ).toHaveText([
        'korkeakoulututkinto 2013—2016, valmistunut',
        'korkeakoulunopintojakso'
      ])
      await opiskeluoikeus.avaaKaikki()
      await expect(opiskeluoikeus.ensimmäisenTutkinnonOsanNimi).toHaveText(
        'Vapaasti valittavat opinnot (KON)'
      )
      await tarkistaPuuttuvatMaksut(opiskeluoikeus)
    })
  })

  test.describe('Maisteri, jolla ensisijainen opiskeluoikeus', () => {
    test('Opintojen tiedot ja lukuvuosimaksut', async ({
      virkailijaPage,
      oppijaHaku,
      oppijaPage
    }) => {
      await virkailijaPage.goto()
      await (await oppijaHaku.search('250668-293Y')).clickOnFirst()
      await oppijaPage.selectOpiskeluoikeus('korkeakoulutus')

      await expect(
        oppijaPage.getOppilaitosNimi('Dipl.ins., kemian tekniikka')
      ).toHaveText('Aalto-yliopisto')
      const opiskeluoikeus = oppijaPage.getKorkeakouluOpiskeluoikeus(
        'Dipl.ins., kemian tekniikka'
      )
      await expect(opiskeluoikeus.tutkinnonNimi).toHaveText(
        'Dipl.ins., kemian tekniikka'
      )

      const maksuton = oppijaPage.getKorkeakouluOpiskeluoikeus(
        /Avoimen opinnot.*2009—2009/
      )
      await tarkistaPuuttuvatMaksut(maksuton)

      const maksullinen = oppijaPage.getKorkeakouluOpiskeluoikeus(
        /Avoimen opinnot.*2015—2016/
      )
      await maksullinen.avaaLisätiedot()
      await expect(maksullinen.lisätiedot).toContainText(
        'Maksettavat lukuvuosimaksut'
      )
      await expect(maksullinen.maksettavatLukuvuosimaksut).toBeVisible()
      await expect(maksullinen.maksettavatLukuvuosimaksut).toContainText(
        /20\.10\.2015[\s\S]*12\.4\.2016/
      )
      await expect(maksullinen.maksunSumma).toHaveText('4000')
    })
  })
})

test.describe('Korkeakoulutus: kansalainen', () => {
  test.describe('Valmis diplomi-insinööri', () => {
    test.use({ storageState: kansalainen('100869-192W') })

    test('Opintojen tiedot ja lukuvuosimaksut', async ({ kansalainenPage }) => {
      await kansalainenPage.goto()
      await kansalainenPage.openOpiskeluoikeus('Dipl.ins., konetekniikka')

      await expect(
        kansalainenPage.getOppilaitosNimi('Dipl.ins., konetekniikka')
      ).toHaveText('Aalto-yliopisto')
      const opiskeluoikeus = kansalainenPage.getKorkeakouluOpiskeluoikeus(
        'Dipl.ins., konetekniikka'
      )
      await expect(opiskeluoikeus.tutkinnonNimi).toHaveText(
        'Dipl.ins., konetekniikka'
      )

      await expect(opiskeluoikeus.ensimmäisenOpintojaksonPainike).toHaveText(
        'Vapaasti valittavat opinnot (KON)'
      )
      await opiskeluoikeus.avaaEnsimmäinenOpintojakso()
      await expect(
        opiskeluoikeus.ensimmäisenOpintojaksonLisätiedot
      ).toBeVisible()
      await tarkistaPuuttuvatMaksut(opiskeluoikeus)
    })
  })

  test.describe('Maisteri, jolla ensisijainen opiskeluoikeus', () => {
    test.use({ storageState: kansalainen('250668-293Y') })

    test('Opintojen tiedot ja lukuvuosimaksut', async ({ kansalainenPage }) => {
      await kansalainenPage.goto()
      await kansalainenPage.openOpiskeluoikeus('Dipl.ins., kemian tekniikka')

      await expect(
        kansalainenPage.getOppilaitosNimi('Dipl.ins., kemian tekniikka')
      ).toHaveText('Aalto-yliopisto')
      const opiskeluoikeus = kansalainenPage.getKorkeakouluOpiskeluoikeus(
        'Dipl.ins., kemian tekniikka'
      )
      await expect(opiskeluoikeus.tutkinnonNimi).toHaveText(
        'Dipl.ins., kemian tekniikka'
      )

      await kansalainenPage.openOpiskeluoikeus(/Avoimen opinnot.*2009—2009/)
      const maksuton = kansalainenPage.getKorkeakouluOpiskeluoikeus(
        /Avoimen opinnot.*2009—2009/
      )
      await tarkistaPuuttuvatMaksut(maksuton)

      await kansalainenPage.openOpiskeluoikeus(/Avoimen opinnot.*2015—2016/)
      const maksullinen = kansalainenPage.getKorkeakouluOpiskeluoikeus(
        /Avoimen opinnot.*2015—2016/
      )
      await maksullinen.avaaLisätiedot()
      await expect(maksullinen.lisätiedot).toContainText(
        'Maksettavat lukuvuosimaksut'
      )
      await expect(maksullinen.maksettavatLukuvuosimaksut).toBeVisible()
      await expect(maksullinen.maksettavatLukuvuosimaksut).toContainText(
        /20\.10\.2015[\s\S]*12\.4\.2016/
      )
      await expect(maksullinen.maksunSumma).toHaveText('4000')
    })
  })
})

async function tarkistaPuuttuvatMaksut(
  opiskeluoikeus: KorkeakouluOpiskeluoikeus
) {
  await opiskeluoikeus.avaaLisätiedot()
  await expect(opiskeluoikeus.maksettavatLukuvuosimaksut).toHaveCount(0)
  await expect(opiskeluoikeus.lisätiedot).not.toContainText(
    'Maksettavat lukuvuosimaksut'
  )
}
