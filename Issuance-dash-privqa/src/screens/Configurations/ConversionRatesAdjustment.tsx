import React from "react"
import { useSelector } from "react-redux"
import { ROOTSTATE } from "src/utils/modules"
import "../Configurations/Configurations.scss"
import Markup from "src/screens/Configurations/conversionRatesAdjustments/Markup"
import Markdown from "src/screens/Configurations/conversionRatesAdjustments/Markdown"

import PageHeading from "../../components/ui/PageHeading"

const ConversionRatesAdjustment: React.FC = () => {
  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  return (
    <div className="configurations">
      <PageHeading title="Conversion Rates Adjustment" />
      <Markup buttonColor={buttonColor} />
      <Markdown buttonColor={buttonColor} />
    </div>
  )
}

export default ConversionRatesAdjustment
