import {
  CloseOutlined,
  DownOutlined,
  SearchOutlined,
  UserOutlined,
} from "@ant-design/icons";
import {
  Badge,
  Button,
  Col,
  DatePicker,
  Dropdown,
  Form,
  Input,
  InputRef,
  MenuProps,
  Pagination,
  Popover,
  Row,
  Select,
  Space,
  Table,
  TableColumnType,
  TableColumnsType,
  Typography,
  message,
  notification,
} from "antd";
import {
  FilterDropdownProps,
  TableRowSelection,
} from "antd/es/table/interface";
import React, { useCallback, useContext, useEffect, useRef, useState } from "react";
import "./index.css";
import Permissions from "./Permissions";
import TextArea from "antd/es/input/TextArea";
import {
  useCreateRole,
  useGetRoleList,
  useGetUserRoleList,
  useUpdateRole,
} from "src/api/roles";
import { Level, RoleCreate, RoleList } from "src/api/models";
import { json } from "stream/consumers";
import Highlighter from "react-highlight-words";
import { use } from "i18next";
import { useGetLevelList } from "src/api/user";
import _ from "lodash";
import EmptyRoles from "./EmptyRoles";
import { randomInt, randomUUID } from "crypto";
import { set } from "date-fns";
import { daysOptions } from "src/api/constants";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import ModalConfirm from "./ModalConfirm";
import { LevelContext } from "src/context/Level";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { RouteType } from "src/constants/routeTypes";
import DateRangePicker from "src/components/ui/DateRangePicker";
interface DataType {
  key: React.ReactNode;
  name: string;
  age: number;
  address: string;
  children?: DataType[];
}
type FilterByTypes={
updatedBy:boolean,
roleName?:boolean,
roleId?:boolean
}
type RoleItems = { roleName: string; roleId: string };
const RoleManagement = () => {
  const [currentRoleLevel, setCurrentRoleLevel] = useState({
    role: "",
    level: "",
    levelId: "",
    showCreate: false,
  });
  const [currentLevel, setCurrentLevel] = useState<Level>();
  const [selectedCopy, setSelectedCopy] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [readOnly, setReadOnly] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });
  const [pageSize, setPageSize] = useState(10);
  const [noPerms, setNoPerms] = useState(false)
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [currentPageSelect, setCurrentPageSelect] = useState<number>(1);
const [rolesSelectData, setRolesSelectData] = useState<any>()
  const [days, setDays] = useState(0);
  const { RangePicker } = DatePicker;
  const [form] = Form.useForm();
  const [rolesData, setRolesData] = useState<any>();
  const [editMode, setEditMode] = useState(false);
  const [showPermission, setShowPermission] = useState(false);
  const searchInput = useRef<InputRef>(null);  
  const [currentSearch, setCurrentSearch] = useState(true);
  const [key, setKey] = useState(0);
  const [permKey, setPermKey] = useState(1);
  const [scroll, setScroll] = useState(false);
  const [refetch, setRefetch] = useState(true)
  const [levelsClicked, setLevelsClicked] = useState(false)
  const [searchBy, setSearchBy] = useState<string>("");
  const [filterBy, setFilterBy] = useState<string>("");
  const [filterByProp, setFilterByProp] = useState<FilterByTypes>();
  const [searchText, setSearchText] = useState("");
  const [searchObject, setSearchObject] = useState<any>({});
