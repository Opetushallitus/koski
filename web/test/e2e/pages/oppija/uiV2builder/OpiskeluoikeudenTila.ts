import { arrayOf } from './builder'
import { Button } from './Button'
import { FormField, Control } from './controls'
import { Input } from './Input'
import { Label } from './Label'
import { RadioButtons } from './RadioButtons'

const OpiskeluoikeudenTilaButtons = RadioButtons(
  'eronnut',
  'hyvaksytystisuoritettu',
  'katsotaaneronneeksi',
  'keskeytynyt',
  'lasna',
  'loma',
  'mitatoity',
  'paattynyt',
  'peruutettu',
  'valiaikaisestikeskeytynyt',
  'valmistunut'
)

const RahoitusButtons = RadioButtons(
  '1',
  '2',
  '3',
  '4',
  '6',
  '7',
  '8',
  '9',
  '10',
  '11',
  '12',
  '13',
  '14',
  '15'
)

export const OpiskeluoikeudenTila = () => ({
  value: {
    items: arrayOf({
      date: Label,
      tila: Label,
      rahoitus: Label
    })
  },
  edit: {
    items: arrayOf({
      date: FormField(Input, Input),
      tila: Label,
      rahoitus: Label,
      remove: Button
    }),
    add: Button,
    modal: {
      date: FormField(Input, Input),
      tila: FormField(
        OpiskeluoikeudenTilaButtons,
        OpiskeluoikeudenTilaButtons as Control
      ),
      rahoitus: FormField(RahoitusButtons, RahoitusButtons as Control),
      submit: Button,
      cancel: Button
    }
  }
})
