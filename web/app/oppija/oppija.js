import {modelItems} from '../editor/EditorModel'

export const hasOpintoja = oppija => modelItems(oppija, 'opiskeluoikeudet').length > 0
