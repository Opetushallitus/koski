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
import { User } from "../state/auth"

export type HomeViewProps = {
  user: User
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
          </TableBody>
        </Table>
      </CardBody>
    </Card>
  </Page>
)
