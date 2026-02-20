package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import com.example.accounting.infrastructure.persistence.entity.AutoJournalPatternEntity;
import com.example.accounting.infrastructure.persistence.mapper.AutoJournalPatternMapper;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("自動仕訳パターンリポジトリ実装")
class AutoJournalPatternRepositoryImplTest {

    @Mock
    private AutoJournalPatternMapper autoJournalPatternMapper;

    private AutoJournalPatternRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new AutoJournalPatternRepositoryImpl(autoJournalPatternMapper);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("新規パターンを保存できる")
        void shouldInsertNewPattern() {
            AutoJournalPattern pattern = AutoJournalPattern.create("AP001", "売上計上", "sales", "説明");
            AutoJournalPatternEntity savedEntity = new AutoJournalPatternEntity();
            savedEntity.setId(1L);
            savedEntity.setPatternCode("AP001");
            savedEntity.setPatternName("売上計上");
            savedEntity.setSourceTableName("sales");
            savedEntity.setDescription("説明");
            savedEntity.setIsActive(true);

            // MyBatis useGeneratedKeys がinsert時にIDをセットする動作を再現
            doAnswer(invocation -> {
                AutoJournalPatternEntity e = invocation.getArgument(0);
                e.setId(1L);
                return null;
            }).when(autoJournalPatternMapper).insert(any(AutoJournalPatternEntity.class));
            when(autoJournalPatternMapper.findById(1L)).thenReturn(Optional.of(savedEntity));

            Try<AutoJournalPattern> result = repository.save(pattern);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get().getPatternCode()).isEqualTo("AP001");
            verify(autoJournalPatternMapper).insert(any(AutoJournalPatternEntity.class));
            verify(autoJournalPatternMapper, never()).update(any(AutoJournalPatternEntity.class));
        }

        @Test
        @DisplayName("既存パターンを更新できる")
        void shouldUpdateExistingPattern() {
            AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                    AutoJournalPatternId.of(1L), "AP001", "売上計上（更新）", "sales", "新説明", true,
                    List.of(AutoJournalPatternItem.reconstruct(1, "D", "1100", "amount", "売上"))
            );
            AutoJournalPatternEntity existingEntity = new AutoJournalPatternEntity();
            existingEntity.setId(1L);
            existingEntity.setPatternCode("AP001");

            AutoJournalPatternEntity updatedEntity = new AutoJournalPatternEntity();
            updatedEntity.setId(1L);
            updatedEntity.setPatternCode("AP001");
            updatedEntity.setPatternName("売上計上（更新）");
            updatedEntity.setSourceTableName("sales");
            updatedEntity.setDescription("新説明");
            updatedEntity.setIsActive(true);

            when(autoJournalPatternMapper.findById(1L))
                    .thenReturn(Optional.of(existingEntity))
                    .thenReturn(Optional.of(updatedEntity));

            Try<AutoJournalPattern> result = repository.save(pattern);

            assertThat(result.isSuccess()).isTrue();
            verify(autoJournalPatternMapper).update(any(AutoJournalPatternEntity.class));
            verify(autoJournalPatternMapper).deleteItems(1L);
            verify(autoJournalPatternMapper).insertItems(anyList());
        }

        @Test
        @DisplayName("明細なしのパターンを保存できる")
        void shouldSavePatternWithNoItems() {
            AutoJournalPattern pattern = AutoJournalPattern.create("AP001", "売上計上", "sales", null);
            AutoJournalPatternEntity savedEntity = new AutoJournalPatternEntity();
            savedEntity.setId(1L);
            savedEntity.setPatternCode("AP001");
            savedEntity.setPatternName("売上計上");
            savedEntity.setSourceTableName("sales");
            savedEntity.setIsActive(true);

            // MyBatis useGeneratedKeys がinsert時にIDをセットする動作を再現
            doAnswer(invocation -> {
                AutoJournalPatternEntity e = invocation.getArgument(0);
                e.setId(1L);
                return null;
            }).when(autoJournalPatternMapper).insert(any(AutoJournalPatternEntity.class));
            when(autoJournalPatternMapper.findById(1L)).thenReturn(Optional.of(savedEntity));

            Try<AutoJournalPattern> result = repository.save(pattern);

            assertThat(result.isSuccess()).isTrue();
            verify(autoJournalPatternMapper).insert(any(AutoJournalPatternEntity.class));
            verify(autoJournalPatternMapper, never()).insertItems(anyList());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("存在する場合はパターンを返す")
        void shouldReturnPatternWhenFound() {
            AutoJournalPatternEntity entity = new AutoJournalPatternEntity();
            entity.setId(1L);
            entity.setPatternCode("AP001");
            entity.setPatternName("売上計上");
            entity.setSourceTableName("sales");
            entity.setIsActive(true);

            when(autoJournalPatternMapper.findById(1L)).thenReturn(Optional.of(entity));

            Try<Optional<AutoJournalPattern>> result = repository.findById(AutoJournalPatternId.of(1L));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isPresent();
            assertThat(result.get().get().getPatternCode()).isEqualTo("AP001");
        }

        @Test
        @DisplayName("存在しない場合は空を返す")
        void shouldReturnEmptyWhenNotFound() {
            when(autoJournalPatternMapper.findById(999L)).thenReturn(Optional.empty());

            Try<Optional<AutoJournalPattern>> result = repository.findById(AutoJournalPatternId.of(999L));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("全パターンを返す")
        void shouldReturnAllPatterns() {
            AutoJournalPatternEntity entity = new AutoJournalPatternEntity();
            entity.setId(1L);
            entity.setPatternCode("AP001");
            entity.setPatternName("売上計上");
            entity.setSourceTableName("sales");
            entity.setIsActive(true);

            when(autoJournalPatternMapper.findAll()).thenReturn(List.of(entity));

            Try<List<AutoJournalPattern>> result = repository.findAll();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).hasSize(1);
        }

        @Test
        @DisplayName("パターンがない場合は空リストを返す")
        void shouldReturnEmptyListWhenNoPatterns() {
            when(autoJournalPatternMapper.findAll()).thenReturn(List.of());

            Try<List<AutoJournalPattern>> result = repository.findAll();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("指定IDのパターンを削除できる")
        void shouldDeletePattern() {
            Try<Void> result = repository.deleteById(AutoJournalPatternId.of(1L));

            assertThat(result.isSuccess()).isTrue();
            verify(autoJournalPatternMapper).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("existsByCode")
    class ExistsByCode {

        @Test
        @DisplayName("存在する場合は true を返す")
        void shouldReturnTrueWhenExists() {
            when(autoJournalPatternMapper.existsByCode("AP001")).thenReturn(true);

            Try<Boolean> result = repository.existsByCode("AP001");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isTrue();
        }

        @Test
        @DisplayName("存在しない場合は false を返す")
        void shouldReturnFalseWhenNotExists() {
            when(autoJournalPatternMapper.existsByCode("AP999")).thenReturn(false);

            Try<Boolean> result = repository.existsByCode("AP999");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isFalse();
        }
    }
}
