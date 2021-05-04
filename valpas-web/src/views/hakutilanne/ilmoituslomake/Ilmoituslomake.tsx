import React from "react"
import { Modal } from "../../../components/containers/Modal"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import { IlmoitusForm } from "./IlmoitusForm"

export type IlmoituslomakeProps = {
  oppijat: OppijaHakutilanteillaSuppeatTiedot[]
}

export const Ilmoituslomake = (props: IlmoituslomakeProps) => {
  return (
    <Modal title="Ilman opiskelupaikkaa jäävien ilmoittaminen">
      <p>Tarkista ilmoitettavien oppijoiden yhteystiedot.</p>
      {props.oppijat.map((oppija) => (
        <IlmoitusForm key={oppija.oppija.henkilö.oid} oppija={oppija} />
      ))}
    </Modal>
  )
}
