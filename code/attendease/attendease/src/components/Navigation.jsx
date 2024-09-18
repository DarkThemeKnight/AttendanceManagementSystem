import { Link } from "react-router-dom";
import "../styles/navigation.css";
import Reset from "./Reset";
import { useState } from "react";

const Navigation = ({ handleClose, BASE, jwtToken }) => {
  const [showForm, setShowForm] = useState(false);

  const handleChange = () => {
    setShowForm(true);
    // handleClose();
  };

  const handleLogout = async () => {
    await fetch(`${BASE}/auth/logout`, {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${jwtToken}`,
      },
    })
      .then((response) => {
        if (response.ok) {
          console.log("Logged out successfully");
        }
        return response;
      })
      .then((data) => {
        localStorage.clear();
        window.location.reload();
      })
      .catch((error) => {
        console.log(error);
      });
  };

  return (
    <div className="navigation">
      <button onClick={handleClose}>X</button>
      <ul>
        <li>
          <Link to="/login" onClick={handleLogout}>
            Logout
          </Link>
        </li>
        <li>
          <Link onClick={handleChange}>Change Password</Link>
        </li>
      </ul>
      {showForm && <Reset handleClose={handleClose} BASE={BASE} />}
    </div>
  );
};

export default Navigation;
