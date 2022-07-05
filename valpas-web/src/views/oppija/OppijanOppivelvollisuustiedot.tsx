import bem from "bem-ts"
import * as A from "fp-ts/Array"
import React, { useCallback, useState } from "react"
import { fetchKuntailmoituksenPohjatiedot } from "../../api/api"
import { useApiMethod } from "../../api/apiHooks"
import { isError, isLoading, isSuccess, mapLoading } from "../../api/apiUtils"
import { FlatButton } from "../../components/buttons/FlatButton"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { VisibleForKäyttöoikeusrooli } from "../../components/containers/VisibleForKäyttöoikeusrooli"
import { EditIcon } from "../../components/icons/Icon"
import { Spinner } from "../../components/icons/Spinner"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { InfoTooltip } from "../../components/tooltip/InfoTooltip"
import { Error } from "../../components/typography/error"
import { T, t } from "../../i18n/i18n"
import { kuntavalvontaAllowed } from "../../state/accessRights"
import { HenkilöLaajatTiedot } from "../../state/apitypes/henkilo"
import {
  isKeskeytysToistaiseksi,
  OppivelvollisuudenKeskeytys,
} from "../../state/apitypes/oppivelvollisuudenkeskeytys"
import { ISODate } from "../../state/common"
import { formatDate, formatNullableDate } from "../../utils/date"
import { Ilmoituslomake } from "../../views/ilmoituslomake/Ilmoituslomake"
import "./OppijaView.less"
import { OppivelvollisuudenKeskeytyksenLisäysModal } from "./oppivelvollisuudenkeskeytys/OppivelvollisuudenKeskeytyksenLisäysModal"
import { OppivelvollisuudenKeskeytyksenMuokkausModal } from "./oppivelvollisuudenkeskeytys/OppivelvollisuudenKeskeytyksenMuokkausModal"

const b = bem("oppijaview")

export type OppijanOppivelvollisuustiedotProps = {
  henkilö: HenkilöLaajatTiedot
  opiskelee: boolean
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti?: ISODate
  oppivelvollisuusVoimassaAsti: ISODate
  oppivelvollisuudenKeskeytykset: OppivelvollisuudenKeskeytys[]
  onOikeusTehdäKuntailmoitus?: boolean
}

