import prisma from "@/lib/prisma"

export const GET = async (req: Request) => {
  const list = await prisma.itemDao.findMany()
  return Response.json(list)
}
