import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Esiopetus } from './Esiopetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * EsiopetuksenSuoritus
 *
 * @see `fi.oph.koski.schema.EsiopetuksenSuoritus`
 */
export type EsiopetuksenSuoritus = {
  $class: 'fi.oph.koski.schema.EsiopetuksenSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'esiopetuksensuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: Esiopetus
  toimipiste: OrganisaatioWithOid
  osaAikainenErityisopetus?: Array<
    Koodistokoodiviite<'osaaikainenerityisopetuslukuvuodenaikana', string>
  >
  vahvistus?: HenkilövahvistusPaikkakunnalla
  suorituskieli: Koodistokoodiviite<'kieli', string>
  kielikylpykieli?: Koodistokoodiviite<'kieli', string>
}

export const EsiopetuksenSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'esiopetuksensuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: Esiopetus
  toimipiste: OrganisaatioWithOid
  osaAikainenErityisopetus?: Array<
    Koodistokoodiviite<'osaaikainenerityisopetuslukuvuodenaikana', string>
  >
  vahvistus?: HenkilövahvistusPaikkakunnalla
  suorituskieli: Koodistokoodiviite<'kieli', string>
  kielikylpykieli?: Koodistokoodiviite<'kieli', string>
}): EsiopetuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'esiopetuksensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.EsiopetuksenSuoritus',
  ...o
})

EsiopetuksenSuoritus.className =
  'fi.oph.koski.schema.EsiopetuksenSuoritus' as const

export const isEsiopetuksenSuoritus = (a: any): a is EsiopetuksenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.EsiopetuksenSuoritus'
