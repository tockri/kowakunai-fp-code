import type { AsyncFunc, Func } from "./function"

export const compose =
  <V1, V2, V3>(func1: Func<V1, V2>, func2: Func<V2, V3>): Func<V1, V3> =>
  (value1) =>
    func2(func1(value1))

export function pipe<T1, T2, T3>(
  func1: Func<T1, T2>,
  func2: Func<T2, T3>
): Func<T1, T3>
export function pipe<T1, T2, T3, T4>(
  func1: Func<T1, T2>,
  func2: Func<T2, T3>,
  func3: Func<T3, T4>
): Func<T1, T4>
export function pipe<T1, T2, T3, T4, T5>(
  func1: Func<T1, T2>,
  func2: Func<T2, T3>,
  func3: Func<T3, T4>,
  func4: Func<T4, T5>
): Func<T1, T5>
export function pipe<T1, T2, T3, T4, T5, T6>(
  func1: Func<T1, T2>,
  func2: Func<T2, T3>,
  func3: Func<T3, T4>,
  func4: Func<T4, T5>,
  func5: Func<T5, T6>
): Func<T1, T6>
export function pipe<T1, T2, T3, T4, T5, T6, T7>(
  func1: Func<T1, T2>,
  func2: Func<T2, T3>,
  func3: Func<T3, T4>,
  func4: Func<T4, T5>,
  func5: Func<T5, T6>,
  func6: Func<T6, T7>
): Func<T1, T7>

/* eslint-disable @typescript-eslint/no-explicit-any */
export function pipe(...funcs: readonly Func<any, any>[]): Func<any, any> {
  return (arg) => funcs.reduce((acc, func) => func(acc), arg)
}

export function pipeAsync<T1, T2, T3>(
  func1: AsyncFunc<T1, T2>,
  func2: AsyncFunc<T2, T3>
): Func<T1, Promise<T3>>
export function pipeAsync<T1, T2, T3, T4>(
  func1: AsyncFunc<T1, T2>,
  func2: AsyncFunc<T2, T3>,
  func3: AsyncFunc<T3, T4>
): Func<T1, Promise<T4>>
export function pipeAsync<T1, T2, T3, T4, T5>(
  func1: AsyncFunc<T1, T2>,
  func2: AsyncFunc<T2, T3>,
  func3: AsyncFunc<T3, T4>,
  func4: AsyncFunc<T4, T5>
): Func<T1, Promise<T5>>
export function pipeAsync<T1, T2, T3, T4, T5, T6>(
  func1: AsyncFunc<T1, T2>,
  func2: AsyncFunc<T2, T3>,
  func3: AsyncFunc<T3, T4>,
  func4: AsyncFunc<T4, T5>,
  func5: AsyncFunc<T5, T6>
): Func<T1, Promise<T6>>
export function pipeAsync<T1, T2, T3, T4, T5, T6, T7>(
  func1: AsyncFunc<T1, T2>,
  func2: AsyncFunc<T2, T3>,
  func3: AsyncFunc<T3, T4>,
  func4: AsyncFunc<T4, T5>,
  func5: AsyncFunc<T5, T6>,
  func6: AsyncFunc<T6, T7>
): Func<T1, Promise<T7>>

/* eslint-disable @typescript-eslint/no-explicit-any */
export function pipeAsync(
  ...funcs: readonly AsyncFunc<any, any>[]
): Func<any, Promise<any>> {
  return (arg) => funcs.reduce(async (acc, func) => func(await acc), arg)
}
