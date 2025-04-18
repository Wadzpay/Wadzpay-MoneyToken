import {
  Button,
  Checkbox,
  Dropdown,
  Form,
  Input,
  InputNumber,
  MenuProps,
  Popconfirm,
  Radio,
  Steps,
  Table,
  Typography,
  notification,
} from "antd";
import React, { useContext, useEffect, useRef, useState } from "react";
import { Pos, PosCreate } from "src/api/models";
import {
  patternIp,
  useValidationSchemas,
} from "src/constants/validationSchemas";
import { useCreatePos, useDeletePos, useEditPos, useGetPosList } from "src/api/user";
import dayjs from "dayjs";
import moment from "moment";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import PageHeading from "src/components/ui/PageHeading";
import { SaveContext } from "src/context/SaveContext";
import { RouteType } from "src/constants/routeTypes";
const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 },
];
function AddPos(props:any /*{
  /* posList: Array<Pos>, */
  /* addPosToOutlet: (args: any,flag:boolean) => void, */
  /* handleCallBack: (value: any) => void, */
  /* posDetails: any, */
  /* form: any, formRef: any, editMode: boolean, addMode: boolean, */
  /* isSavePos:boolean,isListing:boolean, */
  /* showFormProps:boolean, */
  /* setIsListing:(flag:boolean)=>void,setIsSavePos:(flag:boolean)=>void , */
  /* removePosInOutlet: (args: any, flag: boolean) => void, */
  /* setShowFormProps:(flag:boolean)=>void 
}*/
) {
  /* const [editMode, setEditMode] = useState(props.editMode)
  const [addMode, setAddMode] = useState(props.addMode) */
  const [posList, setPosList] = useState<any>([]);
  const { isSave, setIsSave, isSaveAndClose, setIsSaveAndClose } =
  useContext(SaveContext);
  const [pageSize, setPageSize] = useState(10)
  const {
    aggregatorID,
    aggregatorName,
    instituteID,
    instituteName,
    merchantGroupID,
    merchantGroupName,
    merchantAcquirerId,
    merchantAcquirerName,
    subMerchantId,
    subMerchantName,
    outletId,
    outletName,
  } = useParams();
  const location = useLocation();
  const [current, setCurrent] = useState(0);
  const [posDetails, setPosDetails] = useState(location?.state);
  const [statusPos, setStatusPos] = useState(posDetails?.status ?? "active");
  const [form] = Form.useForm();
  const [currentPage, setCurrentPage] = useState<number>();

  let posStatusKey = {
    active: { activeKey: 1, status: "active" },
    inActive: { inActiveKey: 2, status: "inactive" },
  };
  const {
    data,
    mutate:getPosListFromApi,
    isSuccess: isPosDataSuccess,
  } = useGetPosList();
  const [posId, setPosId] = useState("");
  const [posIdError, setPosIdError] = useState(false);
  const [active, setActiveChecked] = useState(posDetails?.status);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(/* props.showFormProps?? */ false);
  const [error, setError] = useState({
    ipAddressError: null,
    serialNumberError: null,
    PosIdError: null,
    posMacAddressError: null,
  });
  const formRef = useRef<HTMLInputElement>(null);
  const [posModel, setPosModel] = useState();
  const [posManufacturer, setPosManufacturer] = useState();
  const [posSerialNum, setPosSerialNum] = useState();
  const [posMacAddress, setPosMacAddress] = useState();
  const [posIPAddress, setPosIPAddress] = useState();
  const [editingKey, setEditingKey] = useState<string>("");
  const [search, setSearch] = useState<boolean>(false);
  const [status, setStatus] = useState();
  const [filteredData, setFilteredData] = useState();
  const [validFromEdit, setValidFromEdit] = useState<string | Date>("");
  const [isButtonDisabled, setIsButtonDisabled] = useState<boolean>(false);
  const [deactivateRow, setDeactivateRow] = useState<any>();
  const [isDeactive, setIsDeactive] = useState<boolean>(false);
  const [editMode, setEditMode] = useState(location?.state?.update?.update??false);
  const [showRegister, setShowRegister] = useState(location?.state?.showRegister?.register??false);

  const navigate = useNavigate();
  const {
    mutate: createPosDetails,
    error: createPosError,
    isSuccess,
  } = useCreatePos();
  const {
    mutate: editPos,
    error: editPosError,
    isSuccess: editPosSuccess,
  } = useEditPos();
  const {
    mutate: deletePosById,
    error: deletePosError,
   isSuccess: isSuccessDelete,
  } = useDeletePos();
  const validateId = (value: string) => {
    setPosId(value);
    let isValid=true
    let PosIdError;
    if (posList)
      posList.filter((pos: any) => {
        if (pos.posId.toLocaleLowerCase() == value) {
          PosIdError = "Pos Id already Exists";
          isValid=false
        }
      });
    let errCopy = JSON.parse(JSON.stringify(error));
    errCopy.PosIdError = PosIdError;
    setError(errCopy);
    return isValid
  };
  const submitPosDetails = (values: any) => {
   form.setFieldValue("status", statusPos);
    /* if(!form.getFieldsValue().posKey){
      form.setFieldValue("posKey", posDetails.posKey);
    } */
    let f = form.getFieldsValue();

    let request: PosCreate = {
      aggregatorPreferenceId: aggregatorID,
      insitutionPreferenceId: instituteID,
      merchantGroupPreferenceId: merchantGroupID,
      merchantAcquirerPreferenceId: merchantAcquirerId,
      subMerchantPreferenceId: subMerchantId,
      outletPreferenceId: outletId,      
      posId: form.getFieldValue("posId"),
      posFirmwareVersion: form.getFieldValue("posFirmwareVersion"),
      posIPAddress: form.getFieldValue("posIPAddress"),
      posMacAddress: form.getFieldValue("posMacAddress"),
      posManufacturer: form.getFieldValue("posManufacturer"),
      posSerialNum: form.getFieldValue("posSerialNum"),
      posModel: form.getFieldValue("posModel"),
      status: form.getFieldValue("status"),
    };

    form.validateFields()
    .then((values) => {
      if (editMode) {
        Object.assign(request,{posUniqueId:posDetails.posUniqueId})
        editPos(request);
      }else{
        if(!validateId(form.getFieldValue("posId"))){
          notification["error"]({
            message: "Notification",
            description: `${error?.PosIdError}`,
          });
    
      }
      else{
        Object.assign(request,{posUniqueId:""})
        createPosDetails(request);
      }}
  
      /*   if(!addMode)
     { 
      f.posKey=posDetails.posKey
        }
   */
      //props.addPosToOutlet(f,addMode)
      setPosDetails(form.getFieldsValue());
      //addPostoList(form.getFieldsValue(),addMode)
      setShowForm(false);
      // props.setShowFormProps(false)
      //props.setIsListing(true)
    }) 
    .catch((errorInfo) => {
      notification["error"]({
        message: "Notification",
        description: `One or more fields are not valid`,
      });

    });

    
  };
  // console.log(editMode,addMode)
  /* useEffect(() => {
    return () => {
      // props.setShowFormProps(false)
    };
  }, []); */
  useEffect(() => {
    if (isSave || isSaveAndClose) {
      navigate(RouteType.AGGREGATOR_MANAGEMENT);

    }}
    ,[isSave,isSaveAndClose])
  useEffect(() => {
    if (data) setPosList(data);
  }, [data]);
  useEffect(() => {
   let message=""
    if(editPosSuccess){
message="Pos have been updated successfully."
    }
    if(isSuccess){
      message="Pos have been created successfully."

    }
    if(isSuccessDelete){
      message="Pos  deleted successfully."
    }
   if(message!="")
      notification["success"]({
        message: "Notification",
        description: message,
        placement: "bottomRight",
      });    
    if (isSuccess||editPosSuccess||isSuccessDelete) {
      fetchPosList();
      setPosDetails({})
    }

  }, [isSuccess,editPosSuccess,isSuccessDelete]);

  useEffect(() => {
    if (loading) {
      fetchPosList();
      setLoading(false);
    }
  }, [loading]);

  const fetchPosList=()=>{
    const requestParams: any = {
      page: currentPage || 1,
      aggregatorPreferenceId: aggregatorID,
      institutionPreferenceId: instituteID,
      merchantGroupPreferenceId: merchantGroupID,
      merchantAcquirerPreferenceId:merchantAcquirerId,
      subMerchantPreferenceId:subMerchantId,
      outletPreferenceId: outletId,

      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: pageSize,
    };
    getPosListFromApi(requestParams)
  }
  const onFinish = (values: any) => {
    //save pos
    if (!values.status) values.posStatus = "active";
    //props.saveInstitutionData(values);

    if (posList && posList?.length > 0) {
      //props.setIsListing(true)
    }
    // props.handleCallBack(true);

    // props.setIsSavePos(true)
    //props.form.resetFields()
    /*     if (editMode){}
     */
  };

  const addPostoList = (pos: Pos, add: boolean) => {
    if (pos && posList) {
      /*   console.log(editMode,addMode)
       */ if (add) {
        setPosList([...posList, pos]);
      } else {
        setPosList([
          ...posList.filter((e: any) => e.posKey != pos.posKey),
          pos,
        ]);
      }
    }
  };

  const onFinishFailed = (errorInfo: any) => {
    console.log(errorInfo);
    //props.handleCallBack(false);
  };
  const updateEditMode = (flag: boolean, pos: Pos) => {
    // setEditMode(flag)
    setPosDetails(pos);
  };
  const updateAddMode = (flag: boolean, details: boolean) => {
    if (details) {
      setPosDetails({});
    }
    // setAddMode(flag)
  };
  /*  useEffect(() => {
    if (addMode)
      setPosDetails({})
  }, [addMode])
  */

  const isObjectEmpty = (objectName: any) => {
    return (
      objectName &&
      Object.keys(objectName).length === 0 &&
      objectName.constructor === Object
    );
  };
 
  const validateSerialNum = (value: string) => {
    let serialNumberError;
    if (posList)
      posList.filter((pos: any) => {
        if (pos.posSerialNum == value) {
          serialNumberError = "Pos SerialNumber already Exists";
        }
      });
    let errCopy = JSON.parse(JSON.stringify(error));
    errCopy.serialNumberError = serialNumberError;
    setError(errCopy);
  };
  const validateIP = (value: string) => {
    setPosId(value);
    let ipAddressError;
    if (posList)
      posList.filter((pos: any) => {
        if (pos.posIPAddress?.toLocaleLowerCase() == value) {
          ipAddressError = "Pos Ip already Exists";
        }
      });
    let errCopy = JSON.parse(JSON.stringify(error));
    errCopy.ipAddressError = ipAddressError;
    setError(errCopy);
  };
  const validateMac = (value: string) => {
    setPosId(value);
    let posMacAddressError;
    if (posList)
      posList.filter((pos: any) => {
        if (pos.posMacAddress?.toLocaleLowerCase() == value) {
          posMacAddressError = "Mac address already Exists";
        }
      });
    let errCopy = JSON.parse(JSON.stringify(error));
    errCopy.posMacAddressError = posMacAddressError;
    setError(errCopy);
  };
  useEffect(() => {
    if (loading) {
      setLoading(false);
    }
  }, [loading]);

  const currentDate = () => {
    return dayjs(moment().format())
      .tz("Asia/Kolkata")
      .format("D MMM YYYY, hh:mm:ss a");
  };
  const isEditing = (record: Pos) => record.posKey === editingKey;
  const cancel = () => {
    setEditingKey("");
  };
  const save = async (key: React.Key) => {
    try {
      const row = (await form.validateFields()) as Pos;
      if (posList) {
        const newData = [...posList];
        const index = newData.findIndex((item) => key === item.posKey);
        if (index > -1) {
          const item = newData[index];
          let request: PosCreate = {
            aggregatorPreferenceId: aggregatorID,
            insitutionPreferenceId: instituteID,
            merchantGroupPreferenceId: merchantAcquirerId,
            merchantAcquirerPreferenceId: merchantAcquirerId,
            subMerchantPreferenceId: subMerchantId,
            outletPreferenceId: outletId,
            posId: item.posId,
            posManufacturer: row.posManufacturer!!,
            posIPAddress: row.posIPAddress!!,
            posSerialNum: row.posSerialNum!!,
            posFirmwareVersion: row.posFirmwareVersion,
            status: row.status,
            posMacAddress: row.posMacAddress!!,
            posModel: row.posModel!!,
          };
          editPos(request);
          setEditingKey("");
        } else {
          newData.push(row);
          setPosList(newData);
          setEditingKey("");
        }
      }
    } catch (errInfo) {
      console.log("Validate Failed:", errInfo);
    }
  };
  const deactivatePos = () => {
    if (deactivateRow != null) {
      let request: PosCreate = {
        aggregatorPreferenceId: aggregatorID,
        insitutionPreferenceId: instituteID,
        merchantGroupPreferenceId: merchantAcquirerId,
        merchantAcquirerPreferenceId: merchantAcquirerId,
        subMerchantPreferenceId: subMerchantId,
        outletPreferenceId: outletId,
        posId: deactivateRow.posId,
        posManufacturer: deactivateRow.posManufacturer!!,
        posIPAddress: deactivateRow.posIPAddress!!,
        posSerialNum: deactivateRow.posSerialNum!!,
        posFirmwareVersion: deactivateRow.posFirmwareVersion,
        status: 'inactive',
        posMacAddress: deactivateRow.posMacAddress!!,
        posModel: deactivateRow.posModel!!,
      };
      editPos(request);
      setIsDeactive(true);
    }
  };
  useEffect(() => {  
    form.resetFields();
   
  }, [posDetails])
  
  const updatePosRow = () => {
    /*     updateEditMode(true, posDetails)
    updateAddMode(false, false)
    setShowForm(true)
 */ //props.setShowFormProps(true)
    setEditMode(true);
    form.setFieldsValue(posDetails);

    // setPosDetails(posDetails);
  };
  const closePos = () => {
    //let posId={pos}
    posDetails["status"] ="closed"
  editPos(posDetails)

    //props.removePosInOutlet(posDetails,true)
  };
  const updatePosDetails = (args: any, flag: boolean) => {
    // props.removePosInOutlet(args,false)
  };
  const addPos = () => {
/*     let request: PosCreate = {
      aggregatorPreferenceId: aggregatorID,
      insitutionPreferenceId: instituteID,
      merchantGroupPreferenceId: merchantAcquirerId,
      merchantAcquirerPreferenceId: merchantAcquirerId,
      subMerchantPreferenceId: subMerchantId,
      outletPreferenceId: outletId,
      posId: form.getFieldValue("posId"),
      posFirmwareVersion: form.getFieldValue("posFirmwareVersion"),
      posIPAddress: form.getFieldValue("posIPAddress"),
      posMacAddress: form.getFieldValue("posMacAddress"),
      posManufacturer: form.getFieldValue("posManufacturer"),
      posSerialNum: form.getFieldValue("posSerialNum"),
      posModel: form.getFieldValue("posModel"),
      status: form.getFieldValue("status"),
    };
    createPosDetails(request);
 */     setEditMode(false)
    setPosDetails({})
    form.resetFields();
    setShowForm(true);
    
    // props.setShowFormProps(true)
    //updateAddMode(true, true);
  };
  const edit = (record: Partial<Pos> & { key: React.Key }) => {
    form.setFieldsValue({ ...record });
    if (record.posId) setEditingKey(record?.posKey ?? posId);
    setValidFromEdit("");
  };
  const handleChange = (e: any, name: string) => {
    if (name === "posId") {
      setPosId(e);
    }
    if (name === "posModel") {
      setPosModel(e);
    }
    if (name === "posIPAddress") {
      setPosIPAddress(e);
    }
    if (name === "posMacAddress") {
      setPosMacAddress(e);
    }
    if (name === "posManufacturer") {
      setPosManufacturer(e);
    }
    if (name === "posSerialNum") {
      setPosSerialNum(e);
    }
    if (name === "status") {
      setStatus(e);
    }
    if (name === "search") {
      setSearch(e.target.value === "" ? false : true);
      const cdata = posList?.map((element: any, key: number) => {
        return {
          key: element.posId,
          posId: element.posId,
          posManufacturer: element.posManufacturer,
          posMacAddress: element.posMacAddress,
          posModel: element.posModel,
          posIPAddress: element.posIPAddress,
          posSerialNum: element.posSerialNum,
          status: element.status,
        };
      });
      const filteredData: any = cdata?.filter((res: any) => {
        return JSON.stringify(res)
          .toLocaleLowerCase()
          .match(e.target.value.toLocaleLowerCase());
      });
      setFilteredData(filteredData);
    }
  };
  const actionItems: any = [
    {
      key: "3",
      label: (
        <Popconfirm title="Sure to delete?" onConfirm={() => closePos()}>
          <span>Close</span>
        </Popconfirm>
      ),
    },
  ];

  const actionItemsBlock: any = [
    {
      key: "1",
      label: <a onClick={() => updatePosRow()}>Update</a>,
    },
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to deactivate?" onConfirm={() => blockPos()}>
          <span>De Activate</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsUnblock: any = [
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to Activate?" onConfirm={() => blockPos()}>
          <span>Activate</span>
        </Popconfirm>
      ),
    },
  ];
  const blockPos = () => {
    posDetails["status"] =
      posDetails.status == "active" ? "inactive" : "active";
    editPos(posDetails)
    //updatePosDetails(posDetails, false);
    //setStatusPos(posDetails["status"])
    //setPosDetails({});
  };

  const updatePosActive = () => {
    if (statusPos !== "active") {
      setStatusPos("active");
    }

    //setPosDetails({});
  };
  useEffect(() => {
    if (isPosDataSuccess) {
      setPosList(data);
    }
  }, [isPosDataSuccess]);

  const updatePosInActive = () => {
    if (statusPos == "active") {
      setStatusPos("inactive");
    }

    //setPosDetails({});
  };
  interface EditableCellProps extends React.HTMLAttributes<HTMLElement> {
    editing: boolean;
    dataIndex: string;
    title: any;
    inputType: "number" | "text";
    record: Pos;
    index: number;
    children: React.ReactNode;
  }
  const EditableCell: React.FC<EditableCellProps> = ({
    editing,
    dataIndex,
    title,
    inputType,
    record,
    index,
    children,
    ...restProps
  }) => {
    let inputNode;
    let rules: any;
    if (dataIndex === "posId") {
      inputNode = (
        <Input
          value={record.posId}
          size="large"
          placeholder="posId"
          style={{ width: "100%" }}
        />
      );
      rules = [
        {
          required: false,
        },
      ];
    }
    /* if (dataIndex === "posName") {
      inputNode = (
        <Input
          value={record.posName}
          size="large"
          placeholder="Pos Name"
          style={{ width: "100%" }}
        />
      )

      rules = [
        {
          required: false,
          message: ""
        }
      ]
    } */
    if (dataIndex === "status") {
      inputNode = (
        <Input
          value={record.status}
          placeholder="status"
          size="large"
          style={{ width: "100%" }}
        />
      );
    }
    const steps = [
      {
        key: 0,
        title: "View Pos Terminals",
        content: "Pos Terminals",
      },
    ];

    const items = steps.map((item: any, key: number) => ({
      key: item.title,
      title: item.title,
      description: key < current ? "Completed" : "",
    }));

    return (
      <td {...restProps}>
        {editing ? (
          <>
            <div
              style={{ width: "222% !important" }}
              // className="tableRows tableRowsEdit more-screen-size-600"
            ></div>
            <Form.Item name={dataIndex} style={{ margin: 0 }} rules={rules}>
              {inputNode}
            </Form.Item>
          </>
        ) : (
          children
        )}
      </td>
    );
  };
  const columns = [
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>ID </div>
        </>
      ),
      dataIndex: "posId",
      width: "16%",
      editable: true,
