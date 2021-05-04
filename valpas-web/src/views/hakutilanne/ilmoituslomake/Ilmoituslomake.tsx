import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"

export type IlmoituslomakeProps = {
  oppijat: OppijaHakutilanteillaSuppeatTiedot[]
}

export const Ilmoituslomake = (props: IlmoituslomakeProps) => {
  console.log("Ilmoituslomake", props)
  return null
  // return (
  //   <Modal title="Ilman opiskelupaikkaa jäävien ilmoittaminen">
  //     <p>Tarkista ilmoitettavien oppijoiden yhteystiedot.</p>
  //     {props.oppijat.map((oppija) => (
  //       <IlmoitusForm key={oppija.oppija.henkilö.oid} />
  //     ))}
  //   </Modal>
  // )
}
