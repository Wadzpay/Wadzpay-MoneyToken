import React, { useState, useEffect } from "react"
import { AiOutlineSearch } from "react-icons/ai"
import { Button } from "react-bootstrap"
import { input } from "src/i18next-parser.config"
import DOMPurify from "dompurify"
type Props = {
  title: string
  searchDefaultTxt: string
  value?: string
  setFilterSearch: (value?: string) => void
  serachData?: () => void | undefined
  autoFocusRequired?: boolean
  inputRef?: any
}

export const FilterSearch: React.FC<Props> = ({
  title,
  searchDefaultTxt,
  value,
  setFilterSearch,
  serachData,
  autoFocusRequired,
  inputRef
}: Props) => {
  const [query, setQuery] = useState(value)
  const [searchErr, setSearchErr] = useState<string | null>(null)

  useEffect(() => {
    if (query?.length == 0) {
      setFilterSearch("")
    } else {
      setFilterSearch(query)
    }
    setSearchErr(null)
  }, [query])

  const handleChange = (searchValue?: any) => {
    const sanitizedInput = DOMPurify.sanitize(searchValue.value)
    setQuery(sanitizedInput)
  }

  return (
    <>
      <div className="search-addon right-addon">
        {/* <Button
            className="btn wdz-btn-primary me-1"
            variant="warning"
            onClick={() => serachData()}
          >
            Search
        </Button> */}
        <div className="search-crypto-input">
          <input
            type="search"            
            value={query || ""}
            autoFocus={autoFocusRequired}
            placeholder={searchDefaultTxt}
            aria-autocomplete='both'
            aria-haspopup="false"
            ref={inputRef}
            autoComplete="off"
            onChange={(e: React.ChangeEvent<Element>) => handleChange(e.target)}
            title={searchDefaultTxt}
            style={{ width: "90%" }}
            
          />
          {query ? (
            <button className="search-clear" onClick={() => handleChange("")}>
              x
            </button>
          ) : null}
          {value === "" || undefined ? (
            <AiOutlineSearch
              className="search-icon"
              onClick={(e: React.MouseEvent<Element>) => {
                e.stopPropagation()
              }}
            />
          ) : null}
        </div>
        {searchErr ? (
          <span style={{ color: "red", marginLeft: "10px" }}>{searchErr}</span>
        ) : null}
      </div>
    </>
  )
}
