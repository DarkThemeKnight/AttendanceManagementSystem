import Banner from "./Banner";
import MyCalendar from "./Calendar";
import Initialize from "./Initialize";
import NotificationSection from "./NotificationSection";

const MainContent = ({ BASE }) => {
  const isLecturer = localStorage
    .getItem("userRoles")
    .includes("ROLE_LECTURER");
  return (
    <div className="row">
      <div className="right layout">
        <Banner BASE={BASE} />
        <NotificationSection BASE={BASE} />
      </div>
      <div className="left layout">
        <MyCalendar />
        {isLecturer && <Initialize />}
      </div>
    </div>
  );
};

export default MainContent;
