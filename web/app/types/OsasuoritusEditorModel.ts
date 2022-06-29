import {
  ChangeBusContext,
  Contextualized,
  SaveChangesBusContext,
} from "./EditorModelContext";
import { EditorModel, ObjectModel } from "./EditorModels";

export type OsasuoritusEditorModel = ObjectModel &
  Contextualized<OsasuoritusModelContext>;

export type OsasuoritusModelContext = ChangeBusContext &
  SaveChangesBusContext & {
    kansalainen?: ObjectModel;
    toimipiste: EditorModel;
    opiskeluoikeus: EditorModel;
  };
