import { SuoritusModel } from '../suoritus/SuoritustaulukkoCommon'
import { ChangeBusContext, SaveChangesBusContext } from './EditorModelContext'
import { EditorModel, ObjectModel } from './EditorModels'

export type OsasuoritusEditorModel = SuoritusModel<OsasuoritusModelContext>

export type OsasuoritusModelContext = ChangeBusContext &
  SaveChangesBusContext & {
    kansalainen?: ObjectModel
    toimipiste: EditorModel
    opiskeluoikeus: EditorModel
  }
