import React, { useState } from "react"
import { ModalButtonGroup } from "../components/buttons/ModalButtonGroup"
import { RaisedButton } from "../components/buttons/RaisedButton"
import { Card, CardBody, CardHeader } from "../components/containers/cards"
import { Column, ColumnsContainer } from "../components/containers/Columns"
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

      <Card>
        <CardHeader>Sarakkeet</CardHeader>
        <ColumnsContainer>
          <Column size={4} style={{ background: "#ffc" }}>
            <p style={{ background: "#ff4" }}>X</p>
          </Column>
          <Column size={8} style={{ background: "#cff" }}>
            <p style={{ background: "#4ff" }}>
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam sit
              amet leo ligula. Duis finibus aliquet ipsum, a ultricies quam
              ornare id. Etiam metus enim, efficitur at tellus non, faucibus
              volutpat libero. Pellentesque at magna quis tellus consequat
              porttitor a eu elit. Nam egestas odio eget efficitur rhoncus.
              Fusce vitae arcu ex. Nulla egestas quam sit amet turpis facilisis
              facilisis.
            </p>
            <p style={{ background: "#4ff" }}>
              Aliquam tincidunt ut leo eget porttitor. Etiam dictum pretium
              ligula eu aliquam. Vestibulum auctor est nec faucibus porttitor.
              Donec metus mauris, aliquet ut ligula at, rhoncus tempus mauris.
              In interdum ex eu turpis ultricies tincidunt. Integer laoreet odio
              at ipsum vulputate, ac vulputate justo vulputate. Nunc vel egestas
              risus.
            </p>
          </Column>
        </ColumnsContainer>
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
