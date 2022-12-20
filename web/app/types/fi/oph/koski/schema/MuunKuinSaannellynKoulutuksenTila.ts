import { MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso } from './MuunKuinSaannellynKoulutuksenOpiskeluoikeudenJakso'

/**
 * MuunKuinSäännellynKoulutuksenTila
 *
 * @see `fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenTila`
 */
export type MuunKuinSäännellynKoulutuksenTila = {
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenTila'
  opiskeluoikeusjaksot: Array<MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso>
}

export const MuunKuinSäännellynKoulutuksenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso>
  } = {}
): MuunKuinSäännellynKoulutuksenTila => ({
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

export const isMuunKuinSäännellynKoulutuksenTila = (
  a: any
): a is MuunKuinSäännellynKoulutuksenTila =>
  a?.$class === 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenTila'
