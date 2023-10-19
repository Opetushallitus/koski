import { arrayOf } from './builder'
import { Button } from './Button'
import { FormField } from './controls'
import { Input } from './Input'
import { Label } from './Label'
import { RadioButtons } from './RadioButtons'

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
      date: Input,
      tila: Label,
      rahoitus: Label,
      remove: Button
    }),
    add: Button,
    modal: {
      date: FormField(Input, Input),
      tila: FormField(RadioButtons, RadioButtons),
      rahoitus: FormField(RadioButtons, RadioButtons),
      submit: Button,
      cancel: Button
    }
  }
})
