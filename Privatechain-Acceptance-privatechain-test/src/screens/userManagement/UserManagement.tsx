import React, { useEffect, useRef, useState, useCallback } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Button,
  Col,
  Dropdown,
  Form,
  Input,
  InputRef,
  MenuProps,
  Row,
  Select,
  Space,
  Table,
  TableColumnType,
  Typography,
  Skeleton,
  notification,
  DatePicker,
  Empty,
  Pagination,
  Divider,
  Tooltip,
} from "antd";
import _ from "lodash";
import { SearchOutlined, CloseOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import { FilterDropdownProps } from "antd/es/table/interface";
import { ThreeDots } from "react-loader-spinner";
import "../roleManagement/index.css";
import "./index.scss";
import { Level, UserCreate, UserUpdate, UserList } from "src/api/models";
import {
  useGetLevelList,
  useCreateUser,
  useGetUserManagementList,
  useUpdateUser,
  useDeActivateUser,
} from "src/api/user";
import { RouteType } from "src/constants/routeTypes";
import EmptyRecordScreen from "../../components/ui/EmptyRecordScreen";
import { dateTimeFormat } from "src/helpers/Utils";
import { icons } from "src/utils/icons";
import ConfirmModal from "./ConfirmModal";
import UserFrom from "./UserFrom";
import { exportToCSV, exportToPDF } from "src/utils/export";
import DateRangePicker from "src/components/ui/DateRangePicker";

const items: MenuProps["items"] = [
  {
    label: "CSV",
    key: "csv",
  },
  {
    label: "PDF",
    key: "pdf",
  },
];

const UserManagement = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const { levelNumber: levelParam } = useParams();

  const [currentUserLevel, setCurrentUserLevel] = useState({
    role: "",
    level: "",
    levelId: "",
    showCreate: false,
  });
  const [currentLevel, setCurrentLevel] = useState<Level>();
  const [isLoading, setIsLoading] = useState(true);
  const [pageSize, setPageSize] = useState(10);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [userData, setUserData] = useState<any>();
  const [isUsers, setIsUsers] = useState<boolean>(false);
  const searchInput = useRef<InputRef>(null);
  const clearRef: any = useRef(null);
  const [searchBy, setSearchBy] = useState<string>("");
  const [filterBy, setFilterBy] = useState<string>("");
  const [searchText, setSearchText] = useState("");
  const [searchedColumn, setSearchedColumn] = useState("");
  const [searchObject, setSearchObject] = useState<any>(null);
  const [openFilterColumn, setOpenFilterColumn] = useState<DataIndex | null>(
    null
  );
  const [key, setKey] = useState(0);
  const [deactiveUserDetails, setDeactiveUserDetails] = useState<object | any>(
    null
  );
  const [formData, setFormData] = useState<any>({
    assignRoleFrom: 0,
    roleId: 0,
    comment: null,
  });
  const [isError, setIsError] = useState<boolean>(false);
  const [expandedRowKeys, setExpandedRowKeys] = useState<any[]>([]);
  const [editingViewType, setEditingViewType] = useState<string>("");
  const [editCurrentUserRecord, setEditCurrentUserRecord] = useState<
    object | any
  >({});
  const [exportType, setExportType] = useState<string>("");

  const [levelList, setLevelList] = useState<any>([]);

  const [sortField, setSortField] = useState("createdAt");
  const [sortOrder, setSortOrder] = useState("desc");
  const [fromDate, setFromDate] = useState<string | null>(null);
  const [toDate, setToDate] = useState<string | null>(null);
  const [selectedTimePeriod, setSelectedTimePeriod] = useState<string | null>(
    null
  );

  const {
    data: levels,
    mutate: getLevelListFromApi,
    isSuccess: isLevelDataSuccess,
  } = useGetLevelList();

  const {
    mutate: createUser,
    error: createUserError,
    isSuccess,
    isLoading: createUserLoading,
  } = useCreateUser();

  const {
    mutate: updateUser,
    error: updateUserError,
    isSuccess: isSuccessUpdate,
    isLoading: updateUserLoading,
  } = useUpdateUser();

  const {
    mutate: deActivateUser,
    error: deActivateUserError,
    isSuccess: isSuccessdeActivateUser,
    isLoading: deActivateUserLoading,
  } = useDeActivateUser();

  const {
    data: usersList,
    mutate: getUsersList,
    isSuccess: listSuccess,
    isLoading: userLoading,
  } = useGetUserManagementList();

  const {
    data: exportUsersData,
    mutate: exportUsersList,
    isSuccess: exportListSuccess,
  } = useGetUserManagementList();

  const onFinish = () => {
    const levelId: any = currentUserLevel?.levelId;

    const {
      userId,
      userName,
      userEmail,
      userPreferenceId,
      assignRoleFrom,
      designation,
      countryCode,
      userMobile,
      roleId,
      roleFromUserId,
      departmentId,
      comment,
    } = formData;

    if (
      userName &&
      userName.trim() !== "" &&
      userPreferenceId &&
      userPreferenceId.trim() !== "" &&
      userEmail &&
      userEmail.trim() !== "" &&
      countryCode &&
      countryCode.trim() !== "" &&
      userMobile &&
      userMobile.trim() !== "" &&
      assignRoleFrom &&
      assignRoleFrom !== 0 &&
      roleId &&
      roleId
    ) {
      setIsError(false);
    } else {
      setIsError(true);
      return;
    }

    let obj = {
      currentLevel: levelId || 1,
      userName: userName,
      userPreferenceId: userPreferenceId,
      countryCode: countryCode,
      mobileNo: userMobile,
      emailId: userEmail,
      designation: designation,
      departmentId: departmentId,
      roleId: roleId,
      roleFromUserId: roleFromUserId,
      comment: comment,
    };

    if (expandedRowKeys.length > 0) {
      const payload: UserUpdate = {
        ...obj,
        userId,
      };

      updateUser(payload);
    } else {
      const payload: UserCreate = obj;

      createUser(payload);
    }
  };

  type DataIndex = keyof UserList["user"];

  const handleSearch = (
    selectedKeys: string[],
    confirm: FilterDropdownProps["confirm"],
    dataIndex: DataIndex
  ) => {
    confirm();
    setCurrentPage(1);
    setSearchBy(selectedKeys[0]);
    setFilterBy(dataIndex);
    setSearchText(selectedKeys[0]);
    setSearchedColumn(dataIndex);
    let obj: any = {};
    obj[dataIndex] = selectedKeys[0];

    if (!obj) {
      setSearchObject(Object.assign(searchObject, obj));
    }
  };

  const handleReset = (clearFilters: () => void) => {
    setSearchBy("");
    setFilterBy("");
    clearFilters();
    setSearchText("");
    setCurrentPage(1);
    setPageSize(pageSize);
    // setKey((prevKey) => prevKey + 1);
    setOpenFilterColumn(null);
  };

  const getColumnSearchProps = (
    dataIndex: DataIndex
  ): TableColumnType<UserList["user"]> => ({
    filterDropdown: ({
      setSelectedKeys,
      selectedKeys,
      confirm,
      clearFilters,
      close,
    }) => (
      <div
        className="search-container"
        style={{ borderRadius: "0px" }}
        // onKeyDown={(e) => e.stopPropagation()}
      >
        <div
          className="float"
          style={{
            justifyContent: "end",
            paddingLeft: "126px",
            marginTop: "0px",
            transform: "translate(-4px,-6px)",
          }}
        >
          <CloseOutlined
            rev={1}
            style={{ fontSize: "10px" }}
            onClick={() => {
              close();
              setOpenFilterColumn(null);
            }}
          />
        </div>
        <div className="search-popup-input">
          <Input
            aria-autocomplete="both"
            aria-haspopup="false"
            ref={searchInput}
            className="search-input"
            placeholder={`Search ${_.startCase(
              dataIndex === "userPreferenceId" ? "userId" : dataIndex
            )}`}
            value={selectedKeys[0]}
            onChange={(e) => {
              setSelectedKeys(e.target.value ? [e.target.value] : []);
            }}
            onPressEnter={() => {
              handleSearch(selectedKeys as string[], confirm, dataIndex);
            }}
          />
        </div>
        <Space>
          <Button
            type="primary"
            className="role-search-btn"
            style={{ width: "60px", margin: "0 0 4px 4px" }}
            onClick={() =>
              handleSearch(selectedKeys as string[], confirm, dataIndex)
            }
            size="small"
          >
            <p className="text-def-btn"> Search</p>
          </Button>
          <Button
            onClick={() => {
              if (clearFilters) {
                handleReset(clearFilters);
              }
              handleSearch([], confirm, dataIndex);
            }}
            size="small"
            className="role-search-reset-btn"
            style={{ width: "60px", margin: "0 0 4px 0px" }}
          >
            <p className="text-def-btn"> Reset</p>
          </Button>
        </Space>
      </div>
    ),
    filterIcon: (filtered: boolean) => (
      <SearchOutlined
        rev={1}
        className="filter-icon"
        style={{ color: filtered ? "#1677ff" : undefined }}
      />
    ),
    onFilter: (value, record) => {
      return record[dataIndex]
        ?.toString()
        .toLowerCase()
        .includes((value as string).toLowerCase());
    },
    filterDropdownOpen: openFilterColumn === dataIndex,
    onFilterDropdownOpenChange: (visible) => {
      if (visible) {
        setOpenFilterColumn(dataIndex);
        setTimeout(() => searchInput.current?.select(), 100);
      } else if (openFilterColumn === dataIndex) {
        setOpenFilterColumn(null);
      }
    },
  });

  const handleExpand = (key: any) => {
    setExpandedRowKeys(expandedRowKeys.includes(key) ? [] : [key]);
  };

  // On Click View and Edit
  const handleViewEdit = (key: any, type: string, record: any) => {
    const {
      userId,
      userName,
      userPreferenceId,
      userEmail,
      countryCode,
      userMobile,
      designation,
      departmentId,
      assignedRoleId,
      roleFromUserId,
      createdAt,
      assignedRole,
    } = record;

    setFormData({
      ...formData,
      userId,
      userName,
      userPreferenceId,
      userEmail,
      countryCode,
      userMobile,
      designation,
      departmentId,
      assignedRoleId,
      roleName: assignedRole,
      assignRoleFrom: roleFromUserId === null ? 1 : 2,
      roleId: assignedRoleId,
      createdAt,
    });

    setEditCurrentUserRecord(record);
    setEditingViewType(type);
    handleExpand(key); // Ensure the row is expanded when editing
  };

  const columns: any = [
    {
      title: (item: any) => (
        <div style={{ display: "flex" }}>
          <span>USER&nbsp;NAME</span>
          <Tooltip
            title={
              item.sortOrder != undefined && item.sortOrder === "ascend"
                ? "Click to sort descending"
                : "Click to sort ascending"
            }
          >
            <span
              style={{
                position: "absolute",
                textAlign: "right",
                right: "-14px",
              }}
            >
              &nbsp;&nbsp;&nbsp;
            </span>
          </Tooltip>
        </div>
      ),
      dataIndex: "userName",
      key: "userName",
      sorter: true,
      showSorterTooltip: false,
      ...getColumnSearchProps("userName"),
    },
    {
      title: (item: any) => (
        <div style={{ display: "flex" }}>
          <span>USER&nbsp;ID</span>
          <Tooltip
            title={
              item.sortOrder != undefined && item.sortOrder === "ascend"
                ? "Click to sort descending"
                : "Click to sort ascending"
            }
          >
            <span
              style={{
                position: "absolute",
                textAlign: "right",
                right: "-14px",
              }}
            >
              &nbsp;&nbsp;&nbsp;
            </span>
          </Tooltip>
        </div>
      ),
      dataIndex: "userPreferenceId",
      key: "userPreferenceId",
      sorter: true,
      showSorterTooltip: false,
      ...getColumnSearchProps("userPreferenceId"),
    },
    {
      title: (item: any) => (
        <div style={{ display: "flex" }}>
          <span>USER&nbsp;EMAIL&nbsp;ID</span>
          <Tooltip
            title={
              item.sortOrder != undefined && item.sortOrder === "ascend"
                ? "Click to sort descending"
                : "Click to sort ascending"
            }
          >
            <span
              style={{
                position: "absolute",
                textAlign: "right",
                right: "-14px",
              }}
            >
              &nbsp;&nbsp;&nbsp;
            </span>
          </Tooltip>
        </div>
      ),
      dataIndex: "userEmail",
      sorter: true,
      showSorterTooltip: false,
      key: "userEmail",
      ...getColumnSearchProps("userEmail"),
    },
    {
      title: (item: any) => (
        <div style={{ display: "flex" }}>
          <span>ASSIGNED&nbsp;ROLE</span>
          <Tooltip
            title={
              item.sortOrder != undefined && item.sortOrder === "ascend"
                ? "Click to sort descending"
                : "Click to sort ascending"
            }
          >
            <span
              style={{
                position: "absolute",
                textAlign: "right",
                right: "-14px",
              }}
            >
              &nbsp;&nbsp;&nbsp;
            </span>
          </Tooltip>
        </div>
      ),
      dataIndex: "assignedRole",
      key: "assignedRole",
      sorter: true,
      showSorterTooltip: false,
      ...getColumnSearchProps("assignedRole"),
    },
    {
      title: (item: any) => (
        <div style={{ display: "flex" }}>
          <span>REQUESTED&nbsp;BY</span>
          <Tooltip
            title={
              item.sortOrder != undefined && item.sortOrder === "ascend"
                ? "Click to sort descending"
                : "Click to sort ascending"
            }
          >
            <span
              style={{
                position: "absolute",
                textAlign: "right",
                right: "-14px",
              }}
            >
              &nbsp;&nbsp;&nbsp;
            </span>
          </Tooltip>
        </div>
      ),
      dataIndex: "requestedBy",
      key: "requestedBy",
      sorter: true,
      showSorterTooltip: false,
      render: (requestedBy: string) =>
        requestedBy === null ? "-" : requestedBy,
      ...getColumnSearchProps("requestedBy"),
    },
    {
      title: (item: any) => (
        <div style={{ display: "flex" }}>
          <span>APPROVE/REJECT&nbsp;BY</span>
          <Tooltip
            title={
              item.sortOrder != undefined && item.sortOrder === "ascend"
                ? "Click to sort descending"
                : "Click to sort ascending"
            }
          >
            <span
              style={{
                position: "absolute",
                textAlign: "right",
                right: "-14px",
              }}
            >
              &nbsp;&nbsp;&nbsp;
            </span>
          </Tooltip>
        </div>
      ),
      dataIndex: "actionBy",
      key: "actionBy",
      sorter: true,
      showSorterTooltip: false,
      render: (actionBy: string) => (actionBy === null ? "-" : actionBy),
      ...getColumnSearchProps("approveReject"),
    },
    {
      title: (item: any) => (
        <div style={{ display: "flex" }}>
          <span>UPDATED&nbsp;ON</span>
          <Tooltip
            title={
              item.sortOrder != undefined && item.sortOrder === "ascend"
                ? "Click to sort descending"
                : "Click to sort ascending"
            }
          >
            <span
              style={{
                position: "absolute",
                textAlign: "right",
                right: "-14px",
              }}
            >
              &nbsp;&nbsp;&nbsp;
            </span>
          </Tooltip>
        </div>
      ),
      dataIndex: "updatedAt",
      key: "updatedAt",
      sorter: true,
      showSorterTooltip: false,
      // render: (updatedAt: string) => <>{dateTimeFormat(updatedAt)}</>,
      render: (updatedAt: string) => updatedAt,
    },
    {
      title: (item: any) => (
        <div style={{ display: "flex" }}>
          <span>LAST&nbsp;ACTIVE</span>
          <Tooltip
            title={
              item.sortOrder != undefined && item.sortOrder === "ascend"
                ? "Click to sort descending"
                : "Click to sort ascending"
            }
          >
            <span
              style={{
                position: "absolute",
                textAlign: "right",
                right: "-14px",
              }}
            >
              &nbsp;&nbsp;&nbsp;
            </span>
          </Tooltip>
        </div>
      ),
      dataIndex: "lastActiveAt",
      key: "lastActiveAt",
      sorter: true,
      showSorterTooltip: false,
      render: (lastActiveAt: string) =>
        lastActiveAt === null ? "N/A" : lastActiveAt,
    },
    {
      title: "STATUS",
      dataIndex: "status",
      key: "status",
      render: (status: string) =>
        status === "Pending" || status === "Pending Activation" ? (
          <div className="status" style={{ display: "flex" }}>
            <img
              src={
                status === "Pending Activation"
                  ? icons.pendingApprovalIcon
                  : icons.activeIcon
              }
              style={{ marginRight: "4px" }}
            />{" "}
            <span style={{ fontFamily: "inter" }}>{status}</span>
          </div>
        ) : (
          <div style={{ alignItems: "center", alignContent: "center" }}>
            <img
              src={"/images/blocked-icon.svg"}
              style={{ marginRight: "4px" }}
            />{" "}
            {"DeActivated"}
          </div>
        ),
      ...getColumnSearchProps("status"),
    },
    {
      key: "none",
      // fixed: "right",
      width: 100,
      title: "",
      render: (record: any) => {
        return record.status === "Pending" ||
          record.status === "Pending Activation" ? (
          <div className="more-actions" style={{ display: "flex" }}>
            <Dropdown
              disabled={expandedRowKeys.length === 0 ? false : true}
              menu={{
                items: [
                  {
                    key: "1",
                    label: (
                      <a
                        onClick={() =>
                          handleViewEdit(record.key, "view", record)
                        }
                      >
                        View Details
                      </a>
                    ),
                  },
                  {
                    key: "2",
                    label: (
                      <a
                        onClick={() =>
                          handleViewEdit(record.key, "edit", record)
                        }
                      >
                        Edit
                      </a>
                    ),
                  },
                  {
                    key: "3",
                    label: (
                      <a
                        onClick={() =>
                          setDeactiveUserDetails({
                            userId: record.userId,
                            userName: record.userName,
                            userPreferenceId: record.userPreferenceId,
                          })
                        }
                      >
                        Deactivate
                      </a>
                    ),
                  },
                ],
              }}
              trigger={["click"]}
            >
              <Typography.Link>
                <img className="action-tab" src={"/images/moreOptions.svg"} />
              </Typography.Link>
            </Dropdown>
          </div>
        ) : null;
      },
    },
  ];

  const handleDeActivateUser = (userId: any | null) => {
    if (userId === null) {
      setDeactiveUserDetails(null);
      return;
    }

    const payload = {
      userId,
    };

    // deactive user
    deActivateUser(payload);
  };

  function getUserListDetails(
    level: any,
    page?: number,
    size?: number,
    scrollbar?: boolean
  ) {
    // Request Params
    let requestParams: any = {
      page: page ? page : currentPage || 1,
      duration: 0,
      currentLevel: level,
      limit: size ? size : pageSize,
    };

    if (filterBy != "" && searchBy != "") {
      if (filterBy === "status") {
        if (searchBy?.toLowerCase() === "active") {
          requestParams[filterBy] = true;
        } else if (searchBy?.toLowerCase() === "false") {
          requestParams[filterBy] = false;
        }
      } else {
        requestParams[filterBy] = searchBy;
      }
    } else {
      delete requestParams[filterBy];
    }

    if (fromDate !== null && toDate !== null) {
      requestParams["fromDate"] = fromDate;
      requestParams["toDate"] = toDate;
    } else {
      delete requestParams["fromDate"];
      delete requestParams["toDate"];
    }
    requestParams.sortField = sortField;
    requestParams.sortOrder = sortOrder;

    if (searchObject !== null) {
      requestParams = Object.assign(requestParams, searchObject);
    }

    if (
      requestParams.currentLevel !== undefined &&
      requestParams.currentLevel !== ""
    )
      getUsersList(requestParams);
  }

  const fetchLevelList = () => {
    const requestParams: any = {
      page: currentPage || 1,
      limit: 12,
    };

    getLevelListFromApi(requestParams);
  };

  const handlePaginationChange = (pagination: any) => {
    setCurrentPage(pagination);
    setPageSize(pageSize);
  };

  const handleTableChange = (pagination: any, filters: any, sorter: any) => {
    setSortField(sorter.field);
    setSortOrder(sorter.order === "ascend" ? "asc" : "desc");
  };

  useEffect(() => {
    fetchLevelList();
  }, []);

  useEffect(() => {
    if (deActivateUserError) {
      notification.error({
        message: ``,
        description: deActivateUserError.message,
      });
    }
  }, [deActivateUserError]);

  useEffect(() => {
    if (createUserError) {
      notification.error({
        message: ``,
        description: createUserError.message,
      });
    }
  }, [createUserError]);

  useEffect(() => {
    if (updateUserError) {
      notification.error({
        message: ``,
        description: updateUserError.message,
      });
    }
  }, [updateUserError]);

  useEffect(() => {
    if (levels) {
      const levelsData: any = levels;

      const { levelId, levelNumber, levelName } = levelsData[0];

      // set Current User Level
      setCurrentUserLevel({
        level: levelNumber,
        levelId: levelId,
        role: levelName,
        showCreate: false,
      });

      // set Current Level
      setCurrentLevel(levelName);

      setLevelList(levels);

      getUserListDetails(
        levelParam && levelParam == "0" ? levelId : levelParam
      );
    }
  }, [levels]);

  useEffect(() => {
    if (isSuccess) {
      notification.success({
        message: "",
        description: `You have added '${formData?.userName} (User ID:${formData?.userPreferenceId})' User successfully`,
      });

      handleCancel();
      getUserListDetails(currentUserLevel?.levelId);
    }
  }, [isSuccess]);

  useEffect(() => {
    if (isSuccessUpdate) {
      setExpandedRowKeys([]);

      notification.success({
        message: "",
        description: `You have updated '${formData?.userName} (User ID:${formData?.userPreferenceId})' User details successfully`,
      });

      handleCancel();

      getUserListDetails(currentUserLevel?.levelId);
    }
  }, [isSuccessUpdate]);

  useEffect(() => {
    if (isSuccessdeActivateUser) {
      notification.error({
        message: "",
        description: `You have Deactivated  '${deactiveUserDetails.userName} (User ID:${deactiveUserDetails.userPreferenceId})' User.`,
      });

      setDeactiveUserDetails(null);
      getUserListDetails(currentUserLevel?.levelId);
    }
  }, [isSuccessdeActivateUser]);

  useEffect(() => {
    if (isLoading) {
      setCurrentUserLevel({ ...currentUserLevel, showCreate: false });
      getUserListDetails(currentUserLevel?.levelId);
      setIsLoading(false);
    }
  }, [isLoading]);

  // Sort user list
  useEffect(() => {
    getUserListDetails(currentUserLevel?.levelId);
  }, [searchBy, sortField, sortOrder, fromDate, toDate]);

  function onCreateUser() {
    setCurrentUserLevel({ ...currentUserLevel, showCreate: true });
    // reset edit view type
    setEditingViewType("");

    // Clear form data
    form.resetFields();

    setFormData({
      assignRoleFrom: 0,
      roleId: 0,
    });

    setIsError(false);

    setExpandedRowKeys([]);
  }

  // Handle menu click
  const handleMenuClick: MenuProps["onClick"] = async (e) => {
    setExportType(e.key);
    exportUserList(currentUserLevel?.levelId, userData?.totalCount);
  };

  // Menu props
  const menuProps = {
    items,
    onClick: handleMenuClick,
  };

  //export data
  function exportUserList(level: any, limit: number) {
    // Request Params
    const requestParams: any = {
      page: 1,
      duration: 0,
      currentLevel: level,
      limit,
    };

    if (filterBy != "" && searchBy != "") {
      if (filterBy === "status") {
        if (searchBy?.toLowerCase() === "active") {
          requestParams[filterBy] = true;
        } else if (searchBy?.toLowerCase() === "false") {
          requestParams[filterBy] = false;
        }
      } else {
        requestParams[filterBy] = searchBy;
      }
    } else {
      delete requestParams[filterBy];
    }
    requestParams.sortField = sortField;
    requestParams.sortOrder = sortOrder;

    if (requestParams.currentLevel !== undefined)
      exportUsersList(requestParams);
  }

  useEffect(() => {
    if (exportListSuccess) {
      const data: any = exportUsersData;

      const fileName = `${
        currentUserLevel?.role.charAt(0).toUpperCase() +
        currentUserLevel?.role.slice(1).toLowerCase()
      } users`;

      if (exportType === "csv") {
        const finalData = data?.userDetailList?.map((item: any) => {
          const obj: any = {};
          obj["User Name"] = item.userName;
          obj["User Id"] = item.userPreferenceId;
          obj["User Email Id"] = item.userEmail;
          obj["Assigned Role"] = item.assignedRole || "-";
          obj["Requested By"] = item.requestedBy || "-";
          obj["Approve/Reject By"] = item.actionBy || "-";
          // obj["Updated On"] = item.updatedAt
          //   ? dateTimeFormat(item.updatedAt)
          //   : "-";
          obj["Updated On"] = item.updatedAt ? item.updatedAt : "-";
          obj["Last Active"] = item.lastActiveAt || "N/A";
          obj["Status"] = item.status || "-";

          return obj;
        });

        exportToCSV(fileName, finalData);
      }

      if (exportType === "pdf") {
        const head = [
          "User Name",
          "User Id",
          "User Email Id",
          "Assigned Role",
          "Requested By",
          "Approve/Reject By",
          "Updated On",
          "Last Active",
          "Status",
        ];

        const finalData =
          data?.userDetailList?.map((item: any) => [
            item.userName,
            item.userPreferenceId,
            item.userEmail,
            item.assignedRole || "-",
            item.requestedBy || "N/A",
            item.actionBy || "-",
            // item.updatedAt ? dateTimeFormat(item.updatedAt) : "N/A",
            item.updatedAt ? item.updatedAt : "N/A",
            item.lastActiveAt || "N/A",
            item.status || "N/A",
          ]) || [];

        exportToPDF(fileName, head, finalData, "landscape");
      }

      setExportType("");
    }
  }, [exportListSuccess]);

  useEffect(() => {
    if (listSuccess) {
      const response: any = usersList;

      const updatedResponse = {
        ...response,
        userDetailList: response.userDetailList.map((user: any) => ({
          ...user,
          key: user.userId,
        })),
      };

      setUserData(updatedResponse);
    }
  }, [listSuccess]);

  useEffect(() => {
    if (userData?.userDetailList.length > 0) {
      setIsUsers(true);
    }
  }, [userData]);

  useEffect(() => {
    let temp = currentUserLevel;
    temp.showCreate = currentUserLevel.showCreate;
  }, [currentUserLevel.showCreate]);

  // Handle the call back function for Form Data
  const handleFormData = (formData: any) => {
    setFormData(formData);
  };

  // Handle the call back function to View Edit Cancel
  const handleSelectRoleAndUser = () => {
    form.resetFields(["userRole"]);
    form.resetFields(["roleFromUser"]);
  };

  const renderUserInfo = () => {
    if (currentUserLevel.showCreate)
      return (
        "Add User for " +
        `L${currentUserLevel.level}.${_.startCase(
          currentUserLevel.role.toLowerCase()
        )}`
      );
    if (expandedRowKeys.length > 0) {
      const typeTitle =
        editingViewType === "edit" ? `Edit Details` : `User Details`;
      return `${typeTitle} (${editCurrentUserRecord?.userName})`;
    }
    return `L${currentUserLevel.level}.${_.startCase(
      currentUserLevel.role.toLowerCase()
    )} Users`;
  };

  const handleCancel = () => {
    setCurrentUserLevel({
      ...currentUserLevel,
      showCreate: false,
    });
    form.resetFields();
    form.setFieldsValue({});
    setExpandedRowKeys([]);

    setFormData({
      assignRoleFrom: 0,
      roleId: 0,
    });

    setEditCurrentUserRecord({});
  };

  const captureDate = useCallback((startDate: string, endDate: string) => {
    setCurrentPage(1);

    if (startDate === "" && endDate === "") {
      setFromDate(null);
      setToDate(null);
    } else {
      setFromDate(dayjs(startDate).format("YYYY-MM-DD"));
      setToDate(dayjs(endDate).format("YYYY-MM-DD"));
    }
  }, []);

  return (
    <>
      <div className={`role-management`}>
        <div className="role-header">
          <p className="role-title">User Management</p>
        </div>

        <Row className="role-body-row">
          <Col className="role-nav-col" xs={6} xl={5}>
            <div className="role-nav-col-title">
              <p
                style={{
                  lineHeight: "64px",
                  fontSize: "14px",
                  marginLeft: "16px",
                  fontWeight: 700,
                  fontFamily: "Inter",
                }}
              >
                User Level Modules
              </p>
            </div>
            {!isLevelDataSuccess && (
              <>
                {Array(8)
                  .fill(0)
                  .map((_, index) => (
                    <div className="role-level" key={index}>
                      <div className="level-container-role">
                        <Skeleton.Input
                          active={true}
                          size="small"
                          style={{ marginTop: "7px", width: "225px" }}
                        />
                      </div>
                    </div>
                  ))}
              </>
            )}
            {levelList
              ?.filter((level: any) => level.status === true)
              ?.map((level: any, index: any) => {
                return (
                  <div
                    className={
                      levelParam == index || levelParam == level.levelId
                        ? "role-level-active"
                        : "role-level"
                    }
                    key={index}
                  >
                    <div className="level-container-role">
                      <img
                        style={{ marginRight: "4px" }}
                        src={level.imageUrl}
                        height={20}
                        width={20}
                      ></img>{" "}
                      <span
                        role="button"
                        style={{
                          fontSize: "10px",
                          fontFamily: "Inter",
                          alignContent: "center",
                        }}
                        onClick={() => {
                          setCurrentUserLevel({
                            level: level.levelNumber,
                            levelId: level.levelId,
                            role: level.levelName,
                            showCreate: false,
                          });
                          setIsUsers(false);
                          setCurrentLevel(level);
                          form.resetFields();
                          form.setFieldsValue({});
                          setFromDate(null);
                          setToDate(null);
                          setCurrentPage(1);
                          setKey((prevKey) => prevKey + 1);
                          setSearchBy("");
                          setFilterBy("");
                          setExpandedRowKeys([]);
                          setEditCurrentUserRecord({});
                          setSearchObject(null);
                          getUserListDetails(level.levelId, 1);
                          setOpenFilterColumn(null);
                          navigate(
                            RouteType.USER_MANAGEMENT + "/" + level?.levelId,
                            { replace: true }
                          );
                        }}
                      >
                        L{index}.{level.levelName}
                      </span>
                    </div>
                  </div>
                );
              })}
          </Col>
          <Col
            xs={18}
            xl={19}
            // style={{
            //   borderBottom: "1px solid #dedede",
            // }}
          >
            <Form
              form={form}
              onFinish={onFinish}
              className="create-user-from"
              layout="vertical"
            >
              <div className="form-inside">
                {currentLevel && (
                  <p className="user-info-text">
                    {currentUserLevel.showCreate ||
                    expandedRowKeys.length > 0 ? (
                      <>
                        <img
                          src="/images/back-icon.svg"
                          alt="Back"
                          className="back-icon"
                          onClick={handleCancel}
                        />
                        &nbsp;&nbsp;&nbsp;
                      </>
                    ) : null}
                    {renderUserInfo()}
                  </p>
                )}

                {currentUserLevel.showCreate || expandedRowKeys.length > 0 ? (
                  <span>
                    {editingViewType !== "view" && (
                      <Button
                        style={{
                          color: "#000000",
                          borderColor: "#ffc235",
                        }}
                        className="role-create-btn"
                        htmlType="submit"
                        disabled={createUserLoading || updateUserLoading}
                      >
                        <p
                          style={{
                            color: "black",
                            lineHeight: "40px",
                            display: "flex",
                          }}
                        >
                          {currentUserLevel.showCreate
                            ? "Add User"
                            : "Update User"}
                          &nbsp;
                          {(createUserLoading || updateUserLoading) && (
                            <span style={{ marginTop: "7px" }}>
                              <ThreeDots
                                height="25"
                                width="25"
                                color="#000"
                                ariaLabel="three-dots-loading"
                                visible={true}
                              />
                            </span>
                          )}
                        </p>
                      </Button>
                    )}
                    <Button
                      style={{ border: "1px solid #131313" }}
                      className="cancel-button"
                      onClick={handleCancel}
                    >
                      <p className="cancel-button-text">Cancel</p>
                    </Button>
                  </span>
                ) : (
                  isUsers &&
                  currentLevel && (
                    <Button
                      style={{ borderColor: "#ffc235" }}
                      className="role-create-btn"
                      onClick={onCreateUser}
                    >
                      <p className="add-user-text">Add User</p>
                    </Button>
                  )
                )}
              </div>

              {currentUserLevel?.level === ""
                ? null
                : currentUserLevel.showCreate && (
                    <UserFrom
                      isError={isError}
                      formData={formData}
                      handleFormData={handleFormData}
                      currentUserLevel={currentUserLevel}
                      handleAddCancel={handleCancel}
                    />
                  )}

              {expandedRowKeys.length > 0 &&
                Object.keys(editCurrentUserRecord).length !== 0 && (
                  <UserFrom
                    key={editCurrentUserRecord.userName}
                    isError={isError}
                    formData={formData}
                    handleFormData={handleFormData}
                    currentUserLevel={currentUserLevel}
                    editCurrentUser={editCurrentUserRecord}
                    editingViewType={editingViewType}
                    handleSelectRoleAndUser={handleSelectRoleAndUser}
                  />
                )}
            </Form>
            {currentUserLevel?.level === ""
              ? null
              : !currentUserLevel.showCreate &&
                expandedRowKeys.length === 0 && (
                  <div
                    className="tableHeader more-screen-size-600 user-management"
                    style={{ marginTop: "8px" }}
                  >
                    {isUsers && (
                      <div className="row bg-white rounded mt-2 mb-2 ms-2">
                        <div className="d-sm-flex align-items-center">
                          <div className="">Duration</div>
                          <div className="ms-2" style={{ width: "235px" }}>
                            <DateRangePicker
                              ref={clearRef}
                              updateDateRange={captureDate}
                            />
                          </div>
                          <div className="ms-2">
                            <Dropdown trigger={["click"]} menu={menuProps}>
                              <Button
                                style={{
                                  color: "#000",
                                  border: "1px solid #C4C4C4",
                                  fontWeight: 400,
                                  backgroundColor: "white",
                                }}
                              >
                                <Space>Export to</Space>&nbsp;
                                <img src={"/images/down-arrow.svg"} />
                              </Button>
                            </Dropdown>
                          </div>
                        </div>
                      </div>
                    )}

                    <div
                      className={`table-responsive ${isUsers && "custom-css"}`}
                    >
                      {!listSuccess && !isUsers && (
                        <EmptyRecordScreen
                          title={
                            <ThreeDots
                              height="40"
                              width="40"
                              color="#000"
                              ariaLabel="three-dots-loading"
                              visible={true}
                            />
                          }
                        />
                      )}

                      {listSuccess &&
                        !isUsers &&
                        userData?.userDetailList.length === 0 && (
                          <EmptyRecordScreen
                            title={"It seems to be no users to manage"}
                            buttonTitle="Add User"
                            onCreateUser={onCreateUser}
                            imageUrl={"/images/user-management.svg"}
                          />
                        )}

                      {isUsers && (
                        <>
                          <div className="table-responsive">
                            <Table
                              key={key}
                              columns={columns}
                              onChange={handleTableChange}
                              dataSource={userData?.userDetailList}
                              size="middle"
                              className="table-custom headerBg"
                              loading={{
                                indicator: (
                                  <div
                                    className="loader"
                                    style={{ width: "40px" }}
                                  >
                                    <ThreeDots
                                      height="40"
                                      width="40"
                                      color="#000"
                                      ariaLabel="three-dots-loading"
                                      visible={true}
                                    />
                                  </div>
                                ),
                                spinning: userLoading,
                              }}
                              pagination={false}
                              onExpandedRowsChange={(keys: any) =>
                                setExpandedRowKeys(keys)
                              }
                              locale={{
                                emptyText: (
                                  <Empty
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    description="No users found"
                                  />
                                ),
                              }}
                            />
                          </div>
                          {userData?.userDetailList?.length !== 0 && (
                            <div className="table-footer">
                              <Pagination
                                responsive={true}
                                showQuickJumper
                                current={currentPage}
                                className="pagination custom-pagination"
                                showSizeChanger
                                pageSizeOptions={[
                                  2, 5, 10, 15, 20, 30, 50, 100,
                                ]}
                                showPrevNextJumpers
                                pageSize={pageSize} // Set page size
                                total={userData?.pagination?.total_records}
                                showTotal={(total, range) => {
                                  const start = isNaN(range[0]) ? 1 : range[0];
                                  const end = isNaN(range[1])
                                    ? total
                                    : range[1];
                                  return `${start}-${end} of ${total} items`;
                                }}
                                defaultPageSize={10}
                                defaultCurrent={1}
                                onShowSizeChange={(page, size) =>
                                  setPageSize(size)
                                }
                                onChange={(page, size) => {
                                  setCurrentPage(page);
                                  getUserListDetails(
                                    currentUserLevel?.levelId,
                                    page,
                                    size,
                                    false
                                  );
                                }}
                              />
                            </div>
                          )}
                        </>
                      )}
                    </div>
                  </div>
                )}
          </Col>
        </Row>

        {/* Disable Language Info Modal */}
        {deactiveUserDetails !== null && (
          <ConfirmModal
            isModal={true}
            handleCallback={handleDeActivateUser}
            deactiveUserDetails={deactiveUserDetails}
            loading={deActivateUserLoading}
          />
        )}
      </div>
      <style>
        {`
          .search-container {
            border-radius: 4px !important;
          }
          .role-search-btn {
            background: #ffc235 !important;
          }
          .ant-dropdown .ant-dropdown-menu {
            border-radius: 4px !important;
            border: 1px solid #c4c4c4 !important;
            width: 160px !important;
            padding: 0px 0 !important;
            
            .ant-dropdown-menu-item-only-child {
              border-bottom: 1px solid #c4c4c4 !important;
              border-radius: 0 !important;
            }
          }
        `}
      </style>
    </>
  );
};

export default UserManagement;
