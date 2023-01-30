import Bacon from 'baconjs'
import {
  EditorElement,
  ModelErrorRecord,
  ChangeBusAction
} from '../editor/EditorModel'
import { EditorModel, EditorModelPrototypeRecord } from './EditorModels'

export const hasContext = <T extends object>(
  model?: Partial<Contextualized<T>>
): model is Contextualized<T> => model?.context !== undefined

export type Contextualized<T extends object = object> = {
  context: ContextData<T>
  path: string[]
  parent?: Contextualized<T>
}

export type ContextData<T extends object> = T & BaseContext

export type BaseContext = {
  path: DataPath
  prototypes: EditorModelPrototypeRecord
  edit?: boolean
}

export type DataPath = string[]

export type EditorMappingContext = {
  editorMapping: Record<string, EditorElement>
}

export type ValidationContext = {
  validationResult?: ModelErrorRecord
}

export type PrototypeListContext = {
  prototypes: EditorModelPrototypeRecord
}

export type ChangeBusContext<M extends EditorModel = EditorModel> = {
  changeBus: ChangeBus<M>
}

export type SaveChangesBusContext<M extends EditorModel = EditorModel> = {
  saveChangesBus: ChangeBus<M>
}

export type EditBusContext<M extends EditorModel = EditorModel> = {
  editBus: ChangeBus<M>
}

export type ChangeBus<M extends EditorModel = EditorModel> = Bacon.Bus<
  M,
  ChangeBusAction[]
>
