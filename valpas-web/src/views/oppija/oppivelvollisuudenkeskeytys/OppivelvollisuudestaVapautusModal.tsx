import bem from "bem-ts"
import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import React, { useCallback, useState } from "react"
import {
  createOvVapautus,
  deleteOvVapautus,
  fetchOvVapautuksenPohjatiedot,
} from "../../../api/api"
import {
  useApiMethod,
  useApiOnce,
  useOnApiSuccess,
} from "../../../api/apiHooks"
import { isError, isSuccess } from "../../../api/apiUtils"
import { ButtonGroup } from "../../../components/buttons/ButtonGroup"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import { Modal } from "../../../components/containers/Modal"
import { usePrompt } from "../../../components/containers/Prompt"
import { LabeledCheckbox } from "../../../components/forms/Checkbox"
import { DatePicker } from "../../../components/forms/DatePicker"
import {
  Dropdown,
  DropdownOption,
  koodistoToOptions,
} from "../../../components/forms/Dropdown"
import { ApiErrors } from "../../../components/typography/error"
import {
  SecondaryHeading,
  TertiaryHeading,
} from "../../../components/typography/headings"
import { T, t } from "../../../i18n/i18n"
import { HenkilöLaajatTiedot } from "../../../state/apitypes/henkilo"
import { OppivelvollisuudestaVapautus } from "../../../state/apitypes/oppivelvollisuudestavapautus"
import { ISODate } from "../../../state/common"
import { nonNull } from "../../../utils/arrays"
import { dateToday, maxNullableDate, toISODate } from "../../../utils/date"
import "./OppivelvollisuudestaVapautusModal.less"

const b = bem("ovvapautus")

export type OppivelvollisuudestaVapautusModalProps = {
  henkilö: HenkilöLaajatTiedot
  mitätöinti?: OppivelvollisuudestaVapautus
  onClose: () => void
  onSubmit: () => void
}

export const OppivelvollisuudestaVapautusModal = (
  props: OppivelvollisuudestaVapautusModalProps
) => {
  const pohjatiedot = useApiOnce(fetchOvVapautuksenPohjatiedot)
  const [date, setDate] = useState<ISODate | null>(
    props.mitätöinti?.vapautettu || toISODate(dateToday())
  )
  const setBoundedDate = useCallback(
    (newDate: ISODate | null) => {
      setDate(
        maxNullableDate(
          isSuccess(pohjatiedot) ? pohjatiedot.data.aikaisinPvm : null,
          newDate
        )
      )
    },
    [pohjatiedot]
  )

  const [confirmed, setConfirmed] = useState(false)

  const [kuntaOptions, setKuntaOptions] = useState<
    Array<DropdownOption<string>>
  >([])
  const [kunta, setKunta] = useState<string | undefined>()

  useOnApiSuccess(pohjatiedot, (response) => {
    const kunnat = koodistoToOptions(
      response.data.kunnat.map((k) => k.kotipaikka).filter(nonNull)
    )
    setKuntaOptions(kunnat)
    setKunta(props.mitätöinti?.kunta?.kotipaikka?.koodiarvo || kunnat[0]?.value)
  })

  const isValid = date && confirmed && kunta

  const uusiVapautus = useApiMethod(createOvVapautus)
  const submitUusiVapautus = useCallback(async () => {
    if (date && kunta) {
      pipe(
        await uusiVapautus.call({
          oppijaOid: props.henkilö.oid,
          vapautettu: date,
          kuntakoodi: kunta,
        }),
        E.map(props.onSubmit)
      )
    }
  }, [date, kunta, props, uusiVapautus])

  const mitätöinti = useApiMethod(deleteOvVapautus)
  const submitMitätöinti = useCallback(async () => {
    if (kunta) {
      pipe(
        await mitätöinti.call({
          oppijaOid: props.henkilö.oid,
          kuntakoodi: kunta,
        }),
        E.map(props.onSubmit)
      )
    }
  }, [kunta, mitätöinti, props])

  const prompt = usePrompt()

  return (
    <>
      <Modal
        title={
          props.mitätöinti
            ? t("ovvapautus__oppivelvollisuuden_vapauttamisen_mitätöinti_title")
            : t("ovvapautus__oppivelvollisuudesta_vapauttaminen_title")
        }
        testId="ovvapautus-modal"
        onClose={props.onClose}
      >
        {isSuccess(pohjatiedot) && (
          <section>
            <SecondaryHeading data-testid="ovkeskeytys-secondary-heading">
              {props.henkilö.sukunimi} {props.henkilö.etunimet}
              {props.henkilö.hetu && ` (${props.henkilö.hetu})`}
            </SecondaryHeading>
            <TertiaryHeading>
              <T id="ovvapautus__taho_heading" />
            </TertiaryHeading>
            <Dropdown
              options={kuntaOptions}
              value={kunta}
              onChange={setKunta}
              disabled={kuntaOptions.length < 2 || !!props.mitätöinti}
            />
            <TertiaryHeading>
              <T id="ovvapautus__päivämäärä_heading" />
            </TertiaryHeading>
            <DatePicker
              value={date}
              onChange={setBoundedDate}
              disabled={!!props.mitätöinti}
            />
            {!props.mitätöinti && (
              <LabeledCheckbox
                label={t(
                  props.mitätöinti
                    ? "ovvapautus__mitätöinti_confirm_checkbox"
                    : "ovvapautus__confirm_checkbox"
                )}
                value={confirmed}
                onChange={setConfirmed}
                className={b("confirmcb")}
                testId={"ovvapautus-vahvistus"}
              />
            )}

            {isError(uusiVapautus) && (
              <ApiErrors errors={uusiVapautus.errors} />
            )}
            {isError(mitätöinti) && <ApiErrors errors={mitätöinti.errors} />}

            <ButtonGroup className={b("buttons")}>
              <RaisedButton
                onClick={
                  props.mitätöinti
                    ? () =>
                        prompt.show(
                          t("ovvapautus__mitätöinnin_varmistus"),
                          submitMitätöinti
                        )
                    : () =>
                        prompt.show(
                          t("ovvapautus__vapautuksen_varmistus"),
                          submitUusiVapautus
                        )
                }
                disabled={!isValid && !props.mitätöinti}
                hierarchy="danger"
              >
                {t(
                  props.mitätöinti
                    ? "ovvapautus__mitätöinti_submit_btn"
                    : "ovvapautus__vapautus_submit_btn"
                )}
              </RaisedButton>
            </ButtonGroup>
          </section>
        )}
      </Modal>
      {prompt.component}
    </>
  )
}
