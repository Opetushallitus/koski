import bem from "bem-ts"
import React, { useMemo, useState } from "react"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import { LabeledCheckbox } from "../../../components/forms/Checkbox"
import {
  displayOrd,
  Dropdown,
  DropdownOption,
  koodistoToOptions,
} from "../../../components/forms/Dropdown"
import { TextField } from "../../../components/forms/TextField"
import {
  CaretDownIcon,
  CaretUpIcon,
  SuccessCircleIcon,
  WarningIcon,
} from "../../../components/icons/Icon"
import { Error } from "../../../components/typography/error"
import { SecondaryHeading } from "../../../components/typography/headings"
import { getLocalized, T, t } from "../../../i18n/i18n"
import { HenkilöSuppeatTiedot } from "../../../state/apitypes/henkilo"
import { KoodistoKoodiviite } from "../../../state/apitypes/koodistot"
import {
  KuntailmoitusKunta,
  OppijanPohjatiedot,
  PohjatietoYhteystieto,
} from "../../../state/apitypes/kuntailmoituspohjatiedot"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import {
  isAlkuperäHakemukselta,
  isAlkuperäRekisteristä,
} from "../../../state/apitypes/yhteystiedot"
import { Oid, OrganisaatioWithOid } from "../../../state/common"
import { expectNonEmptyString } from "../../../state/formValidators"
import { FormValidators, useFormState } from "../../../state/useFormState"
import { plainComponent } from "../../../utils/plaincomponent"
import "./IlmoitusForm.less"

const b = bem("ilmoitusform")

const koodiarvoFinland = "246"

export type IlmoitusFormValues = {
  asuinkunta?: string
  yhteydenottokieli?: string
  maa?: string
  postinumero: string
  postitoimipaikka: string
  lähiosoite: string
  puhelinnumero: string
  email: string
  hakenutOpiskelemaanYhteyshakujenUlkopuolella: boolean
}

const initialValues: IlmoitusFormValues = {
  asuinkunta: undefined,
  yhteydenottokieli: "FI",
  maa: koodiarvoFinland,
  postinumero: "",
  postitoimipaikka: "",
  lähiosoite: "",
  puhelinnumero: "",
  email: "",
  hakenutOpiskelemaanYhteyshakujenUlkopuolella: false,
}

const validators: FormValidators<IlmoitusFormValues> = {
  asuinkunta: [expectNonEmptyString("ilmoituslomake__pakollinen_tieto")],
  yhteydenottokieli: [],
  maa: [],
  postitoimipaikka: [],
  postinumero: [],
  lähiosoite: [],
  puhelinnumero: [],
  email: [],
  hakenutOpiskelemaanYhteyshakujenUlkopuolella: [],
}

export type IlmoitusFormProps = {
  oppija: OppijaHakutilanteillaSuppeatTiedot
  pohjatiedot: OppijanPohjatiedot
  kunnat: Array<OrganisaatioWithOid>
  maat: Array<KoodistoKoodiviite>
  kielet: Array<KoodistoKoodiviite>
  formIndex: number
  numberOfForms: number
  onSubmit?: (values: IlmoitusFormValues) => void
}

export const IlmoitusForm = (props: IlmoitusFormProps) => {
  const form = useFormState({
    initialValues: {
      ...initialValues,
      yhteydenottokieli: props.pohjatiedot.yhteydenottokieli?.koodiarvo,
      hakenutOpiskelemaanYhteyshakujenUlkopuolella: false, // TODO
    },
    validators,
  })
  const [isOpen, setOpen] = useState(true)
  const [isSubmitted, setSubmitted] = useState(false)

  const submit = form.submitCallback((formData) => {
    setSubmitted(true)
    if (props.onSubmit) {
      props.onSubmit(formData)
    }
  })

  const kunnat = props.kunnat
  const kuntaOptions = useMemo(() => kunnatToOptions(kunnat), [kunnat])

  return (
    <IlmoitusFormFrame>
      <IlmoitusHeader
        henkilö={props.oppija.oppija.henkilö}
        pohjatiedot={props.pohjatiedot}
        formIndex={props.formIndex}
        numberOfForms={props.numberOfForms}
        isOpen={isOpen}
        isSubmitted={isSubmitted}
        onClick={() => setOpen(!isOpen)}
      />
      {isOpen && !isSubmitted ? (
        <IlmoitusBody>
          <IlmoitusPrefillSelector
            pohjatiedot={props.pohjatiedot}
            onSelect={form.patch}
          />
          {props.pohjatiedot.turvakielto && <TurvakieltoWarning />}
          <Dropdown
            label={t("ilmoituslomake__asuinkunta")}
            required
            options={kuntaOptions}
            sort={displayOrd}
            {...form.fieldProps("asuinkunta")}
          />
          <Dropdown
            label={t("ilmoituslomake__yhteydenottokieli")}
            options={koodistoToOptions(props.kielet)}
            {...form.fieldProps("yhteydenottokieli")}
          />
          <SecondaryHeading className={b("muutyhteystiedototsikko")}>
            <T id="ilmoituslomake__muut_yhteystiedot" />
          </SecondaryHeading>
          <Dropdown
            label={t("ilmoituslomake__maa")}
            options={koodistoToOptions(props.maat)}
            {...form.fieldProps("maa")}
          />
          <TextField
            label={t("ilmoituslomake__postinumero")}
            {...form.fieldProps("postinumero")}
          />
          <TextField
            label={t("ilmoituslomake__postitoimipaikka")}
            {...form.fieldProps("postitoimipaikka")}
          />
          <TextField
            label={t("ilmoituslomake__katuosoite")}
            {...form.fieldProps("lähiosoite")}
          />
          <TextField
            label={t("ilmoituslomake__puhelinnumero")}
            {...form.fieldProps("puhelinnumero")}
          />
          <TextField
            label={t("ilmoituslomake__sähköposti")}
            {...form.fieldProps("email")}
          />
          <LabeledCheckbox
            label={t(
              "ilmoituslomake__hakenut_opiskelemaan_yhteishakujen_ulkopuolella"
            )}
            {...form.fieldProps("hakenutOpiskelemaanYhteyshakujenUlkopuolella")}
          />
          {form.allFieldsValidated && !form.isValid ? (
            <Error>
              <T id="ilmoituslomake__täytä_pakolliset_tiedot" />
            </Error>
          ) : null}
          <RaisedButton
            disabled={form.isValid ? false : "byLook"}
            onClick={form.isValid ? submit : form.validateAll}
          >
            <T id="ilmoituslomake__ilmoita_asuinkunnalle" />
          </RaisedButton>
        </IlmoitusBody>
      ) : null}
    </IlmoitusFormFrame>
  )
}

