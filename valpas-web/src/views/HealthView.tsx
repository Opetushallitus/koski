import React from "react"
import { healthCheck } from "../api/api"
import { useApiOnce } from "../api/apiHooks"
import { isInitial, isLoading, isSuccess } from "../api/apiUtils"
import { Page } from "../components/containers/Page"
import { Heading } from "../components/typography/headings"

export default () => {
  const res = useApiOnce(healthCheck)
  return (
    <Page id="health-view">
      <Heading>Service status</Heading>
      <p id="health-status">
        {isInitial(res) || isLoading(res)
          ? "Checking..."
          : isSuccess(res) && res.data === ""
            ? "OK"
            : "Backend failure"}
      </p>
    </Page>
  )
}
