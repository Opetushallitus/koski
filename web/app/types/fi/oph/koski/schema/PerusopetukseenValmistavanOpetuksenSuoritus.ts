import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina } from './OmanAidinkielenOpinnotLaajuusVuosiviikkotunteina'
import { PerusopetukseenValmistavaOpetus } from './PerusopetukseenValmistavaOpetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { PerusopetukseenValmistavanOpetuksenOsasuoritus } from './PerusopetukseenValmistavanOpetuksenOsasuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Perusopetukseen valmistavan opetuksen suorituksen tiedot
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenSuoritus`
 */
export type PerusopetukseenValmistavanOpetuksenSuoritus = {
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenSuoritus'
  kokonaislaajuus?: LaajuusVuosiviikkotunneissa
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetukseenvalmistavaopetus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: PerusopetukseenValmistavaOpetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PerusopetukseenValmistavanOpetuksenOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const PerusopetukseenValmistavanOpetuksenSuoritus = (o: {
  kokonaislaajuus?: LaajuusVuosiviikkotunneissa
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetukseenvalmistavaopetus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: PerusopetukseenValmistavaOpetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PerusopetukseenValmistavanOpetuksenOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
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
