import React from "react";
import { User } from "./User";

const AdminPage: React.FC = () => {
  return (
    <div>
      Here is admin page.
      <React.Suspense fallback="...">
        <User userId={101} />
      </React.Suspense>
    </div>
  );
};
export default AdminPage;
