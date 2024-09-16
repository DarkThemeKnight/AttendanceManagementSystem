const postReq = (
  userId,
  password,
  category,
  navigate,
  setError,
  setJwtToken
) => {
  const BASE = "http://192.168.43.218:8080/api/v1";

  function setToken(token, date, userRoles) {
    console.log("Setting token:", token);
    console.log("Setting expiry date:", date);
    console.log("Setting user roles:", userRoles);
  
    localStorage.setItem("jwtToken", token);
    localStorage.setItem("expiryDate", date);
    localStorage.setItem("userRoles", JSON.stringify(userRoles || []));
    localStorage.setItem("tokenIssueTime", Date.now().toString());
    
    // Additional logging
    console.log("Token after setting:", localStorage.getItem("jwtToken"));
    console.log("Expiry Date after setting:", localStorage.getItem("expiryDate"));
    console.log("User Roles after setting:", localStorage.getItem("userRoles"));
    console.log("Token Issue Time after setting:", localStorage.getItem("tokenIssueTime"));
    
    setJwtToken(token); // Update the state with the new token
  }
  
  console.log("Sending login request with:", { id: userId, password, role: category });
  fetch(`${BASE}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ id: userId, password, role: category }),
  })
    .then((response) => {
      console.log("Checking localStorage before Sending:", localStorage);
      console.log("Received response:", response);
      if (!response.ok) {
        throw new Error("Login failed");
      }
      return response.json();
    })
    .then((data) => {
      console.log("Parsed response data:", data);

      if (
        data.user_roles.includes("ROLE_SUPER_ADMIN") &&
        category === "admin"
      ) {
        console.log("User is SUPER_ADMIN, saving token and navigating...");
        setToken(data.jwt_token, data.expiryDate, data.user_roles);
        console.log("Checking localStorage before navigating:", localStorage.getItem("jwtToken"));
        navigate("/");
      } else if (
        data.user_roles.includes("ROLE_ADMIN") &&
        category === "admin"
      ) {
        console.log("User is ADMIN, saving token and navigating...");
        setToken(data.jwt_token, data.expiryDate, data.user_roles);
        navigate("/");
      } else if (
        data.user_roles.includes("ROLE_LECTURER") &&
        category === "lecturer"
      ) {
        console.log("User is LECTURER, saving token and navigating...");
        setToken(data.jwt_token, data.expiryDate, data.user_roles);
        navigate("/");
      } else if (
        data.user_roles.includes("ROLE_STUDENT") &&
        category === "student"
      ) {
        console.log("User is STUDENT, saving token and navigating...");
        setToken(data.jwt_token, data.expiryDate, data.user_roles);
        navigate("/student");
      } else {
        console.log("User role does not match, clearing localStorage...");
        localStorage.clear();
        throw new Error("Login failed");
      }
    })
    .catch((error) => {
      console.error("Login failed:", error.message);
      setError("Login failed: " + error.message);
    });
};

export default postReq;