const{levelNumber:levelParam}=useParams()
  const [userRoles, setUserRoles] = useState<any>();
  const [selectedOption, setSelectedOption] = useState(0);
  const [searchedColumn, setSearchedColumn] = useState("");
  const [editRecord, setEditRecord] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<any>();
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [isDateFilterRefetch,setIsDateFilterRefetch ] = useState(false)
  const {levelNumber,setLevelNumber} = useContext(LevelContext)
  const [roleStatusMessage, setRoleStatusMessage] = useState("");
  const [preservePerm, setPreservePerm] = useState(true);
  const [showModalDeActive, setShowModalDeActive] = useState(false);
  const {
    data: levels,
    mutate: getLevelListFromApi,
    isSuccess: isLevelDataSuccess,
  } = useGetLevelList() as any;

  const {
    mutate: createRole,
    error: createRoleError,
    data:recordCreated,
    isSuccess,
  } = useCreateRole() as any;
  const {
    mutate: updateRole,
    error: updateRoleError,
    isSuccess: isSuccessUpdate,
  } = useUpdateRole();
  const {
    data: rolesList,
    mutate: getRolesList,
    isSuccess: listSuccess,
    isLoading: rolesLoading,
  } = useGetRoleList() as any;
  const {
    data: rolesListSelect,
    mutate: getRolesListSelect,
    isSuccess: listSuccessSelect,
    isLoading: rolesLoadingSelect,
  } = useGetRoleList() as any;

  const {
    data: rolesListEmptyCheck,
    mutate: getRolesListEmptyCheck,
    isSuccess: listSuccessEmpty,
    isLoading: rolesLoadingEmpty,
  } = useGetRoleList() as any;

  const {
    data: rolesListNoPage,
    mutate: getRolesListNoPage,
    isSuccess: listSuccessNoPage,
    isLoading: rolesLoadingNoPage,
  } = useGetRoleList() as any;
  const {
    data: userRolesList,
    mutate: getUserRolesList,
    isSuccess: listUserSuccess,
  } = useGetUserRoleList();
  const onFinish = (values: any) => {
    const role = values;
    role.levelId = currentRoleLevel.levelId;
    role.aggregatorId = "1";
    const payload: RoleCreate = {
      role: role,
      module: selectedRowKeys!!,
    };
    if (editMode) {
      role.roleId = currentRecord?.roleId;

      if (preservePerm) {
        payload.module = savedSelectedRowKeys;
      }
      setRoleStatusMessage(
        ` Role ${payload.role.roleName}(${payload.role.roleId}) updated successfully`
      );
      updateRole(payload);
    } else {
      if (preservePerm) {
        payload.module = selectedRowKeysFromRoles;
      }
     createRole(payload) 
    }
  };
  function showSucessMessage(roleName:string,id:string){
    return ` Role ${roleName} (Id:${id}) created successfully`
  }
  function setCurretRecord(record: any) {
    setEditRecord(record);
  }
  type DataIndex = keyof RoleList["role"];
  const actionItems: any = [
    {
      key: "1",
      label: (
        <a
          onClick={() => {
            let temp = currentRoleLevel;
            temp.showCreate = true;
            //setCurrentRoleLevel(temp)
            setCurrentRoleLevel((prevCurrentRoleLevel) => ({
              ...prevCurrentRoleLevel,
              showCreate: true,
            }));
            setEditRecord(true);
            setEditMode(true);
            setReadOnly(true);            
          }}
        >
          View Details
        </a>
      ),
    },
    {
      key: "2",
      label: (
        <a
          onClick={() => {
            let temp = currentRoleLevel;
            temp.showCreate = true;
            //setCurrentRoleLevel(temp)
            setCurrentRoleLevel((prevCurrentRoleLevel) => ({
              ...prevCurrentRoleLevel,
              showCreate: true,
            }));
            setEditRecord(true);
            setEditMode(true);
          }}
        >
          Edit
        </a>
      ),
    },

    {
      key: "3",
      label: (
        <a
          onClick={() => {
            setShowModalDeActive(true);
          }}
        >
          Deactivate
        </a>
      ),
    },
  ];
  const handleSearch = (
    selectedKeys: string[],
    confirm: FilterDropdownProps["confirm"],
    dataIndex: DataIndex
  ) => {
    confirm();
    setSearchBy(selectedKeys[0]);
    setFilterBy(dataIndex);
    let obj:any={}
    obj[dataIndex]=selectedKeys[0]
    setSearchObject(Object.assign(searchObject,obj))
    const filterByPropTemp=Object.assign({},filterByProp)
    if(filterByProp?.roleId){
      filterByPropTemp.roleId=true
    }
    if(filterByProp?.roleName){
      filterByPropTemp.roleName=true
    }
    if(filterByProp?.updatedBy){
      filterByPropTemp.updatedBy=true
    }
    setSearchText(selectedKeys[0]);
    setSearchedColumn(dataIndex);
   // setDays(3)
  };
  const handleReset = (clearFilters: () => void,dataIndex:any) => {
    let temp:any=Object.assign({},searchObject)
    if(!(dataIndex=='status'))
    {
      temp[dataIndex]=""}
    setSearchObject(temp)
    setSearchBy("");
    setFilterBy("");
    clearFilters();
    setSearchText("");
    setCurrentPage(1);
    setPageSize(pageSize); 
   //setKey((prevKey) => prevKey + 1);
  };
  useEffect(() => {
    if(currentLevel?.levelNumber===undefined){
     const topLevel= levels?.filter((level:any)=>level?.levelNumber===0)[0]
      setCurrentRoleLevel({
        level: topLevel?.levelNumber,
        levelId: topLevel?.levelId,
        role: topLevel?.levelName,
        showCreate: false,
      });
    }
  }, [isLevelDataSuccess])
  
  const captureDate=  useCallback(
    (startDate:string,endDate:string)=>{
      setStartDate(startDate)
      setEndDate(endDate)
      //setIsDateFilterRefetch()
      setDays(3)
    },
    [],
  )

  const getColumnSearchProps = (
    dataIndex: DataIndex
  ): TableColumnType<RoleList["role"]> => ({
    filterDropdown: ({
      setSelectedKeys,
      selectedKeys,
      confirm,
      clearFilters,
      close,
    }) => {
   return   <div
        className="search-container"
        style={{ borderRadius: "0px" }}
        onKeyDown={(e) => e.stopPropagation()}
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
            }}
          />
        </div>
        <div className="search-popup-input">
          {" "}
          <Input
            aria-autocomplete="both"
            aria-haspopup="false"
            ref={searchInput}
            className="search-input"
            placeholder={`Search ${_.startCase(dataIndex)}`}
            value={selectedKeys[0]}
            /*           autoComplete="true"
             */ onChange={(e) => {
              // setSearchBy(e.target.value)
              // setFilterBy(dataIndex!=='aggregatorPreferenceId'?dataIndex:'aggregatorId')

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
            onClick={() =>{
              handleSearch(selectedKeys as string[], confirm, dataIndex)}
            }
            /* icon={<SearchOutlined rev={1} />}
             */ size="small"
          >
            <p className="text-def-btn"> Search</p>
          </Button>
          <Button
            onClick={() => {
              clearFilters && handleReset(clearFilters,dataIndex)
              handleSearch([], confirm, dataIndex);

            }}
            size="small"
            className="role-search-reset-btn"
            style={{ width: "60px", margin: "0 0 4px 0px" }}
          >
            <p className="text-def-btn"> Reset</p>
          </Button>
          {/* <Button
            type="link"
            size="small"
            onClick={() => {
              confirm({ closeDropdown: false });
//let current:any={}
//current[dataIndex]=selectedKeys.length>0?selectedKeys [0]:''
             // console.log("[dataIndex]:currentSearch[(selectedKeys  as string[])[0]]",dataIndex,selectedKeys,"===",currentSearch,"====",current,{dataIndex:selectedKeys.length>0?selectedKeys [0]:''})
             // setCurrentSearch(Object.assign({}, currentSearch, current))
              //setCurrentSearch(...currentSearch,currentSearch[(selectedKeys  as string[])[0]])
              setSearchText((selectedKeys as string[])[0]);
              setSearchedColumn(dataIndex);
     //         handleSearch(selectedKeys as string[], confirm, dataIndex)

            }}
          >
            Filter
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => {
              close();
            }}
          >
            close
          </Button> */}
        </Space>
      </div>
    },
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
    onFilterDropdownOpenChange: (visible) => {
      if (visible) {
        setTimeout(() => searchInput.current?.select(), 100);
      }
    },
  });
  const columns = [
    {
      title: "Role Id",
      dataIndex: "roleId",
      key: "roleId",
      sorter: true,
      ...getColumnSearchProps("roleId"),
    },
    {
      title: "Role Name",
      dataIndex: "roleName",
      key: "roleName",
      sorter: true,
      ...getColumnSearchProps("roleName"),
    },
    {
      title: "Updated By",
      dataIndex: "updatedBy",
      sorter: true,
      key: "updatedBy",
      ...getColumnSearchProps("updatedBy"),
    },
    {
      title: "Updated On",
      dataIndex: "updatedAt",
      key: "updatedAt",
      sorter: true,
    },
    { title: "Users", dataIndex: "users", key: "users", sorter: false },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      sorter: true,
      render: (status: string) =>
        status === "active" ? (
          <div className="status">
            <img
              src={"/images/active-icon.svg"}
              style={{ marginRight: "4px" }}
            />{" "}
            <span style={{ fontFamily: "inter" }}>{"Active"}</span>
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
      title: "",
      render: (record: any) => {
        return record?.status === "active" ? (
          <Dropdown
            menu={{
              items: actionItems,
            }}
            trigger={["click"]}
            arrow
          >
            <Typography.Link
              onClick={() => {
                setRoleName(record?.roleName);
                setRoleComments(record?.roleComments);
                setsavedSelectedRowKeys(record?.module);
                /*                 setEditRecord(false);
                setCurrentRoleLevel((prevCurrentRoleLevel) => ({
                  ...prevCurrentRoleLevel,
                  showCreate: false,
                }));
 */
                setCurrentRecord(record);
              }}
            >
              <img className="action-tab" src={"/images/moreOptions.svg"} />
            </Typography.Link>
          </Dropdown>
        ) : null;
      },
    },
  ];
  function currentRows(val: any, isTouched: boolean = false) {
    if (!isTouched) {
      setPreservePerm(true);
    } else {
      setPreservePerm(false);
    }
    setSelectedRowKeys(val);
  }
  const rowSelection: TableRowSelection<DataType> = {
    onChange: (selectedRowKeys, selectedRows) => {},
    onSelect: (record, selected, selectedRows) => {},
    onSelectAll: (selected, selectedRows, changeRows) => {},
  };
  const [selectedRowKeys, setSelectedRowKeys] = useState<any[]>([]);
  const [savedSelectedRowKeys, setsavedSelectedRowKeys] = useState(
    currentRecord?.moduleId ?? []
  );
  const [selectedRowKeysFromRoles, setselectedRowKeysFromRoles] = useState([]);
  const [levelPageSize, setLevelPageSize] = useState(10);
  const [isReset, setIsReset] = useState(false);
  const [sortField, setSortField] = useState("updatedAt");
  const [roleFrom, setRoleFrom] = useState(0);

  const [sortOrder, setSortOrder] = useState("desc");
  const clearRef: any = useRef(null);
  const resetRef: any = useRef(null);
  const [levelList, setLevelList] = useState<any>([]);
  const [roleNameError, setRoleNameError] = useState("");
  const [roleName, setRoleName] = useState(currentRecord?.roleName ?? "");
  const [roleComments, setRoleComments] = useState(
    currentRecord?.roleComments ?? ""
  );
  const items: MenuProps["items"] = [
    {
      label: "1st menu item",
      key: "1",
      icon: <UserOutlined rev={null} />,
    },
    {
      label: "2nd menu item",
      key: "2",
      icon: <UserOutlined rev={null} />,
    },
    {
      label: "3rd menu item",
      key: "3",
      icon: <UserOutlined rev={null} />,
      danger: true,
    },
    {
      label: "4rd menu item",
      key: "4",
      icon: <UserOutlined rev={null} />,
      danger: true,
      disabled: true,
    },
  ];
  const itemsExport: MenuProps["items"] = [
    {
      key: "1",
      label: <a>CSV</a>,
    },
    {
      key: "2",
      label: <a>PDF</a>,
    },
  ];
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
    { key: 3, value: 3, label: <div>Create A Custom Role & Permissions</div> },
  ];

  useEffect(() => {
    if (
      (selectedRowKeysFromRoles?.length > 0 &&
        (selectedOption === 1 || selectedOption === 2)) ||
      editMode ||
      selectedOption > 2
    ) {
      setShowPermission(true);
    } else {
      setShowPermission(false);
    }
  }, [selectedOption, editMode, selectedRowKeysFromRoles]);
  function getRoleListDetailsSelect(
    level: any,
    page?: number,
    size?: number,
    scrollbar?: boolean
  ){
    let requestParams: any = {
      page: page ? page : currentPage || 1,
      //duration: days,
      startDate: startDate ?? "",
      endDate: endDate ?? "",
      status:true,
      currentLevel: level,

      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: size ? size : pageSize,
    };
    requestParams.aggregatorId = "1";
    requestParams[filterBy]=searchBy
    getRolesListSelect(requestParams)

  }

  function getRoleListDetails(
    level: any,
    page?: number,
    size?: number,
    scrollbar?: boolean
  ) {

    if (page && scrollbar) {
      setScroll(true);
    }
    getRolesListEmptyCheck({page:1,currentLevel: level,limit: 2,status:true,aggregatorId:"1",startDate:"",endDate:""})
    let requestParams: any = {
      page: page ? page : currentPage || 1,
      //duration: days,
      startDate: startDate ?? "",
      endDate: endDate ?? "",
      status:true,
      currentLevel: level,

      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: size ? size : pageSize,
    };
    requestParams.aggregatorId = "1";
    

    if (!searchObject.areAllValuesEmpty) {
      if(!searchObject.status){
        searchObject.status=true
      }
      if (searchObject.status) {
        if (searchObject.status?.toString()?.toLowerCase() === "active") {
          searchObject.status = true;
        } else if (searchObject.status?.toString()?.toLowerCase() === "false") {
          searchObject.status = false;
        }
        requestParams = Object.assign(requestParams,searchObject);
      } else {
        requestParams = Object.assign(requestParams,searchObject);
      }
    } else {
      delete requestParams[searchObject];
    }
    requestParams.currentLevel=level/* ??(levelParam&&levelParam?.length>0?levelParam:levelNumber)  */                           
/*     requestParamsNoPage.startDate = "";
    requestParamsNoPage.endDate = "";
 */  
    requestParams.sortField = sortField;
    requestParams.sortOrder = sortOrder;
    let requestParamsSelect = Object.assign({}, requestParams);
    let requestParamsNoPage = Object.assign({}, requestParams);
    requestParamsNoPage.page = 0;
    requestParamsNoPage.limit = 0;
    getRolesListNoPage(requestParamsNoPage);
    requestParams.currentLevel=requestParams.currentLevel??0
    if (requestParams.currentLevel !== undefined) {
      getRolesList(requestParams);
      // if(searchBy.length>0||scrollbar){
        //requestParamsSelect[filterBy]=searchBy
/*       }
 */    }
  }
  useEffect(() => {
  
    
  }, [listSuccessSelect])
  
  function getUserRoleListDetails(
    level: any,
    page?: number,
    scrollbar?: boolean
  ) {
    if (page && scrollbar) {
      setScroll(true);
    }
    const requestParams: any = {
      page: page ? page : currentPage || 1,
      duration: days,
      currentLevel: currentLevel?.levelId,
      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: pageSize,
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
    requestParams.aggregatorId = "1";
   let levelFound= levelList?.filter((level:any)=>level?.levelNumber===levelNumber)
    if (requestParams.currentLevel !== undefined){
      getUserRolesList(requestParams);
  }
  else if(levelFound.length>0){
    requestParams.currentLevel=levelFound[0]?.levelId
    getUserRolesList(requestParams);
  }
  }
  useEffect(() => {
    form.resetFields();
    form.setFieldsValue({});
  }, [editRecord]);
  useEffect(() => {
    if(isSuccess){
    notification["success"]({
      message: "Notification",
      description: showSucessMessage(recordCreated?.role?.roleName,recordCreated?.role?.roleId),
    });
    setCurrentRoleLevel((prevCurrentRoleLevel) => ({
      ...prevCurrentRoleLevel,
      showCreate: false,
    }));
    setEditMode(false)
  }
  }, [isSuccess])
  
  useEffect(() => {}, [currentRecord]);
  const fetchLevelList = () => {
    const requestParams: any = {
      page: currentPage || 1,

      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: 12,
    };
    getLevelListFromApi(requestParams);
  };
  useEffect(() => {
    if (levels) {
      setLevelList(levels);
    }
  }, [levels]);
  useEffect(() => {
    if (isSuccessUpdate) {
      notification["success"]({
        message: "Notification",
        description:roleStatusMessage,
      });
      setCurrentRoleLevel((prevCurrentRoleLevel) => ({
        ...prevCurrentRoleLevel,
        showCreate: false,
      }));
        setEditMode(false)
        setShowCreate(false)
    }
  }, [isSuccessUpdate]);

  useEffect(() => {    
    if (isLoading  || days > 0) {
      fetchLevelList();
      setShowCreate(false);
      setSelectedOption(0);
      //setRefetch(false)
      let temp = currentRoleLevel;
      temp.showCreate = false;
      setCurrentRoleLevel(temp);

      setEditRecord(false);
      getUserRoleListDetails(currentLevel?.levelNumber);
      getRoleListDetails(currentLevel?.levelNumber);
      getRoleListDetailsSelect(
        currentLevel?.levelNumber,
        currentPageSelect,
        undefined,
        true
      );
      if(days>0){
        setDays(-1)
      }
      setIsLoading(false);
    }
  }, [
    isLoading,
    // isSuccess,
    currentRoleLevel,
    isSuccessUpdate,
    currentSearch,
    days,
    
  ])
  useEffect(() => {
    if ( ((isSuccess || isSuccessUpdate)&&refetch) ) {
      fetchLevelList();
      setShowCreate(false);
      setSelectedOption(0);
      setRefetch(false)
      let temp = currentRoleLevel;
      temp.showCreate = false;
      setCurrentRoleLevel(temp);
      setEditRecord(false);            
      getUserRoleListDetails(currentLevel?.levelNumber);
      getRoleListDetails(currentLevel?.levelNumber);
         }       
      
  },
   [
    isSuccess,
    currentRoleLevel,
    isSuccessUpdate,
    currentSearch,
  ]);

  useEffect(() => {
    getRoleListDetails(currentLevel?.levelNumber);
  }, [searchBy, sortField, sortOrder]);

 
  function onCreateLevel() {
    let temp = currentRoleLevel;
    temp.showCreate = true;
    setCurrentRoleLevel(temp);
    setShowCreate(true);
  }

  const handleMenuClick: MenuProps["onClick"] = (e) => {};
  const menuProps = {
    items,
    onClick: handleMenuClick,
  };
  useEffect(() => {
    if (listUserSuccess) {
      setUserRoles(userRolesList);
    }
  }, [listUserSuccess]);

  useEffect(() => {
    if (listSuccess) {
      if (scroll) {
        let rolesListTemp: any = rolesList;
        let roleDataTemp = rolesData?.roleList;
        let tempObj: { pagination: any; roleList: any } = {
          pagination: undefined,
          roleList: [],
        };
       /*  const filteredExistingRoles = roleDataTemp.filter((existingRole:any) =>
          !rolesListTemp.roleList.some((rolesListTemp:any) => rolesListTemp.roleId === existingRole.roleId)
        );
        */ 
        let roleList: any
     /*   if(searchBy.length>0)
       { 
        console.log("==========================================")
        roleList = [...roleDataTemp,   ...rolesListTemp?.roleList?.filter((apiRole:any) => 
        !roleDataTemp.some((existingRole:any) => 
          existingRole.roleId === apiRole.roleId && existingRole.roleName === apiRole.roleName
        )
      )]
      console.log("==========================================",roleList)

    }
    else{
      const filteredExistingRoles = roleDataTemp.filter((existingRole:any) =>
          !rolesListTemp.roleList.some((rolesListTemp:any) => rolesListTemp.roleId === existingRole.roleId)
        );

         roleList = [...filteredExistingRoles, ...rolesListTemp.roleList];
         console.log("==eee========================================",roleList)

    } */
   // roleList = [...roleDataTemp, ...rolesListTemp?.roleList];

        tempObj.pagination = rolesListSelect?.pagination;
        tempObj.roleList = rolesListSelect;
     // setRolesData(tempObj);
     setRolesData(rolesList);

      } else {
        setRolesData(rolesList);
      }
      setDays(-1)
    }
    if(listSuccessSelect){
      let rolesOptions=rolesSelectData
      let roles
      if(rolesSelectData){
        const filteredExistingRoles = rolesSelectData.forEach((existingRole:any) =>{
       return   rolesListSelect.roleList.forEach((rolesListTemp:any) => rolesListTemp.roleId === existingRole.roleId)
        }
        );
        const roleMap = new Map(rolesListSelect.roleList.map((role:any) => 
        {
        return [role?.role?.roleId, role]}));
const mergedArray = [
    ...rolesSelectData.filter((role:any) => !roleMap.has(role.role.roleId)), // Roles not in array2
    ...rolesListSelect ?.roleList                                           // Add all roles from array2
];
      roles =mergedArray /* [...filteredExistingRoles, ...rolesListSelect?.roleList]    */

    }
      else{
        roles=rolesListSelect?.roleList

      }
      setRolesSelectData(roles)
    }
  }, [listSuccess,listSuccessSelect,scroll]);
  useEffect(() => {
    setRolesSelectData([])
    getRoleListDetailsSelect(
      currentLevel?.levelNumber??levelParam,
      1,
      undefined,
      true
    );

  }, [levelNumber,isLoading])
  
 
  const handleDropdownItemClick = (e: any) => {
    //getAggregatorReport(aggregator)
    if (e.key == 1) {
      downloadRolesAsCSV();
    } else if (e.key == 2) {
      downloadRolesAsPDF();
    }
  };
  const downloadRolesAsPDF = () => {
    if (rolesListNoPage) {
      const data =
        typeof rolesListNoPage !== "object"
          ? JSON.parse(rolesListNoPage)
          : rolesListNoPage;
      const currentData = data.roleList.map((it: any) => {
        it.role.users = it.users;
        return it;
      });
      const head = [
        [
          "Role Id ",
          "Role Name",
          "Updated By",
          "Updated On",
          "Users",
          "Status",
          /*               "Comments"
           */
        ],
      ];
      const finalData: any = [];
      currentData?.map((item: any, index: any) => {
        const arr = [];
        const role = item.role;
        if (Object.prototype.hasOwnProperty.call(role, "roleId")) {
          arr.push(role.roleId);
        }
        if (Object.prototype.hasOwnProperty.call(role, "roleName")) {
          arr.push(role.roleName);
        }
        if (Object.prototype.hasOwnProperty.call(role, "updatedBy")) {
          arr.push(role.updatedBy);
        }
        if (Object.prototype.hasOwnProperty.call(role, "updatedAt")) {
          arr.push(role.updatedAt);
        }
        if (Object.prototype.hasOwnProperty.call(role, "users")) {
          arr.push(role.users);
        }
        if (Object.prototype.hasOwnProperty.call(role, "status")) {
          arr.push(role.status ? "Active" : "Deactivated");
        }
        /* if (Object.prototype.hasOwnProperty.call(role, "roleComments")) {
              arr.push((role.roleComments))
            } */

        finalData.push(arr);
      });
      const doc = new jsPDF();
      autoTable(doc, {
        head: head,
        body: finalData,
        columnStyles: {
          0: { cellWidth: 20 },
          1: { cellWidth: 40 },
          2: { cellWidth: 38 },
          5: { cellWidth: 20 },
        },
        tableWidth: "wrap",
      });

      doc.save(
        `${_.startCase(currentLevel?.levelName.toLowerCase())}-RolesReport.pdf`
      );
    }
  };
  function confirmDeActivate(flag: boolean) {
    setShowModalDeActive(false);
    if (flag) {
      let deActivateRole = currentRecord;
      deActivateRole.status = false;
      let payload = {
        role: {
          status: deActivateRole.status,
          roleId: deActivateRole.roleId,
          roleName: deActivateRole.roleName,
          levelId: deActivateRole.levelId?.levelId,
          roleComments: deActivateRole.roleComments,
          aggregatorId: "1",
        },
        module: deActivateRole.module,
      };
      setRoleStatusMessage(
        `You have Deactivated ${deActivateRole.roleName} (ID:${deActivateRole.roleId}) role.`
      );
      setRefetch(true)
      updateRole(payload);
    }
  }
  useEffect(() => {
    if(levelParam){
    setEditRecord(false);
    //setCurrentLevel(levelFromPath);
    setLevelNumber(Number.parseInt(levelParam))                          
    setEditMode(false);
    setScroll(false);
    setPageSize(10);
    setCurrentPage(1);
    setSelectedOption(0);
    form.resetFields();
    form.setFieldsValue({});
    setRoleName("");
    setRoleComments("");
    setFilterBy("");
    setSearchBy("");
    setSearchObject({})
    setKey((prevKey) => prevKey + 1);
    setCurrentRecord({});
    setReadOnly(false);
    getRoleListDetails(levelParam, undefined, undefined, false);
  }
  }, [levelParam])  
  const downloadRolesAsCSV = () => {
    JSONToCSVConvertor(
      rolesListNoPage,
      `${_.startCase(currentLevel?.levelName.toLowerCase())}-RolesReport`,
      true
    );
  };
  const JSONToCSVConvertor = (
    JSONData: any,
    ReportTitle: string,
    ShowLabel: boolean
  ) => {
    const data = typeof JSONData !== "object" ? JSON.parse(JSONData) : JSONData;
    let CSV = "";
    const arrData = data.roleList.map((it: any) => {
      it.role.users = it.users;
      return it;
    });
    const headersRequried = [
      "roleId",
      "roleName",
      "updatedBy",
      "updatedAt",
      "users",
      "status" /* ,
          "roleComments" */,
    ];
    if (ShowLabel) {
      let row = "";
      for (const index in arrData[0].role) {
        switch (index) {
          case "roleId":
            row += "Role Id" + ",";
            break;
          case "roleName":
            row += "Role Name" + ",";
            break;
          case "updatedBy":
            row += "Updated By" + ",";
            break;
          case "updatedAt":
            row += "Updated On" + ",";
            break;
          case "users":
            row += "Users" + ",";
            break;
          case "status":
            row += "Status" + ",";
            break;
          /*               case "roleComments":
                row+="Comments"+",";
                break
 */ default:
            break;
        }
      }
      row = row.slice(0, -1);
      CSV += row + "\r\n";
    }
    for (let i = 0; i < arrData?.length; i++) {
      let row = "";
      for (const index in arrData[i].role) {
        if (headersRequried.includes(index)) {
          if (index === "status") {
            let statusText = arrData[i].role[index] ? "Active" : "DeActive";
            row += '"' + statusText + '",';
          } else {
            //   console.log(arrData?.roleList[i].role[index.role])
            row += '"' + arrData[i].role[index] + '",';
          }
        }
      }
      row.slice(0, row.length - 1);
      CSV += row + "\r\n";
    }
    if (CSV === "") {
      return;
    }
    let fileName = "";
    fileName += ReportTitle /* .replace(/ /g, "-") */;
    const uri = "data:text/csv;charset=utf-8,%EF%BB%BF" + encodeURI(CSV);
    const link = document.createElement("a");
    link.href = uri;
    link.style.visibility = "hidden";
    link.download = fileName + ".csv";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  /*   useEffect(() => {
    
    
  }, [roleFrom])
 */
    useEffect(() => {
    setShowCreate(currentRoleLevel.showCreate);
    let temp = currentRoleLevel;
    temp.showCreate = currentRoleLevel.showCreate;
    /* setCurrentRoleLevel(prevCurrentRoleLevel => ({
    ...prevCurrentRoleLevel,
    showCreate: currentRoleLevel.showCreate
  })); */
  }, [currentRoleLevel.showCreate]);
  useEffect(() => {
    setShowCreate(editRecord);
  }, [editRecord]);
  useEffect(() => {
    //Initialize selectedRowKeys with saved selection
    setSelectedRowKeys(savedSelectedRowKeys);
  }, []);
  const renderPagination=()=>{
        return(rolesList?.roleList?.length > 0?<Pagination
                            showQuickJumper
                            current={currentPage}                            
                            className="pagination"
                            showSizeChanger
                            pageSizeOptions={[2, 5, 10, 15, 20, 30, 50, 100]}
                            showPrevNextJumpers
                            //showTotal={rolesData?.pagination?.total_record}
                            pageSize={pageSize} // Set page size
                            total={rolesList?.pagination?.total_records}
                            showTotal={(total, range) =>
                              `${range[0]}-${range[1]} of ${total} items`
                            }
                            defaultPageSize={10}
                            defaultCurrent={1}
                            onShowSizeChange={(page, size) =>
                              //console.log("page s",size,page)
                              setPageSize(size)
                            }
                            onChange={(page:any, size) => {
                              setCurrentPage(page);    
                              // setPageSize()
                              getRoleListDetails(
                                currentLevel?.levelNumber,
                                page,
                                size,
                                false
                              );
                            }}
                          />:null)
  }
const handleCancel=()=>{
  let temp = currentRoleLevel;
  temp.showCreate = true;
  // setCurrentRoleLevel(temp)
  setShowCreate(true);
  setEditRecord(false);
  setScroll(false);
  setSelectedOption(0);
  setCurrentRoleLevel((prevCurrentRoleLevel) => ({
    ...prevCurrentRoleLevel,
    showCreate: false,
  }));
  setEditMode(false);
  setReadOnly(false);
}
  const handlePaginationChange = (pagination: any) => {
    setCurrentPage(pagination);
    setPageSize(pageSize);
    getRoleListDetails(currentLevel?.levelNumber, pagination, undefined, false);
  };
  const handleScroll = (event: any) => {
    event.persist(); 
    let roleList: any = rolesListSelect;
    const bottom =
      event.target.scrollHeight ===
      event.target.scrollTop + event.target.clientHeight;
    if (
      bottom &&
      rolesListSelect?.roleList?.length + (currentPageSelect - 1) * 10 <
      rolesListSelect?.pagination?.total_records
    ) {
      setCurrentPageSelect((prevCurrentPageSelect) => prevCurrentPageSelect + 1);
      getRoleListDetailsSelect(
        currentLevel?.levelNumber,
        currentPageSelect + 1,
        undefined,
        true
      );
    }
    //getRoleListDetails(currentLevel?.levelNumber)
  };

  let debounce_getRoleListDetails = _.debounce(function () {
    getRoleListDetailsSelect(currentLevel?.levelNumber);
  }, 1000);
  const handleTableChange = (pagination: any, filters: any, sorter: any) => {
    setPagination(pagination);
    setSortField(sorter.field);
    setSortOrder(sorter.order === "ascend" ? "asc" : "desc");    
  };

function clearDate(){
  setStartDate("")
  setEndDate("")
}
  const validateName = (value: string) => {
    setRoleName(value.trim());
    const roleFilteredByName = rolesListNoPage?.roleList.filter((role: any) => {
      return role?.role?.roleName?.toLowerCase() === value.toLowerCase();
    });
    if (roleFilteredByName.length > 0&&!editMode) {
      setRoleNameError("Role Name already exists");
    } else {
      setRoleNameError("");
    }
  };
  return (
    <>
      <div className="role-management">
        <div className="role-header">
          <p className="role-title">Role Management</p>
        </div>

        <Row>
          {/*  <PageHeading title="Role Management" />
           */}
        </Row>
        <Row className="role-body-row">
          <Col
            className="role-nav-col"
            /* style={ {   
    minHeight:"100vh",
     borderRight:"1px solid #dedede" }
} */ xs={6}
            xl={5}
          >
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
                Role Level Modules
              </p>
            </div>
            {levelList
              ?.filter((level: any) => level.status === true)
              ?.map((level: any, index: any) => {
                return (
                  <div
                    className={
                      (/* currentRoleLevel.role === level.levelName|| */levelParam==level.levelNumber)
                        ? "role-level-active"
                        : "role-level"
                    }
                  >
                    <div
                      className="level-container-role" /*  style={{lineHeight:"40px" */ /* borderRadius:"1px",borderColor:"gray" */ /* }} */
                    >
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
                          setCurrentRoleLevel({
                            level: level.levelNumber,
                            levelId: level.levelId,
                            role: level.levelName,
                            showCreate: false,
                          });
                          setEditRecord(false);
                          setCurrentLevel(level);
                          setLevelNumber(level.levelNumber)                          
                          setEditMode(false);
                          setScroll(false);
                          setPageSize(10);
                          setCurrentPage(1);
                          setSelectedOption(0);
                          setRolesSelectData([])
                          form.resetFields();
                          form.setFieldsValue({});
                          setRoleName("");
                          setRoleComments("");                                              
                          clearDate();
                          setFilterBy("");
                          setSearchBy("");
                          setSearchObject({})
                          setLevelsClicked(true)
                          setKey((prevKey) => prevKey + 1);
                          setCurrentRecord({});                          
                          clearRef.current?.clear()
                          setReadOnly(false);
                          const requestParams: any = {
                            page: currentPage || 1,
                            duration: days,
                            currentLevel: index,
                            // sortBy: "STATUS",
                            // sortDirection: "DESC",
                            limit: pageSize,
                          };
                          getRoleListDetails(index);
                          navigate(RouteType.ROLE_MANAGEMENT+'/'+level?.levelNumber, { replace: true })
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
            style={{
              /* boxShadow:"0 0 2px #ccc" */ borderBottom: "1px solid #dedede",
            }}
          >
            <Form form={form} onFinish={onFinish}>
              <div
                className="form-inside"
                style={{
                  /* boxShadow:"0 0 2px #ccc", */ marginRight: "0px",
                  height: "64px",
                  color: "F7F7F7",
                  display: "flex",
                  lineHeight: "64px",
                  alignContent: "center",
                  borderBottom: "1px solid #dedede",
                  borderRight: "1px solid #dedede",
                  borderTop: "1px solid #dedede",
                  backgroundColor: "white",
                  justifyContent: "space-between",
                }}
              >
                {(currentLevel||levelParam!=undefined) && (
                  <p
                    style={{
                      lineHeight: "64px",
                      width: "50%",
                      fontSize: "16px",
                      marginLeft: "16px",
                      fontWeight: 700,
                      fontFamily: "Inter",
                    }}
                  > {(currentRoleLevel?.showCreate || editMode||readOnly)&&
                                            <img
                          src="/images/back-icon.svg"
                          alt="Back"
                          style={{marginRight:"4px"}}
                          className="back-icon"
                          onClick={handleCancel}
                        />}

                    {(currentRoleLevel?.showCreate || editMode||readOnly)?((readOnly?"View":editMode?"Edit":"Create")+` Role for L${levelParam??"0"}. ${_.startCase(levelList.filter((level:any)=>level?.levelNumber==levelParam)[0]?.levelName?.toLowerCase()??'Wadzpay Admin')}`):
                      "L"+(levelParam) +
                      "." +
                      _.startCase(levelList.filter((level:any)=>level?.levelNumber==levelParam)[0]?.levelName?.toLowerCase()??"Wadzpay Admin")+
                    " Roles"}
                  </p>
                )}
                 {(selectedOption > 0 || editMode) && !readOnly && (
                            <div className="form-div" style={{marginLeft:"200px"}}>
                              <Button
                                style={{
                                  color: "000000",
/*                                   marginLeft: "24px",
 */                                  marginRight: "2px",
/*                                   marginBottom: "40px",
 */                                  display: showPermission ? "block" : "none",
                                }}
                                className="role-create-btn"
                                htmlType="submit"
                                disabled={
                                  roleName.length === 0 /* ||roleNameError.length>0||
                                  (!editMode&&selectedRowKeys.length==0) */||
                                  !(
                                    
                                    (editMode&&preservePerm/*  &&
                                      selectedRowKeysFromRoles?.length > 0 */) ||
                                    (editMode && selectedRowKeys?.length > 0) ||
                                    (!editMode &&
                                      selectedOption !== 1 &&
                                      selectedRowKeys?.length > 0) ||
                                    (!editMode &&
                                      selectedOption === 1 &&
                                      selectedRowKeysFromRoles?.length > 0)
                                      ||(!editMode &&
                                        selectedOption === 2 &&
                                        selectedRowKeysFromRoles?.length > 0)
/*                                       ||(!editMode&&selectedOption===3&&selectedRowKeys?.length > 0)
 */                                  )

                                }

                                /*             onClick={()=>{}
            
            }
 */
                              >
                                <p
                                  style={{ color: "black", lineHeight: "40px" }}
                                >
                                  {" "}
                                  {editMode ? "Update" : "Create Role"}
                                </p>
                              </Button>
                              {/* <Button
                                style={{
                                  marginBottom: "40px",
                                  display: showPermission ? "block" : "none",
                                }}
                                className="cancel-button"
                                onClick={() => {
                                  let temp = currentRoleLevel;
                                  temp.showCreate = true;
                                  // setCurrentRoleLevel(temp)
                                  setShowCreate(true);
                                  setEditRecord(false);
                                  setSelectedOption(0);
                                  setCurrentRoleLevel(
                                    (prevCurrentRoleLevel) => ({
                                      ...prevCurrentRoleLevel,
                                      showCreate: false,
                                    })
                                  );
                                  setEditMode(false);
                                }}
                              >
                                <p
                                  style={{ color: "black", lineHeight: "40px" }}
                                >
                                  {" "}
                                  Cancel
                                </p>
                              </Button> */}
                            </div>
                          )}
                {currentRoleLevel.showCreate || editMode ? (
                  <Button
                    style={{
                      color: "000000",
                      alignSelf: "center",
                      marginRight: "8px",
                      /*               marginLeft:"16px",
              marginRight:"8px",
              marginBottom:"40px"
 */
                    }}
                    className="cancel-button"
                    /*             type="submit"*/
                    onClick={handleCancel}

                  >
                    <p style={{ color: "black", lineHeight: "40px" }}>
                      {" "}
                      Cancel
                    </p>
                  </Button>
                ) : (

                  rolesListEmptyCheck?.roleList?.length > 0 &&
                  (currentLevel||!levelsClicked) &&
                  rolesListEmptyCheck?.roleList?.length > 0 &&
                  !(currentRoleLevel.showCreate || editMode) && (
                    <Button
                      className="role-create-btn"
                      onClick={() => {
                        setCurrentPageSelect(1)
                        setRoleName("");
                        setRoleNameError("");
                        setRoleComments("");
                        form.resetFields();
                        onCreateLevel();                        
                        getRoleListDetailsSelect(
                          currentLevel?.levelNumber??levelParam,
                          currentPageSelect,
                          undefined,
                          true
                        );
                        getUserRoleListDetails(currentLevel?.levelNumber);
                      }}
                    >
                      <p style={{ color: "black", lineHeight: "40px" }}>
                        {" "}
                        Create Role
                      </p>
                    </Button>
                  )
                )}
              </div>

              {currentRoleLevel?.level === ""
                ? null
                : (currentRoleLevel.showCreate || editMode) && (
                    <>
                      {
                        /* currentRoleLevel.level!==""&& */ <div
                          style={{ paddingTop: "16px" }}
                        >
                          <label className="label-create" htmlFor="roleName" >
                            Role Name
                          </label>      <span style={{ color: 'red', marginLeft: '4px' }}>*</span>

                          <div /* className="row" */>
                            <div className="col-4 ">
                              <Form.Item
                                name={"roleName"}
                                initialValue={roleName}                                
                                rules={[
                                  {
                                    required: true,
                                    message: "Please enter Name",                                    
                                  },
                                  {
                                    max:30,
                                    message:"Maximum character limit 30 only"
                                  },
                                  {
                                    pattern:new RegExp(/^(?!\s)(?=.*[a-zA-Z0-9])[a-zA-Z0-9\s]*[^\s]$/g),
                                    message:'Alphanumeric and space allowed,No trailing/leading spaces'
                                  }
                                  
                                ]}
                                validateStatus={
                                  roleNameError == "" ? "success" : "error"
                                }
                                help={
                                  roleNameError != "" ? roleNameError : null
                                }
                              >
                                <Input
                                  type="text"
                                  value={roleName}
                                  maxLength={30}
                                  disabled={readOnly}
                                  onChange={(e) => validateName(e.target.value)}
                                  data-testid="roleName"
                                  className="input-create"
                                  aria-autocomplete="both"
                                  style={{color:readOnly?"rgba(0,0,0,0.9)":"rgba(0,0,0,1"}}
                                  aria-haspopup="false"
                                  placeholder="Enter Role Name"

                                  /*             autoComplete="true"
                                   */
                                />
                              </Form.Item>
                            </div>
                            {/*         <Form.Item name={"selectedCopy"}>
                             */}
                            {!editMode && (
                              <>
                                <label className="label-create">
                                  Create Role from                                  
                                </label>
                                <span style={{ color: 'red', marginLeft: '4px' }}>*</span>
                                <div
                                  className="select-popup"
                                  style={{
                                    zIndex: 11,
                                    /* marginLeft:"16px" */
                                  }} /* className="row" */
                                >
                                  <Select
                                    className="input-create-select"
                                    onChange={(e) => {
                                      setScroll(true);
                                      setSelectedOption(Number.parseInt(e));
                                      setselectedRowKeysFromRoles([]);                                   
                                      //setShowPermission(false);
                                      resetRef.current?.reset([]);
                                      form.setFieldValue("roleFrom", "");
                                      setRoleFrom(0);
                                      if (Number.parseInt(e) !== 3) {
                                        setShowPermission(false);
                                      }
                                    }}
                                    getPopupContainer={(trigger) =>
                                      trigger.parentNode
                                    }
                                    defaultValue={selectedCopy}
                                    /* defaultValue={selectedValue} options={options} */ id={
                                      "roleOptions"
                                    }
                                    style={{ width: "33%" }}
                                  >
                                    {/*  <div className="options-container">*/}
                                    <option
                                      className="options"
                                      selected
                                      value={""}
                                    >
                                      Select a role source
                                    </option>
                                    {options.map((option) => {
                                      return (
                                        <option
                                          className="options"
                                          key={option.value}
                                          value={option.value}
                                          disabled={
                                            (option.value == 1 &&
                                              rolesData?.roleList?.length ===
                                                0) ||
                                            (option.value == 2 &&
                                              (userRoles?.length === 0||userRoles==undefined))
                                          }
                                        >
                                          {option.label}
                                        </option>
                                      );
                                    })}
                                    {/* </div>
                                     */}
                                  </Select>
                                </div>
                                {/* </Form.Item>
                                 */}
                                {(selectedOption === 1 ||
                                  selectedOption === 2) && (
                                  <>
                                    <label className="label-create">
                                      {selectedOption === 1
                                        ? "Select Role"
                                        : "Select User"}
                                    </label>
                                    <span style={{ color: 'red', marginLeft: '4px' }}>*</span>
                                    <div
                                      className="select-popup"
                                      style={
                                        {
                                          /* marginLeft:"16px" */
                                        }
                                      } /* className="row" */
                                    >
                                      <Form.Item name="roleFrom">
                                        <Select
                                          className="input-create-select"
                                          showSearch
                                          optionFilterProp="label"
                                          onSearch={(e) => {
                                            setSearchBy(e);
                                            setFilterBy("roleName");
                                            debounce_getRoleListDetails();
                                          }}
                                          onChange={(e) => {
                                            // setPermKey((prevPermKey) => prevPermKey + 1);
                                            if (selectedOption === 1) {
                                              let sected =
                                                rolesSelectData?.filter(
                                                  (item: any) =>
                                                    item.role?.roleId === e
                                                )[0];

                                              resetRef.current?.reset(
                                                sected?.roleModule?.moduleId
                                              );

                                              setselectedRowKeysFromRoles(
                                                sected?.roleModule?.moduleId
                                              );
                                            } else {
                                              let sected = userRoles?.filter(
                                                (item: any) =>
                                                  item?.roleId === e
                                              )[0];

                                              resetRef.current?.reset(
                                                sected?.roleModuleList.map(
                                                  (it: any) => it.moduleId
                                                )
                                              );
                                              setselectedRowKeysFromRoles(
                                                sected?.roleModuleList.map(
                                                  (it: any) => it.moduleId
                                                )
                                              );
                                            }
                                          }}
                                          onPopupScroll={handleScroll}
                                          
                                          getPopupContainer={(trigger) =>
                                            trigger.parentNode
                                          }
                                          defaultValue={roleFrom}
                                          /* defaultValue={selectedValue} options={options} */ id={
                                            "roleOptions"
                                          }
                                          style={{ width: "33%" }}
                                        >
                                          <option selected value={""}>
                                            Select
                                          </option>
                                          {selectedOption === 1
                                            ? rolesSelectData
                                                ?.filter(
                                                  (roleObj: any) =>
                                                    roleObj.role.status === true
                                                )
                                                .map((item: any) => {
                                                  return (
                                                    <option
                                                      key={
                                                        item?.role?.roleId /* +
                                                        Math.random() */
                                                      }
                                                      value={item?.role?.roleId}
                                                      label={
                                                        `${item?.role?.roleName}(${item?.role?.roleId})`
                                                      }
                                                    >
                                                      {item?.role?.roleName}{`(${item?.role?.roleId})`}
                                                    </option>
                                                  );
                                                })
                                            : userRoles.map((item: any) => {
                                                return (
                                                  <option
                                                    key={
                                                      item?.roleId +
                                                      Math.random()
                                                    }
                                                    value={item?.roleId}
                                                    label={item?.userName}
                                                  >
                                                    {item?.userName}{`(${item?.roleId})`}
                                                  </option>
                                                );
                                              })}
                                        </Select>
                                      </Form.Item>{" "}
                                    </div>
                                  </>
                                )}
                              </>
                            )}
                          </div>
                          {(!(
                            selectedOption === -1 ||
                            selectedOption.toString() === ""
                          ) /* ||(form.getFieldValue("roleFrom")!==''&&selectedOption ==1 ) */ /* &&selectedRowKeysFromRoles?.length>0 */ ||
                            editMode) && (
                            <div
                              key={permKey}
                              className="role-table-container"
                              style={{
                                display: showPermission ? "block" : "none",
                              }} /*{ visibility:(selectedOption>1|| */ /* form.getFieldValue("roleFrom")!=0&& */ /* (form.getFieldValue("roleFrom")!==''&&selectedOption ==1 )||(editMode))?'visible':'hidden'} */ /* } */
                              /* style={ */
                            >
                            
{/* { ((!editMode&&noPerms)||(!editMode&&!preservePerm&&selectedRowKeys.length==0)||(editMode&&preservePerm&&selectedRowKeysFromRoles.length==0)||(editMode&&!preservePerm&&selectedRowKeys.length==0))    &&                  <div className="error-message">{"Permissions must be selected"}</div>}
 */} 
                              <div className="role-permission-header">
                                <p className="role-permission-header-text">
                                  Permissions
                                </p>
                                <span
                                  style={{
                                    paddingRight: "34px",
                                    lineHeight: "50px",
                                  }}
                                >
                                  {" "}
                                  {!readOnly && (
                                    <Button
                                      type="link"
                                      style={{ color: "#006BE2" }}
                                      onClick={() => {
                                        if (resetRef !== null) {
                                          resetRef.current?.reset();
                                          setSelectedRowKeys([])
                                         /*  if(!editMode){
                                            setNoPerms(true)
                                          }
                                          if(editMode){
                                            setPreservePerm(true)
                                          } */
                                          
                                        }
                                      }}
                                      disabled={readOnly}
                                    >
                                      <p> Reset All</p>
                                    </Button>
                                  )}
                                </span>
                              </div>
                              <div className="role-attr-container">
                                <span>
                                  {" "}
                                  <p className="attribute-title">
                                    Attribute Name
                                  </p>
                                </span>
                                <span>
                                  {" "}
                                  <p className="read-view"> Access</p>
                                </span>
                              </div>

                              <Permissions
                                editMode={editMode}
                                readOnly={readOnly}
                                currentRows={currentRows}
                                ref={resetRef}
                                keysFromRole={selectedRowKeysFromRoles}
                                defaultData={savedSelectedRowKeys}
                              />
                            </div>
                          )}
                          {(!(
                            selectedOption === -1 ||
                            selectedOption.toString() === ""
                          ) /* showPermission|| */ ||
                            editMode) && (
                            /*  (selectedOption > 0||editMode) && */ <div className="form-div">
                              <div
                                className="col-lg-7"
                                style={{
                                  display: showPermission ? "block" : "none",
                                }}
                              >
                                <label
                                  className="label-create"
                                  style={{ marginBottom: "4px" }}
                                >
                                  Comment
                                </label>

                                <Form.Item
                                  style={{
                                    marginLeft: "24px",
                                    fontFamily: "Inter",
                                    fontSize: "12px",
                                  }}
                                  name={"roleComments"}
                                  /*               label="comment"
                                   */ // rules={[{ required: true, message: "" }]}
                                  initialValue={roleComments}
                                >
                                  <TextArea
                                    style={{ lineHeight: "14px" }}
                                    className="textArea-custom"
                                    disabled={readOnly}
                                    /* showCount */
                                    maxLength={150}
                                    showCount={true}
                                    value={roleComments}
                                    /*                 onChange={()=>{}}
                                     */ /*                 autoComplete="true"
                                     */ placeholder=" Write your comment here"
                                  />
                                </Form.Item>
                              </div>
                            </div>
                          )}
                          {(selectedOption > 0 || editMode) && !readOnly && (
                            <div className="form-div">
                              <Button
                                style={{
                                  color: "000000",
                                  marginLeft: "24px",
                                  marginRight: "8px",
                                  marginBottom: "40px",
                                  display: showPermission ? "block" : "none",
                                }}
                                className="role-create-btn"
                                htmlType="submit"
                                disabled={
                                  roleName.length === 0 ||
                                  !(
                                    
                                    (editMode&&preservePerm /* &&
                                      selectedRowKeysFromRoles?.length > 0 */) ||
                                    (editMode && selectedRowKeys?.length > 0) ||
                                    (!editMode &&
                                      selectedOption !== 1 &&
                                      selectedRowKeys?.length > 0) ||
                                    (!editMode &&
                                      selectedOption === 1 &&
                                      selectedRowKeysFromRoles?.length > 0)
                                      ||(!editMode &&
                                      selectedOption === 2 &&
                                      selectedRowKeysFromRoles?.length > 0)
/*                                       ||(!editMode&&selectedOption===3&&selectedRowKeys?.length > 0)
 */                                  )

                                }

                                /*             onClick={()=>{}
            
            }
 */
                              >
                                <p
                                  style={{ color: "black", lineHeight: "40px" }}
                                >
                                  {" "}
                                  {editMode ? "Update" : "Create Role"}
                                </p>
                              </Button>
                              <Button
                                style={{
                                  marginBottom: "40px",
                                  display: showPermission ? "block" : "none",
                                }}
                                className="cancel-button"
                                onClick={() => {
                                  let temp = currentRoleLevel;
                                  temp.showCreate = true;
                                  // setCurrentRoleLevel(temp)
                                  setScroll(false)
                                  setShowCreate(true);
                                  setEditRecord(false);
                                  setSelectedOption(0);
                                  setCurrentRoleLevel(
                                    (prevCurrentRoleLevel) => ({
                                      ...prevCurrentRoleLevel,
                                      showCreate: false,
                                    })
                                  );
                                  setEditMode(false);
                                }}
                              >
                                <p
                                  style={{ color: "black", lineHeight: "40px" }}
                                >
                                  {" "}
                                  Cancel
                                </p>
                              </Button>
                            </div>
                          )}
                        </div>
                      }
                    </>
                  )}
            </Form>
            {currentRoleLevel?.level === ""
              ? null
              : !currentRoleLevel.showCreate &&
                !editMode && (
                  <div
                    className="tableHeader more-screen-size-600"
                    style={{ marginTop: "8px" }}
                  >
                    <div
                      className="table-responsive"
                      style={{ marginLeft: "16px", font: "Inter" }}
                    >
                      {rolesListEmptyCheck?.roleList?.length==0 &&
                      !showCreate ? (
                        <EmptyRoles onCreateLevel={onCreateLevel} />
                      ) : (
                        /* } */
                        /* { */
                        /* rolesList&&rolesData?.roleList?.length > 0 && */
                        <>
                          <div className="row bg-white rounded mt-2 mb-2">
                            <div className="d-sm-flex align-items-center">
                              <label style={{marginRight:"5px"}}> Duration </label>
                              <DateRangePicker  ref={clearRef} updateDateRange={captureDate} ></DateRangePicker>
                              
                              <div className="ms-1 ps-1 select-popup ">
                                <Dropdown
                                  getPopupContainer={(trigger: any) =>
                                    trigger.parentNode
                                  }

                                  menu={{
                                    items: itemsExport,
                                    onClick: handleDropdownItemClick,
                                  }}
                                  trigger={["click"]}
                                >
                                  <Button
                                    style={{
                                      color: "#000",
                                      border: "1px solid #C4C4C4",
                                      fontWeight: 400,
                                      backgroundColor:"white"
                                                                          
                                    }}
                                    
                                    
                                  >
                                    <Space>Export to</Space>&nbsp;
                                    <img src={"/images/down-arrow.svg"} />
                                  </Button>
                                </Dropdown>
                              </div>
                            </div>
                          </div>
                          <ModalConfirm
                            setShowModalDeActive={confirmDeActivate}
                            deactivateConfirm={showModalDeActive}
                            role={currentRecord}
                          ></ModalConfirm>
                          <div style={{borderRadius:"4px",border:"1px solid #d4d4d4"}}>
                        <Table
                            columns={columns}
                            key={key}
/*                             key={key}
/*                             rowKey={"key"}*/
                           onChange={handleTableChange}                                                     
                            dataSource={rolesList?.roleList?.map((it: any) => {
                              let object: any = {};

                              //it.role.status=(it?.role?.status==true?"active":"deactivated")
                              let role = it?.role;
                              object = {
                                ...role,
                                status:
                                  role?.status == true
                                    ? "active"
                                    : "deactivated",
                              };
                              object.users = it.users;
                              object.module = it?.roleModule?.moduleId;
                              return object;
                            })}
                            size="middle"
                            pagination={false}
                            loading={rolesLoading}                            
                            className="table-custom headerBg" /* pagination={{
                            /*                     pagination={{ pageSize: 10 }}
                             
                            current: currentPage,
                            pageSize: pageSize,
                            total: rolesData?.pagination?.total_records,
                            onChange: handlePaginationChange,
                            showTotal: (total, range) => {
                              return `${range
                                ?.toString()
                                .replace(",", "-")} of Total ${total}`; // return an empty string unless you want to show it next to the pagination
                            },
                          }} */
                          />
{/*                          <p> {listSuccess?"ttt"+rolesList?.pagination.:"faslse"}</p>
 */}                         {renderPagination() }
                          </div>
                        </>
                      )}
                    </div>
                  </div>
                )}
          </Col>
        </Row>
      </div>
    </>
  );
};

export default RoleManagement;

