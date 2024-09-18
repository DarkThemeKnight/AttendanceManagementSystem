import React, { useState, useEffect } from "react";
import { NavLink } from "react-router-dom";
import "../styles/menus.css";

const Menus = () => {
  const [role, setRole] = useState(localStorage.getItem("userRoles") || []);

  useEffect(() => {
    const handleStorageChange = () => {
      setRole(localStorage.getItem("userRoles") || []);
    };

    window.addEventListener("storage", handleStorageChange);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
    };
  }, []);

  return (
    <section className="menus">
      <ul>
        {(role.includes("ROLE_SUPER_ADMIN") ||
          role.includes("ROLE_ADMIN") ||
          role.includes("ROLE_LECTURER")) && (
          <li>
            <NavLink
              to="/"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              Home
            </NavLink>
          </li>
        )}
        {(role.includes("ROLE_SUPER_ADMIN") || role.includes("ROLE_ADMIN")) && (
          <>
            <li>
              <NavLink
                to="/users"
                className={({ isActive }) => (isActive ? "active" : "")}
              >
                Users
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/courses"
                className={({ isActive }) => (isActive ? "active" : "")}
              >
                Courses
              </NavLink>
            </li>
          </>
        )}
        {role.includes("ROLE_LECTURER") && (
          <>
            <li>
              <NavLink
                to="/attendance"
                className={({ isActive }) => (isActive ? "active" : "")}
              >
                Attendance sheet
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/students"
                className={({ isActive }) => (isActive ? "active" : "")}
              >
                Students
              </NavLink>
            </li>
          </>
        )}
        {role.includes("ROLE_STUDENT") && (
          <>
            <li>
              <NavLink
                to="/student"
                className={({ isActive }) => (isActive ? "active" : "")}
              >
                Register
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/capture"
                className={({ isActive }) => (isActive ? "active" : "")}
              >
                Capture
              </NavLink>
            </li>
          </>
        )}
      </ul>
    </section>
  );
};

export default Menus;
