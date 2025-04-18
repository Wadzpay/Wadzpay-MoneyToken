import "./index.css";
import { useEffect, useState } from "react";
import {
  useCreateModule,
  useGetModuleList,
  useGetModuleListTree,
  useUpdateModule,
} from "src/api/user";

import {
  Button,
  Cascader,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Select,
  Switch,
  Table,
  Tooltip,
  Typography,
} from "antd";
import { RmModule } from "src/api/models";
import PageHeading from "src/components/ui/PageHeading";
import { itemType } from "src/api/constants";
import EmptyRoles from "./EmptyModules";
import EmptyModules from "./EmptyModules";

const EditableCell = ({
  editing,
  dataIndex,
  title,
  inputType,
  record,
  index,
  children,
  ...restProps
}: any) => {
  const inputNode = inputType === "number" ? <InputNumber /> : <Input />;
  return (
    <td {...restProps}>
      {editing ? (
        <Form.Item
          name={dataIndex}
          style={{
            margin: 0,
          }}
          rules={[
            {
              required: true,
              message: `Please Input ${title}!`,
            },
          ]}
        >
          {inputNode}
        </Form.Item>
      ) : (
        children
      )}
    </td>
  );
};
const ModuleManagement = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [currentPage, setCurrentPage] = useState<number>();
  const [pageSize, setPageSize] = useState(10);
  const [moduleList, setModuleList] = useState<any>([]);
  const [moduleListTree, setModuleListTree] = useState<any>([]);
  const [selectedOption, setSelectedOption] = useState(0);
  const [selectedModuleType, setSelectedModuleType] = useState("");
  const {
    data: treeList,
    mutate: getModuleListTree,
    isSuccess: isModuleTreeDataSuccess,
  } = useGetModuleListTree();

  const [form] = Form.useForm();
  const [form2] = Form.useForm();
  const [copySuccess, setCopySuccess] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [selectedCopy, setSelectedCopy] = useState("");

  const initial = {
    moduleName: "initial",
    moduleType: "initial",
    imageUrl: "initial",
    status: "initial",
  };

  const {
    data,
    mutate: getModuleListFromApi,
    isSuccess: isModuleDataSuccess,
  } = useGetModuleList();
  const {
    data: moduleData,
    mutate: createModule,
    isSuccess: isModuleCreateSuccess,
  } = useCreateModule();
  const {
    data: moduleUpdateData,
    mutate: updateModule,
    isSuccess: isModuleUpdateSuccess,
  } = useUpdateModule();

  const [editingKey, setEditingKey] = useState("");
  const isEditing = (record: RmModule) => record?.moduleId === editingKey;
  const edit = (record: any) => {
    form2.setFieldsValue({
      moduleName: "",
      moduleType: "",
      imageUrl: "",
      status: "",
      ...record,
    });

    setEditingKey(record?.moduleId);
  };
  const cancel = () => {
    setEditingKey("");
  };
  const create = async () => {
    let modulePayload: RmModule = {
      moduleName: form.getFieldValue("moduleName"),
      moduleType: form.getFieldValue("moduleType"),
      imageUrl: form.getFieldValue("imageUrl"),
      moduleUrl: form.getFieldValue("moduleUrl"),

      status: form.getFieldValue("status"),
    };
    let parentName = form.getFieldValue("parentName");
    let parent = moduleList.filter((it: any) => it.moduleId === selectedOption);
    if (parent.length > 0) {
      modulePayload.parent = parent[0];
      modulePayload.parentName = parent[0].moduleName;
    }
    modulePayload.moduleType = selectedModuleType;
    try{
 await form.validateFields();

    createModule(modulePayload);}
    catch{
      console.log("validation error cannot create module")
    }
  };
  const toggleStatus = (status: boolean, record: RmModule) => {
    record.status = status;
    updateModule(record);
  };
  const copyToClipBoard = async (copyText: any) => {
    try {
      await navigator.clipboard.writeText(copyText);
      setCopySuccess("Copied!");
    } catch (err) {
      setCopySuccess("Failed to copy!");
    }
  };

  const save = async (key: any) => {
    try {
      const row = await form2.validateFields();
      const newData = [...moduleList];
      const index = newData.findIndex((item) => key === item.key);
      row.moduleId = key;
      row.parent = moduleList.filter(
        (it: any) => it.moduleName === row.parentName
      )[0].moduleId;

      if (index > -1) {
        const item = newData[index];

        updateModule(row);
        /*  newData.splice(index, 1, {
                   ...item,
                   ...row,
                 });
                 setModuleList(newData); */
        setEditingKey("");
      } else {
        updateModule(row);
        // newData.push(row);
        setModuleList(newData);
        setEditingKey("");
      }
    } catch (errInfo) {}
  };
  const fetchModuleList = () => {
    const requestParams: any = {
      page: currentPage || 1,

      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: pageSize,
    };
    getModuleListFromApi(requestParams);

    getModuleListTree(requestParams);
  };
  useEffect(() => {
    if (loading || isModuleUpdateSuccess || isModuleCreateSuccess) {
      fetchModuleList();
      setShowForm(false);
      form.resetFields();
      if (loading) setLoading(false);
    }
  }, [loading, isModuleUpdateSuccess, isModuleCreateSuccess]);
  useEffect(() => {
    if (data) {
      setModuleList(data);
      setLoading(false);
    }
  }, [data]);
  useEffect(() => {
    if (treeList) {
      setModuleListTree(treeList);
    }
  }, [isModuleTreeDataSuccess]);

  const columns = [
    {
      title: "MODULE NAME",
      dataIndex: "moduleName",
      width: "10%",
      editable: true,
      render: (value: any) => (value === "initial" ? <Input /> : value),
    },
    {
      title: "PARENT NAME",
      dataIndex: "parentName",
      width: "10%",
      editable: true,
      render: (value: any) => {
        return value === "initial" ? <Input /> : <span>{value}</span>;
      },
    },
    {
      title: "TYPE",
      dataIndex: "moduleType",
      width: "6%",
      editable: true,
      render: (value: any) => (value === "initial" ? <Input /> : value),
    },
    {
      title: "IMAGE URL",
      dataIndex: "imageUrl",
      width: "2%",
      editable: true,
      render: (imageUrl: any) => (
        <>
          {" "}
          <div style={{ display: "flex" }}>
            {" "}
            <img src={imageUrl} style={{ height: "30px", width: "30px" }} />
            <Button type="link" onClick={(e) => copyToClipBoard(imageUrl)}>
              Copy URL
            </Button>
          </div>
        </>
      ),
    },
    {
      title: "Module URL",
      dataIndex: "moduleUrl",
      width: "2%",
      editable: true,
      render: (imageUrl: any) => (
        <>
          <Button type="link" onClick={(e) => copyToClipBoard(imageUrl)}>
            Copy URL
          </Button>
        </>
      ),
    },
    {
      title: "CREATED BY",
      dataIndex: "createdBy",
      width: "4%",
      editable: true,
      render: (value: any) => (value === "initial" ? <Input /> : value),
    },
    {
      title: "CREATED DATE",
      dataIndex: "createdAt",
      width: "14%",
      render: (date: string) => {
        return <span> {date.substring(0, date?.indexOf("T"))}</span>;
      },
    },
    {
      title: "UPDATED BY",
      dataIndex: "updatedBy",
      width: "5%",
    },
    {
      title: "UPDATED DATE",
      dataIndex: "updatedAt",
      width: "14%",
      render: (date: string) => {
        return <span> {date.substring(0, date?.indexOf("T"))}</span>;
      },
    },
    {
      title: "STATUS",
      dataIndex: "status",
      // key:'moduleId',
      width: "2%",
      editable: false,

      render: (status: any, record: any) => {
        return (
          <Switch
            size="small"
            checked={status}
            onChange={(status) => toggleStatus(status, record)}
          />
        );
      },
    },
    {
      title: "ACTIONS",
      // dataIndex: 'action',
      width: "13%",
      render: (record: any) => {
        const editable = isEditing(record);
        return editable ? (
          <span>
            <Typography.Link
              onClick={() => save(record.moduleId)}
              style={{
                marginRight: 8,
              }}
            >
              Save
            </Typography.Link>
            <Popconfirm title="Sure to cancel?" onConfirm={cancel}>
              <a>Cancel</a>
            </Popconfirm>
          </span>
        ) : (
          <Typography.Link
            disabled={editingKey !== ""}
            onClick={() => {
              setShowForm(false);
              edit(record);
            }}
          >
            Edit
          </Typography.Link>
        );
      },
    },
  ];
  const mergedColumns = columns.map((col) => {
    if (!col.editable) {
      return col;
    }
    return {
      ...col,
      onCell: (record: any) => ({
        record,
        inputType: col.dataIndex === "parentId" ? "number" : "text",
        dataIndex: col.dataIndex,
        title: col.title,
        editing: isEditing(record),
      }),
    };
  });
  const onFinish = (values: any) => {};
  const onFinishFailed = (errorInfo: any) => {};

  return (
    <div className="main-container">
      <div className="header-section">
        {" "}
        <h5 className="h5 mt-3">
          Module Management
          {moduleList.length > 0 && !showForm && (
            <Button className="btn-create" onClick={() => setShowForm(true)}>
              <p className="title-btn-create"> Create Module</p>
            </Button>
          )}
        </h5>
        {/* <PageHeading title="Module Management" /> */}
      </div>
      {data && moduleList.length === 0 && !showForm && (
        <div>
          <EmptyModules onCreateModule={() => setShowForm(true)} />
        </div>
      )}

      <div>
        {showForm && (
          <Form
            autoComplete="off"
            name="basic"
            form={form}
            onFinish={onFinish}
            onSubmitCapture={(values) => {}}
            component={false}
            onFinishFailed={onFinishFailed}
          >
            {
              <div>
                <div className="form-div">
                  <div className="col-lg-5">
                    <Form.Item
                      name="moduleName"
                      label="Module Name"
                      /*  initialValue={props?.allDetails!= undefined ?
                                       props?.allDetails.contactDetails.entityContactDetailsFirstName : ""} */
                      rules={[
                        { required: true, message: "Please enter Module Name" },
                      ]}
                    >
                      <Input
                        className="input"
                        aria-autocomplete="both"
                        aria-haspopup="false"
                        placeholder="Enter Module name" /* autoComplete="true" */
                      />
                    </Form.Item>
                  </div>
                  <div className="col-lg-1"></div>

                  <div className="col-lg-5">
                    <Form.Item
                      name="parentName"
                      label="Parent Name"
                     /*  rules={[
                        { required: true, message: "Please select Parent " },
                        {
                          validator: (rule, value) => {
                            if (value === "Please Select Parent") {
                              return Promise.reject(
                                "Please select a valid  option for Parent!"
                              );
                            }
                            return Promise.resolve();
                          },
                        },
                      ]} */
                      /*  initialValue={props?.allDetails!= undefined ?
                                       props?.allDetails.contactDetails.entityContactDetailsFirstName : ""} */
                    >
                      <Select
                        onChange={(e) => setSelectedOption(Number.parseInt(e))}
                        defaultValue={"Please Select Parent"}
                        /* defaultValue={selectedValue} options={options} */ id={
                          "roleOptions"
                        }
                        placeholder={"Please select parent"}
                        options={[
                          {
                            id: "",
                            name: "Please select",
                            value: "Please Select Parent",
                            selected: true,
                            default: true,
                          },
                          ...moduleList.map((it: any) => {
                            let module: any = {};
                            module.label =
                              (it?.parentName ? it?.parentName + " >> " : "") +
                              it?.moduleName;
                            module.value = it?.moduleId;
                            module.children = it?.children;
                            return module;
                          }),
                        ]}
                      >
                        <option className="options" selected value={""}>
                          Select an option
                        </option>
                      </Select>
                    </Form.Item>
                  </div>
                </div>

                <div className="form-div">
                  <div className="col-lg-5">
                    <Form.Item
                      name="moduleType"
                      label="Module Type"
                      /*  initialValue={props?.allDetails!= undefined ?
                                       props?.allDetails.contactDetails.entityContactDetailsFirstName : ""} */
                      rules={[
                        {required:true},
                       {validator: (rule, value) => {
                          if (selectedModuleType === "") {
                            return Promise.reject(
                              "Please select a valid  option for type!"
                            );
                          }
                          return Promise.resolve();
                        }}
                       
                      ]}
                    >
                      <Select
                        onChange={(e) => {
                          form.setFieldValue('moduleType',e)
                          setSelectedModuleType(e)}}
                        defaultValue={selectedCopy}
                      >
                        {/*  <div className="options-container">*/}
                        <option selected value={""}>
                          Select an option
                        </option>

                        {itemType.map((item: any) => {
                          return (
                            <option key={item.key} value={item.value}>
                              {item.label}
                            </option>
                          );
                        })}
                        {/* </div>
                         */}
                      </Select>{" "}
                    </Form.Item>
                  </div>
                  <div className="col-lg-1"></div>

                  <div className="col-lg-5">
                    <Form.Item
                      name="moduleUrl"
                      label="Module URL"
                      /*  initialValue={props?.allDetails!= undefined ?
                                       props?.allDetails.contactDetails.entityContactDetailsFirstName : ""} */
                      rules={[
                        { required: true, message: "Please enter Module URL" },
                      ]}
                    >
                      <Input
                        className="input"
                        aria-autocomplete="both"
                        aria-haspopup="false"
                        placeholder="Enter Module URL" /* autoComplete="true" */
                      />
                    </Form.Item>
                  </div>
                </div>

                <div className="form-div">
                  <div className="col-lg-5">
                    <Form.Item
                      name="imageUrl"
                      label="Image URL"
                      /*  initialValue={props?.allDetails!= undefined ?
                                       props?.allDetails.contactDetails.entityContactDetailsFirstName : ""} */
                      rules={[
                        { required: true, message: "Please enter Image URL" },
                      ]}
                    >
                      <Input
                        className="input"
                        aria-autocomplete="both"
                        aria-haspopup="false"
                        placeholder="Enter Image URL" /* autoComplete="true" */
                      />
                    </Form.Item>
                  </div>
                  <div className="col-lg-1" />

                  <div className="col-lg-5">
                    <Form.Item
                      name="status"
                      label="Status"
                      /*  initialValue={props?.allDetails!= undefined ?
                                       props?.allDetails.contactDetails.entityContactDetailsFirstName : ""} */
                      rules={[
                        { required: true, message: "Please enter Image URL" },
                      ]}
                    >
                      <Select
                        suffixIcon={<img src={"/images/down-arrow.svg"} />}
                        placeholder="Select Status"
                      >
                        <Select.Option value={1} key={1}>
                          Active
                        </Select.Option>
                        <Select.Option value={0} key={0}>
                          Inactive
                        </Select.Option>
                      </Select>
                    </Form.Item>
                  </div>
                </div>
              </div>
            }
            <div>
              <Button className="btn-create" onClick={() => create()}>
                <p className="title-btn-create"> Create Module</p>
              </Button>
            </div>
          </Form>
        )}
        <div></div>
        <br />
        <Form form={form2}>
          <div>
            {data && moduleList.length > 0 && (
              <Table
                scroll={{ x: 1400 }}
                components={{
                  body: {
                    cell: EditableCell,
                  },
                }}
                bordered
                dataSource={
                  moduleList.map((column: any) => {
                    delete column.children;
                    return column;
                  }) /* ?.unshift(initial) */
                }
                columns={mergedColumns}
                rowClassName="editable-row"
                pagination={{
                  onChange: cancel,
                }}
              />
            )}
          </div>
        </Form>
      </div>
    </div>
  );
};
export default ModuleManagement;