export type IlmoitusHeaderProps = {
  henkilö: HenkilöSuppeatTiedot
  pohjatiedot: OppijanPohjatiedot
  formIndex: number
  numberOfForms: number
  isOpen: boolean
  isSubmitted: boolean
  onClick: () => void
}

const IlmoitusHeader = (props: IlmoitusHeaderProps) => (
  <IlmoitusHeaderFrame onClick={props.onClick}>
    <IlmoitusTitle>
      <IlmoitusTitleIndex>
        {props.formIndex + 1}/{props.numberOfForms}
      </IlmoitusTitleIndex>
      <IlmoitusTitleTexts>
        <IlmoitusTitleText>
          {props.henkilö.sukunimi} {props.henkilö.etunimet}
          {props.pohjatiedot.hetu && ` (${props.pohjatiedot.hetu})`}
        </IlmoitusTitleText>
        <IlmoitusSubtitle>Oppija {props.henkilö.oid}</IlmoitusSubtitle>
      </IlmoitusTitleTexts>
      {!props.isSubmitted ? (
        <IlmoitusTitleCaret>
          {props.isOpen ? <CaretDownIcon /> : <CaretUpIcon />}
        </IlmoitusTitleCaret>
      ) : null}
    </IlmoitusTitle>
    {props.isSubmitted ? (
      <IlmoitusSubmitted>
        <SuccessCircleIcon inline color="white" />
        <T id="ilmoituslomake__ilmoitus_lähetetty" />
      </IlmoitusSubmitted>
    ) : null}
  </IlmoitusHeaderFrame>
)

const IlmoitusFormFrame = plainComponent("div", b("frame"))
const IlmoitusHeaderFrame = plainComponent("header", b("header"))
const IlmoitusTitle = plainComponent("h3", b("title"))
const IlmoitusTitleIndex = plainComponent("div", b("titleindex"))
const IlmoitusTitleTexts = plainComponent("div", b("titletexts"))
const IlmoitusTitleText = plainComponent("div", b("titletext"))
const IlmoitusTitleCaret = plainComponent("div", b("titlecaret"))
const IlmoitusSubtitle = plainComponent("h4", b("subtitle"))
const IlmoitusSubmitted = plainComponent("div", b("submitted"))
const IlmoitusBody = plainComponent("div", b("body"))

type IlmoitusPrefillSelectorProps = {
  pohjatiedot: OppijanPohjatiedot
  onSelect: (values: Partial<IlmoitusFormValues>) => void
}

const IlmoitusPrefillSelector = (props: IlmoitusPrefillSelectorProps) => (
  <div className={b("prefill")}>
    <T id="ilmoituslomake__esitäytä_yhteystiedoilla" />
    <ul className={b("prefilllist")}>
      {props.pohjatiedot.yhteystiedot.map((yhteystieto, index) => (
        <li
          key={index}
          className={b("prefillitem")}
          onClick={() =>
            props.onSelect({
              ...yhteystieto.yhteystiedot,
              asuinkunta: yhteystieto.kunta?.oid,
              maa: yhteystieto.yhteystiedot.maa?.koodiarvo,
            })
          }
        >
          {index + 1}) {yhteystiedonNimi(yhteystieto)}
        </li>
      ))}
    </ul>
  </div>
)

const yhteystiedonNimi = (yhteystieto: PohjatietoYhteystieto): string => {
  const alkuperä = yhteystieto.yhteystietojenAlkuperä
  if (isAlkuperäRekisteristä(alkuperä)) {
    return getLocalized(alkuperä.alkuperä.nimi) || "?"
  }
  if (isAlkuperäHakemukselta(alkuperä)) {
    return getLocalized(alkuperä.hakuNimi) || "?"
  }
  return "?"
}

const TurvakieltoWarning = () => (
  <div className={b("turvakielto")}>
    <WarningIcon />
    <div className={b("turvakieltotext")}>
      <T id="ilmoituslomake__turvakielto_ohje" />
    </div>
  </div>
)

const kunnatToOptions = (kunnat: KuntailmoitusKunta[]): DropdownOption<Oid>[] =>
  kunnat.map((kunta) => ({
    value: kunta.oid,
    display: getLocalized(kunta.kotipaikka?.nimi || kunta.nimi) || kunta.oid,
  }))
