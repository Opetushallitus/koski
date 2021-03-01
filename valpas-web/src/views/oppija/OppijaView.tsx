import React from "react"
import { RouteComponentProps } from "react-router-dom"
import { fetchOppija, fetchOppijaCache } from "../../api/api"
import { ApiMethodState, useApiWithParams } from "../../api/apiHooks"
import { mapError, mapLoading, mapSuccess } from "../../api/apiUtils"
import { ButtonLabel } from "../../components/buttons/ButtonLabel"
import { FlatLink } from "../../components/buttons/FlatButton"
import { Card, CardBody, CardHeader } from "../../components/containers/cards"
import { Column, ColumnsContainer } from "../../components/containers/Columns"
import { Page } from "../../components/containers/Page"
import { BackIcon } from "../../components/icons/Icon"
import { Heading, SecondaryHeading } from "../../components/typography/headings"
import { T, t } from "../../i18n/i18n"
import { Oppija } from "../../state/oppijat"
import { OppijanHaut } from "./OppijanHaut"
import { OppijanOpiskeluhistoria } from "./OppijanOpiskeluhistoria"

export type OppijaViewProps = RouteComponentProps<{
  oid: string
}>

export const OppijaView = (props: OppijaViewProps) => {
  const queryOid = props.match.params.oid
  const oppija = useApiWithParams(fetchOppija, [queryOid], fetchOppijaCache)

  return (
    <Page id="oppija">
      <BackNav />
      <OppijaHeadings oppija={oppija} oid={queryOid} />

      {mapSuccess(oppija, (oppijaData: Oppija) => (
        <>
          {/* <ColumnsContainer>
            <Column size={5}>
              <Card>
                <CardHeader>
                  <T id="oppija__oppivelvollisuustiedot_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanOppivelvollisuustiedot oppija={oppijaData} />
                </CardBody>
              </Card>
            </Column>
            <Column size={7}>
              <Card>
                <CardHeader>
                  <T id="oppija__yhteystiedot_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanYhteystiedot oppija={oppijaData} />
                </CardBody>
              </Card>
            </Column>
          </ColumnsContainer> */}
          <ColumnsContainer>
            <Column size={5}>
              <Card id="opiskeluhistoria">
                <CardHeader>
                  <T id="oppija__opiskeluhistoria_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanOpiskeluhistoria oppija={oppijaData} />
                </CardBody>
              </Card>
            </Column>
            <Column size={7}>
              <Card id="haut">
                <CardHeader>
                  <T id="oppija__haut_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanHaut oppija={oppijaData} />
                </CardBody>
              </Card>
            </Column>
          </ColumnsContainer>
        </>
      ))}
    </Page>
  )
}

const BackNav = () => (
  <div>
    <FlatLink to="/oppijat">
      <BackIcon />
      <ButtonLabel>
        <T id="oppija__muut_oppijat" />
      </ButtonLabel>
    </FlatLink>
  </div>
)

const OppijaHeadings = (props: {
  oppija: ApiMethodState<Oppija>
  oid: string
}) => (
  <>
    <Heading>
      {mapLoading(props.oppija, () => t("oppija__oletusotsikko"))}
      {mapSuccess(
        props.oppija,
        (oppija) =>
          `${oppija.henkilö.sukunimi} ${oppija.henkilö.etunimet} (${oppija.henkilö.hetu})`
      )}
      {mapError(props.oppija, () => t("oppija__oletusotsikko"))}
    </Heading>
    <SecondaryHeading>
      {mapLoading(props.oppija, () => t("Ladataan"))}
      {mapSuccess(props.oppija, (oppija) =>
        t("oppija__oppija_oid", { oid: oppija.henkilö.oid })
      )}
      {mapError(props.oppija, () => t("oppija__ei_löydy", { oid: props.oid }))}
    </SecondaryHeading>
  </>
)