export const OppijanOppivelvollisuustiedot = (
  props: OppijanOppivelvollisuustiedotProps
) => {
  const [keskeytysModalVisible, setKeskeytysModalVisible] = useState(false)
  const [muokattavaKeskeytys, setMuokattavaKeskeytys] = useState<
    OppivelvollisuudenKeskeytys | undefined
  >(undefined)

  const openKeskeytysModal = useCallback(
    (keskeytys?: OppivelvollisuudenKeskeytys) => {
      setMuokattavaKeskeytys(keskeytys)
      setKeskeytysModalVisible(true)
    },
    []
  )

  const oppijaOids = [props.henkilö.oid]

  const pohjatiedot = useApiMethod(fetchKuntailmoituksenPohjatiedot)

  return (
    <>
      <InfoTable>
        <InfoTableRow
          label={t("oppija__opiskelutilanne")}
          value={t(
            props.opiskelee
              ? "oppija__opiskelutilanne__opiskelemassa"
              : "oppija__opiskelutilanne__ei_opiskelupaikkaa"
          )}
        />
        <InfoTableRow
          label={t("oppija__oppivelvollisuus_voimassa")}
          value={oppivelvollisuusValue(
            props.oppivelvollisuudenKeskeytykset,
            props.oppivelvollisuusVoimassaAsti,
            openKeskeytysModal
          )}
        />
        <InfoTableRow
          label={t("oppija__maksuttomuus_voimassa")}
          value={t("oppija__maksuttomuus_voimassa_value", {
            date: formatNullableDate(
              props.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti
            ),
          })}
        />
        <InfoTableRow
          value={
            <VisibleForKäyttöoikeusrooli rooli={kuntavalvontaAllowed}>
              <RaisedButton
                id="ovkeskeytys-btn"
                data-testid="ovkeskeytys-btn"
                hierarchy="secondary"
                onClick={() => openKeskeytysModal()}
              >
                <T id="oppija__keskeytä_oppivelvollisuus" />
              </RaisedButton>

              {keskeytysModalVisible &&
                (muokattavaKeskeytys ? (
                  <OppivelvollisuudenKeskeytyksenMuokkausModal
                    henkilö={props.henkilö}
                    keskeytys={muokattavaKeskeytys}
                    onClose={() => setKeskeytysModalVisible(false)}
                    onSubmit={() => window.location.reload()}
                    onDelete={() => window.location.reload()}
                  />
                ) : (
                  <OppivelvollisuudenKeskeytyksenLisäysModal
                    henkilö={props.henkilö}
                    onClose={() => setKeskeytysModalVisible(false)}
                    onSubmit={() => window.location.reload()}
                  />
                ))}
            </VisibleForKäyttöoikeusrooli>
          }
        />
        {props.onOikeusTehdäKuntailmoitus && (
          <InfoTableRow
            value={
              <>
                <div className={b("ilmoitusbuttonwithinfo")}>
                  <RaisedButton
                    disabled={isLoading(pohjatiedot)}
                    hierarchy="secondary"
                    onClick={() => pohjatiedot.call(oppijaOids)}
                  >
                    <T id="oppija__tee_ilmoitus_valvontavastuusta" />
                  </RaisedButton>
                  <InfoTooltip
                    content={t(
                      "oppija__tee_ilmoitus_valvontavastuusta_tooltip"
                    )}
                  />
                </div>
                {mapLoading(pohjatiedot, () => (
                  <Spinner />
                ))}
                {isError(pohjatiedot) && (
                  <Error>
                    <T
                      id={
                        pohjatiedot.status == 403
                          ? "oppija__ei_oikeuksia_tehdä_ilmoitusta"
                          : "oppija__pohjatietojen_haku_epäonnistui"
                      }
                    />
                  </Error>
                )}
                {isSuccess(pohjatiedot) &&
                !A.isEmpty(pohjatiedot.data.mahdollisetTekijäOrganisaatiot) ? (
                  <Ilmoituslomake
                    oppijat={[{ henkilö: props.henkilö }]}
                    pohjatiedot={pohjatiedot.data}
                    tekijäorganisaatio={
                      pohjatiedot.data.mahdollisetTekijäOrganisaatiot[0]!!
                    }
                    onClose={() => {
                      pohjatiedot.clear()
                      window.location.reload()
                    }}
                  />
                ) : null}
              </>
            }
          />
        )}
      </InfoTable>
    </>
  )
}

const oppivelvollisuusValue = (
  oppivelvollisuudenKeskeytykset: OppivelvollisuudenKeskeytys[],
  oppivelvollisuusVoimassaAsti: ISODate,
  openKeskeytysModal: (keskeytys: OppivelvollisuudenKeskeytys) => void
): React.ReactNode => {
  const keskeytykset = oppivelvollisuudenKeskeytykset
    .filter((ovk) => ovk.voimassa || ovk.tulevaisuudessa)
    .map((ovk) => (
      <>
        {ovk.loppu !== undefined
          ? t("oppija__oppivelvollisuus_keskeytetty_value", {
              alkuPvm: formatDate(ovk.alku),
              loppuPvm: formatDate(ovk.loppu),
            })
          : t("oppija__oppivelvollisuus_keskeytetty_toistaiseksi_value", {
              alkuPvm: formatDate(ovk.alku),
            })}

        <VisibleForKäyttöoikeusrooli rooli={kuntavalvontaAllowed}>
          <FlatButton
            className={b("editkeskeytysbtn")}
            onClick={() => openKeskeytysModal(ovk)}
          >
            <EditIcon
              inline
              title={t("oppija__muokkaa_oppivelvollisuuden_keskeytystä_btn")}
            />
          </FlatButton>
        </VisibleForKäyttöoikeusrooli>
      </>
    ))

  const keskeytysToistaiseksi = oppivelvollisuudenKeskeytykset.some(
    isKeskeytysToistaiseksi
  )

  const rows = !keskeytysToistaiseksi
    ? [
        t("oppija__oppivelvollisuus_voimassa_value", {
          date: formatNullableDate(oppivelvollisuusVoimassaAsti),
        }),
        ...keskeytykset,
      ]
    : keskeytykset

  return rows.length > 1 ? (
    <ul>
      {rows.map((row, index) => (
        <li key={index}>{row}</li>
      ))}
    </ul>
  ) : (
    rows[0]
  )
}
