import bem from "bem-ts"
import React, { useState } from "react"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import { LabeledCheckbox } from "../../../components/forms/Checkbox"
import { Dropdown, koodistoToOptions } from "../../../components/forms/Dropdown"
import { TextField } from "../../../components/forms/TextField"
import { CaretDownIcon, CaretUpIcon } from "../../../components/icons/Icon"
import { HenkilöSuppeatTiedot } from "../../../state/apitypes/henkilo"
import { KoodistoKoodiviite } from "../../../state/apitypes/koodistot"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import {
  expectNonEmptyString,
  validateIfElse,
} from "../../../state/formValidators"
import { FormState, useFormState } from "../../../state/useFormState"
import { plainComponent } from "../../../utils/plaincomponent"
import "./IlmoitusForm.less"

const b = bem("ilmoitusform")

type IlmoitusFormValues = {
  asuinkunta: string
  yhteydenottokieli?: string
  postinumero: string
  katuosoite: string
  puhelinnumero: string
  sähköposti: string
  maa: string
  hakenutOpiskelemaanUlkomailleTaiAhvenanmaalle: boolean
}

const initialValues: IlmoitusFormValues = {
  asuinkunta: "",
  yhteydenottokieli: "",
  postinumero: "",
  katuosoite: "",
  puhelinnumero: "",
  sähköposti: "",
  maa: "",
  hakenutOpiskelemaanUlkomailleTaiAhvenanmaalle: false,
}

const validators = {
  asuinkunta: [expectNonEmptyString()],
  yhteydenottokieli: [expectNonEmptyString()],
  postinumero: [expectNonEmptyString()],
  katuosoite: [
    validateIfElse(
      (form: FormState<IlmoitusFormValues>) =>
        form.maa.currentValue === "Suomi",
      [expectNonEmptyString("ei voi olla tyhjä jos maa suomi")],
      []
    ),
  ],
  puhelinnumero: [expectNonEmptyString()],
  sähköposti: [expectNonEmptyString()],
  maa: [expectNonEmptyString()],
  hakenutOpiskelemaanUlkomailleTaiAhvenanmaalle: [],
}

export type IlmoitusFormProps = {
  oppija: OppijaHakutilanteillaSuppeatTiedot
  yhteydenottokielet: Array<KoodistoKoodiviite>
  formIndex: number
  numberOfForms: number
}

export const IlmoitusForm = (props: IlmoitusFormProps) => {
  const form = useFormState({ initialValues, validators })
  const [isOpen, setOpen] = useState(true)

  console.log(form)

  return (
    <IlmoitusFormFrame>
      <IlmoitusHeader
        henkilö={props.oppija.oppija.henkilö}
        formIndex={props.formIndex}
        numberOfForms={props.numberOfForms}
        isOpen={isOpen}
        onClick={() => setOpen(!isOpen)}
      />
      {isOpen ? (
        <IlmoitusBody>
          <TextField label="Asuinkunta" {...form.fieldProps("asuinkunta")} />
          <TextField label="Katuosoite" {...form.fieldProps("katuosoite")} />
          <Dropdown
            label="Yhteydenottokieli"
            options={koodistoToOptions(props.yhteydenottokielet)}
            {...form.fieldProps("yhteydenottokieli")}
          />
          <TextField label="Maa" {...form.fieldProps("maa")} />
          <LabeledCheckbox
            label="Hakenut opiskelemaan muualle"
            {...form.fieldProps(
              "hakenutOpiskelemaanUlkomailleTaiAhvenanmaalle"
            )}
          />
          <RaisedButton disabled={!form.isValid}>Ilmoita</RaisedButton>
        </IlmoitusBody>
      ) : null}
    </IlmoitusFormFrame>
  )
}

export type IlmoitusHeaderProps = {
  henkilö: HenkilöSuppeatTiedot
  formIndex: number
  numberOfForms: number
  isOpen: boolean
  onClick: () => void
}

const IlmoitusHeader = (props: IlmoitusHeaderProps) => (
  <IlmoitusHeaderFrame onClick={props.onClick}>
    <IlmoitusTitle>
      <IlmoitusTitleText>
        {props.numberOfForms > 1
          ? `${props.formIndex + 1}/${props.numberOfForms} `
          : ""}
        {props.henkilö.sukunimi} {props.henkilö.etunimet} (TODO: hetu)
      </IlmoitusTitleText>
      <IlmoitusTitleCaret>
        {props.isOpen ? <CaretDownIcon /> : <CaretUpIcon />}
      </IlmoitusTitleCaret>
    </IlmoitusTitle>
    <IlmoitusSubtitle>Oppija {props.henkilö.oid}</IlmoitusSubtitle>
  </IlmoitusHeaderFrame>
)

const IlmoitusFormFrame = plainComponent("form", b("frame"))
const IlmoitusHeaderFrame = plainComponent("header", b("header"))
const IlmoitusTitle = plainComponent("h3", b("title"))
const IlmoitusTitleText = plainComponent("div", b("titletext"))
const IlmoitusTitleCaret = plainComponent("div", b("titlecaret"))
const IlmoitusSubtitle = plainComponent("h4", b("subtitle"))
const IlmoitusBody = plainComponent("div", b("body"))
