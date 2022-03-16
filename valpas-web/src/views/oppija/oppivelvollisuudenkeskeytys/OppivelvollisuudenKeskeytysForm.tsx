import bem from "bem-ts"
import { isNonEmpty } from "fp-ts/lib/Array"
import React, { useCallback, useState } from "react"
import { ApiError } from "../../../api/apiFetch"
import { ButtonGroup } from "../../../components/buttons/ButtonGroup"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import { LabeledCheckbox } from "../../../components/forms/Checkbox"
import { DatePicker } from "../../../components/forms/DatePicker"
import {
  DateRange,
  DateRangePicker,
} from "../../../components/forms/DateRangePicker"
import {
  Dropdown,
  organisaatiotToOptions,
} from "../../../components/forms/Dropdown"
import { RadioButton } from "../../../components/forms/RadioButton"
import { ApiErrors } from "../../../components/typography/error"
import { T, t } from "../../../i18n/i18n"
import { Organisaatio } from "../../../state/apitypes/organisaatiot"
import { ISODate, Oid } from "../../../state/common"
import { today } from "../../../utils/date"
import "./OppivelvollisuudenKeskeytys.less"

const b = bem("ovkeskeytys")

export type OppivelvollisuudenKeskeytysFormProps = {
  organisaatiot: Organisaatio[]
  onSubmit: (aikaväli: OppivelvollisuudenKeskeytysFormValues) => void
  errors: ApiError[]
  muokattavaKeskeytys?: OppivelvollisuudenKeskeytysFormValues
  onDelete?: () => void
}

export type OppivelvollisuudenKeskeytysFormValues = {
  alku: ISODate
  loppu?: ISODate
  tekijäOrganisaatioOid: Oid
}

export type Aikavalinta = "määräaikainen" | "toistaiseksi"

export const OppivelvollisuudenKeskeytysForm = (
  props: OppivelvollisuudenKeskeytysFormProps
) => {
  const [aikavalinta, setAikavalinta] = useState<Aikavalinta>(
    props.muokattavaKeskeytys && props.muokattavaKeskeytys.loppu === undefined
      ? "toistaiseksi"
      : "määräaikainen"
  )

  const [toistaiseksiVahvistettu, setToistaiseksiVahvistettu] = useState(false)
  const [dateRange, setDateRange] = useState<DateRange>([
    props.muokattavaKeskeytys?.alku || today(),
    props.muokattavaKeskeytys?.loppu || null,
  ])
  const [organisaatio, setOrganisaatio] = useState<Oid | undefined>(
    props.muokattavaKeskeytys?.tekijäOrganisaatioOid ||
      props.organisaatiot[0]?.oid
  )

  const määräaikainenSelected = aikavalinta === "määräaikainen"
  const toistaiseksiSelected = aikavalinta === "toistaiseksi"
  const isOk =
    organisaatio !== undefined &&
    (määräaikainenSelected
      ? dateRange.every((d) => d != null)
      : toistaiseksiVahvistettu)

  const { onSubmit } = props
  const submit = useCallback(() => {
    if (
      määräaikainenSelected &&
      dateRange[0] !== null &&
      dateRange[1] !== null
    ) {
      onSubmit({
        alku: dateRange[0],
        loppu: dateRange[1],
        tekijäOrganisaatioOid: organisaatio!,
      })
    } else if (
      toistaiseksiSelected &&
      dateRange[0] &&
      toistaiseksiVahvistettu
    ) {
      onSubmit({
        alku: dateRange[0],
        tekijäOrganisaatioOid: organisaatio!,
      })
    }
  }, [
    dateRange,
    määräaikainenSelected,
    onSubmit,
    organisaatio,
    toistaiseksiSelected,
    toistaiseksiVahvistettu,
  ])

  return (
    <section className={b()}>
      {props.organisaatiot.length !== 1 ? (
        <Dropdown
          label={t("ovkeskeytys__organisaatio")}
          options={organisaatiotToOptions(props.organisaatiot)}
          value={organisaatio}
          onChange={setOrganisaatio}
          testId="organisaatio"
          disabled={Boolean(props.muokattavaKeskeytys)}
        />
      ) : null}

      <OppivelvollisuudenKeskeytysOption
        selected={määräaikainenSelected}
        onSelect={() => setAikavalinta("määräaikainen")}
        label={t("ovkeskeytys__keskeytys_määräajalle")}
      >
        <DateRangePicker
          value={dateRange}
          onChange={setDateRange}
          disabled={!määräaikainenSelected}
        />
      </OppivelvollisuudenKeskeytysOption>

      <OppivelvollisuudenKeskeytysOption
        selected={toistaiseksiSelected}
        onSelect={() => setAikavalinta("toistaiseksi")}
        label={t("ovkeskeytys__keskeytys_toistaiseksi")}
      >
        <DatePicker
          value={dateRange[0]}
          onChange={(startDate) => setDateRange([startDate, dateRange[1]])}
          disabled={!toistaiseksiSelected}
        />
        <LabeledCheckbox
          label={t("ovkeskeytys__keskeytys_toistaiseksi_vahvistus")}
          value={toistaiseksiVahvistettu}
          onChange={setToistaiseksiVahvistettu}
          disabled={!toistaiseksiSelected}
          className={b("confirmcb")}
        />
      </OppivelvollisuudenKeskeytysOption>

      {isNonEmpty(props.errors) && <ApiErrors errors={props.errors} />}

      {props.muokattavaKeskeytys ? (
        <ButtonGroup>
          <RaisedButton
            id="ovkeskeytys-submit-edit"
            className={b("submit")}
            onClick={submit}
            disabled={!isOk}
          >
            <T id="ovkeskeytys__tallenna_btn" />
          </RaisedButton>
          <RaisedButton
            id="ovkeskeytys-delete"
            hierarchy="danger"
            className={b("delete")}
            onClick={props.onDelete}
          >
            <T id="ovkeskeytys__poista_btn" />
          </RaisedButton>
        </ButtonGroup>
      ) : (
        <RaisedButton
          id="ovkeskeytys-submit"
          className={b("submit")}
          onClick={submit}
          disabled={!isOk}
        >
          <T id="ovkeskeytys__keskeytä_oppivelvollisuus_nappi" />
        </RaisedButton>
      )}
    </section>
  )
}

type OppivelvollisuudenKeskeytysOptionProps = {
  selected: boolean
  onSelect?: () => void
  label: string
  children: React.ReactNode
}

const OppivelvollisuudenKeskeytysOption = (
  props: OppivelvollisuudenKeskeytysOptionProps
) => (
  <div className={b("option")}>
    <RadioButton
      selected={props.selected}
      onChange={(checked) => checked && props.onSelect?.()}
    >
      {props.label}
    </RadioButton>
    <div className={b("optionform")}>{props.children}</div>
  </div>
)
