import { Button } from './Button'
import { Label } from './Label'
import { OpiskeluoikeudenTila } from './OpiskeluoikeudenTila'
import { arrayOf } from './builder'

export const OpiskeluoikeusHeader = () => ({
  nimi: Label,
  oid: Label,
  voimassaoloaika: Label,
  edit: Button,
  cancelEdit: Button,
  invalidate: {
    button: Button,
    confirm: Button,
    cancel: Button
  },
  tila: OpiskeluoikeudenTila(),
  lisätiedotButton: Button,
  lisätiedot: {
    maksuttomuudet: arrayOf({ remove: Button })
  }
})
