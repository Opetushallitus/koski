import { TaiteenPerusopetuksenArviointi } from './TaiteenPerusopetuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TaiteenPerusopetuksenOpintotaso } from './TaiteenPerusopetuksenOpintotaso'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from './TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'

/**
 * Taiteen perusopetuksen laajan oppimäärän perusopintojen opintotason suoritus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus`
 */
export type TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus'
  arviointi?: Array<TaiteenPerusopetuksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'taiteenperusopetuksenlaajanoppimaaranperusopinnot'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
}

export const TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus = (o: {
  arviointi?: Array<TaiteenPerusopetuksenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'taiteenperusopetuksenlaajanoppimaaranperusopinnot'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: TaiteenPerusopetuksenOpintotaso
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
}): TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'taiteenperusopetuksenlaajanoppimaaranperusopinnot',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus',
  ...o
})

export const isTaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus = (
  a: any
): a is TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus'
