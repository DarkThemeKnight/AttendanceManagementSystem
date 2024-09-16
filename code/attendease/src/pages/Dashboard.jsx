import { Outlet } from "react-router-dom";
import Menus from "../components/Menus";
import "../styles/dashboard.css";

const Dashboard = () => {
  return (
    <main>
      <div className="container">
        <Menus />
        <div className="contents">
          <Outlet />
        </div>
      </div>
    </main>
  );
};

export default Dashboard;
