import React from "react"
import { Redirect } from "react-router-dom"
import {
  onHakeutumisVelvollisuudenValvonnanOikeuksia,
  OrganisaatioJaKayttooikeusrooli,
} from "../state/common"

export type WithRequiresHakeutumisenValvontaProps = {
  kayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
  redirectJosKäyttäjälläEiOleOikeuksiaTo: string
}

export const withRequiresHakeutumisenValvonta = <P extends object>(
  Component: React.ComponentType<P>
): React.FC<P & WithRequiresHakeutumisenValvontaProps> => (
  props: WithRequiresHakeutumisenValvontaProps
) =>
  onHakeutumisVelvollisuudenValvonnanOikeuksia(props.kayttooikeusroolit) ? (
    <Component {...(props as P)} />
  ) : (
    <Redirect to={props.redirectJosKäyttäjälläEiOleOikeuksiaTo} />
  )
