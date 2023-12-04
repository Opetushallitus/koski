import { arrayOf, BuiltIdNode } from './builder'
import { Button } from './Button'
import { FormField } from './controls'
import { Input } from './Input'
import { Label } from './Label'
import { Select } from './Select'

export type BuiltSuorituksenVahvistus = BuiltIdNode<
  ReturnType<typeof SuorituksenVahvistus>
>

export const SuorituksenVahvistus = () => ({
  value: {
    status: Label,
    details: Label,
    henkilö: arrayOf(Label)
  },
  edit: {
    status: Label,
    details: Label,
    henkilö: arrayOf(Label),
    merkitseValmiiksi: Button,
    merkitseKeskeneräiseksi: Button,
    modal: {
      date: FormField(Input, Input),
      organisaatiohenkilöt: {
        edit: {
          add: Select,
          henkilö: arrayOf({
            delete: Button,
            newHenkilö: { nimi: Input, titteli: Input },
            storedHenkilö: Select
          })
        }
      },
      organisaatio: FormField(Select, Select),
      paikkakunta: FormField(Select, Select),
      submit: Button,
      cancel: Button
    }
  }
})

export const vahvistaSuoritusUudellaHenkilöllä = async (
  suoritusIds: BuiltSuorituksenVahvistus,
  nimi: string,
  titteli: string,
  pvm: string
) => {
  const vahvistus = suoritusIds.edit
  await vahvistus.merkitseValmiiksi.click()

  await vahvistus.modal.date.set(pvm)

  const myöntäjät = vahvistus.modal.organisaatiohenkilöt.edit
  await myöntäjät.add.set('__NEW__')

  const henkilö = myöntäjät.henkilö(0).newHenkilö
  await henkilö.nimi.set(nimi)
  await henkilö.titteli.set(titteli)

  await vahvistus.modal.submit.click()
}

export const vahvistaSuoritusTallennetullaHenkilöllä = async (
  suoritusIds: BuiltSuorituksenVahvistus,
  nimi: string,
  pvm: string
) => {
  const vahvistus = suoritusIds.edit

  await vahvistus.merkitseValmiiksi.click()
  await vahvistus.modal.date.set(pvm)
  await vahvistus.modal.organisaatiohenkilöt.edit.add.set(nimi)
  await vahvistus.modal.submit.click()
}

export const isMerkitseKeskeneräiseksiDisabled = async (
  suoritusIds: BuiltSuorituksenVahvistus
) => {
  return suoritusIds.edit.merkitseKeskeneräiseksi.isDisabled()
}

export const isMerkitseValmiiksiDisabled = async (
  suoritusIds: BuiltSuorituksenVahvistus
) => {
  return suoritusIds.edit.merkitseValmiiksi.isDisabled()
}
