import { useEffect, useState } from "react";
import "../styles/header.css";
import { Link, useNavigate } from "react-router-dom";
import Navigation from "./Navigation";

const Header = ({ jwtToken, BASE }) => {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(!!jwtToken);
  const [isShow, setIsShow] = useState(false);

  // const handleLogout = async () => {
  //   await fetch(`${BASE}/auth/logout`, {
  //     method: "PUT",
  //     headers: {
  //       Authorization: `Bearer ${jwtToken}`,
  //     },
  //   })
  //     .then((response) => {
  //       if (response.ok) {
  //         console.log("Logged out successfully");

  //         // navigate("/");
  //         console.log("Hit");
  //       }
  //       return response;
  //     })
  //     .then((data) => {
  //       localStorage.clear();
  //     })
  //     .catch((error) => {
  //       console.log(error);
  //     });
  //   navigate("/login");
  // };

  const showMenu = () => {
    setIsShow(true);
  };
  const handleClose = () => {
    setIsShow(false);
  };
  useEffect(() => {
    setIsLoggedIn(!!jwtToken);
  }, [jwtToken]);

  return (
    <header>
      <div className="container">
        <Link to={"/"}>
          <div className="logo">
            <div className="icon">✓</div>
            <div className="text">AttendEase</div>
          </div>
        </Link>
        {!isLoggedIn && (
          <button onClick={() => navigate("/login")}>Login</button>
        )}
        {isLoggedIn && <button onClick={showMenu}>Menu</button>}
        {isShow && (
          <Navigation
            handleClose={handleClose}
            BASE={BASE}
            jwtToken={jwtToken}
          />
        )}
      </div>
    </header>
  );
};

export default Header;
