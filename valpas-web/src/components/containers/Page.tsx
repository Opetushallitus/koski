import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import { VirkailijaMainNavigation } from "../../views/hakutilanne/VirkailijaMainNavigation"
import "./Page.less"

const b = bem("page")

export type PageProps = React.HTMLAttributes<HTMLDivElement> & {
  hideNavigation?: boolean
}

export const Page = ({ className, ...props }: PageProps) => (
  <>
    {!props.hideNavigation && <VirkailijaMainNavigation />}
    <article className={joinClassNames(b(), className)} {...props} />
  </>
)
