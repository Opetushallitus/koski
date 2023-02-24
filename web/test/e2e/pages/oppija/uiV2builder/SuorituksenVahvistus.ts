import { arrayOf } from './builder'
import { Button } from './Button'
import { FormField } from './controls'
import { Input } from './Input'
import { Label } from './Label'
import { Select } from './Select'

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
      myöntäjät: {
        edit: {
          add: Select,
          henkilö: arrayOf({ delete: Button }),
          newHenkilö: arrayOf({ nimi: Input, titteli: Input }),
          storedHenkilö: arrayOf(Select)
        }
      },
      organisaatio: FormField(Select, Select),
      paikkakunta: FormField(Select, Select),
      submit: Button,
      cancel: Button
    }
  }
})
