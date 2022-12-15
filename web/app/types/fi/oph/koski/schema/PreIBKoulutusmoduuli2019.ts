import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Pre-IB-koulutuksen tunnistetiedot 2019
 *
 * @see `fi.oph.koski.schema.PreIBKoulutusmoduuli2019`
 */
export type PreIBKoulutusmoduuli2019 = {
  $class: 'fi.oph.koski.schema.PreIBKoulutusmoduuli2019'
  tunniste: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara2019'>
}

export const PreIBKoulutusmoduuli2019 = (
  o: {
    tunniste?: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara2019'>
  } = {}
): PreIBKoulutusmoduuli2019 => ({
  $class: 'fi.oph.koski.schema.PreIBKoulutusmoduuli2019',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'preiboppimaara2019',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isPreIBKoulutusmoduuli2019 = (
  a: any
): a is PreIBKoulutusmoduuli2019 => a?.$class === 'PreIBKoulutusmoduuli2019'
