import React, { useEffect, useState } from "react"
import { Popconfirm, Radio, notification, ConfigProvider } from "antd"
import { useDispatch, useSelector } from "react-redux"
import { ROOTSTATE } from "src/utils/modules"
import { useUpdateIssuanceDetails } from "src/api/user"
import { setSettingsConfig } from "src/utils/settingsSlice"

const P2pConfiguration: React.FC = () => {
  const dispatch = useDispatch()
  // get settings configuration
  const settingsConfig = useSelector((store: ROOTSTATE) => store.settingsConfig)

  const [status, setStatus] = useState<boolean>(settingsConfig.p2pTransfer)

  // update issuance p2p transfer status api
  const {
    mutate: updateIssuanceDetails,
    error,
    isSuccess
  } = useUpdateIssuanceDetails()

  // error while updating p2p transfer status
  useEffect(() => {
    if (error) {
      if (error) {
        notification["error"]({
          message: "An error occurred",
          description: error.message
        })
      }
    }
  }, [error])

  useEffect(() => {
    if (settingsConfig) {
      setStatus(settingsConfig.p2pTransfer)
    }
  }, [settingsConfig])

  // update p2p transfer status based on confirm status
  const confirmToggle = (isP2PEnabled: boolean) => {
    setStatus(isP2PEnabled)

    const requestParams: any = {
      isP2PEnabled
    }

    // Dispatch action to update settingsConfiguration
    dispatch(
      setSettingsConfig({
        ...settingsConfig,
        p2pTransfer: isP2PEnabled
      })
    )

    // update issuance p2p transfer status
    updateIssuanceDetails(requestParams)
  }

  return (
    <ConfigProvider>
      <div className="table-responsive" style={{ overflowX: "visible" }}>
        <>
          <div className="mt-3">
            <p>
              <b>P2P Funds Transfer </b>
            </p>
          </div>
          <div className="ml-4 mt-3 mb-3" data-testid="merchantPhoneNumber">
            <Radio.Group value={status}>
              <Popconfirm
                title={
                  status
                    ? "Are you sure to disable?"
                    : "Are you sure to enable?"
                }
                onConfirm={() => confirmToggle(!status)} // Toggle the status on confirmation
                okText="Yes"
                cancelText="No"
                disabled={status}
              >
                <Radio value={true}>Enable</Radio>
              </Popconfirm>
              <Popconfirm
                title={
                  status
                    ? "Are you sure to disable?"
                    : "Are you sure to enable?"
                }
                onConfirm={() => confirmToggle(!status)} // Toggle the status on confirmation
                okText="Yes"
                cancelText="No"
                disabled={!status ? true : false}
              >
                <Radio value={false}>Disable</Radio>
              </Popconfirm>
            </Radio.Group>
          </div>
        </>
      </div>
    </ConfigProvider>
  )
}

export default P2pConfiguration
