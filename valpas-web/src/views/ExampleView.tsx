import React, { useState } from "react"
import { ModalButtonGroup } from "../components/buttons/ModalButtonGroup"
import { RaisedButton } from "../components/buttons/RaisedButton"
import { Card, CardBody, CardHeader } from "../components/containers/cards"
import { Modal } from "../components/containers/Modal"
import { Page } from "../components/containers/Page"
import { Dropdown } from "../components/forms/Dropdown"
import { TextField } from "../components/forms/TextField"
import { SearchIcon, WarningIcon } from "../components/icons/Icon"
import { SelectableDataTable } from "../components/tables/SelectableDataTable"
import { Heading } from "../components/typography/headings"

export const ExampleView = () => {
  const [modalVisible, setModalVisible] = useState(false)

  return (
    <Page>
      <Heading>Valpas-komponenttikirjasto</Heading>
      <Card>
        <CardHeader>Kortti</CardHeader>
        <CardBody>
          <SelectableDataTable
            columns={[
              { label: "Nimi", filter: "freetext" },
              { label: "Oppilaitos", filter: "dropdown" },
            ]}
            data={[
              {
                key: "123",
                values: [
                  { value: "Heikki", icon: <SearchIcon /> },
                  { value: "Järvenpään yhteiskoulu" },
                ],
              },
              {
                key: "321",
                values: [
                  { value: "Heli" },
                  { value: "Järvenpään yhteiskoulu" },
                ],
              },
              {
                key: "3211",
                values: [
                  { value: "Aatu" },
                  { value: "Järvenpään yhteiskoulu" },
                ],
              },
              {
                key: "3",
                values: [
                  { value: "Osmo" },
                  { value: "Esimerkkilän yhteiskoulu", icon: <WarningIcon /> },
                ],
              },
            ]}
            onChange={(selected) => console.log("Selected", selected)}
          />
        </CardBody>
      </Card>
      <Card>
        <CardHeader>Lomakkeet</CardHeader>
        <CardBody>
          <TextField
            label="Otsikko"
            value="Arvo"
            onChange={(value) => console.log("Value", value)}
          />
          <TextField
            label="Otsikko"
            icon={<SearchIcon />}
            value="fhdkfjhdkfjhdfjkhjkfhdsjkfhjksdhfjkshdjkfhsdkufheurhfglkudghdfukhgdfukhgdfulkghdflukgfudngldhglkudshglukdh"
            placeholder="Placeholder"
            onChange={(value) => console.log("Value", value)}
          />
          <Dropdown
            label="Pudotusvalikko"
            options={[
              {
                value: 1,
                display: "Yksi",
              },
              {
                value: 2,
                display: "Kaksi",
              },
              {
                value: 3,
                display: "Kolme",
              },
            ]}
            value={1}
            onChange={(value) => console.log("Value:", value)}
          />
          <RaisedButton onClick={() => setModalVisible(true)}>
            Näytä popup
          </RaisedButton>
        </CardBody>
      </Card>
      {modalVisible && (
        <Modal title="Modaalipopuppi" onClose={() => setModalVisible(false)}>
          <p>Tämmöistä se nyt vaan on</p>
          <ModalButtonGroup>
            <RaisedButton
              hierarchy="secondary"
              onClick={() => setModalVisible(false)}
            >
              Peruuta
            </RaisedButton>
            <RaisedButton onClick={() => setModalVisible(false)}>
              Valmista
            </RaisedButton>
          </ModalButtonGroup>
        </Modal>
      )}
    </Page>
  )
}
