import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukioonValmistavaKoulutus } from './LukioonValmistavaKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { LukioonValmistavanKoulutuksenOsasuoritus } from './LukioonValmistavanKoulutuksenOsasuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Lukioon valmistavan koulutus (LUVA) suoritus
 *
 * @see `fi.oph.koski.schema.LukioonValmistavanKoulutuksenSuoritus`
 */
export type LukioonValmistavanKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.LukioonValmistavanKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luva'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: LukioonValmistavaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukioonValmistavanKoulutuksenOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const LukioonValmistavanKoulutuksenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'luva'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: LukioonValmistavaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukioonValmistavanKoulutuksenOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): LukioonValmistavanKoulutuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'luva',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: LukioonValmistavaKoulutus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '999906',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.LukioonValmistavanKoulutuksenSuoritus',
  ...o
})

export const isLukioonValmistavanKoulutuksenSuoritus = (
  a: any
): a is LukioonValmistavanKoulutuksenSuoritus =>
  a?.$class === 'LukioonValmistavanKoulutuksenSuoritus'
