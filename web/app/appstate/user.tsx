import React, { useContext } from 'react'
import { UserWithAccessRights } from '../types/fi/oph/koski/koskiuser/UserWithAccessRights'

export type UserContext = UserWithAccessRights | null

const UserContext = React.createContext<UserContext>(null)

export type UserProviderProps = React.PropsWithChildren<{
  user: UserWithAccessRights
}>

export const UserProvider: React.FC<UserProviderProps> = (props) => (
  <UserContext.Provider value={props.user}>
    {props.children}
  </UserContext.Provider>
)

export const useUser = () => useContext(UserContext)