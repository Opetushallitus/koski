import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * MuuPerusopetuksenLisäopetuksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.schema.MuuPerusopetuksenLisäopetuksenKoulutusmoduuli`
 */
export type MuuPerusopetuksenLisäopetuksenKoulutusmoduuli = {
  $class: 'fi.oph.koski.schema.MuuPerusopetuksenLisäopetuksenKoulutusmoduuli'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusVuosiviikkotunneissa
}

export const MuuPerusopetuksenLisäopetuksenKoulutusmoduuli = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusVuosiviikkotunneissa
}): MuuPerusopetuksenLisäopetuksenKoulutusmoduuli => ({
  $class: 'fi.oph.koski.schema.MuuPerusopetuksenLisäopetuksenKoulutusmoduuli',
  ...o
})

export const isMuuPerusopetuksenLisäopetuksenKoulutusmoduuli = (
  a: any
): a is MuuPerusopetuksenLisäopetuksenKoulutusmoduuli =>
  a?.$class ===
  'fi.oph.koski.schema.MuuPerusopetuksenLisäopetuksenKoulutusmoduuli'
