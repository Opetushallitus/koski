import bem from "bem-ts"
import React from "react"
import { Link } from "react-router-dom"
import {
  fetchKuntailmoitusLaajatTiedot,
  fetchKuntailmoitusLaajatTiedotCache,
} from "../../api/api"
import { useApiWithParams } from "../../api/apiHooks"
import { isSuccess } from "../../api/apiUtils"
import { ModalButtonGroup } from "../../components/buttons/ModalButtonGroup"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { Modal } from "../../components/containers/Modal"
import { useBasePath } from "../../state/basePath"
import { oppijaPath } from "../../state/paths"
import { OppijaKuntailmoitus } from "../oppija/OppijaKuntailmoitus"
import "./KunnalleIlmoitettuModal.less"

const b = bem("KunnalleIlmoitettuModal")

export type KunnalleIlmoitettuModalProps = {
  organisaatioOid: string
  oppijaOid: string
  kuntailmoitusId: string
  onClose: () => void
}

export const KunnalleIlmoitettuModal = (
  props: KunnalleIlmoitettuModalProps,
) => {
  const ilmoitus = useApiWithParams(
    fetchKuntailmoitusLaajatTiedot,
    [props.kuntailmoitusId],
    fetchKuntailmoitusLaajatTiedotCache,
  )

  const basePath = useBasePath()
  const oppijaLink =
    props.oppijaOid !== ""
      ? oppijaPath.href(basePath, {
          oppijaOid: props.oppijaOid,
          kunnalleIlmoitetutRef: props.organisaatioOid,
        })
      : undefined

  return (
    <Modal onClose={props.onClose}>
      <div className={b("container")}>
        {isSuccess(ilmoitus) ? (
          <OppijaKuntailmoitus kuntailmoitus={ilmoitus.data} />
        ) : null}
      </div>
      <ModalButtonGroup>
        <RaisedButton onClick={props.onClose}>Sulje</RaisedButton>
        {oppijaLink && <Link to={oppijaLink}>Näytä oppijan tiedot</Link>}
      </ModalButtonGroup>
    </Modal>
  )
}
