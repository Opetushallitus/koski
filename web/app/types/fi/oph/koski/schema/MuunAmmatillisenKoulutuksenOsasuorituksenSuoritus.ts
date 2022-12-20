import { MuunAmmatillisenKoulutuksenArviointi } from './MuunAmmatillisenKoulutuksenArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunAmmatillisenKoulutuksenOsasuoritus } from './MuunAmmatillisenKoulutuksenOsasuoritus'

/**
 * MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus
 *
 * @see `fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus`
 */
export type MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = {
  $class: 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus'
  arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'muunammatillisenkoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuunAmmatillisenKoulutuksenOsasuoritus
  osasuoritukset?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus>
}

export const MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = (o: {
  arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
  näyttö?: Näyttö
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'muunammatillisenkoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuunAmmatillisenKoulutuksenOsasuoritus
  osasuoritukset?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus>
}): MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muunammatillisenkoulutuksenosasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus',
  ...o
})

export const isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = (
  a: any
): a is MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus'
