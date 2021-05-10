import React, { useEffect } from "react"
import { Redirect } from "react-router-dom"
import { runningLocally } from "../utils/environment"

type Feature = "valpas" | "ilmoittaminen"

export const featureFlags: Record<Feature, string> = {
  valpas: "valpas-feature",
  ilmoittaminen: "valpas-ilmoittaminen",
}

const featureFlagEnabledValue = "enabled"

export type FeatureFlagEnablerProps = {
  features: Feature[]
  redirectTo: string
}

export const FeatureFlagEnabler = (props: FeatureFlagEnablerProps) => {
  useEffect(() => {
    props.features.map((feature) => {
      window.localStorage.setItem(
        featureFlags[feature],
        featureFlagEnabledValue
      )
    })
  })

  return <Redirect to={props.redirectTo} />
}

export const isFeatureFlagEnabled = (feature: Feature) =>
  runningLocally ||
  window.localStorage.getItem(featureFlags[feature]) === featureFlagEnabledValue
