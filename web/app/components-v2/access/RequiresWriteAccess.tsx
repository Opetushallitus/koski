import React, { useContext } from 'react'
import { useVirkailijaUser } from '../../appstate/user'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'

export type RequiresWriteAccessProps = React.PropsWithChildren<{
  opiskeluoikeus: Opiskeluoikeus
}>

// Onko käyttäjällä muokkausoikeus juuri tähän opiskeluoikeuteen. Käyttäjä voi
// nähdä opiskeluoikeuden, jota hän ei saa muokata, vaikka hänellä on
// muokkausoikeus toisessa organisaatiossa (esim. toisen oppilaitoksen
// sisältyvä opiskeluoikeus). Tieto tulee taustajärjestelmän editorimallista.
export const OpiskeluoikeusEditableContext = React.createContext(true)

export const useWriteAccess = (opiskeluoikeus: Opiskeluoikeus): boolean => {
  const user = useVirkailijaUser()
  const editable = useContext(OpiskeluoikeusEditableContext)
  return (
    Boolean(user?.hasWriteAccess) &&
    editable &&
    opiskeluoikeus.lähdejärjestelmänId === undefined
  )
}

export const RequiresWriteAccess: React.FC<RequiresWriteAccessProps> = (
  props
) => (useWriteAccess(props.opiskeluoikeus) ? <>{props.children}</> : null)
