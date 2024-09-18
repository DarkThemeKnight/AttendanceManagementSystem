const Notifications = ({ notifications }) => {
  return (
    <ul className="notification">
      {notifications.map((notification, index) => (
        <li key={` ${notification[0]}${index}${notification[1]}`}>
          <h3>{notification.title}</h3>
          <p>{notification.content}</p>
        </li>
      ))}
    </ul>
  );
};

export default Notifications;
