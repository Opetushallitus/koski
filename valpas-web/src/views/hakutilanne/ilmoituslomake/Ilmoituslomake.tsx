import React from "react"
import { Modal } from "../../../components/containers/Modal"
import { KoodistoKoodiviite } from "../../../state/apitypes/koodistot"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import { IlmoitusForm, PrefilledIlmoitusFormValues } from "./IlmoitusForm"

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
          kunnat={mockAsuinkunnat}
          maat={mockMaat}
          kielet={mockYhteydenottokielet}
          prefilledValues={[
            mockPrefilledYhteishakuValues,
            mockPrefilledDvvValues,
          ]}
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

const mockAsuinkunnat = mockKoodisto("kunta", {
  "091": "Helsinki",
  "179": "Jyväskylä",
  "272": "Kokkola",
})

const mockMaat = mockKoodisto("maatjavaltiot2", {
  "004": "Afganistan",
  "246": "Suomi",
  "752": "Ruotsi",
  "840": "Yhdysvallat (USA)",
})

const mockYhteydenottokielet = mockKoodisto("kielivalikoima", {
  FI: "suomi",
  SV: "ruotsi",
  EN: "englanti",
  AR: "arabia",
})

const mockPrefilledDvvValues: PrefilledIlmoitusFormValues = {
  label: "DVV yhteystiedot",
  values: {
    asuinkunta: "272",
    postinumero: "67100",
    postitoimipaikka: "Kokkola",
    katuosoite: "Esimerkkikatu 123",
  },
}

const mockPrefilledYhteishakuValues: PrefilledIlmoitusFormValues = {
  label: "Yhteishaku kevät 2021",
  values: {
    asuinkunta: "179",
    postinumero: "12345",
    postitoimipaikka: "Jyväskylä",
    katuosoite: "Jytäraitti 83",
  },
}
