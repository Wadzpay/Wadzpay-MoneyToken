import React from "react";
import { Drawer } from "antd";
import { Form } from "react-bootstrap";

type Props = {
  open: boolean;
  onClose: (value: boolean) => void;
  viewPermissionsModules: any;
  role: string;
};

const ViewPermissions: React.FC<Props> = ({
  open,
  onClose,
  viewPermissionsModules,
  role,
}: Props) => {
  return (
    <Drawer
      title={`Role Permissions ${role !== "" ? "-" : ""} ${role}`}
      open={open}
      onClose={() => onClose(false)}
      className="viewPermissions"
    >
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <b>Modules</b>
        <b>Access</b>
      </div>
      <hr />
      {viewPermissionsModules?.map(({ moduleName }: any) => {
        return (
          <>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <span>{moduleName}</span>
              <span>
                <Form.Check type="checkbox" checked={true} />
              </span>
            </div>
            <hr />
          </>
        );
      })}
    </Drawer>
  );
};

export default ViewPermissions;
