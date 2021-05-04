import React from "react"
import { TextField } from "../../../components/forms/TextField"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import {
  expectNonEmptyString,
  validateIfElse,
} from "../../../state/formValidators"
import { FormState, useFormState } from "../../../state/useFormState"

type IlmoitusFormValues = {
  asuinkunta: string
  yhteydenottokieli: string
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
}

export const IlmoitusForm = (props: IlmoitusFormProps) => {
  const form = useFormState({ initialValues, validators })
  return (
    <div>
      <p>{props.oppija.oppija.henkilö.sukunimi}</p>
      <TextField label="Asuinkunta" {...form.fieldProps("asuinkunta")} />
      <TextField label="Katuosoite" {...form.fieldProps("katuosoite")} />
      <TextField
        label="Yhteydenottokieli"
        {...form.fieldProps("yhteydenottokieli")}
      />
      <TextField label="Maa" {...form.fieldProps("maa")} />
    </div>
  )
}
