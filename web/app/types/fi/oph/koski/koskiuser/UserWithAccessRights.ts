/**
 * UserWithAccessRights
 *
 * @see `fi.oph.koski.koskiuser.UserWithAccessRights`
 */
export type UserWithAccessRights = {
  $class: 'fi.oph.koski.koskiuser.UserWithAccessRights'
  name: string
  hasAnyInvalidateAccess: boolean
  oid: string
  varhaiskasvatuksenJärjestäjäKoulutustoimijat: Array<string>
  hasKelaUiAccess: boolean
  hasOneKoulutustoimijaWriteAccess: boolean
  hasAnyReadAccess: boolean
  hasGlobalReadAccess: boolean
  hasWriteAccess: boolean
  hasHenkiloUiWriteAccess: boolean
  hasLocalizationWriteAccess: boolean
  isViranomainen: boolean
  hasRaportitAccess: boolean
  hasLähdejärjestelmäkytkennänPurkaminenAccess: boolean
}

export const UserWithAccessRights = (o: {
  name: string
  hasAnyInvalidateAccess: boolean
  oid: string
  varhaiskasvatuksenJärjestäjäKoulutustoimijat?: Array<string>
  hasKelaUiAccess: boolean
  hasOneKoulutustoimijaWriteAccess: boolean
  hasAnyReadAccess: boolean
  hasGlobalReadAccess: boolean
  hasWriteAccess: boolean
  hasHenkiloUiWriteAccess: boolean
  hasLocalizationWriteAccess: boolean
  isViranomainen: boolean
  hasRaportitAccess: boolean
  hasLähdejärjestelmäkytkennänPurkaminenAccess: boolean
}): UserWithAccessRights => ({
  $class: 'fi.oph.koski.koskiuser.UserWithAccessRights',
  varhaiskasvatuksenJärjestäjäKoulutustoimijat: [],
  ...o
})

UserWithAccessRights.className =
  'fi.oph.koski.koskiuser.UserWithAccessRights' as const

export const isUserWithAccessRights = (a: any): a is UserWithAccessRights =>
  a?.$class === 'fi.oph.koski.koskiuser.UserWithAccessRights'
