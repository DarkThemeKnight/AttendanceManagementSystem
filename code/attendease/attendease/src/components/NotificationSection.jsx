import { useCallback, useEffect, useState } from "react";
import useFetch from "../hooks/useFetch";
import Notifications from "./Notifications";
import AddNotification from "./AddNotification";
import "../styles/notification.css";

const NotificationSection = ({ BASE }) => {
  const { token, userRoles } = useFetch();
  const [showAddBtn, setShowAddBtn] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [isOpen, setIsOpen] = useState(false);

  const showForm = () => {
    setIsOpen(true);
  };

  const closeForm = () => {
    setIsOpen(false);
  };

  const handleGetNotification = useCallback(async () => {
    await fetch(`${BASE}/general/notification`, {
      mode: "cors",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((response) => response.json())
      .then((data) => {
        setNotifications(data.data);
      })
      .catch((error) => {
        console.error("Login failed:", error);
      });
  }, [token, BASE]);

  useEffect(() => {
    handleGetNotification();
  }, [handleGetNotification]);

  useEffect(() => {
    if (
      (userRoles && userRoles.includes("ROLE_SUPER_ADMIN")) ||
      userRoles.includes("ROLE_ADMIN")
    ) {
      setShowAddBtn(true);
    } else {
      console.log("User not an Admin");
    }
  }, [userRoles]);

  return (
    <div className="notification-section">
      <div className="top">
        <h2>Notifications</h2>
        {showAddBtn && <button onClick={showForm}>+</button>}
      </div>
      <div>
        <Notifications notifications={notifications} />
      </div>
      {isOpen && (
        <AddNotification
          isOpen={isOpen}
          handleClose={closeForm}
          handleNotification={handleGetNotification}
          BASE={BASE}
        />
      )}
    </div>
  );
};

export default NotificationSection;
