import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { isTerminaalitila } from '../../util/opiskeluoikeus'

export type RequiresLahdejarjestelmakytkennanPurkaminenAccessProps =
  React.PropsWithChildren<{
    opiskeluoikeus: Opiskeluoikeus
  }>

export const RequiresLahdejarjestelmakytkennanPurkaminenAccess: React.FC<
  RequiresLahdejarjestelmakytkennanPurkaminenAccessProps
> = (props) =>
  useVirkailijaUser()?.hasLähdejärjestelmäkytkennänPurkaminenAccess &&
  isPurettavaOpiskeluoikeus(props.opiskeluoikeus) ? (
    <>{props.children}</>
  ) : null

export const isPurettavaOpiskeluoikeus = (
  opiskeluoikeus: Opiskeluoikeus
): boolean =>
  opiskeluoikeus.lähdejärjestelmänId !== undefined &&
  opiskeluoikeus.tila.opiskeluoikeusjaksot.find((j) =>
    isTerminaalitila(j.tila)
  ) !== undefined
