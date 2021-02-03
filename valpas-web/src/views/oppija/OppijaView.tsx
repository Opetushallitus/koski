import { pipe } from "fp-ts/lib/pipeable"
import * as O from "fp-ts/Option"
import React from "react"
import { RouteComponentProps } from "react-router-dom"
import { fetchOppija } from "../../api/api"
import { ApiMethodState, useApiWithParams } from "../../api/apiHooks"
import { mapApiResult } from "../../api/ApiResult"
import { ButtonLabel } from "../../components/buttons/ButtonLabel"
import { FlatLink } from "../../components/buttons/FlatButton"
import { Page } from "../../components/containers/Page"
import { BackIcon } from "../../components/icons/Icon"
import { Heading, SecondaryHeading } from "../../components/typography/headings"
import { t } from "../../i18n/i18n"
import { Oppija } from "../../state/oppijat"

export type OppijaViewProps = RouteComponentProps<{
  oid: string
}>

export const OppijaView = (props: OppijaViewProps) => {
  const queryOid = props.match.params.oid
  const oppija = useApiWithParams(fetchOppija, [queryOid])

  return (
    <Page id="oppija">
      <BackNav />
      <OppijaHeadings oppija={oppija} oid={queryOid} />
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

const OppijaHeadings = (props: {
  oppija: ApiMethodState<Oppija>
  oid: string
}) =>
  pipe(
    mapApiResult(props.oppija, {
      loading: () => [t("oppija__oletusotsikko"), t("Ladataan")],
      successful: (oppija) => [
        `${oppija.nimi} (${oppija.hetu})`,
        t("oppija__oppija_oid", { oid: oppija.oid }),
      ],
      error: () => [
        t("oppija__oletusotsikko"),
        t("oppija__ei_löydy", { oid: props.oid }),
      ],
    }),
    O.map(([main, sub]) => (
      <>
        <Heading>{main}</Heading>
        <SecondaryHeading>{sub}</SecondaryHeading>
      </>
    )),
    O.toNullable
  )
