class: middle, center

# Result

---

## Billion-Dollar Mistake

> I call it my billion-dollar mistake. It was the invention of the **null reference** in 1965. At that time, I was designing the first comprehensive type system for references in an object oriented language (ALGOL W). My goal was to ensure that all use of references should be absolutely safe, with checking performed automatically by the compiler. But I couldn't resist the temptation to put in a null reference, simply because it was so easy to implement. This has led to innumerable errors, vulnerabilities, and system crashes, which have probably caused a billion dollars of pain and damage in the last forty years.

???

* Tony Hoare
* quicksort / Hoare Logic / Communicating Sequential Processes (CSP)
* QCon London 2009

---
class: middle, center

## `null`

???
What is the problem with `null`

* `NullPointerException`
* Null checks
* Signal failure

---

```java
Id userId = Id(37);
User user = repository.getBy(userId);
```

???

Let's notify a user.

Can you now use user to send a notification?

No! It could be null.

But what does that mean?

* Could the connection to the database not be established
* Is the table is present?
* Is the user unknown?
* Bug?

---
class: middle, center

## Kotlin

???

* Rich type system: `T` vs `T?`
* syntax for `T?` is jarring
* Checked Exception

---
class: middle, center

## `Result`
### ... explicit about success and failure

???

Created to get help from the compiler and prevent

* using null
* checks
* throwing exceptions
* use of jarring syntax

---

## API

```kotlin
sealed class Result<Error, Value>
```

???

* **sealed class**; open class with known subclasses
* **Error**; generic parameter of which mistakes
* **Value**; generic parameter for computed values

E.g.

Fetching user from database
* Value : user
* Error : FetchError
---

### Creation

```kotlin
data class Success<Error, Value>(val data: Value)
    : Result<Error, Value>() {
```

```kotlin
data class Failure<Error, Value>(val error: Error)
    : Result<Error, Value>() {
```

???

How to create instances of `Result`?

---

### Chaining

```kotlin
fun <T> map(transform: (Value) -> T): Result<Error, T>
```

```kotlin
fun <T> mapError(transform: (Error) -> T): Result<T, Value>
```

???

Transform a result

---

### Chaining (Continued)

```kotlin
fun <T> andThen(chain: (Value) -> Result<Error, T>)
    : Result<Error, T>
```

```kotlin
fun <T> andThenError(chain: (Error) -> Result<T, Value>)
    : Result<T, Value>
```

???

Chain a computation

Corresponds with `flatMap`

---

### Chaining (Continued)

```kotlin
fun use(actOn: (Value) -> Unit): Result<Error, Value>
```

```kotlin
fun useError(actOn: (Error) -> Unit): Result<Error, Value>
```

???

Use a result

---

### Unwrapping

```kotlin
fun withDefault(defaultValue: Value): Value
```

```kotlin
fun withDefault(producer: (Error) -> Value): Value
```

```kotlin
fun orThrowException(exceptionProducer: (Error) -> Exception): Value
```

???

Je kunt de dans wel

Lang ontspringen

Tot het moment en dat is nu

Dat je toch eens dansen moet

---

## Throwing dice

*Dice notation*

> a system to represent different combinations of dice in wargames and tabletop role-playing games using simple algebra-like notation such as 2d6+12.

???

We will restrict ourselves to `<m>d<n>`.

---

```kotlin
"3d6"
    .toDice()
    .andThen(Dice::roll)
    .use { pips -> println("$input threw $pips")}
    .useError { error -> println("could not roll dice: $error")}
```

---

```kotlin
fun String.toDice(): Result<DiceError, Dice> {
    val matcher = dicePattern.matcher(this)
    return if (matcher.matches()) {
        val number = matcher.group(1).toInt()
        val faces = matcher.group(2).toInt()
        return Success(Dice(number, faces))
    } else {
        Failure(ParseError(this))
    }
}
private val dicePattern: Pattern =
    Pattern.compile("^([1-9]\\d*)d([1-9]\\d*)$")
```

---

```kotlin
    private fun single(): Result<DiceError, Int> {
        return source.integer()
            .mapError(::RandomSourceError)
            .map { n -> n.modulo(faces) }
            .map { n -> n + 1 }
    }
```

---

```kotlin
    fun roll(): Result<DiceError, Int> {
        return List(number) { single() }
            .fold(Success(emptyList()), ::combine)
            .map { xs -> xs.sum() }
    }
```

--

```kotlin
List<Result<DiceError, Int>> -> Result<DiceError, List<Int>>
```

---

```kotline
fun combine(
    accumulator: Result<DiceError, List<Int>>,
    element: Result<DiceError, Int>
): Result<DiceError, List<Int>> {
    return accumulator.andThen { xs ->
        element.map { x -> xs + x }
    }
}
```

---

## What about?

* Exceptions
* `Kotlin.Result`
* Λrrow

---

## Exceptions

> Exceptions are controversial

???

* Checked exception should be handled locally, but that often does not happen or is impossible
* Unchecked exception do not have to be declared
* Sometimes exceptions are not exceptional
* Syntax is jarring

---

## `Kotlin.Result`

> A discriminated union that encapsulates a successful outcome with a value of type `T` or a failure with an arbitrary `Throwable` exception.

???

* We do not want to restrict failure to `Throwable`
* Api allows checking, breaks law of demeter

---

## Λrrow

> Functional companion to Kotlin's Standard Library

???

* `Result` is Λrrows `Either`
* `Result` has a friendlier API
* Avoids scary words lik monoid, functor and monad
* `Result` is a gateway drug for Λ, or similar libraries

---
class: middle, center

## Questions

???

* Do you look at the types?