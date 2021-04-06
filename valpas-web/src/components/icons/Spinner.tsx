import React from "react"
import "./Spinner.less"

export const Spinner = () => (
  <div className="spinner">
    <div className="spinner-circle spinner-circle-green" />
    <div className="spinner-circle spinner-circle-blue" />
  </div>
)

export const LoadingModal = () => (
  <div className="loading-modal">
    <Spinner />
  </div>
)
