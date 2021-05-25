import bem from "bem-ts"
import React from "react"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import {
  Dropdown,
  organisaatiotToOptions,
} from "../../../components/forms/Dropdown"
import { TextField } from "../../../components/forms/TextField"
import { t, T } from "../../../i18n/i18n"
import { KuntailmoituksenTekijäLaajatTiedot } from "../../../state/apitypes/kuntailmoitus"
import { Organisaatio } from "../../../state/apitypes/organisaatiot"
import { expectNonEmptyString } from "../../../state/formValidators"
import { FormValidators, useFormState } from "../../../state/useFormState"
import { removeFalsyValues } from "../../../utils/objects"
import { plainComponent } from "../../../utils/plaincomponent"
import "./IlmoitusForm.less"

const b = bem("ilmoitusform")

export type IlmoituksenTekijäFormProps = {
  tekijä: KuntailmoituksenTekijäLaajatTiedot
  organisaatiot: Organisaatio[]
  onSubmit: (tekijä: KuntailmoituksenTekijäLaajatTiedot) => void
}

export type YhteystiedotValues = {
  organisaatio?: string
  email: string
  puhelinnumero: string
}

const initialValues = (
  tekijä: KuntailmoituksenTekijäLaajatTiedot
): YhteystiedotValues => ({
  organisaatio: tekijä.organisaatio.oid,
  email: tekijä.henkilö?.email || "",
  puhelinnumero: tekijä.henkilö?.puhelinnumero || "",
})

const validators: FormValidators<YhteystiedotValues> = {
  organisaatio: [expectNonEmptyString("ilmoituslomake__pakollinen_tieto")],
  email: [],
  puhelinnumero: [],
}

export const IlmoituksenTekijäForm = (props: IlmoituksenTekijäFormProps) => {
  const form = useFormState({
    initialValues: initialValues(props.tekijä),
    validators,
  })

  const submit = (data: YhteystiedotValues) => {
    const org = props.organisaatiot.find((o) => o.oid === data.organisaatio)
    if (org) {
      props.onSubmit({
        organisaatio: org,
        henkilö: removeFalsyValues({
          ...props.tekijä.henkilö,
          email: data.email,
          puhelinnumero: data.puhelinnumero,
        }),
      })
    }
  }

  return (
    <YhteystiedotFrame>
      <YhteystiedotBody>
        <TextField
          label={t("ilmoituksentekijälomake__nimi")}
          value={`${props.tekijä.henkilö?.sukunimi || ""} ${
            props.tekijä.henkilö?.etunimet || ""
          }`}
        />
        <Dropdown
          label={t("ilmoituksentekijälomake__organisaatio")}
          options={organisaatiotToOptions(props.organisaatiot)}
          {...form.fieldProps("organisaatio")}
          testId="organisaatio"
        />
        <TextField
          label={t("ilmoituksentekijälomake__sähköposti")}
          {...form.fieldProps("email")}
          testId="email"
        />
        <TextField
          label={t("ilmoituksentekijälomake__puhelinnumero")}
          {...form.fieldProps("puhelinnumero")}
          testId="puhelin"
        />
        <RaisedButton
          onClick={form.submitCallback(submit)}
          className={b("continue")}
        >
          <T id="ilmoituksentekijälomake__jatka" />
        </RaisedButton>
      </YhteystiedotBody>
    </YhteystiedotFrame>
  )
}

const YhteystiedotFrame = plainComponent("div", b("frame"))
const YhteystiedotBody = plainComponent("div", b("body"))
