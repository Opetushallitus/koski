import { Oppilaitos } from './Oppilaitos'

/**
 * Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden tiedot. Nämä tiedot kertovat, että kyseessä on ns. ulkopuolisen sopimuskumppanin suoritustieto, joka liittyy päävastuullisen koulutuksen järjestäjän luomaan opiskeluoikeuteen. Ks. tarkemmin https://wiki.eduuni.fi/display/OPHPALV/4.+Ammatillisten+opiskeluoikeuksien+linkitys
 *
 * @see `fi.oph.koski.schema.SisältäväOpiskeluoikeus`
 */
export type SisältäväOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.SisältäväOpiskeluoikeus'
  oppilaitos: Oppilaitos
  oid: string
}

export const SisältäväOpiskeluoikeus = (o: {
  oppilaitos: Oppilaitos
  oid: string
}): SisältäväOpiskeluoikeus => ({
  $class: 'fi.oph.koski.schema.SisältäväOpiskeluoikeus',
  ...o
})

export const isSisältäväOpiskeluoikeus = (
  a: any
): a is SisältäväOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.SisältäväOpiskeluoikeus'
