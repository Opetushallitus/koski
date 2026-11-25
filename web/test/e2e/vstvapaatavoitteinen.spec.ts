import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'

test.describe('Vapaan sivistystyön vapaatavoitteinen koulutus 1.1.2027 alkaen', () => {
  test.use({ storageState: virkailija('pää') })
  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Opiskeluoikeus voi jäädä tilaan Läsnä ilman osasuorituksia, mutta vahvistettu suoritus vaatii arvioidun osasuorituksen', async ({
    page,
    vstOppijaPage
  }) => {
    const uusiOppijaPage = new KoskiUusiOppijaPage(page)
    await uusiOppijaPage.goTo('150304A3115')
    await uusiOppijaPage.fill(
      {
        oppilaitos: 'Varsinais-Suomen kansanopisto',
        etunimet: 'Vilma',
        sukunimi: 'Vapaatavoitteinen',
        opiskeluoikeus: 'Vapaan sivistystyön koulutus',
        suoritustyyppi: 'Vapaan sivistystyön vapaatavoitteinen koulutus',
        // Läsnä on sallittu vain 1.1.2027 tai sen jälkeen voimassa olevalle opiskeluoikeudelle
        aloituspäivä: new Date(2027, 0, 8),
        opiskeluoikeudenTila: 'Läsnä'
      },
      true
    )
    // Opintokokonaisuus-kenttä ilmestyy vasta suoritustyypin valinnan jälkeen
    await uusiOppijaPage.fill({ opintokokonaisuus: '13435 4H Företagare' })
    await uusiOppijaPage.submitAndExpectSuccess()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toHaveText('Läsnä')

    // Vahvistus ja päättävä tila ilman yhtäkään arvioitua osasuoritusta
    await vstOppijaPage.edit()
    await vstOppijaPage.vahvistaSuoritusUudellaHenkilöllä(
      'Reijo',
      '9.1.2027',
      'Rehtori'
    )
    const tila = vstOppijaPage.$.opiskeluoikeus.tila.edit
    await tila.add.click()
    await tila.modal.date.set('9.1.2027')
    await tila.modal.tila.set('hyvaksytystisuoritettu')
    await tila.modal.submit.click()

    await vstOppijaPage.tallennaVirheellisenä(
      'Suoritus koulutus/099999 on merkitty valmiiksi, mutta sillä on tyhjä osasuorituslista tai opiskeluoikeudelta puuttuu linkitys',
      'Vapaatavoitteisella vapaan sivistystyön koulutuksella tulee olla vähintään yksi arvioitu osasuoritus'
    )

    // Arvioitu osasuoritus korjaa virheen
    await vstOppijaPage.addNewOsasuoritus('Testikurssi')
    await vstOppijaPage.osasuoritus(0).setVapaatavoitteinenArvosana(4)
    await vstOppijaPage.tallenna()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toHaveText('Hyväksytysti suoritettu')
  })
})
