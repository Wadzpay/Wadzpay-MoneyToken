import React, { useState, useEffect } from "react"
import { Radio, Skeleton, Spin } from "antd"
import { LoadingOutlined } from "@ant-design/icons"
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from "chart.js"
import { Line } from "react-chartjs-2"

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)

import { useWalletSummary, useWalletSummaryGraph } from "../../../api/user"
import WalletsSummary from "./WalletsSummary"
interface Data {
  labels: any
  datasets: any
}
interface Options {
  responsive: boolean
  plugins: any
}

const WalletTransactionsChart: React.FC = () => {
  const [walletSummaryGraph, setwalletSummaryGraph] = useState<any>()
  const [labels, setLabels] = useState<any>()
  const [graphType, setGraphType] = useState<string>("monthly")
  const [graphData, setGraphData] = useState<any>()

  // Api's call to get wallet summary
  const {
    data: walletSummaryData,
    isFetching: isFetchingWalletSummary,
    error: errorWalletSummary
  } = useWalletSummary()

  // Api's call to get wallet summary Graph
  const {
    data: walletSummaryGraphData,
    isFetching: isFetchingWalletSummaryGraph,
    error: errorwalletSummaryGraph
  } = useWalletSummaryGraph()

  useEffect(() => {
    if (walletSummaryGraphData) {
      setwalletSummaryGraph(walletSummaryGraphData)
    }
  }, [walletSummaryGraphData])

  useEffect(() => {
    if (walletSummaryGraph) {
      if (graphType === "yearly" || graphType === "weekly") {
        const labelArray = Object.keys(
          graphType === "weekly"
            ? walletSummaryGraph?.totalWalletsBalances?.[graphType].data
            : walletSummaryGraph?.totalWalletsBalances?.[graphType]
        ).sort()
        setLabels(
          labelArray.map((word) =>
            graphType === "yearly"
              ? word.slice(1)
              : graphType === "weekly"
              ? `${word} (${walletSummaryGraph?.totalWalletsBalances?.[graphType].labels[word]})`
              : word
          )
        )
        // set graph data
        const totalWalletsArray = Object.values(
          graphType === "weekly"
            ? walletSummaryGraph?.totalWalletsBalances?.[graphType].data
            : walletSummaryGraph?.totalWalletsBalances?.[graphType]
        )
        const enableWalletsArray = Object.values(
          graphType === "weekly"
            ? walletSummaryGraph?.enabledWalletsBalances?.[graphType].data
            : walletSummaryGraph?.enabledWalletsBalances?.[graphType]
        )
        const totalDipositsArray = Object.values(
          graphType === "weekly"
            ? walletSummaryGraph?.totalDepositsBalances?.[graphType].data
            : walletSummaryGraph?.totalDepositsBalances?.[graphType]
        )

        const dataArray: any = {
          totalWalletsData: totalWalletsArray,
          enableWalletsData: enableWalletsArray,
          totalDipositsData: totalDipositsArray
        }

        setGraphData(dataArray)
      } else {
        monthlyData(walletSummaryGraph, graphType)
      }
    }
  }, [walletSummaryGraph, graphType])

  const monthlyData = (data: any, graphType: string) => {
    const totalWalletsBalances: [] = data?.totalWalletsBalances?.[graphType]
    const labelArray = totalWalletsBalances.map(({ monthName }) => monthName)

    // set graph data
    const totalWalletsArray: [] = data?.totalWalletsBalances?.[graphType]
    const enableWalletsArray: [] = data?.enabledWalletsBalances?.[graphType]
    const totalDipositsArray: [] = data?.totalDepositsBalances?.[graphType]

    const dataArray: any = {
      totalWalletsData: totalWalletsArray.map(({ monthValue }) => monthValue),
      enableWalletsData: enableWalletsArray.map(({ monthValue }) => monthValue),
      totalDipositsData: totalDipositsArray.map(({ monthValue }) => monthValue)
    }

    setLabels(labelArray)
    setGraphData(dataArray)
  }

  const data: Data = {
    labels: labels,
    datasets: [
      {
        label: "Total Wallets",
        data:
          graphType === "yearly" || graphType === "weekly"
            ? graphData?.totalWalletsData.sort()
            : graphData?.totalWalletsData,
        fill: true,
        lineTension: 0.5,
        borderColor: "#26A6E0",
        pointStyle: false,
        borderWidth: 1.5
      },
      {
        label: "Enable Wallets",
        data:
          graphType === "yearly" || graphType === "weekly"
            ? graphData?.enableWalletsData.sort()
            : graphData?.enableWalletsData,
        fill: true,
        lineTension: 0.5,
        borderColor: "#00AD6E",
        pointStyle: false,
        borderWidth: 1.5
      },
      {
        label: "Total Deposits",
        data:
          graphType === "yearly" || graphType === "weekly"
            ? graphData?.totalDipositsData.sort()
            : graphData?.totalDipositsData,
        fill: true,
        lineTension: 0.5,
        borderColor: "#F38300",
        pointStyle: false,
        borderWidth: 1.5
      }
    ]
  }

  const options: Options = {
    responsive: true,
    plugins: {
      legend: {
        display: false
      }
    }
  }

  const handleChange = (e: any) => {
    setGraphType(e.target.value)
  }

  return (
    <>
      <div className="card-header custom-header py-3">
        <h5 className="m-0 float-start">Wallets Summary</h5>
      </div>
      {/* WalletsSummary */}
      <WalletsSummary walletSummary={walletSummaryData} />
      <div className="row">
        <div className="card-body py-0 walletSummaryTypes">
          <div className="col-xl-5 col-md-6 ms-2 float-start">
            <div
              className="float-start"
              style={{
                color: "#131313",
                fontSize: "10px",
                width: "28%",
                marginTop: "10px"
              }}
            >
              <span
                className="progress wdz-main-bg-color float-start"
                style={{
                  width: "20%",
                  height: "7px",
                  borderRadius: "10px",
                  marginTop: "3px"
                }}
              ></span>
              <span>&nbsp;Total Wallets</span>
            </div>
            <div
              className="float-start"
              style={{
                color: "#131313",
                fontSize: "10px",
                width: "28%",
                marginTop: "10px"
              }}
            >
              <span
                className="progress wdz-green-bg-color float-start"
                style={{
                  width: "20%",
                  height: "7px",
                  borderRadius: "10px",
                  marginTop: "3px"
                }}
              ></span>
              <span>&nbsp;Enable Wallets</span>
            </div>
            <div
              className="float-start ms-2"
              style={{
                color: "#131313",
                fontSize: "10px",
                width: "28%",
                marginTop: "10px"
              }}
            >
              <span
                className="progress wdz-yellow-bg-color float-start"
                style={{
                  width: "20%",
                  height: "7px",
                  borderRadius: "10px",
                  marginTop: "3px"
                }}
              ></span>
              <span>&nbsp;Total Deposits</span>
            </div>
          </div>
          <Radio.Group
            onChange={(e) => handleChange(e)}
            className="float-end me-3"
          >
            <Radio.Button
              style={
                graphType == "weekly"
                  ? {
                      background: "#26a6e0",
                      color: "#ffffff",
                      borderColor: "#26a6e0"
                    }
                  : {}
              }
              value="weekly"
            >
              Weekly
            </Radio.Button>
            <Radio.Button
              style={
                graphType == "monthly"
                  ? {
                      background: "#26a6e0",
                      color: "#ffffff",
                      borderColor: "#26a6e0"
                    }
                  : {}
              }
              value="monthly"
            >
              Monthly
            </Radio.Button>
            <Radio.Button
              style={
                graphType == "yearly"
                  ? {
                      background: "#26a6e0",
                      color: "#ffffff",
                      borderColor: "#26a6e0"
                    }
                  : {}
              }
              value="yearly"
            >
              Yearly
            </Radio.Button>
          </Radio.Group>
        </div>
      </div>
      <div className="card-body">
        <Line data={data} options={options} height={131} />
      </div>
    </>
  )
}

export default WalletTransactionsChart
