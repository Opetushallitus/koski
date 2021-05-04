import React from "react"
import { Modal } from "../../../components/containers/Modal"
import { KoodistoKoodiviite } from "../../../state/apitypes/koodistot"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import { IlmoitusForm } from "./IlmoitusForm"

export type IlmoituslomakeProps = {
  oppijat: OppijaHakutilanteillaSuppeatTiedot[]
}

export const Ilmoituslomake = (props: IlmoituslomakeProps) => {
  return (
    <Modal title="Ilman opiskelupaikkaa jäävien ilmoittaminen">
      <p>Tarkista ilmoitettavien oppijoiden yhteystiedot.</p>
      {props.oppijat.map((oppija, index) => (
        <IlmoitusForm
          key={oppija.oppija.henkilö.oid}
          formIndex={index}
          numberOfForms={props.oppijat.length}
          oppija={oppija}
          yhteydenottokielet={mockYhteydenottokielet}
        />
      ))}
    </Modal>
  )
}

const mockKoodisto = (
  uri: string,
  arvot: Record<string, string>
): Array<KoodistoKoodiviite> =>
  Object.entries(arvot).map(([arvo, nimi]) => ({
    koodistoUri: uri,
    koodiarvo: arvo,
    nimi: {
      fi: nimi,
    },
  }))

const mockYhteydenottokielet = mockKoodisto("kielivalikoima", {
  FI: "Suomi",
  SV: "Ruotsi",
  EN: "Englanti",
  AR: "Arabia",
})
