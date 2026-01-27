package com.example.accounting.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OptimisticLockException")
class OptimisticLockExceptionTest {

    @Test
    @DisplayName("メッセージを保持できる")
    void shouldHoldMessage() {
        OptimisticLockException exception = new OptimisticLockException("ロック");

        assertThat(exception).hasMessage("ロック");
    }

    @Test
    @DisplayName("RuntimeException を継承している")
    void shouldExtendRuntimeException() {
        OptimisticLockException exception = new OptimisticLockException("ロック");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
