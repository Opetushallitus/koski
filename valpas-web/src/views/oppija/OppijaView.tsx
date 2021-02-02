import React from "react"
import { RouteComponentProps } from "react-router-dom"
import { fetchOppija } from "../../api/api"
import { isSuccessful, useApiWithParams } from "../../api/apiHooks"
import { ButtonLabel } from "../../components/buttons/ButtonLabel"
import { FlatLink } from "../../components/buttons/FlatButton"
import { Page } from "../../components/containers/Page"
import { BackIcon } from "../../components/icons/Icon"

export type OppijaViewProps = RouteComponentProps<{
  oid: string
}>

export const OppijaView = (props: OppijaViewProps) => {
  const oppija = useApiWithParams(fetchOppija, [props.match.params.oid])

  return (
    <Page id="oppija">
      <BackNav />
      {isSuccessful(oppija) ? oppija.data.nimi : null}
    </Page>
  )
}

const BackNav = () => (
  <div>
    <FlatLink to="/oppijat">
      <BackIcon />
      <ButtonLabel>Muut oppijat</ButtonLabel>
    </FlatLink>
  </div>
)
