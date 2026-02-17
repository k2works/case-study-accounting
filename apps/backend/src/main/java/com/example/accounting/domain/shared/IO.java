package com.example.accounting.domain.shared;

import io.vavr.control.Either;
import io.vavr.control.Try;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * IO モナド - 副作用を持つ計算の記述を表す型
 *
 * <p>IO 値を作成しただけでは副作用は発生しない。
 * {@link #unsafeRun()} で実際に実行される。</p>
 *
 * <p>関数型プログラミングにおける副作用の管理パターンを提供する。</p>
 *
 * @param <A> 計算結果の型
 */
public final class IO<A> {

    private final Supplier<A> thunk;

    private IO(Supplier<A> thunk) {
        this.thunk = thunk;
    }

    /**
     * 副作用のある式を遅延実行する IO を作成
     *
     * @param supplier 副作用を持つ式
     * @param <A>      結果の型
     * @return IO インスタンス
     */
    public static <A> IO<A> delay(Supplier<A> supplier) {
        return new IO<>(supplier);
    }

    /**
     * 既存の値をラップ（副作用なし）
     *
     * @param value 値
     * @param <A>   値の型
     * @return IO インスタンス
     */
    public static <A> IO<A> pure(A value) {
        return new IO<>(() -> value);
    }

    /**
     * 何もしない IO（Unit を返す）
     *
     * @return IO インスタンス
     */
    public static IO<Void> unit() {
        return new IO<>(() -> null);
    }

    /**
     * 副作用のみを実行する IO を作成
     *
     * @param runnable 副作用
     * @return IO インスタンス
     */
    @SuppressWarnings("PMD.AvoidReturningNull")
    public static IO<Void> effect(Runnable runnable) {
        return new IO<>(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * IO を実行して結果を取得（副作用が発生する）
     *
     * @return 計算結果
     */
    public A unsafeRun() {
        return thunk.get();
    }

    /**
     * IO を安全に実行して Try でラップ
     *
     * @return Try でラップされた結果
     */
    public Try<A> safeRun() {
        return Try.of(thunk::get);
    }

    /**
     * 結果を変換する
     *
     * @param f   変換関数
     * @param <B> 変換後の型
     * @return 変換された IO
     */
    public <B> IO<B> map(Function<A, B> f) {
        return new IO<>(() -> f.apply(thunk.get()));
    }

    /**
     * IO を返す関数を適用してフラット化
     *
     * @param f   IO を返す関数
     * @param <B> 結果の型
     * @return フラット化された IO
     */
    public <B> IO<B> flatMap(Function<A, IO<B>> f) {
        return new IO<>(() -> f.apply(thunk.get()).unsafeRun());
    }

    /**
     * 失敗時のフォールバック値を指定
     *
     * @param defaultValue フォールバック値
     * @return フォールバック付き IO
     */
    public IO<A> orElse(A defaultValue) {
        return new IO<>(() -> {
            try {
                return thunk.get();
            } catch (Exception _) {
                return defaultValue;
            }
        });
    }

    /**
     * 失敗時のフォールバック IO を指定
     *
     * @param fallback フォールバック IO
     * @return フォールバック付き IO
     */
    public IO<A> orElse(Supplier<IO<A>> fallback) {
        return new IO<>(() -> {
            try {
                return thunk.get();
            } catch (Exception _) {
                return fallback.get().unsafeRun();
            }
        });
    }

    /**
     * 指定回数リトライ
     *
     * @param maxRetries 最大リトライ回数
     * @return リトライ付き IO
     */
    public IO<A> retry(int maxRetries) {
        return IntStream.range(0, maxRetries)
                .boxed()
                .reduce(this, (result, _) -> {
                    IO<A> current = result;
                    return result.orElse(() -> current);
                }, (a, b) -> b);
    }

    /**
     * IO を Either に変換（成功/失敗を型で表現）
     *
     * @return Either でラップされた IO
     */
    public IO<Either<Throwable, A>> attempt() {
        return new IO<>(() -> {
            try {
                return Either.right(thunk.get());
            } catch (Exception e) {
                return Either.left(e);
            }
        });
    }

    /**
     * 2つの IO を順番に実行し、両方の結果を組み合わせる
     *
     * @param other 2番目の IO
     * @param f     結果を組み合わせる関数
     * @param <B>   2番目の IO の結果型
     * @param <C>   組み合わせ後の型
     * @return 組み合わせられた IO
     */
    public <B, C> IO<C> zipWith(IO<B> other, java.util.function.BiFunction<A, B, C> f) {
        return this.flatMap(a -> other.map(b -> f.apply(a, b)));
    }
}
