import prisma from "@/lib/prisma"
import React from "react"

export const ItemListView: React.FC = async () => {
  const list = await prisma.itemDao.findMany()
  return (
    <div>
      <ul>
        {list.map((item) => (
          <li>
            {item.id} | {item.name} <div>{item.description}</div>
          </li>
        ))}
      </ul>
    </div>
  )
}
