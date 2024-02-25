export type Func<T, S> = (t: T) => S

export type AsyncFunc<T, S> = (t: T) => S | Promise<S>
