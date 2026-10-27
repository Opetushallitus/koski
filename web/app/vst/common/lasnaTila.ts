import { todayISODate } from '../../date/date'
import { VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinOpiskeluoikeusjakso'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import { config } from '../../util/config'

// Vapaatavoitteisen koulutuksen ja osaamismerkin Läsnä-tila on sallittu vasta lain voimaantulosta alkaen (TOR-2786).
// Backend hylkää tilan sitä ennen, joten se piilotetaan valinnoista.

const rajatutSuoritustyypit = [
  'vstvapaatavoitteinenkoulutus',
  'vstosaamismerkki'
]

const rajatutJaksoluokat: string[] = [
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className,
  VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso.className
]

export const vstLäsnäTilaSiirrettävissä = (): boolean =>
  todayISODate() >= config().rajapäivät.vstLäsnäTilaSiirrettävissäAlkaen

export const vstLäsnäTilaPiilotettuSuoritustyypiltä = (
  suoritustyyppi?: string
): boolean =>
  suoritustyyppi !== undefined &&
  rajatutSuoritustyypit.includes(suoritustyyppi) &&
  !vstLäsnäTilaSiirrettävissä()

export const vstLäsnäTilaPiilotettuJaksoluokalta = (
  jaksoluokka: string
): boolean =>
  rajatutJaksoluokat.includes(jaksoluokka) && !vstLäsnäTilaSiirrettävissä()
