import React from "react"
import { Redirect } from "react-router-dom"
import { Card, CardBody, CardHeader } from "../components/containers/cards"
import { Page } from "../components/containers/Page"
import {
  Data,
  HeaderCell,
  Row,
  Table,
  TableBody,
} from "../components/tables/Table"
import { getLocalized, t } from "../i18n/i18n"
import {
  hakeutumisenValvontaAllowed,
  useKäyttöoikeusroolit,
  useOrganisaatiotJaKäyttöoikeusroolit,
} from "../state/accessRights"
import { OrganisaatioJaKayttooikeusrooli, User } from "../state/common"

export type HomeViewProps = {
  user: User
  redirectJosHakeutumisoikeuksiaTo: string
}

export const HomeView = (props: HomeViewProps) => {
  const roles = useKäyttöoikeusroolit()

  return hakeutumisenValvontaAllowed(roles) ? (
    <Redirect to={props.redirectJosHakeutumisoikeuksiaTo} />
  ) : (
    <AccessRightsPage />
  )
}

type OrganisaatiotListProps = {
  organisaatiotJaKayttooikeusroolit: OrganisaatioJaKayttooikeusrooli[]
}

const OrganisaatiotRows = (props: OrganisaatiotListProps) => (
  <>
    {props.organisaatiotJaKayttooikeusroolit.map((org) => (
      <Row key={`${org.organisaatioHierarkia.oid}-${org.kayttooikeusrooli}`}>
        <HeaderCell style={{ whiteSpace: "normal", width: "30%" }}>
          {getLocalized(org.organisaatioHierarkia.nimi)}
        </HeaderCell>
        <Data style={{ whiteSpace: "normal" }}>
          {t("kayttooikeusrooli_" + org.kayttooikeusrooli)}
        </Data>
      </Row>
    ))}
  </>
)

const AccessRightsPage = () => {
  const roles = useOrganisaatiotJaKäyttöoikeusroolit()

  return (
    <Page>
      <Card>
        <CardHeader>{t("title__Valpas")}</CardHeader>
        <CardBody>
          <p className={"ohjeteksti"}>
            {t("homeview_olet_onnistuneesti_kirjautunut")}
          </p>
          <Table className={"kayttooikeudet"} style={{ tableLayout: "fixed" }}>
            <TableBody>
              <OrganisaatiotRows organisaatiotJaKayttooikeusroolit={roles} />
            </TableBody>
          </Table>
        </CardBody>
      </Card>
    </Page>
  )
}
