# Result 
A [Kotlin][kotlin] library that is explicit about success and failure.

### not under active development
The team in which the `Result` library originated has moved on to use the excellent [Arrow library][arrow]. Therefore `Result` is not under active development anymore. It still can serve as an example how to create, structure and foster an open source library.

## Rationale
Kotlin is explicit about which values can be `null`. This [null-safety][kotlin:docs:null-safety] is achieved via 
nullable and non-nullable types. The distinction is made for type `T` with a question mark: `T?` means that values
can be `null`, `T` does not allow `null` values.

Some developers use nullable types to signify that a computation could fail. A returned `null` signals that a
computation has failed. Even though Kotlin has [special syntax][kotlin:docs:safe-calls] to work with nullable types,
this is problematic for a number of reasons, among them

* The syntax for working with nullable types is markedly different from the syntax for regular methods.
* By using `null` to indicate failure we lose all context of what failure occurred.
* By using `null` clients of the computation are forced to do `null`-checks, cluttering the code.

These shortcomings resulted in creation of the *Result* library. It allows a developer to signal that a computation
might  fail and provide details about the failure.

### What about `kotlin.Result`?
The Kotlin standard library provides a [`kotlin.Result`][kotlin:docs:Result]. It has a similar intent as the *Result*
library but misses the mark for our use-cases. In particular, `kotlin.Result` failures are restricted to be `Throwable`.
*Result* library on the other hand does not restrict failures in any way.

Furthermore, `kotlin.Result` provides methods to inspect the actual kind of result. These invites developers to
constantly check if a result is a success or a failure, negating any benefits of wrapping the computation. *Result*
instead provides a rich interface to work with results without the need to know which kind of result it is.

## Design
The crux of the _Result_ library is the [sealed class][kotlin:docs:sealed-class] `Result<Error, Value>`. It has two
subclasses `Success` and `Failure`.

`Success` is a [data class][kotlin:docs:data-class] that contains the result of a successful computation as data.
`Failure` is a data class that contains the reason why a computation failed.

Although the sealed `Result` class allows one to [safely switch][kotlin:docs:when] over a result, that should be
avoided. Instead, one should use the various methods on `Result` to transform data into a desired shape.

## Documentation
The API of `Result` is documented on [our wiki][Result:docs:API]. The KDocs can be found on the
[website][Result:docs:KDoc].

## Development
This project uses [Gradle][gradle] as a build tool. To see which tasks are available run

```
./gradlew tasks
```

[kotlin]: https://kotlinlang.org/
[arrow]: https://arrow-kt.io/
[kotlin:docs:null-safety]: https://kotlinlang.org/docs/reference/null-safety.html
[kotlin:docs:safe-calls]: https://kotlinlang.org/docs/reference/null-safety.html#safe-calls
[kotlin:docs:Result]: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/
[kotlin:docs:sealed-class]: https://kotlinlang.org/docs/reference/sealed-classes.html
[kotlin:docs:data-class]: https://kotlinlang.org/docs/reference/data-classes.html
[kotlin:docs:when]: https://kotlinlang.org/docs/reference/control-flow.html#when-expression
[Result:docs:API]: https://github.com/alliander-opensource/Result/wiki/API
[Result:docs:KDoc]: https://alliander-opensource.github.io/Result/kdoc/result/index.html
[gradle]: https://gradle.org/
