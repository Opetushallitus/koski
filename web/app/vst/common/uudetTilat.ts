import { todayISODate } from '../../date/date'
import { VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinOpiskeluoikeusjakso'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import { config } from '../../util/config'

// Vapaatavoitteisen koulutuksen Läsnä-tila sekä osaamismerkin Läsnä- ja Keskeytynyt-tilat ovat sallittuja
// vasta lain voimaantulosta alkaen (TOR-2786). Backend hylkää tilat sitä ennen, joten ne piilotetaan valinnoista.

const rajatutTilatSuoritustyypeittäin: Record<string, string[]> = {
  vstvapaatavoitteinenkoulutus: ['lasna'],
  vstosaamismerkki: ['lasna', 'keskeytynyt']
}

const rajatutTilatJaksoluokittain: Record<string, string[]> = {
  [VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className]:
    ['lasna'],
  [VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso.className]: [
    'lasna',
    'keskeytynyt'
  ]
}

export const vstUudetTilatSiirrettävissä = (): boolean =>
  todayISODate() >= config().rajapäivät.vstLäsnäTilaSiirrettävissäAlkaen

export const vstPiilotetutTilatSuoritustyypiltä = (
  suoritustyyppi?: string
): string[] =>
  suoritustyyppi === undefined || vstUudetTilatSiirrettävissä()
    ? []
    : (rajatutTilatSuoritustyypeittäin[suoritustyyppi] ?? [])

export const vstPiilotetutTilatJaksoluokalta = (
  jaksoluokka: string
): string[] =>
  vstUudetTilatSiirrettävissä()
    ? []
    : (rajatutTilatJaksoluokittain[jaksoluokka] ?? [])
