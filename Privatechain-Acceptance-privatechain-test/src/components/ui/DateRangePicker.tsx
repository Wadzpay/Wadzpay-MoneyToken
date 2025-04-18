import { DatePicker, TimeRangePickerProps } from "antd";
import dayjs, { Dayjs } from "dayjs";
import React, {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useState,
} from "react";
interface DateRangePickerProps {
  updateDateRange: (start: string, end: string) => void;
}

const DatePickerCustom = forwardRef(function DatePickerCustom(
  { updateDateRange }: DateRangePickerProps,
  ref
) {
  const { RangePicker } = DatePicker;
  const [dates, setDates] = useState<any>(null);

  const rangePresets: TimeRangePickerProps["presets"] = [
    { label: "Last 7 Days", value: [dayjs().add(-6, "d"), dayjs()] },
    { label: "Last 14 Days", value: [dayjs().add(-13, "d"), dayjs()] },
    { label: "Last 30 Days", value: [dayjs().add(-29, "d"), dayjs()] },
    { label: "Last 90 Days", value: [dayjs().add(-89, "d"), dayjs()] },
  ];
  const onRangeChange = (
    dates: null | (Dayjs | null)[],
    dateStrings: string[]
  ) => {
    if (dates) {
      updateDateRange(
        dates[0]?.toISOString() ?? "",
        dates[1]?.toISOString() ?? ""
      );
      setDates(dates);
    } else {
      updateDateRange("", "");
      setDates(null);
      console.log("Clear");
    }
  };
  useImperativeHandle(ref, () => ({
    clear() {
      clear();
    },
  }));
  function clear() {
    setDates(null);
  }

  return (
    <div>
      {" "}
      <RangePicker
        presets={[
          /* {
        label: <span aria-label="Current Time to End of Day">Now ~ EOD</span>,
        value:()=>dates,
       // value: () => [dayjs(), dayjs().endOf('day')], // 5.8.0+ support function
      }, */
          ...rangePresets,
        ]}
        // showTime
        value={dates}
        format="YYYY-MMM-DD"
        onChange={onRangeChange}
        disabledDate={(current: any) => current && current >= Date.now()}
      />
    </div>
  );
});
const DateRangePicker = React.memo(DatePickerCustom);

export default DateRangePicker;
