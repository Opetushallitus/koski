// Huom! Nämä vastaavat EditorModelSerializer.scalan serialisoimia ja frontendin muokkaamia tietotyypityksiä, eivät Scalan EditorModel- yms. luokkia.

import { Contextualized } from './EditorModelContext'

export type EditorModelType =
  | 'object'
  | 'prototype'
  | 'array'
  | 'enum'
  | 'number'
  | 'boolean'
  | 'date'
  | 'string'

export type EditorModel<T = any> = (
  | ObjectModel
  | PrototypeModel
  | ListModel
  | EnumeratedModel<T>
  | NumberModel
  | StringModel
  | BooleanModel
  | DateModel
  | DateTimeModel
) &
  (OptionalModel | {}) &
  (OneOfModel | {})

export type EditorModelWithValue<T = any> =
  | ObjectModel
  | ListModel
  | EnumeratedModel<T>
  | NumberModel
  | StringModel
  | BooleanModel
  | DateModel
  | DateTimeModel

export type EditableModel<T = any> =
  | ObjectModel
  | ValueModel
  | EnumeratedModel<T>

export type TypedEditorModelBase<T extends EditorModelType = EditorModelType> =
  {
    type: T
  } & Metadata

export type EditorModelPrototypeRecord = Record<string, EditorModel>

export const isTypedEditorModel =
  <M extends TypedEditorModelBase>(t: M['type']) =>
  (model: any): model is M =>
    (model as TypedEditorModelBase)?.type === t

export const hasValue = <T>(
  model: EditorModel<T> | OptionalModel | OneOfModel
): model is EditorModelWithValue<T> =>
  (model as EditorModelWithValue<T>)?.value !== undefined

export const isEditableModel = <T>(
  model: EditorModel<T>
): model is EditableModel<T> =>
  isObjectModel(model) || isEnumeratedModel(model) || isValueModel(model)

export type Maybe<T extends object> = Partial<T>

// ObjectModel

export const isObjectModel = isTypedEditorModel<ObjectModel>('object')

export type ObjectModel = TypedEditorModelBase<'object'> & {
  value: ObjectModelValue
  editable: boolean
  invalidatable: boolean
  prototypes: EditorModelPrototypeRecord
}

export type ObjectModelValue = {
  classes: string[]
  title?: string
  properties: ObjectModelProperty[]
  description?: string[]
}

export type ObjectModelProperty = {
  key: string
  title: string
  model: EditorModel
  editable?: boolean
}

export type ContextualizedObjectModelProperty<
  M extends EditorModel<T> & Contextualized,
  T extends object
> = {
  key: string
  title: string
  owner: M
  model: M
  editable?: boolean
}

// PrototypeModel

export const isPrototypeModel = isTypedEditorModel<PrototypeModel>('prototype')

export type PrototypeModel = TypedEditorModelBase<'prototype'> & {
  key: string
}

// OptionalModel

export const isSomeOptionalModel = (model: any): model is OptionalSomeModel =>
  model &&
  typeof model === 'object' &&
  (model as OptionalSomeModel).optional === true

export type OptionalModel = OptionalSomeModel | OptionalNoneModel

export type OptionalSomeModel = {
  optional: true
  optionalPrototype: EditorModel
}

export type OptionalNoneModel = {}

// ListModel

export const isListModel = isTypedEditorModel<ListModel>('array')

export type ListModel = TypedEditorModelBase<'array'> & {
  value: EditorModel[]
  arrayPrototype?: EditorModel
  arrayKey?: number
}

// EnumeratedModel

export const isEnumeratedModel =
  isTypedEditorModel<EnumeratedModel<any>>('enum')

export type EnumeratedModel<T> = TypedEditorModelBase<'enum'> & {
  value?: EnumValue<T>
  alternatives?: EnumValue<T>[]
  alternativesPath?: string
}

export type EnumValue<T> = {
  value: string
  title: string
  data: T
  groupName?: string
}

// OneOfModel

export const isOneOfModel = <M extends EditorModel>(
  model: M & MaybeOneOfModel
): model is M & OneOfModel =>
  typeof (model as OneOfModel).oneOfClass === 'string'

export type OneOfModel = {
  oneOfClass: string
  oneOfPrototypes: PrototypeModel[]
}

export type MaybeOneOfModel = Maybe<OneOfModel> | {}

// Value models

export type ValueModelValueType = number | boolean | Date | string

export type GenericValueModel<
  T extends EditorModelType,
  V extends ValueModelValueType
> = TypedEditorModelBase<T> & {
  value: ValueModelValue<V>
}

export type ValueModelValue<T extends ValueModelValueType> = {
  data: T
  classes?: string[]
  title?: string
}

export type ValueModel =
  | NumberModel
  | BooleanModel
  | DateModel
  | DateTimeModel
  | StringModel

export type NumberModel = GenericValueModel<'number', number>
export type BooleanModel = GenericValueModel<'boolean', boolean>
export type DateModel = GenericValueModel<'date', Date>
export type DateTimeModel = GenericValueModel<'date', Date>
export type StringModel = GenericValueModel<'string', string>

export const isNumberModel = isTypedEditorModel<NumberModel>('number')
export const isBooleanModel = isTypedEditorModel<BooleanModel>('boolean')
export const isDateModel = isTypedEditorModel<DateModel>('date')
export const isDateTimeModel = isTypedEditorModel<DateTimeModel>('date')
export const isStringModel = isTypedEditorModel<StringModel>('string')

export const isValueModel = (model: EditorModel<any>): model is ValueModel =>
  [
    isNumberModel,
    isBooleanModel,
    isDateModel,
    isDateTimeModel,
    isStringModel
  ].some((isValue) => isValue(model))

// Metadata

export const isIdentified = <T extends EditorModel>(
  model: T
): model is Identified<T> => !!(model as any).modelId

export type Identified<T extends EditorModel> = T & {
  modelId: number
}

// TODO: Monet näistä tekevät järkeä vain tietynlaisten modelien kanssa, joten tämän voisi vielä
// pilkkoa joten järkevämpiin kokonaisuuksiin.
export type Metadata = {
  minItems?: number
  maxItems?: number
  minValue?: number
  maxValue?: number
  minValueExclusive?: number
  maxValueExclusive?: number
  maxLines?: number
  scale?: number
  unitOfMeasure?: string
  regularExpression?: string
  example?: string
  onlyWhen?: OnlyWhen[]
  notWhen?: NotWhen[]
  title?: string
  key?: string
}

export type OnlyWhen = {
  path: string
  value: any
}

export type NotWhen = {
  path: string
  values: any | ArrayLike<any>
}
