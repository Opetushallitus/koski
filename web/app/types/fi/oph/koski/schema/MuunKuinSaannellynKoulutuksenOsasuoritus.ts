import { MuunKuinSäännellynKoulutuksenArviointi } from './MuunKuinSaannellynKoulutuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli } from './MuunKuinSaannellynKoulutuksenOsasuorituksenKoulutusmoduuli'
import { Vahvistus } from './Vahvistus'

/**
 * MuunKuinSäännellynKoulutuksenOsasuoritus
 *
 * @see `fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOsasuoritus`
 */
export type MuunKuinSäännellynKoulutuksenOsasuoritus = {
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOsasuoritus'
  arviointi?: Array<MuunKuinSäännellynKoulutuksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'muunkuinsaannellynkoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli
  osasuoritukset?: Array<MuunKuinSäännellynKoulutuksenOsasuoritus>
  vahvistus?: Vahvistus
}

export const MuunKuinSäännellynKoulutuksenOsasuoritus = (o: {
  arviointi?: Array<MuunKuinSäännellynKoulutuksenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'muunkuinsaannellynkoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli
  osasuoritukset?: Array<MuunKuinSäännellynKoulutuksenOsasuoritus>
  vahvistus?: Vahvistus
}): MuunKuinSäännellynKoulutuksenOsasuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muunkuinsaannellynkoulutuksenosasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOsasuoritus',
  ...o
})

export const isMuunKuinSäännellynKoulutuksenOsasuoritus = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOsasuoritus =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenOsasuoritus'
