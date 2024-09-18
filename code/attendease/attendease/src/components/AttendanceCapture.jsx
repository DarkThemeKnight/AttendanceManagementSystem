import React, { useState } from "react";
// import Webcam from "react-webcam";
import account from "../asset/account.svg";
import Modal from "./Modal";

const AttendanceCapture = ({
  submitAttendance,
  heading,
  text,
  handleClose,
}) => {
  const [attendanceCode, setAttendanceCode] = useState("");

  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    setFile(selectedFile);
    setPreview(URL.createObjectURL(selectedFile));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const attendanceData = {
      image: file,
      code: attendanceCode,
    };
    console.log(file["name"]);

    await submitAttendance(attendanceData);
  };

  return (
    <div className="student layout">
      <div className="form">
        <form onSubmit={handleSubmit}>
          <div className="image">
            {!preview && <img src={account} alt="Preview" />}
            {preview && <img src={preview} alt="Preview" />}
          </div>
          <fieldset>
            <label>
              Capture Face
              <input
                type="file"
                name="file"
                accept="image/*"
                capture="user"
                onChange={handleFileChange}
              />
            </label>
            <label className="attend-code">
              Attendance Code:
              <input
                type="text"
                value={attendanceCode}
                onChange={(e) => setAttendanceCode(e.target.value)}
                required
              />
            </label>
            <button type="submit">Sumit attendance</button>
          </fieldset>
        </form>
      </div>
      {text && (
        <Modal text={text} handleClose={handleClose} heading={heading} />
      )}
    </div>
  );
};

export default AttendanceCapture;
