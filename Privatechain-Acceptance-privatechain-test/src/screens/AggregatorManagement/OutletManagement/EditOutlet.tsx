import React, { useState } from 'react';
//import './index.css';
import { Divider, Steps } from 'antd';

const EditOutlet: React.FC = () => {
  const [current, setCurrent] = useState(0);

  const onChange = (value: number) => {
    console.log('onChange:', value);
    setCurrent(value);
  };
  const description = 'This is a description.';
  const steps = [
    {
      key: 1,
      title: "Primary Business Details",
      content:
        "These details will reflect in the wadzpay system by individual institution",
    },
    {
      key: 2,
      title: "Merchant Address",
      content: "This address will be consider as merchant address",
    },
    {
      key: 3,
      title: "Contact Person Details",
      content: "This address will be consider as merchant address",
    },
    {
      key: 4,
      title: "Admin Details",
      content:
        "This admin details are for future responsible for any outlet related things",
    },
    {
      key: 5,
      title: "Pos",
      content:
        "Pos Details",
    },
    {
      key: 6,
      title: "Others",
      content:
        "This admin details are for future responsible for any outlet related things",
    }
  ];
  const items = steps.map((item: any, key: number) => ({
    key: item.title,
    title: item.title,
    description: key < current ? "Completed" : "",
  }));
  return (
    <>
      <Steps
        current={current}
        direction="vertical"

        onChange={onChange}
        items={items}
      />

      <Divider />

    
    </>
  );
};

export default EditOutlet;