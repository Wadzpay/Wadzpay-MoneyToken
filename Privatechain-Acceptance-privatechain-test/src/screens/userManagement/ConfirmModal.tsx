import React from "react";
import { Modal, Button } from "antd";
import { ThreeDots } from "react-loader-spinner";
import styled from "styled-components";

type Props = {
  isModal: boolean;
  handleCallback: (userId: number | null) => void;
  deactiveUserDetails: any;
  loading?: boolean;
};

const Flexbox = styled.div`
  font-family: sans-serif;
  text-align: center;
  display: flex;
  flex-direction: column;
  background: palevioletred;
  border-radius: 5px;
`;

const ConfirmModal: React.FC<Props> = ({
  isModal,
  handleCallback,
  deactiveUserDetails,
  loading,
}: Props) => {
  return (
    <Flexbox>
      <Modal
        className="custom-modal"
        title={
          <>
            Deactivate User
            <hr />
          </>
        }
        centered
        open={isModal}
        onOk={() => handleCallback(null)}
        onCancel={() => handleCallback(null)} // Ensure modal closes on cancel
        cancelButtonProps={{ style: { display: "none" } }}
        width={537}
        footer={
          <>
            <Button
              onClick={() => handleCallback(null)}
              className="cancel-button"
            >
              Cancel
            </Button>
            &nbsp;
            <Button
              className="ok-button"
              onClick={() => handleCallback(deactiveUserDetails.userId)}
              disabled={loading}
            >
              <div style={{ display: "flex" }}>
                {"Yes, Deactivate"}&nbsp;
                {loading && (
                  <ThreeDots
                    height="25"
                    width="25"
                    color="#000"
                    ariaLabel="three-dots-loading"
                    visible={true}
                  />
                )}
              </div>
            </Button>
          </>
        }
      >
        <div
          style={{
            fontSize: "14px",
            fontWeight: "600",
            marginBottom: "8px",
            color: "#1E1E1E",
          }}
        >
          {`Are you sure you want to Deactivate "${deactiveUserDetails.userName}" User (${deactiveUserDetails.userPreferenceId})?`}
        </div>
        <hr style={{ marginTop: "16px" }} />
      </Modal>
    </Flexbox>
  );
};

export default ConfirmModal;
