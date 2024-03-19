import prisma from "../../../lib/prisma"
import { Item } from "./types"

const pageSize = 20

export const GET = async (req: Request) => {
  const query = new URL(req.url).searchParams
  const page = query.get("page")

  const list: readonly Item[] = await prisma.itemDao.findMany({
    take: pageSize,
    skip: (page && (parseInt(page) - 1) * pageSize) || 0
  })
  return Response.json(list)
}
