import React from 'react'

export const withFeatureFlag = (featureFlag, FeatureComponent) => props =>
  featureFlag === true && <FeatureComponent {...props}/>
