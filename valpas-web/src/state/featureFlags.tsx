import React, { useEffect } from "react"
import { Redirect } from "react-router-dom"
import { runningLocally } from "../utils/environment"

export type Feature = "valpas" | "ilmoittaminen" | "mock"

export const featureFlags: Record<Feature, string> = {
  valpas: "valpas-feature",
  ilmoittaminen: "valpas-ilmoittaminen",
  mock: "valpas-mock",
}

const featureFlagEnabledValue = "enabled"

export type FeatureFlagEnablerProps = {
  features: Feature[]
  redirectTo: string
}

export const FeatureFlagEnabler = (props: FeatureFlagEnablerProps) => {
  useEffect(() => {
    props.features.map(enableFeature)
  })

  return <Redirect to={props.redirectTo} />
}

export const isFeatureFlagEnabled = (feature: Feature) =>
  runningLocally ||
  window.localStorage.getItem(featureFlags[feature]) === featureFlagEnabledValue

export const enableFeature = (feature: Feature) => {
  window.localStorage.setItem(featureFlags[feature], featureFlagEnabledValue)
}
