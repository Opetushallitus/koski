import { MuunAmmatillisenKoulutuksenArviointi } from './MuunAmmatillisenKoulutuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunAmmatillisenKoulutuksenOsasuoritus } from './MuunAmmatillisenKoulutuksenOsasuoritus'
import { Näyttö } from './Naytto'

/**
 * MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus
 *
 * @see `fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus`
 */
export type MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = {
  $class: 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus'
  arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuunAmmatillisenKoulutuksenOsasuoritus
  osasuoritukset?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'muunammatillisenkoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = (o: {
  arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuunAmmatillisenKoulutuksenOsasuoritus
  osasuoritukset?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus>
  näyttö?: Näyttö
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'muunammatillisenkoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}): MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus => ({
  $class:
    'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muunammatillisenkoulutuksenosasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus.className =
  'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus' as const

export const isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = (
  a: any
): a is MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus'
