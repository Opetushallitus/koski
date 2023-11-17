import React, { useContext, useMemo } from 'react'
import { CommonProps, common } from '../components-v2/CommonProps'

export type TestIdContext = string | undefined

const TestIdContext = React.createContext<TestIdContext>(undefined)

export type TestIdHook = {
  TestIdLayer: TestIdLayerComponent
  testId: string
}

export type TestIdLayerComponent = React.FC<{ children: React.ReactNode }>

export const useParentTestId = (): string | undefined => {
  return useContext(TestIdContext)
}

export const useTestId = (
  ...id: Array<string | number | undefined>
): string | undefined => {
  const parentTestId = useParentTestId()
  return useMemo(
    () =>
      id.length === 0 || id.some((t) => t === undefined)
        ? undefined
        : parentTestId && `${parentTestId}.${id.join('.')}`,
    [parentTestId, id]
  )
}

useTestId.toString = () => 'testi testi'

/**
 * Käytä tätä komponenttia, kun haluat aloittaa uuden testitunnisteiden nimiavaruuden.
 * Jos käytät tätä nimiavaruuden sisällä, siihen mennessä kertynyt tunnisteen polku hylätään
 * ja aloitetaan ns. puhtaalta pöydältä.
 *
 * Tämä komponentti ei lisää DOM-puuhun elementtiä.
 *
 * <TestIdRoot id="app">
 *    <TestIdRoot id="sivupalkki">
 *      <Sivupalkki /> <--- tämä komponentti näkee testId:n olevan "sivupalkki"
 *    </TestIdRoot>
 * </TestIdRoot>
 */
export const TestIdRoot: React.FC<{
  children: React.ReactNode
  id: string
}> = (props) => (
  <TestIdContext.Provider value={props.id} key={props.id}>
    {props.children}
  </TestIdContext.Provider>
)

/**
 * Käytä tätä komponenttia, kun haluat syventää olemassaolevan testitunnisteen polkua.
 * Puuttuva TestIdRoot ylätasolla aiheuttaa virheen.
 *
 * Tämä komponentti ei lisää DOM-puuhun elementtiä ellei wrap-propertya ole määritelty,
 * muussa tapauksessa lapset kääritään sen sisälle. Voit tällä tavalla lisätä välietappeja,
 * jos haluat esimerkiksi mahdollistaa kaikkien lapsien tekstien hakemisen yhdellä kertaa.
 *
 * <TestIdRoot id="app">
 *    <TestIdLayer id="sivupalkki">
 *      <Sivupalkki /> <--- tämä komponentti näkee testId:n olevan "app.sivupalkki"
 *    </TestIdLayer>
 * </TestIdRoot>
 */
export const TestIdLayer: React.FC<{
  children: React.ReactNode
  id: string | number
  wrap?: string
}> = (props) => {
  const testId = useTestId(props.id)
  const passedTestId = testId ?? 'PARENT_MISSING'
  const Wrap = props.wrap
  return (
    <TestIdContext.Provider value={passedTestId} key={passedTestId}>
      {Wrap ? (
        // @ts-ignore
        <Wrap data-testid={passedTestId}>{props.children}</Wrap>
      ) : (
        props.children
      )}
    </TestIdContext.Provider>
  )
}

/**
 * Kääräisee annetut lapset span-elementtiin, jolla on annettu testitunniste.
 */
export const TestIdText: React.FC<
  CommonProps<{
    children: React.ReactNode
    id?: string | number
  }>
> = (props) => {
  const testId = useTestId(props.id)
  return (
    <span {...common(props)} data-testid={testId} key={testId}>
      {props.children}
    </span>
  )
}
