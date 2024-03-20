import prisma from "../../../lib/prisma"
import { RestApi } from "../RestApi"
import { Item } from "./types"

const pageSize = 20

type GetParams = {
  page: string
}

export const GET = RestApi(async (req, params: GetParams) => {
  const page = parseInt(params.page || "0")
  const items: readonly Item[] = await prisma.itemDao.findMany({
    take: pageSize,
    skip: (page - 1) * pageSize
  })
  return { items }
})