/*       defaultSortOrder: "ascend",
 */      ellipsis: true,

/*       sortDirections: ["ascend", "descend", "ascend"],
 *//*       sorter: (a: any, b: any) => a?.posId?.localeCompare(b?.posId),
 */      showSorterTooltip: false,
      render: (key: string) => (
        <>
          <div className="tableRows more-screen-size-600"></div>
          {key}
{/*           {posId}
 */}        </>
      ),
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>MODEL </div>
        </>
      ),
      dataIndex: "posModel",
      width: "100px",
      editable: true,
      ellipsis: true,
/*       defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a?.posModel?.localeCompare(b?.posModel),
      showSorterTooltip: false, */
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            MANUFACTURER{" "}
          </div>
        </>
      ),
      dataIndex: "posManufacturer",
      width: "17%",
      editable: true,
      ellipsis: true,

      /* defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) =>
        a?.posManufacturer?.localeCompare(b?.posManufacturer),
      showSorterTooltip: false,
  */   },

    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            SERIAL NUMBER{" "}
          </div>
        </>
      ),
      dataIndex: "posSerialNum",
      width: "120px",
      editable: true,
      ellipsis: true,

/*       defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"], */
/*       sorter: (a: Pos, b: Pos) => a?.posSerialNum?.localeCompare(b?.posSerialNum!!),
       showSorterTooltip: false,*/

      render: (posSerialNum: string) => <>{posSerialNum}</>,
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            MAC ADDRESS{" "}
          </div>
        </>
      ),
      dataIndex: "posMacAddress",
      width: "140px",
      editable: true,
      ellipsis: true,
     /*  defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) =>
        a?.posMacAddress?.localeCompare(b?.posMacAddress),
      showSorterTooltip: false, */
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            IP ADDRESS{" "}
          </div>
        </>
      ),
      dataIndex: "posIPAddress",
      width: "100px",
      editable: true,
      ellipsis: true,

      /* defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.posIPAddress?.localeCompare(b.posIPAddress),
      showSorterTooltip: false, */
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            FIRMWARE VERSION{" "}
          </div>
        </>
      ),
      dataIndex: "posFirmwareVersion",
      width: "140px",
      editable: true,
      ellipsis: true,
      /* defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) =>
        a.posFirmwareVersion.localeCompare(b.posFirmwareVersion),
      showSorterTooltip: false, */
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>STATUS </div>
        </>
      ),
      width: "17%",
      editable: true,
      dataIndex: "status",
      key: "status",
