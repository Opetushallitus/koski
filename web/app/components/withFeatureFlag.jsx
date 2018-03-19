import React from 'react'

export const withFeatureFlag = (featureFlag, FeatureComponent) => props =>
  (document.domain !== PRODUCTION_DOMAIN || featureFlag === true) &&
  <FeatureComponent {...props}/>
