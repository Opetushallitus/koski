import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'

/**
 * Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa`
 */
export type PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa = {
  $class: 'fi.oph.koski.schema.PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
}

export const PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
}): PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa => ({
  $class:
    'fi.oph.koski.schema.PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa',
  ...o
})

PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa.className =
  'fi.oph.koski.schema.PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa' as const

export const isPaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa = (
  a: any
): a is PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa =>
  a?.$class ===
  'fi.oph.koski.schema.PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa'
