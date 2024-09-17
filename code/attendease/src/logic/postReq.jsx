const fetch = require('node-fetch');
const https = require('https');

const postReq = (
  userId,
  password,
  category,
  navigate,
  setError,
  setJwtToken,
  BASE
) => {
  function setToken(token, date, userRoles) {
    localStorage.setItem("jwtToken", token);
    localStorage.setItem("expiryDate", date);
    localStorage.setItem("userRoles", userRoles || []);
    localStorage.setItem("tokenIssueTime", Date.now());
    setJwtToken(token); // Update the state with the new token
  }

  // Create an HTTPS agent with rejectUnauthorized set to false
  const agent = new https.Agent({
    rejectUnauthorized: false,
  });

  fetch(`${BASE}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ id: userId, password, role: category }),
    agent: agent, // Use the agent with fetch
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Login failed");
      }
      return response.json();
    })
    .then((data) => {
      if (
        data.user_roles.includes("ROLE_SUPER_ADMIN") &&
        category === "admin"
      ) {
        setToken(data.jwt_token, data.expiryDate, data.user_roles);
        navigate("/");
      } else if (
        data.user_roles.includes("ROLE_ADMIN") &&
        category === "admin"
      ) {
        setToken(data.jwt_token, data.expiryDate, data.user_roles);
        navigate("/");
      } else if (
        data.user_roles.includes("ROLE_LECTURER") &&
        category === "lecturer"
      ) {
        setToken(data.jwt_token, data.expiryDate, data.user_roles);
        navigate("/");
      } else if (
        data.user_roles.includes("ROLE_STUDENT") &&
        category === "student"
      ) {
        setToken(data.jwt_token, data.expiryDate, data.user_roles);
        navigate("/student");
      } else {
        localStorage.clear();
        throw new Error("Login failed");
      }
    })
    .catch((error) => {
      // Show an error message to the user
      setError("Login failed: " + error.message);
      console.error("Login failed:", error);
    });
};

module.exports = postReq;

