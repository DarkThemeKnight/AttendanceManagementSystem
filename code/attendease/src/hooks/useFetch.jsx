import { useEffect, useState } from "react";

const useFetch = () => {
  const [hasExpired, setHasExpired] = useState(false);
  const token = localStorage.getItem("jwtToken");
  const userRoles = localStorage.getItem("userRoles") || [];

  useEffect(() => {
    const expiryDate = new Date(localStorage.getItem("expiryDate"));
    const now = new Date();

    setHasExpired(now > expiryDate);
  }, [token]);

  return { hasExpired, token, userRoles };
};

export default useFetch;
