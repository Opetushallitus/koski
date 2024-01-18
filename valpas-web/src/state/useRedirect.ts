import { useCallback } from "react"
import { useHistory } from "react-router-dom"
import { useBasePath } from "./basePath"
import { Oid } from "./common"
import { OrganisaatioOidProps, PathDeclaration } from "./paths"

export const useImperativeRedirect = <T, A extends any[]>(
  pathDeclaration: PathDeclaration<A>,
  getArgs: (id: T) => A,
) => {
  const history = useHistory()
  const basePath = useBasePath()
  return useCallback(
    (id?: T) => {
      if (id !== undefined) {
        history.push(pathDeclaration.href(basePath, ...getArgs(id)))
      }
    },
    [basePath, getArgs, history, pathDeclaration],
  )
}

export const useRedirectToOrganisaatio = (
  pathDeclaration: PathDeclaration<[OrganisaatioOidProps]>,
): ((organisaatioOid?: Oid) => void) =>
  useImperativeRedirect(pathDeclaration, (organisaatioOid: Oid) => [
    { organisaatioOid },
  ])
