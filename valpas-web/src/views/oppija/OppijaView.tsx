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
import { InfoTooltip } from "../../components/tooltip/InfoTooltip"
import { Heading, SecondaryHeading } from "../../components/typography/headings"
import { T, t } from "../../i18n/i18n"
import { OppijaHakutilanteilla } from "../../state/oppijat"
import { OppijanHaut } from "./OppijanHaut"
import { OppijanOpiskeluhistoria } from "./OppijanOpiskeluhistoria"
import { OppijanOppivelvollisuustiedot } from "./OppijanOppivelvollisuustiedot"
import { OppijanYhteystiedot } from "./OppijanYhteystiedot"

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

      {mapSuccess(oppija, (oppijaData: OppijaHakutilanteilla) => (
        <>
          <ColumnsContainer>
            <Column size={5}>
              <Card id="oppivelvollisuustiedot">
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
                  <InfoTooltip>
                    <T id="oppija__yhteystiedot_tooltip" />
                  </InfoTooltip>
                </CardHeader>
                <CardBody>
                  <OppijanYhteystiedot oppija={oppijaData} />
                </CardBody>
              </Card>
            </Column>
          </ColumnsContainer>
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
  oppija: ApiMethodState<OppijaHakutilanteilla>
  oid: string
}) => (
  <>
    <Heading>
      {mapLoading(props.oppija, () => t("oppija__oletusotsikko"))}
      {mapSuccess(
        props.oppija,
        (oppija) =>
          `${oppija.oppija.henkilö.sukunimi} ${oppija.oppija.henkilö.etunimet} (${oppija.oppija.henkilö.hetu})`
      )}
      {mapError(props.oppija, () => t("oppija__oletusotsikko"))}
    </Heading>
    <SecondaryHeading>
      {mapLoading(props.oppija, () => t("Ladataan"))}
      {mapSuccess(props.oppija, (oppija) =>
        t("oppija__oppija_oid", { oid: oppija.oppija.henkilö.oid })
      )}
      {mapError(props.oppija, () => t("oppija__ei_löydy", { oid: props.oid }))}
    </SecondaryHeading>
  </>
)
