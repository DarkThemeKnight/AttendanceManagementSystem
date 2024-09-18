import React, { useState } from "react";
import AttendanceCapture from "../components/AttendanceCapture";
import "../styles/capture.css";
import useFetch from "../hooks/useFetch";

const AttendancePage = ({ BASE }) => {
  const { token } = useFetch();
  const [text, setText] = useState(null);
  const [heading, setHeading] = useState(null);

  // function base64ToBlob(base64Data, contentType) {
  //   const byteString = atob(base64Data.split(",")[1]);
  //   const arrayBuffer = new ArrayBuffer(byteString.length);
  //   const uintArray = new Uint8Array(arrayBuffer);
  //   for (let i = 0; i < byteString.length; i++) {
  //     uintArray[i] = byteString.charCodeAt(i);
  //   }
  //   return new Blob([arrayBuffer], { type: contentType });
  // }

  const submitAttendance = async (data) => {
    const { image, code } = data;

    // Convert base64 image to Blob
    // const file = base64ToBlob(image, "image/jpeg");

    console.log(image);

    const formData = new FormData();
    // formData.append("attendanceCode", code);
    formData.append("image", image);

    console.log([...formData.entries()]);

    try {
      const response = await fetch(
        `${BASE}/students/update?attendanceCode=${code}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        }
      );

      const responseData = await response.json();
      setHeading("Attendance Submitted!");
      setText(responseData.message);

      if (!response.ok) {
        throw new Error("Invalid request! Please try again.");
      }
    } catch (error) {
      console.error("Error uploading file:", error);
      setHeading("Failed to Mark Attendance!");
      setText(error.message);
    }
  };

  const handleClose = () => {
    setText(null);
  };

  return (
    <div className="attendance-page">
      <AttendanceCapture
        submitAttendance={submitAttendance}
        text={text}
        heading={heading}
        handleClose={handleClose}
      />
    </div>
  );
};

export default AttendancePage;
