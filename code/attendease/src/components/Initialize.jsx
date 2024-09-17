import { useEffect, useState } from "react";
import InitializeAttendance from "./InitializeAttendance";

const Initialize = ({ BASE }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [initialized, setInitialized] = useState(false);

  const code = localStorage.getItem("code");
  const handleClick = () => {
    setIsOpen(true);
  };

  const handleClose = () => {
    setIsOpen(false);
  };

  useEffect(() => {
    if (code) {
      setInitialized(true);
    }
  }, [code]);

  return (
    <div className="initialize">
      {initialized && (
        <div className="codes">
          <h2>Attendance Code:</h2>
          <h2 className="code">{code}</h2>
        </div>
      )}
      {!initialized && (
        <>
          <p>No attendance has been initialized Yet</p>
        </>
      )}
      <button className="initialize-btn" onClick={handleClick}>
        Initialize New Attendance
      </button>
      {isOpen && <InitializeAttendance handleClose={handleClose} BASE={BASE} />}
    </div>
  );
};

export default Initialize;
