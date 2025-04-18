import { t } from "i18next"
import React, { useState } from "react"
import { Dropdown } from "react-bootstrap"
import { AiFillCloseCircle } from "react-icons/ai"

type Props = {
  title: string
  value?: string
  items: { label: string; value: string; title?: string }[]
  setFilter: (value?: string) => void
}
export const FilterDropdown: React.FC<Props> = ({
  title,
  value,
  items,
  setFilter
}: Props) => {
  const [showDropdown, setShowDropdown] = useState(false)

  const updateFilter = (e: React.MouseEvent<Element>, filterValue?: string) => {
    e.preventDefault()
    setFilter(filterValue)
    setShowDropdown(false)
  }

  return (
    <>
      {(!value || showDropdown) && (
        <Dropdown
          className="me-2"
          onClick={() => setShowDropdown(true)}
          onToggle={(isOpen: boolean) => {
            if (!isOpen) {
              setShowDropdown(false)
            }
          }}
        >
          <Dropdown.Toggle variant="outline-secondary transaction-filter-dropdown">
            {t(title)}
          </Dropdown.Toggle>
          <Dropdown.Menu show={showDropdown}>
            {items.map((item) => (
              <Dropdown.Item
                key={item.value}
                active={item.value === value}
                onClick={(e: React.MouseEvent<Element>) =>
                  updateFilter(e, item.value)
                }
                title={item.title}
              >
                {item.label}
              </Dropdown.Item>
            ))}
          </Dropdown.Menu>
        </Dropdown>
      )}
      {value && !showDropdown && (
        <div>
          <button
            type="button"
            className="me-2 btn btn-secondary"
            onClick={() => setShowDropdown(true)}
          >
            {`${items
              .filter((item) => item.value === value)
              .map((item) => item.label)} `}
            <AiFillCloseCircle
              onClick={(e: React.MouseEvent<Element>) => {
                e.stopPropagation()
                setFilter(undefined)
              }}
            />
          </button>
        </div>
      )}
    </>
  )
}
