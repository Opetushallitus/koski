import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuuKorkeakoulunOpinto } from './MuuKorkeakoulunOpinto'
import { Oppilaitos } from './Oppilaitos'
import { KorkeakoulunOpintojaksonSuoritus } from './KorkeakoulunOpintojaksonSuoritus'
import { Päivämäärävahvistus } from './Paivamaaravahvistus'

/**
 * Muut kuin tutkintoon johtavat opiskeluoikeudet, joilla ei ole koulutuskoodia
 *
 * @see `fi.oph.koski.schema.MuuKorkeakoulunSuoritus`
 */
export type MuuKorkeakoulunSuoritus = {
  $class: 'fi.oph.koski.schema.MuuKorkeakoulunSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'muukorkeakoulunsuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuKorkeakoulunOpinto
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}

export const MuuKorkeakoulunSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'muukorkeakoulunsuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuKorkeakoulunOpinto
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}): MuuKorkeakoulunSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muukorkeakoulunsuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MuuKorkeakoulunSuoritus',
  ...o
})

export const isMuuKorkeakoulunSuoritus = (
  a: any
): a is MuuKorkeakoulunSuoritus =>
  a?.$class === 'fi.oph.koski.schema.MuuKorkeakoulunSuoritus'
