import React from 'react'
import featureFlags from '../../featureFlags'

export const withFeatureFlag = (featureFlagName, FeatureComponent) => props => {
  const currentEnvFeatures = featureFlags[window.environment]
  const featureEnabled = currentEnvFeatures && currentEnvFeatures[featureFlagName]
  if (featureEnabled === undefined) {
    console.warn('Feature flag ' + featureFlagName + ' not found for env ' + window.environment)
  }
  FeatureComponent.displayName = `withFeatureFlag(${FeatureComponent.displayName})`
  return featureEnabled === true && <FeatureComponent {...props}/>
}
