import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PreIBKoulutusmoduuli2015 } from './PreIBKoulutusmoduuli2015'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { PreIBSuorituksenOsasuoritus2015 } from './PreIBSuorituksenOsasuoritus2015'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.PreIBSuoritus2015`
 */
export type PreIBSuoritus2015 = {
  $class: 'fi.oph.koski.schema.PreIBSuoritus2015'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: PreIBKoulutusmoduuli2015
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PreIBSuorituksenOsasuoritus2015>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const PreIBSuoritus2015 = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: PreIBKoulutusmoduuli2015
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PreIBSuorituksenOsasuoritus2015>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): PreIBSuoritus2015 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'preiboppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: PreIBKoulutusmoduuli2015({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'preiboppimaara',
      koodistoUri: 'suorituksentyyppi'
    })
  }),
  $class: 'fi.oph.koski.schema.PreIBSuoritus2015',
  ...o
})

export const isPreIBSuoritus2015 = (a: any): a is PreIBSuoritus2015 =>
  a?.$class === 'fi.oph.koski.schema.PreIBSuoritus2015'
