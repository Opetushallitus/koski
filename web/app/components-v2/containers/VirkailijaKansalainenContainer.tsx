import React from 'react'
import { useTree } from '../../appstate/tree'
import { useKansalainenTaiSuoritusjako } from '../../appstate/user'
import {
  OpiskeluoikeusTitle,
  OpiskeluoikeusTitleProps
} from '../opiskeluoikeus/OpiskeluoikeusTitle'

export type VirkailijaKansalainenContainerProps = React.PropsWithChildren<
  OpiskeluoikeusTitleProps & {
    renderTitle?: React.FC<OpiskeluoikeusTitleProps>
  }
>

export const VirkailijaKansalainenContainer: React.FC<
  VirkailijaKansalainenContainerProps
> = (props) => {
  const { TreeNode: OpiskeluoikeusTreeNode, ...opiskeluoikeusTree } = useTree()
  const kansalainenTaiSuoritusjako = useKansalainenTaiSuoritusjako()
  const { children, renderTitle, ...titleProps } = props
  const Title = renderTitle || OpiskeluoikeusTitle

  return kansalainenTaiSuoritusjako ? (
    <OpiskeluoikeusTreeNode>
      <Title {...titleProps} tree={opiskeluoikeusTree} />
      {opiskeluoikeusTree.isOpen && props.children}
    </OpiskeluoikeusTreeNode>
  ) : (
    <>
      <Title {...titleProps} />
      {props.children}
    </>
  )
}
