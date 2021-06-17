import bem from "bem-ts"
import React, { useCallback, useState } from "react"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { Modal } from "../../components/containers/Modal"
import { LabeledCheckbox } from "../../components/forms/Checkbox"
import {
  DateRange,
  DateRangePicker,
} from "../../components/forms/DateRangePicker"
import { RadioButton } from "../../components/forms/RadioButton"
import { SecondaryHeading } from "../../components/typography/headings"
import { OppijaLaajatTiedot } from "../../state/apitypes/oppija"
import "./OppivelvollisuudenKeskeytysModal.less"

const b = bem("ovkeskeytys")

export type OppivelvollisuudenKeskeytysModalProps = {
  oppija: OppijaLaajatTiedot
  onClose: () => void
}

export const OppivelvollisuudenKeskeytysModal = (
  props: OppivelvollisuudenKeskeytysModalProps
) => {
  const submit = console.log

  return (
    <Modal title="Oppivelvollisuuden keskeytys" onClose={props.onClose}>
      <SecondaryHeading>
        {props.oppija.henkilö.sukunimi} {props.oppija.henkilö.etunimet}
        {props.oppija.henkilö.hetu && ` (${props.oppija.henkilö.hetu})`}
      </SecondaryHeading>
      <OppivelvollisuudenKeskeytysForm onSubmit={submit} />
    </Modal>
  )
}

// Lomake

type OppivelvollisuudenKeskeytysFormProps = {
  onSubmit: (range: DateRange) => void
}

type Aikavalinta = "määräaikainen" | "toistaiseksi"

const OppivelvollisuudenKeskeytysForm = (
  props: OppivelvollisuudenKeskeytysFormProps
) => {
  const [aikavalinta, setAikavalinta] = useState<Aikavalinta>("määräaikainen")
  const [toistaiseksiVahvistettu, setToistaiseksiVahvistettu] = useState(false)
  const [dateRange, setDateRange] = useState<DateRange>([null, null])

  const määräaikainenSelected = aikavalinta === "määräaikainen"
  const toistaiseksiSelected = aikavalinta === "toistaiseksi"
  const isOk = määräaikainenSelected
    ? dateRange.every((d) => d != null)
    : toistaiseksiVahvistettu

  const { onSubmit } = props
  const submit = useCallback(() => {
    onSubmit(määräaikainenSelected ? dateRange : [null, null])
  }, [dateRange, määräaikainenSelected, onSubmit])

  return (
    <section className={b()}>
      <OppivelvollisuudenKeskeytysOption
        selected={määräaikainenSelected}
        onSelect={() => setAikavalinta("määräaikainen")}
        label="Oppivelvollisuus keskeytetään määräajaksi ajalle"
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
        label="Oppivelvollisuus keskeytetään toistaiseksi"
      >
        {toistaiseksiSelected && (
          <LabeledCheckbox
            label="Vahvistan, että oppivelvollisuuden keskeytyksen syynä on oppivelvollisuuden suorittamisen estävä pysyvä sairaus tai vamma."
            value={toistaiseksiVahvistettu}
            onChange={setToistaiseksiVahvistettu}
          />
        )}
      </OppivelvollisuudenKeskeytysOption>

      <RaisedButton onClick={submit} disabled={!isOk}>
        Keskeytä oppivelvollisuus
      </RaisedButton>
    </section>
  )
}

type OppivelvollisuudenKeskeytysOptionProps = {
  selected: boolean
  onSelect: () => void
  label: string
  children: React.ReactNode
}

const OppivelvollisuudenKeskeytysOption = (
  props: OppivelvollisuudenKeskeytysOptionProps
) => (
  <div className={b("option")}>
    <RadioButton
      selected={props.selected}
      onChange={(checked) => checked && props.onSelect()}
    >
      {props.label}
    </RadioButton>
    <div className={b("optionform")}>{props.children}</div>
  </div>
)
