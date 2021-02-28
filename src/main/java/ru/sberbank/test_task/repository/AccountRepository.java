package ru.sberbank.test_task.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.test_task.model.Account;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    boolean existsByOpenDateBeforeAndAccountNumber(LocalDate localDate, String accountNumber);

    @Query("FROM Account ac WHERE ac.accountNumber = :accountNumber AND ac.openDate < :date " +
            "AND (ac.closeDate >= :date OR ac.closeDate IS null)")
    List<Account> findAllByDateAndAccountNumber(@Param("date") LocalDate date,
                                                @Param("accountNumber") String accountNumber);

}
