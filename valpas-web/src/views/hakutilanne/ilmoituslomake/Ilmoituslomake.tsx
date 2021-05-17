import React from "react"
import { ModalButtonGroup } from "../../../components/buttons/ModalButtonGroup"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import { Modal } from "../../../components/containers/Modal"
import { t, T } from "../../../i18n/i18n"
import { KuntailmoitusPohjatiedot } from "../../../state/apitypes/kuntailmoituspohjatiedot"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import { IlmoitusForm } from "./IlmoitusForm"

export type IlmoituslomakeProps = {
  oppijat: OppijaHakutilanteillaSuppeatTiedot[]
  pohjatiedot: KuntailmoitusPohjatiedot
  onClose: () => void
}

export const Ilmoituslomake = (props: IlmoituslomakeProps) => (
  <Modal title={t("ilmoituslomake__otsikko")} onClose={props.onClose}>
    <p>
      <T id="ilmoituslomake__ohje" />
    </p>
    {props.pohjatiedot.oppijat.map((oppija, index) => (
      <IlmoitusForm
        key={oppija.oppijaOid}
        formIndex={index}
        numberOfForms={props.pohjatiedot.oppijat.length}
        oppija={
          props.oppijat.find((o) => o.oppija.henkilö.oid === oppija.oppijaOid)!!
        }
        pohjatiedot={oppija}
        kunnat={props.pohjatiedot.kunnat}
        maat={props.pohjatiedot.maat}
        kielet={props.pohjatiedot.yhteydenottokielet}
      />
    ))}
    <ModalButtonGroup>
      <RaisedButton hierarchy="secondary" onClick={props.onClose}>
        <T id="ilmoituslomake__sulje" />
      </RaisedButton>
    </ModalButtonGroup>
  </Modal>
)
