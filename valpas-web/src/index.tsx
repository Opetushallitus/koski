import React from "react"
import ReactDOM from "react-dom"
import "./style/index.less"
import { Page } from "./components/containers/Page"
import { Heading } from "./components/typography/headings"
import { Card, CardBody, CardHeader } from "./components/containers/cards"
import { DataTable } from "./components/tables/DataTable"
import { TextField } from "./components/forms/TextField"
import { SearchIcon, WarningIcon } from "./components/icons/Icon"
import { Dropdown } from "./components/forms/Dropdown"

ReactDOM.render(
  <Page>
    <Heading>Valpas-komponenttikirjasto</Heading>
    <Card>
      <CardHeader>Kortti</CardHeader>
      <CardBody>
        <DataTable
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
              values: [{ value: "Heli" }, { value: "Järvenpään yhteiskoulu" }],
            },
            {
              key: "3211",
              values: [{ value: "Aatu" }, { value: "Järvenpään yhteiskoulu" }],
            },
            {
              key: "3",
              values: [
                { value: "Osmo" },
                { value: "Esimerkkilän yhteiskoulu", icon: <WarningIcon /> },
              ],
            },
          ]}
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
      </CardBody>
    </Card>
  </Page>,
  document.getElementById("app")
)
