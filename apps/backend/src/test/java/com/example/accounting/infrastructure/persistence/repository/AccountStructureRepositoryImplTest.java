package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.domain.model.account.AccountStructure;
import com.example.accounting.infrastructure.persistence.entity.AccountStructureEntity;
import com.example.accounting.infrastructure.persistence.mapper.AccountStructureMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountStructureRepositoryImpl 単体テスト")
class AccountStructureRepositoryImplTest {

    @Mock
    private AccountStructureMapper mapper;

    private AccountStructureRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new AccountStructureRepositoryImpl(mapper);
    }

    @Test
    @DisplayName("save で新規登録時は insert を呼ぶ")
    void saveNewStructureShouldInsert() {
        AccountStructure structure = AccountStructure.reconstruct("1100", "1000~1100", 2, "1000", 1);
        when(mapper.existsByCode("1100")).thenReturn(false);
        when(mapper.findByCode("1100")).thenReturn(Optional.of(createEntity("1100", "1000~1100", 2, "1000", 1)));

        AccountStructure result = repository.save(structure).getOrNull();

        assertThat(result).isNotNull();
        verify(mapper).insert(any(AccountStructureEntity.class));
    }

    @Test
    @DisplayName("save で既存更新時は update を呼ぶ")
    void saveExistingStructureShouldUpdate() {
        AccountStructure structure = AccountStructure.reconstruct("1100", "1000~1100", 2, "1000", 1);
        when(mapper.existsByCode("1100")).thenReturn(true);
        when(mapper.findByCode("1100")).thenReturn(Optional.of(createEntity("1100", "1000~1100", 2, "1000", 1)));

        AccountStructure result = repository.save(structure).getOrNull();

        assertThat(result).isNotNull();
        verify(mapper).update(any(AccountStructureEntity.class));
    }

    @Test
    @DisplayName("findByCode で見つかる場合は値を返す")
    void findByCodeFoundShouldReturnPresent() {
        when(mapper.findByCode("1000")).thenReturn(Optional.of(createEntity("1000", "1000", 1, null, 1)));

        Optional<AccountStructure> result = repository.findByCode("1000").getOrNull();

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getAccountCode()).isEqualTo("1000");
    }

    @Test
    @DisplayName("findByCode で見つからない場合は empty を返す")
    void findByCodeNotFoundShouldReturnEmpty() {
        when(mapper.findByCode("9999")).thenReturn(Optional.empty());

        Optional<AccountStructure> result = repository.findByCode("9999").getOrNull();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll ですべて返す")
    void findAllShouldReturnAllStructures() {
        when(mapper.findAll()).thenReturn(List.of(
                createEntity("1000", "1000", 1, null, 1),
                createEntity("1100", "1000~1100", 2, "1000", 2)
        ));

        List<AccountStructure> result = repository.findAll().getOrNull();

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getAccountCode()).isEqualTo("1100");
    }

    @Test
    @DisplayName("findByParentCode で子階層を返す")
    void findByParentCodeShouldReturnChildren() {
        when(mapper.findByParentCode("1000")).thenReturn(List.of(
                createEntity("1100", "1000~1100", 2, "1000", 1),
                createEntity("1200", "1000~1200", 2, "1000", 2)
        ));

        List<AccountStructure> result = repository.findByParentCode("1000").getOrNull();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AccountStructure::getParentAccountCode).containsOnly("1000");
    }

    @Test
    @DisplayName("deleteByCode で mapper を呼ぶ")
    void deleteByCodeShouldCallMapper() {
        repository.deleteByCode("1100");

        verify(mapper).deleteByCode("1100");
    }

    @Test
    @DisplayName("existsByCode の結果を返す")
    void existsByCodeShouldReturnResult() {
        when(mapper.existsByCode("1000")).thenReturn(true);

        Boolean result = repository.existsByCode("1000").getOrNull();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("循環参照がある場合は true を返す")
    void hasCircularReferenceWithCircularShouldReturnTrue() {
        when(mapper.findByCode("1100")).thenReturn(Optional.of(createEntity("1100", "1000~1100", 2, "1000", 1)));

        Boolean result = repository.hasCircularReference("1000", "1100").getOrNull();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("循環参照がない場合は false を返す")
    void hasCircularReferenceWithNoCircularShouldReturnFalse() {
        when(mapper.findByCode("2100")).thenReturn(Optional.of(createEntity("2100", "2000~2100", 2, "2000", 1)));

        Boolean result = repository.hasCircularReference("1000", "2100").getOrNull();

        assertThat(result).isFalse();
    }

    private AccountStructureEntity createEntity(String code, String path, int level, String parentCode, int order) {
        AccountStructureEntity entity = new AccountStructureEntity();
        entity.setAccountCode(code);
        entity.setAccountPath(path);
        entity.setHierarchyLevel(level);
        entity.setParentAccountCode(parentCode);
        entity.setDisplayOrder(order);
        return entity;
    }
}
