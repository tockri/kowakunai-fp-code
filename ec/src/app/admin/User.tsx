"use server";

import React from "react";

export const User: React.FC<{ userId: number }> = ({ userId }) => {
  return <div>User {userId}</div>;
};
