import bem from "bem-ts"
import { plainComponent } from "../../utils/plaincomponent"
import "./NoDataMessage.less"

const b = bem("nodatamessage")

export const NoDataMessage = plainComponent("div", b())
