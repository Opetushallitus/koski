import { expectNonEmptyString } from "../../../state/formValidators"
import { useFormState } from "../../../state/useFormState"

type IlmoitusFormValues = {
  nimi: string
  kunta: string
}

const initialValues: IlmoitusFormValues = {
  nimi: "",
  kunta: "",
}

const validators = {
  nimi: [expectNonEmptyString],
  kunta: [],
}

export type IlmoitusFormProps = {}

export const IlmoitusForm = (_props: IlmoitusFormProps) => {
  const form = useFormState({ initialValues, validators })
  form.set("nimi", "Heikki")
  return null
}
