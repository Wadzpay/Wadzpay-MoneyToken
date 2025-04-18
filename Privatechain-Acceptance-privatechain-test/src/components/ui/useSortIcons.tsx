import React, { useState } from "react"
import { useTranslation } from "react-i18next"
import {
  AiOutlineSortAscending,
  AiOutlineArrowUp,
  AiOutlineArrowDown
} from "react-icons/ai"
import { BsArrowDownUp } from "react-icons/bs"

interface Props {
  sortData: (value: string) => void
  sortedField: string
  sortedDirection: string
}
interface SortIconsProps {
  element: string
  sortField: string
  wordLabel: string
}

export const useSortIcons = (props: Props) => {
  const { sortData, sortedField, sortedDirection } = props

  const { t } = useTranslation()

  const SortIcons: React.FC<SortIconsProps> = ({
    element,
    sortField,
    wordLabel
  }) => {
    const [hover, setHover] = useState(false)
    const [hoverElement, setHoverElement] = useState("")

    return (
      <th
        className={`pe-0 ${hover && hoverElement === element && "bg-light"}`}
        style={{ cursor: "default" }}
        scope="col"
        onClick={() => sortData(sortField)}
        // onMouseOver={() => {
        //   setHover(true)
        //   setHoverElement(element)
        // }}
        // onMouseLeave={() => {
        //   setHover(false)
        // }}
      >
        <div
          className={
            wordLabel == "Order Digital Amount"
              ? "d-flex flex-row justify-content-end"
              : "d-flex flex-row justify-content-between"
          }
        >
          <div
            className={wordLabel == "Order Digital Amount" ? "txt-right" : ""}
          >
            {t(wordLabel)}
          </div>
          <div style={{ minWidth: "1em", marginLeft: "5px" }}>
            {/* {hover && hoverElement === element && sortedField !== sortField && (
              <AiOutlineSortAscending />
            )} */}
            {sortedField === sortField ? (
              sortedDirection === "ASC" ? (
                <AiOutlineArrowUp />
              ) : (
                <AiOutlineArrowDown />
              )
            ) : (
              <BsArrowDownUp />
            )}
          </div>
        </div>
      </th>
    )
  }

  return SortIcons
}
