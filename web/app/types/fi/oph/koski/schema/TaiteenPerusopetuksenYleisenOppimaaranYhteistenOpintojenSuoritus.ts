import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TaiteenPerusopetuksenOpintotaso } from './TaiteenPerusopetuksenOpintotaso'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from './TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'

/**
 * Taiteen perusopetuksen yleisen oppimäärän yhteisten opintojen opintotason suoritus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus`
 */
export type TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
}

export const TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus =
  (o: {
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: TaiteenPerusopetuksenOpintotaso
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus>
    vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
  }): TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus',
    ...o
  })

TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus.className =
  'fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus' as const

export const isTaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus =
  (
    a: any
  ): a is TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus'
