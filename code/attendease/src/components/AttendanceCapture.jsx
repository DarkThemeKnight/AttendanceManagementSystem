import React, { useState, useRef } from "react";
import Webcam from "react-webcam";
import account from "../asset/account.svg";

const AttendanceCapture = ({ submitAttendance }) => {
  const [attendanceCode, setAttendanceCode] = useState("");
  const [capturedImage, setCapturedImage] = useState(null);
  const [isCameraOn, setIsCameraOn] = useState(false);
  const webcamRef = useRef(null);

  const captureImage = () => {
    const imageSrc = webcamRef.current.getScreenshot();
    setCapturedImage(imageSrc);
    stopCamera();
  };

  const startCamera = () => {
    navigator.mediaDevices
      .getUserMedia({ video: true })
      .then(() => {
        setIsCameraOn(true);
      })
      .catch((error) => {
        alert("Camera access denied. Please enable camera permissions.");
        console.error("Error accessing camera: ", error);
      });
  };

  const stopCamera = () => {
    setIsCameraOn(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!capturedImage || !attendanceCode) {
      alert("Please capture your face and enter the attendance code.");
      return;
    }

    const attendanceData = {
      image: capturedImage,
      code: attendanceCode,
    };
    await submitAttendance(attendanceData);
  };

  const capturedImageStyle = capturedImage
    ? {
        backgroundImage: `url(${capturedImage})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
      }
    : {};

  return (
    <div className="student layout">
      <div className="form">
        <form onSubmit={handleSubmit}>
          <div
            className="image"
            style={{
              width: "300px",
              height: "300px",
              ...capturedImageStyle,
            }}
          >
            {!capturedImage && (
              <>
                {!isCameraOn && (
                  <img src={account} alt="Preview" width={300} height={300} />
                )}
                {isCameraOn && (
                  <Webcam
                    audio={false}
                    ref={webcamRef}
                    screenshotFormat="image/jpeg"
                    width={300}
                    height={300}
                    videoConstraints={{
                      facingMode: "user", // For mobile, select front camera
                    }}
                    onUserMediaError={(error) =>
                      console.error("Webcam error:", error)
                    }
                  />
                )}
              </>
            )}
          </div>

          <fieldset>
            {!isCameraOn && !capturedImage && (
              <button type="button" onClick={startCamera}>
                Start Camera
              </button>
            )}

            {isCameraOn && !capturedImage && (
              <button type="button" onClick={captureImage}>
                Capture Face
              </button>
            )}

            {capturedImage && (
              <button type="button" onClick={() => setCapturedImage(null)}>
                Retake
              </button>
            )}

            <label className="attend-code">
              Attendance Code:
              <input
                type="text"
                value={attendanceCode}
                onChange={(e) => setAttendanceCode(e.target.value)}
                required
              />
            </label>

            <button type="submit">Submit Attendance</button>
          </fieldset>
        </form>
      </div>
    </div>
  );
};

export default AttendanceCapture;
