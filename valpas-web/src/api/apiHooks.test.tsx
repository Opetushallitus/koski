import { act, render, waitFor } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import * as E from "fp-ts/Either"
import React, { useState } from "react"
import { ApiResponse } from "./apiFetch"
import { useApiOnce, useApiWithParams } from "./apiHooks"
import { isError, isLoading, isSuccess } from "./apiUtils"

describe("apiHooks", () => {
  it("käsittele siististi tilanne, jossa api-kutsun tehnyt komponentti häviää ennen kuin kutsu on valmis", async () => {
    const { promise, resolve } = mockPromise()
    const container = render(<Container promise={promise} />)

    // Mock-api-kutsun tekevä elementti tulee näkyville ja kutsu käynnistyy
    userEvent.click(container.getByText("Show"))

    // Elementti piilotetaan, kutsun käsittely pitäisi siivoutua pois
    userEvent.click(container.getByText("Hide"))

    // Jos siivoaminen ei toimi oikein, resolven kutsuminen kirjoittaa konsoliin virheen
    // "Can't perform a React state update on an unmounted component" ja testi epäonnistuu
    await resolve()
  })

  it("useApiWithParams: vanhemman kutsun myöhässä saapuva vastaus ei korvaa uudemman kutsun vastausta", async () => {
    const resolvers = new Map<string, () => void>()
    const fetchFn = (param: string) =>
      new Promise<ApiResponse<string>>((resolve) => {
        resolvers.set(param, () =>
          resolve(E.right({ status: 200, data: `vastaus ${param}` })),
        )
      })

    const { rerender, getByTestId } = render(
      <ParamsComponent fetchFn={fetchFn} param="A" />,
    )
    rerender(<ParamsComponent fetchFn={fetchFn} param="B" />)

    await act(async () => resolvers.get("B")!())
    await act(async () => resolvers.get("A")!())

    expect(getByTestId("data").textContent).toEqual("vastaus B")
  })
})

type ParamsComponentProps = {
  fetchFn: (param: string) => Promise<ApiResponse<string>>
  param: string
}

const ParamsComponent = (props: ParamsComponentProps) => {
  const api = useApiWithParams(props.fetchFn, [props.param])
  return <div data-testid="data">{isSuccess(api) ? api.data : ""}</div>
}

const mockPromise = () => {
  jest.useFakeTimers()
  let resolved = false

  const promise = new Promise((resolve) => {
    const run = () => {
      if (resolved) {
        resolve(null)
      } else {
        setTimeout(run, 10)
      }
    }
    run()
  })

  return {
    promise,
    resolve: async () => {
      resolved = true
      jest.advanceTimersByTime(1000)
      await waitFor(() => promise)
    },
  }
}

type Props = {
  promise: Promise<any>
}

const Container = (props: Props) => {
  const [visible, setVisible] = useState(false)
  return (
    <div>
      {visible ? <ApiMockComponent promise={props.promise} /> : null}
      <button onClick={() => setVisible(false)}>Hide</button>
      <button onClick={() => setVisible(true)}>Show</button>
    </div>
  )
}

const ApiMockComponent = (props: Props) => {
  const mock = useApiOnce(() =>
    props.promise.then(() => E.right({ status: 200, data: "ok" })),
  )
  return (
    <div>
      {isLoading(mock) ? "loading" : ""}
      {isSuccess(mock) ? "success" : ""}
      {isError(mock) ? "error" : ""}
    </div>
  )
}
