import React from "react"
import { IconSection } from "../../components/containers/IconSection"
import { HakuIcon } from "../../components/icons/Icon"
import { Datum } from "../../components/tables/DataTable"
import { LeanTable } from "../../components/tables/LeanTable"
import { TertiaryHeading } from "../../components/typography/headings"
import { Oppija } from "../../state/oppijat"

export type OppijanHautProps = {
  oppija: Oppija
}

export const OppijanHaut = (props: OppijanHautProps) => (
  <div>
    <Haku name="Yhteishaku 2021" oppija={props.oppija} />
  </div>
)

type HakuProps = {
  name: string
  oppija: Oppija
}

const Haku = (props: HakuProps) => (
  <IconSection icon={<HakuIcon color="gray" />}>
    <TertiaryHeading>{props.name}</TertiaryHeading>
    <LeanTable
      columns={[
        {
          label: "Oppilaitos",
        },
        {
          label: "Valinta",
        },
        {
          label: "Pisteet",
        },
        {
          label: "Alin pistemäärä",
        },
      ]}
      data={[
        mapToHakuTableValue(1, "Järvenpään lukio"),
        mapToHakuTableValue(2, "Keravan lukio ja aikuislukio"),
        mapToHakuTableValue(3, "Martinlaakson lukio / Draamalinja"),
        mapToHakuTableValue(4, "Lumon lukio"),
        mapToHakuTableValue(5, "Helsingin medialukio"),
      ]}
    />
  </IconSection>
)

const mapToHakuTableValue = (n: number, name: string): Datum => ({
  key: n.toString(),
  values: [
    { value: `${n}. ${name}` },
    { value: "" },
    { value: "" },
    { value: "" },
  ],
})
