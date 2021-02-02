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
import { getLocalized } from "../i18n/i18n"
import { Organisaatio, User } from "../state/types"

export type HomeViewProps = {
  user: User
  organisaatiot: Organisaatio[]
}

export const HomeView = (props: HomeViewProps) => (
  <Page>
    <Card>
      <CardHeader>Valpas</CardHeader>
      <CardBody>
        <p>
          Olet onnistuneesti kirjautunut Valpas-järjestelmään seuraavalla
          tunnuksella
        </p>
        <Table>
          <TableBody>
            <Row>
              <HeaderCell>Käyttäjätunnus</HeaderCell>
              <Data>{props.user.username}</Data>
            </Row>
            <Row>
              <HeaderCell>Nimi</HeaderCell>
              <Data>{props.user.name}</Data>
            </Row>
            <Row>
              <HeaderCell>OID</HeaderCell>
              <Data>{props.user.oid}</Data>
            </Row>
            <Row>
              <HeaderCell>Organisaatiot</HeaderCell>
              <Data>
                <OrganisaatiotList organisaatiot={props.organisaatiot} />
              </Data>
            </Row>
          </TableBody>
        </Table>
      </CardBody>
    </Card>
  </Page>
)

type OrganisaatiotListProps = {
  organisaatiot: Organisaatio[]
  style?: React.CSSProperties
}

const OrganisaatiotList = (props: OrganisaatiotListProps) => (
  <ul style={props.style}>
    {props.organisaatiot.map((org) => (
      <li key={org.oid}>
        {getLocalized(org.nimi)}
        {org.children.length > 0 ? (
          <OrganisaatiotList
            organisaatiot={org.children}
            style={{ marginLeft: 30 }}
          />
        ) : null}
      </li>
    ))}
  </ul>
)
