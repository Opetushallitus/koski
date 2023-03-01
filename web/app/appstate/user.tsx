import React, { useContext, useMemo } from 'react'
import { UserWithAccessRights } from '../types/fi/oph/koski/koskiuser/UserWithAccessRights'

export type UserContext =
  | (UserWithAccessRights & { isKansalainen: boolean })
  | null

const UserContext = React.createContext<UserContext>(null)

export type UserProviderProps = React.PropsWithChildren<{
  user: UserWithAccessRights
  isKansalainen: boolean
}>

export const UserProvider: React.FC<UserProviderProps> = (props) => {
  const value = useMemo(
    () => ({ ...props.user, isKansalainen: props.isKansalainen }),
    [props.isKansalainen, props.user]
  )
  return (
    <UserContext.Provider value={value}>{props.children}</UserContext.Provider>
  )
}

/**
 * Palauttaa kirjautuneen käyttäjän tiedot.
 * @returns kirjautunut käyttäjä
 */
export const useUser = () => useContext(UserContext)

export const useVirkailijaUser = (): UserWithAccessRights | null => {
  const user = useUser()
  return user?.isKansalainen === false ? user : null
}

export const useKansalainenUser = (): UserWithAccessRights | null => {
  const user = useUser()
  return user?.isKansalainen === true ? user : null
}
