import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { DiplomaLuokkaAste } from './DiplomaLuokkaAste'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { DiplomaIBOppiaineenSuoritus } from './DiplomaIBOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.DiplomaVuosiluokanSuoritus`
 */
export type DiplomaVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.DiplomaVuosiluokanSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschooldiplomavuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  koulutusmoduuli: DiplomaLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<DiplomaIBOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const DiplomaVuosiluokanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschooldiplomavuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  koulutusmoduuli: DiplomaLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<DiplomaIBOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): DiplomaVuosiluokanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschooldiplomavuosiluokka',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.DiplomaVuosiluokanSuoritus',
  ...o
})

DiplomaVuosiluokanSuoritus.className =
  'fi.oph.koski.schema.DiplomaVuosiluokanSuoritus' as const

export const isDiplomaVuosiluokanSuoritus = (
  a: any
): a is DiplomaVuosiluokanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.DiplomaVuosiluokanSuoritus'
