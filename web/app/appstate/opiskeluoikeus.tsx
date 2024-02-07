import React, { Dispatch, SetStateAction, useMemo, useState } from 'react'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { Koulutustoimija } from '../types/fi/oph/koski/schema/Koulutustoimija'

export type OpiskeluoikeusContextValue = {
  readonly organisaatio: Oppilaitos | Koulutustoimija | undefined
  readonly setOrganisaatio: Dispatch<
    SetStateAction<Oppilaitos | Koulutustoimija | undefined>
  >
}

export const OpiskeluoikeusContext =
  React.createContext<OpiskeluoikeusContextValue>({
    organisaatio: undefined,
    setOrganisaatio: () => {}
  })

export type OpiskeluoikeusProviderProps = React.PropsWithChildren<{}>

export const OpiskeluoikeusProvider = (props: OpiskeluoikeusProviderProps) => {
  const [organisaatio, setOrganisaatio] = useState<
    Oppilaitos | Koulutustoimija | undefined
  >()

  const contextValue: OpiskeluoikeusContextValue = useMemo(
    () => ({
      organisaatio,
      setOrganisaatio
    }),
    [organisaatio, setOrganisaatio]
  )

  return (
    <OpiskeluoikeusContext.Provider value={contextValue}>
      {props.children}
    </OpiskeluoikeusContext.Provider>
  )
}
