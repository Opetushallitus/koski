import bem from "bem-ts"
import React, { useState } from "react"
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
) => (
  <Modal title="Oppivelvollisuuden keskeytys" onClose={props.onClose}>
    <SecondaryHeading>
      {props.oppija.henkilö.sukunimi} {props.oppija.henkilö.etunimet}
      {props.oppija.henkilö.hetu && ` (${props.oppija.henkilö.hetu})`}
    </SecondaryHeading>
    <OppivelvollisuudenKeskeytysForm />
  </Modal>
)

// Lomake

type OppivelvollisuudenKeskeytysFormProps = {}
type Aikavalinta = "määräaikainen" | "toistaiseksi"

const OppivelvollisuudenKeskeytysForm = (
  _props: OppivelvollisuudenKeskeytysFormProps
) => {
  const [aikavalinta, setAikavalinta] = useState<Aikavalinta>("määräaikainen")
  const [toistaiseksiVahvistettu, setToistaiseksiVahvistettu] = useState(false)
  const [dateRange, setDateRange] = useState<DateRange>([null, null])

  const määräaikainenSelected = aikavalinta === "määräaikainen"
  const toistaiseksiSelected = aikavalinta === "toistaiseksi"

  return (
    <div>
      <OppivelvollisuudenKeskeytysOption
        selected={määräaikainenSelected}
        onSelect={() => setAikavalinta("määräaikainen")}
        label="Oppivelvollisuus keskeytetään määräajaksi ajalle"
      >
        <DateRangePicker value={dateRange} onChange={setDateRange} />
      </OppivelvollisuudenKeskeytysOption>

      <OppivelvollisuudenKeskeytysOption
        selected={toistaiseksiSelected}
        onSelect={() => setAikavalinta("toistaiseksi")}
        label="Oppivelvollisuus keskeytetään toistaiseksi"
      >
        <LabeledCheckbox
          label="Vahvistan, että oppivelvollisuuden keskeytyksen syynä on oppivelvollisuuden suorittamisen estävä pysyvä sairaus tai vamma."
          value={toistaiseksiVahvistettu}
          onChange={setToistaiseksiVahvistettu}
          disabled={!toistaiseksiSelected}
        />
      </OppivelvollisuudenKeskeytysOption>
    </div>
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
