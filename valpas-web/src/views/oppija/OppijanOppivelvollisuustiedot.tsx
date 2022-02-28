import bem from "bem-ts"
import * as A from "fp-ts/Array"
import React, { useState } from "react"
import { fetchKuntailmoituksenPohjatiedot } from "../../api/api"
import { useApiMethod } from "../../api/apiHooks"
import { isError, isLoading, isSuccess, mapLoading } from "../../api/apiUtils"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { VisibleForKäyttöoikeusrooli } from "../../components/containers/VisibleForKäyttöoikeusrooli"
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
            props.oppivelvollisuusVoimassaAsti
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
                hierarchy="secondary"
                onClick={() => setKeskeytysModalVisible(true)}
              >
                <T id="oppija__keskeytä_oppivelvollisuus" />
              </RaisedButton>

              {keskeytysModalVisible && (
                <OppivelvollisuudenKeskeytyksenLisäysModal
                  henkilö={props.henkilö}
                  onClose={() => setKeskeytysModalVisible(false)}
                  onSubmit={() => window.location.reload()}
                />
              )}
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
  oppivelvollisuusVoimassaAsti: ISODate
): React.ReactNode => {
  const keskeytykset = oppivelvollisuudenKeskeytykset
    .filter((ovk) => ovk.voimassa || ovk.tulevaisuudessa)
    .map((ovk) =>
      ovk.loppu !== undefined
        ? t("oppija__oppivelvollisuus_keskeytetty_value", {
            alkuPvm: formatDate(ovk.alku),
            loppuPvm: formatDate(ovk.loppu),
          })
        : t("oppija__oppivelvollisuus_keskeytetty_toistaiseksi_value", {
            alkuPvm: formatDate(ovk.alku),
          })
    )

  const keskeytysToistaiseksi = oppivelvollisuudenKeskeytykset.some(
    isKeskeytysToistaiseksi
  )

  const strs = !keskeytysToistaiseksi
    ? [
        t("oppija__oppivelvollisuus_voimassa_value", {
          date: formatNullableDate(oppivelvollisuusVoimassaAsti),
        }),
        ...keskeytykset,
      ]
    : keskeytykset

  return strs.length > 1 ? (
    <ul>
      {strs.map((str, index) => (
        <li key={index}>{str}</li>
      ))}
    </ul>
  ) : (
    <>{strs.join("")}</>
  )
}
