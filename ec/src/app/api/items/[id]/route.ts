import prisma from "../../../../lib/prisma"
import { Item } from "../types"

type Segments = {
  params: {
    id: string
  }
}

export const GET = async (req: Request, seg: Segments) => {
  const id = seg.params.id && parseInt(seg.params.id)
  if (!id) {
    return new Response("not found.", {
      status: 404
    })
  }
  const item: Item | null = await prisma.itemDao.findUnique({
    where: {
      id
    }
  })
  return Response.json(item)
}
