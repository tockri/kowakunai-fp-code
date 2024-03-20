import { NextRequest, NextResponse } from "next/server"
import { BadRequestException, NotFoundException } from "../../lib/exceptions"

export type ApiFunc<ParamsT = Record<string, string>, S = any> = (
  req: NextRequest,
  params: ParamsT
) => S | Promise<S>

export const RestApi =
  <ParamsT = Record<string, string>, S = any>(func: ApiFunc<ParamsT, S>) =>
  async (req: NextRequest) => {
    const url = new URL(req.url)

    const params: Record<string, string> = {}
    for (const [key, value] of url.searchParams.entries()) {
      params[key] = value
    }
    try {
      const ret = await func(req, params as ParamsT)
      if (ret) {
        return NextResponse.json(ret)
      } else {
        return new NextResponse("OK", {
          status: 200
        })
      }
    } catch (e) {
      if (e instanceof NotFoundException) {
        return new NextResponse(e.message, {
          status: 404
        })
      } else if (e instanceof BadRequestException) {
        return new NextResponse(e.message, {
          status: 400
        })
      } else {
        return new NextResponse("Internal Error", {
          status: 500
        })
      }
    }
  }
