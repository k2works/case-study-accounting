package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.DeleteAccountStructureCommand;
import com.example.accounting.application.port.out.AccountStructureRepository;
import com.example.accounting.application.port.out.DeleteAccountStructureResult;
import com.example.accounting.domain.model.account.AccountStructure;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("勘定科目構成削除サービス")
class DeleteAccountStructureServiceTest {

    @Mock
    private AccountStructureRepository accountStructureRepository;

    private DeleteAccountStructureService service;

    @BeforeEach
    void setUp() {
        service = new DeleteAccountStructureService(accountStructureRepository);
    }

    @Test
    @DisplayName("有効な削除で構成を削除できる")
    void executeWithValidDeleteShouldDeleteStructure() {
        DeleteAccountStructureCommand command = DeleteAccountStructureCommand.of("1100").get();
        AccountStructure structure = AccountStructure.reconstruct("1100", "1000~1100", 2, "1000", 1);

        when(accountStructureRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(structure)));
        when(accountStructureRepository.findByParentCode("1100")).thenReturn(Try.success(List.of()));
        when(accountStructureRepository.deleteByCode("1100")).thenReturn(Try.success(null));

        DeleteAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isTrue();
        assertThat(result.accountCode()).isEqualTo("1100");
        assertThat(result.message()).isEqualTo("勘定科目構成を削除しました");
        assertThat(result.errorMessage()).isNull();
        verify(accountStructureRepository).deleteByCode("1100");
    }

    @Test
    @DisplayName("既存構成がない場合は失敗する")
    void executeWithNonExistentStructureShouldReturnFailure() {
        DeleteAccountStructureCommand command = DeleteAccountStructureCommand.of("1100").get();

        when(accountStructureRepository.findByCode("1100")).thenReturn(Try.success(Optional.empty()));

        DeleteAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("勘定科目構成が見つかりません");
        verify(accountStructureRepository, never()).deleteByCode("1100");
    }

    @Test
    @DisplayName("子階層が存在する場合は失敗する")
    void executeWithChildrenShouldReturnFailure() {
        DeleteAccountStructureCommand command = DeleteAccountStructureCommand.of("1100").get();
        AccountStructure structure = AccountStructure.reconstruct("1100", "1000~1100", 2, "1000", 1);
        AccountStructure child = AccountStructure.reconstruct("1110", "1000~1100~1110", 3, "1100", 1);

        when(accountStructureRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(structure)));
        when(accountStructureRepository.findByParentCode("1100")).thenReturn(Try.success(List.of(child)));

        DeleteAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("子階層が存在するため削除できません");
        verify(accountStructureRepository, never()).deleteByCode("1100");
    }
}
