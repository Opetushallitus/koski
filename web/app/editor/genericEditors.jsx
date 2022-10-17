import { OptionalEditor } from './OptionalEditor'
import { ObjectEditor } from './ObjectEditor'
import { ArrayEditor } from './ArrayEditor'
import { EnumEditor } from './EnumEditor'
import { StringEditor } from './StringEditor'
import { NumberEditor } from './NumberEditor'
import { LocalizedStringEditor } from './LocalizedStringEditor'
import { DateEditor } from './DateEditor'
import { BooleanEditor } from './BooleanEditor'

export default {
  optional: OptionalEditor,
  object: ObjectEditor,
  array: ArrayEditor,
  string: StringEditor,
  localizedstring: LocalizedStringEditor,
  number: NumberEditor,
  date: DateEditor,
  boolean: BooleanEditor,
  enum: EnumEditor
}
