import React from "react"
import { Button } from "react-bootstrap"

interface Props {
  paginate: (pageNumber: number) => void
  currPage?: number
  nextPage?: boolean
  loading?: boolean
  alltransactionlength: number
}

function findLastPageNo(data: any) {
  if (data.length === 0) {
    return 0
  }

  const subtract = data / 10

  if (`${subtract}`.includes(".")) {
    const num = parseInt(`${subtract}`)
    return num + 1
  }

  return subtract
}

const Pagination: React.FC<Props> = ({
  paginate,
  currPage,
  nextPage,
  loading,
  alltransactionlength
}: Props) => {
  return (
    <>
      {currPage && (
        <div data-testid="paginate">
          <Button
            variant="outline-primary"
            disabled={loading || currPage === 1}
            onClick={() => paginate(1)}
            style={{ marginRight: "10px" }}
          >
            First
          </Button>
          <Button
            variant="outline-primary"
            disabled={loading || currPage === 1}
            onClick={() =>
              currPage >= 2 ? paginate(currPage - 1) : paginate(currPage)
            }
          >
            &lt;
          </Button>
          <span
            className="d-inline-block p-3 text-center"
            style={{ minWidth: "3em" }}
          >
            {currPage}
          </span>
          <Button
            variant="outline-primary"
            disabled={loading || nextPage}
            onClick={() => paginate(currPage + 1)}
          >
            &gt;
          </Button>
          <Button
            variant="outline-primary"
            disabled={loading || nextPage}
            onClick={() => paginate(findLastPageNo(alltransactionlength) || 1)}
            style={{ marginLeft: "10px" }}
          >
            Last
          </Button>
        </div>
      )}
    </>
  )
}

export default Pagination
