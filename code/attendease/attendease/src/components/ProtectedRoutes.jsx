import React from "react";
import { Navigate } from "react-router-dom";

const ProtectedRoutes = ({ element, allowedRoles, userRole }) => {
  if (!userRole) {
    return <Navigate to="/login" />;
  }

  if (allowedRoles && !allowedRoles.includes(userRole)) {
    return (
      <div className="access">
        <h1>Access Denied</h1>
        <p>You do not have permission to view this page.</p>
      </div>
    );
  }

  return element;
};

export default ProtectedRoutes;
