import { LaajuusVuosiviikkotunneissaTaiTunneissa } from './LaajuusVuosiviikkotunneissaTaiTunneissa'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PerusopetukseenValmistavaOpetus } from './PerusopetukseenValmistavaOpetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { PerusopetukseenValmistavanOpetuksenOsasuoritus } from './PerusopetukseenValmistavanOpetuksenOsasuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'
import { OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina } from './OmanAidinkielenOpinnotLaajuusVuosiviikkotunteina'

/**
 * Perusopetukseen valmistavan opetuksen suorituksen tiedot
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenSuoritus`
 */
export type PerusopetukseenValmistavanOpetuksenSuoritus = {
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenSuoritus'
  kokonaislaajuus?: LaajuusVuosiviikkotunneissaTaiTunneissa
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetukseenvalmistavaopetus'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: PerusopetukseenValmistavaOpetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PerusopetukseenValmistavanOpetuksenOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
}

export const PerusopetukseenValmistavanOpetuksenSuoritus = (o: {
  kokonaislaajuus?: LaajuusVuosiviikkotunneissaTaiTunneissa
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetukseenvalmistavaopetus'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: PerusopetukseenValmistavaOpetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PerusopetukseenValmistavanOpetuksenOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
}): PerusopetukseenValmistavanOpetuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetukseenvalmistavaopetus',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: PerusopetukseenValmistavaOpetus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '999905',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenSuoritus',
  ...o
})

PerusopetukseenValmistavanOpetuksenSuoritus.className =
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenSuoritus' as const

export const isPerusopetukseenValmistavanOpetuksenSuoritus = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenSuoritus'
