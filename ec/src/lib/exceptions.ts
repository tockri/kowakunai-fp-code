export class NotFoundException extends Error {
  constructor(objectName: string, cause?: Error) {
    super(`${objectName} not found`, { cause })
  }
}

export class BadRequestException extends Error {
  constructor(message: string, cause?: Error) {
    super(message, { cause })
  }
}
