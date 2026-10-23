import { arrayOf } from './builder'
import { Button } from './Button'
import { FormField } from './controls'
import { Input } from './Input'
import { Label } from './Label'
import { RadioButtons } from './RadioButtons'

const opiskeluoikeudenTilat = [
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
] as const

export type OpiskeluoikeudenTilaKoodiarvo =
  (typeof opiskeluoikeudenTilat)[number]

const OpiskeluoikeudenTilaButtons = RadioButtons(...opiskeluoikeudenTilat)

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
      tila: FormField(OpiskeluoikeudenTilaButtons, OpiskeluoikeudenTilaButtons),
      rahoitus: FormField(RahoitusButtons, RahoitusButtons),
      submit: Button,
      cancel: Button
    }
  }
})
