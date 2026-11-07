package ro.challenge.coremaker.entry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntryRepository extends JpaRepository<EntryEntity, Long> {

    // Account history, newest first (nice to have)
    List<EntryEntity> findByAccountIdOrderByIdDesc(String accountId, Pageable pageable);
}
