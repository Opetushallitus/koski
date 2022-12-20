import { SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi } from './SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TutkintokoulutukseenValmentavanKoulutuksenMuuOsa } from './TutkintokoulutukseenValmentavanKoulutuksenMuuOsa'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen tiedot
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus`
 */
export type TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus'
  arviointi?: Array<SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    | 'tutkintokoulutukseenvalmentava'
    | 'tuvaperusopetus'
    | 'tuvalukiokoulutus'
    | 'tuvaammatillinenkoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenMuuOsa
  tunnustettu?: OsaamisenTunnustaminen
}

export const TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus = (o: {
  arviointi?: Array<SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    | 'tutkintokoulutukseenvalmentava'
    | 'tuvaperusopetus'
    | 'tuvalukiokoulutus'
    | 'tuvaammatillinenkoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenMuuOsa
  tunnustettu?: OsaamisenTunnustaminen
}): TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus => ({
  $class:
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus',
  ...o
})

export const isTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus = (
  a: any
): a is TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus'
