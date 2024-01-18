import bem from "bem-ts"
import React, { useState } from "react"
import { fetchOmatJaHuollettavienTiedot } from "../../../api/api"
import { useApiOnce, useOnApiSuccess } from "../../../api/apiHooks"
import { isError, isLoading, isSuccess } from "../../../api/apiUtils"
import { Card, CardHeader } from "../../../components/containers/cards"
import { KansalainenPage } from "../../../components/containers/KansalainenPage"
import { Dropdown, DropdownOption } from "../../../components/forms/Dropdown"
import { Spinner } from "../../../components/icons/Spinner"
import { ApiErrors } from "../../../components/typography/error"
import { Heading } from "../../../components/typography/headings"
import { t, T, TParagraphs } from "../../../i18n/i18n"
import {
  KansalainenIlmanTietoja,
  KansalainenOppijatiedot,
  KansalaisnäkymänTiedot,
} from "../../../state/apitypes/kansalainen"
import { User } from "../../../state/common"
import { hetuToBirthday } from "../../../state/hetu"
import { renderOption } from "../../../utils/option"
import { plainComponent } from "../../../utils/plaincomponent"
import { KansalainenGrid } from "../../oppija/OppijaGrid"
import "./KansalainenOmatJaHuollettavienTiedotView.less"

const b = bem("omattiedot")

export type KansalainenOmatTiedotViewProps = {
  user: User
}

type SelectedOppija = {
  nimi: string
  hetu?: string
  oppija?: KansalainenOppijatiedot
  eiTietoja?: KansalainenIlmanTietoja
}

type OppijaDropdownOption = DropdownOption<SelectedOppija>

const SecondaryHeading = plainComponent("h2", b("secondaryheading"))

export const KansalainenOmatJaHuollettavienTiedotView = (
  props: KansalainenOmatTiedotViewProps,
) => {
  const [options, setOptions] = useState<OppijaDropdownOption[]>([])
  const [oppija, selectOppija] = useState<SelectedOppija>()

  const tiedot = useApiOnce(fetchOmatJaHuollettavienTiedot)
  useOnApiSuccess(tiedot, (response) => {
    const options = asOppijaOptions(props.user, response.data)
    setOptions(options)
    selectOppija(options[0]?.value)
  })

  return (
    <KansalainenPage>
      <Heading>
        <T id="kansalainen_omattiedot_otsikko" />
      </Heading>
      <div className={b("lead")}>
        <TParagraphs id="kansalainen_omattiedot_esittely" />
      </div>

      {options.length > 1 && (
        <Oppijavalitsin
          options={options}
          value={oppija}
          onSelect={selectOppija}
        />
      )}

      {isLoading(tiedot) && <Spinner />}
      {isError(tiedot) && <ApiErrors errors={tiedot.errors} />}
      {isSuccess(tiedot) && <OppijanTiedot selected={oppija} />}
    </KansalainenPage>
  )
}

type OppijavalitsinProps = {
  options: OppijaDropdownOption[]
  value?: SelectedOppija
  onSelect: (oppija?: SelectedOppija) => void
}

const Oppijavalitsin = (props: OppijavalitsinProps) => (
  <div className={b("oppijavalitsin")}>
    <SecondaryHeading>
      <label htmlFor="oppija-select">
        <T id="kansalainen_henkilövalinta_otsikko" />
      </label>
    </SecondaryHeading>
    <div>
      <Dropdown
        selectorId="oppija-select"
        options={props.options}
        value={props.value}
        onChange={props.onSelect}
      />
    </div>
  </div>
)

type OppijanTiedotProps = {
  selected?: SelectedOppija
}

const OppijanTiedot = (props: OppijanTiedotProps) =>
  props.selected ? (
    <section>
      {props.selected.oppija ? (
        <>
          <OppijaHeader selected={props.selected} />
          <KansalainenGrid data={props.selected.oppija} />
        </>
      ) : (
        <EiTietoja selected={props.selected} />
      )}
    </section>
  ) : null

type OppijaHeaderProps = {
  selected: SelectedOppija
}

const OppijaHeader = (props: OppijaHeaderProps) => (
  <header className={b("header")}>
    <SecondaryHeading>{props.selected.nimi}</SecondaryHeading>
    {renderOption(
      hetuToBirthday(props.selected.hetu || props.selected.eiTietoja?.hetu),
      (pvm) => (
        <p className={b("birthday")}>
          <T id="kansalainen_syntynyt" params={{ pvm }} />
        </p>
      ),
    )}
  </header>
)

type EiTietojaProps = {
  selected: SelectedOppija
}

const EiTietoja = (props: EiTietojaProps) => (
  <Card>
    <CardHeader>
      <TParagraphs
        id="kansalainen_omattiedot_ei_tietoja"
        params={{
          nimi: props.selected.nimi,
        }}
      />
    </CardHeader>
  </Card>
)

const asOppijaOptions = (
  user: User,
  tiedot: KansalaisnäkymänTiedot,
): OppijaDropdownOption[] => [
  asOmatTiedot(user, tiedot.omatTiedot),
  ...tiedot.huollettavat.map(henkilöOption),
  ...tiedot.huollettavatIlmanTietoja.map((h) =>
    eiTietojaOption(h.nimi, h.hetu),
  ),
] // TODO: sort

const asOmatTiedot = (
  user: User,
  omatTiedot?: KansalainenOppijatiedot,
): OppijaDropdownOption =>
  omatTiedot ? henkilöOption(omatTiedot) : eiTietojaOption(user.name)

const henkilöOption = (o: KansalainenOppijatiedot): OppijaDropdownOption => {
  const henkilö = o.oppija.henkilö
  const nimi = `${henkilö.sukunimi} ${henkilö.etunimet}`

  return {
    display: optionDisplayText(nimi, henkilö.hetu),
    value: { nimi, hetu: henkilö.hetu, oppija: o },
  }
}

const optionDisplayText = (nimi: string, hetu?: string) =>
  `${nimi} ${hetu ? `(${hetu})` : ""}`

const eiTietojaOption = (
  nimi: string,
  hetu?: string,
): OppijaDropdownOption => ({
  display: `${optionDisplayText(nimi, hetu)} (${t(
    "kansalainen_henkilövalinta_ei_tietoja",
  )})`,
  value: {
    nimi,
    eiTietoja: { nimi, hetu },
  },
})
