import React from 'react'
import { useTree } from '../../appstate/tree'
import { useKansalainenTaiSuoritusjako } from '../../appstate/user'
import {
  OpiskeluoikeusTitle,
  OpiskeluoikeusTitleProps
} from '../opiskeluoikeus/OpiskeluoikeusTitle'

export type VirkailijaKansalainenContainerProps = React.PropsWithChildren<
  OpiskeluoikeusTitleProps & {
    renderTitle?: (props: OpiskeluoikeusTitleProps) => React.ReactNode
  }
>

export const VirkailijaKansalainenContainer: React.FC<
  VirkailijaKansalainenContainerProps
> = (props) => {
  const { TreeNode: OpiskeluoikeusTreeNode, ...opiskeluoikeusTree } = useTree()
  const kansalainenTaiSuoritusjako = useKansalainenTaiSuoritusjako()
  const { children, renderTitle, ...titleProps } = props
  const title =
    renderTitle ??
    ((p: OpiskeluoikeusTitleProps) => <OpiskeluoikeusTitle {...p} />)

  return kansalainenTaiSuoritusjako ? (
    <OpiskeluoikeusTreeNode>
      {title({ ...titleProps, tree: opiskeluoikeusTree })}
      {opiskeluoikeusTree.isOpen && props.children}
    </OpiskeluoikeusTreeNode>
  ) : (
    <>
      {title(titleProps)}
      {props.children}
    </>
  )
}
