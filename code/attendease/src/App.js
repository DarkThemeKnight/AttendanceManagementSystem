import "./App.css";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import Header from "./components/Header";
import Login from "./components/Login";
import Home from "./pages/Home";
import { useState, useEffect } from "react";
import MainContent from "./components/MainContent";
import Users from "./pages/Users";
import UserForm from "./components/UserForm";
import Students from "./pages/Students";
import AddStudent from "./pages/AddStudent";
import Attendance from "./pages/Attendance";
import Courses from "./pages/Courses";
import AddCourse from "./components/AddCourse";
import Student from "./pages/Student";
import ProtectedRoutes from "./components/ProtectedRoutes";
import AttendancePage from "./pages/AttendancePage";

const BASE = "https://192.168.1.126:8080/api/v1";

function App() {
  const [jwtToken, setJwtToken] = useState(localStorage.getItem("jwtToken"));
  const [isTokenExpired, setIsTokenExpired] = useState(false);
  const [userRole, setUserRole] = useState(localStorage.getItem("userRoles"));

  const token = localStorage.getItem("jwtToken");

  useEffect(() => {
    const expiryDate = new Date(localStorage.getItem("expiryDate"));

    if (token && expiryDate) {
      const now = Date.now();
      if (now > expiryDate) {
        // Token has expired
        localStorage.clear();
        setJwtToken(null);
        setIsTokenExpired(true);
      } else {
        setJwtToken(token);
        setIsTokenExpired(false);
        setUserRole(localStorage.getItem("userRoles"));
      }
    } else {
      localStorage.clear();
      setJwtToken(null);
      setIsTokenExpired(true);
    }
  }, [token]);

  return (
    <div className="App">
      <Router>
        <Header jwtToken={jwtToken} BASE={BASE} />
        <Routes>
          <Route
            path="/login"
            element={
              isTokenExpired ? (
                <Login setJwtToken={setJwtToken} BASE={BASE} />
              ) : userRole === "ROLE_STUDENT" ? (
                <Navigate to="/student" /> // Ensure student is redirected here
              ) : (
                <Navigate to="/" />
              )
            }
          />
          <Route
            path="/"
            element={
              <ProtectedRoutes
                element={<Home />}
                allowedRoles={[
                  "ROLE_SUPER_ADMIN",
                  "ROLE_LECTURER",
                  "ROLE_ADMIN",
                  "ROLE_STUDENT",
                ]}
                userRole={userRole}
              />
            }
          >
            <Route
              index
              element={
                <ProtectedRoutes
                  element={<MainContent BASE={BASE} />}
                  allowedRoles={[
                    "ROLE_SUPER_ADMIN",
                    "ROLE_LECTURER",
                    "ROLE_ADMIN",
                  ]}
                  userRole={userRole}
                />
              }
            />
            <Route
              path="users"
              element={
                <ProtectedRoutes
                  element={<Users BASE={BASE} />}
                  allowedRoles={["ROLE_SUPER_ADMIN", "ROLE_ADMIN"]}
                  userRole={userRole}
                />
              }
            />
            <Route
              path="users/add"
              element={
                <ProtectedRoutes
                  element={<UserForm BASE={BASE} />}
                  allowedRoles={["ROLE_SUPER_ADMIN", "ROLE_ADMIN"]}
                  userRole={userRole}
                />
              }
            />
            <Route
              path="courses"
              element={
                <ProtectedRoutes
                  element={<Courses BASE={BASE} />}
                  allowedRoles={["ROLE_SUPER_ADMIN", "ROLE_ADMIN"]}
                  userRole={userRole}
                />
              }
            />
            <Route
              path="courses/add"
              element={
                <ProtectedRoutes
                  element={<AddCourse BASE={BASE} />}
                  allowedRoles={["ROLE_SUPER_ADMIN", "ROLE_ADMIN"]}
                  userRole={userRole}
                />
              }
            />
            <Route
              path="students"
              element={
                <ProtectedRoutes
                  element={<Students BASE={BASE} />}
                  allowedRoles={["ROLE_LECTURER"]}
                  userRole={userRole}
                />
              }
            />
            <Route
              path="attendance"
              element={
                <ProtectedRoutes
                  element={<Attendance BASE={BASE} />}
                  allowedRoles={["ROLE_LECTURER"]}
                  userRole={userRole}
                />
              }
            />
            <Route
              path="students/add"
              element={
                <ProtectedRoutes
                  element={<AddStudent BASE={BASE} />}
                  allowedRoles={["ROLE_LECTURER"]}
                  userRole={userRole}
                />
              }
            />
            <Route
              path="/student"
              element={
                <ProtectedRoutes
                  element={<Student BASE={BASE} />}
                  allowedRoles={["ROLE_STUDENT"]}
                  userRole={userRole}
                />
              }
            />
            <Route path="/capture" element={<AttendancePage BASE={BASE} />} />
          </Route>
        </Routes>
      </Router>
    </div>
  );
}

export default App;
