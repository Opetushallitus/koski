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
import { isValidSortedValues, today } from "../../../utils/date"
import "./OppivelvollisuudenKeskeytys.less"

const b = bem("ovkeskeytys")

export type OppivelvollisuudenKeskeytysFormProps = {
  organisaatiot: Organisaatio[]
  testId?: string
  onSubmit: (aikaväli: OppivelvollisuudenKeskeytysFormValues) => void
  errors: ApiError[]
  muokattavaKeskeytys?: OppivelvollisuudenKeskeytysFormValues
  onDelete?: () => void
} & Omit<React.HTMLAttributes<HTMLElement>, "onSubmit">

export type OppivelvollisuudenKeskeytysFormValues = {
  alku: ISODate
  loppu?: ISODate
  tekijäOrganisaatioOid: Oid
}

export type Aikavalinta = "määräaikainen" | "toistaiseksi"

export const OppivelvollisuudenKeskeytysForm = (
  props: OppivelvollisuudenKeskeytysFormProps
) => {
  const {
    organisaatiot,
    testId,
    errors,
    muokattavaKeskeytys,
    onSubmit,
    onDelete,
    ...rest
  } = props
  const [aikavalinta, setAikavalinta] = useState<Aikavalinta>(
    muokattavaKeskeytys && muokattavaKeskeytys.loppu === undefined
      ? "toistaiseksi"
      : "määräaikainen"
  )

  const [toistaiseksiVahvistettu, setToistaiseksiVahvistettu] = useState(false)
  const [dateRange, setDateRange] = useState<DateRange>([
    muokattavaKeskeytys?.alku || today(),
    muokattavaKeskeytys?.loppu || null,
  ])
  const [organisaatio, setOrganisaatio] = useState<Oid | undefined>(
    muokattavaKeskeytys?.tekijäOrganisaatioOid || organisaatiot[0]?.oid
  )

  const määräaikainenSelected = aikavalinta === "määräaikainen"
  const toistaiseksiSelected = aikavalinta === "toistaiseksi"
  const isOk =
    organisaatio !== undefined &&
    (määräaikainenSelected
      ? dateRange.every((d) => d !== null)
      : toistaiseksiVahvistettu)

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
    <section className={b()} {...rest}>
      {organisaatiot.length !== 1 ? (
        <Dropdown
          label={t("ovkeskeytys__organisaatio")}
          options={organisaatiotToOptions(organisaatiot)}
          value={organisaatio}
          onChange={setOrganisaatio}
          testId="organisaatio"
          disabled={Boolean(muokattavaKeskeytys)}
        />
      ) : null}

      <OppivelvollisuudenKeskeytysOption
        selected={määräaikainenSelected}
        onSelect={() => setAikavalinta("määräaikainen")}
        label={t("ovkeskeytys__keskeytys_määräajalle")}
        testId={"ovkeskeytys-maaraaikainen-option"}
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
        testId={"ovkeskeytys-toistaiseksi-option"}
      >
        <DatePicker
          value={dateRange[0]}
          onChange={(startDate) => {
            if (isValidSortedValues(startDate, dateRange[1])) {
              setDateRange([startDate, dateRange[1]])
            }
          }}
          disabled={!toistaiseksiSelected}
        />
        <LabeledCheckbox
          label={t("ovkeskeytys__keskeytys_toistaiseksi_vahvistus")}
          value={toistaiseksiVahvistettu}
          onChange={setToistaiseksiVahvistettu}
          disabled={!toistaiseksiSelected}
          className={b("confirmcb")}
          testId={"ovkeskeytys-toistaiseksi-vahvistus"}
        />
      </OppivelvollisuudenKeskeytysOption>

      {isNonEmpty(errors) && <ApiErrors errors={errors} />}

      {muokattavaKeskeytys ? (
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
            onClick={onDelete}
          >
            <T id="ovkeskeytys__poista_btn" />
          </RaisedButton>
        </ButtonGroup>
      ) : (
        <RaisedButton
          id="ovkeskeytys-submit"
          data-testid="ovkeskeytys-submit"
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
  testId?: string
} & React.PropsWithChildren<React.HTMLAttributes<HTMLDivElement>>

const OppivelvollisuudenKeskeytysOption: React.FC<
  OppivelvollisuudenKeskeytysOptionProps
> = (props) => {
  return (
    <div className={b("option")}>
      <RadioButton
        selected={props.selected}
        onChange={(checked) => checked && props.onSelect?.()}
        testId={props.testId ? props.testId : ""}
      >
        {props.label}
      </RadioButton>
      <div className={b("optionform")}>{props.children}</div>
    </div>
  )
}
