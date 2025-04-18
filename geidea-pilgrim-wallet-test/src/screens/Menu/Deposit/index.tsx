import React, { useContext } from "react"

import FakeOnRamp from "./FakeOnRamp"
import OnRamp from "./OnRamp"

import { Modal, theme } from "~/components/ui"
import { isProd } from "~/utils"
import { EnvironmentContext } from "~/context"

type Props = {
  isVisible: boolean
  onDismiss: () => void
}

const Deposit: React.FC<Props> = ({ isVisible, onDismiss }: Props) => {
  const { isFakeOnRamp } = useContext(EnvironmentContext)
  const useFakeOnRamp = !isProd && isFakeOnRamp

  return (
    <Modal
      variant="bottom"
      title={useFakeOnRamp ? "On Ramp" : undefined}
      isVisible={isVisible}
      onDismiss={onDismiss}
      dismissButtonVariant="cancel"
      swipeDirection={useFakeOnRamp ? "down" : []}
      contentStyle={{ height: theme.modalFullScreenHeight }}
    >
      {useFakeOnRamp ? <FakeOnRamp /> : <OnRamp />}
    </Modal>
  )
}

export default Deposit
