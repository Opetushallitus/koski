import { Oid } from "../../../state/common"
import { expectNonEmptyString } from "../../../state/formValidators"
import { useFormState } from "../../../state/useFormState"

export type IlmoituslomakeProps = {
  oppijaOids: Oid[]
}

type IlmoitusKunnalle = {
  nimi: string
  kunta: string
}

const initialValues: IlmoitusKunnalle = {
  nimi: "",
  kunta: "",
}

const validators = {
  nimi: [expectNonEmptyString],
  kunta: [],
}

export const Ilmoituslomake = (_props: IlmoituslomakeProps) => {
  const form = useFormState<IlmoitusKunnalle>({ initialValues, validators })
  form.set("nimi", "asdas")
}
