import bem from "bem-ts"
import { plainComponent } from "../../utils/plaincomponent"
import "./BottomDrawer.less"

const b = bem("bottomdrawer")

export const BottomDrawer = plainComponent("aside", b("container"))
