import { render, screen } from "@testing-library/react"
import "@testing-library/jest-dom"
import P2pConfiguration from "../../screens/Settings/p2pConfiguration/P2pConfiguration"

test("Should load P2pConfiguration component", () => {
  render(<P2pConfiguration />)

  const heading = screen.getByRole("heading")

  expect(heading).toBeInTheDocument()
})
