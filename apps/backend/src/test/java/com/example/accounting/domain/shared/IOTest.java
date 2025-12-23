package com.example.accounting.domain.shared;

import io.vavr.control.Either;
import io.vavr.control.Try;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IO モナド")
class IOTest {

    @Nested
    @DisplayName("delay")
    class Delay {

        @Test
        @DisplayName("副作用は unsafeRun まで遅延される")
        void shouldDeferExecutionUntilUnsafeRun() {
            AtomicInteger counter = new AtomicInteger(0);

            IO<Integer> io = IO.delay(counter::incrementAndGet);

            assertThat(counter.get()).isZero();
            assertThat(io.unsafeRun()).isEqualTo(1);
            assertThat(counter.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("pure")
    class Pure {

        @Test
        @DisplayName("値をラップして返す")
        void shouldWrapValue() {
            IO<String> io = IO.pure("hello");

            assertThat(io.unsafeRun()).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("unit")
    class Unit {

        @Test
        @DisplayName("null を返す")
        void shouldReturnNull() {
            IO<Void> io = IO.unit();

            assertThat(io.unsafeRun()).isNull();
        }
    }

    @Nested
    @DisplayName("effect")
    class Effect {

        @Test
        @DisplayName("副作用を実行して null を返す")
        void shouldExecuteSideEffectAndReturnNull() {
            AtomicInteger counter = new AtomicInteger(0);

            IO<Void> io = IO.effect(counter::incrementAndGet);

            assertThat(counter.get()).isZero();
            assertThat(io.unsafeRun()).isNull();
            assertThat(counter.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("safeRun")
    class SafeRun {

        @Test
        @DisplayName("成功時は Success を返す")
        void shouldReturnSuccessOnSuccess() {
            IO<Integer> io = IO.pure(42);

            Try<Integer> result = io.safeRun();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("失敗時は Failure を返す")
        void shouldReturnFailureOnError() {
            IO<Integer> io = IO.delay(() -> {
                throw new RuntimeException("error");
            });

            Try<Integer> result = io.safeRun();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).hasMessage("error");
        }
    }

    @Nested
    @DisplayName("map")
    class Map {

        @Test
        @DisplayName("結果を変換する")
        void shouldTransformResult() {
            IO<Integer> io = IO.pure(5).map(n -> n * 2);

            assertThat(io.unsafeRun()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("flatMap")
    class FlatMap {

        @Test
        @DisplayName("IO を返す関数を適用してフラット化する")
        void shouldFlattenNestedIO() {
            IO<Integer> io = IO.pure(5)
                    .flatMap(n -> IO.pure(n * 2));

            assertThat(io.unsafeRun()).isEqualTo(10);
        }

        @Test
        @DisplayName("連鎖的に処理できる")
        void shouldChainOperations() {
            AtomicInteger counter = new AtomicInteger(0);

            IO<Integer> io = IO.delay(counter::incrementAndGet)
                    .flatMap(n -> IO.delay(() -> n + counter.incrementAndGet()));

            assertThat(io.unsafeRun()).isEqualTo(3);
            assertThat(counter.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("orElse (値)")
    class OrElseValue {

        @Test
        @DisplayName("成功時は元の値を返す")
        void shouldReturnOriginalValueOnSuccess() {
            IO<String> io = IO.pure("success").orElse("fallback");

            assertThat(io.unsafeRun()).isEqualTo("success");
        }

        @Test
        @DisplayName("失敗時はフォールバック値を返す")
        void shouldReturnFallbackOnError() {
            IO<String> io = IO.<String>delay(() -> {
                throw new RuntimeException("error");
            }).orElse("fallback");

            assertThat(io.unsafeRun()).isEqualTo("fallback");
        }
    }

    @Nested
    @DisplayName("orElse (IO)")
    class OrElseIO {

        @Test
        @DisplayName("成功時は元の IO の結果を返す")
        void shouldReturnOriginalIOResultOnSuccess() {
            IO<String> io = IO.pure("success")
                    .orElse(() -> IO.pure("fallback"));

            assertThat(io.unsafeRun()).isEqualTo("success");
        }

        @Test
        @DisplayName("失敗時はフォールバック IO の結果を返す")
        void shouldReturnFallbackIOResultOnError() {
            IO<String> io = IO.<String>delay(() -> {
                throw new RuntimeException("error");
            }).orElse(() -> IO.pure("fallback"));

            assertThat(io.unsafeRun()).isEqualTo("fallback");
        }
    }

    @Nested
    @DisplayName("retry")
    class Retry {

        @Test
        @DisplayName("成功時はリトライしない")
        void shouldNotRetryOnSuccess() {
            AtomicInteger attempts = new AtomicInteger(0);

            IO<Integer> io = IO.delay(() -> {
                attempts.incrementAndGet();
                return 42;
            }).retry(3);

            assertThat(io.unsafeRun()).isEqualTo(42);
            assertThat(attempts.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("失敗時にリトライする")
        void shouldRetryOnFailure() {
            AtomicInteger attempts = new AtomicInteger(0);

            IO<Integer> io = IO.delay(() -> {
                int attempt = attempts.incrementAndGet();
                if (attempt < 3) {
                    throw new RuntimeException("error");
                }
                return 42;
            }).retry(3);

            assertThat(io.unsafeRun()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("attempt")
    class Attempt {

        @Test
        @DisplayName("成功時は Right を返す")
        void shouldReturnRightOnSuccess() {
            IO<Either<Throwable, Integer>> io = IO.pure(42).attempt();

            Either<Throwable, Integer> result = io.unsafeRun();

            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("失敗時は Left を返す")
        void shouldReturnLeftOnError() {
            RuntimeException error = new RuntimeException("error");
            IO<Either<Throwable, Integer>> io = IO.<Integer>delay(() -> {
                throw error;
            }).attempt();

            Either<Throwable, Integer> result = io.unsafeRun();

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(error);
        }
    }

    @Nested
    @DisplayName("zipWith")
    class ZipWith {

        @Test
        @DisplayName("2つの IO の結果を組み合わせる")
        void shouldCombineTwoIOResults() {
            IO<Integer> io1 = IO.pure(2);
            IO<Integer> io2 = IO.pure(3);

            IO<Integer> combined = io1.zipWith(io2, (a, b) -> a + b);

            assertThat(combined.unsafeRun()).isEqualTo(5);
        }

        @Test
        @DisplayName("順番に実行される")
        void shouldExecuteInOrder() {
            AtomicInteger counter = new AtomicInteger(0);
            IO<Integer> io1 = IO.delay(counter::incrementAndGet);
            IO<Integer> io2 = IO.delay(counter::incrementAndGet);

            IO<Integer> combined = io1.zipWith(io2, (a, b) -> a * 10 + b);

            assertThat(combined.unsafeRun()).isEqualTo(12);
        }
    }
}
