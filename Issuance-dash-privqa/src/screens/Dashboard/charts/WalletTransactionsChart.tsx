import React, { useEffect, useState } from "react"
import { Radio } from "antd"
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

import { useWalletTransactionGraph } from "../../../api/user"

const WalletTransactionsChart: React.FC = () => {
  const [walletTransactionsGraph, setWalletTransactionsGraph] = useState<any>()
  const [labels, setLabels] = useState<any>()
  const [graphType, setGraphType] = useState<string>("monthlyTransaction")
  const [graphData, setGraphData] = useState<any>()

  const {
    data: walletTransactionsGraphData,
    isFetching: isFetchingWalletTransactionsGraph,
    error: errorWalletTransactionsGraph
  } = useWalletTransactionGraph()

  useEffect(() => {
    setWalletTransactionsGraph(walletTransactionsGraphData)
  }, [isFetchingWalletTransactionsGraph])

  useEffect(() => {
    if (walletTransactionsGraph) {
      if (
        graphType === "yearlyTransaction" ||
        graphType === "weeklyTransaction"
      ) {
        const labelArray = Object.keys(
          graphType === "weeklyTransaction"
            ? walletTransactionsGraph?.[graphType].data
            : walletTransactionsGraph?.[graphType]
        ).sort()

        setLabels(
          labelArray.map((word) =>
            graphType === "yearlyTransaction"
              ? word.slice(1)
              : graphType === "weeklyTransaction"
              ? `${word} (${walletTransactionsGraph?.[graphType].labels[word]})`
              : word
          )
        )

        // set graph data
        const dataArray = Object.values(
          graphType === "weeklyTransaction"
            ? walletTransactionsGraph?.[graphType].data
            : walletTransactionsGraph?.[graphType]
        ).sort()
        setGraphData(dataArray)
      } else {
        monthlyData(walletTransactionsGraph?.[graphType])
      }
    }
  }, [walletTransactionsGraph, graphType])

  const monthlyData = (data: []) => {
    const labelArray = data.map(({ monthName }) => monthName)
    const dataArray = data.map(({ monthValue }) => monthValue)

    setLabels(labelArray)
    setGraphData(dataArray)
  }

  const data = {
    labels: labels,
    datasets: [
      {
        label: "Wallet Transactions",
        data: graphData,
        fill: true,
        lineTension: 0.5,
        borderColor: "#222983",
        borderWidth: 2
      }
    ]
  }

  interface Options {
    responsive: boolean
    plugins: any
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
      <div className="card-header custom-header py-3 walletTansations">
        <h5 className="m-0 font-weight-bold float-start">
          Wallet Transactions
        </h5>
        <Radio.Group onChange={(e) => handleChange(e)} className="float-end">
          <Radio.Button
            style={
              graphType == "weeklyTransaction"
                ? {
                    background: "#26a6e0",
                    color: "#ffffff",
                    borderColor: "#26a6e0"
                  }
                : {}
            }
            value="weeklyTransaction"
          >
            Weekly
          </Radio.Button>
          <Radio.Button
            style={
              graphType == "monthlyTransaction"
                ? {
                    background: "#26a6e0",
                    color: "#ffffff",
                    borderColor: "#26a6e0"
                  }
                : {}
            }
            value="monthlyTransaction"
          >
            Monthly
          </Radio.Button>
          <Radio.Button
            style={
              graphType == "yearlyTransaction"
                ? {
                    background: "#26a6e0",
                    color: "#ffffff",
                    borderColor: "#26a6e0"
                  }
                : {}
            }
            value="yearlyTransaction"
          >
            Yearly
          </Radio.Button>
        </Radio.Group>
      </div>
      <div className="card-body">
        <Line data={data} options={options} />
      </div>
    </>
  )
}

export default WalletTransactionsChart
