import { Button } from "antd";

type Props = {
  title?: any;
  buttonTitle?: string;
  onCreateUser?: () => void;
  imageUrl?: string;
};

const EmptyRecordScreen: React.FC<Props> = ({
  title,
  buttonTitle,
  onCreateUser,
  imageUrl,
}: Props) => {
  return (
    <div className="empty-record-container">
      {imageUrl && (
        <div className="empty-record-image">
          <img src="/images/user-management.svg" alt="User Management" />
        </div>
      )}
      {title && <p className="empty-record-title">{title}</p>}
      {buttonTitle && (
        <div className="empty-record-button">
          <Button
            className="role-empty-btn empty-btn"
            style={{ color: "#131313", border: "#ffc235" }}
            onClick={onCreateUser}
          >
            {buttonTitle}
          </Button>
        </div>
      )}
    </div>
  );
};

export default EmptyRecordScreen;
