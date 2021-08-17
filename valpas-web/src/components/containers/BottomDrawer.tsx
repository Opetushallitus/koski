import bem from "bem-ts"
import { forwardRefComponent } from "../../utils/plaincomponent"
import "./BottomDrawer.less"

const b = bem("bottomdrawer")

export const BottomDrawer = forwardRefComponent("aside", b("container"))
