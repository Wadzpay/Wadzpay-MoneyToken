import React from "react"
import { Modal, Button } from "antd"
import styled from "styled-components"

type Props = {
  isModal: boolean
  setIsModal?: () => void
  institutionsList?: any
  languageName?: string
}

const Flexbox = styled.div`
  font-family: sans-serif;
  text-align: center;
  display: flex;
  flex-direction: column;
  background: palevioletred;
  border-radius: 5px;
`

const DisableLanguageInfo: React.FC<Props> = ({
  isModal,
  setIsModal,
  institutionsList,
  languageName
}: Props) => {
  return (
    <Flexbox>
      <Modal
        className="custom-modal"
        title={
          <>
            <img
              src="/images/info-icon.svg"
              alt="Info Icon"
              style={{ marginRight: 8, marginTop: "-2px" }}
            />
            Disable Language Info
            <hr />
          </>
        }
        centered
        open={isModal}
        onOk={setIsModal}
        onCancel={setIsModal} // Ensure modal closes on cancel
        cancelButtonProps={{ style: { display: "none" } }}
        width={600}
        footer={
          <button onClick={setIsModal} className="ok-button">
            Ok
          </button>
        }
      >
        <div
          style={{
            fontSize: "12px",
            color: "#1E1E1E",
            marginTop: "-5px",
            marginBottom: "10px"
          }}
        >
          {`There are ${institutionsList.length} institutions that still rely on this ‘${languageName}’ language, and due to this, it's not possible to disable it. Instead, you need to inform these institutions to update their language settings to disable the language, which will require their cooperation and coordination.`}
        </div>

        <div
          style={{ fontSize: "14px", fontWeight: "600", marginBottom: "8px" }}
        >
          {`Please find the below institutions are using this ${languageName} language.`}
        </div>
        {institutionsList.map((institution: string, index: number) => {
          return (
            <Button
              key={institution}
              className={
                index == 0 || index % 2 === 0
                  ? "landuage-disabled mb-1 me-2"
                  : "landuage-disabled mb-1"
              }
            >
              {institution}
            </Button>
          )
        })}
        <hr style={{ marginTop: "10px" }} />
      </Modal>
    </Flexbox>
  )
}

export default DisableLanguageInfo
