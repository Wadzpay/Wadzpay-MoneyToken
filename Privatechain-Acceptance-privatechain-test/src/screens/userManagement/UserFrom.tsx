import React, { useState, useEffect, useRef } from "react";
import { Row, Button, Form, Input, Space, Select, Col } from "antd";
import { ThreeDots } from "react-loader-spinner";
import TextArea from "antd/es/input/TextArea";
import { icons } from "src/utils/icons";
import {
  useGetRoles,
  useGetRolesByUsers,
  useGetDepartments,
} from "src/api/user";
import ViewPermissions from "./ViewPermissions";
import { dateTimeFormat } from "src/helpers/Utils";

type Props = {
  isError: boolean;
  formData: any;
  handleFormData: (value: boolean) => void;
  currentUserLevel: any;
  handleAddCancel?: () => void;
  editCurrentUser?: any;
  editingViewType?: string;
  handleSelectRoleAndUser?: () => void;
};

const options = [
  {
    key: 1,
    value: 1,
    label: <div>Existing Role</div>,
  },
  {
    key: 2,
    value: 2,
    label: <div>Existing User</div>,
  },
];

const UserFrom: React.FC<Props> = ({
  isError,
  formData,
  handleFormData,
  currentUserLevel,
  handleAddCancel,
  editCurrentUser,
  editingViewType,
  handleSelectRoleAndUser,
}: Props) => {
  const [form] = Form.useForm();
  const selectRef = useRef<any>(null);
  const [departments, setDepartments] = useState<any>([]);
  const [viewPermissionsData, setViewPermissionsData] = useState<any>([]);
  const [isSelectChange, setIsSelectChange] = useState<boolean>(false);

  // get departments API
  const {
    data: getDepartments,
    isFetching: isFetchingDepartments,
    error: departmentsError,
  } = useGetDepartments();

  // get roles API
  const {
    data: getRoles,
    error: usersError,
    refetch: refetchRoles,
  } = useGetRoles(`currentLevel=${currentUserLevel?.levelId}`);

  // get roles by users API
  const {
    data: getRolesByUsers,
    error: getRolesByUsersError,
    refetch: refetchRolesByUsers,
  } = useGetRolesByUsers(`currentLevel=${currentUserLevel?.levelId}`);

  const handleViewPermissions = (roleId: number) => {
    const modules: any =
      formData.assignRoleFrom === 1 ? getRoles : getRolesByUsers;
    const roleModuleList =
      modules?.find((role: any) => role.roleId === roleId)?.roleModuleList ||
      [];

    setViewPermissionsData(roleModuleList);
  };

  const onViewPremissionsClose = () => {
    setViewPermissionsData([]);
  };

  useEffect(() => {
    if (currentUserLevel) {
      // get roles and roles by users
      refetchRoles();
      refetchRolesByUsers();
    }
  }, [currentUserLevel]);

  useEffect(() => {
    if (getDepartments) {
      setDepartments(
        getDepartments?.map(({ departmentId, departmentName }: any) => ({
          key: departmentId,
          value: departmentId,
          label: <div>{departmentName}</div>,
        }))
      );
    }
  }, [getDepartments]);

  useEffect(() => {
    if (formData?.userMobile) {
      const numericValue = formData?.userMobile.replace(/[^+\d]/g, "");

      // Update only if the numeric value has changed
      if (numericValue !== formData.userMobile) {
        handleFormData({
          ...formData,
          userMobile: numericValue,
        });
      }
    }
  }, [formData?.userMobile]);

  // useEffect(() => {
  //   if (isSelectChange) {
  //     console.log(">>>>>>>> mohit isSelectChange");
  //     () => handleViewEditCancel;
  //   }
  // }, [isSelectChange]);

  return (
    <>
      {!editCurrentUser ? (
        <div style={{ paddingTop: "16px" }}>
          <div className="col-7 ms-3">
            <Row gutter={[16, 16]}>
              <Col xs={24} md={12}>
                <Form.Item
                  label="User Name"
                  name="userName"
                  rules={[
                    {
                      required: true,
                      message: "Please enter user name",
                    },
                  ]}
                >
                  <Input
                    status={
                      isError && !formData?.userName?.trim() ? "error" : ""
                    }
                    className="input-field"
                    placeholder="Enter User Name"
                    onChange={(e) =>
                      handleFormData({
                        ...formData,
                        userName: e.target.value,
                      })
                    }
                    onKeyPress={(e) => {
                      if (!formData?.userName?.trim() && e.key === " ") {
                        e.preventDefault();
                      }
                    }}
                  />
                </Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item
                  label="User ID"
                  name="userId"
                  rules={[
                    {
                      required: true,
                      message: "Please enter user ID",
                    },
                  ]}
                >
                  <Input
                    status={
                      isError && !formData?.userPreferenceId?.trim()
                        ? "error"
                        : ""
                    }
                    className="input-field"
                    placeholder="Enter user ID"
                    onChange={(e) =>
                      handleFormData({
                        ...formData,
                        userPreferenceId: e.target.value,
                      })
                    }
                    onKeyPress={(e) => {
                      if (
                        !formData?.userPreferenceId?.trim() &&
                        e.key === " "
                      ) {
                        e.preventDefault();
                      }
                    }}
                  />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={[16, 16]}>
              <Col xs={24} md={12}>
                <Form.Item
                  label="Email ID"
                  name="userEmail"
                  rules={[
                    {
                      required: true,
                      message: "Please enter email ID",
                    },
                  ]}
                >
                  <Input
                    status={
                      isError && !formData?.userEmail?.trim() ? "error" : ""
                    }
                    className="input-field"
                    placeholder="Enter email id"
                    onChange={(e) =>
                      handleFormData({
                        ...formData,
                        userEmail: e.target.value,
                      })
                    }
                    onKeyPress={(e) => {
                      if (!formData?.userEmail?.trim() && e.key === " ") {
                        e.preventDefault();
                      }
                    }}
                  />
                </Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item
                  label={
                    <>
                      <label htmlFor="code">
                        Code
                        <span
                          style={{
                            color: "red",
                            marginLeft: "4px",
                          }}
                        >
                          *
                        </span>
                      </label>
                      <label
                        htmlFor="userMobile"
                        style={{ marginLeft: "70px" }}
                      >
                        Mobile Number
                        <span
                          style={{
                            color: "red",
                            marginLeft: "4px",
                          }}
                        >
                          *
                        </span>
                      </label>
                    </>
                  }
                >
                  <Space.Compact>
                    <Form.Item
                      name={["mobileNumber", "countryCode"]}
                      noStyle
                      rules={[{ required: true, message: "Enter Code" }]}
                    >
                      <Select
                        className="countryCode"
                        style={{
                          width: "202px",
                        }}
                        placeholder="Select"
                        options={[
                          {
                            value: "+91",
                            label: "+91",
                          },
                          {
                            value: "+1",
                            label: "+1",
                          },
                        ]}
                        onChange={(value) =>
                          handleFormData({
                            ...formData,
                            countryCode: value,
                          })
                        }
                        status={
                          isError && !formData?.countryCode?.trim()
                            ? "error"
                            : ""
                        }
                      />
                    </Form.Item>
                    <Form.Item
                      name={["mobileNumber", "userMobile"]}
                      noStyle
                      rules={[
                        { required: true, message: "Enter mobile number" },
                      ]}
                    >
                      <Input
                        className="userMobile"
                        style={{ width: "100%" }}
                        placeholder="Enter mobile number"
                        onChange={(e) =>
                          handleFormData({
                            ...formData,
                            userMobile: e.target.value,
                          })
                        }
                        onKeyPress={(event) => {
                          if (!/[0-9]/.test(event.key)) {
                            event.preventDefault();
                          }
                        }}
                        status={
                          isError && !formData?.userMobile?.trim()
                            ? "error"
                            : ""
                        }
                        pattern="[0-9]*"
                      />
                    </Form.Item>
                  </Space.Compact>
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={[16, 16]}>
              <Col xs={24} md={12}>
                <Form.Item label="Designation" name="designation">
                  <Input
                    className="input-field"
                    placeholder="Enter Designation"
                    onChange={(e) =>
                      handleFormData({
                        ...formData,
                        designation: e.target.value,
                      })
                    }
                    onKeyPress={(e) => {
                      if (!formData?.designation?.trim() && e.key === " ") {
                        e.preventDefault();
                      }
                    }}
                  />
                </Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item label="Department" name="department">
                  <Select
                    placeholder="Select"
                    className="input-field"
                    onChange={(value) =>
                      handleFormData({
                        ...formData,
                        departmentId: value,
                      })
                    }
                  >
                    {departments?.map((option: any) => (
                      <Select.Option
                        className="options"
                        key={option.value}
                        value={option.value}
                      >
                        {option.label}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={[16, 16]}>
              <Col xs={24} md={12}>
                <Form.Item
                  label="Assign Role From"
                  name="assignRoleFrom"
                  rules={[
                    {
                      required: true,
                      message: "Please select role from",
                    },
                  ]}
                >
                  <Select
                    status={
                      formData?.assignRoleFrom == 0 && isError ? "error" : ""
                    }
                    placeholder="Select"
                    className="input-field"
                    onChange={(value) =>
                      handleFormData({
                        ...formData,
                        assignRoleFrom: value,
                        roleId: 0,
                      })
                    }
                    id="roleOptions"
                  >
                    {options.map((option) => (
                      <Select.Option
                        className="options"
                        key={option.value}
                        value={option.value}
                      >
                        {option.label}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
              {formData.assignRoleFrom !== 0 && (
                <>
                  <Col xs={24} md={12} style={{ position: "relative" }}>
                    {formData.assignRoleFrom === 1 && (
                      <Form.Item
                        label="Select User Role"
                        name="userRole"
                        rules={[
                          {
                            required: true,
                            message: "Please select user role",
                          },
                        ]}
                      >
                        <Select
                          status={
                            isError && formData?.roleId === 0 ? "error" : ""
                          }
                          placeholder="Select"
                          className="input-field"
                          showSearch
                          filterOption={(input, option: any) =>
                            option?.children
                              ?.toLowerCase()
                              .includes(input.toLowerCase())
                          }
                          onChange={(value, option: any) =>
                            handleFormData({
                              ...formData,
                              roleId: value,
                              roleName: option.roleName,
                            })
                          }
                        >
                          {getRoles?.map((item: any) => {
                            return (
                              <Select.Option
                                key={item?.roleId}
                                value={item?.roleId}
                                roleName={item?.roleName}
                              >
                                {item?.roleName}
                              </Select.Option>
                            );
                          })}
                        </Select>
                      </Form.Item>
                    )}
                    {formData.assignRoleFrom === 2 && (
                      <Form.Item
                        label="Search User Name"
                        name="userRole"
                        rules={[
                          {
                            required: true,
                            message: "Please select user",
                          },
                        ]}
                      >
                        <Select
                          status={
                            isError && formData?.roleFromUserId === null
                              ? "error"
                              : ""
                          }
                          placeholder="Select"
                          className="input-field"
                          showSearch
                          filterOption={(input, option: any) => {
                            // Combine userName, userPreferenceId, and roleName into a single string
                            const combinedText =
                              `${option.userName} (${option.userPreferenceId}) ${option.roleName}`.toLowerCase();
                            return combinedText.includes(input.toLowerCase());
                          }}
                          onChange={(value, option: any) =>
                            handleFormData({
                              ...formData,
                              roleFromUserId: value,
                              roleUserName: option.userName,
                              roleUserPreferenceId: option.userPreferenceId,
                              roleId: option.roleId,
                              roleName: option.roleName,
                            })
                          }
                          value={
                            formData?.roleUserName
                              ? `${formData.roleUserName} (${formData.roleUserPreferenceId}) - ${formData.roleName}`
                              : "Select"
                          }
                        >
                          {getRolesByUsers?.map((item: any) => (
                            <Select.Option
                              key={item?.userId}
                              value={item?.userId}
                              userPreferenceId={item?.userPreferenceId}
                              userName={item?.userName}
                              roleId={item?.roleId}
                              roleName={item?.roleName}
                            >
                              <div>{`${item?.userName} (${item?.userPreferenceId})`}</div>
                              <div>{`${item?.roleName}`}</div>
                            </Select.Option>
                          ))}
                        </Select>
                      </Form.Item>
                    )}
                    {formData?.roleId !== 0 && (
                      <Button
                        style={{
                          border: "none",
                          position: "absolute",
                          top: "28px",
                          right: "-150px",
                        }}
                        type="link"
                        onClick={() => handleViewPermissions(formData?.roleId)}
                      >
                        <img
                          src="/images/view_icon.svg"
                          style={{ marginTop: "-2px" }}
                        ></img>{" "}
                        &nbsp;View Permissions
                      </Button>
                    )}
                  </Col>
                </>
              )}
            </Row>
            <Row gutter={[16, 16]}>
              <Col xs={24} md={24}>
                <Form.Item label="Comment" name={"userComments"}>
                  <TextArea
                    rows={4}
                    className="input-field"
                    showCount
                    maxLength={250}
                    placeholder="Write your comments here"
                    onChange={(e) =>
                      handleFormData({
                        ...formData,
                        comment: e.target.value,
                      })
                    }
                    onKeyPress={(e) => {
                      if (!formData?.comment?.trim() && e.key === " ") {
                        e.preventDefault();
                      }
                    }}
                  />
                </Form.Item>
              </Col>
            </Row>
          </div>
        </div>
      ) : (
        /* Edit From */
        <div className="view-edit">
          <div style={{ paddingTop: "16px" }}>
            <div className="col-7 ms-3">
              <Row gutter={[16, 16]}>
                <Col xs={24} md={12}>
                  <Form.Item
                    label={
                      <>
                        User Name{" "}
                        {formData?.userName?.trim() && (
                          <span className="requiredIcon">*</span>
                        )}
                      </>
                    }
                    name="userName"
                    rules={[
                      {
                        required: !formData?.userName?.trim(),
                        message: "Please enter user name",
                      },
                    ]}
                  >
                    <Input
                      status={
                        isError && !formData?.userName?.trim() ? "error" : ""
                      }
                      defaultValue={editCurrentUser?.userName}
                      value={editCurrentUser?.userName}
                      className="input-field"
                      placeholder="Enter User Name"
                      disabled={editingViewType === "view" ? true : false}
                      onChange={(e) =>
                        handleFormData({
                          ...formData,
                          userName: e.target.value,
                        })
                      }
                      onKeyPress={(e) => {
                        if (!formData?.userName?.trim() && e.key === " ") {
                          e.preventDefault();
                        }
                      }}
                    />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    label={
                      <>
                        User ID{" "}
                        {formData?.userPreferenceId?.trim() && (
                          <span className="requiredIcon">*</span>
                        )}
                      </>
                    }
                    name="userPreferenceId"
                    rules={[
                      {
                        required: !formData?.userPreferenceId?.trim(),
                        message: "Please enter user ID",
                      },
                    ]}
                  >
                    <Input
                      status={
                        isError && !formData?.userPreferenceId?.trim()
                          ? "error"
                          : ""
                      }
                      defaultValue={editCurrentUser?.userPreferenceId}
                      value={editCurrentUser?.userPreferenceId}
                      className="input-field"
                      placeholder="Enter user ID"
                      disabled={editingViewType === "view" ? true : false}
                      onChange={(e) =>
                        handleFormData({
                          ...formData,
                          userPreferenceId: e.target.value,
                        })
                      }
                      onKeyPress={(e) => {
                        if (
                          !formData?.userPreferenceId?.trim() &&
                          e.key === " "
                        ) {
                          e.preventDefault();
                        }
                      }}
                    />
                  </Form.Item>
                </Col>
              </Row>
              <Row gutter={[16, 16]}>
                <Col xs={24} md={12}>
                  <Form.Item
                    label={
                      <>
                        Email ID{" "}
                        {formData?.userEmail?.trim() && (
                          <span className="requiredIcon">*</span>
                        )}
                      </>
                    }
                    name="userEmail"
                    rules={[
                      {
                        required: !formData?.userEmail?.trim(),
                        message: "Please enter email ID",
                      },
                    ]}
                  >
                    <Input
                      defaultValue={editCurrentUser?.userEmail}
                      value={editCurrentUser?.userEmail}
                      status={
                        isError && !formData?.userEmail?.trim() ? "error" : ""
                      }
                      className="input-field"
                      placeholder="Enter email id"
                      onChange={(e) =>
                        handleFormData({
                          ...formData,
                          userEmail: e.target.value,
                        })
                      }
                      onKeyPress={(e) => {
                        if (!formData?.userEmail?.trim() && e.key === " ") {
                          e.preventDefault();
                        }
                      }}
                      disabled={editingViewType === "view" ? true : false}
                    />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    label={
                      <>
                        <label htmlFor="code">
                          Code
                          <span
                            style={{
                              color: "red",
                              marginLeft: "4px",
                            }}
                          >
                            *
                          </span>
                        </label>
                        <label
                          htmlFor="userMobile"
                          style={{ marginLeft: "70px" }}
                        >
                          Mobile Number
                          <span
                            style={{
                              color: "red",
                              marginLeft: "4px",
                            }}
                          >
                            *
                          </span>
                        </label>
                      </>
                    }
                  >
                    <Space.Compact>
                      <Form.Item
                        name={["mobileNumber", "countryCode"]}
                        noStyle
                        rules={[
                          {
                            required: isError && !formData?.countryCode?.trim(),
                            message: "Enter Code",
                          },
                        ]}
                      >
                        <Select
                          className="countryCode"
                          style={{
                            width: "202px",
                          }}
                          placeholder="Select"
                          options={[
                            {
                              value: "+91",
                              label: "+91",
                            },
                            {
                              value: "+1",
                              label: "+1",
                            },
                          ]}
                          onChange={(value) =>
                            handleFormData({
                              ...formData,
                              countryCode: value,
                            })
                          }
                          status={
                            isError && !formData?.countryCode?.trim()
                              ? "error"
                              : ""
                          }
                          defaultValue={editCurrentUser?.countryCode}
                          value={editCurrentUser?.countryCode}
                          disabled={editingViewType === "view" ? true : false}
                        />
                      </Form.Item>
                      <Form.Item
                        name={["mobileNumber", "userMobile"]}
                        noStyle
                        rules={[
                          {
                            required: isError && !formData?.userMobile?.trim(),
                            message: "Enter mobile number",
                          },
                        ]}
                      >
                        <Input
                          className="userMobile"
                          style={{ width: "100%" }}
                          placeholder="Enter mobile number"
                          defaultValue={editCurrentUser?.userMobile}
                          value={editCurrentUser?.userMobile}
                          onChange={(e) =>
                            handleFormData({
                              ...formData,
                              userMobile: e.target.value,
                            })
                          }
                          onKeyPress={(event) => {
                            if (!/[0-9]/.test(event.key)) {
                              event.preventDefault();
                            }
                          }}
                          status={
                            isError && !formData?.userMobile?.trim()
                              ? "error"
                              : ""
                          }
                          disabled={editingViewType === "view" ? true : false}
                          pattern="[0-9]*"
                        />
                      </Form.Item>
                    </Space.Compact>
                  </Form.Item>
                </Col>
              </Row>
              <Row gutter={[16, 16]}>
                <Col xs={24} md={12}>
                  <Form.Item label="Designation" name="designation">
                    <Input
                      defaultValue={editCurrentUser?.designation}
                      value={editCurrentUser?.designation}
                      className="input-field"
                      placeholder="Enter Designation"
                      onChange={(e) =>
                        handleFormData({
                          ...formData,
                          designation: e.target.value,
                        })
                      }
                      onKeyPress={(e) => {
                        if (!formData?.designation?.trim() && e.key === " ") {
                          e.preventDefault();
                        }
                      }}
                      disabled={editingViewType === "view" ? true : false}
                    />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item label="Department" name="department">
                    <Select
                      placeholder="Select"
                      className="input-field"
                      onChange={(value) =>
                        handleFormData({
                          ...formData,
                          departmentId: value,
                        })
                      }
                      disabled={editingViewType === "view" ? true : false}
                      defaultValue={editCurrentUser?.departmentId}
                    >
                      {departments?.map((option: any) => (
                        <Select.Option
                          className="options"
                          key={option.value}
                          value={option.value}
                        >
                          {option.label}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                </Col>
              </Row>
              <Row gutter={[16, 16]}>
                <>
                  <Col xs={24} md={12}>
                    <Form.Item
                      label={
                        <>
                          Assign Role From{" "}
                          {<span className="requiredIcon">*</span>}
                        </>
                      }
                      name="assignRoleFrom"
                    >
                      <Select
                        status={
                          formData?.assignRoleFrom == 0 && isError
                            ? "error"
                            : ""
                        }
                        placeholder="Select"
                        className="input-field"
                        onChange={(value) => {
                          handleFormData({
                            ...formData,
                            assignRoleFrom: value,
                            roleId: null,
                            roleName: "",
                            roleFromUserId: null,
                            roleUserName: "",
                            roleUserPreferenceId: "",
                          });
                          handleSelectRoleAndUser?.();
                        }}
                        id="roleOptions"
                        disabled={editingViewType === "view" ? true : false}
                        defaultValue={
                          editCurrentUser?.roleFromUserId == null ? 1 : 2
                        }
                      >
                        {options.map((option) => (
                          <Select.Option
                            className="options"
                            key={option.value}
                            value={option.value}
                          >
                            {option.label}
                          </Select.Option>
                        ))}
                      </Select>
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={12} style={{ position: "relative" }}>
                    {formData.assignRoleFrom === 1 && (
                      <Form.Item
                        label={
                          <>
                            Select User Role{" "}
                            {formData?.assignedRoleId !== 0 && (
                              <span className="requiredIcon">*</span>
                            )}
                          </>
                        }
                        name="userRole"
                        rules={[
                          {
                            required:
                              formData?.assignedRoleId === 0 ? true : false,
                            message: "Please select user role",
                          },
                        ]}
                      >
                        <Select
                          status={
                            isError && formData?.assignedRoleId === 0
                              ? "error"
                              : ""
                          }
                          ref={selectRef}
                          placeholder="Select"
                          className="input-field"
                          showSearch
                          filterOption={(input, option: any) =>
                            option?.children
                              ?.toLowerCase()
                              .includes(input.toLowerCase())
                          }
                          onChange={(value, option: any) =>
                            handleFormData({
                              ...formData,
                              roleId: value,
                              roleName: option.roleName,
                              roleFromUserId: null,
                              roleUserName: "",
                              roleUserPreferenceId: "",
                            })
                          }
                          defaultValue={formData?.roleId}
                          value={formData?.roleId}
                          disabled={editingViewType === "view" ? true : false}
                        >
                          {getRoles?.map((item: any) => {
                            return (
                              <Select.Option
                                key={item?.roleId}
                                value={item?.roleId}
                                roleName={item?.roleName}
                              >
                                {item?.roleName}
                              </Select.Option>
                            );
                          })}
                        </Select>
                      </Form.Item>
                    )}
                    {formData.assignRoleFrom === 2 && (
                      <Form.Item
                        label={
                          <>
                            Search User Name{" "}
                            {formData?.roleFromUserId !== null && (
                              <span className="requiredIcon">*</span>
                            )}
                          </>
                        }
                        name="roleFromUser"
                        rules={[
                          {
                            required:
                              formData?.roleFromUserId === null ? true : false,
                            message: "Please select user",
                          },
                        ]}
                      >
                        <Select
                          status={
                            isError && formData?.roleFromUserId === null
                              ? "error"
                              : ""
                          }
                          ref={selectRef}
                          placeholder="Select"
                          className="input-field"
                          showSearch
                          filterOption={(input, option: any) => {
                            // Combine userName, userPreferenceId, and roleName into a single string
                            const combinedText =
                              `${option.userName} (${option.userPreferenceId}) ${option.roleName}`.toLowerCase();
                            return combinedText.includes(input.toLowerCase());
                          }}
                          onChange={(value, option: any) =>
                            handleFormData({
                              ...formData,
                              roleFromUserId: value,
                              roleUserName: option.userName,
                              roleUserPreferenceId: option.userPreferenceId,
                              roleId: option.roleId,
                              roleName: option.roleName,
                            })
                          }
                          defaultValue={formData?.roleFromUserId}
                          value={
                            formData?.roleUserName
                              ? `${formData.roleUserName} (${formData.roleUserPreferenceId}) - ${formData.roleName}`
                              : "Select"
                          }
                          disabled={editingViewType === "view" ? true : false}
                        >
                          {getRolesByUsers
                            ?.filter(
                              (item: any) =>
                                item.userPreferenceId !==
                                editCurrentUser?.userPreferenceId
                            )
                            ?.map((item: any) => (
                              <Select.Option
                                key={item?.userId}
                                value={item?.userId}
                                userPreferenceId={item?.userPreferenceId}
                                userName={item?.userName}
                                roleId={item?.roleId}
                                roleName={item?.roleName}
                              >
                                <div>{`${item?.userName} (${item?.userPreferenceId})`}</div>
                                <div>{`${item?.roleName}`}</div>
                              </Select.Option>
                            ))}
                        </Select>
                      </Form.Item>
                    )}
                    <Button
                      style={{
                        border: "none",
                        position: "absolute",
                        top: "28px",
                        right: "-150px",
                      }}
                      type="link"
                      onClick={() => handleViewPermissions(formData?.roleId)}
                    >
                      <img
                        src="/images/view_icon.svg"
                        style={{ marginTop: "-2px" }}
                      ></img>{" "}
                      &nbsp;View Permissions
                    </Button>
                  </Col>
                </>
              </Row>
              {editingViewType === "view" &&
              editCurrentUser?.comment === null ? null : (
                <Row gutter={[16, 16]}>
                  <Col xs={24} sm={24} md={24} lg={24} xl={24}>
                    <label
                      style={{
                        marginBottom: "4px",
                        color: "#2d2d2d",
                        fontFamily: "Inter",
                        fontSize: "12px",
                      }}
                      htmlFor="userId"
                    >
                      Comment
                    </label>
                    <div className="comment">
                      {editingViewType === "edit" && (
                        <Form.Item
                          className="input-create-textArea"
                          name={"comment"}
                          key={"comment"}
                        >
                          <TextArea
                            onChange={(e) =>
                              handleFormData({
                                ...formData,
                                comment: e.target.value,
                              })
                            }
                            onKeyPress={(e) => {
                              if (!formData?.comment?.trim() && e.key === " ") {
                                e.preventDefault();
                              }
                            }}
                            className="textArea-custom"
                            showCount
                            maxLength={250}
                            placeholder="Write your comments here"
                            rows={4}
                          />
                        </Form.Item>
                      )}

                      {editCurrentUser?.comment?.length > 0 && (
                        <>
                          {editingViewType === "edit" && (
                            <hr
                              style={{
                                width: "98%",
                                marginLeft: "6px",
                                marginTop: "20px",
                                borderColor: "rgb(196, 196, 196)",
                              }}
                            />
                          )}
                          {editCurrentUser?.comment?.map((item: any) => (
                            <div key={item.commentDate}>
                              <div>
                                <span
                                  style={{
                                    fontSize: "12px",
                                    color: "#131313",
                                  }}
                                >
                                  <b>{item?.createdBy}</b>
                                </span>
                                <span
                                  style={{
                                    fontSize: "10px",
                                    color: "#131313",
                                    marginLeft: "6px",
                                  }}
                                >
                                  {item?.commentDate}
                                </span>
                              </div>
                              <p
                                style={{
                                  fontSize: "12px",
                                  color: "#131313",
                                  marginLeft: "6px",
                                  maxWidth: "100%",
                                  wordBreak: "break-word",
                                  whiteSpace: "normal",
                                }}
                              >
                                {item?.comment}
                              </p>
                            </div>
                          ))}
                        </>
                      )}
                    </div>
                  </Col>
                </Row>
              )}
            </div>
          </div>
        </div>
      )}

      {/* View Permissions */}
      <ViewPermissions
        open={viewPermissionsData.length > 0 ? true : false}
        onClose={onViewPremissionsClose}
        viewPermissionsModules={viewPermissionsData}
        role={formData.roleName}
      />
      <style>
        {`
         
        `}
      </style>
    </>
  );
};

export default UserFrom;
