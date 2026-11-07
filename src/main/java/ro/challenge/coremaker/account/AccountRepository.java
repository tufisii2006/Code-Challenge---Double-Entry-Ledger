package ro.challenge.coremaker.account;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
public interface AccountRepository extends JpaRepository<AccountEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a where a.id in :ids order by a.id")
    List<AccountEntity> findAllForUpdate(@Param("ids") Collection<String> ids);

}
