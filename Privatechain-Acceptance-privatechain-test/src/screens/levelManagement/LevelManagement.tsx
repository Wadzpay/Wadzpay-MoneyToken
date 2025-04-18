import "./index.css";
import { useEffect, useState } from "react";
import { useCreateLevel, useGetLevelList, useUpdateLevel } from "src/api/user";

import {
  Button,
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
import { Level } from "src/api/models";
import PageHeading from "src/components/ui/PageHeading";
import EmptyLevels from "./EmptyLevels";
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
const LevelManagement = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [currentPage, setCurrentPage] = useState<number>();
  const [pageSize, setPageSize] = useState(10);
  const [levelList, setLevelList] = useState<any>([]);
  const [form] = Form.useForm();
  const [form2] = Form.useForm();
  const [copySuccess, setCopySuccess] = useState("");
  const [showForm, setShowForm] = useState(false);
  const initial = {
    levelName: "initial",
    levelNumber: "initial",
    imageUrl: "initial",
    status: "initial",
  };

  const {
    data,
    mutate: getLevelListFromApi,
    isSuccess: isLevelDataSuccess,
  } = useGetLevelList();
  const {
    data: levelData,
    mutate: createLevel,
    isSuccess: isLevelCreateSuccess,
  } = useCreateLevel();
  const {
    data: levelUpdateData,
    mutate: updateLevel,
    isSuccess: isLevelUpdateSuccess,
  } = useUpdateLevel();

  const [editingKey, setEditingKey] = useState("");
  const isEditing = (record: Level) => record?.levelId === editingKey;
  const edit = (record: any) => {
    form2.setFieldsValue({
      levelName: "",
      levelNumber: "",
      status: "",
      imageUrl: "",
      ...record,
    });

    setEditingKey(record?.levelId);
  };
  const cancel = () => {
    setEditingKey("");
  };
  const create = () => {
    let levelPayload: Level = {
      levelName: form.getFieldValue("levelName"),
      levelNumber: form.getFieldValue("levelNumber"),
      imageUrl: form.getFieldValue("imageUrl"),
      status: form.getFieldValue("status"),
    };
    createLevel(levelPayload);
  };
  const toggleStatus = (status: boolean, record: Level) => {
    record.status = status;
    updateLevel(record);
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
      const newData = [...levelList];
      const index = newData.findIndex((item) => key === item.key);
      row.levelId = key;
      if (index > -1) {
        const item = newData[index];
        updateLevel(row);
        /*  newData.splice(index, 1, {
          ...item,
          ...row,
        });
        setLevelList(newData); */
        setEditingKey("");
      } else {
        updateLevel(row);
        // newData.push(row);
        setLevelList(newData);
        setEditingKey("");
      }
    } catch (errInfo) {
      console.log("Validate Failed:", errInfo);
    }
  };
  const fetchLevelList = () => {
    const requestParams: any = {
      page: currentPage || 1,

      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: pageSize,
    };
    getLevelListFromApi(requestParams);
  };
  useEffect(() => {
    if (loading || isLevelUpdateSuccess || isLevelCreateSuccess) {
      fetchLevelList();
      setShowForm(false);
      form.resetFields();
      if (loading) setLoading(false);
    }
  }, [loading, isLevelUpdateSuccess, isLevelCreateSuccess]);
  useEffect(() => {
    if (data) {
      setLevelList(data);
      setLoading(false);
    }
  }, [data]);
  const columns = [
    {
      title: "LEVEL NAME",
      dataIndex: "levelName",
      width: "13%",
      editable: true,
      render: (value: any) => (value === "initial" ? <Input /> : value),
    },
    {
      title: "LEVEL No.",
      dataIndex: "levelNumber",
      width: "8%",
      editable: true,
      render: (value: any) => (value === "initial" ? <Input /> : value),
    },
    {
      title: "IMAGE URL",
      dataIndex: "imageUrl",
      width: "15%",
      editable: true,
      render: (imageUrl: any) => (
        <>
          {" "}
          <img src={imageUrl} style={{ height: "40px", width: "40px" }} />
          <Button type="link" onClick={(e) => copyToClipBoard(imageUrl)}>
            Copy URL
          </Button>
        </>
      ),
    },
    {
      title: "CREATED BY",
      dataIndex: "createdBy",
      width: "9%",
    },
    {
      title: "CREATED DATE",
      dataIndex: "createdAt",
      width: "10%",
      render: (date: string) => {
        return <span> {date.substring(0, date?.indexOf("T"))}</span>;
      }
    },
    {
      title: "UPDATED BY",
      dataIndex: "updatedBy",
      width: "9%",
    },
    {
      title: "UPDATED DATE",
      dataIndex: "updatedAt",
      width: "10%",
      render: (date: string) => {
        return <span> {date.substring(0, date?.indexOf("T"))}</span>;
      }
    },
    {
      title: "STATUS",
      dataIndex: "status",
      key: "levelId",
      width: "8%",
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
      width: "10%",

      /*       dataIndex: 'levelId',
       */ render: (record: any) => {
        const editable = isEditing(record);
        return editable ? (
          <span>
            <Typography.Link
              onClick={() => save(record.levelId)}
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
        inputType: col.dataIndex === "levelNumber" ? "number" : "text",
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
        <h5 className="h5 mt-3 ">
          Level Management
          {levelList.length > 0 && !showForm && (
            <Button className="btn-create" onClick={() => setShowForm(true)}>
              <p className="title-btn-create">Create Level</p>
            </Button>
          )}
        </h5>
        {/* <PageHeading title="Level Management" /> */}
      </div>
      {data&&levelList.length === 0 &&!showForm&& (
        <EmptyLevels onCreateLevel={() => setShowForm(true)} />
      )}

      <div className="form-container">
        {showForm && (
          <Form
            autoComplete="off"
            name="basic"
            form={form}
            onFinish={onFinish}
            onSubmitCapture={(values) => {
              console.log(values);
            }}
            component={false}
            onFinishFailed={onFinishFailed}
          >
            {
              <div>
                <div className="form-div">
                  <div className="col-lg-5">
                    <Form.Item
                      name="levelName"
                      label="Level Name"
                      /*  initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.contactDetails.entityContactDetailsFirstName : ""} */
                      rules={[
                        { required: true, message: "Please enter Level Name" },
                      ]}
                    >
                      <Input
                        className="input"
                        aria-autocomplete="both"
                        aria-haspopup="false"
                        placeholder="Enter Level name" /* autoComplete="true" */
                      />
                    </Form.Item>
                  </div>
                  <div className="col-lg-1"></div>

                  <div className="col-lg-5">
                    <Form.Item
                      name="levelNumber"
                      label="Level Number"
                      /*  initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.contactDetails.entityContactDetailsFirstName : ""} */
                      rules={[
                        {
                          required: true,
                          message: "Please enter Level Number",
                        },
                      ]}
                    >
                      <Input
                        className="input"
                        aria-autocomplete="both"
                        aria-haspopup="false"
                        max={99}
                        maxLength={2}
                        placeholder="Enter Level Number" /* autoComplete="true" */
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
            <div className="form-div">
              <Button className="btn-create" onClick={() => create()}>
                <p className="title-btn-create">Create Level</p>
              </Button>
            </div>
          </Form>
        )}
        <div></div>
        <br />
        <Form form={form2}>
          <div>
            {data&&levelList.length>0 && <Table
              components={{
                body: {
                  cell: EditableCell,
                },
              }}
              bordered
              dataSource={levelList /* ?.unshift(initial) */}
              columns={mergedColumns}
              rowClassName="editable-row"
              pagination={{
                onChange: cancel,
              }}
            />}
          </div>
        </Form>
      </div>
    </div>
  );
};
export default LevelManagement;