/*       sorter: (a: any, b: any) => a.status?.localeCompare(b.status),
 */
      render: (status: string) =>
        status === "active" ? (
          <>
            <img src={"/images/active-icon.svg"} /> {"Active"}
          </>
        ) : (
          <>
            <img src={"/images/pendng-icon.svg"} /> {"In-active"}
          </>
        ) /* : (
          status === "block" ? <span style={{ color: "#D15241" }}>
            <img src={"/images/blocked-icon.svg"} /> {"Blocked"}
          </span> : "In-Active"+status
        ) */,
    },
    {
      title: "",
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...(record?.status === "active"
                ? actionItemsBlock
                : actionItemsUnblock),
              ...actionItems,
            ],
          }}
          trigger={["click"]}
          arrow
        >
          <Typography.Link onClick={() =>{
            setPosDetails(record)
            setEditMode(true)
          }
            }>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
  ];
  const items: MenuProps["items"] = [
    {
      label: (
        <Popconfirm
          title="Sure to deactive?"
          onConfirm={() => deactivatePos()}
        >
          <span>Deactivate</span>
        </Popconfirm>
      ),
      key: "0",
    },
  ];
  const mergedColumns = columns.map((col: any) => {
    if (!col.editable) {
      return col;
    }
    return {
      ...col,
      onCell: (record: Pos) => ({
        record,
        inputType: col.dataIndex === "age" ? "number" : "text",
        dataIndex: col.dataIndex,
        title: "",
        editing: isEditing(record),
      }),
    };
  });
  const steps = [
    {
      key: 0,
      title: "Pos Terminals",
      content:
        "These details will reflect in the wadzpay system by individual Outlet",
    },
  ];
  const stepItems = steps.map((item: any, key: number) => ({
    key: item.title,
    title: item.title,
    description: key < current ? "Completed" : "",
  }));
  useEffect(() => {
    fetchPosList();
  }, [currentPage, pageSize]);
  const handlePaginationChange = (pagination:any) => {
    setCurrentPage(pagination);
    setPageSize(pageSize);
  };
  return (
    <div className="row bg-white institution-div p-1" style={{ width: "105%" }}>
      {location?.state && !location.state.direct ? (
        <PageHeading
          topTitle="Pos management" 
          backIcon={true}
          title={"Edit Pos"}
          submitButton="Close"
          cancelTitle="Cancel"
          current={current}
          max={0}
          isEdit={true}
        />
      ) : (
        <PageHeading
          topTitle="Pos management"
          backIcon={true}
          title="Register Pos"
          cancelTitle="Cancel"
          buttonTitle="Save & Close"
          submitButton="Close"
          current={current}
          max={0}
        />
      )}
      <hr className="mt-2" style={{ color: "#b9b9b9" }} />
      <div className="col-lg-3 mt-3 mb-5 steps-div">
        <Steps direction="vertical" current={current} items={stepItems} />
      </div>
      <div className="col-lg-8 mt-5 mb-5">
        <div className="title">
          {steps[current]?.title}
          {current === 4 && showForm && (
            <Button
              style={{ background: "#ffffff", float: "right" }}
              onClick={(e) => setShowForm(false)}
            >
              {" "}
              Close
            </Button>
          )}
        </div>
        <div className="content mb-4">{steps[current]?.content}</div>
        <Form
          name="pos"
          form={form}
          // onFinish={(e)=>console.log('onFinish called',onFinish(e))}
          onFinish={onFinish}
          onFinishFailed={onFinishFailed}
          autoComplete="off"
        >
          {
            /* ((addMode || editMode)&& */ /* props.showFormProps) && */ <>
              <div className="form-div-pos">
                <div className="col-lg-14">
                  <Form.Item
                    name={"posId"}
                    label="POS ID"
                     validateStatus={error.PosIdError==null?'success':'error'}
              help={error.PosIdError!=null?error.PosIdError:null} 

                    initialValue={
                      posDetails != undefined
                        ? /* editMode ?
                           */ posDetails?.posId ?? ""
                        : ""
                    }
                    rules={[{ required: true, message: "Please enter Pos ID" },
                  {pattern:new RegExp('^.{8}$'),message:"Must be 8 character length"}]}
                  >
                    <Input
                      placeholder="Enter Pos Device ID"  onChange={e=>
               validateId(e.target.value) } 
                    />
                  </Form.Item>
                </div>
                <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="posIdConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>

              </div>
              <div className="form-div-pos">
                <div className="col-lg-14">
                  <Form.Item
                    name={"posModel"}
                    label="Model"
                    initialValue={
                      posDetails != undefined /* editMode ? */
                        ? posDetails.posModel ?? ""
                        : ""
                    }
                    // rules={[{ required: true, message: "" }]}
                  >
                    <Input placeholder="Enter POS Device Model" />
                  </Form.Item>
                </div>
              </div>
              <div className="form-div-pos">
                <div className="col-lg-14">
                  <Form.Item
                    name={"posManufacturer"}
                    label="Manfacturer"
                    initialValue={
                      posDetails != undefined /* editMode ? */
                        ? posDetails?.posManufacturer ?? ""
                        : ""
                    }
                    rules={[{ required: true, message: "Manfacturer is required" }]}
                  >
                    <Input placeholder="Enter POS Device Manfacturer" />
                  </Form.Item>
                </div>
              </div>
              <div className="form-div-pos">
                <div className="col-lg-14">
                  <Form.Item
                    name="posSerialNum"
                    label="Serial Number"
                    validateStatus={error.serialNumberError==null?'success':'error'}
              help={error.serialNumberError!=null?error.serialNumberError:null}   

                    initialValue={
                      posDetails != undefined /* editMode ? */
                        ? posDetails.posSerialNum ?? ""
                        : ""
                    }
                    rules={[
                      { required: true,message:"Please enter Pos Serial Number" },

                      {
                        pattern: new RegExp("^[0-9]*$"),
                        message: "Please enter valid SerialNumber ",
                      },
                    ]}
                  >
                    <Input
                      placeholder="Enter POS Device Serial Number" /* onChange={e=>validateSerialNum(e.target.value)} */
                    />
                  </Form.Item>
                </div>
              </div>
              <div className="form-div-pos">
                <div className="col-lg-14">
                  <Form.Item
                    name={"posMacAddress"}
                    label="MAC Address"
                    initialValue={
                      posDetails != undefined /* editMode ? */
                        ? posDetails?.posMacAddress ?? ""
                        : ""
                    }
                    rules={[{ required: true, message: "Please enter MAC Address" }]}
                     validateStatus={error.posMacAddressError==null?'success':'error'}
              help={error.posMacAddressError!=null?error.posMacAddressError:null}   
                  >
                    <Input
                      placeholder="Enter POS Terminal MAC Address" /* onChange={e=>validateMac(e.target.value)} */
                    />
                  </Form.Item>
                </div>
              </div>
              <div className="form-div-pos">
                <div className="col-lg-14">
                  <Form.Item
                    name="posIPAddress"
                    label="IP Address"
                    initialValue={
                      posDetails != undefined /* editMode ? */
                        ? posDetails.posIPAddress ?? ""
                        : ""
                    }
                      validateStatus={error.ipAddressError==null?'success':'error'}
              help={error.ipAddressError!=null?error.ipAddressError:null}  
                    rules={[
                      { required: true,message:"Please enter Ip Address" },
                      {
                        pattern: new RegExp(patternIp),
                        message: "Please enter valid Ip Address",
                      },
                    ]}
                  >
                    <Input
                      placeholder="Enter POS Device IP Address" /* onChange={e=>validateIP(e.target.value)} */
                    />
                  </Form.Item>
                </div>
              </div>
              <div className="form-div-pos">
                <div className="col-lg-14">
                  <Form.Item
                    name="posFirmwareVersion"
                    label="Firmware Version"
                    initialValue={
                      posDetails != undefined /* editMode ? */
                        ? posDetails.posFirmwareVersion ?? ""
                        : ""
                    }
                    // rules={[{ required: true, message: "" }]}
                  >
                    <Input placeholder="Enter POS Device Firmware Version" />
                  </Form.Item>
                </div>
              </div>
              <div className="form-div-pos">
                <div className="col-lg-14">
                  <Form.Item
                    name="status"
                    label="Status"
                    valuePropName="checked"
                    initialValue={
                      posDetails?.status == undefined
                        ? "active"
                        :( posDetails?.status ==="active" 
                        ? "active" : posDetails?.status ==="inactive" ? "inactive"
                        : "active")
                    }
                    // rules={[{ required: true, message: "" }]}
                  >
                    <Radio.Group defaultValue={posDetails?.status
                       /*  ? "active"
                        :( posDetails?.status ==="active" 
                        ? "active" : posDetails?.status ==="inactive" ? "inactive"
                        : "active") */}>
                      <Radio
                        onChange={() => updatePosActive()}
                        checked={posDetails?.status == "active" }
                        value={"active"}
                        key={
                          1
                        } /* checked={posDetails.status===1||posDetails.status==undefined} */
                      >
                        Active
                      </Radio>
                      <Radio
                        key={2}
                        checked={posDetails?.status === "inactive"}
                        onChange={() => updatePosInActive()}
                        value={"inactive"}
                      >
                        Inactive
                      </Radio>
                    </Radio.Group>
                  </Form.Item>
                
                </div>
              </div>

              <div className="form-div-pos  ">
                <div className="col-lg-8  align:flex-start justify-content:right">
                  <Form.Item>
                    <Button
/*                     disabled={form.validateFields||error.PosIdError!=null||error.ipAddressError!=null||error.posMacAddressError!=null||error.serialNumberError!=null} 
 */                      style={{
                        background: "#faad14",
                        color: "#000000",
                        width: "100px",
                      }}
                      onClick={submitPosDetails}
                      /*  htmlType="submit" ref={props?.formRef} */
                    >
                      {editMode?"Update" : "Create"}
                    </Button>
                  </Form.Item>
                </div>
              </div>
            </>
          }
          {/* <PosList updateEditMode={updateEditMode} posList={props.posList} handleCallBack={props.handleCallBack} updateAddMode={updateAddMode} removePosInOutlet={props.removePosInOutlet} /> */}
          <div className="configurations conversion-rate">
            <div className="p-2 ms-1">
              <div className="row bg-white mt-0">
                <div className="row bg-white rounded mt-2 d-flex justify-content-start">
                  <div className="col-xl-4 col-lg-8 col-sm-12 mt-2 mb-2 ">
                    <Input
                      type="search"
                      size="large"
                      placeholder="Search"
                      onChange={(e) => handleChange(e, "search")}
                      style={{ width: "100%" }}
                      suffix={
                        !search ? (
                          <img src={"/images/search-icon.svg"} />
                        ) : (
                          <span style={{ display: "none" }} />
                        )
                      }
                    />
                  </div>
                  <div className="col-xl-6 col-lg-6 col-lg-6 mt-1 ml-5  d-flex justify-content-end">
                    {showRegister&&
                      /* !props.showFormProps&& */ <Button
                        className="ml-5"
                        style={{
                          background: "#faad14",
                          color: "#000000",
                        }}
                        onClick={addPos}
                      >
                        Add Pos Terminal
                      </Button>
                    }
                  </div>
                  <div className="table-responsive">
                    <Table
                      className="table-custom conversion-rates"
                      components={{
                        body: {
                          cell: EditableCell,
                        },
                      }}
                      scroll={{ x: "max-content" }}
                      dataSource={!search ? posList : filteredData}
                      columns={mergedColumns}
                      loading={loading}
                      pagination={{
                        current: posList?.pagination?.current_page,
                        pageSize: pageSize,
                        total: posList?.pagination?.total_records,
                        onChange: handlePaginationChange,
                      }}

                      rowClassName="editable-row"
/*                       pagination={false}
 */                      locale={{ emptyText: "No Pos List found!" }}
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
          <Form.Item
            initialValue={posDetails?.posKey}
            name={posDetails?.posKey}
            style={{ display: "none" }}
          >
            <Input defaultValue={posDetails?.posKey} />
          </Form.Item>
          <Form.Item style={{ display: "none" }}>
            <Button htmlType="submit" ref={formRef}>
              Save
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
}

export default AddPos;
