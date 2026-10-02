package edu.cibertec.account.mapper;

import edu.cibertec.account.domain.entity.Account;
import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "idStatus", source = "statusId")
    Account toEntity(AccountRequest request, Long statusId);

    @Mapping(target = "idStatus", source = "statusId")
    void updateEntity(@MappingTarget Account account, AccountRequest request, Long statusId);

    @Mapping(target = "status", source = "statusName")
    AccountResponse toResponse(Account account, String statusName);
}
