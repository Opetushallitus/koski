import React from "react"
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
import { useOrganisaatiotJaKäyttöoikeusroolit } from "../state/accessRights"

export const AccessRightsView = () => (
  <Page>
    <Card>
      <CardHeader>{t("title__Valpas")}</CardHeader>
      <CardBody>
        <p className={"ohjeteksti"}>
          {t("homeview_olet_onnistuneesti_kirjautunut")}
        </p>
        <OrganisaatioTable />
      </CardBody>
    </Card>
  </Page>
)

const OrganisaatioTable = () => {
  const roles = useOrganisaatiotJaKäyttöoikeusroolit()

  return (
    <Table className={"kayttooikeudet"} style={{ tableLayout: "fixed" }}>
      <TableBody>
        {roles.map((org) => (
          <Row
            key={`${org.organisaatioHierarkia.oid}-${org.kayttooikeusrooli}`}
          >
            <HeaderCell style={{ whiteSpace: "normal", width: "30%" }}>
              {getLocalized(org.organisaatioHierarkia.nimi)}
            </HeaderCell>
            <Data style={{ whiteSpace: "normal" }}>
              {t("kayttooikeusrooli_" + org.kayttooikeusrooli)}
            </Data>
          </Row>
        ))}
      </TableBody>
    </Table>
  )
}
