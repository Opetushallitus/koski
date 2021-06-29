import bem from "bem-ts"
import * as A from "fp-ts/Array"
import React, { useMemo, useState } from "react"
import { fetchKuntailmoituksenPohjatiedot } from "../../api/api"
import { useApiWithParams } from "../../api/apiHooks"
import { isLoading, isSuccess, mapError, mapLoading } from "../../api/apiUtils"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { BottomDrawer } from "../../components/containers/BottomDrawer"
import { Spinner } from "../../components/icons/Spinner"
import { Error } from "../../components/typography/error"
import { T } from "../../i18n/i18n"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../state/apitypes/oppija"
import { Organisaatio } from "../../state/apitypes/organisaatiot"
import { Ilmoituslomake } from "../../views/ilmoituslomake/Ilmoituslomake"
import "./HakutilanneDrawer.less"

const b = bem("hakutilannedrawer")

export type HakutilanneDrawerProps = {
  selectedOppijat: OppijaHakutilanteillaSuppeatTiedot[]
  tekijäorganisaatio: Organisaatio
}

export const HakutilanneDrawer = (props: HakutilanneDrawerProps) => {
  const oppijat = props.selectedOppijat

  const [modalVisible, setModalVisible] = useState(false)
  const oppijaOids = useMemo(() => oppijat.map((o) => o.oppija.henkilö.oid), [
    oppijat,
  ])
  const pohjatiedot = useApiWithParams(
    fetchKuntailmoituksenPohjatiedot,
    modalVisible ? [oppijaOids, props.tekijäorganisaatio.oid] : undefined
  )

  return (
    <>
      <BottomDrawer>
        <div className={b("ilmoittaminen")}>
          <h4 className={b("ilmoittaminentitle")}>
            <T id="ilmoittaminen_drawer__title" />
          </h4>
          <div className={b("ilmoittamisenalarivi")}>
            <span className={b("valittujaoppilaita")}>
              <T
                id="ilmoittaminen_drawer__valittuja_oppilaita"
                params={{ määrä: oppijat.length }}
              />
            </span>
            <RaisedButton
              disabled={A.isEmpty(oppijat) || isLoading(pohjatiedot)}
              onClick={() => setModalVisible(true)}
            >
              <T id="ilmoittaminen_drawer__siirry_ilmoittamiseen" />
            </RaisedButton>
            {mapLoading(pohjatiedot, () => (
              <Spinner />
            ))}
            {mapError(pohjatiedot, () => (
              <Error>
                <T id="ilmoittaminen_drawer__pohjatietojen_haku_epäonnistui" />
              </Error>
            ))}
          </div>
        </div>
      </BottomDrawer>

      {modalVisible && isSuccess(pohjatiedot) ? (
        <Ilmoituslomake
          oppijat={oppijat.map((o) => ({
            henkilö: o.oppija.henkilö,
            opiskeluoikeudet: o.oppija.opiskeluoikeudet,
            lisätiedot: o.lisätiedot,
          }))}
          pohjatiedot={pohjatiedot.data}
          tekijäorganisaatio={props.tekijäorganisaatio}
          onClose={() => setModalVisible(false)}
        />
      ) : null}
    </>
  )
}
